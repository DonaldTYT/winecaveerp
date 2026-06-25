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
 * ProductSyncController (Saleor 3.21.x)
 *
 * Routes (all GET for easy testing):
 *  - /admin/GetProductJson        : export products to /tmp/products.json
 *  - /admin/DeleteAllProduct      : delete all products
 *  - /admin/PutProductJson        : import (create/update) products from /tmp/products.json
 *  - /admin/UpdateProductAttribute: update a single product's attribute by slugs
 *
 * JSON export shape (example):
 * [
 *   {
 *     "name":"Cheval des Andes 2018",
 *     "slug":"cheval-des-andes-2018",
 *     "productType":"wine",
 *     "category":"red-wine",
 *     "description":"{\"time\":0,\"blocks\":[...]}",   // EditorJS string or null
 *     "attributes":[
 *       {"attribute":"maturity","inputType":"PLAIN_TEXT","values":["2025-26"]},
 *       {"attribute":"owner","inputType":"DROPDOWN","values":["consignee-a"]} // value slugs
 *     ]
 *   }
 * ]
 *
 * Notes:
 * - Attributes here are product-level (PRODUCT_TYPE). Variant attributes are out of scope for this controller.
 * - For DROPDOWN/MULTISELECT we export/import **value slugs**; controller resolves to value IDs (creating them if missing).
 * - For RICH_TEXT, description and attribute values should be **EditorJS JSON strings**.
 */
@RestController
@RequestMapping("/admin")
public class ProductSyncController {

    @Autowired
    private SaleorConfig config;

    private final RestTemplate rest = new RestTemplate();

    private ObjectMapper mapper() { return config.getMapper(); }

    private String editorJs(String text) {
        ObjectNode root = mapper().createObjectNode();
        root.put("time", 0);
        root.put("version", "2.26.5");
        ArrayNode blocks = mapper().createArrayNode();

        ObjectNode para = mapper().createObjectNode();
        para.put("type", "paragraph");
        ObjectNode data = mapper().createObjectNode();
        data.put("text", text == null ? "" : text);
        para.set("data", data);
        blocks.add(para);

        root.set("blocks", blocks);
        try { return mapper().writeValueAsString(root); }
        catch (Exception e) { return "{\"time\":0,\"blocks\":[]}"; }
    }   

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

    /* ------------------------ GraphQL (Saleor 3.21.x) ------------------------ */

    private static final String Q_PRODUCT_BY_SLUG = ""
        + "query($slug:String!){\n"
        + "  products(first:1, filter:{ slugs: [$slug] }){\n"
        + "    edges{ node{ id slug name } }\n"
        + "  }\n"
        + "}";

    private static final String Q_PRODUCT_TYPE_BY_SLUG = ""
        + "query($slug:String!){\n"
        + "  productTypes(first:1, filter:{ slugs: [$slug] }){\n"
        + "    edges{ node{ id slug } }\n"
        + "  }\n"
        + "}";

    private static final String Q_CATEGORY_BY_SLUG = ""
        + "query($slug:String!){\n"
        + "  categories(first:1, filter:{ slugs: [$slug] }){\n"
        + "    edges{ node{ id slug } }\n"
        + "  }\n"
        + "}";

    private static final String Q_ATTRS_PAGE = ""
        + "query($after:String){\n"
        + "  attributes(first:100, after:$after){\n"
        + "    pageInfo{ hasNextPage endCursor }\n"
        + "    edges{ node{ id slug type inputType\n"
        + "      choices(first:100){ edges{ node{ id slug name } } }\n"
        + "    } }\n"
        + "  }\n"
        + "}";

    private static final String M_ATTRIBUTE_VALUE_CREATE = ""
        + "mutation($attr:ID!, $name:String!, $slug:String!){\n"
        + "  attributeValueCreate(attribute:$attr, input:{ name:$name, slug:$slug }){\n"
        + "    value{ id slug }\n"
        + "    errors{ field message code }\n"
        + "  }\n"
        + "}";

    private static final String M_PRODUCT_CREATE = ""
        + "mutation($input: ProductCreateInput!){\n"
        + "  productCreate(input:$input){ product{ id slug } errors{ field message code } }\n"
        + "}";

