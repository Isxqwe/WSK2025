
package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.RobotContainer;
import frc.robot.gamepad.OI;
import frc.robot.subsystems.OMS;

public class TeleopOMS extends CommandBase {
    private static final OMS oms = RobotContainer.oms;
    private static final OI oi = RobotContainer.oi;

    // Garra
    private static final double CLAW_OPEN_DEG = 260.0; // seu OMS permite até 220
    private static final double CLAW_CLOSED_DEG = 50.0;

    // Linear (padrão): Up = OUT (estendido), Down = IN (retraído)
    private static final double LINEAR_OUT_DEG = 160.0; // estendido
    private static final double LINEAR_IN_DEG = 0.0; // retraído

    // Garrarot (3 estados)
    private static final double ROT_STOW_DEG = 25.0; // Guardar (0)
    private static final double ROT_GRIP1_DEG = 90.0; // Pegada 1
    private static final double ROT_GRIP2_DEG = 210.0; // Pegada 2

    // Elevator (manual)
    private static final double ELEV_SPEED_UP = 0.7;
    private static final double ELEV_SPEED_DOWN = -0.7;

    private double currentLinearDeg = LINEAR_OUT_DEG; // Inicializa com a posição retraída (evitar que o servo inicie estendido)
    private double currentClawDeg = CLAW_CLOSED_DEG;

    // Estado inicial: Pegada 1 (90°)
    private int rotState = 2; // 0=stow, 1=grip1, 2=grip2
    private double currentRotDeg = ROT_GRIP1_DEG;

    // Debounce
    private boolean prevDpadLeft = false;
    private boolean prevDpadRight = false;

    // Reaplica setpoint do garrarot 1x após enable
    private boolean rotSetpointLatched = false;

    public TeleopOMS() {
        addRequirements(oms);
    }

    @Override
    public void initialize() {
        // Inicializa com a posição atual do linear (evita movimento inesperado)
        try {
            Thread.sleep(100); // Espera 100ms
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        currentLinearDeg = oms.getLinearGarraDeg();
    }

    @Override
    public void execute() {
        // ===== Inputs =====
        boolean elevUp = oi.getDriveRightBumper();
        boolean elevDown = oi.getDriveRightTrigger();
        boolean clawOpen = oi.getDriveLeftBumper();

        boolean dpadUp = oi.getDriveDPadUp(); // Linear OUT (estendido)
        boolean dpadDown = oi.getDriveDPadDown(); // Linear IN (retraído)

        // Garrarot: Right = avança (0→1→2), Left = volta (2→1→0)
        boolean dpadLeft = oi.getDriveDPadLeft();
        boolean dpadRight = oi.getDriveDPadRight();

        // ===== Elevator =====
        if (elevUp) oms.setElevatorMotorSpeed(ELEV_SPEED_UP);
        else if (elevDown) oms.setElevatorMotorSpeed(ELEV_SPEED_DOWN);
        else oms.setElevatorMotorSpeed(0.0);

        // ===== Garra abre/fecha =====
        currentClawDeg = clawOpen ? CLAW_OPEN_DEG : CLAW_CLOSED_DEG;
        oms.setClawDeg(currentClawDeg);

        // ===== Linear =====
        // Movimento do linear apenas quando o botão correspondente for pressionado
        if (dpadDown) {
            currentLinearDeg = LINEAR_OUT_DEG; // Vai para a posição estendida
        } else if (dpadUp) {
            currentLinearDeg = LINEAR_IN_DEG; // Vai para a posição retraída
        }
        oms.setLinearGarraDeg(currentLinearDeg);

        // ===== Garrarot (sequencial) =====
        boolean risingLeft = dpadLeft && !prevDpadLeft;
        boolean risingRight = dpadRight && !prevDpadRight;

        // Se ambos os botões forem pressionados ao mesmo tempo, ignora a ação
        if (risingLeft && risingRight) {
            // Ambos pressionados -> ignora para evitar salto
        } else if (risingRight) {
            // Avança: 0->1->2
            if (rotState < 2) rotState++;
        } else if (risingLeft) {
            // Volta: 2->1->0
            if (rotState > 0) rotState--;
        }

        // Atualiza o valor do ângulo do rot
        switch (rotState) {
            case 0: currentRotDeg = ROT_STOW_DEG; break;
            case 1: currentRotDeg = ROT_GRIP1_DEG; break;
            case 2: currentRotDeg = ROT_GRIP2_DEG; break;
        }
        oms.setGarraRotDeg(currentRotDeg);

        // Atualiza o debounce
        prevDpadLeft = dpadLeft;
        prevDpadRight = dpadRight;
    }

    @Override
    public void end(boolean interrupted) {
        oms.setElevatorMotorSpeed(0.0);
    }
}
