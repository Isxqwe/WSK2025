package frc.robot.commands.auto;

import frc.robot.commands.driveCommands.RotateToAngleWithPID;

public class RotateToAngleWithPIDCommand extends AutoCommand {
    public RotateToAngleWithPIDCommand(double angle, double tolerance) {
        // Gira para o ângulo fornecido, com o erro de tolerância fornecido
        super(new RotateToAngleWithPID(angle, tolerance).withTimeout(5));
        // Timeout de 5 segundos, caso o comando não termine a tempo
    }
}
