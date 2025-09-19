package frc.robot.commands.auto;

import frc.robot.commands.driveCommands.SimpleDrive;

public class TeleopAuto extends AutoCommand
{
    public TeleopAuto ()
    {
        super(new SimpleDrive(0.0, 0.0, 0.0).withTimeout(0));
            
    }
}