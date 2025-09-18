package frc.robot.subsystems;

import com.orbbec.obsensor.*;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.cscore.CvSource;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.imgcodecs.Imgcodecs;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

@SuppressWarnings("deprecation")
public class DepthCamera extends SubsystemBase {

    private CvSource outputStream;
    private Mat frameMat;

    private OBContext obContext;
    private final Object pipeLock = new Object();
    private Pipeline pipeline;

    private volatile double distanceToWall = -1.0;
    private volatile boolean initialized = false;

    private volatile byte[] lastDepthBytes = null;
    private volatile int depthWidth = 0;
    private volatile int depthHeight = 0;

    public DepthCamera() {
        outputStream = CameraServer.getInstance().putVideo("Orbbec Color", 640, 480);
        frameMat = new Mat(480, 640, CvType.CV_8UC3);
        initCamera();
    }

    // ------------------- NOVO MÉTODO -------------------
    /** 
     * Retorna uma cópia defensiva do último frame colorido da Orbbec.
     * Se ainda não houver frame, retorna um Mat vazio.
     */
    public synchronized Mat getLastFrame() {
        return frameMat.empty() ? new Mat() : frameMat.clone();
    }
    // ---------------------------------------------------

    private void initCamera() {
        obContext = new OBContext(new DeviceChangedCallback() {
            @Override
            public void onDeviceAttach(DeviceList deviceList) {
                initPipeline(deviceList);
                startStreams();
                deviceList.close();
            }

            @Override
            public void onDeviceDetach(DeviceList deviceList) {
                stopStreams();
                deInitPipeline();
                deviceList.close();
            }
        });

        DeviceList deviceList = obContext.queryDevices();
        if (deviceList != null && deviceList.getDeviceCount() > 0) {
            initPipeline(deviceList);
            startStreams();
        }
        if (deviceList != null) deviceList.close();
    }

    private void initPipeline(DeviceList deviceList) {
        synchronized (pipeLock) {
            if (pipeline != null) pipeline.close();
            try (Device device = deviceList.getDevice(0)) {
                pipeline = new Pipeline(device);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void deInitPipeline() {
        synchronized (pipeLock) {
            try {
                if (pipeline != null) { pipeline.close(); pipeline = null; }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void startStreams() {
        synchronized (pipeLock) {
            if (pipeline == null) return;

            Config config = new Config();
            try (StreamProfileList colorProfileList = pipeline.getStreamProfileList(SensorType.COLOR)) {
                VideoStreamProfile colorProfile = colorProfileList.getStreamProfile(0).as(StreamType.VIDEO);
                if (colorProfile != null) config.enableStream(colorProfile);
                colorProfile.close();
            } catch (Exception e) { e.printStackTrace(); }

            try (StreamProfileList depthProfileList = pipeline.getStreamProfileList(SensorType.DEPTH)) {
                VideoStreamProfile depthProfile = depthProfileList.getStreamProfile(0).as(StreamType.VIDEO);
                if (depthProfile != null) config.enableStream(depthProfile);
                depthProfile.close();
            } catch (Exception e) { e.printStackTrace(); }

            try {
                pipeline.start(config, new FrameSetCallback() {
                    @Override
                    public void onFrameSet(FrameSet frameSet) {
                        processFrameSet(frameSet);
                        frameSet.close();
                    }
                });
                config.close();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void stopStreams() {
        synchronized (pipeLock) {
            try { if (pipeline != null) pipeline.stop(); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private synchronized void processFrameSet(FrameSet frameSet) {
        try (ColorFrame colorFrame = frameSet.getFrame(FrameType.COLOR)) {
            if (colorFrame != null) sendFrameToCameraServer(colorFrame);
        } catch (Exception e) { e.printStackTrace(); }

        try (DepthFrame depthFrame = frameSet.getFrame(FrameType.DEPTH)) {
            if (depthFrame != null) processDepthFrame(depthFrame);
        } catch (Exception e) { e.printStackTrace(); }

        updateSmartDashboard();
    }

    private void sendFrameToCameraServer(ColorFrame colorFrame) {
        try {
            byte[] data = new byte[colorFrame.getDataSize()];
            colorFrame.getData(data);

            if (data.length == frameMat.total() * frameMat.channels()) {
                frameMat.put(0, 0, data);
            } else {
                Mat encoded = new Mat(1, data.length, CvType.CV_8UC1);
                encoded.put(0, 0, data);
                frameMat = Imgcodecs.imdecode(encoded, Imgcodecs.IMREAD_COLOR);
            }

            outputStream.putFrame(frameMat);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void processDepthFrame(DepthFrame frame) {
        int width  = frame.getWidth();
        int height = frame.getHeight();

        byte[] data = new byte[frame.getDataSize()];
        frame.getData(data);

        this.lastDepthBytes = data;
        this.depthWidth = width;
        this.depthHeight = height;

        int cx = width / 2, cy = height / 2;
        int idx = (cy * width + cx) * 2;
        if (idx + 1 < data.length) {
            int d1 = data[idx] & 0xFF;
            int d2 = data[idx + 1] & 0xFF;
            int depth = (d2 << 8) | d1;
            this.distanceToWall = depth > 0 ? depth / 1000.0 : -1.0;
        }

        if (!initialized) initialized = true;
    }

    private void updateSmartDashboard() {
        SmartDashboard.putBoolean("Camera Initialized", initialized);
        SmartDashboard.putNumber("Distance to Wall (m)", distanceToWall);
        SmartDashboard.putNumber("Distance to Wall (cm)", distanceToWall > 0 ? distanceToWall * 100.0 : -1.0);
    }

    public boolean isInitialized() { return initialized; }
    public double getDistanceToWall() { return distanceToWall; }

    public byte[] getDepthBytesSnapshot() {
        byte[] src = this.lastDepthBytes;
        if (src == null) return null;
        byte[] copy = new byte[src.length];
        System.arraycopy(src, 0, copy, 0, src.length);
        return copy;
    }
    public int getDepthWidth()  { return depthWidth; }
    public int getDepthHeight() { return depthHeight; }

    public void destroyCamera() {
        synchronized(pipeLock) {
            try {
                if (pipeline != null) { pipeline.stop(); pipeline.close(); pipeline = null; }
                if (obContext != null) { obContext.close(); obContext = null; }
                initialized = false;
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
