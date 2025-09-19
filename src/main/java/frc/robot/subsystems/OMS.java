package frc.robot.subsystems;

import com.studica.frc.Servo;
import com.studica.frc.TitanQuad;
import com.studica.frc.TitanQuadEncoder;

import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import frc.robot.Constants;

public class OMS extends SubsystemBase {
    // ====== Motor/encoder do elevador ======
    private final TitanQuad elevator;
    private final TitanQuadEncoder elevatorEncoder;

    // ====== Servos ======
    private final Servo claw;
    private final Servo linearGarra;
    private final Servo garraRot;

    // Limites (0–220° conforme seu hardware)
    private static final double MIN_DEG = 0.0;
    private static final double MAX_DEG = 260.0;

    // === Calibração específica do garrarot ===
    private static final boolean ROT_INVERT = false; // true para inverter sentido
    private static final double  ROT_OFFSET = 0.0;   // deslocamento do zero mecânico (°)

    // ====== Controle de posição do elevador (PID) ======
    // Unidades: mesmas de getElevatorEncoderDistance() (cm, dado seu ELEVATOR_DIST_TICK)
    private final PIDController elevPid = new PIDController(0.06, 0.0, 0.0); // ajuste fino em campo
    private static final double ELEV_TOL_CM   = 0.8;   // tolerância de posição (cm)
    private static final double ELEV_MAX_CMD  = 0.6;   // saturação de saída (|cmd| <= 0.6)
    private static final double ELEV_KG       = 0.05;  // feedforward gravidade (constante)

    private boolean elevatorPosMode = false; // true => PID ativo no periodic
    private double elevatorTargetCm = 0.0;   // alvo (cm)

    public OMS() {
        elevator        = new TitanQuad(Constants.TITAN_ID, Constants.M3);
        elevatorEncoder = new TitanQuadEncoder(elevator, Constants.M3, Constants.ELEVATOR_DIST_TICK);

        claw        = new Servo(Constants.GARSERVO);
        linearGarra = new Servo(Constants.LINEARGARRA);
        garraRot    = new Servo(Constants.ROTGARRA);

        elevPid.setTolerance(ELEV_TOL_CM);
    }

    /* ========= ELEVATOR ========= */

    /** Modo velocidade (teleop). Também desliga o modo posição (PID). */
    public void setElevatorMotorSpeed(double speed) {
        elevatorPosMode = false; // teleop tem prioridade quando usado
        elevator.set(speed);
    }
    public double getLinearGarraDeg() {
        return linearGarra.getAngle(); // Retorna a posição atual do servo linear
    }

    /** Distância atual do elevador (cm, com base no ELEVATOR_DIST_TICK). */
    public double getElevatorEncoderDistance() {
        return elevatorEncoder.getEncoderDistance();
    }

    /** Alias semântico, se preferir trabalhar com "posição". */
    public double getElevatorPosition() {
        return getElevatorEncoderDistance();
    }

    public void resetEncoders() {
        elevatorEncoder.reset();
    }

    /** Liga/desliga controle de posição por PID (autônomo/precisão). */
    public void enableElevatorPositionMode(boolean enable) {
        elevatorPosMode = enable;
        if (enable) {
            elevPid.reset();
        } else {
            elevator.set(0.0);
        }
    }

    /** Define o alvo de posição (cm). */
    public void setElevatorTarget(double targetCm) {
        this.elevatorTargetCm = targetCm;
    }

    /** Retorna true quando dentro da tolerância configurada. */
    public boolean atElevatorTarget() {
        return elevPid.atSetpoint();
    }

    /* ========= SERVOS ========= */

    private static double clampValue(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    private double clamp(double deg) {
        return clampValue(deg, MIN_DEG, MAX_DEG);
    }

    // Mapeia ângulo lógico -> físico do garrarot (invert/offset + clamp)
    private double mapRot(double logicalDeg) {
        double x = ROT_INVERT ? (MAX_DEG - logicalDeg) : logicalDeg;
        return clamp(x + ROT_OFFSET);
    }

    public void setClawDeg(double deg)        { claw.setAngle(clamp(deg)); }
    public void setLinearGarraDeg(double deg) { linearGarra.setAngle(clamp(deg)); }
    public void setGarraRotDeg(double deg)    { garraRot.setAngle(mapRot(deg)); }

    /* ========= PERIODIC ========= */

    @Override
    public void periodic() {
        if (elevatorPosMode) {
            double meas = getElevatorPosition();
            double out  = elevPid.calculate(meas, elevatorTargetCm);

            // Feedforward de gravidade simples (ajuste ELEV_KG conforme necessário)
            out += Math.copySign(ELEV_KG, elevatorTargetCm - meas);

            // Saturação de segurança (sem MathUtil)
            out = clampValue(out, -ELEV_MAX_CMD, ELEV_MAX_CMD);

            elevator.set(out);
        }
        // (Se quiser, adicione telemetria aqui)
    }
}