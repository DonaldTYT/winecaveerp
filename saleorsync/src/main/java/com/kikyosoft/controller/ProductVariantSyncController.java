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

/**
 * ProductVariantSyncController (Saleor 3.21.x)
 *
 * GET endpoints:
 *  - /admin/GetProductVariantJson      : export all variants -> /tmp/variants.json
 *  - /admin/PutProductVariantJson      : import (create/update) variants from /tmp/variants.json
 *  - /admin/DeleteAllProdctVariant     : delete all variants
 *  - /admin/UpdateProductVariant       : update one variant by SKU (price/stock/attribute)
 *
 * Export JSON shape (example):
 * [
 *   {
 *     "productSlug": "cheval-des-andes-2018",
 *     "sku": "CDA-2018-750",
 *     "name": "750ml",
 *     "attributes": [
 *       {"attribute":"format","inputType":"DROPDOWN","values":["750ml"]}
 *     ],
 *     "channels": [
 *       {"slug":"hk","price":"880.00","costPrice":"650.00","currency":"HKD"}
 *     ],
 *     "stocks": [
 *       {"warehouse":"main-hk","quantity":12}
 *     ]
 *   }
 * ]
 */
@RestController
@RequestMapping("/admin")
public class ProductVariantSyncController {

    @Autowired private SaleorConfig config;
    private final RestTemplate rest = new RestTemplate();
    private ObjectMapper mapper() { return config.getMapper(); }

