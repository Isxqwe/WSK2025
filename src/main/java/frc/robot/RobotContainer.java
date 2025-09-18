package frc.robot;

import java.util.HashMap;
import java.util.Map;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.commands.Teleop;
import frc.robot.commands.auto.AutoCommand;
import frc.robot.commands.auto.DriveForward;
import frc.robot.gamepad.OI;
import frc.robot.subsystems.ControlPanel;
import frc.robot.subsystems.DriveTrain;
//import frc.robot.subsystems.OMS;
//import frc.robot.commands.TeleopOMS;

public class RobotContainer {

  public static ControlPanel controlpanel;

  public static DriveTrain driveTrain;
  // public static OMS oms;
  public static OI oi;

  public static SendableChooser<String> autoChooser;
  public static Map<String, AutoCommand> autoMode = new HashMap<>();

  public RobotContainer() {

    driveTrain = new DriveTrain();
    // oms = new OMS();
    oi = new OI();
    controlpanel = new ControlPanel();

    driveTrain.setDefaultCommand(new Teleop());
    // oms.setDefaultCommand(new TeleopOMS());
  }

  public Command getAutonomousCommand() {
    String mode = autoChooser.getSelected();
    return autoMode.getOrDefault(mode, new DriveForward());
  }

}
