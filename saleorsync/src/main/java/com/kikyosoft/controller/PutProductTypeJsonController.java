package com.kikyosoft.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kikyosoft.config.SaleorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class PutProductTypeJsonController {

    @Autowired private SaleorConfig config;

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    private static final String INPUT_FILE = "/tmp/product_types.json";

    private static final String ALL_ATTRS = " query { attributes(first: 100) { edges { node { id slug type } } } } ";

    private static final String PT_BY_SLUG = " query($q:String!){ productTypes(first:1, filter:{search:$q}) { edges { node { id slug name } } } } ";

    @GetMapping("/oadmin/PutProductTypeJson")
    public ResponseEntity<String> importProductTypesAndLinkAttrs() {
        try {
            // 0) load json
            File file = new File(INPUT_FILE);
            if (!file.exists()) {
                return ResponseEntity.status(404).body("File not found: " + file.getAbsolutePath());
            }
            JsonNode root = om.readTree(file);
            JsonNode edges = root.path("edges");
            if (!edges.isArray()) {
                return ResponseEntity.badRequest().body("product_types.json must contain 'edges' array.");
            }

            // 1) attr slug -> id map
            Map<String, String> attrSlugToId = new HashMap<>();
            JsonNode attrResp = call(config.getApiUrl(), config.getAuthHeader(), ALL_ATTRS, Map.of());
            for (JsonNode e : attrResp.path("data").path("attributes").path("edges")) {
                String slug = e.path("node").path("slug").asText();
                String id   = e.path("node").path("id").asText();
                if (!slug.isBlank() && !id.isBlank()) attrSlugToId.put(slug, id);
            }

            StringBuilder report = new StringBuilder();
            int created = 0, updated = 0, errors = 0;

            for (JsonNode e : edges) {
                JsonNode node = e.path("node");
                String name = node.path("name").asText();
                String slug = node.path("slug").asText();
                boolean hasVariants = node.path("hasVariants").asBoolean(false);
                boolean isShippingRequired = node.path("isShippingRequired").asBoolean(true);

                List<String> productAttrIds = resolveAttrIds(node.path("productAttributes"), attrSlugToId, report, slug, "product");
                List<String> variantAttrIds = resolveAttrIds(node.path("variantAttributes"), attrSlugToId, report, slug, "variant");

                // Does PT exist?
                JsonNode ptQ = call(config.getApiUrl(), config.getAuthHeader(), PT_BY_SLUG, Map.of("q", slug));
                JsonNode existing = ptQ.path("data").path("productTypes").path("edges");

                if (existing.isArray() && existing.size() > 0) {
                    String ptId = existing.get(0).path("node").path("id").asText();
                    // UPDATE with inline input
                    String mutation = String.format(" mutation { productTypeUpdate( id: \"%s\", input: { name: %s, slug: %s, hasVariants: %s, isShippingRequired: %s, productAttributes: %s, variantAttributes: %s }){ productType { id name slug } errors { field message code } } } ",
                            ptId,
                            gqlString(name),
                            gqlString(slug),
                            hasVariants,
                            isShippingRequired,
                            gqlIdArray(productAttrIds),   // e.g. ["ID1","ID2"] or []
                            gqlIdArray(variantAttrIds)
                    );

                    JsonNode res = callRaw(config.getApiUrl(), config.getAuthHeader(), mutation);
                    JsonNode err = res.path("data").path("productTypeUpdate").path("errors");
                    if (err.isArray() && err.size() > 0) {
                        errors++;
                        report.append("⛔ UPDATE errors for ").append(slug).append(": ").append(err.toString()).append("\n");
                    } else {
                        updated++;
                        report.append("🔄 Updated ").append(name).append(" (").append(slug).append(")\n");
                    }
                } else {
                    // CREATE with inline input
                    String mutation = String.format(" mutation { productTypeCreate( input: { name: %s, slug: %s, hasVariants: %s, isShippingRequired: %s, productAttributes: %s, variantAttributes: %s }){ productType { id name slug } errors { field message code } } } ",                            gqlString(name),
                            gqlString(slug),
                            hasVariants,
                            isShippingRequired,
                            gqlIdArray(productAttrIds),
                            gqlIdArray(variantAttrIds)
                    );

                    JsonNode res = callRaw(config.getApiUrl(), config.getAuthHeader(), mutation);
                    JsonNode err = res.path("data").path("productTypeCreate").path("errors");
                    if (err.isArray() && err.size() > 0) {
                        errors++;
                        report.append("⛔ CREATE errors for ").append(slug).append(": ").append(err.toString()).append("\n");
                    } else {
                        created++;
                        report.append("✅ Created ").append(name).append(" (").append(slug).append(")\n");
                    }
                }
            }

            report.append("\nDone. Created: ").append(created)
                  .append(", Updated: ").append(updated)
                  .append(", Errors: ").append(errors);
            return ResponseEntity.ok(report.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + ex.getMessage());
        }
    }

    // ----- helpers -----
    private static String gqlString(String s) {
        if (s == null) return "null";
        // escape quotes/backslashes
        String esc = s.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + esc + "\"";
    }

    private static String gqlIdArray(List<String> ids) {
        if (ids == null || ids.isEmpty()) return "[]";
        String joined = ids.stream()
                .map(id -> "\"" + id + "\"")
                .collect(Collectors.joining(","));
        return "[" + joined + "]";
    }

    private List<String> resolveAttrIds(JsonNode arr, Map<String,String> map, StringBuilder report, String ptSlug, String kind) {
        List<String> out = new ArrayList<>();
        if (arr != null && arr.isArray()) {
            for (JsonNode a : arr) {
                String slug = a.path("slug").asText(null);
                if (slug == null) continue;
                String id = map.get(slug);
                if (id == null) {
                    report.append("⚠️ Missing attr in dev for PT ").append(ptSlug)
                            .append(" (").append(kind).append("): ").append(slug).append("\n");
                } else {
                    out.add(id);
                }
            }
        }
        return out;
    }

    private JsonNode call(String url, String auth, String query, Map<String,Object> vars) throws Exception {
        Map<String,Object> payload = new HashMap<>();
        payload.put("query", query);
        payload.put("variables", vars == null ? Collections.emptyMap() : vars);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", auth);

        ResponseEntity<String> res = rest.exchange(url, HttpMethod.POST, new HttpEntity<>(om.writeValueAsString(payload), headers), String.class);
        return om.readTree(res.getBody());
    }

    private JsonNode callRaw(String url, String auth, String rawMutation) throws Exception {
        Map<String,Object> payload = new HashMap<>();
        payload.put("query", rawMutation);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", auth);

        ResponseEntity<String> res = rest.exchange(url, HttpMethod.POST, new HttpEntity<>(om.writeValueAsString(payload), headers), String.class);
        return om.readTree(res.getBody());
    }
}
