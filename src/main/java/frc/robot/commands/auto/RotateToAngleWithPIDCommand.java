package frc.robot.commands.auto;

import frc.robot.commands.driveCommands.RotateToAngleWithPID;

public class RotateToAngleWithPIDCommand extends AutoCommand {
    public RotateToAngleWithPIDCommand() {
        // Gira para 90 graus, com erro de 1 grau
        super(new RotateToAngleWithPID(90.0, 1).withTimeout(5));
        // Timeout usado caso o comando não termine
    }
}