    private static final String M_PRODUCT_UPDATE = ""
        + "mutation($id:ID!, $input: ProductInput!){\n"
        + "  productUpdate(id:$id, input:$input){ product{ id slug } errors{ field message code } }\n"
        + "}";

    private static final String M_PRODUCT_DELETE = ""
        + "mutation($id:ID!){ productDelete(id:$id){ errors{ field message code } } }";
    
    
 // List channels to map slug -> id
    private static final String Q_CHANNELS =
        "query{\n" +
        "  channels{ id slug name }\n" +
        "}";

    // For products page, include channelListings (REPLACE your Q_PRODUCTS_PAGE with this)
    private static final String Q_PRODUCTS_PAGE = ""
        + "query($after:String){\n"
        + "  products(first:100, after:$after){\n"
        + "    pageInfo{ hasNextPage endCursor }\n"
        + "    edges{ node{\n"
        + "      id name slug description productType{ slug } category{ slug }\n"
        + "      attributes{ attribute{ slug inputType } values{ name slug } }\n"
        + "      channelListings{\n"
        + "        channel{ id slug }\n"
        + "        isPublished\n"
        + "        visibleInListings\n"
        + "        publicationDate\n"
        + "      }\n"
        + "    } }\n"
        + "  }\n"
        + "}";

    // Needed to update a product's channel listings
    private static final String M_PRODUCT_CHANNEL_LISTING_UPDATE = ""
        + "mutation($id:ID!, $input: ProductChannelListingUpdateInput!){\n"
        + "  productChannelListingUpdate(id:$id, input:$input){\n"
        + "    errors{ field message code }\n"
        + "    product{ id }\n"
        + "  }\n"
        + "}";

    

    /* ------------------------------- Lookups -------------------------------- */
    private String productIdBySlug(String slug) {
        JsonNode r = call(Q_PRODUCT_BY_SLUG, vars("slug", slug));
        JsonNode edges = r.path("data").path("products").path("edges");
        return edges.isArray() && edges.size() > 0 ? edges.get(0).path("node").path("id").asText() : null;
    }

    private String productTypeIdBySlug(String slug) {
        JsonNode r = call(Q_PRODUCT_TYPE_BY_SLUG, vars("slug", slug));
        JsonNode edges = r.path("data").path("productTypes").path("edges");
        return edges.isArray() && edges.size() > 0 ? edges.get(0).path("node").path("id").asText() : null;
    }

    private String categoryIdBySlug(String slug) {
        JsonNode r = call(Q_CATEGORY_BY_SLUG, vars("slug", slug));
        JsonNode edges = r.path("data").path("categories").path("edges");
        return edges.isArray() && edges.size() > 0 ? edges.get(0).path("node").path("id").asText() : null;
    }

