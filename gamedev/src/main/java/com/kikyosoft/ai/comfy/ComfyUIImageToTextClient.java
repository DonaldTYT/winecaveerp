package com.kikyosoft.ai.comfy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kikyosoft.ai.utils.AiClientBase;

import okhttp3.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ComfyUIImageToTextClient extends AiClientBase {

    private static final String BASE_URL = "http://192.168.1.204:8002";
    private static final String WS_URL = "ws://192.168.1.204:8002/ws";

    // Replace these with your actual node IDs from imageToText.json
    private static final String IMAGE_NODE_ID = "2";
    private static final String PROMPT_NODE_ID = "1";
    private static final String PREVIEW_TEXT_NODE_ID = "3";

    private final OkHttpClient httpClient;
    private final ObjectMapper mapper;

    public ComfyUIImageToTextClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(20))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .build();
        this.mapper = new ObjectMapper();
    }

    public static void main(String[] args) throws Exception {
        ComfyUIImageToTextClient client = new ComfyUIImageToTextClient();

        String fileName = "c:/tmp/IMG_0310.PNG";
        File workflowFile = new File("c:/tmp/imageToText_api.json");
        File imageFile = new File(fileName);
        InputStream imgStream = new FileInputStream(imageFile);
        String promptText = "Please describe this image.";

        String result = client.runWorkflow(workflowFile, imgStream , null, promptText);
        System.out.println("=== Preview as Text output ===");
        System.out.println(result);
    }

    public String runWorkflow(File workflowFile, InputStream imageStream, String fileName,String promptText) throws Exception {
        String clientId = UUID.randomUUID().toString();
        String promptId = UUID.randomUUID().toString();

        // 1) upload image
        UploadResult upload = uploadImage(imageStream,fileName);

        // 2) load workflow json
        Map<String, Object> workflow = loadWorkflow(workflowFile);

        // 3) patch workflow
        patchWorkflow(workflow, upload.fileName, promptText);

        // 4) open websocket
        CompletableFuture<Void> executionDone = new CompletableFuture<>();
        WebSocket ws = openWebSocket(clientId, promptId, executionDone);

        try {
            // 5) queue prompt
            queuePrompt(workflow, clientId, promptId);

            // 6) wait until execution is complete
            executionDone.get(10, TimeUnit.MINUTES);

            // 7) fetch history
            JsonNode history = getHistory(promptId);

            // 8) extract Preview as Text node output
//            return extractPreviewText(history, promptId, PREVIEW_TEXT_NODE_ID);
            return extractText(history, promptId);

        } finally {
            ws.close(1000, "done");
        }
    }

    private UploadResult uploadImage(InputStream imageStream,String fileName) throws IOException {
    	byte[] bytes = imageStream.readAllBytes();
    	
        RequestBody fileBody = RequestBody.create(bytes, MediaType.parse("application/octet-stream"));

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", fileName == null ? "imageFile": fileName, fileBody)
                .addFormDataPart("type", "input")
                .addFormDataPart("overwrite", "true")
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/upload/image")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Upload failed: " + response.code() + " " + response.message());
            }

            String json = response.body().string();
            JsonNode root = mapper.readTree(json);

            // ComfyUI usually returns fields like "name", "subfolder", "type"
            String name = textOrNull(root, "name");
            if (name == null || name.isBlank()) {
                name = textOrNull(root, "filename");
            }
            if (name == null || name.isBlank()) {
                throw new IOException("Upload response does not contain image filename: " + json);
            }

            String subfolder = textOrNull(root, "subfolder");
            String type = textOrNull(root, "type");

            return new UploadResult(name, subfolder, type);
        }
    }

    private Map<String, Object> loadWorkflow(File workflowFile) throws IOException {
        return mapper.readValue(workflowFile, new TypeReference<Map<String, Object>>() {});
    }
    
    @SuppressWarnings("unchecked")
    private void patchWorkflow(Map<String, Object> workflow, String uploadedImageFileName, String promptText) {
        Map<String, Object> imageNode = (Map<String, Object>) workflow.get(IMAGE_NODE_ID);
        if (imageNode == null) {
            throw new IllegalArgumentException("Image node not found: " + IMAGE_NODE_ID);
        }
        Map<String, Object> imageInputs = (Map<String, Object>) imageNode.get("inputs");
        if (imageInputs == null) {
            throw new IllegalArgumentException("Image node inputs missing: " + IMAGE_NODE_ID);
        }
        imageInputs.put("image", uploadedImageFileName);

        Map<String, Object> promptNode = (Map<String, Object>) workflow.get(PROMPT_NODE_ID);
        if (promptNode == null) {
            throw new IllegalArgumentException("Prompt node not found: " + PROMPT_NODE_ID);
        }
        Map<String, Object> promptInputs = (Map<String, Object>) promptNode.get("inputs");
        if (promptInputs == null) {
            throw new IllegalArgumentException("Prompt node inputs missing: " + PROMPT_NODE_ID);
        }
        promptInputs.put("custom_prompt", promptText);
    }

    private WebSocket openWebSocket(String clientId, String promptId, CompletableFuture<Void> executionDone) {
        String fullWsUrl = WS_URL + "?clientId=" + urlEncode(clientId);

        Request request = new Request.Builder()
                .url(fullWsUrl)
                .build();

        return httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JsonNode msg = mapper.readTree(text);
                    String type = textOrNull(msg, "type");

                    if ("execution_error".equals(type)) {
                        executionDone.completeExceptionally(
                                new RuntimeException("ComfyUI execution error: " + msg)
                        );
                        return;
                    }

                    if ("executing".equals(type)) {
                        JsonNode data = msg.get("data");
                        if (data != null) {
                            JsonNode node = data.get("node");
                            String wsPromptId = textOrNull(data, "prompt_id");

                            if (wsPromptId != null && wsPromptId.equals(promptId)
                                    && (node == null || node.isNull())) {
                                executionDone.complete(null);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore non-JSON/binary preview messages.
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                executionDone.completeExceptionally(t);
            }
        });
    }

    private void queuePrompt(Map<String, Object> workflow, String clientId, String promptId) throws IOException {
        Map<String, Object> payload = Map.of(
                "prompt", workflow,
                "client_id", clientId,
                "prompt_id", promptId
        );

        String jsonBody = mapper.writeValueAsString(payload);

        Request request = new Request.Builder()
                .url(BASE_URL + "/prompt")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String err = response.body() != null ? response.body().string() : "";
                throw new IOException("Queue prompt failed: " + response.code() + " " + err);
            }
        }
    }

    private JsonNode getHistory(String promptId) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/history/" + urlEncode(promptId))
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Get history failed: " + response.code() + " " + response.message());
            }
            String json = response.body().string();
            return mapper.readTree(json);
        }
    }
    
    private String extractText(JsonNode historyRoot, String promptId) {
        JsonNode run = historyRoot.get(promptId);
        if (run == null || run.isNull()) {
            throw new IllegalStateException("History does not contain prompt_id: " + promptId);
        }

        JsonNode outputs = run.get("outputs");
        if (outputs == null || outputs.isNull()) {
            throw new IllegalStateException("No outputs found in history for prompt_id: " + promptId);
        }

        String text = tryExtract(outputs.get("1"));
        if (text != null) return text;

        text = tryExtract(outputs.get("3"));
        if (text != null) return text;

        return outputs.toPrettyString();
    }

    private String tryExtract(JsonNode node) {
        if (node == null || node.isNull()) return null;

        // Most common for QwenVL
        if (node.has("response")) {
            JsonNode r = node.get("response");
            if (r.isTextual()) return r.asText();
            if (r.isArray()) return joinArray(r);
        }

        if (node.has("text")) {
            JsonNode t = node.get("text");
            if (t.isTextual()) return t.asText();
            if (t.isArray()) return joinArray(t);
        }

        if (node.has("string")) {
            return node.get("string").asText();
        }

        if (node.has("ui")) {
            JsonNode ui = node.get("ui");

            if (ui.has("text")) {
                JsonNode t = ui.get("text");
                if (t.isTextual()) return t.asText();
                if (t.isArray()) return joinArray(t);
            }

            if (ui.has("string")) {
                JsonNode s = ui.get("string");
                if (s.isTextual()) return s.asText();
                if (s.isArray()) return joinArray(s);
            }
        }

        return null;
    }

    private String extractPreviewText(JsonNode historyRoot, String promptId, String previewNodeId) {
        JsonNode run = historyRoot.get(promptId);
        if (run == null || run.isNull()) {
            throw new IllegalStateException("History does not contain prompt_id: " + promptId);
        }

        JsonNode outputs = run.get("outputs");
        if (outputs == null || outputs.isNull()) {
            throw new IllegalStateException("No outputs found in history for prompt_id: " + promptId);
        }

        JsonNode nodeOut = outputs.get(previewNodeId);
        if (nodeOut == null || nodeOut.isNull()) {
            throw new IllegalStateException("Preview node output not found: " + previewNodeId);
        }

        // Common patterns used by text preview/custom nodes
        JsonNode text = nodeOut.get("text");
        if (text != null && text.isTextual()) {
            return text.asText();
        }
        if (text != null && text.isArray()) {
            return joinArray(text);
        }

        JsonNode ui = nodeOut.get("ui");
        if (ui != null && !ui.isNull()) {
            JsonNode uiText = ui.get("text");
            if (uiText != null && uiText.isTextual()) {
                return uiText.asText();
            }
            if (uiText != null && uiText.isArray()) {
                return joinArray(uiText);
            }
        }

        JsonNode string = nodeOut.get("string");
        if (string != null && string.isTextual()) {
            return string.asText();
        }

        return nodeOut.toPrettyString();
    }

    private static String joinArray(JsonNode arrayNode) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arrayNode.size(); i++) {
            if (i > 0) sb.append("\n");
            sb.append(arrayNode.get(i).asText());
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static class UploadResult {
        final String fileName;
        final String subfolder;
        final String type;

        UploadResult(String fileName, String subfolder, String type) {
            this.fileName = fileName;
            this.subfolder = subfolder;
            this.type = type;
        }
    }
}