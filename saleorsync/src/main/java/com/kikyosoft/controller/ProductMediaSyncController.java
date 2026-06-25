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

/**
 * ProductMediaSyncController (Saleor 3.21.x)
 *
 * Routes (all GET for easy testing):
 *  - /admin/GetProductMediaJson
 *      → Exports all product media to /tmp/product_media.json
 *      → Format:
 *        [
 *          {
 *            "product":"cheval-des-andes-2018",
 *            "media":[
 *              {"id":"...", "url":"https://.../front.jpg", "alt":"Front", "sortOrder":0},
 *              {"id":"...", "url":"https://.../back.jpg",  "alt":"Back",  "sortOrder":1}
 *            ]
 *          }
 *        ]
 *
 *  - /admin/DeleteAllProductMedia
 *      → Deletes all product media (keeps products intact)
 *
 *  - /admin/PutProductMediaJson
 *      → Imports media from /tmp/product_media.json
 *      → Idempotent by URL per product (skips if URL already present)
 *      → Optionally reorders by sortOrder if provided
 *
 *  - /admin/UpdateProductMedia
 *      → Update a single media of a product
 *      → Params (one of mediaId or existingUrl is required):
 *        productSlug=... & (mediaId=... | existingUrl=...) &
 *        newAlt=... & newUrl=... & newSort=#
 *      → Notes:
 *        - Changing URL = delete old media + create a new one with the new URL (Saleor only updates alt via update)
 *        - Changing sort uses productMediaReorder
 */
@RestController
@RequestMapping("/admin")
public class ProductMediaSyncController {

    @Autowired
    private SaleorConfig config;

    private final RestTemplate rest = new RestTemplate();

    private ObjectMapper mapper() { return config.getMapper(); }

    /* ----------------------------- HTTP → GraphQL ----------------------------- */
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

    /* ------------------------------ GraphQL docs ------------------------------ */
    private static final String Q_PRODUCTS_PAGE = ""
            + "query($after:String){\n"
            + "  products(first:100, after:$after){\n"
            + "    pageInfo{ hasNextPage endCursor }\n"
            + "    edges{ node{\n"
            + "      id name slug\n"
            + "      media{ id url alt sortOrder }\n"
            + "    } }\n"
            + "  }\n"
            + "}";

    private static final String Q_PRODUCT_BY_SLUG = ""
            + "query($slug:String!){\n"
            + "  products(first:1, filter:{ slugs: [$slug] }){\n"
            + "    edges{ node{ id slug name media{ id url alt sortOrder } } }\n"
            + "  }\n"
            + "}";

    private static final String M_PRODUCT_MEDIA_CREATE = ""
            + "mutation($productId:ID!, $url:String!, $alt:String){\n"
            + "  productMediaCreate(\n"
            + "    product:$productId,\n"
            + "    input:{ mediaUrl:$url, alt:$alt }\n"
            + "  ){\n"
            + "    media{ id url alt sortOrder }\n"
            + "    errors{ field message code }\n"
            + "  }\n"
            + "}";

    private static final String M_PRODUCT_MEDIA_UPDATE = ""
            + "mutation($id:ID!, $alt:String){\n"
            + "  productMediaUpdate(id:$id, input:{ alt:$alt }){\n"
            + "    media{ id url alt sortOrder }\n"
            + "    errors{ field message code }\n"
            + "  }\n"
            + "}";

    private static final String M_PRODUCT_MEDIA_DELETE = ""
            + "mutation($id:ID!){\n"
            + "  productMediaDelete(id:$id){ errors{ field message code } }\n"
            + "}";

    private static final String M_PRODUCT_MEDIA_REORDER = ""
            + "mutation($productId:ID!, $mediaIds:[ID!]!){\n"
            + "  productMediaReorder(productId:$productId, mediaIds:$mediaIds){\n"
            + "    product{ id }\n"
            + "    errors{ field message code }\n"
            + "  }\n"
            + "}";

    /* ------------------------------- Lookups --------------------------------- */
    private ObjectNode getProductBySlug(String slug) {
        JsonNode r = call(Q_PRODUCT_BY_SLUG, vars("slug", slug));
        JsonNode edges = r.path("data").path("products").path("edges");
        if (edges.isArray() && edges.size() > 0) return (ObjectNode) edges.get(0).path("node");
        return null;
    }

