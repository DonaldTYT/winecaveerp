package com.kikyosoft.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductMcpController {
    private static final String PROTOCOL_VERSION = "2025-06-18";
    private final ProductCatalogService catalog;
    private final ProductApiAccess access;
    private final ObjectMapper mapper;

    public ProductMcpController(ProductCatalogService catalog, ProductApiAccess access, ObjectMapper mapper) {
        this.catalog = catalog; this.access = access; this.mapper = mapper;
    }

    @PostMapping(value = "/mcp", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> message(@RequestBody JsonNode message, HttpServletRequest request) {
        access.requireAllowedOrigin(request);
        access.requireClientAccess(request);
        if (!"2.0".equals(message.path("jsonrpc").asText()) || !message.path("method").isTextual()) {
            return ResponseEntity.badRequest().body(error(message.get("id"), -32600, "Invalid JSON-RPC request"));
        }
        JsonNode id = message.get("id");
        String method = message.path("method").asText();
        if (id == null || id.isNull()) {
            return ResponseEntity.accepted().build();
        }
        try {
            switch (method) {
                case "initialize": return ResponseEntity.ok(success(id, initializeResult()));
                case "ping": return ResponseEntity.ok(success(id, Collections.emptyMap()));
                case "tools/list": return ResponseEntity.ok(success(id, Map.of("tools", tools())));
                case "tools/call": return ResponseEntity.ok(success(id, callTool(message.path("params"))));
                default: return ResponseEntity.ok(error(id, -32601, "Method not found: " + method));
            }
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.ok(error(id, -32602, ex.getMessage()));
        }
    }

    private Map<String, Object> initializeResult() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", PROTOCOL_VERSION);
        result.put("capabilities", Map.of("tools", Map.of("listChanged", false)));
        result.put("serverInfo", Map.of("name", "saleor-product-mcp", "version", "1.0.0"));
        result.put("instructions", "Read-only Saleor product catalogue tools. Pagination is capped at 100 items.");
        return result;
    }

    /** Returns the same read-only tool catalogue used by MCP and internal AI clients. */
    public List<Map<String, Object>> tools() {
        List<Map<String, Object>> tools = new ArrayList<>();
        tools.add(tool("list_products", "List or search customer-visible products in the enforced hk channel. pricing.priceRange.start and stop are the minimum and maximum variant prices for hk. productCode is the unique product-level business code; SKU belongs only to entries in productVariants.edges[].node.sku. Never call productCode or metadata.icode an SKU.", properties(
                prop("first", "integer", "Page size, 1 to 100"), prop("after", "string", "Pagination cursor"),
                prop("search", "string", "Product search text"))));
        tools.add(tool("get_product", "Get one customer-visible hk product by exactly one of id or slug. pricing.priceRange contains its hk minimum and maximum variant prices. productCode is the product code; each productVariants edge has its own distinct variant SKU. Never label productCode as SKU.", properties(
                prop("id", "string", "Product global ID"), prop("slug", "string", "Product slug"))));
        tools.add(tool("list_product_variants", "List hk product variants including their true sku, attributes, prices and stock. The parent product.productCode is a product code, not an SKU.", properties(
                prop("first", "integer", "Page size, 1 to 100"), prop("after", "string", "Pagination cursor"))));
        tools.add(tool("get_product_variant", "Get one variant by exactly one of id or SKU.", properties(
                prop("id", "string", "Variant global ID"), prop("sku", "string", "Variant SKU"))));
        tools.add(tool("list_product_types", "List Saleor product types and assigned attributes.", properties(
                prop("first", "integer", "Page size, 1 to 100"), prop("after", "string", "Pagination cursor"))));
        tools.add(tool("get_product_type", "Get a Saleor product type by global ID.", requiredProperties("id",
                prop("id", "string", "Product type global ID"))));
        tools.add(tool("list_product_attributes", "List or search Saleor product attributes and choices.", properties(
                prop("first", "integer", "Page size, 1 to 100"), prop("after", "string", "Pagination cursor"),
                prop("search", "string", "Attribute search text"))));
        tools.add(tool("get_product_attribute", "Get one Saleor attribute by exactly one of id or slug.", properties(
                prop("id", "string", "Attribute global ID"), prop("slug", "string", "Attribute slug"))));
        return tools;
    }

    /** Executes one MCP product tool without applying HTTP transport authentication. */
    public Map<String, Object> callTool(JsonNode params) {
        String name = params.path("name").asText("");
        if (name.isEmpty()) throw new IllegalArgumentException("params.name is required");
        JsonNode args = params.path("arguments");
        if (!args.isObject() && !args.isMissingNode()) throw new IllegalArgumentException("params.arguments must be an object");
        try {
            JsonNode value;
            switch (name) {
                case "list_products": value = catalog.listProducts(integer(args,"first"), text(args,"after"), text(args,"search"), text(args,"channel")); break;
                case "get_product": value = catalog.getProduct(text(args,"id"), text(args,"slug"), text(args,"channel")); break;
                case "list_product_variants": value = catalog.listVariants(integer(args,"first"), text(args,"after"), text(args,"channel")); break;
                case "get_product_variant": value = catalog.getVariant(text(args,"id"), text(args,"sku"), text(args,"channel")); break;
                case "list_product_types": value = catalog.listProductTypes(integer(args,"first"), text(args,"after")); break;
                case "get_product_type": value = catalog.getProductType(text(args,"id")); break;
                case "list_product_attributes": value = catalog.listAttributes(integer(args,"first"), text(args,"after"), text(args,"search"), text(args,"channel")); break;
                case "get_product_attribute": value = catalog.getAttribute(text(args,"id"), text(args,"slug")); break;
                default: throw new IllegalArgumentException("Unknown tool: " + name);
            }
            return toolResult(value, false);
        } catch (ProductApiException ex) {
            return toolResult(mapper.getNodeFactory().textNode(ex.getMessage()), true);
        }
    }

    private Map<String, Object> toolResult(JsonNode value, boolean isError) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            result.put("content", List.of(Map.of("type", "text", "text", mapper.writeValueAsString(value))));
        } catch (JsonProcessingException ex) {
            throw new ProductApiException(500, "Unable to serialize MCP result", ex);
        }
        result.put("structuredContent", Map.of("result", value));
        result.put("isError", isError);
        return result;
    }

    private static Map<String, Object> tool(String name, String description, Map<String, Object> schema) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("name", name); value.put("description", description); value.put("inputSchema", schema);
        value.put("annotations", Map.of("readOnlyHint", true, "destructiveHint", false, "idempotentHint", true, "openWorldHint", true));
        return value;
    }

    @SafeVarargs
    private static Map<String, Object> properties(Map.Entry<String, Object>... fields) {
        return schema(fields, Collections.emptyList());
    }

    @SafeVarargs
    private static Map<String, Object> requiredProperties(String required, Map.Entry<String, Object>... fields) {
        return schema(fields, List.of(required));
    }

    private static Map<String, Object> schema(Map.Entry<String, Object>[] fields, List<String> required) {
        Map<String, Object> props = new LinkedHashMap<>();
        for (Map.Entry<String, Object> field : fields) props.put(field.getKey(), field.getValue());
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object"); schema.put("properties", props); schema.put("additionalProperties", false);
        if (!required.isEmpty()) schema.put("required", required);
        return schema;
    }

    private static Map.Entry<String, Object> prop(String name, String type, String description) {
        return Map.entry(name, Map.of("type", type, "description", description));
    }

    private static Integer integer(JsonNode args, String name) {
        JsonNode value = args.get(name);
        if (value == null || value.isNull()) return null;
        if (!value.canConvertToInt()) throw new IllegalArgumentException(name + " must be an integer");
        return value.intValue();
    }

    private static String text(JsonNode args, String name) {
        JsonNode value = args.get(name);
        if (value == null || value.isNull()) return null;
        if (!value.isTextual()) throw new IllegalArgumentException(name + " must be a string");
        return value.textValue();
    }

    private static Map<String, Object> success(JsonNode id, Object result) {
        Map<String, Object> value = new LinkedHashMap<>(); value.put("jsonrpc", "2.0"); value.put("id", id); value.put("result", result); return value;
    }

    private static Map<String, Object> error(JsonNode id, int code, String message) {
        Map<String, Object> value = new LinkedHashMap<>(); value.put("jsonrpc", "2.0"); value.put("id", id);
        value.put("error", Map.of("code", code, "message", message)); return value;
    }
}
