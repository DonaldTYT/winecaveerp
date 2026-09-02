  package com.kikyosoft.chatai;

  import org.springframework.beans.factory.annotation.Value;
  import org.springframework.http.MediaType;
  import org.springframework.web.bind.annotation.*;
  import org.json.JSONArray;
  import org.json.JSONObject;

  import com.fasterxml.jackson.databind.JsonNode;
  import com.fasterxml.jackson.databind.ObjectMapper;
  import com.fasterxml.jackson.databind.node.ObjectNode;
  import com.kikyosoft.api.ProductMcpController;

  import java.io.IOException;
  import java.nio.charset.StandardCharsets;
  import java.util.List;
  import java.util.Map;

  /**
   * POST /ai/chat
   * Body: {"message":"..."}
   * Response: {"reply":"..."}
   *
   * Uses OpenAI Chat Completions + tool calling.
   * - Model: gpt-5-mini
   * - Tools: mock order status plus the read-only Saleor Product MCP catalogue
   * - Safe tool reply shape (role=tool, tool_call_id, content)
   */
  @RestController
  @RequestMapping(path = "/ai/chat", produces = MediaType.APPLICATION_JSON_VALUE)
  public class ChatController {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-5-mini";
    private static final int MAX_TOOL_ROUNDS = 6;
    private static final int MAX_HISTORY_MESSAGES = 20;
    private static final int MAX_MESSAGE_LENGTH = 12000;
    private static final int MAX_HISTORY_CHARACTERS = 24000;
    private static final int MAX_CHAT_TOOL_PAGE_SIZE = 10;
    private final String openAiKey;
    private final ProductMcpController productMcp;
    private final ObjectMapper mapper;

    public ChatController(@Value("${openai.chat.api-key}") String openAiKey,
                          ProductMcpController productMcp,
                          ObjectMapper mapper) {
      if (openAiKey == null || openAiKey.trim().isEmpty()) {
        throw new IllegalStateException("openai.chat.api-key is not configured");
      }
      this.openAiKey = openAiKey.trim();
      this.productMcp = productMcp;
      this.mapper = mapper;
    }

    // Toggle to false once you wire Saleor GraphQL
    private static final boolean USE_MOCK_ORDER = true;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> chat(@RequestBody Map<String, Object> body) throws Exception {
      final JSONArray tools = openAiTools();
      final JSONArray messages = new JSONArray()
          .put(new JSONObject().put("role","system").put("content",
              "You are a helpful Saleor catalogue assistant. Use the supplied read-only "
              + "product tools whenever the user asks for live products, variants, product "
              + "types or attributes. The customer catalogue channel is always hk; never request, "
              + "suggest or display products from another channel. Product Code is product.productCode "
              + "(sourced from metadata.icode); "
              + "SKU belongs only to a product variant's sku field. Never label Product Code as SKU. "
              + "When a product has variants, report their actual SKUs from productVariants.edges[].node.sku. "
              + "For prices, use pricing.priceRange for hk. Show one price when start equals stop; "
              + "otherwise show the minimum-to-maximum range, using gross or net according to displayGrossPrices. "
              + "When a thumbnail URL is available, render it as Markdown image syntax: "
              + "![Product thumbnail](URL). Do not invent catalogue data. Be concise."))
          ;
      JSONArray history = validatedHistory(body);
      for (int i = 0; i < history.length(); i++) messages.put(history.getJSONObject(i));

      for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
        final JSONObject request = new JSONObject()
            .put("model", MODEL)
            .put("messages", messages)
            .put("tools", tools)
            .put("tool_choice", "auto");
        final JSONObject response = postJson(OPENAI_URL, request);
        final JSONObject assistant = response.getJSONArray("choices").getJSONObject(0)
            .getJSONObject("message");
        final JSONArray calls = assistant.optJSONArray("tool_calls");
        if (calls == null || calls.length() == 0) {
          return Map.of("reply", assistant.optString("content", "(no content)"));
        }

        messages.put(assistant);
        for (int i = 0; i < calls.length(); i++) {
          final JSONObject call = calls.getJSONObject(i);
          final JSONObject function = call.getJSONObject("function");
          final String name = function.getString("name");
          final JSONObject arguments = new JSONObject(function.optString("arguments", "{}"));
          messages.put(new JSONObject()
              .put("role", "tool")
              .put("tool_call_id", call.getString("id"))
              .put("content", executeTool(name, arguments)));
        }
      }
      throw new IllegalStateException("AI exceeded the maximum tool-call rounds");
    }

    /** Accepts only bounded user/assistant history; system and tool roles remain server-controlled. */
    private static JSONArray validatedHistory(Map<String, Object> body) {
      Object supplied = body.get("messages");
      JSONArray history = new JSONArray();
      if (supplied instanceof List<?>) {
        List<?> source = (List<?>) supplied;
        int start = source.size();
        int retainedCharacters = 0;
        while (start > 0 && source.size() - start < MAX_HISTORY_MESSAGES) {
          Object candidate = source.get(start - 1);
          int candidateLength = candidate instanceof Map<?, ?>
              ? String.valueOf(((Map<?, ?>) candidate).get("content")).length()
              : 0;
          if (retainedCharacters + candidateLength > MAX_HISTORY_CHARACTERS && start < source.size()) break;
          retainedCharacters += candidateLength;
          start--;
        }
        for (int i = start; i < source.size(); i++) {
          Object item = source.get(i);
          if (!(item instanceof Map<?, ?>)) throw new IllegalArgumentException("Each history message must be an object");
          Map<?, ?> message = (Map<?, ?>) item;
          String role = String.valueOf(message.get("role"));
          if (!"user".equals(role) && !"assistant".equals(role)) {
            throw new IllegalArgumentException("History role must be user or assistant");
          }
          Object rawContent = message.get("content");
          String content = rawContent == null ? "" : String.valueOf(rawContent).trim();
          if (content.isEmpty()) throw new IllegalArgumentException("History message content is required");
          if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("History message exceeds " + MAX_MESSAGE_LENGTH + " characters");
          }
          history.put(new JSONObject().put("role", role).put("content", content));
        }
      } else {
        // Backward compatibility for existing clients that send {"message":"..."}.
        String content = String.valueOf(body.getOrDefault("message", "")).trim();
        if (!content.isEmpty()) history.put(new JSONObject().put("role", "user").put("content", content));
      }
      if (history.length() == 0 || !"user".equals(history.getJSONObject(history.length() - 1).optString("role"))) {
        throw new IllegalArgumentException("The conversation must end with a user message");
      }
      return history;
    }

    // Optional quick sanity check
    @GetMapping
    public Map<String, String> ping() { return Map.of("ok","true"); }

    // ---------- Tool schema ----------
    private static JSONObject orderToolSchema() {
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

    /** Converts the Product MCP catalogue to the OpenAI function-tool format. */
    private JSONArray openAiTools() {
      JSONArray result = new JSONArray().put(orderToolSchema());
      for (Map<String, Object> tool : productMcp.tools()) {
        JSONObject function = new JSONObject()
            .put("name", tool.get("name"))
            .put("description", tool.get("description"))
            .put("parameters", new JSONObject(castMap(tool.get("inputSchema"))));
        result.put(new JSONObject().put("type", "function").put("function", function));
      }
      return result;
    }

    private String executeTool(String name, JSONObject arguments) throws Exception {
      if ("get_order_status".equals(name)) {
        String orderNo = arguments.optString("order_number", "").trim();
        return (orderNo.isEmpty()
            ? new JSONObject().put("error", "order_number_required")
            : getOrderStatus(orderNo)).toString();
      }
      clampListPageSize(name, arguments);
      ObjectNode params = mapper.createObjectNode();
      params.put("name", name);
      JsonNode argumentNode = mapper.readTree(arguments.toString());
      params.set("arguments", argumentNode);
      JsonNode mcpResult = mapper.valueToTree(productMcp.callTool(params));
      JsonNode result = mcpResult.path("structuredContent").path("result");
      return mapper.writeValueAsString(result);
    }

    private static void clampListPageSize(String name, JSONObject arguments) {
      if (!name.startsWith("list_")) return;
      int requested = arguments.optInt("first", MAX_CHAT_TOOL_PAGE_SIZE);
      if (requested < 1 || requested > MAX_CHAT_TOOL_PAGE_SIZE) requested = MAX_CHAT_TOOL_PAGE_SIZE;
      arguments.put("first", requested);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
      return (Map<String, Object>) value;
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
    private JSONObject postJson(String url, JSONObject payload) throws IOException {
      final java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Authorization", "Bearer " + openAiKey);
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
