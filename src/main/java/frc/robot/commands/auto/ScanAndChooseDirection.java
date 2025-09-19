package frc.robot.commands.auto;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ConditionalCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;

import frc.robot.RobotContainer;
import frc.robot.subsystems.DriveTrain;
import frc.robot.subsystems.DepthWallRange;

/**
 * Varre esquerda e direita, mede a distância (DepthWallRange) e decide seguir
 * pelo lado com MAIOR espaço livre.
 */
public class ScanAndChooseDirection extends SequentialCommandGroup {

    private static final DriveTrain drive = RobotContainer.driveTrain;
    private static final DepthWallRange wall = RobotContainer.wallRange;

    private final double stopDistMeters; // Distância de parada (ex. 0.60)
    private final double maxStepMeters;  // Distância máxima a percorrer (ex. 3.0)
    private final double settleSeconds;  // Tempo de espera para estabilização (ex. 0.25)
    private final double hysteresisMeters; // Histerese para comparação (ex. 0.05)

    private double yaw0;  // Armazena o valor inicial do yaw
    private double dLeft = 0.0;  // Distância medida à esquerda
    private double dRight = 0.0; // Distância medida à direita

    // Construtor com valores padrão para histerese
    public ScanAndChooseDirection(double stopDistMeters, double maxStepMeters, double settleSeconds) {
        this(stopDistMeters, maxStepMeters, settleSeconds, 0.05);  // Valor padrão para histerese
    }

    // Construtor com todos os parâmetros
    public ScanAndChooseDirection(double stopDistMeters, double maxStepMeters, double settleSeconds, double hysteresisMeters) {
        this.stopDistMeters = stopDistMeters;
        this.maxStepMeters = maxStepMeters;
        this.settleSeconds = settleSeconds;
        this.hysteresisMeters = hysteresisMeters;

        // Adiciona os comandos para medir as distâncias à esquerda e à direita
        addCommands(
            new InstantCommand(() -> yaw0 = drive.getYaw()),  // Captura o yaw inicial

            // Medição para a DIREITA (+90 graus)
            new RotateToAngleWithPIDCommand(yaw0 + 90.0, 6.0), 
            new WaitCommand(settleSeconds),
            new InstantCommand(() -> {
                wall.update();
                if (wall.hasValidReading()) 
                    dRight = wall.getFilteredDistanceMeters();
                System.out.println("[Scan] RIGHT: " + dRight + " m");
            }),

            // Medição para a ESQUERDA (-90 graus)
            new RotateToAngleWithPIDCommand(yaw0 - 90.0, 6.0), 
            new WaitCommand(settleSeconds),
            new InstantCommand(() -> {
                wall.update();
                if (wall.hasValidReading()) 
                    dLeft = wall.getFilteredDistanceMeters();
                System.out.println("[Scan] LEFT:  " + dLeft + " m");
            }),

            // Decisão do lado a seguir (direita ou esquerda)
            new ConditionalCommand(
                new SequentialCommandGroup(  // Se for para a DIREITA
                    new RotateToAngleWithPIDCommand(yaw0 + 90.0, 6.0),
                    new DriveUntilWallWithPid(stopDistMeters, yaw0 + 90.0, maxStepMeters)
                ),
                new SequentialCommandGroup(  // Se for para a ESQUERDA
                    new RotateToAngleWithPIDCommand(yaw0 - 90.0, 6.0),
                    new DriveUntilWallWithPid(stopDistMeters, yaw0 - 90.0, maxStepMeters)
                ),
                () -> chooseRight()  // Condição de escolha (direita ou esquerda)
            )
        );
    }

    /** Retorna true se devemos ir para a direita (com histerese e fallbacks). */
    private boolean chooseRight() {
        // Fallbacks quando uma leitura falhou
        if (dRight <= 0 && dLeft > 0) return false;  // Somente esquerda válida
        if (dLeft <= 0 && dRight > 0) return true;   // Somente direita válida

        // Comparação com histerese
        if (dRight > dLeft + hysteresisMeters) return true;
        if (dLeft > dRight + hysteresisMeters) return false;

        // Empate ~ iguais: padrão é ir para a direita
        return true;
    }

    // Getters para telemetria (opcional)
    public double getRightDistanceMeters() {
        return dRight;
    }

    public double getLeftDistanceMeters() {
        return dLeft;
    }
}