    private Map<String, AttrInfo> loadProductAttributes() {
        Map<String, AttrInfo> map = new HashMap<>();
        String after = null;
        do {
            JsonNode r = call(Q_ATTRS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("attributes");
            for (JsonNode e : conn.path("edges")) {
                JsonNode n = e.path("node");
                if (!"PRODUCT_TYPE".equals(n.path("type").asText())) continue;
                AttrInfo ai = new AttrInfo();
                ai.id = n.path("id").asText();
                ai.inputType = n.path("inputType").asText();
                ai.valueSlugToId = new HashMap<>();
                JsonNode choices = n.path("choices").path("edges");
                if (choices.isArray()) {
                    for (JsonNode ce : choices) {
                        String vs = ce.path("node").path("slug").asText();
                        String vid = ce.path("node").path("id").asText();
                        if (!vs.isBlank() && !vid.isBlank()) ai.valueSlugToId.put(vs, vid);
                    }
                }
                map.put(n.path("slug").asText(), ai);
            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);
        return map;
    }
    
    private Map<String, String> loadChannelSlugToId() {
        Map<String, String> out = new HashMap<>();
        JsonNode resp = call(Q_CHANNELS, null);
        for (JsonNode ch : resp.path("data").path("channels")) {
            String slug = ch.path("slug").asText();
            String id = ch.path("id").asText();
            if (!slug.isEmpty() && !id.isEmpty()) out.put(slug, id);
        }
        return out;
    }


    private static class AttrInfo {
        String id;
        String inputType;
        Map<String, String> valueSlugToId; // for choice-based types
    }

    /* --------------------------- 1) EXPORT to JSON --------------------------- */
    @GetMapping("/GetProductJson")
    public String exportProducts() throws Exception {
        ArrayNode out = mapper().createArrayNode();
        String after = null;

        do {
            JsonNode r = call(Q_PRODUCTS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("products");
            for (JsonNode e : conn.path("edges")) {
                JsonNode p = e.path("node");

                ObjectNode row = mapper().createObjectNode();
                row.put("name", p.path("name").asText());
                row.put("slug", p.path("slug").asText());
                row.put("productType", p.path("productType").path("slug").asText(null));
                row.put("category", p.path("category").isNull() ? null : p.path("category").path("slug").asText(null));
                if (!p.path("description").isNull()) {
                    row.put("description", p.path("description").asText());
                } else {
                    row.putNull("description");
                }

                // Export product-level attributes as { attribute, inputType, values:[slugs or raw strings] }
                ArrayNode attrs = mapper().createArrayNode();
                for (JsonNode av : p.path("attributes")) {
                    String aSlug = av.path("attribute").path("slug").asText();
                    String inputType = av.path("attribute").path("inputType").asText();
                    ArrayNode values = mapper().createArrayNode();

                    if (isChoiceBased(inputType)) {
                        for (JsonNode v : av.path("values")) {
                            values.add(v.path("slug").asText()); // export value slugs
                        }
                    } else {
                        // for non-choice, Saleor returns .values[].name as display; for PLAIN_TEXT/NUMERIC/BOOLEAN/RICH_TEXT we export name as raw
                        for (JsonNode v : av.path("values")) {
                            values.add(v.path("name").asText());
                        }
                    }

                    ObjectNode one = mapper().createObjectNode();
                    one.put("attribute", aSlug);
                    one.put("inputType", inputType);
                    one.set("values", values);
                    attrs.add(one);
                }
                row.set("attributes", attrs);
                
             // Channel listings export
                ArrayNode channels = mapper().createArrayNode();
                for (JsonNode cl : p.path("channelListings")) {
                    ObjectNode ch = mapper().createObjectNode();
                    ch.put("slug", cl.path("channel").path("slug").asText());
                    ch.put("isPublished", cl.path("isPublished").asBoolean(false));
                    ch.put("visibleInListings", cl.path("visibleInListings").asBoolean(false));
                    if (!cl.path("publicationDate").isNull()) {
                        ch.put("publicationDate", cl.path("publicationDate").asText()); // YYYY-MM-DD
                    }
                    channels.add(ch);
                }
                row.set("channels", channels);


                out.add(row);
            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);

        File f = new File("/tmp/products.json");
        mapper().writerWithDefaultPrettyPrinter().writeValue(f, out);
        return "Exported " + out.size() + " products → " + f.getAbsolutePath();
    }

    /* --------------------------- 2) DELETE ALL ------------------------------- */
    @GetMapping("/DeleteAllProduct")
    public String deleteAllProducts() {
        int deleted = 0, errors = 0;
        String after = null;
        while (true) {
            JsonNode r = call(Q_PRODUCTS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("products");
            List<JsonNode> nodes = new ArrayList<>();
            for (JsonNode e : conn.path("edges")) nodes.add(e.path("node"));

            if (nodes.isEmpty()) break;

            for (JsonNode p : nodes) {
                String id = p.path("id").asText();
                String slug = p.path("slug").asText();
                JsonNode d = call(M_PRODUCT_DELETE, vars("id", id));
                JsonNode errs = d.path("data").path("productDelete").path("errors");
                if (errs.isArray() && errs.size() > 0) {
                    System.out.println("⚠️ productDelete error for slug=" + slug + " : " + errs);
                    errors++;
                } else {
                    deleted++;
                }
            }

            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            if (!hasNext) break;
            after = conn.path("pageInfo").path("endCursor").asText(null);
        }
        return String.format("Deleted %d products. Errors: %d", deleted, errors);
    }

    /* --------------------------- Attributes payload -------------------------- */
    private ArrayNode buildAttributesPayload(ArrayNode exportAttrs, Map<String, AttrInfo> attrMap) {
        ArrayNode out = mapper().createArrayNode();
        if (exportAttrs == null) return out;

        for (JsonNode a : exportAttrs) {
            String aSlug = a.path("attribute").asText();
            String inputType = a.path("inputType").asText();
            
            JsonNode valuesNode = a.path("values");
            ArrayNode exportedValues = valuesNode.isArray() ? (ArrayNode) valuesNode : mapper().createArrayNode();


            AttrInfo ai = attrMap.get(aSlug);
            if (ai == null) {
                System.out.println("⚠️ Missing attribute in server: " + aSlug);
                continue;
            }

            ObjectNode entry = mapper().createObjectNode();
            entry.put("id", ai.id);

            if (isChoiceBased(inputType)) {
                if ("DROPDOWN".equalsIgnoreCase(inputType)) {
                    // single selection expected; take first if present
                    String vSlug = exportedValues.size() > 0 ? exportedValues.get(0).asText() : null;
                    if (vSlug != null && !vSlug.isBlank()) {
                        String vId = ai.valueSlugToId.get(vSlug);
                        if (vId == null) {
                            // create missing value
                            JsonNode mk = call(M_ATTRIBUTE_VALUE_CREATE,
                                vars("attr", ai.id, "input",
                                    mapper().createObjectNode().put("name", vSlug).put("slug", vSlug)));
                            JsonNode err = mk.path("data").path("attributeValueCreate").path("errors");
                            if (err.isArray() && err.size() > 0) {
                                System.out.println("⚠️ attributeValueCreate error for " + aSlug + "/" + vSlug + " : " + err);
                            } else {
                                vId = mk.path("data").path("attributeValueCreate").path("value").path("id").asText();
                                if (vId != null) ai.valueSlugToId.put(vSlug, vId);
                            }
                        }
                        if (vId != null) entry.put("dropdown", vId);
                    }
                } else {
                    // MULTISELECT/SWATCH -> array of IDs
                    ArrayNode ids = mapper().createArrayNode();
                    for (JsonNode v : exportedValues) {
                        String vSlug = v.asText();
                        if (vSlug == null || vSlug.isBlank()) continue;
                        String vId = ai.valueSlugToId.get(vSlug);
                        if (vId == null) {
                            JsonNode mk = call(M_ATTRIBUTE_VALUE_CREATE,
                                vars("attr", ai.id, "input",
                                    mapper().createObjectNode().put("name", vSlug).put("slug", vSlug)));
                            JsonNode err = mk.path("data").path("attributeValueCreate").path("errors");
                            if (err.isArray() && err.size() > 0) {
                                System.out.println("⚠️ attributeValueCreate error for " + aSlug + "/" + vSlug + " : " + err);
                                continue;
                            }
                            vId = mk.path("data").path("attributeValueCreate").path("value").path("id").asText();
                            if (vId != null) ai.valueSlugToId.put(vSlug, vId);
                        }
                        if (vId != null) ids.add(vId);
                    }
                    entry.set("multiselect", ids);
                }
            } else {
                // non-choice: copy raw strings
                // For BOOLEAN, accept "true"/"false" strings; for NUMERIC, pass as string as Saleor accepts PositiveDecimal in string form.
                if ("RICH_TEXT".equalsIgnoreCase(inputType)) {
                    String rich = (exportedValues != null && exportedValues.size() > 0)
                            ? editorJs(exportedValues.get(0).asText()) // convert to EditorJS JSON string
                            : editorJs("");
                    entry.put("richText", rich);
                } else if ("PLAIN_TEXT".equalsIgnoreCase(inputType)) {
                    String v = exportedValues.size() > 0 ? exportedValues.get(0).asText() : null;
                    if (v != null) entry.put("plainText", v);
                } else if ("NUMERIC".equalsIgnoreCase(inputType)) {
                    String v = exportedValues.size() > 0 ? exportedValues.get(0).asText() : null;
                    if (v != null) entry.put("numeric", v);
                } else if ("BOOLEAN".equalsIgnoreCase(inputType)) {
                    String v = exportedValues.size() > 0 ? exportedValues.get(0).asText() : null;
                    if (v != null) entry.put("boolean", Boolean.parseBoolean(v));
                } else {
                    // DATE, DATE_TIME, FILE, REFERENCE are uncommon at product level—add as plain text fallback
                    String v = exportedValues.size() > 0 ? exportedValues.get(0).asText() : null;
                    if (v != null) entry.put("plainText", v);
                }
            }

            out.add(entry);
        }
        return out;
    }

    /* --------------------------- 3) IMPORT from JSON ------------------------- */
    @GetMapping("/PutProductJson")
    public String importProducts() throws Exception {
    	
	
        File f = new File("/tmp/products.json");
        if (!f.exists()) return "File not found: " + f.getAbsolutePath();

        ArrayNode products = (ArrayNode) mapper().readTree(Files.readAllBytes(f.toPath()));

        // warm attribute cache
        Map<String, AttrInfo> attrMap = loadProductAttributes();
    	Map<String, String> channelSlugToId = loadChannelSlugToId();

        int created = 0, updated = 0, failed = 0;

        for (JsonNode p : products) {
            String name = p.path("name").asText();
            String slug = p.path("slug").asText();
            String ptSlug = p.path("productType").asText(null);
            String catSlug = p.path("category").asText(null);
            String description = p.path("description").isNull() ? null : p.path("description").asText(null);

            String ptId = ptSlug == null ? null : productTypeIdBySlug(ptSlug);
            if (ptId == null) {
                System.out.println("❌ Missing productType: " + ptSlug + " (skip " + slug + ")");
                failed++;
                continue;
            }

            String catId = null;
            if (catSlug != null && !catSlug.isBlank()) {
                catId = categoryIdBySlug(catSlug);
                if (catId == null) {
                    System.out.println("⚠️ category slug not found: " + catSlug + " (product " + slug + " will be created without category)");
                }
            }

            ArrayNode attrsPayload = buildAttributesPayload((ArrayNode) p.path("attributes"), attrMap);

            String productId = productIdBySlug(slug);
            if (productId == null) {
                ObjectNode input = mapper().createObjectNode();
                input.put("name", name);
                input.put("slug", slug);
                input.put("productType", ptId);
                if (catId != null) input.put("category", catId);
                if (description != null) input.put("description", description);
                input.set("attributes", attrsPayload);
                
          


                JsonNode resp = call(M_PRODUCT_CREATE, vars("input", input));
                JsonNode errs = resp.path("data").path("productCreate").path("errors");
                if (errs.isArray() && errs.size() > 0) {
                    System.out.println("❌ productCreate errors for " + slug + ": " + errs);
                    failed++;
                    continue;
                }
                productId = resp.path("data").path("productCreate").path("product").path("id").asText();
                
                // Apply channel listings (idempotent via updateChannels)
                JsonNode chArr = p.path("channels");
                if (chArr.isArray() && chArr.size() > 0) {
                    ArrayNode updateChannels = mapper().createArrayNode();
                    for (JsonNode ch : chArr) {
                        String chSlug = ch.path("slug").asText();
                        String chId = channelSlugToId.get(chSlug);
                        if (chId == null) {
                            System.out.println("⚠️ Unknown channel slug: " + chSlug + " (skip)");
                            continue;
                        }
                        ObjectNode row = mapper().createObjectNode();
                        row.put("channelId", chId);
                        row.put("isPublished", ch.path("isPublished").asBoolean(false));
                        row.put("visibleInListings", ch.path("visibleInListings").asBoolean(false));
                        if (ch.hasNonNull("publicationDate")) {
                            row.put("publicationDate", ch.path("publicationDate").asText()); // YYYY-MM-DD
                        }
                        updateChannels.add(row);
                    }
                    if (updateChannels.size() > 0) {
                        ObjectNode inputCh = mapper().createObjectNode();
                        inputCh.set("updateChannels", updateChannels);
                        JsonNode chResp = call(M_PRODUCT_CHANNEL_LISTING_UPDATE, vars("id", productId, "input", inputCh));
                        JsonNode chErr = chResp.path("data").path("productChannelListingUpdate").path("errors");
                        if (chErr.isArray() && chErr.size() > 0) {
                            System.out.println("⚠️ productChannelListingUpdate errors for " + slug + ": " + chErr);
                        }
                    }
                }
                
                created++;
            } else {
                ObjectNode input = mapper().createObjectNode();
                input.put("name", name);
                if (catId != null) input.put("category", catId);
                if (description != null) input.put("description", description);
                input.set("attributes", attrsPayload);
                
  
 

                JsonNode resp = call(M_PRODUCT_UPDATE, vars("id", productId, "input", input));
                JsonNode errs = resp.path("data").path("productUpdate").path("errors");
                if (errs.isArray() && errs.size() > 0) {
                    System.out.println("❌ productUpdate errors for " + slug + ": " + errs);
                    failed++;
                    continue;
                }
                
                // Apply channel listings (idempotent via updateChannels)
                JsonNode chArr = p.path("channels");
                if (chArr.isArray() && chArr.size() > 0) {
                    ArrayNode updateChannels = mapper().createArrayNode();
                    for (JsonNode ch : chArr) {
                        String chSlug = ch.path("slug").asText();
                        String chId = channelSlugToId.get(chSlug);
                        if (chId == null) {
                            System.out.println("⚠️ Unknown channel slug: " + chSlug + " (skip)");
                            continue;
                        }
                        ObjectNode row = mapper().createObjectNode();
                        row.put("channelId", chId);
                        row.put("isPublished", ch.path("isPublished").asBoolean(false));
                        row.put("visibleInListings", ch.path("visibleInListings").asBoolean(false));
                        if (ch.hasNonNull("publicationDate")) {
                            row.put("publicationDate", ch.path("publicationDate").asText()); // YYYY-MM-DD
                        }
                        updateChannels.add(row);
                    }
                    if (updateChannels.size() > 0) {
                        ObjectNode inputCh = mapper().createObjectNode();
                        inputCh.set("updateChannels", updateChannels);
                        JsonNode chResp = call(M_PRODUCT_CHANNEL_LISTING_UPDATE, vars("id", productId, "input", inputCh));
                        JsonNode chErr = chResp.path("data").path("productChannelListingUpdate").path("errors");
                        if (chErr.isArray() && chErr.size() > 0) {
                            System.out.println("⚠️ productChannelListingUpdate errors for " + slug + ": " + chErr);
                        }
                    }
                }
                
                updated++;
            }
        }

        return String.format("Done. Created: %d, Updated: %d, Failed: %d", created, updated, failed);
    }

    /* --------------------- 4) Update single product attribute ---------------- */
    /**
     * Update a product's attribute value by slugs.
     *
     * Example usages:
     *  - PLAIN_TEXT:
     *    /admin/UpdateProductAttribute?productSlug=cheval-des-andes-2018&attributeSlug=maturity&plainText=2025-26
     *
     *  - RICH_TEXT (URL-encode your EditorJS JSON string):
     *    /admin/UpdateProductAttribute?productSlug=cheval-des-andes-2018&attributeSlug=long-description&richText=%7B%22time%22%3A...%7D
     *
     *  - DROPDOWN (single value slug):
     *    /admin/UpdateProductAttribute?productSlug=cheval-des-andes-2018&attributeSlug=owner&dropdown=consignee-a
     *    (Auto-creates the value if missing)
     *
     *  - MULTISELECT (pipe-separated slugs):
     *    /admin/UpdateProductAttribute?productSlug=cheval-des-andes-2018&attributeSlug=tags&multiselect=tag-a|tag-b
     */
    @GetMapping("/UpdateProductAttribute")
    public String updateProductAttribute(
        @RequestParam("productSlug") String productSlug,
        @RequestParam("attributeSlug") String attributeSlug,
        @RequestParam(value = "plainText", required = false) String plainText,
        @RequestParam(value = "richText", required = false) String richText,
        @RequestParam(value = "numeric", required = false) String numeric,
        @RequestParam(value = "bool", required = false) Boolean bool,
        @RequestParam(value = "dropdown", required = false) String dropdownSlug,
        @RequestParam(value = "multiselect", required = false) String multiselectSlugs // pipe-separated
    ) {
        String productId = productIdBySlug(productSlug);
        if (productId == null) return "Product not found: " + productSlug;

        Map<String, AttrInfo> attrMap = loadProductAttributes();
        AttrInfo ai = attrMap.get(attributeSlug);
        if (ai == null) return "Attribute not found (PRODUCT_TYPE): " + attributeSlug;

        ObjectNode input = mapper().createObjectNode();
        ArrayNode attrs = mapper().createArrayNode();
        ObjectNode one = mapper().createObjectNode();
        one.put("id", ai.id);

        String it = ai.inputType;

        try {
            if (isChoiceBased(it)) {
                if (dropdownSlug != null && !dropdownSlug.isBlank()) {
                    String vId = ai.valueSlugToId.get(dropdownSlug);
                    if (vId == null) {
                        // auto-create missing value
                        JsonNode mk = call(M_ATTRIBUTE_VALUE_CREATE,
                            vars("attr", ai.id, "input",
                                mapper().createObjectNode().put("name", dropdownSlug).put("slug", dropdownSlug)));
                        JsonNode err = mk.path("data").path("attributeValueCreate").path("errors");
                        if (err.isArray() && err.size() > 0) return "Failed to create dropdown value: " + err;
                        vId = mk.path("data").path("attributeValueCreate").path("value").path("id").asText();
                    }
                    one.put("dropdown", vId);
                } else if (multiselectSlugs != null && !multiselectSlugs.isBlank()) {
                    ArrayNode ids = mapper().createArrayNode();
                    for (String s : multiselectSlugs.split("\\|")) {
                        s = s.trim();
                        if (s.isEmpty()) continue;
                        String vId = ai.valueSlugToId.get(s);
                        if (vId == null) {
                            JsonNode mk = call(M_ATTRIBUTE_VALUE_CREATE,
                                vars("attr", ai.id, "input",
                                    mapper().createObjectNode().put("name", s).put("slug", s)));
                            JsonNode err = mk.path("data").path("attributeValueCreate").path("errors");
                            if (err.isArray() && err.size() > 0) return "Failed to create multiselect value '" + s + "': " + err;
                            vId = mk.path("data").path("attributeValueCreate").path("value").path("id").asText();
                        }
                        ids.add(vId);
                    }
                    one.set("multiselect", ids);
                } else {
                    return "Choice-based attribute requires 'dropdown' or 'multiselect' parameter.";
                }
            } else {
                if ("RICH_TEXT".equalsIgnoreCase(it)) {
                    if (richText == null) return "Missing 'richText' value for RICH_TEXT attribute.";
                    one.put("richText", richText);
                    String rich = (richText != null && richText.length() > 0)
                        ? editorJs(richText) // convert to EditorJS JSON string
                        : editorJs("");
                    one.put("richText", rich);
                } else if ("PLAIN_TEXT".equalsIgnoreCase(it)) {
                    if (plainText == null) return "Missing 'plainText' value for PLAIN_TEXT attribute.";
                    one.put("plainText", plainText);
                } else if ("NUMERIC".equalsIgnoreCase(it)) {
                    if (numeric == null) return "Missing 'numeric' value for NUMERIC attribute.";
                    one.put("numeric", numeric);
                } else if ("BOOLEAN".equalsIgnoreCase(it)) {
                    if (bool == null) return "Missing 'bool' value for BOOLEAN attribute.";
                    one.put("boolean", bool);
                } else {
                    // DATE, DATE_TIME, FILE, REFERENCE — keep simplest path using plainText
                    if (plainText == null) return "Provide 'plainText' for attribute of type " + it;
                    one.put("plainText", plainText);
                }
            }
        } catch (Exception ex) {
            return "Failed to build attribute payload: " + ex.getMessage();
        }

        attrs.add(one);
        input.set("attributes", attrs);

        JsonNode resp = call(M_PRODUCT_UPDATE, vars("id", productId, "input", input));
        JsonNode errs = resp.path("data").path("productUpdate").path("errors");
        if (errs.isArray() && errs.size() > 0) return "Update failed: " + errs.toString();

        return "Updated product attribute: product=" + productSlug + ", attribute=" + attributeSlug;
    }
}

