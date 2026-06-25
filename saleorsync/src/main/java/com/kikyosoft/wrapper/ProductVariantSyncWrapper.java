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
import java.util.concurrent.ConcurrentHashMap;
import com.kyoko.common.StringReturnCallback;


@Component
public class ProductVariantSyncWrapper {
    private final SaleorConfig config;
    private final RestTemplate rest;

    public ProductVariantSyncWrapper(SaleorConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.rest = restTemplate;
    }

    private ObjectMapper mapper() { return config.getMapper(); }

    /* ---------------- Caches (per JVM) ---------------- */
    private final Map<String, String> productSlugToId = new ConcurrentHashMap<>();
    private final Map<String, String> variantSkuToId = new ConcurrentHashMap<>();
    private final Map<String, String> channelSlugToId = new ConcurrentHashMap<>();

    /* ---------------- Utilities ---------------- */
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

    private static boolean isChoiceBased(String inputType) {
        if (inputType == null) return false;
        switch (inputType.toUpperCase()) {
            case "DROPDOWN":
            case "MULTISELECT":
            case "SWATCH":
                return true;
            default:
                return false;
        }
    }

    /* ---------------- GraphQL (Saleor 3.21.x) ---------------- */

    private static final String Q_CHANNELS =
        "query{ channels{ id slug name currencyCode } }";

    private static final String Q_PRODUCTS_PAGE =
        "query($after:String){\n" +
        "  products(first:250, after:$after){\n" +
        "    pageInfo{ hasNextPage endCursor }\n" +
        "    edges{ node{ id slug } }\n" +
        "  }\n" +
        "}";

    private static final String Q_PRODUCT_ID_BY_SLUG =
        "query($slug:String!){\n" +
        "  products(first:1, filter:{ slugs: [$slug] }){\n" +
        "    edges{ node{ id slug } }\n" +
        "  }\n" +
        "}";
    
    private static final String Q_PRODUCTS_BY_SLUGS =
    	    "query($slugs:[String!]!){\n" +
    	    "  products(first:1000, filter:{ slugs: $slugs }){\n" +
    	    "    edges{\n" +
    	    "      node{\n" +
    	    "        id slug\n" +
    	    "        variants{\n" +
    	    "          id sku name trackInventory\n" +
    	    "          attributes{ attribute{ slug inputType } values{ name slug } }\n" +
    	    "          channelListings{\n" +
    	    "            channel{ id slug }\n" +
    	    "            price{ amount currency }\n" +
    	    "            costPrice{ amount currency }\n" +
    	    "          }\n" +
    	    "        }\n" +
    	    "      }\n" +
    	    "    }\n" +
    	    "  }\n" +
    	    "}";

    

    private static final String Q_VARIANTS_PAGE =
        "query($after:String){\n" +
        "  productVariants(first:250, after:$after){\n" +
        "    pageInfo{ hasNextPage endCursor }\n" +
        "    edges{ node{ id sku product{ id } } }\n" +
        "  }\n" +
        "}";

    // 3.21.x: 'skus:' filter removed; use 'search:' instead
    private static final String Q_VARIANT_BY_SKU =
        "query($sku:String!){\n" +
        "  productVariants(first:1, filter:{ search: $sku }){\n" +
        "    edges{ node{ id sku product{ id slug } } }\n" +
        "  }\n" +
        "}";

    private static final String Q_VARIANT_ATTRS_PAGE =
        "query($after:String){\n" +
        "  attributes(first:100, after:$after){\n" +
        "    pageInfo{ hasNextPage endCursor }\n" +
        "    edges{ node{ id slug type inputType\n" +
        "      choices(first:100){ edges{ node{ id slug name } } }\n" +
        "    } }\n" +
        "  }\n" +
        "}";

    private static final String Q_PRODUCTS_WITH_VARIANTS_PAGE =
        "query($after:String){\n" +
        "  products(first:100, after:$after){\n" +
        "    pageInfo{ hasNextPage endCursor }\n" +
        "    edges{ node{\n" +
        "      id slug\n" +
        "      variants{\n" +
        "        id sku name trackInventory\n" +
        "        attributes{ attribute{ slug inputType } values{ name slug } }\n" +
        "        channelListings{\n" +
        "          channel{ id slug }\n" +
        "          price{ amount currency }\n" +
        "          costPrice{ amount currency }\n" +
        "        }\n" +
        "      }\n" +
        "    } }\n" +
        "  }\n" +
        "}";

    private static final String M_ATTRIBUTE_VALUE_CREATE =
        "mutation($attr:ID!, $name:String!, $slug:String!){\n" +
        "  attributeValueCreate(attribute:$attr, input:{ name:$name, slug:$slug }){\n" +
        "    value{ id slug }\n" +
        "    errors{ field message code }\n" +
        "  }\n" +
        "}";

