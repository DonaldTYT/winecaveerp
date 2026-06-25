package com.kikyosoft.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
public class DeleteAllProductsController {

    // ====== CONFIG ======
    private static final String SALEOR_GRAPHQL_URL = "http://192.168.46.104:8000/graphql/";
    private static final String APP_TOKEN = "vdbkrq6BwIwH0ko6p4TFFrrrJjzHkM";
	
    private static final int PAGE_SIZE = 100; // how many product IDs to fetch per page
    private static final long PAUSE_MS = 150; // small pause between bulk deletes
    // ====================

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    // Query product IDs in pages
    private static final String PRODUCTS_QUERY = ""
            + "query Products($first:Int!, $after:String){\n"
            + "  products(first:$first, after:$after){\n"
            + "    pageInfo{ hasNextPage endCursor }\n"
            + "    edges{ node{ id } }\n"
            + "  }\n"
            + "}";

    // Bulk delete by IDs
    private static final String BULK_DELETE_MUTATION = ""
            + "mutation BulkDel($ids:[ID!]!){\n"
            + "  productBulkDelete(ids:$ids){\n"
            + "    count\n"
            + "    errors{ field message code }\n"
            + "  }\n"
            + "}";

    /**
     * DELETE ALL PRODUCTS (requires MANAGE_PRODUCTS)
     *
     * Call:
     *   GET /admin/delete-all-products?confirm=yes
     * Dry run:
     *   GET /admin/delete-all-products   (shows how many would be deleted)
     */
    @GetMapping("/oadmin/delete-all-products")
    public ResponseEntity<String> deleteAllProducts(@RequestParam(required = false) String confirm) {
        try {
            int totalFound = 0;
            int totalDeleted = 0;
            String cursor = null;
            boolean hasNext;

            // First pass: count and (optionally) delete page by page
            do {
                Map<String, Object> vars = new HashMap<>();
                vars.put("first", PAGE_SIZE);
                if (cursor != null) vars.put("after", cursor);

                JsonNode listResp = execGraphQL(PRODUCTS_QUERY, vars);
                JsonNode products = listResp.path("data").path("products");
                if (products.isMissingNode() || products.isNull()) {
                    return ResponseEntity.status(500)
                            .body("Failed to list products: " + listResp.toString());
                }

                List<String> ids = new ArrayList<>();
                for (JsonNode edge : products.path("edges")) {
                    String id = edge.path("node").path("id").asText(null);
                    if (id != null && !id.isBlank()) ids.add(id);
                }

                hasNext = products.path("pageInfo").path("hasNextPage").asBoolean(false);
                cursor = products.path("pageInfo").path("endCursor").asText(null);

                totalFound += ids.size();

                if (!"yes".equalsIgnoreCase(confirm)) {
                    // Dry-run mode: don’t delete, just continue paging
                    continue;
                }

                if (!ids.isEmpty()) {
                    JsonNode delResp = execGraphQL(BULK_DELETE_MUTATION, Map.of("ids", ids));
                    JsonNode del = delResp.path("data").path("productBulkDelete");
                    if (del.isMissingNode() || del.isNull()) {
                        return ResponseEntity.status(500)
                                .body("Bulk delete failed: " + delResp.toString());
                    }
                    int count = del.path("count").asInt(0);
                    totalDeleted += count;

                    // Log minimal progress to stdout
                    System.out.println("[Saleor] Deleted batch: " + count + " (running total " + totalDeleted + ")");
                    Thread.sleep(PAUSE_MS);
                }
            } while (hasNext);

            if (!"yes".equalsIgnoreCase(confirm)) {
                return ResponseEntity.ok("Dry run complete. Products found: " + totalFound
                        + ". To delete, call /admin/delete-all-products?confirm=yes");
            }

            return ResponseEntity.ok("Deleted products total: " + totalDeleted);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error during delete: " + e.getMessage());
        }
    }

    /** Executes a GraphQL operation with basic error surfacing */
    private JsonNode execGraphQL(String query, Map<String, Object> variables) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("query", query);
        payload.put("variables", variables == null ? Collections.emptyMap() : variables);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + APP_TOKEN);

        HttpEntity<String> entity = new HttpEntity<>(om.writeValueAsString(payload), headers);

        ResponseEntity<String> res = rest.exchange(
                SALEOR_GRAPHQL_URL, HttpMethod.POST, entity, String.class);

        JsonNode json = om.readTree(res.getBody());
        if (json.has("errors") && json.get("errors").isArray() && json.get("errors").size() > 0) {
            // Keep errors visible to the caller
            System.err.println("[Saleor] GraphQL errors: " + json.get("errors").toString());
        }
        return json;
        // (Callers inspect "data" node and decide if it’s a hard failure)
    }
}
