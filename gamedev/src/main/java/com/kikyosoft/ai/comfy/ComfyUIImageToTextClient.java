package com.kikyosoft.ai.comfy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kikyosoft.ai.utils.AiClientBase;

import okhttp3.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ComfyUIImageToTextClient extends AiClientBase {

//    private static final String DEFAULT_SERVER_URL = "http://192.168.46.13:8190";
    private static final String DEFAULT_SERVER_URL = "http://1.208.108.242:33570/?token=cf6003e9b9339352e4e702c477fc9917a85abfd32f0576a47ccd2705e06a6d2c";
    private static final String SERVER_URL_PROPERTY = "comfy.server.url";
    private static final String SERVER_URL_ENV = "COMFY_SERVER_URL";
    public static final String DEFAULT_PROMPT_TEXT = "Please describe this image.";
    public static final String PROMPT_TEXT_PROPERTY = "comfy.imageToText.prompt";

    // Replace these with your actual node IDs from imageToText.json
    private static final String IMAGE_NODE_ID = "2";
    private static final String PROMPT_NODE_ID = "1";
    private static final String PREVIEW_TEXT_NODE_ID = "3";

    private final HttpUrl serverUrl;
    private final OkHttpClient httpClient;
    private final ObjectMapper mapper;

    public ComfyUIImageToTextClient() {
        this(resolveDefaultServerUrl());
    }

    /**
     * Creates a client for either a local ComfyUI URL or a remote URL whose
     * query parameters contain access credentials, for example:
     * https://example.trycloudflare.com/?token=...
     */
    public ComfyUIImageToTextClient(String serverUrl) {
        this.serverUrl = parseServerUrl(serverUrl);
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.mapper = new ObjectMapper();
    }

    public static void main(String[] args) throws Exception {
        ComfyUIImageToTextClient client = new ComfyUIImageToTextClient();

        String fileName = "c:/tmp/IMG_0310.PNG";
        File workflowFile = new File("c:/tmp/imageToText_api.json");
        File imageFile = new File(fileName);
        InputStream imgStream = new FileInputStream(imageFile);
        String promptText = resolveDefaultPromptText();

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
        patchWorkflow(workflow, upload.fileName, resolvePromptText(promptText));

        // 4) open websocket
        CompletableFuture<Void> executionDone = new CompletableFuture<>();
        WebSocket ws = openWebSocket(clientId, promptId, executionDone);

        try {
            // 5) queue prompt
            queuePrompt(workflow, clientId, promptId);

            // 6) wait until execution is complete. History polling also covers
            // workflows whose outputs were returned entirely from ComfyUI cache.
            waitForExecution(promptId, executionDone, 10, TimeUnit.MINUTES);

            // 7) fetch history
            JsonNode history = waitForHistory(promptId, 30, TimeUnit.SECONDS);

            // 8) extract Preview as Text node output
//            return extractPreviewText(history, promptId, PREVIEW_TEXT_NODE_ID);
            return extractText(history, promptId);

        } finally {
            ws.close(1000, "done");
        }
    }

    private UploadResult uploadImage(InputStream imageStream,String fileName) throws IOException {
        byte[] bytes = readAllBytes(imageStream);
    	
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), bytes);

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", fileName == null ? "imageFile": fileName, fileBody)
                .addFormDataPart("type", "input")
                .addFormDataPart("overwrite", "true")
                .build();

        Request request = new Request.Builder()
                .url(buildHttpUrl("upload/image"))
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
            if (isBlank(name)) {
                name = textOrNull(root, "filename");
            }
            if (isBlank(name)) {
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
        HttpUrl wsHttpUrl = buildHttpUrl("ws").newBuilder()
                .addQueryParameter("clientId", clientId)
                .build();
        String fullWsUrl = toWebSocketUrl(wsHttpUrl);

        Request request = new Request.Builder()
                .url(fullWsUrl)
                .build();

        return httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JsonNode msg = mapper.readTree(text);
                    String type = textOrNull(msg, "type");
                    JsonNode data = msg.get("data");
                    String wsPromptId = data == null ? null : textOrNull(data, "prompt_id");

                    if (wsPromptId != null && !wsPromptId.equals(promptId)) {
                        return;
                    }

                    if ("execution_error".equals(type)) {
                        executionDone.completeExceptionally(
                                new RuntimeException("ComfyUI execution error: " + msg)
                        );
                        return;
                    }

                    if ("execution_interrupted".equals(type)) {
                        executionDone.completeExceptionally(
                                new RuntimeException("ComfyUI execution interrupted: " + msg)
                        );
                        return;
                    }

                    // Current ComfyUI sends this for both normally executed and
                    // fully cached prompts.
                    if ("execution_success".equals(type)) {
                        executionDone.complete(null);
                        return;
                    }

                    if ("executing".equals(type)) {
                        if (data != null) {
                            JsonNode node = data.get("node");

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

    private void waitForExecution(String promptId, CompletableFuture<Void> executionDone,
                                  long timeout, TimeUnit timeoutUnit) throws Exception {
        long deadline = System.nanoTime() + timeoutUnit.toNanos(timeout);
        while (true) {
            long remainingNanos = deadline - System.nanoTime();
            if (remainingNanos <= 0) {
                throw new TimeoutException("Timed out waiting for ComfyUI prompt: " + promptId);
            }

            long waitMillis = Math.min(TimeUnit.NANOSECONDS.toMillis(remainingNanos), 1000L);
            try {
                executionDone.get(Math.max(waitMillis, 1L), TimeUnit.MILLISECONDS);
                return;
            } catch (TimeoutException timeoutException) {
                try {
                    JsonNode history = getHistory(promptId);
                    if (hasCompletedHistory(history, promptId)) {
                        executionDone.complete(null);
                        return;
                    }
                } catch (IOException ignored) {
                    // The WebSocket remains the primary completion channel.
                }
            }
        }
    }

    private JsonNode waitForHistory(String promptId, long timeout, TimeUnit timeoutUnit) throws Exception {
        long deadline = System.nanoTime() + timeoutUnit.toNanos(timeout);
        JsonNode lastHistory = null;
        while (System.nanoTime() < deadline) {
            lastHistory = getHistory(promptId);
            if (hasHistoryOutputs(lastHistory, promptId)) {
                return lastHistory;
            }
            Thread.sleep(200L);
        }
        throw new TimeoutException("ComfyUI completed but history was not available for prompt: "
                + promptId + "; last history: " + lastHistory);
    }

    private static boolean hasCompletedHistory(JsonNode history, String promptId) {
        if (history == null) {
            return false;
        }
        JsonNode run = history.get(promptId);
        if (run == null || run.isNull()) {
            return false;
        }

        JsonNode status = run.get("status");
        if (status != null && status.path("completed").asBoolean(false)) {
            return true;
        }

        return hasHistoryOutputs(history, promptId);
    }

    private static boolean hasHistoryOutputs(JsonNode history, String promptId) {
        if (history == null) {
            return false;
        }
        JsonNode run = history.get(promptId);
        if (run == null || run.isNull()) {
            return false;
        }
        JsonNode outputs = run.get("outputs");
        return outputs != null && outputs.isObject() && outputs.size() > 0;
    }

    private void queuePrompt(Map<String, Object> workflow, String clientId, String promptId) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("prompt", workflow);
        payload.put("client_id", clientId);
        payload.put("prompt_id", promptId);

        String jsonBody = mapper.writeValueAsString(payload);

        Request request = new Request.Builder()
                .url(buildHttpUrl("prompt"))
                .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
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
                .url(buildHttpUrl("history").newBuilder()
                        .addPathSegment(promptId)
                        .build())
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

        return outputs.toString();
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

        return nodeOut.toString();
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

    private HttpUrl buildHttpUrl(String relativePath) {
        String basePath = serverUrl.encodedPath();
        if (!basePath.endsWith("/")) {
            basePath += "/";
        }
        while (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return serverUrl.newBuilder()
                .encodedPath(basePath + relativePath)
                .build();
    }

    private static String toWebSocketUrl(HttpUrl httpUrl) {
        String url = httpUrl.toString();
        if (url.startsWith("https://")) {
            return "wss://" + url.substring("https://".length());
        }
        return "ws://" + url.substring("http://".length());
    }

    private static HttpUrl parseServerUrl(String serverUrl) {
        if (isBlank(serverUrl)) {
            throw new IllegalArgumentException("ComfyUI server URL must not be blank");
        }
        HttpUrl parsed = HttpUrl.parse(serverUrl.trim());
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid ComfyUI server URL");
        }
        return parsed;
    }

    private static String resolveDefaultServerUrl() {
        String configuredUrl = System.getProperty(SERVER_URL_PROPERTY);
        if (isBlank(configuredUrl)) {
            configuredUrl = System.getenv(SERVER_URL_ENV);
        }
        return isBlank(configuredUrl) ? DEFAULT_SERVER_URL : configuredUrl;
    }

    public static String resolveDefaultPromptText() {
        String configuredPrompt = System.getProperty(PROMPT_TEXT_PROPERTY);
        return isBlank(configuredPrompt) ? DEFAULT_PROMPT_TEXT : configuredPrompt;
    }

    public static String resolvePromptText(String promptText) {
        return isBlank(promptText) ? resolveDefaultPromptText() : promptText;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
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