    private String productIdBySlug(String slug) {
        ObjectNode n = getProductBySlug(slug);
        return n == null ? null : n.path("id").asText();
    }

    /* -------------------------------- Export --------------------------------- */
    @GetMapping("/GetProductMediaJson")
    public String exportProductMedia() throws Exception {
        ArrayNode out = mapper().createArrayNode();

        String after = null;
        do {
            JsonNode r = call(Q_PRODUCTS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("products");
            for (JsonNode e : conn.path("edges")) {
                JsonNode p = e.path("node");
                ArrayNode mediaArr = mapper().createArrayNode();
                for (JsonNode m : p.path("media")) {
                    ObjectNode row = mapper().createObjectNode();
                    row.put("id", m.path("id").asText());
                    row.put("url", m.path("url").asText());
                    row.put("alt", m.path("alt").isNull() ? null : m.path("alt").asText());
                    row.put("sortOrder", m.path("sortOrder").isNull() ? null : m.path("sortOrder").asInt());
                    mediaArr.add(row);
                }
                ObjectNode pr = mapper().createObjectNode();
                pr.put("product", p.path("slug").asText());
                pr.set("media", mediaArr);
                out.add(pr);
            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);

        File f = new File("/tmp/product_media.json");
        mapper().writerWithDefaultPrettyPrinter().writeValue(f, out);
        return "Exported product media for " + out.size() + " products → " + f.getAbsolutePath();
    }

    /* -------------------------------- Delete --------------------------------- */
    @GetMapping("/DeleteAllProductMedia")
    public String deleteAllProductMedia() {
        int deleted = 0, errors = 0;
        String after = null;

        do {
            JsonNode r = call(Q_PRODUCTS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("products");

            List<JsonNode> nodes = new ArrayList<>();
            for (JsonNode e : conn.path("edges")) nodes.add(e.path("node"));

            if (nodes.isEmpty()) break;

            for (JsonNode p : nodes) {
                for (JsonNode m : p.path("media")) {
                    String id = m.path("id").asText();
                    JsonNode d = call(M_PRODUCT_MEDIA_DELETE, vars("id", id));
                    JsonNode errs = d.path("data").path("productMediaDelete").path("errors");
                    if (errs.isArray() && errs.size() > 0) {
                        System.out.println("⚠️ productMediaDelete error for media=" + id + " : " + errs);
                        errors++;
                    } else {
                        deleted++;
                    }
                }
            }

            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);

        return String.format("Deleted %d media entries. Errors: %d", deleted, errors);
    }

    /* -------------------------------- Import --------------------------------- */
    @GetMapping("/PutProductMediaJson")
    public String importProductMedia() throws Exception {
        File f = new File("/tmp/product_media.json");
        if (!f.exists()) return "File not found: " + f.getAbsolutePath();

        JsonNode root = mapper().readTree(Files.readAllBytes(f.toPath()));
        if (!root.isArray()) return "Invalid JSON: expected an array.";

        int created = 0, updatedAlt = 0, reordered = 0, skipped = 0, errors = 0;

        for (JsonNode row : root) {
            String productSlug = row.path("product").asText(null);
            if (productSlug == null) continue;

            ObjectNode product = getProductBySlug(productSlug);
            if (product == null) {
                System.out.println("⚠️ Product not found: " + productSlug + " → skip");
                continue;
            }
            String pid = product.path("id").asText();

            // Build current maps by URL and by ID
            Map<String, ObjectNode> byUrl = new HashMap<>();
            Map<String, ObjectNode> byId  = new HashMap<>();
            for (JsonNode m : product.path("media")) {
                ObjectNode mm = (ObjectNode) m;
                byUrl.put(mm.path("url").asText(), mm);
                byId.put(mm.path("id").asText(), mm);
            }

            // Create or update alt if same URL exists
            List<ObjectNode> desired = new ArrayList<>();
            for (JsonNode m : row.path("media")) {
                String url = m.path("url").asText(null);
                String alt = m.path("alt").isNull() ? null : m.path("alt").asText(null);
                Integer sort = m.path("sortOrder").isNull() ? null : m.path("sortOrder").asInt();

                if (url == null || url.isBlank()) continue;

                ObjectNode existing = byUrl.get(url);
                if (existing == null) {
                    // create
                    JsonNode c = call(M_PRODUCT_MEDIA_CREATE, vars("productId", pid, "url", url, "alt", alt));
                    JsonNode errs = c.path("data").path("productMediaCreate").path("errors");
                    if (errs.isArray() && errs.size() > 0) {
                        System.out.println("❌ productMediaCreate error for product=" + productSlug + " url=" + url + " : " + errs);
                        errors++;
                    } else {
                        JsonNode media = c.path("data").path("productMediaCreate").path("media");
                        desired.add((ObjectNode) media);
                        created++;
                    }
                } else {
                    // maybe update alt
                    String id = existing.path("id").asText();
                    String curAlt = existing.path("alt").isNull() ? null : existing.path("alt").asText(null);
                    if ((alt != null && !alt.equals(curAlt)) || (alt == null && curAlt != null)) {
                        JsonNode u = call(M_PRODUCT_MEDIA_UPDATE, vars("id", id, "alt", alt));
                        JsonNode errs = u.path("data").path("productMediaUpdate").path("errors");
                        if (errs.isArray() && errs.size() > 0) {
                            System.out.println("⚠️ productMediaUpdate error for id=" + id + " : " + errs);
                            errors++;
                        } else {
                            updatedAlt++;
                            existing = (ObjectNode) u.path("data").path("productMediaUpdate").path("media");
                        }
                    }
                    // collect the existing (will be used for reorder)
                    ObjectNode collected = mapper().createObjectNode();
                    collected.put("id", id);
                    collected.put("url", existing.path("url").asText());
                    collected.put("alt", existing.path("alt").isNull() ? null : existing.path("alt").asText(null));
                    collected.put("sortOrder", sort == null ? existing.path("sortOrder").asInt(0) : sort);
                    desired.add(collected);
                }
            }

            // Reorder if any sortOrder provided
            boolean anySortProvided = row.path("media").isArray() && row.path("media").size() > 0
                    && row.path("media").get(0).has("sortOrder");
            if (anySortProvided && desired.size() > 0) {
                desired.sort(Comparator.comparingInt(a -> a.path("sortOrder").asInt(0)));
                ArrayNode orderIds = mapper().createArrayNode();
                for (ObjectNode d : desired) orderIds.add(d.path("id").asText());
                JsonNode r = call(M_PRODUCT_MEDIA_REORDER, vars("productId", pid, "mediaIds", mapper().convertValue(orderIds, List.class)));
                JsonNode errs = r.path("data").path("productMediaReorder").path("errors");
                if (errs.isArray() && errs.size() > 0) {
                    System.out.println("⚠️ productMediaReorder error for product=" + productSlug + " : " + errs);
                    errors++;
                } else {
                    reordered++;
                }
            } else {
                skipped++; // nothing to reorder or no new media
            }
        }

        return String.format("Import done. Created: %d, AltUpdated: %d, Reordered: %d, Skipped: %d, Errors: %d",
                created, updatedAlt, reordered, skipped, errors);
    }

    /* ---------------------------- Update one media ---------------------------- */
    /**
     * Update a single media entry of a product.
     * Provide either mediaId or existingUrl to identify the media.
     *
     * Examples:
     *  - Update alt:
     *    /admin/UpdateProductMedia?productSlug=cheval-des-andes-2018&mediaId=QX...&newAlt=Front%20label
     *
     *  - Replace URL (delete+create):
     *    /admin/UpdateProductMedia?productSlug=cheval-des-andes-2018&existingUrl=https://old.jpg&newUrl=https://new.jpg&newAlt=Front
     *
     *  - Change order to index 0:
     *    /admin/UpdateProductMedia?productSlug=cheval-des-andes-2018&mediaId=QX...&newSort=0
     */
    @GetMapping("/UpdateProductMedia")
    public String updateProductMedia(
            @RequestParam("productSlug") String productSlug,
            @RequestParam(value = "mediaId", required = false) String mediaId,
            @RequestParam(value = "existingUrl", required = false) String existingUrl,
            @RequestParam(value = "newAlt", required = false) String newAlt,
            @RequestParam(value = "newUrl", required = false) String newUrl,
            @RequestParam(value = "newSort", required = false) Integer newSort
    ) {
        ObjectNode product = getProductBySlug(productSlug);
        if (product == null) return "Product not found: " + productSlug;

        String pid = product.path("id").asText();

        // find media by id or url
        ObjectNode target = null;
        if (mediaId != null && !mediaId.isBlank()) {
            for (JsonNode m : product.path("media")) {
                if (mediaId.equals(m.path("id").asText())) { target = (ObjectNode) m; break; }
            }
            if (target == null) return "Media not found by id for product=" + productSlug;
        } else if (existingUrl != null && !existingUrl.isBlank()) {
            for (JsonNode m : product.path("media")) {
                if (existingUrl.equals(m.path("url").asText())) { target = (ObjectNode) m; break; }
            }
            if (target == null) return "Media not found by url for product=" + productSlug;
            mediaId = target.path("id").asText();
        } else {
            return "Provide either mediaId or existingUrl to identify the media.";
        }

        int updatedAlt = 0, reordered = 0, replaced = 0;

        // 1) Replace URL if requested → delete + create
        if (newUrl != null && !newUrl.isBlank()) {
            // keep current index so we can restore ordering later
            int currentIndex = target.path("sortOrder").asInt(0);

            // delete old
            JsonNode d = call(M_PRODUCT_MEDIA_DELETE, vars("id", mediaId));
            JsonNode derrs = d.path("data").path("productMediaDelete").path("errors");
            if (derrs.isArray() && derrs.size() > 0) return "Delete old media failed: " + derrs.toString();

            // create new
            JsonNode c = call(M_PRODUCT_MEDIA_CREATE, vars("productId", pid, "url", newUrl, "alt", newAlt));
            JsonNode cerrs = c.path("data").path("productMediaCreate").path("errors");
            if (cerrs.isArray() && cerrs.size() > 0) return "Create new media failed: " + cerrs.toString();
            String newId = c.path("data").path("productMediaCreate").path("media").path("id").asText();
            replaced++;

            // reorder to the same index if requested explicitly or keep as is if newSort is null
            if (newSort != null || currentIndex == 0) {
                // reload product media
                product = getProductBySlug(productSlug);
                List<ObjectNode> list = new ArrayList<>();
                for (JsonNode m : product.path("media")) list.add((ObjectNode) m);

                // Desired target index
                int targetIndex = (newSort != null ? newSort : 0);

                // Build current order list
                List<String> order = list.stream()
                        .map(n -> n.path("id").asText())
                        .collect(Collectors.toList());

                // Move the newId to position targetIndex
                order.remove(newId);
                if (targetIndex < 0) targetIndex = 0;
                if (targetIndex > order.size()) targetIndex = order.size();
                order.add(targetIndex, newId);

                JsonNode r = call(M_PRODUCT_MEDIA_REORDER, vars("productId", pid, "mediaIds", order));
                JsonNode rerrs = r.path("data").path("productMediaReorder").path("errors");
                if (rerrs.isArray() && rerrs.size() > 0) return "Reorder failed: " + rerrs.toString();
                reordered++;
            }

            return String.format("Media replaced (new URL). Replaced:%d, Reordered:%d", replaced, reordered);
        }

        // 2) Update alt if requested
        if (newAlt != null) {
            JsonNode u = call(M_PRODUCT_MEDIA_UPDATE, vars("id", mediaId, "alt", newAlt));
            JsonNode uerrs = u.path("data").path("productMediaUpdate").path("errors");
            if (uerrs.isArray() && uerrs.size() > 0) return "Alt update failed: " + uerrs.toString();
            updatedAlt++;
        }

        // 3) Reorder if requested
        if (newSort != null) {
            // reload latest media list
            product = getProductBySlug(productSlug);
            List<ObjectNode> list = new ArrayList<>();
            for (JsonNode m : product.path("media")) list.add((ObjectNode) m);

            // current order
            List<String> order = list.stream().map(n -> n.path("id").asText()).collect(Collectors.toList());
            String id = mediaId;

            if (!order.contains(id)) return "Media id not present after update, cannot reorder.";
            order.remove(id);
            int idx = newSort;
            if (idx < 0) idx = 0;
            if (idx > order.size()) idx = order.size();
            order.add(idx, id);

            JsonNode r = call(M_PRODUCT_MEDIA_REORDER, vars("productId", pid, "mediaIds", order));
            JsonNode rerrs = r.path("data").path("productMediaReorder").path("errors");
            if (rerrs.isArray() && rerrs.size() > 0) return "Reorder failed: " + rerrs.toString();
            reordered++;
        }

        return String.format("Media updated. AltUpdated:%d, Reordered:%d", updatedAlt, reordered);
    }
}
