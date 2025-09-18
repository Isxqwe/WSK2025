package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import java.io.*;
import java.nio.file.*;

public class Monitoramento extends SubsystemBase {

    // ---- Configs ----
    private static final double TEMP_WARN_C = 70.0; // amarelo
    private static final double TEMP_CRIT_C = 80.0; // vermelho
    private static final long POLL_MS = 1000; // taxa de atualização (1 s)

    // ---- Estado ----
    private long lastPoll = 0L;
    private double lastCpuTotal = -1, lastCpuIdle = -1;
    private double emaFps = 0.0;
    private boolean fpsEmaInit = false;

    // Chame isto sempre que um frame for produzido (após putFrame/stream)
    public void noteFrameProduced(double dtSeconds) {
        // Exponential Moving Average para suavizar FPS
        double fps = (dtSeconds > 1e-6) ? (1.0 / dtSeconds) : 0.0;
        double alpha = 0.15; // suavização
        if (!fpsEmaInit) {
            emaFps = fps;
            fpsEmaInit = true;
        } else {
            emaFps = alpha * fps + (1 - alpha) * emaFps;
        }
        SmartDashboard.putNumber("Vision/FPS (EMA)", emaFps);
        SmartDashboard.putNumber("Vision/FPS (inst)", fps);
    }

    @Override
    public void periodic() {
        long now = System.currentTimeMillis();
        if (now - lastPoll < POLL_MS)
            return;
        lastPoll = now;

        double tempC = readCpuTempC();
        SmartDashboard.putNumber("Raspberry/CPU Temp (°C)", tempC);
        SmartDashboard.putBoolean("Raspberry/Temp > WARN", tempC >= TEMP_WARN_C);
        SmartDashboard.putBoolean("Raspberry/Temp > CRIT", tempC >= TEMP_CRIT_C);

        double cpuPercent = readCpuUsagePercent();
        SmartDashboard.putNumber("Raspberry/CPU (%)", cpuPercent);

        MemInfo mem = readMemInfo();
        if (mem.ok) {
            SmartDashboard.putNumber("Raspberry/RAM Used (MB)", mem.usedMB);
            SmartDashboard.putNumber("Raspberry/RAM Total (MB)", mem.totalMB);
            SmartDashboard.putNumber("Raspberry/RAM (%)", mem.usedPct);
        }

        // Info do processo Java (heap) — útil para vazamentos
        long heapUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        long heapMax = (Runtime.getRuntime().maxMemory()) / (1024 * 1024);
        SmartDashboard.putNumber("JVM/Heap Used (MB)", heapUsed);
        SmartDashboard.putNumber("JVM/Heap Max (MB)", heapMax);
    }

    // ---------- Temperatura ----------
    private double readCpuTempC() {
        try {
            // Método direto no Linux (milésimos de °C)
            Path p = Paths.get("/sys/class/thermal/thermal_zone0/temp");
            if (Files.exists(p)) {
                String v = Files.readString(p).trim();
                return Integer.parseInt(v) / 1000.0;
            }
        } catch (Exception ignore) {
        }
        // Fallback: vcgencmd
        try {
            Process proc = new ProcessBuilder("bash", "-lc", "vcgencmd measure_temp").start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line = br.readLine();
                if (line != null && line.startsWith("temp=")) {
                    String onlyNum = line.replaceAll("[^0-9.]", "");
                    return Double.parseDouble(onlyNum);
                }
            }
        } catch (Exception ignore) {
        }
        return -1; // falhou
    }

    // ---------- CPU usage ----------
    // Lê /proc/stat e calcula delta entre leituras (total vs idle)
    private double readCpuUsagePercent() {
        try (BufferedReader br = Files.newBufferedReader(Paths.get("/proc/stat"))) {
            String line = br.readLine();
            if (line == null || !line.startsWith("cpu "))
                return -1;

            String[] t = line.trim().split("\\s+");
            // Campos: cpu user nice system idle iowait irq softirq steal guest guest_nice
            // Usaremos total e idle+iowait
            long user = Long.parseLong(t[1]);
            long nice = Long.parseLong(t[2]);
            long system = Long.parseLong(t[3]);
            long idle = Long.parseLong(t[4]);
            long iowait = Long.parseLong(t[5]);
            long irq = Long.parseLong(t[6]);
            long softirq = Long.parseLong(t[7]);
            long steal = (t.length > 8) ? Long.parseLong(t[8]) : 0;

            double idleAll = idle + iowait;
            double nonIdle = user + nice + system + irq + softirq + steal;
            double total = idleAll + nonIdle;

            double percent = -1;
            if (lastCpuTotal >= 0 && lastCpuIdle >= 0) {
                double totald = total - lastCpuTotal;
                double idled = idleAll - lastCpuIdle;
                double used = (totald - idled);
                if (totald > 0)
                    percent = (used / totald) * 100.0;
            }

            lastCpuTotal = total;
            lastCpuIdle = idleAll;
            return percent;
        } catch (Exception e) {
            return -1;
        }
    }

    // ---------- Memória do sistema ----------
    private static class MemInfo {
        boolean ok;
        double totalMB;
        double usedMB;
        double usedPct;
    }

    private MemInfo readMemInfo() {
        MemInfo out = new MemInfo();
        try (BufferedReader br = Files.newBufferedReader(Paths.get("/proc/meminfo"))) {
            long memTotalKB = 0, memAvailableKB = 0;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("MemTotal:")) {
                    memTotalKB = parseKb(line);
                } else if (line.startsWith("MemAvailable:")) {
                    memAvailableKB = parseKb(line);
                }
            }
            if (memTotalKB > 0 && memAvailableKB > 0) {
                double totalMB = memTotalKB / 1024.0;
                double usedMB = (memTotalKB - memAvailableKB) / 1024.0;
                double usedPct = (usedMB / totalMB) * 100.0;
                out.ok = true;
                out.totalMB = round1(totalMB);
                out.usedMB = round1(usedMB);
                out.usedPct = round1(usedPct);
            }
        } catch (Exception ignore) {
        }
        return out;
    }

    private long parseKb(String line) {
        String num = line.replaceAll("[^0-9]", "");
        if (num.isEmpty())
            return 0;
        return Long.parseLong(num);
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
