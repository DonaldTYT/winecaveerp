package com.kikyosoft.ai.comfy;

import java.io.File;

public class ComfyImageToTextRequest {
    private final File workflowFile;
    private final byte[] imageBytes;
    private final String fileName;
    private final String promptText;

    public ComfyImageToTextRequest(File workflowFile, byte[] imageBytes, String fileName, String promptText) {
        this.workflowFile = workflowFile;
        this.imageBytes = imageBytes;
        this.fileName = fileName;
        this.promptText = promptText;
    }

    public File getWorkflowFile() { return workflowFile; }
    public byte[] getImageBytes() { return imageBytes; }
    public String getFileName() { return fileName; }
    public String getPromptText() { return promptText; }
}
