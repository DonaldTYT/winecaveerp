package com.kikyosoft.ai.comfy;

import com.kikyosoft.ai.queue.AIJobHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ComfyImageToTextHandler implements AIJobHandler<ComfyImageToTextRequest, String> {

    private final ComfyUIImageToTextClient client;

    public ComfyImageToTextHandler(ComfyUIImageToTextClient client) {
        this.client = client;
    }

    @Override
    public String execute(ComfyImageToTextRequest request) throws Exception {
        try (InputStream is = new ByteArrayInputStream(request.getImageBytes())) {
            return client.runWorkflow(
                    request.getWorkflowFile(),
                    is,
                    request.getFileName(),
                    request.getPromptText()
            );
        }
    }
}
