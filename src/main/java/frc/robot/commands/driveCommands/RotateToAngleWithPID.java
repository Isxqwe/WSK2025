package frc.robot.commands.driveCommands;

import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpiutil.math.MathUtil;
import frc.robot.RobotContainer;
import frc.robot.subsystems.DriveTrain;

public class RotateToAngleWithPID extends CommandBase {
    // Subsystem de DriveTrain
    private static final DriveTrain drive = RobotContainer.driveTrain;

    private double setpointAngle; // Ângulo desejado
    private PIDController pidZAxis; // Controlador PID para o ângulo

    public RotateToAngleWithPID(double setpointAngle, double epsilonYaw) {
        this.setpointAngle = setpointAngle;
        addRequirements(drive);

        // Inicializa o controlador PID para o yaw
        pidZAxis = new PIDController(0.05, 0, 0.005); // Ajuste os valores PID conforme necessário
        pidZAxis.setTolerance(epsilonYaw); // Define a tolerância para o erro no ângulo
    }

    @Override
    public void initialize() {
        drive.resetYaw(); // Resetar o yaw ao início do movimento
        pidZAxis.reset(); // Reseta o PID
    }

    @Override
    public void execute() {
        // Calcula o comando de rotação usando PID para o ângulo
        double rotationSpeed = pidZAxis.calculate(drive.getYaw(), setpointAngle);
        
        // Limita a velocidade de rotação para evitar movimentos excessivos
        rotationSpeed = MathUtil.clamp(rotationSpeed, -0.5, 0.5);
        
        // Aplica o comando de rotação ao robô
        drive.holonomicDrive(0.0, 0.0, rotationSpeed);
    }

    @Override
    public void end(boolean interrupted) {
        // Quando a execução acabar, para o robô
        drive.setDriveMotorSpeeds(0.0, 0.0, 0.0);
    }

    @Override
    public boolean isFinished() {
        // O comando termina quando o robô chega no ângulo desejado (dentro da tolerância)
        return pidZAxis.atSetpoint();
    }
}
