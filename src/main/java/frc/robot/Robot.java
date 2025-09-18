package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.commands.auto.AutoCommand;
import frc.robot.commands.auto.DriveForward;
import frc.robot.commands.auto.DriveForwardWithPID;

public class Robot extends TimedRobot {

  private RobotContainer m_robotContainer;
  private Command autonomousCommand;

  private boolean active = false;
  private int countLED;
  private boolean prevLEDValue;

  @Override
  public void robotInit() {
    m_robotContainer = new RobotContainer();
    RobotContainer.controlpanel.setRunningLED(false);
    RobotContainer.controlpanel.setStoppedLED(true);
    countLED = 1;
    prevLEDValue = true;

    if (Constants.ENABLEMOCKDS) {
      RobotContainer.controlpanel.mockDS();
    }
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();

    if (Constants.ENABLEMOCKDS) {

      if (active == true) {

        RobotContainer.controlpanel.setStoppedLED(false);
        RobotContainer.controlpanel.setRunningLED(true);

        // If E-Stop button is pushed disable the robot
        if (RobotContainer.controlpanel.getStopButton()) {

          active = false;
          if (autonomousCommand != null) {
            autonomousCommand.cancel();
          }

          RobotContainer.controlpanel.setStoppedLED(true);
          RobotContainer.controlpanel.setRunningLED(false);

        } else {

          RobotContainer.controlpanel.setStoppedLED(false);
          RobotContainer.controlpanel.setRunningLED(true);

        }
      }

      if (active == false) {

        RobotContainer.controlpanel.setStoppedLED(true);
        RobotContainer.controlpanel.setRunningLED(false);

        if (RobotContainer.controlpanel.getStartButton()) {
          RobotContainer.controlpanel.enableMockDS();

          active = true;

          RobotContainer.controlpanel.setStoppedLED(false);
          RobotContainer.controlpanel.setRunningLED(true);
          autonomousCommand.schedule();

        } else {

          RobotContainer.controlpanel.setStoppedLED(true);
          RobotContainer.controlpanel.setRunningLED(false);

        }
      }

      if (RobotContainer.controlpanel.getResetButton()) {
        active = false;

        if (autonomousCommand != null) {
          autonomousCommand.cancel();
        }
        autonomousCommand.initialize();

        RobotContainer.controlpanel.setStoppedLED(true);
        RobotContainer.controlpanel.setRunningLED(false);
      }

    }
  }

  @Override
  public void disabledInit() {
    if (null == RobotContainer.autoChooser) {
      RobotContainer.autoChooser = new SendableChooser<>();
    }
    RobotContainer.autoChooser.setDefaultOption("Drive Forward", "Drive Forward");
    RobotContainer.autoMode.put("Drive Forward", new DriveForward());
    addAutoMode(RobotContainer.autoChooser, "Drive Forward with PID", new DriveForwardWithPID());
    SmartDashboard.putData(RobotContainer.autoChooser);
  }

  public void addAutoMode(SendableChooser<String> chooser, String auto, AutoCommand cmd) {
    chooser.addOption(auto, auto);
    RobotContainer.autoMode.put(auto, cmd);
  }

  @Override
  public void disabledPeriodic() {
    String mode = RobotContainer.autoChooser.getSelected();
    SmartDashboard.putString("Chosen Auto Mode", mode);
  }

  @Override
  public void autonomousInit() {
    autonomousCommand = m_robotContainer.getAutonomousCommand();
    if (autonomousCommand != null) {
      autonomousCommand.schedule();
    }
  }

  @Override
  public void autonomousPeriodic() {

  }

  @Override
  public void teleopInit() {
    if (autonomousCommand != null) {
      autonomousCommand.cancel();
    }

  }

  @Override
  public void teleopPeriodic() {

  }

  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
  }

  @Override
  public void testPeriodic() {
  }

  @Override
  public void simulationInit() {
  }

  @Override
  public void simulationPeriodic() {
  }

}
