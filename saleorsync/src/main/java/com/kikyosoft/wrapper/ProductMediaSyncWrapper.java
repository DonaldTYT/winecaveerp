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
public class ProductMediaSyncWrapper {

    private final SaleorConfig config;
    private final RestTemplate rest;

    public ProductMediaSyncWrapper(SaleorConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.rest = restTemplate;
    }

    private ObjectMapper mapper() { return config.getMapper(); }

    /* ----------------------------- HTTP → GraphQL ----------------------------- */
    private JsonNode call(String query, Map<String, Object> variables) {
        ObjectNode body = mapper().createObjectNode();
        body.put("query", query);
        body.set("variables", variables == null ? mapper().createObjectNode() : mapper().valueToTree(variables));

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("Authorization", config.getAuthHeader()); // must be "Bearer <token>"
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
    private static final String Q_PRODUCTS_PAGE =
            "query($after:String){\n" +
            "  products(first:100, after:$after){\n" +
            "    pageInfo{ hasNextPage endCursor }\n" +
            "    edges{ node{ id name slug media{ id url alt sortOrder } } }\n" +
            "  }\n" +
            "}";

    private static final String Q_PRODUCT_BY_SLUG =
            "query($slug:String!){\n" +
            "  products(first:1, filter:{ slugs: [$slug] }){\n" +
            "    edges{ node{ id slug name media{ id url alt sortOrder } } }\n" +
            "  }\n" +
            "}";

    // ---- IMPORTANT: matches Saleor 3.21 working schema (your SaleorMediaService) ----
    private static final String M_PRODUCT_MEDIA_CREATE_BY_URL =
            "mutation ($productId: ID!, $imgUrl: String!, $alt: String) {\n" +
            "  productMediaCreate(input: { product: $productId, mediaUrl: $imgUrl, alt: $alt }) {\n" +
            "    media { id url alt sortOrder }\n" +
            "    errors { field message code }\n" +
            "  }\n" +
            "}";

    private static final String M_PRODUCT_MEDIA_UPDATE =
            "mutation($id:ID!, $alt:String){\n" +
            "  productMediaUpdate(id:$id, input:{ alt:$alt }){\n" +
            "    media{ id url alt sortOrder }\n" +
            "    errors{ field message code }\n" +
            "  }\n" +
            "}";

    private static final String M_PRODUCT_MEDIA_DELETE =
            "mutation($id:ID!){ productMediaDelete(id:$id){ errors{ field message code } } }";

    private static final String M_PRODUCT_MEDIA_REORDER =
            "mutation($productId:ID!, $mediaIds:[ID!]!){\n" +
            "  productMediaReorder(productId:$productId, mediaIds:$mediaIds){\n" +
            "    product{ id }\n" +
            "    errors{ field message code }\n" +
            "  }\n" +
            "}";

    /* ------------------------------- Lookups --------------------------------- */
    private ObjectNode getProductBySlug(String slug) {
        JsonNode r = call(Q_PRODUCT_BY_SLUG, vars("slug", slug));
        JsonNode edges = r.path("data").path("products").path("edges");
        if (edges.isArray() && edges.size() > 0) return (ObjectNode) edges.get(0).path("node");
        return null;
    }

    /* -------------------------------- Export --------------------------------- */
    /**
     * Windowed export (no filesystem). Returns: "OK  [ ...json array... ]"
     * @param start zero-based start index across all products while paging
     * @param count number of products to return (0 = all)
     */
    public String exportProductMedia(int start, int count) throws Exception {
        if (start < 0) start = 0;
        if (count < 0) count = 0;

        ArrayNode out = mapper().createArrayNode();
        String after = null;
        int seen = 0, endExclusive = (count == 0 ? Integer.MAX_VALUE : start + count);

        outer:
        do {
            JsonNode r = call(Q_PRODUCTS_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("products");
            for (JsonNode e : conn.path("edges")) {
                if (seen >= endExclusive) break outer;
                JsonNode p = e.path("node");
                if (seen++ < start) continue;

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

        return "OK  " + mapper().writeValueAsString(out);
    }

    /* -------------------------------- Delete --------------------------------- */
    /** Deletes all product media across all products. */
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
                        System.out.println("⚠️ productMediaDelete error media=" + id + " : " + errs);
                        errors++;
                    } else {
                        deleted++;
                    }
                }
            }

            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);

