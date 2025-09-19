package frc.robot;

import java.util.HashMap;
import java.util.Map;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.commands.Teleop;
import frc.robot.commands.auto.AutoCommand;
import frc.robot.commands.auto.DriveForward;
import frc.robot.commands.auto.RotateToAngleWithPIDCommand;
import frc.robot.gamepad.OI;
import frc.robot.subsystems.ControlPanel;
import frc.robot.subsystems.DepthCamera;
import frc.robot.subsystems.DriveTrain;
//import frc.robot.subsystems.OMS;
//import frc.robot.commands.TeleopOMS;
import frc.robot.subsystems.Monitoramento;

public class RobotContainer {

  public final Monitoramento monitor = new Monitoramento();
  public static final DepthCamera camera = new DepthCamera();
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

  public DepthCamera getCamera() {
    return camera;
}


  public Command getAutonomousCommand() {
    String mode = autoChooser.getSelected();
    return autoMode.getOrDefault(mode, new DriveForward());
  }

}
