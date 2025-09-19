package frc.robot.vision;

import edu.wpi.first.networktables.*;

public class VisionPublisher implements AutoCloseable {
  private final NetworkTableEntry hasTarget, x, y, conf, region;

  public VisionPublisher() {
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    inst.setNetworkIdentity("vision-vmx");
    inst.startServer();

    NetworkTable table = inst.getTable("vision");
    hasTarget = table.getEntry("hasTarget");
    x        = table.getEntry("x");
    y        = table.getEntry("y");
    conf     = table.getEntry("conf");
    region   = table.getEntry("region"); // NOVO

    hasTarget.setBoolean(false);
    x.setDouble(0.5);
    y.setDouble(0.5);
    conf.setDouble(0.0);
    region.setString("NONE");
    inst.flush();
  }

  public void publishNoTarget() {
    hasTarget.setBoolean(false);
    region.setString("NONE");
  }

  public void publish(double nx, double ny, double confidence, String pos) {
    hasTarget.setBoolean(true);
    x.setDouble(nx);
    y.setDouble(ny);
    conf.setDouble(confidence);
    region.setString(pos); // publica a string de posição
  }

  @Override public void close() {}
}
