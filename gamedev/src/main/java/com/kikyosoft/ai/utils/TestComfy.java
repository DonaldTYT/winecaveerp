package com.kikyosoft.ai.utils;

import com.kikyosoft.ai.queue.AIJobQueueService;
import com.kikyosoft.ai.queue.ResultNotifier;
import com.kikyosoft.ai.comfy.ComfyImageToTextHandler;
import com.kikyosoft.ai.comfy.ComfyImageToTextRequest;
import com.kikyosoft.ai.comfy.ComfyUIImageToTextClient;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class TestComfy{
	
	
    public static void main(String[] args) throws Exception {

    	
        ComfyUIImageToTextClient comfyClient = new ComfyUIImageToTextClient();
        ComfyImageToTextHandler comfyHandler = new ComfyImageToTextHandler(comfyClient);

        AIJobQueueService<ComfyImageToTextRequest, String> comfyQueue =
                new AIJobQueueService<>("comfy-image-to-text", comfyHandler);

        comfyQueue.start();

//        byte[] imageBytes = Files.readAllBytes(Path.of("c:/tmp/IMG_0310.PNG"));
        byte[] imageBytes = Files.readAllBytes(Path.of("c:/tmp/unknown_label.jpg"));

        ComfyImageToTextRequest req = new ComfyImageToTextRequest(
                new File("c:/tmp/imageToText_api.json"),
                imageBytes,
//                "test.png",
                "test.jpg",
//                "Please describe this image."
                "Please check whether this is a wine label. If yes, tell me the wine detail"
        );

        /*
        String jobId = comfyQueue.submit(req);
        System.out.println("Comfy jobId = " + jobId);

        String result = comfyQueue.waitForResult(jobId, 15, TimeUnit.MINUTES);
        System.out.println(result);
        */
        
        
        ResultNotifier<String> notifier = new ResultNotifier<String>() {
        public boolean notified = false;
	    @Override
	    public void notifySuccess(String jobId, String result) {
	        System.out.println("Job success: " + jobId);
	        System.out.println("Result = " + result);
	        notified = true;
	    }

	    @Override
	    public void notifyFailure(String jobId, String errorMessage, Throwable cause) {
	        System.out.println("Job failed: " + jobId);
	        System.out.println("Error = " + errorMessage);
	        notified = true;
	        if (cause != null) {
	            cause.printStackTrace();
	        }
	    }

		@Override
		public boolean notified() {
			return notified;
		}
        };
        
    	String jobId = comfyQueue.submit(req, notifier);
    	
    	for(int i=0;i<20;i++) {
	        System.out.println("waiting");
	        if(notifier.notified()) break;
    		Thread.sleep(5000);
    	}
	    System.out.println("process ended");
    }
}