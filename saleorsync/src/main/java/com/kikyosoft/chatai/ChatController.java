  package com.kikyosoft.chatai;

  import org.springframework.http.MediaType;
  import org.springframework.web.bind.annotation.*;
  import org.json.JSONArray;
  import org.json.JSONObject;

  import java.io.IOException;
  import java.nio.charset.StandardCharsets;
  import java.util.Map;
  import java.util.Objects;

  /**
   * POST /ai/chat
   * Body: {"message":"..."}
   * Response: {"reply":"..."}
   *
   * Uses OpenAI Chat Completions + tool calling.
   * - Model: gpt-4o-mini
   * - Tool: get_order_status(order_number)
   * - Safe tool reply shape (role=tool, tool_call_id, content)
   */
  @RestController
  @RequestMapping(path = "/ai/chat", produces = MediaType.APPLICATION_JSON_VALUE)
  public class ChatController {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";
    private static final String Secret_Ai_Key = "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";
    private static final String OPENAI_KEY =
        Objects.requireNonNullElse(System.getenv("OPENAI_API_KEY"), Secret_Ai_Key);

    // Toggle to false once you wire Saleor GraphQL
    private static final boolean USE_MOCK_ORDER = true;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> chat(@RequestBody Map<String, Object> body) throws Exception {
      final String userMsg = String.valueOf(body.getOrDefault("message", "")).trim();
      if (userMsg.isEmpty()) {
        throw new IllegalArgumentException("missing 'message'");
      }

      // --- 1) First round: let the model decide if it wants a tool ---
      final JSONArray tools = new JSONArray().put(toolSchema());
      final JSONArray messages = new JSONArray()
          .put(new JSONObject().put("role","system").put("content",
              "You are a helpful Saleor help-desk assistant. Be concise."))
          .put(new JSONObject().put("role","user").put("content", userMsg));

      final JSONObject req1 = new JSONObject()
          .put("model", MODEL)
          .put("messages", messages)
          .put("tools", tools)
          .put("tool_choice", "auto");

      final JSONObject r1 = postJson(OPENAI_URL, req1);
      final JSONObject msg1 = r1.getJSONArray("choices").getJSONObject(0).getJSONObject("message");

      // --- 2) If tool requested, execute it and do a follow-up call ---
      if (msg1.has("tool_calls")) {
        final JSONObject tc = msg1.getJSONArray("tool_calls").getJSONObject(0);
        final JSONObject fn = tc.getJSONObject("function");
        final String name = fn.getString("name");
        final JSONObject args = new JSONObject(fn.optString("arguments","{}"));

        JSONObject toolResult;
        if ("get_order_status".equals(name)) {
          final String orderNo = args.optString("order_number","").trim();
          toolResult = orderNo.isEmpty()
              ? new JSONObject().put("error","order_number_required")
              : getOrderStatus(orderNo);
        } else {
          toolResult = new JSONObject().put("error","unknown_tool");
        }

        final JSONArray messages2 = new JSONArray()
            .put(new JSONObject().put("role","system").put("content",
                "You are a helpful Saleor help-desk assistant. Be concise."))
            .put(new JSONObject().put("role","user").put("content", userMsg))
            .put(msg1) // the assistant message that contains tool_calls
            .put(new JSONObject()
                .put("role","tool")
                .put("tool_call_id", tc.getString("id"))
                // IMPORTANT: no "name" on tool messages; only tool_call_id + content
                .put("content", toolResult.toString()));

        final JSONObject req2 = new JSONObject()
            .put("model", MODEL)
            .put("messages", messages2);

        final JSONObject r2 = postJson(OPENAI_URL, req2);
        final String reply2 = r2.getJSONArray("choices").getJSONObject(0)
                                .getJSONObject("message").optString("content","(no content)");
        return Map.of("reply", reply2);
      }

      // --- 3) No tool needed ---
      final String reply = msg1.optString("content","(no content)");
      return Map.of("reply", reply);
    }

    // Optional quick sanity check
    @GetMapping
    public Map<String, String> ping() { return Map.of("ok","true"); }

    // ---------- Tool schema ----------
    private static JSONObject toolSchema() {
      final JSONObject params = new JSONObject()
          .put("type","object")
          .put("properties", new JSONObject()
              .put("order_number", new JSONObject().put("type","string")))
          .put("required", new JSONArray().put("order_number"));
      return new JSONObject()
          .put("type","function")
          .put("function", new JSONObject()
              .put("name","get_order_status")
              .put("description","Lookup an order status by order number")
              .put("parameters", params));
    }

    // ---------- Tool implementation ----------
    private JSONObject getOrderStatus(String orderNumber) throws IOException {
      if (USE_MOCK_ORDER) {
        return new JSONObject()
            .put("order_number", orderNumber)
            .put("status", "FULFILLED")
            .put("created", "2025-09-16T12:00:00Z");
      }
      // Example real call (fill in your Saleor URL/token and query)
      final String gql = "query($id: ID!){ order(id:$id){ number status created } }";
      final JSONObject payload = new JSONObject()
          .put("query", gql)
          .put("variables", new JSONObject().put("id", orderNumber));
      final String saleorUrl = envOrThrow("SALEOR_URL");
      final String saleorToken = envOrThrow("SALEOR_TOKEN");
      final JSONObject saleorResp = postJsonAuth(saleorUrl, saleorToken, payload);
      final JSONObject order = saleorResp.optJSONObject("data").optJSONObject("order");
      if (order == null) return new JSONObject().put("error","order_not_found");
      return new JSONObject()
          .put("order_number", order.optString("number", orderNumber))
          .put("status", order.optString("status","UNKNOWN"))
          .put("created", order.optString("created",""));
    }

    // ---------- HTTP helpers ----------
    private static JSONObject postJson(String url, JSONObject payload) throws IOException {
      final java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Authorization", "Bearer " + OPENAI_KEY);
      conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      conn.setConnectTimeout(15_000);
      conn.setReadTimeout(60_000);
      conn.setDoOutput(true);
      try (var os = conn.getOutputStream()) { os.write(payload.toString().getBytes(StandardCharsets.UTF_8)); }
      final int code = conn.getResponseCode();
      final var is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
      final String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      if (code < 200 || code >= 300) throw new IOException("OpenAI HTTP " + code + ": " + body);
      return new JSONObject(body);
    }

    private static JSONObject postJsonAuth(String url, String bearer, JSONObject payload) throws IOException {
      final java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Authorization", "Bearer " + bearer);
      conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      conn.setConnectTimeout(15_000);
      conn.setReadTimeout(60_000);
      conn.setDoOutput(true);
      try (var os = conn.getOutputStream()) { os.write(payload.toString().getBytes(StandardCharsets.UTF_8)); }
      final int code = conn.getResponseCode();
      final var is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
      final String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      if (code < 200 || code >= 300) throw new IOException("HTTP " + code + ": " + body);
      return new JSONObject(body);
    }

    private static String envOrThrow(String key) {
      final String v = System.getenv(key);
      if (v == null || v.isEmpty()) throw new IllegalStateException(key + " not set");
      return v;
    }
  }