    private static final String M_VARIANT_CREATE =
        "mutation($input: ProductVariantCreateInput!){\n" +
        "  productVariantCreate(input:$input){\n" +
        "    productVariant{ id sku trackInventory }\n" +
        "    errors{ field message code }\n" +
        "  }\n" +
        "}";

    private static final String M_VARIANT_UPDATE =
        "mutation($id:ID!, $input: ProductVariantInput!){\n" +
        "  productVariantUpdate(id:$id, input:$input){\n" +
        "    productVariant{ id sku trackInventory }\n" +
        "    errors{ field message code }\n" +
        "  }\n" +
        "}";

    private static final String M_VARIANT_DELETE =
        "mutation($id:ID!){ productVariantDelete(id:$id){ errors{ field message code } } }";

    private static final String M_VARIANT_CHANNEL_LISTING_UPDATE =
        "mutation($id:ID!, $input:[ProductVariantChannelListingAddInput!]!){\n" +
        "  productVariantChannelListingUpdate(id:$id, input:$input){\n" +
        "    errors{ field message code }\n" +
        "  }\n" +
        "}";

    /* ---------------- Lookups & Preloads ---------------- */

    private void preloadChannels() {
        if (!channelSlugToId.isEmpty()) return;
        JsonNode r = call(Q_CHANNELS, null);
        for (JsonNode c : r.path("data").path("channels")) {
            String slug = c.path("slug").asText();
            String id = c.path("id").asText();
            if (!slug.isEmpty() && !id.isEmpty()) channelSlugToId.put(slug, id);
        }
        System.out.println("[DBG] channels loaded: " + channelSlugToId);
    }

