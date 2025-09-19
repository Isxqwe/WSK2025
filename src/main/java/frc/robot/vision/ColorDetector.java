package frc.robot.vision;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.*;

public class ColorDetector {
  public static class Detection {
    public final String colorName;
    public final Rect box;
    public final Point center;
    public final Mat mask;
    public Detection(String colorName, Rect box, Point center, Mat mask) {
      this.colorName = colorName;
      this.box = box;
      this.center = center;
      this.mask = mask;
    }
  }

  // Detecta apenas a cor alvo, ou nada se for "NONE"
  public static Optional<Detection> detect(Mat frame, String targetColor) {
    if (targetColor == null || targetColor.equalsIgnoreCase("NONE")) {
      return Optional.empty(); // não procura nada
    }

    Mat hsv = new Mat();
    Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);

    Scalar lower, upper;
    Mat mask = new Mat();

    switch (targetColor.toUpperCase()) {
      case "RED":
        Mat mask1 = new Mat();
        Mat mask2 = new Mat();
        Core.inRange(hsv, new Scalar(0, 120, 70), new Scalar(10, 255, 255), mask1);
        Core.inRange(hsv, new Scalar(170, 120, 70), new Scalar(180, 255, 255), mask2);
        Core.add(mask1, mask2, mask);
        break;

      case "BLUE":
        lower = new Scalar(100, 150, 70);
        upper = new Scalar(140, 255, 255);
        Core.inRange(hsv, lower, upper, mask);
        break;

      case "GREEN":
        lower = new Scalar(40, 70, 70);
        upper = new Scalar(80, 255, 255);
        Core.inRange(hsv, lower, upper, mask);
        break;

      case "YELLOW":
        lower = new Scalar(20, 100, 100);
        upper = new Scalar(30, 255, 255);
        Core.inRange(hsv, lower, upper, mask);
        break;

      default:
        return Optional.empty();
    }

    // Limpeza
    Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5));
    Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);
    Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernel);

    // Contornos
    List<MatOfPoint> contours = new ArrayList<>();
    Imgproc.findContours(mask, contours, new Mat(),
                         Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

    double maxArea = 0;
    Rect best = null;
    for (MatOfPoint c : contours) {
      double area = Imgproc.contourArea(c);
      if (area < 500) continue;
      Rect box = Imgproc.boundingRect(c);
      if (area > maxArea) {
        maxArea = area;
        best = box;
      }
    }

    if (best != null) {
      Point center = new Point(best.x + best.width/2.0, best.y + best.height/2.0);
      return Optional.of(new Detection(targetColor.toUpperCase(), best, center, mask));
    }

    return Optional.empty();
  }
}
