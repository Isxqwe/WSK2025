package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.commands.auto.AutoCommand;
import frc.robot.commands.auto.DriveForward;
import frc.robot.commands.auto.DriveForwardWithPID;
import frc.robot.commands.auto.RotateToAngleWithPIDCommand;
import frc.robot.commands.auto.TeleopAuto;

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

    // Se o MockDS está ativo, o controle do robô vem do MockDS
    if (Constants.ENABLEMOCKDS) {
      if (active) {
        RobotContainer.controlpanel.setStoppedLED(false);
        RobotContainer.controlpanel.setRunningLED(true);

        // Se o botão de stop do MockDS for pressionado, desativa o robô
        if (RobotContainer.controlpanel.getStopButton()) {
          active = false;
          if (autonomousCommand != null) {
            autonomousCommand.cancel();
          }
          RobotContainer.controlpanel.setStoppedLED(true);
          RobotContainer.controlpanel.setRunningLED(false);
        }
      } else {
        RobotContainer.controlpanel.setStoppedLED(true);
        RobotContainer.controlpanel.setRunningLED(false);

        // Se o botão de start do MockDS for pressionado, ativa o robô
        if (RobotContainer.controlpanel.getStartButton()) {
          RobotContainer.controlpanel.enableMockDS();
          active = true;
          RobotContainer.controlpanel.setStoppedLED(false);
          RobotContainer.controlpanel.setRunningLED(true);
          autonomousCommand.schedule();
        }
      }

      // Se o botão de reset do MockDS for pressionado, reinicia o robô
      if (RobotContainer.controlpanel.getResetButton()) {
        active = false;
        if (autonomousCommand != null) {
          autonomousCommand.cancel();
        }
        autonomousCommand.initialize();
        RobotContainer.controlpanel.setStoppedLED(true);
        RobotContainer.controlpanel.setRunningLED(false);
      }

    } else {
      // Controle remoto via Console só pode atuar se o MockDS estiver inativo
      if (!active) {
        if (controlStationStartButtonPressed()) {
          active = true;
          autonomousCommand.schedule();
          RobotContainer.controlpanel.setRunningLED(true);
          RobotContainer.controlpanel.setStoppedLED(false);
        }
      }

      // Se o botão de stop for pressionado no Console, desativa o robô
      if (controlStationStopButtonPressed() && active) {
        active = false;
        if (autonomousCommand != null) {
          autonomousCommand.cancel();
        }
        RobotContainer.controlpanel.setStoppedLED(true);
        RobotContainer.controlpanel.setRunningLED(false);
      }
    }
  }

  // Método fictício que detecta comandos do Control Station Console
  private boolean controlStationStartButtonPressed() {
    // Aqui você pode definir a lógica para detectar o comando de Start
    return false;  // Exemplo fictício
  }

  private boolean controlStationStopButtonPressed() {
    // Aqui você pode definir a lógica para detectar o comando de Stop
    return false;  // Exemplo fictício
  }

  @Override
  public void disabledInit() {
    if (null == RobotContainer.autoChooser) {
      RobotContainer.autoChooser = new SendableChooser<>();
    }
    RobotContainer.autoChooser.setDefaultOption("TeleopAuto", "TeleopAuto");
    RobotContainer.autoMode.put("TeleopAuto", new TeleopAuto());

    addAutoMode(RobotContainer.autoChooser, "Rotate to Angle", new RotateToAngleWithPIDCommand());
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
