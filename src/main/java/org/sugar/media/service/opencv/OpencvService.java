package org.sugar.media.service.opencv;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Date:2025/01/08 17:48:14
 * Author：Tobin
 * Description:
 */


@Slf4j
public class OpencvService {

    private static final int DETECTION_INTERVAL = 5; // 每 5 帧进行一次目标检测

    private static final String YOLO_CFG = "/Users/tobin/env/yolov3.cfg";
    private static final String YOLO_WEIGHTS = "/Users/tobin/env/yolov3.weights";
    String[] objects = {"person", "bicycle", "car", "motorbike", "aeroplane", "bus", "train", "truck", "boat", "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "sofa", "pottedplant", "bed", "diningtable", "toilet", "tvmonitor", "laptop", "mouse", "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"};


    private Net net;
    private VideoCapture capture;

    public OpencvService() {
        // 加载 YOLO 模型
        log.info("启动headless mode...");
        System.setProperty("java.awt.headless", "true");
        log.info("动态库路径：{}", System.getProperty("java.library.path"));
        log.info("开始加载OpenCV库...");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        log.info("load success");


        // 加载 YOLO 模型
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        net = Dnn.readNetFromDarknet(YOLO_CFG, YOLO_WEIGHTS);
        // capture = new VideoCapture("/Users/tobin/env/1.mp4"); // ZLMediaKit 转码后的流地址


    }


    public void processVideo(String videoPath, Mat frame) throws IOException, InterruptedException {

        capture = new VideoCapture("rtsp://admin:Aa12345678@192.168.31.28:554//Streaming/Channels/1");

        if (!capture.isOpened()) {
            System.out.println("Error: Unable to connect to RTSP stream.");
            return;
        }

        String command = String.format("ffmpeg -re -y -stream_loop -1 " +
                "-f rawvideo -vcodec rawvideo -pix_fmt bgr24 -s 1920x1080 -r 25.0 -i " +
                "- -c:v libx264 -an -crf 32 -b:v 1200k -bf 0 -g 25.0 " +
                "-pix_fmt yuv420p -preset ultrafast -f flv %s", rtmpUrl);
        Process process = new ProcessBuilder(command.split(" ")).start();

        // 获取FFmpeg的输入流
        OutputStream outputStream = process.getOutputStream();
        // 加载YOLO模型
        // 假设classNames是事先准备好的YOLO类别名称列表
        // 创建临时文件夹存储每一帧图像
        File tempDir = new File("temp_frames");
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
        ThreadPoolExecutor threadPoolExecutor = ThreadUtil.newExecutor();
        ExecutorService executorService = Executors.newFixedThreadPool(4); // 根据需求调整线程数

        int frameCount = 0;
        // 逐帧读取视频并处理
        while (capture.read(frame)) {

            // 如果达到一定的帧间隔，进行目标检测

            // 直接推送不进行检测的帧

            Mat currentFrame = frame.clone();
//            List<Mat> detections = detectObjects(currentFrame);
//            drawBoundingBoxes(currentFrame, detections);
            pushFrameToRTMP(currentFrame, outputStream);


            // 更新帧计数
            frameCount++;
        }

        capture.release();
        process.waitFor();
        process.destroy();
    }


    public List<Mat> detectObjects(Mat frame) {
        // Create blob from image
        Mat blob = Dnn.blobFromImage(frame, 1.0, new Size(416, 416), new Scalar(0, 0, 0), true, false);
        net.setInput(blob);

        // Get the output layer names
        List<String> outLayerNames = net.getUnconnectedOutLayersNames();
        List<Mat> detections = new ArrayList<>();

        // Forward pass
        net.forward(detections, outLayerNames);

        return detections;
    }

    String rtmpUrl = "rtmp://192.168.31.208/live/100001_ai";

    public void drawBoundingBoxes(Mat frame, List<Mat> detections) throws IOException {


        for (Mat detection : detections) {
            // 遍历每个检测到的目标，获取边界框坐标
            for (int i = 0; i < detection.rows(); i++) {
                float confidence = (float) detection.get(i, 4)[0];
                if (confidence > 0.5) {
                    int x = (int) (detection.get(i, 0)[0] * frame.cols());
                    int y = (int) (detection.get(i, 1)[0] * frame.rows());
                    int w = (int) (detection.get(i, 2)[0] * frame.cols());
                    int h = (int) (detection.get(i, 3)[0] * frame.rows());

                    Rect rect = new Rect(x, y, w, h);
                    Imgproc.rectangle(frame, rect, new Scalar(0, 255, 0), 2);
                    double v = detection.get(i, 5)[0];
                    String label = objects[Convert.toInt(v)];
                    Imgproc.putText(frame, label, new Point(x, y - 10), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(0, 0, 255), 2);

//                    String filename = "./frame_" + String.format("%04d", RandomUtil.randomInt(1,10000)) + ".png"; // 文件名
//                    Imgcodecs.imwrite(filename, frame); // 保存为图片文件


                }
            }
        }
    }


    private void pushFrameToRTMP(Mat frame, OutputStream outputStream) throws IOException {
        // 编码图像为 JPEG 格式
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", frame, matOfByte);

        // 将图像数据写入FFmpeg的管道
        outputStream.write(matOfByte.toArray());
        outputStream.flush();
    }

}
