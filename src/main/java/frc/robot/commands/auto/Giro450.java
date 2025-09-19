package frc.robot.commands.auto;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import frc.robot.RobotContainer;
import frc.robot.commands.driveCommands.RotateToAngleWithPID;
import frc.robot.subsystems.DriveTrain;

public class Giro450 extends SequentialCommandGroup {

    private static final DriveTrain drive = RobotContainer.driveTrain;

    public Giro450() {

        addCommands(

                // Gira para 90 graus, com erro de 1 grau
                new RotateToAngleWithPID(179.0, 1).withTimeout(3), 
                new RotateToAngleWithPID(179.0, 1).withTimeout(3),
                new RotateToAngleWithPID(92.0, 1).withTimeout(3)
        // Timeout usado caso o comando não termine

        );

    }
}