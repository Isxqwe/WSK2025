package frc.robot.subsystems;

import frc.robot.subsystems.DepthCamera;
import edu.wpi.first.wpilibj.Timer;
import java.util.Arrays;

/** ROI central + mediana + filtro exponencial (EMA) para distância à parede. */
public class DepthWallRange {
    private final DepthCamera cam;

    private int halfWindowPx = 10; // ROI de (2*10+1)² = 21x21
    private int step = 2; // amostragem (pula pixels)
    private double emaAlpha = 0.2; // 0..1 (peso do valor novo)

    private double filteredMeters = -1.0;
    private double lastUpdate = 0.0;

    public DepthWallRange(DepthCamera cam) {
        this.cam = cam;
    }

    public void setParams(int halfWindowPx, int step, double emaAlpha) {
        this.halfWindowPx = Math.max(1, halfWindowPx);
        this.step = Math.max(1, step);
        this.emaAlpha = Math.min(1.0, Math.max(0.0, emaAlpha));
    }

    /** Chame frequentemente (ex.: em periodic ou no execute de um comando) */
    public void update() {
        byte[] depth = cam.getDepthBytesSnapshot();
        int w = cam.getDepthWidth();
        int h = cam.getDepthHeight();
        if (depth == null || w <= 0 || h <= 0)
            return;

        int cx = w / 2, cy = h / 2;
        int x0 = Math.max(0, cx - halfWindowPx);
        int x1 = Math.min(w - 1, cx + halfWindowPx);
        int y0 = Math.max(0, cy - halfWindowPx);
        int y1 = Math.min(h - 1, cy + halfWindowPx);

        int maxSamples = ((x1 - x0 + 1) * (y1 - y0 + 1)) / (step * step) + 8;
        int[] samples = new int[maxSamples];
        int k = 0;

        for (int y = y0; y <= y1; y += step) {
            int rowBase = y * w * 2;
            for (int x = x0; x <= x1; x += step) {
                int idx = rowBase + x * 2;
                if (idx + 1 >= depth.length)
                    continue;
                int d1 = depth[idx] & 0xFF;
                int d2 = depth[idx + 1] & 0xFF;
                int mm = (d2 << 8) | d1;
                if (mm > 0) {
                    if (k < samples.length)
                        samples[k++] = mm;
                }
            }
        }
        if (k == 0)
            return;

        Arrays.sort(samples, 0, k);
        int median = (k % 2 == 1) ? samples[k / 2] : ((samples[k / 2 - 1] + samples[k / 2]) / 2);
        double meters = median / 1000.0;

        if (filteredMeters < 0)
            filteredMeters = meters;
        else
            filteredMeters = emaAlpha * meters + (1 - emaAlpha) * filteredMeters;

        lastUpdate = Timer.getFPGATimestamp();
    }

    public double getFilteredDistanceMeters() {
        return filteredMeters;
    }

    public double getLastUpdateTime() {
        return lastUpdate;
    }

    public boolean hasValidReading() {
        return filteredMeters > 0;
    }
}
