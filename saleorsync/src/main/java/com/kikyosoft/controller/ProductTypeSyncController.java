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

@RestController
@RequestMapping("/admin")
public class ProductTypeSyncController {

    private static final String DEFAULT_PRODUCT_TYPE_SLUG = "default-type";
    private static final String DEFAULT_PRODUCT_TYPE_NAME = "Default Type";

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
    private static final String Q_PRODUCT_TYPES_PAGE = ""
        + "query($after:String){\n"
        + "  productTypes(first:100, after:$after){\n"
        + "    pageInfo{ hasNextPage endCursor }\n"
        + "    edges{ node{\n"
        + "      id name slug hasVariants\n"
        + "      productAttributes{ slug }\n"
        + "      assignedVariantAttributes{ attribute{ slug } variantSelection }\n"
        + "    } }\n"
        + "  }\n"
        + "}";

    private static final String Q_PRODUCT_TYPE_BY_SLUG = ""
        + "query($slug:String!){\n"
        + "  productTypes(first:1, filter:{ slugs: [$slug] }){\n"
        + "    edges{ node{\n"
        + "      id name slug hasVariants\n"
        + "      productAttributes{ slug }\n"
        + "      assignedVariantAttributes{ attribute{ slug } variantSelection }\n"
        + "    } }\n"
        + "  }\n"
        + "}";

    private static final String Q_ATTRS_PAGE = ""
        + "query($after:String){\n"
        + "  attributes(first:100, after:$after){\n"
        + "    pageInfo{ hasNextPage endCursor }\n"
        + "    edges{ node{ id slug } }\n"
        + "  }\n"
        + "}";

    private static final String M_PRODUCT_TYPE_CREATE = ""
        + "mutation($input: ProductTypeInput!){\n"
        + "  productTypeCreate(input:$input){ productType{ id slug } errors{ field message code } }\n"
        + "}";

    private static final String M_PRODUCT_TYPE_DELETE = ""
        + "mutation($id:ID!){ productTypeDelete(id:$id){ errors{ field message code } } }";

    private static final String M_PRODUCT_TYPE_UPDATE = ""
        + "mutation($id:ID!, $input: ProductTypeInput!){\n"
        + "  productTypeUpdate(id:$id, input:$input){ productType{ id name slug hasVariants } errors{ field message code } }\n"
        + "}";

    private static final String M_PRODUCT_ATTRIBUTE_ASSIGN = ""
        + "mutation($productTypeId:ID!, $ops:[ProductAttributeAssignInput!]!){\n"
        + "  productAttributeAssign(productTypeId:$productTypeId, operations:$ops){ errors{ field message code } }\n"
        + "}";

    private static final String M_PRODUCT_ATTRIBUTE_UNASSIGN = ""
        + "mutation($productTypeId:ID!, $attributeIds:[ID!]!){\n"
        + "  productAttributeUnassign(productTypeId:$productTypeId, attributeIds:$attributeIds){ errors{ field message code } }\n"
        + "}";

    private static final String M_PRODUCT_ATTRIBUTE_ASSIGNMENT_UPDATE = ""
        + "mutation($productTypeId:ID!, $ops:[ProductAttributeAssignmentUpdateInput!]!){\n"
        + "  productAttributeAssignmentUpdate(productTypeId:$productTypeId, operations:$ops){ errors{ field message code } }\n"
        + "}";

