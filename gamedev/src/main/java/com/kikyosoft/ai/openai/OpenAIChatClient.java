package com.kikyosoft.ai.openai;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kikyosoft.ai.utils.AiClientBase;

/**
 * Non-interactive OpenAI Chat Completions client with local Java tool calling.
 *
 * <p>The {@link #chat(String, String, List)} method sends one system role and
 * one user message. If OpenAI requests tools, this class executes the matching
 * {@link ChatTool} implementations, sends their results back, and returns the
 * final assistant text.</p>
 */
public class OpenAIChatClient extends AiClientBase {

    private static final String DEFAULT_OPENAI_URL =
            "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4o-mini";
    private static final int DEFAULT_MAX_TOOL_ROUNDS = 8;

    private final String apiKey;
    private final String openAIUrl;
    private final String model;
    private final int maxToolRounds;

    /**
     * Uses OPENAI_API_KEY and optional openai.chat.url/openai.chat.model system
     * properties.
     */
    public OpenAIChatClient() {
        this(requiredEnvironmentValue("OPENAI_API_KEY"),
                System.getProperty("openai.chat.url", DEFAULT_OPENAI_URL),
                System.getProperty("openai.chat.model", DEFAULT_MODEL),
                DEFAULT_MAX_TOOL_ROUNDS);
    }

    public OpenAIChatClient(String apiKey) {
        this(apiKey, DEFAULT_OPENAI_URL, DEFAULT_MODEL, DEFAULT_MAX_TOOL_ROUNDS);
    }

    public OpenAIChatClient(String apiKey, String model) {
        this(apiKey, DEFAULT_OPENAI_URL, model, DEFAULT_MAX_TOOL_ROUNDS);
    }

    public OpenAIChatClient(String apiKey,
                            String openAIUrl,
                            String model,
                            int maxToolRounds) {
        this.apiKey = requireNotBlank(apiKey, "apiKey");
        this.openAIUrl = requireNotBlank(openAIUrl, "openAIUrl");
        this.model = requireNotBlank(model, "model");
        if (maxToolRounds <= 0) {
            throw new IllegalArgumentException("maxToolRounds must be greater than zero");
        }
        this.maxToolRounds = maxToolRounds;
    }

    public String chat(String role, String chatMessage) throws Exception {
        return chat(role, chatMessage, Collections.<ChatTool>emptyList());
    }

    /**
     * Sends a single non-interactive request and returns the final reply.
     *
     * @param role system instruction, for example "You are a helpful assistant."
     * @param chatMessage user message
     * @param availableTools tool definitions and their local Java executors
     */
    public String chat(String role,
                       String chatMessage,
                       List<? extends ChatTool> availableTools) throws Exception {
        String userMessage = requireNotBlank(chatMessage, "chatMessage");
        List<? extends ChatTool> tools = availableTools == null
                ? Collections.<ChatTool>emptyList()
                : availableTools;

        Map<String, ChatTool> toolByName = indexTools(tools);
        JSONArray toolDefinitions = buildToolDefinitions(tools);
        JSONArray messages = new JSONArray();

        if (role != null && !role.trim().isEmpty()) {
            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", role.trim()));
        }
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", userMessage));

        for (int round = 0; round < maxToolRounds; round++) {
            JSONObject request = new JSONObject()
                    .put("model", model)
                    .put("messages", messages);

            if (toolDefinitions.length() > 0) {
                request.put("tools", toolDefinitions);
                request.put("tool_choice", "auto");
            }

            JSONObject response = postJson(request);
            JSONObject assistantMessage = getAssistantMessage(response);
            JSONArray toolCalls = assistantMessage.optJSONArray("tool_calls");

            if (toolCalls == null || toolCalls.length() == 0) {
                return contentAsString(assistantMessage.opt("content"));
            }

            messages.put(assistantMessage);
            for (int i = 0; i < toolCalls.length(); i++) {
                JSONObject toolCall = toolCalls.getJSONObject(i);
                messages.put(executeToolCall(toolCall, toolByName));
            }
        }

        throw new IllegalStateException(
                "OpenAI exceeded the maximum tool-call rounds: " + maxToolRounds);
    }