    private void preloadProducts() {
        if (!productSlugToId.isEmpty()) return;
        String after = null;
        do {
            JsonNode r = call(Q_PRODUCTS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("products");
            for (JsonNode e : conn.path("edges")) {
                JsonNode n = e.path("node");
                String slug = n.path("slug").asText();
                String id = n.path("id").asText();
                if (!slug.isBlank() && !id.isBlank()) productSlugToId.put(slug, id);
            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);
        System.out.println("[DBG] products loaded: " + productSlugToId.size() + " slugs");
    }

    /*
    private void preloadVariants(boolean force) {
        if (!force && !variantSkuToId.isEmpty()) return;
        String after = null;
        do {
            JsonNode r = call(Q_VARIANTS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("productVariants");
            for (JsonNode e : conn.path("edges")) {
                JsonNode n = e.path("node");
                String id = n.path("id").asText();
                String sku = n.path("sku").asText();
                if (!id.isBlank() && !sku.isBlank()) variantSkuToId.put(sku, id);
            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);
        System.out.println("[DBG] variants loaded: " + variantSkuToId.size() + " SKUs");
    }
    */
    
    private void preloadVariants(boolean force) {
        if (!force && !variantSkuToId.isEmpty()) return;
        variantSkuToId.clear();;
        String after = null;
        do {
            JsonNode r = call(Q_PRODUCTS_WITH_VARIANTS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("products");

            for (JsonNode pe : conn.path("edges")) {
                JsonNode prod = pe.path("node");
                JsonNode vlist = prod.path("variants");  // still works in 3.21.x (deprecated, but present)
                if (vlist.isArray()) {
                    for (JsonNode v : vlist) {
                        String id  = v.path("id").asText();
                        String sku = v.path("sku").asText();
                        if (!id.isBlank() && !sku.isBlank()) {
                            variantSkuToId.put(sku, id);
                        }
                    }
                }
            }

            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);

        System.out.println("[DBG] variants loaded: " + variantSkuToId.size() + " SKUs");
    }

    
    

    private String productIdBySlug(String slug) {
        if (slug == null || slug.isBlank()) return null;
        String hit = productSlugToId.get(slug);
        if (hit != null) return hit;
        JsonNode r = call(Q_PRODUCT_ID_BY_SLUG, vars("slug", slug));
        JsonNode edges = r.path("data").path("products").path("edges");
        String id = edges.isArray() && edges.size() > 0 ? edges.get(0).path("node").path("id").asText() : null;
        if (id != null) productSlugToId.put(slug, id);
        return id;
    }

    private String variantIdBySku(String sku) {
        if (sku == null || sku.isBlank()) return null;
        String id = variantSkuToId.get(sku);
        if (id != null) return id;
        JsonNode r = call(Q_VARIANT_BY_SKU, vars("sku", sku));
        JsonNode edges = r.path("data").path("productVariants").path("edges");
        id = edges.isArray() && edges.size() > 0 ? edges.get(0).path("node").path("id").asText() : null;
        if (id != null) variantSkuToId.put(sku, id);
        return id;
    }

    /* ---------------- Variant Attributes Helpers ---------------- */

    private static class AttrInfo {
        String id;
        String inputType;
        Map<String, String> valueSlugToId = new HashMap<>();
    }

    /** Variant attributes (accept both VARIANT_TYPE and VARIANT) */
    private Map<String, AttrInfo> loadVariantAttributes() {
        Map<String, AttrInfo> map = new HashMap<>();
        String after = null;
        do {
            JsonNode r = call(Q_VARIANT_ATTRS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("attributes");
            for (JsonNode e : conn.path("edges")) {
                JsonNode n = e.path("node");
//                String t = n.path("type").asText();
//                if (!"VARIANT_TYPE".equals(t) && !"VARIANT".equals(t)) continue;

                AttrInfo ai = new AttrInfo();
                ai.id = n.path("id").asText();
                ai.inputType = n.path("inputType").asText();
                JsonNode choices = n.path("choices").path("edges");
                if (choices.isArray()) {
                    for (JsonNode ce : choices) {
                        String vs = ce.path("node").path("slug").asText();
                        String vid = ce.path("node").path("id").asText();
                        if (!vs.isEmpty() && !vid.isEmpty()) ai.valueSlugToId.put(vs, vid);
                    }
                }
                String aSlug = n.path("slug").asText();
                map.put(aSlug, ai);
            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);

        System.out.println("[DBG] variant attrs loaded: " + map.keySet());
        return map;
    }

    private ArrayNode exportVariantAttributes(JsonNode attrEdges) {
        ArrayNode out = mapper().createArrayNode();
        for (JsonNode av : attrEdges) {
            String aSlug = av.path("attribute").path("slug").asText();
            String inputType = av.path("attribute").path("inputType").asText();
            ArrayNode values = mapper().createArrayNode();
            if (isChoiceBased(inputType)) {
                for (JsonNode v : av.path("values")) values.add(v.path("slug").asText());
            } else {
                for (JsonNode v : av.path("values")) values.add(v.path("name").asText());
            }
            ObjectNode one = mapper().createObjectNode();
            one.put("attribute", aSlug);
            one.put("inputType", inputType);
            one.set("values", values);
            out.add(one);
        }
        return out;
    }

    /** Build GraphQL payload using SERVER inputType (not JSON). */
    private ArrayNode buildVariantAttributesPayload(ArrayNode exportAttrs, Map<String, AttrInfo> attrMap) {
        ArrayNode out = mapper().createArrayNode();
        if (exportAttrs == null) return out;

        System.out.println("[DBG] attrMap has: " + attrMap.keySet());
        for (JsonNode a : exportAttrs) {
            String aSlug = a.path("attribute").asText();
            AttrInfo ai = attrMap.get(aSlug);
            if (ai == null) {
                System.out.println("⚠️ Missing variant attribute (not in attrMap): " + aSlug);
                continue;
            }

            String serverInputType = ai.inputType; // trust server
            ArrayNode exportedValues = a.path("values").isArray() ? (ArrayNode) a.path("values") : mapper().createArrayNode();

            ObjectNode entry = mapper().createObjectNode();
            entry.put("id", ai.id);

            System.out.println("[DBG] building attr payload aSlug=" + aSlug +
                               " serverInputType=" + serverInputType +
                               " values=" + exportedValues.toString());

            if (isChoiceBased(serverInputType)) {
                if ("DROPDOWN".equalsIgnoreCase(serverInputType)) {
                    String vSlug = exportedValues.size() > 0 ? exportedValues.get(0).asText() : null;
                    if (vSlug != null && !vSlug.isEmpty()) {
                        String vId = ai.valueSlugToId.get(vSlug);
                        if (vId == null) {
                            JsonNode mk = call(M_ATTRIBUTE_VALUE_CREATE, vars("attr", ai.id, "name", vSlug, "slug", vSlug));
                            JsonNode err = mk.path("data").path("attributeValueCreate").path("errors");
                            if (err.isArray() && err.size() > 0) {
                                System.out.println("⚠️ attributeValueCreate: " + err);
                            } else {
                                vId = mk.path("data").path("attributeValueCreate").path("value").path("id").asText();
                                if (vId != null) ai.valueSlugToId.put(vSlug, vId);
                            }
                        }
                        if (vId != null) entry.put("dropdown", vId);
                    }
                } else {
                    ArrayNode ids = mapper().createArrayNode();
                    for (JsonNode v : exportedValues) {
                        String vSlug = v.asText();
                        if (vSlug == null || vSlug.isEmpty()) continue;
                        String vId = ai.valueSlugToId.get(vSlug);
                        if (vId == null) {
                            JsonNode mk = call(M_ATTRIBUTE_VALUE_CREATE, vars("attr", ai.id, "name", vSlug, "slug", vSlug));
                            JsonNode err = mk.path("data").path("attributeValueCreate").path("errors");
                            if (err.isArray() && err.size() > 0) {
                                System.out.println("⚠️ attributeValueCreate: " + err);
                                continue;
                            }
                            vId = mk.path("data").path("attributeValueCreate").path("value").path("id").asText();
                            if (vId != null) ai.valueSlugToId.put(vSlug, vId);
                        }
                        ids.add(vId);
                    }
                    entry.set("multiselect", ids);
                }
            } else {
                if ("PLAIN_TEXT".equalsIgnoreCase(serverInputType)) {
                    String v = exportedValues.size() > 0 ? exportedValues.get(0).asText() : null;
                    if (v != null) entry.put("plainText", v);
                } else if ("NUMERIC".equalsIgnoreCase(serverInputType)) {
                    String v = exportedValues.size() > 0 ? exportedValues.get(0).asText() : null;
                    if (v != null) entry.put("numeric", v);
                } else if ("BOOLEAN".equalsIgnoreCase(serverInputType)) {
                    String v = exportedValues.size() > 0 ? exportedValues.get(0).asText() : null;
                    if (v != null) entry.put("boolean", Boolean.parseBoolean(v));
                } else {
                    String v = exportedValues.size() > 0 ? exportedValues.get(0).asText() : null;
                    if (v != null) entry.put("plainText", v);
                }
            }

            out.add(entry);
        }

        System.out.println("[DBG] final built variant attrs payload: " + out.toString());
        return out;
    }

    /* ---------------- Export ---------------- */

    /** Windowed export; returns "OK  [ ... ]" (no stocks) */
    private static final String ACTION_ABORT = "abort";

    /**
     * Parse callback return JSON and decide whether to abort.
     * Expected return example: {"action":"abort","reason":"user clicked cancel"}
     */
    private boolean shouldAbort(String cbReturnJson) {
        if (cbReturnJson == null || cbReturnJson.isBlank()) return false;
        try {
            JsonNode n = mapper().readTree(cbReturnJson);
            String action = n.path("action").asText(null);
            return ACTION_ABORT.equalsIgnoreCase(action);
        } catch (Exception e) {
            // If callback returns non-JSON, ignore and continue (safer default).
            System.out.println("⚠️ callback returned non-JSON (ignored): " + e.getMessage());
            return false;
        }
    }

    /** Optional: get reason for logging */
    private String abortReason(String cbReturnJson) {
        if (cbReturnJson == null || cbReturnJson.isBlank()) return null;
        try {
            JsonNode n = mapper().readTree(cbReturnJson);
            return n.path("reason").asText(null);
        } catch (Exception e) {
            return null;
        }
    }
 
    public String exportVariants(
            int start,
            int count
    ) throws Exception {
    	return(exportVariants(start,count,null,null));
    }
    public String exportVariants(
            int start,
            int count,
            List<String> productSlugs
    ) throws Exception {
    	return(exportVariants(start,count,productSlugs,null));
    }
    public String exportVariants(
            int start,
            int count,
            StringReturnCallback cb
    ) throws Exception {
    	return(exportVariants(start,count,null,cb));
    }
    
    public String exportVariants(
            int start,
            int count,
            List<String> productSlugs,
            StringReturnCallback cb
    ) throws Exception {

        if (start < 0) start = 0;
        if (count < 0) count = 0;

        int fetched = 0;
        int endExclusive = (count == 0) ? Integer.MAX_VALUE : start + count;
        int chunkIndex = 0;

        // Normalize slugs
        List<String> slugs = null;
        if (productSlugs != null) {
            slugs = new ArrayList<>();
            for (String s : productSlugs) {
                if (s != null && !s.isBlank()) slugs.add(s.trim());
            }
            if (slugs.isEmpty()) {
                ObjectNode summary = mapper().createObjectNode();
                summary.put("type", "variantExportSummary");
                summary.put("fetched", 0);
                summary.put("aborted", false);
                summary.put("timestamp", java.time.Instant.now().toString());
                return "OK  " + mapper().writeValueAsString(summary);
            }
        }

        /* =========================
           CASE A: Full scan (no filter)
           ========================= */
        if (slugs == null) {
            String after = null;
            int index = 0;

            outer:
            do {
                JsonNode r = call(Q_PRODUCTS_WITH_VARIANTS_PAGE, vars("after", after));
                JsonNode conn = r.path("data").path("products");

                ArrayNode pageRows = mapper().createArrayNode();

                for (JsonNode pe : conn.path("edges")) {
                    JsonNode prod = pe.path("node");
                    String pslug = prod.path("slug").asText();

                    JsonNode vlist = prod.path("variants");
                    if (vlist.isArray()) {
                        for (JsonNode v : vlist) {
                            if (index >= endExclusive) break outer;

                            if (index >= start) {
                                pageRows.add(buildVariantExportRow(pslug, v));
                                fetched++;
                            }
                            index++;
                        }
                    }
                }

                if (cb != null && pageRows.size() > 0) {
                    ObjectNode ret = mapper().createObjectNode();
                    ret.put("type", "variantExport");
                    ret.put("mode", "productsPage");
                    ret.put("chunkIndex", chunkIndex++);
                    ret.put("fetchedSoFar", fetched);
                    ret.put("start", start);
                    ret.put("count", count);
                    ret.put("timestamp", java.time.Instant.now().toString());
                    ret.set("data", pageRows);

                    String cbResp = cb.returnCallback(mapper().writeValueAsString(ret));
                    if (shouldAbort(cbResp)) {
                        String reason = abortReason(cbResp);
                        ObjectNode summary = mapper().createObjectNode();
                        summary.put("type", "variantExportSummary");
                        summary.put("fetched", fetched);
                        summary.put("aborted", true);
                        if (reason != null) summary.put("reason", reason);
                        summary.put("timestamp", java.time.Instant.now().toString());
                        return "OK  " + mapper().writeValueAsString(summary);
                    }
                }

                boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
                after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;

            } while (after != null);

            ObjectNode summary = mapper().createObjectNode();
            summary.put("type", "variantExportSummary");
            summary.put("fetched", fetched);
            summary.put("aborted", false);
            summary.put("timestamp", java.time.Instant.now().toString());
            return "OK  " + mapper().writeValueAsString(summary);
        }

        /* =========================
           CASE B: Filtered by product slugs (optimized)
           ========================= */

        final int SLUG_BATCH_SIZE = 100;
        int index2 = 0;

        for (int i = 0; i < slugs.size(); i += SLUG_BATCH_SIZE) {
            List<String> batch = slugs.subList(i, Math.min(slugs.size(), i + SLUG_BATCH_SIZE));

            JsonNode r = call(Q_PRODUCTS_BY_SLUGS, vars("slugs", batch));
            JsonNode edges = r.path("data").path("products").path("edges");

            ArrayNode pageRows = mapper().createArrayNode();

            for (JsonNode pe : edges) {
                JsonNode prod = pe.path("node");
                String pslug = prod.path("slug").asText();

                JsonNode vlist = prod.path("variants");
                if (vlist.isArray()) {
                    for (JsonNode v : vlist) {
                        if (index2 >= endExclusive) break;

                        if (index2 >= start) {
                            pageRows.add(buildVariantExportRow(pslug, v));
                            fetched++;
                        }
                        index2++;
                    }
                }
                if (index2 >= endExclusive) break;
            }

            if (cb != null && pageRows.size() > 0) {
                ObjectNode ret = mapper().createObjectNode();
                ret.put("type", "variantExport");
                ret.put("mode", "slugBatch");
                ret.put("chunkIndex", chunkIndex++);
                ret.put("fetchedSoFar", fetched);
                ret.put("start", start);
                ret.put("count", count);
                ret.set("productSlugs", mapper().valueToTree(batch));
                ret.put("timestamp", java.time.Instant.now().toString());
                ret.set("data", pageRows);

                String cbResp = cb.returnCallback(mapper().writeValueAsString(ret));
                if (shouldAbort(cbResp)) {
                    String reason = abortReason(cbResp);
                    ObjectNode summary = mapper().createObjectNode();
                    summary.put("type", "variantExportSummary");
                    summary.put("fetched", fetched);
                    summary.put("aborted", true);
                    if (reason != null) summary.put("reason", reason);
                    summary.put("timestamp", java.time.Instant.now().toString());
                    return "OK  " + mapper().writeValueAsString(summary);
                }
            }

            if (index2 >= endExclusive) break;
        }

        ObjectNode summary = mapper().createObjectNode();
        summary.put("type", "variantExportSummary");
        summary.put("fetched", fetched);
        summary.put("aborted", false);
        summary.put("timestamp", java.time.Instant.now().toString());
        return "OK  " + mapper().writeValueAsString(summary);
    }
 
  

    private ObjectNode buildVariantExportRow(String productSlug, JsonNode v) {
        ObjectNode row = mapper().createObjectNode();

        row.put("productSlug", productSlug);
        row.put("sku", v.path("sku").asText());
        if (!v.path("name").isNull()) row.put("name", v.path("name").asText());
        row.put("trackInventory", v.path("trackInventory").asBoolean(false));
        row.set("attributes", exportVariantAttributes(v.path("attributes")));

        ArrayNode chs = mapper().createArrayNode();
        for (JsonNode cl : v.path("channelListings")) {
            ObjectNode ch = mapper().createObjectNode();
            ch.put("slug", cl.path("channel").path("slug").asText());
            if (!cl.path("price").isNull()) {
                ch.put("price", String.format("%.2f", cl.path("price").path("amount").asDouble(0)));
                ch.put("currency", cl.path("price").path("currency").asText(null));
            }
            if (!cl.path("costPrice").isNull()) {
                ch.put("costPrice", String.format("%.2f", cl.path("costPrice").path("amount").asDouble(0)));
            }
            chs.add(ch);
        }
        row.set("channels", chs);

        return row;
    }
 
    
    
    public String exportVariantsYY(int start, int count) throws Exception {
        if (start < 0) start = 0;
        if (count < 0) count = 0;

        ArrayNode out = mapper().createArrayNode();
        String after = null;
        int index = 0;
        int endExclusive = (count == 0) ? Integer.MAX_VALUE : start + count;

        outer:
        do {
            JsonNode r = call(Q_PRODUCTS_WITH_VARIANTS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("products");
            for (JsonNode pe : conn.path("edges")) {
                JsonNode prod = pe.path("node");
                String pslug = prod.path("slug").asText();

                JsonNode vlist = prod.path("variants");
                if (vlist.isArray()) {
                    for (JsonNode v : vlist) {
                        if (index >= endExclusive) break outer;
                        if (index >= start) {
                            ObjectNode row = mapper().createObjectNode();
                            row.put("productSlug", pslug);
                            row.put("sku", v.path("sku").asText());
                            if (!v.path("name").isNull()) row.put("name", v.path("name").asText());
                            row.put("trackInventory", v.path("trackInventory").asBoolean(false));
                            row.set("attributes", exportVariantAttributes(v.path("attributes")));

                            ArrayNode chs = mapper().createArrayNode();
                            for (JsonNode cl : v.path("channelListings")) {
                                ObjectNode ch = mapper().createObjectNode();
                                ch.put("slug", cl.path("channel").path("slug").asText());
                                if (!cl.path("price").isNull()) {
                                    ch.put("price", String.format("%.2f", cl.path("price").path("amount").asDouble(0)));
                                    ch.put("currency", cl.path("price").path("currency").asText(null));
                                }
                                if (!cl.path("costPrice").isNull()) {
                                    ch.put("costPrice", String.format("%.2f", cl.path("costPrice").path("amount").asDouble(0)));
                                }
                                chs.add(ch);
                            }
                            row.set("channels", chs);

                            out.add(row);
                        }
                        index++;
                    }
                }
            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);

        return "OK  " + mapper().writeValueAsString(out);
    }

    /* ---------------- Import (upsert by SKU) ---------------- */

    public String importVariants(String json) throws Exception {
        if (json == null || json.isBlank()) return "OK  ";

        ArrayNode rows = (ArrayNode) mapper().readTree(json);

        // warm caches
        preloadChannels();
        preloadProducts();
        preloadVariants(true);
        Map<String, AttrInfo> varAttrMap = loadVariantAttributes();  // printed inside

        int created = 0, updated = 0, failed = 0;

        for (JsonNode n : rows) {
            String productSlug = n.path("productSlug").asText(null);
            String sku = n.path("sku").asText(null);
            if (productSlug == null || sku == null || productSlug.isBlank() || sku.isBlank()) {
                System.out.println("❌ Missing productSlug or sku in row: " + n);
                failed++; continue;
            }

            String productId = productIdBySlug(productSlug);
            if (productId == null) {
                System.out.println("❌ product not found for variant import, slug=" + productSlug + " (sku " + sku + ")");
                failed++; continue;
            }

            String name = n.path("name").isNull() ? null : n.path("name").asText(null);
            ArrayNode attrPayload = buildVariantAttributesPayload((ArrayNode) n.path("attributes"), varAttrMap);

            System.out.println("[DBG] about to upsert variant sku=" + sku + " productSlug=" + productSlug +
                               " attrPayload=" + attrPayload.toString());

            String variantId = variantIdBySku(sku);
            if (variantId == null) {
                ObjectNode input = mapper().createObjectNode();
                input.put("product", productId);
                input.put("sku", sku);
                input.put("trackInventory", false);
                if (name != null) input.put("name", name);
                if (attrPayload != null) input.set("attributes", attrPayload);

                System.out.println("[DBG] productVariantCreate input=" + input.toString());

                JsonNode r = call(M_VARIANT_CREATE, vars("input", input));
                JsonNode errs = r.path("data").path("productVariantCreate").path("errors");
                if (errs.isArray() && errs.size() > 0) {
                    System.out.println("❌ productVariantCreate error for sku=" + sku + " : " + errs);
                    failed++; continue;
                }
                variantId = r.path("data").path("productVariantCreate").path("productVariant").path("id").asText();
                if (variantId != null) variantSkuToId.put(sku, variantId);
                created++;
            } else {
                ObjectNode input = mapper().createObjectNode();
                input.put("trackInventory", false);
                if (name != null) input.put("name", name);
                if (attrPayload != null) input.set("attributes", attrPayload);

                System.out.println("[DBG] productVariantUpdate id=" + variantId + " input=" + input.toString());

                JsonNode r = call(M_VARIANT_UPDATE, vars("id", variantId, "input", input));
                JsonNode errs = r.path("data").path("productVariantUpdate").path("errors");
                if (errs.isArray() && errs.size() > 0) {
                    System.out.println("❌ productVariantUpdate error for sku=" + sku + " : " + errs);
                    failed++; continue;
                }
                updated++;
            }

            // Channel listings
            JsonNode chs = n.path("channels");
            if (chs.isArray() && chs.size() > 0) {
                ArrayNode adds = mapper().createArrayNode();
                for (JsonNode ch : chs) {
                    String slug = ch.path("slug").asText(null);
                    if (slug == null || slug.isBlank()) continue;
                    String chId = channelSlugToId.get(slug);
                    if (chId == null) { System.out.println("⚠️ unknown channel slug "+slug+" (skip)"); continue; }
                    ObjectNode row = mapper().createObjectNode();
                    row.put("channelId", chId);
                    if (ch.hasNonNull("price"))     row.put("price", ch.path("price").asText());
                    if (ch.hasNonNull("costPrice")) row.put("costPrice", ch.path("costPrice").asText());
                    adds.add(row);
                }
                if (adds.size() > 0) {
                    System.out.println("[DBG] productVariantChannelListingUpdate id=" + variantId + " input=" + adds.toString());
                    JsonNode r2 = call(M_VARIANT_CHANNEL_LISTING_UPDATE, vars("id", variantId, "input", adds));
                    JsonNode e2 = r2.path("data").path("productVariantChannelListingUpdate").path("errors");
                    if (e2.isArray() && e2.size() > 0) System.out.println("⚠️ variant channel update errors sku="+sku+": " + e2);
                }
            }

            // stocks intentionally ignored/removed when trackInventory=false
        }

        return String.format("OK  Created:%d Updated:%d Failed:%d", created, updated, failed);
    }

    /* ---------------- Delete APIs ---------------- */

    /** Delete selected variants by their SKUs. */
    public String deleteVariantRecords(java.util.List<String> skus) {
        if (skus == null || skus.isEmpty()) return "OK  Deleted:0 Missing:0 Errors:0";
        preloadVariants(true);
        int deleted = 0, missing = 0, errors = 0;

        for (String sku : skus) {
            if (sku == null || sku.isBlank()) { missing++; continue; }
            String id = variantSkuToId.get(sku);
            if (id == null) { missing++; continue; }

            try {
                JsonNode d = call(M_VARIANT_DELETE, vars("id", id));
                JsonNode errs = d.path("data").path("productVariantDelete").path("errors");
                if (errs.isArray() && errs.size() > 0) {
                    System.out.println("⚠️ productVariantDelete error for sku=" + sku + " : " + errs);
                    errors++;
                } else {
                    deleted++;
                    variantSkuToId.remove(sku);
                }
            } catch (Exception ex) {
                System.out.println("⚠️ productVariantDelete exception for sku=" + sku + " : " + ex.getMessage());
                errors++;
            }
        }
        return String.format("OK  Deleted:%d Missing:%d Errors:%d", deleted, missing, errors);
    }

    /** Delete all variants (paged through products). */
    public String deleteAllVariants() {
        int deleted = 0, errors = 0;
        String after = null;
        do {
            JsonNode r = call(Q_PRODUCTS_WITH_VARIANTS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("products");
            for (JsonNode pe : conn.path("edges")) {
                JsonNode vlist = pe.path("node").path("variants");
                if (vlist.isArray()) {
                    for (JsonNode v : vlist) {
                        String vid = v.path("id").asText();
                        String sku = v.path("sku").asText();
                        JsonNode d = call(M_VARIANT_DELETE, vars("id", vid));
                        JsonNode errs = d.path("data").path("productVariantDelete").path("errors");
                        if (errs.isArray() && errs.size() > 0) {
                            System.out.println("⚠️ productVariantDelete error for sku=" + sku + " : " + errs);
                            errors++;
                        } else {
                            deleted++;
                            variantSkuToId.remove(sku);
                        }
                    }
                }
            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);
        return "Deleted " + deleted + " variants. Errors: " + errors;
    }

    /* ---------------- Single-variant update (no stock) ---------------- */

    public String updateVariant(
        String sku,
        String name,
        String setPriceChannel, String price, String costPrice,
        String attrSlug, String plainText, String dropdownSlug, String multiselectSlugs
    ) {
        String variantId = variantIdBySku(sku);
        if (variantId == null) return "Variant not found by sku: " + sku;

        boolean didUpdate = false;
        ObjectNode vin = mapper().createObjectNode();

        // Always enforce trackInventory=false on any update
        vin.put("trackInventory", false);
        didUpdate = true;

        // Name
        if (name != null) { vin.put("name", name); didUpdate = true; }

        // Attribute
        if (attrSlug != null) {
            Map<String, AttrInfo> varAttrMap = loadVariantAttributes();
            AttrInfo ai = varAttrMap.get(attrSlug);
            if (ai == null) return "Variant attribute not found: " + attrSlug;

            ObjectNode one = mapper().createObjectNode();
            one.put("id", ai.id);

            System.out.println("[DBG] updateVariant set attr slug=" + attrSlug + " serverInputType=" + ai.inputType);

            if (isChoiceBased(ai.inputType)) {
                if (dropdownSlug != null && !dropdownSlug.isEmpty()) {
                    String vId = ai.valueSlugToId.get(dropdownSlug);
                    if (vId == null) {
                        JsonNode mk = call(M_ATTRIBUTE_VALUE_CREATE, vars("attr", ai.id, "name", dropdownSlug, "slug", dropdownSlug));
                        JsonNode err = mk.path("data").path("attributeValueCreate").path("errors");
                        if (err.isArray() && err.size() > 0) return "Failed to create value: " + err;
                        vId = mk.path("data").path("attributeValueCreate").path("value").path("id").asText();
                    }
                    one.put("dropdown", vId);
                } else if (multiselectSlugs != null && !multiselectSlugs.isEmpty()) {
                    ArrayNode ids = mapper().createArrayNode();
                    for (String s : multiselectSlugs.split("\\|")) {
                        s = s.trim(); if (s.isEmpty()) continue;
                        String vId = ai.valueSlugToId.get(s);
                        if (vId == null) {
                            JsonNode mk = call(M_ATTRIBUTE_VALUE_CREATE, vars("attr", ai.id, "name", s, "slug", s));
                            JsonNode err = mk.path("data").path("attributeValueCreate").path("errors");
                            if (err.isArray() && err.size() > 0) return "Failed to create value '"+s+"': " + err;
                            vId = mk.path("data").path("attributeValueCreate").path("value").path("id").asText();
                        }
                        ids.add(vId);
                    }
                    one.set("multiselect", ids);
                } else {
                    return "Choice attribute requires dropdown or multiselect";
                }
            } else {
                if ("PLAIN_TEXT".equalsIgnoreCase(ai.inputType)) {
                    if (plainText == null) return "Missing plainText for PLAIN_TEXT attribute";
                    one.put("plainText", plainText);
                } else if ("NUMERIC".equalsIgnoreCase(ai.inputType)) {
                    if (plainText == null) return "Provide numeric as string via plainText";
                    one.put("numeric", plainText);
                } else if ("BOOLEAN".equalsIgnoreCase(ai.inputType)) {
                    if (plainText == null) return "Provide boolean via plainText=true/false";
                    one.put("boolean", Boolean.parseBoolean(plainText));
                } else {
                    if (plainText == null) return "Provide plainText for attribute type " + ai.inputType;
                    one.put("plainText", plainText);
                }
            }
            ArrayNode arr = mapper().createArrayNode();
            arr.add(one);
            vin.set("attributes", arr);
            didUpdate = true;
        }

        if (didUpdate) {
            System.out.println("[DBG] productVariantUpdate id=" + variantId + " input=" + vin.toString());
            JsonNode r = call(M_VARIANT_UPDATE, vars("id", variantId, "input", vin));
            JsonNode errs = r.path("data").path("productVariantUpdate").path("errors");
            if (errs.isArray() && errs.size() > 0) return "variantUpdate failed: " + errs;
        }

        // Channel price
        if (setPriceChannel != null) {
            preloadChannels();
            String chId = channelSlugToId.get(setPriceChannel);
            if (chId == null) return "Unknown channel slug: " + setPriceChannel;

            ArrayNode adds = mapper().createArrayNode();
            ObjectNode row = mapper().createObjectNode();
            row.put("channelId", chId);
            if (price != null)     row.put("price", price);
            if (costPrice != null) row.put("costPrice", costPrice);
            adds.add(row);

            System.out.println("[DBG] productVariantChannelListingUpdate id=" + variantId + " input=" + adds.toString());

            JsonNode r2 = call(M_VARIANT_CHANNEL_LISTING_UPDATE, vars("id", variantId, "input", adds));
            JsonNode e2 = r2.path("data").path("productVariantChannelListingUpdate").path("errors");
            if (e2.isArray() && e2.size() > 0) return "variant channel update failed: " + e2;
        }

        return "OK (sku=" + sku + ")";
    }
}
