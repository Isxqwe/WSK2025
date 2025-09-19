package frc.robot.commands.auto;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import frc.robot.RobotContainer;
import frc.robot.subsystems.DriveTrain;

/**
 * Sequência de teste de labirinto. Usa o comando ScanAndChooseDirection para
 * decidir automaticamente para qual lado seguir baseado nas leituras da câmera
 * de profundidade.
 */
public class LabirintoA extends SequentialCommandGroup {

  private static final DriveTrain drive = RobotContainer.driveTrain;

  public LabirintoA() {
    double stopDist = 250; // parar a 25 cm da parede
    double maxStep = 5000; // fail-safe de 5 m
    double pauseS = 0.25; // pausas curtas (settle time)

    addCommands(
        // Zera encoders e yaw antes de começar
        new InstantCommand(() -> {
          drive.resetEncoders();
          drive.resetYaw();
        }, drive),

        // Usa o comando de varredura e decisão
        new DriveUntilWallWithPid(stopDist, 0.0, maxStep), new ScanAndChooseDirection(stopDist, maxStep, pauseS),

        new InstantCommand(() -> {
          drive.resetEncoders();
          drive.resetYaw();
        }, drive),

        new DriveUntilWallWithPid(stopDist, 0.0, maxStep), new ScanAndChooseDirection(stopDist, maxStep, pauseS),

        new InstantCommand(() -> {
          drive.resetEncoders();
          drive.resetYaw();
        }, drive),

        new DriveUntilWallWithPid(stopDist, 0.0, maxStep), new ScanAndChooseDirection(stopDist, maxStep, pauseS),

        new InstantCommand(() -> {
          drive.resetEncoders();
          drive.resetYaw();
        }, drive),

        new DriveUntilWallWithPid(stopDist, 0.0, maxStep), new ScanAndChooseDirection(stopDist, maxStep, pauseS),

        new InstantCommand(() -> {
          drive.resetEncoders();
          drive.resetYaw();
        }, drive)

    );
  }
}