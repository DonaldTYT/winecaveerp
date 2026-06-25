package com.kikyosoft.controller;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.kikyosoft.config.SaleorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/oadmin")
public class PutProductJsonController {

    @Autowired
    private SaleorConfig config;

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /* -----------------------------------------------------------
     * GraphQL helpers
     * --------------------------------------------------------- */
    private JsonNode call(String query, Map<String, Object> variables) {
        ObjectNode body = mapper.createObjectNode();
        body.put("query", query);
        body.set("variables", variables == null ? mapper.createObjectNode() : mapper.valueToTree(variables));

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("Authorization", config.getAuthHeader());

        ResponseEntity<JsonNode> resp = rest.exchange(
                config.getApiUrl(), HttpMethod.POST, new HttpEntity<>(body, h), JsonNode.class);
        return resp.getBody();
    }

 // --- Category lookup (by slug) ---------------------------------------------
    private final Map<String,String> categorySlugToId = new HashMap<>();

    private String categoryIdBySlug(String slug){
        if (slug == null || slug.isBlank()) return null;
        return categorySlugToId.computeIfAbsent(slug, s -> {
            // Many 3.21 builds support categories(filter:{ slugs: [...] })
            final String Q =
                "query($slug:String!){ " +
                "  categories(first:1, filter:{ slugs: [$slug] }){ edges{ node{ id slug name } } }" +
                "}";
            JsonNode resp = call(Q, vars("slug", s));
            JsonNode edges = resp.path("data").path("categories").path("edges");
            return edges.size() > 0 ? edges.get(0).path("node").path("id").asText() : null;
        });
    }    
    
    
    private static Map<String, Object> vars(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    /* -----------------------------------------------------------
     * Schemas for lookups (Saleor 3.21)
     * --------------------------------------------------------- */
    private static final String Q_ATTRS_PAGE = ""
            + "query($after:String){\n"
            + "  attributes(first:100, after:$after){\n"
            + "    pageInfo{ hasNextPage endCursor }\n"
            + "    edges{ node{ id slug type inputType\n"
            + "      choices(first:100){ edges{ node{ id slug name } } }\n"
            + "    } }\n"
            + "  }\n"
            + "}";

    private static final String Q_PRODUCT_TYPE_BY_SLUG = ""
            + "query($slug:String!){\n"
            + "  productTypes(first:1, filter:{ slugs: [$slug] }){ edges{ node{ id slug } } }\n"
            + "}";

    private static final String Q_PRODUCT_BY_SLUG = ""
            + "query($slug:String!){\n"
            + "  products(first:1, filter:{ slugs: [$slug] }){ edges{ node{ id slug } } }\n"
            + "}";

    private static final String Q_CHANNELS = ""
            + "query{ channels{ id slug name isActive } }";

    private static final String M_ATTRIBUTE_VALUE_CREATE = ""
            + "mutation($attr:ID!, $name:String!, $slug:String!){\n"
            + "  attributeValueCreate(attribute:$attr, input:{ name:$name, slug:$slug }){\n"
            + "    errors{ field message } attribute{ id } value{ id slug }\n"
            + "  }\n"
            + "}";

    private static final String M_PRODUCT_CREATE = ""
            + "mutation($input: ProductCreateInput!){\n"
            + "  productCreate(input:$input){ errors{ field message } product{ id slug } }\n"
            + "}";

    private static final String M_PRODUCT_UPDATE = ""
            + "mutation($id:ID!, $input: ProductInput!){\n"
            + "  productUpdate(id:$id, input:$input){ errors{ field message } product{ id slug } }\n"
            + "}";

    // Saleor 3.21 expects 'input: ProductChannelListingUpdateInput!'
    // with addChannels/updateChannels/removeChannels
    private static final String M_PRODUCT_CHANNEL_LISTING_UPDATE =
    	    "mutation($id:ID!, $input: ProductChannelListingUpdateInput!){\n" +
    	    "  productChannelListingUpdate(id:$id, input:$input){\n" +
    	    "    errors { field message }\n" +
    	    "    product { id }\n" +
    	    "  }\n" +
    	    "}";

    /* -----------------------------------------------------------
     * Caches: attributes & channels
     * --------------------------------------------------------- */
    private static class AttrInfo {
        String id;
        String inputType;                   // from Saleor
        Map<String, String> valueSlugToId;  // for choices
    }

    private Map<String, AttrInfo> loadAttributesWithValues() {
        Map<String, AttrInfo> map = new HashMap<>();
        String after = null;
        while (true) {
            JsonNode resp = call(Q_ATTRS_PAGE, vars("after", after));
            JsonNode conn = resp.path("data").path("attributes");
            for (JsonNode e : conn.path("edges")) {
                JsonNode n = e.path("node");

                // Keep only PRODUCT_TYPE attributes (product-level)
                if (!"PRODUCT_TYPE".equals(n.path("type").asText())) continue;

                AttrInfo ai = new AttrInfo();
                ai.id = n.path("id").asText();
                ai.inputType = n.path("inputType").asText();
                ai.valueSlugToId = new HashMap<>();

                // For choice-based attrs, cache slug->id for values
                JsonNode edges = n.path("choices").path("edges");
                if (edges.isArray()) {
                    for (JsonNode ce : edges) {
                        JsonNode v = ce.path("node");
                        String s = v.path("slug").asText();
                        String id = v.path("id").asText();
                        if (!s.isBlank() && !id.isBlank()) ai.valueSlugToId.put(s, id);
                    }
                }
                map.put(n.path("slug").asText(), ai);
            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            if (!hasNext) break;
            after = conn.path("pageInfo").path("endCursor").asText(null);
        }
        return map;
    }

    private Map<String, String> loadChannelSlugToId() {
        Map<String, String> out = new HashMap<>();
        JsonNode resp = call(Q_CHANNELS, null);
        for (JsonNode ch : resp.path("data").path("channels")) {
            String slug = ch.path("slug").asText();
            String id = ch.path("id").asText();
            if (!slug.isBlank() && !id.isBlank()) out.put(slug, id);
        }
        return out;
    }

    private String getProductTypeIdBySlug(String slug) {
        JsonNode resp = call(Q_PRODUCT_TYPE_BY_SLUG, vars("slug", slug));
        JsonNode edges = resp.path("data").path("productTypes").path("edges");
        return edges.size() > 0 ? edges.get(0).path("node").path("id").asText() : null;
    }

    private String getExistingProductIdBySlug(String slug) {
        JsonNode resp = call(Q_PRODUCT_BY_SLUG, vars("slug", slug));
        JsonNode edges = resp.path("data").path("products").path("edges");
        return edges.size() > 0 ? edges.get(0).path("node").path("id").asText() : null;
    }
    
    private String editorJs(String text) {
        ObjectNode root = mapper.createObjectNode();
        root.put("time", 0);
        root.put("version", "2.26.5");
        ArrayNode blocks = mapper.createArrayNode();

        ObjectNode para = mapper.createObjectNode();
        para.put("type", "paragraph");
        ObjectNode data = mapper.createObjectNode();
        data.put("text", text == null ? "" : text);
        para.set("data", data);
        blocks.add(para);

        root.set("blocks", blocks);
        try { return mapper.writeValueAsString(root); }
        catch (Exception e) { return "{\"time\":0,\"blocks\":[]}"; }
    }   
    

    /* -----------------------------------------------------------
     * Attribute payload builder
     * --------------------------------------------------------- */
    private ArrayNode buildAttributesPayload(ArrayNode exportAttrs, Map<String, AttrInfo> attrMap) {
        ArrayNode out = mapper.createArrayNode();
        if (exportAttrs == null) return out;

        for (JsonNode a : exportAttrs) {
            String aSlug = a.path("attribute").asText();        // your input JSON: { "attribute": "<slug>", "inputType": "...", "values": [...] }
            String inputType = a.path("inputType").asText();     // trust your source, but fallback to Saleor value if missing
            ArrayNode vals = (ArrayNode) a.path("values");

            AttrInfo ai = attrMap.get(aSlug);
            if (ai == null) { System.out.println("⚠️ Missing attribute in Saleor: " + aSlug); continue; }

            ObjectNode entry = mapper.createObjectNode();
            entry.put("id", ai.id);

            String it = (inputType == null || inputType.isBlank()) ? ai.inputType : inputType;
            if (it == null) { System.out.println("⚠️ No inputType for " + aSlug); continue; }

            switch (it.toUpperCase()) {
                case "DROPDOWN": {
                    // expect one value
                    String vSlug = vals != null && vals.size() > 0 ? vals.get(0).asText() : null;
                    if (vSlug == null || vSlug.isBlank()) break;
                    String vId = ensureChoiceId(ai, vSlug);
                    if (vId != null) entry.set("dropdown", obj("id", vId));
                    break;
                }
                case "MULTISELECT": {
                    ArrayNode arr = mapper.createArrayNode();
                    if (vals != null) {
                        for (JsonNode v : vals) {
                            String vId = ensureChoiceId(ai, v.asText());
                            if (vId != null) arr.add(obj("id", vId));
                        }
                    }
                    entry.set("multiselect", arr);
                    break;
                }
                case "SWATCH": {
                    String vSlug = vals != null && vals.size() > 0 ? vals.get(0).asText() : null;
                    if (vSlug == null || vSlug.isBlank()) break;
                    String vId = ensureChoiceId(ai, vSlug);
                    if (vId != null) entry.set("swatch", obj("id", vId));
                    break;
                }
                case "BOOLEAN": {
                    // values: ["true"] or ["false"] in your import
                    boolean b = vals != null && vals.size() > 0 && Boolean.parseBoolean(vals.get(0).asText());
                    entry.put("boolean", b);
                    break;
                }
                case "PLAIN_TEXT": {
                    String s = vals != null && vals.size() > 0 ? vals.get(0).asText() : "";
                    entry.put("plainText", s);
                    break;
                }
                case "RICH_TEXT": {
                    String rich = (vals != null && vals.size() > 0)
                        ? editorJs(vals.get(0).asText()) // convert to EditorJS JSON string
                        : editorJs("");
                    entry.put("richText", rich);
                    break;
                }
                
                case "NUMERIC": {
                    String num = vals != null && vals.size() > 0 ? vals.get(0).asText() : null;
                    if (num != null) entry.put("numeric", num);
                    break;
                }
                case "DATE": {
                    String date = vals != null && vals.size() > 0 ? vals.get(0).asText() : null;
                    if (date != null) entry.put("date", date);
                    break;
                }
                case "DATETIME": {
                    String dt = vals != null && vals.size() > 0 ? vals.get(0).asText() : null;
                    if (dt != null) entry.put("dateTime", dt);
                    break;
                }
                case "FILE": {
                    String file = vals != null && vals.size() > 0 ? vals.get(0).asText() : null;
                    if (file != null) entry.put("file", file);
                    break;
                }
                case "REFERENCE": {
                    ArrayNode refs = mapper.createArrayNode();
                    if (vals != null) for (JsonNode v : vals) refs.add(v.asText());
                    entry.set("references", refs);
                    break;
                }
                default:
                    System.out.println("⚠️ Unsupported inputType: " + it + " for " + aSlug);
            }

            out.add(entry);
        }
        return out;
    }

    private ObjectNode obj(String k, String v) {
        ObjectNode o = mapper.createObjectNode(); o.put(k, v); return o;
    }

    private String ensureChoiceId(AttrInfo ai, String vSlug) {
        if (vSlug == null || vSlug.isBlank()) return null;
        String vId = ai.valueSlugToId.get(vSlug);
        if (vId != null) return vId;

        // create missing value
        JsonNode mk = call(M_ATTRIBUTE_VALUE_CREATE, vars("attr", ai.id, "name", vSlug, "slug", vSlug));
        JsonNode err = mk.path("data").path("attributeValueCreate").path("errors");
        if (err.isArray() && err.size() > 0) {
            System.out.println("⚠️ attributeValueCreate error for " + vSlug + ": " + err);
            return null;
        }
        vId = mk.path("data").path("attributeValueCreate").path("value").path("id").asText();
        if (vId != null && !vId.isBlank()) ai.valueSlugToId.put(vSlug, vId);
        return vId;
    }
 
    
    
    

    /* -----------------------------------------------------------
     * Import entrypoint
     * --------------------------------------------------------- */
    @GetMapping("/PutProductJson")
    public String importProducts() throws Exception {
        // 1) read products file
        File f = new File("/tmp/products.json");
        ArrayNode products = (ArrayNode) mapper.readTree(f);

        // 2) warm caches
        Map<String, AttrInfo> attrMap = loadAttributesWithValues();
        Map<String, String> channelSlugToId = loadChannelSlugToId();

        int created = 0, updated = 0, failed = 0, chOk = 0, chErr = 0;

        // 3) process each product
        for (JsonNode p : products) {
            String name = p.path("name").asText();
            String slug = p.path("slug").asText();
            String description = p.path("description").asText(null);
            String ptSlug = p.path("productType").asText();
         // Attach default category if none is present in your import JSON
            // product type
            String ptId = getProductTypeIdBySlug(ptSlug);
            if (ptId == null) {
                System.out.println("❌ Missing productType in dev: " + ptSlug + " (skip " + slug + ")");
                failed++;
                continue;
            }

            ArrayNode attrsPayload = buildAttributesPayload((ArrayNode) p.path("attributes"), attrMap);

            // create or update
            String productId = getExistingProductIdBySlug(slug);
            if (productId == null) {
                ObjectNode input = mapper.createObjectNode();
                input.put("name", name);
                input.put("slug", slug);
                input.put("productType", ptId);
                String categoryId = categoryIdBySlug("default-category"); // <- adjust if your slug differs
                if (categoryId != null) {
                    input.put("category", categoryId);
                } else {
                    System.out.println("⚠️ Default category slug not found; product will be created without category: " + slug);
                }
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
                created++;
                } else {
                ObjectNode input = mapper.createObjectNode();
                input.put("name", name);
                if (description != null) input.put("description", description);
                input.set("attributes", attrsPayload);

                JsonNode resp = call(M_PRODUCT_UPDATE, vars("id", productId, "input", input));
                JsonNode errs = resp.path("data").path("productUpdate").path("errors");
                if (errs.isArray() && errs.size() > 0) {
                    System.out.println("❌ productUpdate errors for " + slug + ": " + errs);
                    failed++;
                    continue;
                }
                updated++;
            }

            // channels (optional)
            ArrayNode chans = (ArrayNode) p.path("channels");
            if (productId != null && chans != null && chans.size() > 0) {
                ArrayNode addChannels = mapper.createArrayNode();
                for (JsonNode c : chans /* your JSON array from import */) {
                    slug = c.path("channel").asText();
                    String chId = channelSlugToId.get(slug);   // you already cached slug->id
                    if (chId == null) {
                        System.out.println("⚠️ Unknown channel slug: " + slug + " (skip)");
                        continue;
                    }
                    ObjectNode row = mapper.createObjectNode();
                    row.put("channelId", chId);
                    row.put("isPublished", c.path("isPublished").asBoolean(false));
                    row.put("visibleInListings", c.path("visibleInListings").asBoolean(false));
                    addChannels.add(row);
                }

                // Wrap in input: { addChannels: [...] }
                ObjectNode input = mapper.createObjectNode();
                input.set("updateChannels", addChannels);

                // Call mutation
                JsonNode r = call(M_PRODUCT_CHANNEL_LISTING_UPDATE, vars("id", productId, "input", input));
                JsonNode errs = r.path("data").path("productChannelListingUpdate").path("errors");
                if (errs.isArray() && errs.size() > 0) {
                    System.out.println("⚠️ productChannelListingUpdate errors for " + slug + ": " + errs);
                }
            }
        }

        return String.format(
                "Done. Created: %d, Updated: %d, Failed: %d, Channels OK: %d, Channel errs: %d",
                created, updated, failed, chOk, chErr);
    }
}
