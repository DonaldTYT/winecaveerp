package com.kikyosoft.controller;

import com.kikyosoft.config.SaleorConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
public class DeleteAllProductAttributeController {
	@Autowired
	private SaleorConfig config;


    // ====== CONFIG ======
//    private static final String SALEOR_GRAPHQL_URL = "http://192.168.46.104:8000/graphql/";
//    private static final String APP_TOKEN = "vdbkrq6BwIwH0ko6p4TFFrrrJjzHkM";
    private static final int PAGE_SIZE = 100;
    private static final long PAUSE_MS = 150; // small pause between bulk deletes
    // ====================

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    // --- GraphQL queries/mutations ---
    private static final String ATTR_QUERY = " query AttrList($first:Int!, $after:String){ attributes(first:$first, after:$after){ pageInfo{ hasNextPage endCursor } edges{ node{ id name slug } } } } ";

    private static final String BULK_DELETE = " mutation BulkDel($ids:[ID!]!){ attributeBulkDelete(ids:$ids){ count errors{ field message code } } } ";

    /**
     * Deletes all product attributes in the dev Saleor DB.
     * 
     * Dry run:
     *   GET /admin/DeleteAllProductAttribute
     * Actual delete:
     *   GET /admin/DeleteAllProductAttribute?confirm=yes
     */
    @GetMapping("/oadmin/DeleteAllProductAttribute")
    public ResponseEntity<String> deleteAllAttributes(@RequestParam(required = false) String confirm) {
        try {
            int totalFound = 0;
            int totalDeleted = 0;
            String cursor = null;
            boolean hasNext;

            do {
                Map<String, Object> vars = new HashMap<>();
                vars.put("first", PAGE_SIZE);
                if (cursor != null) vars.put("after", cursor);

                JsonNode resp = execGraphQL(ATTR_QUERY, vars);
                JsonNode attrs = resp.path("data").path("attributes");
                if (attrs.isMissingNode() || attrs.isNull()) {
                    return ResponseEntity.status(500)
                            .body("Failed to list attributes: " + resp.toString());
                }

                List<String> ids = new ArrayList<>();
                for (JsonNode edge : attrs.path("edges")) {
                    String id = edge.path("node").path("id").asText(null);
                    if (id != null && !id.isBlank()) ids.add(id);
                }

                hasNext = attrs.path("pageInfo").path("hasNextPage").asBoolean(false);
                cursor = attrs.path("pageInfo").path("endCursor").asText(null);
                totalFound += ids.size();

                if (!"yes".equalsIgnoreCase(confirm)) {
                    // dry-run mode
                    continue;
                }

                if (!ids.isEmpty()) {
                    JsonNode delResp = execGraphQL(BULK_DELETE, Map.of("ids", ids));
                    JsonNode del = delResp.path("data").path("attributeBulkDelete");
                    if (del.isMissingNode() || del.isNull()) {
                        return ResponseEntity.status(500)
                                .body("Bulk delete failed: " + delResp.toString());
                    }
                    int count = del.path("count").asInt(0);
                    totalDeleted += count;
                    System.out.println("[Saleor] Deleted attribute batch: " + count + " (running total " + totalDeleted + ")");
                    Thread.sleep(PAUSE_MS);
                }

            } while (hasNext);

            if (!"yes".equalsIgnoreCase(confirm)) {
                return ResponseEntity.ok("Dry run complete. Attributes found: " + totalFound +
                        ". To delete, call /admin/DeleteAllProductAttribute?confirm=yes");
            }

            return ResponseEntity.ok("Deleted attributes total: " + totalDeleted);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error during delete: " + e.getMessage());
        }
    }

    private JsonNode execGraphQL(String query, Map<String, Object> variables) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("query", query);
        payload.put("variables", variables == null ? Collections.emptyMap() : variables);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", config.getAuthHeader());

        HttpEntity<String> entity = new HttpEntity<>(om.writeValueAsString(payload), headers);
        ResponseEntity<String> res = rest.exchange(config.getApiUrl(), HttpMethod.POST, entity, String.class);
        JsonNode json = om.readTree(res.getBody());

        if (json.has("errors") && json.get("errors").isArray() && json.get("errors").size() > 0) {
            System.err.println("[Saleor] GraphQL errors: " + json.get("errors").toString());
        }
        return json;
    }
}