    /* ------------------------------- Helpers -------------------------------- */
    private List<ObjectNode> listAllProductTypes() {
        List<ObjectNode> out = new ArrayList<>();
        String after = null;
        do {
            JsonNode r = call(Q_PRODUCT_TYPES_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("productTypes");
            for (JsonNode e : conn.path("edges")) out.add(((ObjectNode) e.path("node")).deepCopy());
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);
        return out;
    }

    private ObjectNode getProductTypeBySlug(String slug) {
        JsonNode r = call(Q_PRODUCT_TYPE_BY_SLUG, vars("slug", slug));
        JsonNode edges = r.path("data").path("productTypes").path("edges");
        if (edges.isArray() && edges.size() > 0) return (ObjectNode) edges.get(0).path("node");
        return null;
    }

    private String productTypeIdBySlug(String slug) {
        ObjectNode n = getProductTypeBySlug(slug);
        return n == null ? null : n.path("id").asText();
    }

    private boolean isDefaultType(JsonNode n) {
        String slug = n.path("slug").asText("");
        String name = n.path("name").asText("");
        return DEFAULT_PRODUCT_TYPE_SLUG.equals(slug) || DEFAULT_PRODUCT_TYPE_NAME.equals(name);
    }

    private Map<String, String> loadAttributeSlugToId() {
        Map<String, String> map = new HashMap<>();
        String after = null;
        do {
            JsonNode r = call(Q_ATTRS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("attributes");
            for (JsonNode e : conn.path("edges")) {
                JsonNode n = e.path("node");
                map.put(n.path("slug").asText(), n.path("id").asText());
            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);
        return map;
    }

    /* --------------------------- 1) EXPORT to JSON --------------------------- */
    @GetMapping("/GetProductTypeJson")
    public String exportProductTypes() throws Exception {
        ArrayNode out = mapper().createArrayNode();
        List<ObjectNode> pts = listAllProductTypes();

        for (ObjectNode pt : pts) {
            if (isDefaultType(pt)) continue;

            ObjectNode row = mapper().createObjectNode();
            row.put("name", pt.path("name").asText());
            row.put("slug", pt.path("slug").asText());
            row.put("hasVariants", pt.path("hasVariants").asBoolean(false));

            ArrayNode prodAttrs = mapper().createArrayNode();
            for (JsonNode pa : pt.path("productAttributes"))
                prodAttrs.add(pa.path("slug").asText());
            row.set("productAttributes", prodAttrs);

            ArrayNode varAttrs = mapper().createArrayNode();
            for (JsonNode av : pt.path("assignedVariantAttributes")) {
                ObjectNode one = mapper().createObjectNode();
                one.put("slug", av.path("attribute").path("slug").asText());
                one.put("variantSelection", av.path("variantSelection").asBoolean(false));
                varAttrs.add(one);
            }
            row.set("variantAttributes", varAttrs);
            out.add(row);
        }

        File f = new File("/tmp/product_types.json");
        mapper().writerWithDefaultPrettyPrinter().writeValue(f, out);
        return "Exported " + out.size() + " product types → " + f.getAbsolutePath();
    }

    /* --------------------------- 2) DELETE ALL SAFE -------------------------- */
    @GetMapping("/DeleteAllProductType")
    public String deleteAllProductTypes() {
        List<ObjectNode> pts = listAllProductTypes();
        int deleted = 0, skipped = 0, errors = 0;

        for (ObjectNode pt : pts) {
            if (isDefaultType(pt)) { skipped++; continue; }
            String id = pt.path("id").asText();
            String slug = pt.path("slug").asText();

            JsonNode r = call(M_PRODUCT_TYPE_DELETE, vars("id", id));
            JsonNode errs = r.path("data").path("productTypeDelete").path("errors");
            if (errs.isArray() && errs.size() > 0) {
                System.out.println("⚠️ productTypeDelete error for " + slug + ": " + errs);
                errors++;
            } else deleted++;
        }
        return String.format("Deleted %d product types. Skipped(default): %d, Errors: %d", deleted, skipped, errors);
    }

    /* --------------------------- 3) IMPORT from JSON ------------------------- */
    @GetMapping("/PutProductTypeJson")
    public String importProductTypes() throws Exception {
        File f = new File("/tmp/product_types.json");
        if (!f.exists()) return "File not found: " + f.getAbsolutePath();
        JsonNode root = mapper().readTree(Files.readAllBytes(f.toPath()));
        if (!root.isArray()) return "Invalid JSON: expected array";

        Map<String, String> attrSlugToId = loadAttributeSlugToId();
        int created = 0, updated = 0, assigned = 0, unassigned = 0, updatedSelection = 0, skipped = 0, errors = 0;

        for (JsonNode n : root) {
            String name = n.path("name").asText();
            String slug = n.path("slug").asText();
            boolean hasVariants = n.path("hasVariants").asBoolean(false);
            if (isDefaultType(n)) { skipped++; continue; }

            Set<String> desiredProd = new HashSet<>();
            for (JsonNode a : n.path("productAttributes")) desiredProd.add(a.asText());

            Map<String, Boolean> desiredVar = new HashMap<>();
            for (JsonNode a : n.path("variantAttributes"))
                desiredVar.put(a.path("slug").asText(), a.path("variantSelection").asBoolean(false));

            ObjectNode existing = getProductTypeBySlug(slug);
            String ptId;
            if (existing == null) {
                ObjectNode input = mapper().createObjectNode();
                input.put("name", name);
                input.put("slug", slug);
                input.put("hasVariants", hasVariants);
                JsonNode cr = call(M_PRODUCT_TYPE_CREATE, vars("input", input));
                JsonNode errs = cr.path("data").path("productTypeCreate").path("errors");
                if (errs.isArray() && errs.size() > 0) { errors++; continue; }
                created++;
                ptId = cr.path("data").path("productTypeCreate").path("productType").path("id").asText();
                existing = getProductTypeBySlug(slug);
            } else {
                ptId = existing.path("id").asText();
                boolean curHV = existing.path("hasVariants").asBoolean(false);
                if (curHV != hasVariants) {
                    JsonNode up = call(M_PRODUCT_TYPE_UPDATE, vars("id", ptId,
                        "input", mapper().createObjectNode().put("hasVariants", hasVariants)));
                    if (up.path("data").path("productTypeUpdate").path("errors").size() == 0) updated++;
                }
            }

            // current attributes
            Set<String> curProd = new HashSet<>();
            for (JsonNode pa : existing.path("productAttributes"))
                curProd.add(pa.path("slug").asText());
            Map<String, Boolean> curVar = new HashMap<>();
            for (JsonNode av : existing.path("assignedVariantAttributes")) {
                curVar.put(av.path("attribute").path("slug").asText(),
                           av.path("variantSelection").asBoolean(false));
            }

            // diff
            Set<String> toUnassign = new HashSet<>();
            for (String s : curProd) if (!desiredProd.contains(s)) toUnassign.add(s);
            for (String s : curVar.keySet()) if (!desiredVar.containsKey(s)) toUnassign.add(s);

            List<ObjectNode> toAssign = new ArrayList<>();
            for (String s : desiredProd)
                if (!curProd.contains(s) && attrSlugToId.containsKey(s)) {
                    ObjectNode op = mapper().createObjectNode();
                    op.put("id", attrSlugToId.get(s));
                    op.put("type", "PRODUCT");
                    toAssign.add(op);
                }

            for (Map.Entry<String, Boolean> e : desiredVar.entrySet()) {
                String s = e.getKey(); boolean vs = e.getValue();
                if (!curVar.containsKey(s) && attrSlugToId.containsKey(s)) {
                    ObjectNode op = mapper().createObjectNode();
                    op.put("id", attrSlugToId.get(s));
                    op.put("type", "VARIANT");
                    op.put("variantSelection", vs);
                    toAssign.add(op);
                }
            }

            if (!toUnassign.isEmpty()) {
                List<String> ids = new ArrayList<>();
                for (String s : toUnassign) if (attrSlugToId.containsKey(s)) ids.add(attrSlugToId.get(s));
                if (!ids.isEmpty()) {
                    call(M_PRODUCT_ATTRIBUTE_UNASSIGN, vars("productTypeId", ptId, "attributeIds", ids));
                    unassigned += ids.size();
                }
            }

            if (!toAssign.isEmpty()) {
                call(M_PRODUCT_ATTRIBUTE_ASSIGN, vars("productTypeId", ptId, "ops", toAssign));
                assigned += toAssign.size();
            }

            List<ObjectNode> updOps = new ArrayList<>();
            for (Map.Entry<String, Boolean> e : desiredVar.entrySet()) {
                String s = e.getKey(); boolean want = e.getValue();
                Boolean cur = curVar.get(s);
                if (cur != null && cur.booleanValue() != want && attrSlugToId.containsKey(s)) {
                    ObjectNode op = mapper().createObjectNode();
                    op.put("id", attrSlugToId.get(s));
                    op.put("variantSelection", want);
                    updOps.add(op);
                }
            }
            if (!updOps.isEmpty()) {
                call(M_PRODUCT_ATTRIBUTE_ASSIGNMENT_UPDATE, vars("productTypeId", ptId, "ops", updOps));
                updatedSelection += updOps.size();
            }
        }

        return String.format(
            "Import done. Created: %d, Updated: %d, Assigned: %d, Unassigned: %d, VariantSelectionUpdated: %d, Skipped(default): %d, Errors: %d",
            created, updated, assigned, unassigned, updatedSelection, skipped, errors
        );
    }
}