    /* ----------------------------- HTTP -> GraphQL ----------------------------- */
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
    private static Map<String,Object> vars(Object... kv){
        Map<String,Object> m = new HashMap<>();
        for (int i=0;i+1<kv.length;i+=2) m.put((String)kv[i], kv[i+1]);
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

    /* -------------------------- GraphQL (Saleor 3.21.x) -------------------------- */

    // pages through products and their variants, including variant attributes, channel listings, stocks
    private static final String Q_PRODUCTS_WITH_VARIANTS_PAGE =
    	    "query($after:String){\n" +
    	    "  products(first:100, after:$after){\n" +
    	    "    pageInfo{ hasNextPage endCursor }\n" +
    	    "    edges{ node{\n" +
    	    "      id slug\n" +
    	    "      variants{\n" +                       // <-- plain array (no 'first', no 'edges')
    	    "        id sku name\n" +
    	    "        attributes{ attribute{ slug inputType } values{ name slug } }\n" +
    	    "        channelListings{\n" +
    	    "          channel{ id slug }\n" +
    	    "          price{ amount currency }\n" +
    	    "          costPrice{ amount currency }\n" +
    	    "        }\n" +
    	    "        stocks{\n" +
    	    "          warehouse{ id slug name }\n" +
    	    "          quantity\n" +
    	    "        }\n" +
    	    "      }\n" +
    	    "    } }\n" +
    	    "  }\n" +
    	    "}";


    private static final String Q_PRODUCT_BY_SLUG =
        "query($slug:String!){\n" +
        "  products(first:1, filter:{ slugs: [$slug] }){\n" +
        "    edges{ node{ id slug } }\n" +
        "  }\n" +
        "}";

    private static final String Q_CHANNELS =
        "query{ channels{ id slug name currencyCode } }";

    private static final String Q_WAREHOUSES =
        "query{ warehouses(first:100){ edges{ node{ id slug name } } } }";

    private static final String Q_VARIANT_BY_SKU =
    	    "query($sku:String!){\n" +
    	    "  productVariants(first:1, filter:{ skus: [$sku] }){\n" +
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
        "    productVariant{ id sku }\n" +
        "    errors{ field message code }\n" +
        "  }\n" +
        "}";

    private static final String M_VARIANT_UPDATE =
        "mutation($id:ID!, $input: ProductVariantInput!){\n" +
        "  productVariantUpdate(id:$id, input:$input){\n" +
        "    productVariant{ id sku }\n" +
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

    private static final String M_VARIANT_STOCKS_SET =
        "mutation($variantId:ID!, $stocks:[StockInput!]!){\n" +
        "  productVariantStocksUpdate(variantId:$variantId, stocks:$stocks){\n" +
        "    errors{ field message code }\n" +
        "  }\n" +
        "}";

    /* ----------------------------- Lookups & caches ----------------------------- */

    private String productIdBySlug(String slug){
        JsonNode r = call(Q_PRODUCT_BY_SLUG, vars("slug", slug));
        JsonNode edges = r.path("data").path("products").path("edges");
        return edges.isArray() && edges.size()>0 ? edges.get(0).path("node").path("id").asText() : null;
    }

    private String variantIdBySku(String sku){
        JsonNode r = call(Q_VARIANT_BY_SKU, vars("sku", sku));
        JsonNode edges = r.path("data").path("productVariants").path("edges");
        return edges.isArray() && edges.size()>0 ? edges.get(0).path("node").path("id").asText() : null;
    }

    private Map<String,String> loadChannelSlugToId(){
        Map<String,String> map = new HashMap<>();
        JsonNode r = call(Q_CHANNELS, null);
        for (JsonNode c : r.path("data").path("channels")) {
            map.put(c.path("slug").asText(), c.path("id").asText());
        }
        return map;
    }

    private Map<String,String> loadWarehouseSlugToId(){
        Map<String,String> map = new HashMap<>();
        JsonNode r = call(Q_WAREHOUSES, null);
        JsonNode edges = r.path("data").path("warehouses").path("edges");
        for (JsonNode e : edges) {
            JsonNode n = e.path("node");
            map.put(n.path("slug").asText(), n.path("id").asText());
        }
        return map;
    }

    private static class AttrInfo {
        String id;
        String inputType;
        Map<String,String> valueSlugToId = new HashMap<>();
    }

    // Only VARIANT_TYPE attributes
    private Map<String, AttrInfo> loadVariantAttributes(){
        Map<String, AttrInfo> map = new HashMap<>();
        String after = null;
        do {
            JsonNode r = call(Q_VARIANT_ATTRS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("attributes");
            for (JsonNode e : conn.path("edges")) {
                JsonNode n = e.path("node");
                if (!"VARIANT_TYPE".equals(n.path("type").asText())) continue;
                AttrInfo ai = new AttrInfo();
                ai.id = n.path("id").asText();
                ai.inputType = n.path("inputType").asText();
                JsonNode choices = n.path("choices").path("edges");
                if (choices.isArray()) {
                    for (JsonNode ce : choices) {
                        String vslug = ce.path("node").path("slug").asText();
                        String vid   = ce.path("node").path("id").asText();
                        if (!vslug.isEmpty() && !vid.isEmpty()) ai.valueSlugToId.put(vslug, vid);
                    }
                }
                map.put(n.path("slug").asText(), ai);
            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);
        return map;
    }

    /* --------------------------- helpers: attributes --------------------------- */

    // export variant attributes to simple {attribute, inputType, values:[...]} form
    private ArrayNode exportVariantAttributes(JsonNode attrEdges){
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

    // build API payload array for variant attributes
    private ArrayNode buildVariantAttributesPayload(ArrayNode exportAttrs, Map<String,AttrInfo> attrMap){
        ArrayNode out = mapper().createArrayNode();
        if (exportAttrs == null) return out;

        for (JsonNode a : exportAttrs) {
            String aSlug = a.path("attribute").asText();
            String inputType = a.path("inputType").asText();
            JsonNode valuesNode = a.path("values");
            ArrayNode exportedValues = valuesNode.isArray() ? (ArrayNode) valuesNode : mapper().createArrayNode();

            AttrInfo ai = attrMap.get(aSlug);
            if (ai == null) { System.out.println("⚠️ Missing variant attribute: " + aSlug); continue; }

            ObjectNode entry = mapper().createObjectNode();
            entry.put("id", ai.id);

            if (isChoiceBased(inputType)) {
                if ("DROPDOWN".equalsIgnoreCase(inputType)) {
                    String vSlug = exportedValues.size()>0 ? exportedValues.get(0).asText() : null;
                    if (vSlug != null && !vSlug.isEmpty()) {
                        String vId = ai.valueSlugToId.get(vSlug);
                        if (vId == null) {
                            JsonNode mk = call(M_ATTRIBUTE_VALUE_CREATE, vars("attr", ai.id, "name", vSlug, "slug", vSlug));
                            JsonNode err = mk.path("data").path("attributeValueCreate").path("errors");
                            if (err.isArray() && err.size()>0) { System.out.println("⚠️ attributeValueCreate: "+err); }
                            else {
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
                            if (err.isArray() && err.size()>0) { System.out.println("⚠️ attributeValueCreate: "+err); continue; }
                            vId = mk.path("data").path("attributeValueCreate").path("value").path("id").asText();
                            if (vId != null) ai.valueSlugToId.put(vSlug, vId);
                        }
                        ids.add(vId);
                    }
                    entry.set("multiselect", ids);
                }
            } else {
                if ("PLAIN_TEXT".equalsIgnoreCase(inputType)) {
                    String v = exportedValues.size()>0 ? exportedValues.get(0).asText() : null;
                    if (v != null) entry.put("plainText", v);
                } else if ("NUMERIC".equalsIgnoreCase(inputType)) {
                    String v = exportedValues.size()>0 ? exportedValues.get(0).asText() : null;
                    if (v != null) entry.put("numeric", v);
                } else if ("BOOLEAN".equalsIgnoreCase(inputType)) {
                    String v = exportedValues.size()>0 ? exportedValues.get(0).asText() : null;
                    if (v != null) entry.put("boolean", Boolean.parseBoolean(v));
                } else {
                    String v = exportedValues.size()>0 ? exportedValues.get(0).asText() : null;
                    if (v != null) entry.put("plainText", v);
                }
            }
            out.add(entry);
        }
        return out;
    }

    /* ------------------------------ 1) EXPORT ------------------------------ */
    @GetMapping("/GetProductVariantJson")
    public String exportVariants() throws Exception {
        ArrayNode out = mapper().createArrayNode();
        String after = null;

        do {
            JsonNode r = call(Q_PRODUCTS_WITH_VARIANTS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("products");
            for (JsonNode pe : conn.path("edges")) {
                JsonNode prod = pe.path("node");
                String pslug = prod.path("slug").asText();

                JsonNode vlist = prod.path("variants");
                if (vlist.isArray()) {
                    for (JsonNode v : vlist) {
                        ObjectNode row = mapper().createObjectNode();
                        row.put("productSlug", pslug);
                        row.put("sku", v.path("sku").asText());
                        row.put("name", v.path("name").asText());
                        row.set("attributes", exportVariantAttributes(v.path("attributes")));

                        // channels
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

                        // stocks
                        ArrayNode sts = mapper().createArrayNode();
                        for (JsonNode st : v.path("stocks")) {
                            ObjectNode s = mapper().createObjectNode();
                            s.put("warehouse", st.path("warehouse").path("slug").asText());
                            s.put("quantity", st.path("quantity").asInt(0));
                            sts.add(s);
                        }
                        row.set("stocks", sts);

                        out.add(row);
                    }
                }
                
            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);

        File f = new File("/tmp/variants.json");
        mapper().writerWithDefaultPrettyPrinter().writeValue(f, out);
        return "Exported " + out.size() + " variants → " + f.getAbsolutePath();
    }

    /* ------------------------------ 2) DELETE ALL ------------------------------ */
    @GetMapping("/DeleteAllProdctVariant")
    public String deleteAllVariants() {
        int deleted = 0, errors = 0;
        String after = null;
        do {
            JsonNode r = call(Q_PRODUCTS_WITH_VARIANTS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("products");
            for (JsonNode pe : conn.path("edges")) {
                JsonNode prod = pe.path("node");
                JsonNode vlist = prod.path("variants");
                if (vlist.isArray()) {
                    for (JsonNode v : vlist) {
                        String vid = v.path("id").asText();
                        String sku = v.path("sku").asText();
                        JsonNode d = call(M_VARIANT_DELETE, vars("id", vid));
                        JsonNode errs = d.path("data").path("productVariantDelete").path("errors");
                        if (errs.isArray() && errs.size() > 0) {
                            System.out.println("⚠️ productVariantDelete error for sku=" + sku + " : " + errs);
                            errors++;
                        } else deleted++;
                    }
                }

            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);
        return "Deleted " + deleted + " variants. Errors: " + errors;
    }

    /* ------------------------------ 3) IMPORT ------------------------------ */
    @GetMapping("/PutProductVariantJson")
    public String importVariants() throws Exception {
        File f = new File("/tmp/variants.json");
        if (!f.exists()) return "File not found: " + f.getAbsolutePath();
        ArrayNode rows = (ArrayNode) mapper().readTree(Files.readAllBytes(f.toPath()));

        Map<String,String> channelSlugToId = loadChannelSlugToId();
        Map<String,String> warehouseSlugToId = loadWarehouseSlugToId();
        Map<String,AttrInfo> varAttrMap = loadVariantAttributes();

        int created=0, updated=0, failed=0;

        for (JsonNode n : rows) {
            String productSlug = n.path("productSlug").asText();
            String sku = n.path("sku").asText();
            String name = n.path("name").asText(null);

            String productId = productIdBySlug(productSlug);
            if (productId == null) {
                System.out.println("❌ product not found for variant import, slug=" + productSlug + " (sku " + sku + ")");
                failed++; continue;
            }

            ArrayNode attrPayload = buildVariantAttributesPayload((ArrayNode) n.path("attributes"), varAttrMap);

            String variantId = variantIdBySku(sku);
            if (variantId == null) {
                ObjectNode input = mapper().createObjectNode();
                input.put("product", productId);
                if (sku != null) input.put("sku", sku);
                if (name != null) input.put("name", name);
                input.set("attributes", attrPayload);

                JsonNode r = call(M_VARIANT_CREATE, vars("input", input));
                JsonNode errs = r.path("data").path("productVariantCreate").path("errors");
                if (errs.isArray() && errs.size() > 0) {
                    System.out.println("❌ productVariantCreate error for sku=" + sku + " : " + errs);
                    failed++; continue;
                }
                variantId = r.path("data").path("productVariantCreate").path("productVariant").path("id").asText();
                created++;
            } else {
                ObjectNode input = mapper().createObjectNode();
                if (sku != null) input.put("sku", sku);
                if (name != null) input.put("name", name);
                input.set("attributes", attrPayload);

                JsonNode r = call(M_VARIANT_UPDATE, vars("id", variantId, "input", input));
                JsonNode errs = r.path("data").path("productVariantUpdate").path("errors");
                if (errs.isArray() && errs.size() > 0) {
                    System.out.println("❌ productVariantUpdate error for sku=" + sku + " : " + errs);
                    failed++; continue;
                }
                updated++;
            }

            // (A) Channel listings (prices)
            JsonNode chs = n.path("channels");
            if (chs.isArray() && chs.size()>0) {
                ArrayNode adds = mapper().createArrayNode();
                for (JsonNode ch : chs) {
                    String slug = ch.path("slug").asText();
                    String chId = channelSlugToId.get(slug);
                    if (chId == null) { System.out.println("⚠️ unknown channel slug "+slug+" (skip)"); continue; }
                    ObjectNode row = mapper().createObjectNode();
                    row.put("channelId", chId);
                    if (ch.hasNonNull("price")) row.put("price", ch.path("price").asText()); // string decimal
                    if (ch.hasNonNull("costPrice")) row.put("costPrice", ch.path("costPrice").asText());
                    adds.add(row);
                }
                if (adds.size()>0) {
                    JsonNode r2 = call(M_VARIANT_CHANNEL_LISTING_UPDATE, vars("id", variantId, "input", adds));
                    JsonNode e2 = r2.path("data").path("productVariantChannelListingUpdate").path("errors");
                    if (e2.isArray() && e2.size()>0) System.out.println("⚠️ variant channel update errors sku="+sku+": "+e2);
                }
            }

            // (B) Stocks
            JsonNode sts = n.path("stocks");
            if (sts.isArray() && sts.size()>0) {
                ArrayNode stocks = mapper().createArrayNode();
                for (JsonNode s : sts) {
                    String wslug = s.path("warehouse").asText();
                    Integer qty = s.path("quantity").asInt(0);
                    String wid = warehouseSlugToId.get(wslug);
                    if (wid == null) { System.out.println("⚠️ unknown warehouse slug "+wslug+" (skip)"); continue; }
                    ObjectNode si = mapper().createObjectNode();
                    si.put("warehouse", wid);
                    si.put("quantity", qty);
                    stocks.add(si);
                }
                if (stocks.size()>0) {
                    JsonNode r3 = call(M_VARIANT_STOCKS_SET, vars("variantId", variantId, "stocks", stocks));
                    JsonNode e3 = r3.path("data").path("productVariantStocksUpdate").path("errors");
                    if (e3.isArray() && e3.size()>0) System.out.println("⚠️ stocks update errors sku="+sku+": "+e3);
                }
            }
        }

        return String.format("Variant import done. Created: %d, Updated: %d, Failed: %d", created, updated, failed);
    }

    /* ------------------------------ 4) UPDATE ONE ------------------------------ */
    /**
     * Update one variant by SKU.
     * Params (all optional except sku):
     *  - sku (required)
     *  - name=<new name>
     *  - setPriceChannel=<channelSlug>&price=123.45[&costPrice=99.00]
     *  - setStockWarehouse=<whSlug>&quantity=10
     *  - setAttribute=<attrSlug>&plainText=... OR &dropdown=valueSlug OR &multiselect=a|b
     */
    @GetMapping("/UpdateProductVariant")
    public String updateVariant(
            @RequestParam("sku") String sku,
            @RequestParam(value="name", required=false) String name,
            @RequestParam(value="setPriceChannel", required=false) String setPriceChannel,
            @RequestParam(value="price", required=false) String price,
            @RequestParam(value="costPrice", required=false) String costPrice,
            @RequestParam(value="setStockWarehouse", required=false) String setStockWarehouse,
            @RequestParam(value="quantity", required=false) Integer quantity,
            @RequestParam(value="setAttribute", required=false) String attrSlug,
            @RequestParam(value="plainText", required=false) String plainText,
            @RequestParam(value="dropdown", required=false) String dropdownSlug,
            @RequestParam(value="multiselect", required=false) String multiselectSlugs
    ){
        String variantId = variantIdBySku(sku);
        if (variantId == null) return "Variant not found by sku: " + sku;

        // (A) simple name/attribute update via productVariantUpdate
        boolean didUpdate = false;
        ObjectNode vin = mapper().createObjectNode();

        if (name != null) { vin.put("name", name); didUpdate = true; }

        if (attrSlug != null) {
            Map<String,AttrInfo> varAttrMap = loadVariantAttributes();
            AttrInfo ai = varAttrMap.get(attrSlug);
            if (ai == null) return "Variant attribute not found: " + attrSlug;

            ArrayNode attrs = mapper().createArrayNode();
            ObjectNode one = mapper().createObjectNode();
            one.put("id", ai.id);

            if (isChoiceBased(ai.inputType)) {
                if (dropdownSlug != null && !dropdownSlug.isEmpty()) {
                    String vId = ai.valueSlugToId.get(dropdownSlug);
                    if (vId == null) {
                        JsonNode mk = call(M_ATTRIBUTE_VALUE_CREATE, vars("attr", ai.id, "name", dropdownSlug, "slug", dropdownSlug));
                        JsonNode err = mk.path("data").path("attributeValueCreate").path("errors");
                        if (err.isArray() && err.size()>0) return "Failed to create value: " + err;
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
                            if (err.isArray() && err.size()>0) return "Failed to create value '"+s+"': " + err;
                            vId = mk.path("data").path("attributeValueCreate").path("value").path("id").asText();
                        }
                        ids.add(vId);
                    }
                    one.set("multiselect", ids);
                } else return "Choice attribute requires dropdown or multiselect";
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
            JsonNode r = call(M_VARIANT_UPDATE, vars("id", variantId, "input", vin));
            JsonNode errs = r.path("data").path("productVariantUpdate").path("errors");
            if (errs.isArray() && errs.size()>0) return "variantUpdate failed: " + errs;
        }

        // (B) price on a channel
        if (setPriceChannel != null) {
            Map<String,String> chMap = loadChannelSlugToId();
            String chId = chMap.get(setPriceChannel);
            if (chId == null) return "Unknown channel slug: " + setPriceChannel;

            ArrayNode adds = mapper().createArrayNode();
            ObjectNode row = mapper().createObjectNode();
            row.put("channelId", chId);
            if (price != null) row.put("price", price);
            if (costPrice != null) row.put("costPrice", costPrice);
            adds.add(row);

            JsonNode r2 = call(M_VARIANT_CHANNEL_LISTING_UPDATE, vars("id", variantId, "input", adds));
            JsonNode e2 = r2.path("data").path("productVariantChannelListingUpdate").path("errors");
            if (e2.isArray() && e2.size()>0) return "variant channel update failed: " + e2;
        }

        // (C) stock in a warehouse
        if (setStockWarehouse != null && quantity != null) {
            Map<String,String> whMap = loadWarehouseSlugToId();
            String wid = whMap.get(setStockWarehouse);
            if (wid == null) return "Unknown warehouse slug: " + setStockWarehouse;

            ArrayNode stocks = mapper().createArrayNode();
            ObjectNode si = mapper().createObjectNode();
            si.put("warehouse", wid);
            si.put("quantity", quantity);
            stocks.add(si);

            JsonNode r3 = call(M_VARIANT_STOCKS_SET, vars("variantId", variantId, "stocks", stocks));
            JsonNode e3 = r3.path("data").path("productVariantStocksUpdate").path("errors");
            if (e3.isArray() && e3.size()>0) return "variant stocks update failed: " + e3;
        }

        return "OK (sku=" + sku + ")";
    }
}

