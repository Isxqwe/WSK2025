/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import com.studica.frc.MockDS;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class ControlPanel extends SubsystemBase {
    private DigitalInput startBtn;
    private DigitalInput stopBtn;
    private DigitalInput resetBtn;
    private DigitalOutput runningLED;
    private DigitalOutput stoppedLED;

    private MockDS ds;
    private boolean enabled;

    public ControlPanel() {
        startBtn = new DigitalInput(Constants.controlPanel.STARTBUTTON);
        stopBtn = new DigitalInput(Constants.controlPanel.STOPBUTTON);
        resetBtn = new DigitalInput(Constants.controlPanel.RESETBUTTON);
        runningLED = new DigitalOutput(Constants.controlPanel.RUNNING_LED);
        stoppedLED = new DigitalOutput(Constants.controlPanel.STOPPED_LED);

    }

    public boolean getStartButton() {
        return !startBtn.get();
    }

    public boolean getResetButton() {
        return !resetBtn.get();
    }

    public boolean getStopButton() {
        return !stopBtn.get();
    }

    public void setRunningLED(boolean value) {
        runningLED.set(value);
    }

    public void setStoppedLED(boolean value) {
        stoppedLED.set(value);
    }

    public void mockDS() {
        ds = new MockDS();
        enabled = false;
    }

    public void enableMockDS() {
        this.ds.enable();
        enabled = true;
    }

    public void disableMockDS() {
        this.ds.disable();
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void periodic() {
        // Exibe no SmartDashboard se os botões estão pressionados
        if (Constants.DEBUG_CONTROLPANEL) {
            SmartDashboard.putBoolean("Stop button", getStopButton()); // Estado do botão STOP
            SmartDashboard.putBoolean("Start button", getStartButton()); // Estado do botão START
            SmartDashboard.putBoolean("Reset button", getResetButton()); // Estado do botão RESET
        }
    }
}
