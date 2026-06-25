package com.kikyosoft.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.kikyosoft.config.SaleorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AttributeSyncController {

    @Autowired
    private SaleorConfig config;

    private final RestTemplate rest = new RestTemplate();

    private ObjectMapper mapper() { return config.getMapper(); }

    /* ----------------------------- GraphQL helper ----------------------------- */
    private JsonNode call(String query, Map<String, Object> variables) {
        ObjectNode body = mapper().createObjectNode();
        body.put("query", query);
        body.set("variables", variables == null ? mapper().createObjectNode() : mapper().valueToTree(variables));

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("Authorization", config.getAuthHeader());

        ResponseEntity<JsonNode> resp = rest.exchange(
            config.getApiUrl(), HttpMethod.POST, new HttpEntity<>(body, h), JsonNode.class);
        return resp.getBody();
    }

    private static Map<String, Object> vars(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    /* ------------------------ GraphQL (Saleor 3.21.x) ------------------------ */
    private static final String Q_ATTRS_PAGE = ""
        + "query($after:String){\n"
        + "  attributes(first:100, after:$after){\n"
        + "    pageInfo{ hasNextPage endCursor }\n"
        + "    edges{ node{\n"
        + "      id name slug type inputType entityType\n"
        + "      choices(first:100){ edges{ node{ id name slug } } }\n"
        + "    } }\n"
        + "  }\n"
        + "}";

    private static final String Q_ATTR_BY_SLUG = ""
        + "query($slug:String!){\n"
        + "  attributes(first:1, filter:{ slugs: [$slug] }){\n"
        + "    edges{ node{\n"
        + "      id name slug type inputType entityType\n"
        + "      choices(first:100){ edges{ node{ id name slug } } }\n"
        + "    } }\n"
        + "  }\n"
        + "}";

    private static final String M_ATTRIBUTE_CREATE = ""
        + "mutation($input: AttributeCreateInput!){\n"
        + "  attributeCreate(input:$input){\n"
        + "    attribute{ id slug }\n"
        + "    errors{ field message code }\n"
        + "  }\n"
        + "}";

    private static final String M_ATTRIBUTE_UPDATE = ""
        + "mutation($id:ID!, $input: AttributeUpdateInput!){\n"
        + "  attributeUpdate(id:$id, input:$input){\n"
        + "    attribute{ id name slug inputType entityType }\n"
        + "    errors{ field message code }\n"
        + "  }\n"
        + "}";

    private static final String M_ATTRIBUTE_DELETE = ""
        + "mutation($id:ID!){\n"
        + "  attributeDelete(id:$id){ errors{ field message code } }\n"
        + "}";

    private static final String M_ATTRIBUTE_VALUE_CREATE = ""
        + "mutation($attr:ID!, $input: AttributeValueCreateInput!){\n"
        + "  attributeValueCreate(attribute:$attr, input:$input){\n"
        + "    value{ id slug }\n"
        + "    errors{ field message code }\n"
        + "  }\n"
        + "}";

    private static final String M_ATTRIBUTE_VALUE_DELETE = ""
        + "mutation($id:ID!){\n"
        + "  attributeValueDelete(id:$id){ errors{ field message code } }\n"
        + "}";

    /* ------------------------------- Helpers -------------------------------- */
    private List<ObjectNode> listAllProductAttributes() {
        // Only PRODUCT_TYPE attributes (skip PAGE_TYPE etc.)
        List<ObjectNode> out = new ArrayList<>();
        String after = null;
        do {
            JsonNode r = call(Q_ATTRS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("attributes");
            for (JsonNode e : conn.path("edges")) {
                ObjectNode n = ((ObjectNode) e.path("node")).deepCopy();
                if ("PRODUCT_TYPE".equals(n.path("type").asText())) {
                    out.add(n);
                }
            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);
        return out;
    }

    private ObjectNode getAttributeBySlug(String slug) {
        JsonNode r = call(Q_ATTR_BY_SLUG, vars("slug", slug));
        JsonNode edges = r.path("data").path("attributes").path("edges");
        if (edges.isArray() && edges.size() > 0) return (ObjectNode) edges.get(0).path("node");
        return null;
    }

    private String attributeIdBySlug(String slug) {
        ObjectNode n = getAttributeBySlug(slug);
        return n == null ? null : n.path("id").asText();
    }

    private static boolean isChoiceBased(String inputType) {
        if (inputType == null) return false;
        switch (inputType.toUpperCase()) {
            case "DROPDOWN":
            case "MULTISELECT":
            case "SWATCH":
                return true;
            default:
                return false; // PLAIN_TEXT, NUMERIC, BOOLEAN, RICH_TEXT, DATE, DATE_TIME, FILE, REFERENCE
        }
    }

    /* --------------------------- 1) EXPORT to JSON --------------------------- */
    /**
     * GET /admin/GetAttributeJson
     * Writes an array of attributes to /tmp/attributes.json
     * Format example:
     * [
     *   {
     *     "name": "owner",
     *     "slug": "owner",
     *     "type": "PRODUCT_TYPE",
     *     "inputType": "DROPDOWN",
     *     "entityType": null,
     *     "values": [ {"name":"Consignee A","slug":"consignee-a"} ]
     *   },
     *   { "name":"long-description", "slug":"long-description", "type":"PRODUCT_TYPE", "inputType":"RICH_TEXT", "entityType":null, "values":[] }
     * ]
     */
    @GetMapping("/GetAttributeJson")
    public String exportAttributes() throws Exception {
        ArrayNode out = mapper().createArrayNode();
        List<ObjectNode> attrs = listAllProductAttributes();

        for (ObjectNode a : attrs) {
            ObjectNode row = mapper().createObjectNode();
            row.put("name", a.path("name").asText());
            row.put("slug", a.path("slug").asText());
            row.put("type", a.path("type").asText("PRODUCT_TYPE")); // keep for clarity
            row.put("inputType", a.path("inputType").asText());
            if (!a.path("entityType").isNull()) {
                row.put("entityType", a.path("entityType").asText());
            } else {
                row.putNull("entityType");
            }

            ArrayNode vals = mapper().createArrayNode();
            JsonNode edges = a.path("choices").path("edges");
            if (edges.isArray()) {
                for (JsonNode ce : edges) {
                    JsonNode v = ce.path("node");
                    ObjectNode vv = mapper().createObjectNode();
                    vv.put("name", v.path("name").asText());
                    vv.put("slug", v.path("slug").asText());
                    vals.add(vv);
                }
            }
            row.set("values", vals);

            out.add(row);
        }

        File f = new File("/tmp/attributes.json");
        mapper().writerWithDefaultPrettyPrinter().writeValue(f, out);
        return "Exported " + out.size() + " product attributes → " + f.getAbsolutePath();
    }

    /* --------------------------- 2) DELETE ALL SAFE -------------------------- */
    /**
     * GET /admin/DeleteAllAttribute
     * Deletes all PRODUCT_TYPE attributes.
     * Note: If an attribute is assigned to product types or used by products, deletion may fail; we count errors.
     */
    @GetMapping("/DeleteAllAttribute")
    public String deleteAllAttributes() {
        List<ObjectNode> attrs = listAllProductAttributes();
        int deleted = 0, errors = 0;

        for (ObjectNode a : attrs) {
            String id = a.path("id").asText();
            String slug = a.path("slug").asText();

            JsonNode r = call(M_ATTRIBUTE_DELETE, vars("id", id));
            JsonNode errs = r.path("data").path("attributeDelete").path("errors");
            if (errs.isArray() && errs.size() > 0) {
                System.out.println("⚠️ attributeDelete error for slug=" + slug + " : " + errs);
                errors++;
            } else {
                deleted++;
            }
        }
        return String.format("Deleted %d attributes (PRODUCT_TYPE). Errors: %d", deleted, errors);
    }

    /* --------------------------- 3) IMPORT from JSON ------------------------- */
    /**
     * GET /admin/PutAttributeJson
     * Reads /tmp/attributes.json and creates missing attributes and their missing values.
     * - Idempotent by attribute slug and value slug.
     * - Handles REFERENCE attributes (entityType) and choice-based values.
     */
    @GetMapping("/PutAttributeJson")
    public String importAttributes() throws Exception {
        File f = new File("/tmp/attributes.json");
        if (!f.exists()) return "File not found: " + f.getAbsolutePath();

        JsonNode root = mapper().readTree(Files.readAllBytes(f.toPath()));
        if (!root.isArray()) return "Invalid JSON: expected an array.";

        int createdAttrs = 0, skippedAttrs = 0, createdVals = 0, skippedVals = 0, errors = 0;

        for (JsonNode n : root) {
            String name = n.path("name").asText(null);
            String slug = n.path("slug").asText(null);
            String inputType = n.path("inputType").asText(null);
            String entityType = n.path("entityType").isNull() ? null : n.path("entityType").asText(null);
            String type = n.path("type").asText("PRODUCT_TYPE"); // keep PRODUCT_TYPE

            if (name == null || slug == null || inputType == null) {
                System.out.println("⚠️ Missing name/slug/inputType in row: " + n);
                continue;
            }

            // Find or create attribute
            ObjectNode existing = getAttributeBySlug(slug);
            String attrId;
            if (existing == null) {
                ObjectNode input = mapper().createObjectNode();
                input.put("name", name);
                input.put("slug", slug);
                input.put("type", type);           // PRODUCT_TYPE
                input.put("inputType", inputType); // e.g., DROPDOWN, PLAIN_TEXT, REFERENCE, ...
                if (entityType != null && !"null".equalsIgnoreCase(entityType)) {
                    input.put("entityType", entityType); // for REFERENCE
                }

                JsonNode resp = call(M_ATTRIBUTE_CREATE, vars("input", input));
                JsonNode payload = resp.path("data").path("attributeCreate");
                JsonNode errs = payload.path("errors");
                if (errs.isArray() && errs.size() > 0) {
                    System.out.println("❌ attributeCreate error for slug=" + slug + " : " + errs);
                    errors++;
                    continue;
                }
                attrId = payload.path("attribute").path("id").asText();
                createdAttrs++;
            } else {
                // Attribute exists; optionally ensure input/entity type matches (we won't auto-change it here)
                attrId = existing.path("id").asText();
                skippedAttrs++;
            }

            // Create missing values for choice-based attributes
            if (isChoiceBased(inputType)) {
                // Build set of existing value slugs
                Set<String> existingValueSlugs = new HashSet<>();
                ObjectNode cur = existing != null ? existing : getAttributeBySlug(slug);
                if (cur != null) {
                    JsonNode edges = cur.path("choices").path("edges");
                    if (edges.isArray()) {
                        for (JsonNode ce : edges) {
                            existingValueSlugs.add(ce.path("node").path("slug").asText());
                        }
                    }
                }

                for (JsonNode v : n.path("values")) {
                    String vName = v.path("name").asText(null);
                    String vSlug = v.path("slug").asText(null);
                    if (vName == null || vSlug == null) continue;

                    if (existingValueSlugs.contains(vSlug)) {
                        skippedVals++;
                        continue;
                    }
                    ObjectNode vin = mapper().createObjectNode();
                    vin.put("name", vName);
                    vin.put("slug", vSlug);

                    JsonNode c = call(M_ATTRIBUTE_VALUE_CREATE, vars("attr", attrId, "input", vin));
                    JsonNode errs = c.path("data").path("attributeValueCreate").path("errors");
                    if (errs.isArray() && errs.size() > 0) {
                        System.out.println("⚠️ attributeValueCreate error for " + slug + "/" + vSlug + " : " + errs);
                        errors++;
                    } else {
                        createdVals++;
                    }
                }
            }
        }

        return String.format(
            "Import done. Attrs Created: %d, Skipped(existing): %d, Value Created: %d, Value Skipped: %d, Errors: %d",
            createdAttrs, skippedAttrs, createdVals, skippedVals, errors
        );
    }

    /* --------------------------- 4) UPDATE (single) -------------------------- */
    /**
     * GET /admin/UpdateAttribute
     * Params:
     *  - slug (required): current attribute slug to update
     *  - name (optional): new name
     *  - newSlug (optional): new slug
     *  - inputType (optional): e.g. PLAIN_TEXT, DROPDOWN, MULTISELECT, BOOLEAN, RICH_TEXT, NUMERIC, REFERENCE, SWATCH, DATE, DATE_TIME, FILE
     *  - entityType (optional, used when inputType=REFERENCE): PRODUCT, PRODUCT_VARIANT, PAGE
     *  - addValues (optional, for choice-based): "Name1:slug1|Name2:slug2"
     *  - removeValueSlugs (optional, for choice-based): "slugA|slugB"
     */
    @GetMapping("/UpdateAttribute")
    public String updateAttribute(
        @RequestParam("slug") String slug,
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "newSlug", required = false) String newSlug,
        @RequestParam(value = "inputType", required = false) String inputType,
        @RequestParam(value = "entityType", required = false) String entityType,
        @RequestParam(value = "addValues", required = false) String addValues,
        @RequestParam(value = "removeValueSlugs", required = false) String removeValueSlugs
    ) {
        ObjectNode attr = getAttributeBySlug(slug);
        if (attr == null) return "Attribute not found for slug=" + slug;

        String id = attr.path("id").asText();

        // 1) attributeUpdate (rename/slug/input/entity)
        ObjectNode input = mapper().createObjectNode();
        if (name != null && !name.isBlank()) input.put("name", name);
        if (newSlug != null && !newSlug.isBlank()) input.put("slug", newSlug);
        if (inputType != null && !inputType.isBlank()) input.put("inputType", inputType);
        if (entityType != null && !entityType.isBlank()) input.put("entityType", entityType);

        if (!input.isEmpty()) {
            JsonNode r = call(M_ATTRIBUTE_UPDATE, vars("id", id, "input", input));
            JsonNode errs = r.path("data").path("attributeUpdate").path("errors");
            if (errs.isArray() && errs.size() > 0) {
                return "Update failed: " + errs.toString();
            }
        }

        // Refresh after update
        String effectiveSlug = (newSlug != null && !newSlug.isBlank()) ? newSlug : slug;
        attr = getAttributeBySlug(effectiveSlug);
        if (attr == null) return "Updated attribute not found (slug=" + effectiveSlug + ")";

        // 2) Add values (choice-based only)
        int added = 0, removed = 0, valErr = 0;
        String curInputType = attr.path("inputType").asText();
        if (isChoiceBased(curInputType)) {
            // Build maps for current values
            Map<String, String> valueSlugToId = new HashMap<>();
            JsonNode edges = attr.path("choices").path("edges");
            if (edges.isArray()) {
                for (JsonNode ce : edges) {
                    String vs = ce.path("node").path("slug").asText();
                    String vid = ce.path("node").path("id").asText();
                    valueSlugToId.put(vs, vid);
                }
            }

            // Add values
            if (addValues != null && !addValues.isBlank()) {
                String[] parts = addValues.split("\\|");
                for (String p : parts) {
                    String[] nv = p.split(":", 2);
                    if (nv.length != 2) continue;
                    String vName = nv[0].trim();
                    String vSlug = nv[1].trim();
                    if (vName.isEmpty() || vSlug.isEmpty()) continue;
                    if (valueSlugToId.containsKey(vSlug)) continue;

                    ObjectNode vin = mapper().createObjectNode();
                    vin.put("name", vName);
                    vin.put("slug", vSlug);
                    JsonNode c = call(M_ATTRIBUTE_VALUE_CREATE, vars("attr", attr.path("id").asText(), "input", vin));
                    JsonNode errs = c.path("data").path("attributeValueCreate").path("errors");
                    if (errs.isArray() && errs.size() > 0) {
                        System.out.println("⚠️ attributeValueCreate error for " + effectiveSlug + "/" + vSlug + " : " + errs);
                        valErr++;
                    } else {
                        added++;
                    }
                }
            }

            // Remove values
            if (removeValueSlugs != null && !removeValueSlugs.isBlank()) {
                String[] slugs = removeValueSlugs.split("\\|");
                for (String s : slugs) {
                    s = s.trim();
                    String vid = valueSlugToId.get(s);
                    if (vid == null) continue;
                    JsonNode d = call(M_ATTRIBUTE_VALUE_DELETE, vars("id", vid));
                    JsonNode errs = d.path("data").path("attributeValueDelete").path("errors");
                    if (errs.isArray() && errs.size() > 0) {
                        System.out.println("⚠️ attributeValueDelete error for " + effectiveSlug + "/" + s + " : " + errs);
                        valErr++;
                    } else {
                        removed++;
                    }
                }
            }
        } else {
            if ((addValues != null && !addValues.isBlank()) || (removeValueSlugs != null && !removeValueSlugs.isBlank())) {
                return "Attribute inputType is not choice-based; cannot add/remove values.";
            }
        }

        return String.format("Attribute updated. slug=%s, name=%s, inputType=%s, addedValues=%d, removedValues=%d, valueErrors=%d",
                attr.path("slug").asText(), attr.path("name").asText(), attr.path("inputType").asText(),
                added, removed, valErr);
    }
}