        return String.format("OK  Deleted:%d Errors:%d", deleted, errors);
    }

    /* -------------------------------- Import --------------------------------- */
    /**
     * Imports product media from JSON text (array of {product, media:[{url,alt,sortOrder}]})
     * - Idempotent by URL within a product
     * - Updates alt if URL already present
     * - Reorders when sortOrder is provided
     *
     * JSON format is unchanged.
     */
    public String importProductMedia(String jsonArray) throws Exception {
        if (jsonArray == null || jsonArray.isBlank()) return "OK  Created:0 AltUpdated:0 Reordered:0 Skipped:0 Errors:0";

        JsonNode root = mapper().readTree(jsonArray);
        if (!root.isArray()) return "Invalid JSON: expected an array.";

        int created = 0, updatedAlt = 0, reordered = 0, skipped = 0, errors = 0;

        for (JsonNode row : root) {
            String productSlug = row.path("product").asText(null);
            if (productSlug == null || productSlug.isBlank()) continue;

            ObjectNode product = getProductBySlug(productSlug);
            if (product == null) {
                System.out.println("⚠️ Product not found: " + productSlug + " → skip");
                continue;
            }
            String pid = product.path("id").asText();

            Map<String, ObjectNode> byUrl = new HashMap<>();
            for (JsonNode m : product.path("media")) {
                ObjectNode mm = (ObjectNode) m;
                byUrl.put(mm.path("url").asText(), mm);
            }

            List<ObjectNode> desired = new ArrayList<>();
            for (JsonNode m : row.path("media")) {
                String url = m.path("url").asText(null);
                String alt = m.path("alt").isNull() ? null : m.path("alt").asText(null);
                Integer sort = m.path("sortOrder").isNull() ? null : m.path("sortOrder").asInt();

                if (url == null || url.isBlank()) continue;

                ObjectNode existing = byUrl.get(url);
                if (existing == null) {
                    // ---- CREATE BY URL (matches SaleorMediaService schema) ----
                    JsonNode c = call(
                            M_PRODUCT_MEDIA_CREATE_BY_URL,
                            vars("productId", pid, "imgUrl", url, "alt", alt)
                    );
                    JsonNode payload = c.path("data").path("productMediaCreate");
                    JsonNode errs = payload.path("errors");
                    if (errs.isArray() && errs.size() > 0) {
                        System.out.println("❌ productMediaCreate error product=" + productSlug + " url=" + url + " : " + errs);
                        errors++;
                    } else {
                        ObjectNode media = (ObjectNode) payload.path("media");
                        if (sort != null) media.put("sortOrder", sort);
                        desired.add(media);
                        created++;
                    }
                } else {
                    // maybe update alt
                    String id = existing.path("id").asText();
                    String curAlt = existing.path("alt").isNull() ? null : existing.path("alt").asText(null);
                    if (!Objects.equals(alt, curAlt)) {
                        JsonNode u = call(M_PRODUCT_MEDIA_UPDATE, vars("id", id, "alt", alt));
                        JsonNode errs = u.path("data").path("productMediaUpdate").path("errors");
                        if (errs.isArray() && errs.size() > 0) {
                            System.out.println("⚠️ productMediaUpdate error id=" + id + " : " + errs);
                            errors++;
                        } else {
                            updatedAlt++;
                            existing = (ObjectNode) u.path("data").path("productMediaUpdate").path("media");
                        }
                    }
                    // collect for potential reorder
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
                List<String> orderIds = desired.stream().map(n -> n.path("id").asText()).collect(Collectors.toList());
                JsonNode r = call(M_PRODUCT_MEDIA_REORDER, vars("productId", pid, "mediaIds", orderIds));
                JsonNode errs = r.path("data").path("productMediaReorder").path("errors");
                if (errs.isArray() && errs.size() > 0) {
                    System.out.println("⚠️ productMediaReorder error product=" + productSlug + " : " + errs);
                    errors++;
                } else {
                    reordered++;
                }
            } else {
                skipped++;
            }
        }

        return String.format("OK  Created:%d AltUpdated:%d Reordered:%d Skipped:%d Errors:%d",
                created, updatedAlt, reordered, skipped, errors);
    }

    /* ---------------------------- Update one media ---------------------------- */
    /**
     * Update a single media entry of a product.
     * Provide either mediaId or existingUrl to identify the media.
     *
     * - If newUrl is set: delete old media and create a new one (alt can be set via newAlt)
     * - If newAlt is set: update alt
     * - If newSort is set: reorder media to that index
     */
    public String updateProductMedia(
            String productSlug,
            String mediaId,
            String existingUrl,
            String newAlt,
            String newUrl,
            Integer newSort
    ) {
        ObjectNode product = getProductBySlug(productSlug);
        if (product == null) return "Product not found: " + productSlug;

        String pid = product.path("id").asText();

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
            return "Provide either mediaId or existingUrl.";
        }

        int updatedAlt = 0, reordered = 0, replaced = 0;

        // Replace URL (delete + create by URL) then optionally reorder
        if (newUrl != null && !newUrl.isBlank()) {
            int currentIndex = target.path("sortOrder").asInt(0);

            JsonNode d = call(M_PRODUCT_MEDIA_DELETE, vars("id", mediaId));
            JsonNode derrs = d.path("data").path("productMediaDelete").path("errors");
            if (derrs.isArray() && derrs.size() > 0) return "Delete old media failed: " + derrs.toString();

            // create via URL schema (same as import)
            JsonNode c = call(M_PRODUCT_MEDIA_CREATE_BY_URL, vars("productId", pid, "imgUrl", newUrl, "alt", newAlt));
            JsonNode cerrs = c.path("data").path("productMediaCreate").path("errors");
            if (cerrs.isArray() && cerrs.size() > 0) return "Create new media failed: " + cerrs.toString();
            String newId = c.path("data").path("productMediaCreate").path("media").path("id").asText();
            replaced++;

            if (newSort != null || currentIndex == 0) {
                product = getProductBySlug(productSlug);
                List<String> order = new ArrayList<>();
                for (JsonNode m : product.path("media")) order.add(m.path("id").asText());

                order.remove(newId);
                int targetIndex = (newSort != null ? newSort : 0);
                if (targetIndex < 0) targetIndex = 0;
                if (targetIndex > order.size()) targetIndex = order.size();
                order.add(targetIndex, newId);

                JsonNode r = call(M_PRODUCT_MEDIA_REORDER, vars("productId", pid, "mediaIds", order));
                JsonNode rerrs = r.path("data").path("productMediaReorder").path("errors");
                if (rerrs.isArray() && rerrs.size() > 0) return "Reorder failed: " + rerrs.toString();
                reordered++;
            }
            return String.format("OK  Replaced:%d Reordered:%d", replaced, reordered);
        }

        // Update alt
        if (newAlt != null) {
            JsonNode u = call(M_PRODUCT_MEDIA_UPDATE, vars("id", mediaId, "alt", newAlt));
            JsonNode uerrs = u.path("data").path("productMediaUpdate").path("errors");
            if (uerrs.isArray() && uerrs.size() > 0) return "Alt update failed: " + uerrs.toString();
            updatedAlt++;
        }

        // Reorder
        if (newSort != null) {
            product = getProductBySlug(productSlug);
            List<String> order = new ArrayList<>();
            for (JsonNode m : product.path("media")) order.add(m.path("id").asText());

            if (!order.contains(mediaId)) return "Media id not present after update, cannot reorder.";
            order.remove(mediaId);
            int idx = newSort;
            if (idx < 0) idx = 0;
            if (idx > order.size()) idx = order.size();
            order.add(idx, mediaId);

            JsonNode r = call(M_PRODUCT_MEDIA_REORDER, vars("productId", pid, "mediaIds", order));
            JsonNode rerrs = r.path("data").path("productMediaReorder").path("errors");
            if (rerrs.isArray() && rerrs.size() > 0) return "Reorder failed: " + rerrs.toString();
            reordered++;
        }

        return String.format("OK  AltUpdated:%d Reordered:%d", updatedAlt, reordered);
    }

    /** Delete all product media for the specified product slugs. */
    public String deleteProductMedia(java.util.List<String> p_slugs) {
        if (p_slugs == null || p_slugs.isEmpty()) return "OK  Deleted:0 Missing:0 Empty:0 Errors:0";

        int deleted = 0, errors = 0, missing = 0, empty = 0;

        // de-dupe while keeping order
        for (String slug : new java.util.LinkedHashSet<>(p_slugs)) {
            if (slug == null || slug.isBlank()) { missing++; continue; }

            ObjectNode product = getProductBySlug(slug);
            if (product == null) { missing++; continue; }

            ArrayNode media = (ArrayNode) product.path("media");
            if (media == null || media.size() == 0) { empty++; continue; }

            for (JsonNode m : media) {
                String id = m.path("id").asText(null);
                if (id == null || id.isBlank()) continue;

                try {
                    JsonNode d = call(M_PRODUCT_MEDIA_DELETE, vars("id", id));
                    JsonNode errs = d.path("data").path("productMediaDelete").path("errors");
                    if (errs.isArray() && errs.size() > 0) {
                        System.out.println("⚠️ productMediaDelete error for product=" + slug + " media=" + id + " : " + errs);
                        errors++;
                    } else {
                        deleted++;
                    }
                } catch (Exception ex) {
                    System.out.println("⚠️ productMediaDelete exception for product=" + slug + " media=" + id + " : " + ex.getMessage());
                    errors++;
                }
            }
        }

        return String.format("OK  Deleted:%d Missing:%d Empty:%d Errors:%d", deleted, missing, empty, errors);
    }
}
