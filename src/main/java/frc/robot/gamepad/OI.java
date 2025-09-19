package frc.robot.gamepad;

import edu.wpi.first.wpilibj.Joystick;

public class OI {
    private static final double DEADBAND = 0.05;
    private final Joystick drivePad;

    public OI() {
        drivePad = new Joystick(GamepadConstants.DRIVE_USB_PORT);
    }

    // ===== Sticks =====
    public double getRightDriveY() {
        double v = drivePad.getRawAxis(GamepadConstants.RIGHT_ANALOG_Y);
        return Math.abs(v) < DEADBAND ? 0.0 : v;
    }
    public double getRightDriveX() {
        double v = drivePad.getRawAxis(GamepadConstants.RIGHT_ANALOG_X);
        return Math.abs(v) < DEADBAND ? 0.0 : v;
    }
    public double getLeftDriveY() {
        double v = drivePad.getRawAxis(GamepadConstants.LEFT_ANALOG_Y);
        return Math.abs(v) < DEADBAND ? 0.0 : v;
    }
    public double getLeftDriveX() {
        double v = drivePad.getRawAxis(GamepadConstants.LEFT_ANALOG_X);
        return Math.abs(v) < DEADBAND ? 0.0 : v;
    }

    // ===== Buttons =====
    public boolean getDriveRightTrigger() { return drivePad.getRawButton(GamepadConstants.RIGHT_TRIGGER); }
    public boolean getDriveRightBumper()  { return drivePad.getRawButton(GamepadConstants.RIGHT_BUMPER); }
    public boolean getDriveLeftTrigger()  { return drivePad.getRawButton(GamepadConstants.LEFT_TRIGGER); }
    public boolean getDriveLeftBumper()   { return drivePad.getRawButton(GamepadConstants.LEFT_BUMPER); }
    public boolean getDriveXButton()      { return drivePad.getRawButton(GamepadConstants.SHARE_BUTTON); }
    public boolean getDriveYButton()      { return drivePad.getRawButton(GamepadConstants.TRIANGLE_BUTTON); }
    public boolean getDriveBButton()      { return drivePad.getRawButton(GamepadConstants.CIRCLE_BUTTON); }
    public boolean getDriveAButton()      { return drivePad.getRawButton(GamepadConstants.X_BUTTON); }
    public boolean getDriveBackButton()   { return drivePad.getRawButton(GamepadConstants.SHARE_BUTTON); }
    public boolean getDriveStartButton()  { return drivePad.getRawButton(GamepadConstants.OPTIONS_BUTTON); }
    public boolean getDriveRightAnalogButton() { return drivePad.getRawButton(GamepadConstants.RIGHT_ANALOG_BUTTON); }
    public boolean getDriveLeftAnalogButton()  { return drivePad.getRawButton(GamepadConstants.LEFT_ANALOG_BUTTON); }

    // ===== D-pad via POV (RECOMENDADO) =====
    public boolean getDriveDPadUp() {
        int pov = drivePad.getPOV();
        return pov == 0 || pov == 45 || pov == 315;
    }
    public boolean getDriveDPadDown() {
        int pov = drivePad.getPOV();
        return pov == 180 || pov == 135 || pov == 225;
    }
    public boolean getDriveDPadLeft() {
        int pov = drivePad.getPOV();
        return pov == 270 || pov == 225 || pov == 315;
    }
    public boolean getDriveDPadRight() {
        int pov = drivePad.getPOV();
        return pov == 90 || pov == 45 || pov == 135;
    }

    // ===== (Opcional) Deprecate os antigos para evitar uso futuro =====
    @Deprecated public boolean getDriveDPadX() { return false; } // não usar
    @Deprecated public boolean getDriveDPadY() { return false; } // não usar
}
