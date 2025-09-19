package frc.robot.vision;

import org.opencv.core.Scalar;

public final class VisionConfig {
  // Camera
  public static final int CAMERA_INDEX = 0;
  public static final int FRAME_WIDTH = 640;
  public static final int FRAME_HEIGHT = 480;
  public static final int FRAME_FPS = 20;

  // Faixa HSV da cor alvo (exemplo: verde bem forte)
  public static final Scalar COLOR_LOWER = new Scalar(35, 100, 100);
  public static final Scalar COLOR_UPPER = new Scalar(85, 255, 255);

  // NetworkTables
  public static final String NT_TABLE = "vision";
  public static final String NT_IDENTITY = "vision-vmx";
}
