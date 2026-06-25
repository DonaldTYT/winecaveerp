package com.kikyosoft.wrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kikyosoft.config.SaleorConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProductTypeSyncWrapper {

    private static final String DEFAULT_PRODUCT_TYPE_SLUG = "default-type";
    private static final String DEFAULT_PRODUCT_TYPE_NAME = "Default Type";

    private final SaleorConfig config;
    private final RestTemplate rest;

    public ProductTypeSyncWrapper(SaleorConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.rest = restTemplate;
    }

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
        return "OK  " + mapper().writeValueAsString(out);
    }

    /* -------------------- 2) DELETE ONLY SELECTED BY SLUG -------------------- */
    public String deleteProductTypes(List<String> slugsToDelete) {
        if (slugsToDelete == null || slugsToDelete.isEmpty()) return "OK  ";

        // Load all once to map slugs → ids and filter out default
        List<ObjectNode> pts = listAllProductTypes();
        Map<String, String> slugToId = new HashMap<>();
        for (ObjectNode pt : pts) {
            String slug = pt.path("slug").asText();
            if (DEFAULT_PRODUCT_TYPE_SLUG.equals(slug) || DEFAULT_PRODUCT_TYPE_NAME.equals(pt.path("name").asText())) {
                continue;
            }
            slugToId.put(slug, pt.path("id").asText());
        }

        for (String slug : slugsToDelete) {
            if (!slugToId.containsKey(slug)) continue;
            String id = slugToId.get(slug);
            JsonNode r = call(M_PRODUCT_TYPE_DELETE, vars("id", id));
            JsonNode errs = r.path("data").path("productTypeDelete").path("errors");
            if (errs.isArray() && errs.size() > 0) {
                System.out.println("⚠️ productTypeDelete error for " + slug + ": " + errs);
            }
        }
        return "OK  ";
    }

    /* ------------- 3) IMPORT from JSON STRING (create or update) ------------- */
    /**
     * JSON schema example:
     * [
     *   {
     *     "name": "Wine",
     *     "slug": "wine",
     *     "hasVariants": true,
     *     "productAttributes": ["country","region"],
     *     "variantAttributes": [{"slug":"size","variantSelection":true}]
     *   }
     * ]
     */
    public String importProductTypes(String json) throws Exception {
        if (json == null || json.isBlank()) return "OK  ";

        JsonNode root = mapper().readTree(json);
        if (!root.isArray()) return "OK  ";

        Map<String, String> attrSlugToId = loadAttributeSlugToId();

        for (JsonNode n : root) {
            String name = n.path("name").asText();
            String slug = n.path("slug").asText();
            boolean hasVariants = n.path("hasVariants").asBoolean(false);

            // skip default
            if (DEFAULT_PRODUCT_TYPE_SLUG.equals(slug) || DEFAULT_PRODUCT_TYPE_NAME.equals(name)) continue;

            // desired sets
            Set<String> desiredProd = new HashSet<>();
            for (JsonNode a : n.path("productAttributes")) desiredProd.add(a.asText());

            Map<String, Boolean> desiredVar = new HashMap<>();
            for (JsonNode a : n.path("variantAttributes"))
                desiredVar.put(a.path("slug").asText(), a.path("variantSelection").asBoolean(false));

            // existing?
            ObjectNode existing = getProductTypeBySlug(slug);
            String ptId;
            if (existing == null) {
                ObjectNode input = mapper().createObjectNode();
                input.put("name", name);
                input.put("slug", slug);
                input.put("hasVariants", hasVariants);
                JsonNode cr = call(M_PRODUCT_TYPE_CREATE, vars("input", input));
                JsonNode errs = cr.path("data").path("productTypeCreate").path("errors");
                if (errs.isArray() && errs.size() > 0) {
                    System.out.println("❌ productTypeCreate error for " + slug + " : " + errs);
                    continue;
                }
                ptId = cr.path("data").path("productTypeCreate").path("productType").path("id").asText();
                existing = getProductTypeBySlug(slug); // refresh structure for attribute diffs
            } else {
                ptId = existing.path("id").asText();
                boolean curHV = existing.path("hasVariants").asBoolean(false);

                // Only modify hasVariants (keeping slug stable). Update name if changed.
                ObjectNode upInput = mapper().createObjectNode();
                boolean needUpdate = false;
                if (curHV != hasVariants) { upInput.put("hasVariants", hasVariants); needUpdate = true; }
                if (!existing.path("name").asText("").equals(name) && name != null && !name.isBlank()) {
                    upInput.put("name", name); needUpdate = true;
                }
                if (needUpdate) {
                    JsonNode up = call(M_PRODUCT_TYPE_UPDATE, vars("id", ptId, "input", upInput));
                    JsonNode errs = up.path("data").path("productTypeUpdate").path("errors");
                    if (errs.isArray() && errs.size() > 0) {
                        System.out.println("❌ productTypeUpdate error for " + slug + " : " + errs);
                    } else {
                        // refresh after update
                        existing = getProductTypeBySlug(slug);
                    }
                }
            }

            // current attributes (after create/update)
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
                if (!ids.isEmpty()) call(M_PRODUCT_ATTRIBUTE_UNASSIGN, vars("productTypeId", ptId, "attributeIds", ids));
            }

            if (!toAssign.isEmpty()) {
                call(M_PRODUCT_ATTRIBUTE_ASSIGN, vars("productTypeId", ptId, "ops", toAssign));
            }

            // Update variantSelection flags where the attribute is already assigned as VARIANT
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
            }
        }

        return "OK  ";
    }
}