    private JSONObject executeToolCall(JSONObject toolCall,
                                       Map<String, ChatTool> toolByName) throws JSONException {
        String toolCallId = toolCall.optString("id", "");
        JSONObject function = toolCall.optJSONObject("function");
        String toolName = function == null ? "" : function.optString("name", "");
        String argumentsText = function == null
                ? "{}"
                : function.optString("arguments", "{}");

        String toolOutput;
        if (toolCallId.trim().isEmpty()) {
            throw new IllegalArgumentException("OpenAI tool call is missing id: " + toolCall);
        }

        ChatTool tool = toolByName.get(toolName);
        if (tool == null) {
            toolOutput = errorJson("unknown_tool", toolName);
        } else {
            try {
                JSONObject arguments = new JSONObject(argumentsText);
                Object result = tool.execute(arguments);
                toolOutput = result == null ? "null" : result.toString();
            } catch (JSONException ex) {
                toolOutput = errorJson("invalid_tool_arguments", ex.getMessage());
            } catch (Exception ex) {
                toolOutput = errorJson("tool_execution_failed", ex.toString());
            }
        }

        return new JSONObject()
                .put("role", "tool")
                .put("tool_call_id", toolCallId)
                .put("content", toolOutput);
    }

    private Map<String, ChatTool> indexTools(List<? extends ChatTool> tools) {
        Map<String, ChatTool> result = new LinkedHashMap<String, ChatTool>();
        for (ChatTool tool : tools) {
            if (tool == null) {
                throw new IllegalArgumentException("availableTools contains null");
            }
            String name = requireValidToolName(tool.getName());
            if (result.put(name, tool) != null) {
                throw new IllegalArgumentException("Duplicate tool name: " + name);
            }
        }
        return result;
    }

    private JSONArray buildToolDefinitions(List<? extends ChatTool> tools) throws JSONException {
        JSONArray definitions = new JSONArray();
        for (ChatTool tool : tools) {
            String name = requireValidToolName(tool.getName());
            JSONObject function = new JSONObject()
                    .put("name", name)
                    .put("parameters", defaultParameters(tool.getParameters()));

            String description = tool.getDescription();
            if (description != null && !description.trim().isEmpty()) {
                function.put("description", description.trim());
            }

            definitions.put(new JSONObject()
                    .put("type", "function")
                    .put("function", function));
        }
        return definitions;
    }

    private static JSONObject defaultParameters(JSONObject parameters) throws JSONException {
        if (parameters != null) {
            return parameters;
        }
        return new JSONObject()
                .put("type", "object")
                .put("properties", new JSONObject());
    }

    private JSONObject postJson(JSONObject payload) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(openAIUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(120000);
        connection.setDoOutput(true);

        try {
            byte[] requestBytes = payload.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(requestBytes);
            }

            int status = connection.getResponseCode();
            InputStream responseStream = status >= 200 && status < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String responseBody = responseStream == null
                    ? ""
                    : readUtf8(responseStream);

            if (status < 200 || status >= 300) {
                throw new IOException("OpenAI HTTP " + status + ": " + responseBody);
            }
            return new JSONObject(responseBody);
        } finally {
            connection.disconnect();
        }
    }

    private static JSONObject getAssistantMessage(JSONObject response) {
        JSONArray choices = response.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
            throw new IllegalStateException("OpenAI response contains no choices: " + response);
        }
        JSONObject choice = choices.optJSONObject(0);
        JSONObject message = choice == null ? null : choice.optJSONObject("message");
        if (message == null) {
            throw new IllegalStateException("OpenAI response contains no assistant message: " + response);
        }
        return message;
    }

    private static String readUtf8(InputStream input) throws IOException {
        try (InputStream stream = input;
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = stream.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static String errorJson(String error, String detail) throws JSONException {
        return new JSONObject()
                .put("error", error)
                .put("detail", detail == null ? "" : detail)
                .toString();
    }

    private static String contentAsString(Object content) {
        return content == null || content == JSONObject.NULL ? "" : content.toString();
    }

    private static String requiredEnvironmentValue(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(name + " is not set");
        }
        return value.trim();
    }

    private static String requireNotBlank(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }

    private static String requireValidToolName(String name) {
        String value = requireNotBlank(name, "tool name");
        if (!value.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new IllegalArgumentException("Invalid OpenAI tool name: " + value);
        }
        return value;
    }

    public interface ChatTool {
        String getName();

        String getDescription();

        /** Returns a JSON Schema object describing the tool arguments. */
        JSONObject getParameters();

        /**
         * Executes the tool. Return a JSONObject, JSONArray, String, number,
         * boolean, or another object with a meaningful toString() value.
         */
        Object execute(JSONObject arguments) throws Exception;
    }
}
