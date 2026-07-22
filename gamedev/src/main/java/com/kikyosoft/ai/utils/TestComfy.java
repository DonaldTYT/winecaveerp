package com.kikyosoft.ai.utils;

import com.kikyosoft.ai.comfy.ComfyUIImageToTextQueueService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class TestComfy{

    public static void main(String[] args) throws Exception {
//        byte[] imageBytes = Files.readAllBytes(Paths.get("c:/tmp/IMG_0310.PNG"));
        byte[] imageBytes = Files.readAllBytes(Paths.get("c:/tmp/unknown_label.jpg"));

        File workflowFile = new File("c:/tmp/imageToText_api.json");
        ComfyUIImageToTextQueueService comfyQueue =
                new ComfyUIImageToTextQueueService(workflowFile);
        comfyQueue.start();

        try {
            String jobId = comfyQueue.submit(
                    imageBytes,
                    "test.jpg",
                    "Please check whether this is a wine label. If yes, tell me the wine detail"
            );
            System.out.println("Comfy jobId = " + jobId);

            String result = comfyQueue.waitForResult(jobId, 15, TimeUnit.MINUTES);
            System.out.println("Result = " + result);
        } finally {
            comfyQueue.shutdown();
        }
    }
}
