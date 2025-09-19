package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.vision.ColorDetector;
import frc.robot.vision.VisionPublisher;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.cscore.CvSource;

import edu.wpi.first.networktables.*;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class VisionSubsystem extends SubsystemBase {
  private final VisionPublisher publisher = new VisionPublisher();
  private final DepthCamera camera;
  private final CvSource visionStream;
  private final CvSource maskStream;

  // Cor alvo atual (controlada pelo autônomo ou Dashboard)
  private volatile String targetColor = "NONE";

  // Entrada no NT e chooser para o Dashboard
  private final NetworkTableEntry targetColorEntry;
  private final SendableChooser<String> colorChooser = new SendableChooser<>();

  public VisionSubsystem(DepthCamera camera) {
    this.camera = camera;

    visionStream = CameraServer.getInstance().putVideo("Vision-Processed", 640, 480);
    maskStream   = CameraServer.getInstance().putVideo("Vision-Mask", 640, 480);

    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    NetworkTable table = inst.getTable("vision");
    targetColorEntry = table.getEntry("targetColor");
    targetColorEntry.setString("NONE");

    // Configura o combo box no Shuffleboard
    colorChooser.setDefaultOption("NONE", "NONE");
    colorChooser.addOption("RED", "RED");
    colorChooser.addOption("BLUE", "BLUE");
    colorChooser.addOption("GREEN", "GREEN");
    colorChooser.addOption("YELLOW", "YELLOW");
    SmartDashboard.putData("Target Color", colorChooser);

    // Thread de visão
    new Thread(() -> {
      while (true) {
        // Atualiza cor alvo com o que foi escolhido no Dashboard
        String requested = colorChooser.getSelected();
        if (requested != null && !requested.equals(targetColor)) {
          targetColor = requested;
          targetColorEntry.setString(requested); // sincroniza no NT
        }

        Mat original = camera.getLastFrame();
        if (!original.empty()) {
          Mat frame = original.clone();

          var detOpt = ColorDetector.detect(frame, targetColor);
          if (detOpt.isPresent()) {
            var det = detOpt.get();
            double nx = det.center.x / frame.cols();
            double ny = det.center.y / frame.rows();

            // Determinar posição (ESQ / MEIO / DIR)
            String pos = (nx < 0.33) ? "ESQ" : (nx > 0.66) ? "DIR" : "MEIO";

            // Publicar em NT
            publisher.publish(nx, ny, 1.0, targetColor + "-" + pos);

            // ===== Overlay =====
            Imgproc.rectangle(frame, det.box.tl(), det.box.br(), new Scalar(0,255,0), 4);
            Imgproc.circle(frame, det.center, 10, new Scalar(255,0,0), -1);
            Imgproc.putText(frame, targetColor + " " + pos,
                            new Point(det.box.x, Math.max(20, det.box.y - 10)),
                            0, 1.0, new Scalar(0,0,255), 3);

            // Publicar máscara
            maskStream.putFrame(det.mask);
          } else {
            publisher.publishNoTarget();
          }

          // Linhas divisórias
          int w = frame.cols();
          int h = frame.rows();
          Imgproc.line(frame, new Point(w/3, 0), new Point(w/3, h), new Scalar(255,255,0), 2);
          Imgproc.line(frame, new Point(2*w/3, 0), new Point(2*w/3, h), new Scalar(255,255,0), 2);

          // Publicar frame processado
          visionStream.putFrame(frame);
        } else {
          publisher.publishNoTarget();
        }

        try { Thread.sleep(50); } catch (InterruptedException e) {}
      }
    }, "VisionThread").start();
  }

  // Método para mudar a cor via código (ex.: autônomo)
  public void setTargetColor(String color) {
    this.targetColor = color;
    targetColorEntry.setString(color); // sincroniza com NT
    colorChooser.setDefaultOption(color, color); // atualiza no Dashboard
  }
}
