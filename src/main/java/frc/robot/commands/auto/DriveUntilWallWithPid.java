package frc.robot.commands.auto;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.RobotContainer;
import frc.robot.subsystems.DriveTrain;
import frc.robot.subsystems.DepthWallRange;
import frc.robot.commands.driveCommands.DriveWithPID;

/**
 * Avança em linha reta com heading lock até a parede ficar a <= stopDistanceMeters.
 * Usa PID de distância APENAS como "trilho" (fail-safe: maxTravelMeters), e PID de yaw para heading.
 */
public class DriveUntilWallWithPid extends CommandBase {
    private static final DriveTrain drive = RobotContainer.driveTrain;
    private static final DepthWallRange wallRange = RobotContainer.wallRange;

    private final double stopDistanceMeters;     // Distância de parada em relação à parede (ex.: 0.60 m)
    private final Double holdYawDeg;             // Heading alvo absoluto; se null, usa yaw atual
    private final double maxTravelMeters;        // Percurso máximo (fail-safe), em metros

    private double startForward;
    private boolean finished = false;

    // Comando PID para controlar a distância
    private DriveWithPID driveWithPID;

    /** Construtor sem o yaw speed multiplier. */
    public DriveUntilWallWithPid(double stopDistanceMeters, Double holdYawDeg, double maxTravelMeters) {
        this.stopDistanceMeters = stopDistanceMeters;
        this.holdYawDeg = holdYawDeg;
        this.maxTravelMeters = maxTravelMeters;
        addRequirements(drive);
    }

    @Override
    public void initialize() {
        finished = false;

        drive.resetEncoders();
        drive.resetYaw();

        startForward = drive.getAverageForwardEncoderDistance();

        // Criando o comando PID para controle de distância e yaw
        driveWithPID = new DriveWithPID(startForward + maxTravelMeters, 0.5, holdYawDeg != null ? holdYawDeg : drive.getYaw(), 1.0);

        // Inicializa o comando PID
        driveWithPID.initialize();
    }

    @Override
    public void execute() {
        // Executa o comando PID de movimento
        driveWithPID.execute();

        // Atualiza filtro/estimativa do sensor de parede
        wallRange.update();

        // Critério de parada por parede
        if (wallRange.hasValidReading()) {
            double dist = wallRange.getFilteredDistanceMeters();
            System.out.println("Distância da parede: " + dist + " m");
            if (dist <= stopDistanceMeters) {
                finished = true;
                return;
            }
        }

        // Fail-safe por percurso máximo
        double traveled = Math.abs(drive.getAverageForwardEncoderDistance() - startForward);
        System.out.println("Distância percorrida: " + traveled + " m");
        if (traveled >= maxTravelMeters) {
            finished = true;
        }
    }

    @Override
    public void end(boolean interrupted) {
        drive.setDriveMotorSpeeds(0.0, 0.0, 0.0);
        driveWithPID.end(interrupted);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
