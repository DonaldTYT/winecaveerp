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
public class CategorySyncWrapper {

    private static final String DEFAULT_CATEGORY_SLUG = "default-category";

    private final SaleorConfig config;
    private final RestTemplate rest;

    public CategorySyncWrapper(SaleorConfig config, RestTemplate restTemplate) {
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
    private static final String Q_CATEGORIES_PAGE = ""
            + "query($after:String){\n"
            + "  categories(first:100, after:$after){\n"
            + "    pageInfo{ hasNextPage endCursor }\n"
            + "    edges{ node{ id name slug description parent{ id slug } } }\n"
            + "  }\n"
            + "}";

    private static final String Q_CATEGORY_BY_SLUG = ""
            + "query($slug:String!){\n"
            + "  categories(first:1, filter:{ slugs: [$slug] }){\n"
            + "    edges{ node{ id slug } }\n"
            + "  }\n"
            + "}";

    // Correct per Saleor docs: CategoryInput + parent as a separate arg
    private static final String M_CATEGORY_CREATE =
            "mutation M_CATEGORY_CREATE($input: CategoryInput!, $parent: ID){\n" +
            "  categoryCreate(input: $input, parent: $parent){\n" +
            "    category { id name slug parent { id } }\n" +
            "    errors { field message code }\n" +
            "  }\n" +
            "}";

    // Correct per Saleor docs: CategoryInput for updates
    private static final String M_CATEGORY_UPDATE =
            "mutation M_CATEGORY_UPDATE($id: ID!, $input: CategoryInput!) {\n" +
            "  categoryUpdate(id: $id, input: $input) {\n" +
            "    category { id name slug parent { id } }\n" +
            "    errors { field message code }\n" +
            "  }\n" +
            "}";

    private static final String M_CATEGORY_DELETE = ""
            + "mutation($id:ID!){\n"
            + "  categoryDelete(id:$id){ errors{ field message code } }\n"
            + "}";

    /* ------------------------------- Helpers -------------------------------- */
    private List<ObjectNode> listAllCategories() {
        List<ObjectNode> out = new ArrayList<>();
        String after = null;
        do {
            JsonNode r = call(Q_CATEGORIES_PAGE, vars("after", after));
            JsonNode conn = r.path("data").path("categories");
            for (JsonNode e : conn.path("edges")) {
                ObjectNode n = ((ObjectNode) e.path("node")).deepCopy();
                out.add(n);
            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            after = hasNext ? conn.path("pageInfo").path("endCursor").asText(null) : null;
        } while (after != null);
        return out;
    }

    private String categoryIdBySlug(String slug) {
        JsonNode r = call(Q_CATEGORY_BY_SLUG, vars("slug", slug));
        JsonNode edges = r.path("data").path("categories").path("edges");
        return edges.isArray() && edges.size() > 0 ? edges.get(0).path("node").path("id").asText() : null;
    }

    /* --------------------------- 1) EXPORT to JSON --------------------------- */
    public String exportCategories() throws Exception {
        ArrayNode out = mapper().createArrayNode();
        List<ObjectNode> cats = listAllCategories();

        for (ObjectNode c : cats) {
            String slug = c.path("slug").asText();
            if (DEFAULT_CATEGORY_SLUG.equals(slug)) {
                // Skip the built-in default category
                continue;
            }
            String name = c.path("name").asText();
            String description = c.path("description").isMissingNode() || c.path("description").isNull()
                    ? null : c.path("description").asText();
            String parentSlug = c.path("parent").isNull() ? null : c.path("parent").path("slug").asText(null);

            // If parent is default-category, omit it so we don't create it on import
            if (DEFAULT_CATEGORY_SLUG.equals(parentSlug)) parentSlug = null;

            ObjectNode row = mapper().createObjectNode();
            row.put("name", name);
            row.put("slug", slug);
            if (parentSlug != null) row.put("parentSlug", parentSlug); else row.putNull("parentSlug");
            if (description != null) row.put("description", description); else row.putNull("description");
            out.add(row);
        }
        return "OK  " + mapper().writeValueAsString(out);
    }

    /* -------------------- 2) DELETE ONLY SELECTED BY SLUG -------------------- */
    public String deleteAllCategories(List<String> slugsToDelete) {
        if (slugsToDelete == null || slugsToDelete.isEmpty()) return "Nothing to delete.";

        // Fetch all categories once
        List<ObjectNode> cats = listAllCategories();
        if (cats.isEmpty()) return "No categories to delete.";

        // Build maps
        Map<String, String> slugToId = new HashMap<>();
        Map<String, String> idToParentId = new HashMap<>();
        for (ObjectNode c : cats) {
            String id = c.path("id").asText();
            String slug = c.path("slug").asText();
            String parentId = c.path("parent").isNull() ? null : c.path("parent").path("id").asText(null);
            slugToId.put(slug, id);
            idToParentId.put(id, parentId);
        }

        // Filter only requested, skip default-category
        List<String> targetSlugs = slugsToDelete.stream()
                .filter(s -> !DEFAULT_CATEGORY_SLUG.equals(s))
                .filter(slugToId::containsKey)
                .collect(Collectors.toList());
        if (targetSlugs.isEmpty()) return "Nothing to delete.";

        // Compute depth in tree to delete deepest first
        Map<String, Integer> depth = new HashMap<>();
        for (String slug : targetSlugs) {
            String id = slugToId.get(slug);
            int d = 0; String cur = id;
            while (idToParentId.get(cur) != null) { d++; cur = idToParentId.get(cur); }
            depth.put(slug, d);
        }

        targetSlugs.sort(Comparator.comparingInt((String s) -> depth.getOrDefault(s, 0)).reversed());

        int deleted = 0, skipped = 0, errors = 0;
        for (String slug : targetSlugs) {
            String id = slugToId.get(slug);
            if (id == null) { skipped++; continue; }

            JsonNode r = call(M_CATEGORY_DELETE, vars("id", id));
            JsonNode errs = r.path("data").path("categoryDelete").path("errors");
            if (errs.isArray() && errs.size() > 0) {
                System.out.println("⚠️ categoryDelete error for slug=" + slug + " : " + errs);
                errors++;
            } else {
                deleted++;
            }
        }
        return "OK  ";
    }

    /* ------------- 3) IMPORT from JSON STRING (create or update) ------------- */
    /**
     * @param json the JSON string representing an array of rows:
     *             [{ "name": "...", "slug": "...", "parentSlug": "optional", "description": "optional" }, ...]
     */
    public String importCategories(String json) throws Exception {
        if (json == null || json.isBlank()) return "Invalid JSON: empty input.";

        JsonNode root = mapper().readTree(json);
        if (!root.isArray()) return "Invalid JSON: expected an array.";

        // Load rows, skipping any default-category lines if present
        List<ObjectNode> rows = new ArrayList<>();
        for (JsonNode n : root) {
            String slug = n.path("slug").asText();
            if (DEFAULT_CATEGORY_SLUG.equals(slug)) {
                System.out.println("⚠️ Skipping default-category in import payload");
                continue;
            }
            rows.add((ObjectNode) n);
        }
        if (rows.isEmpty()) return "Nothing to import (after skipping default-category).";

        Map<String, ObjectNode> bySlug = rows.stream()
                .collect(Collectors.toMap(n -> n.path("slug").asText(), n -> (ObjectNode) n));

        Map<String, String> knownSlugToId = new HashMap<>();
        // Seed with default-category id, if present
        String defaultCatId = categoryIdBySlug(DEFAULT_CATEGORY_SLUG);
        if (defaultCatId != null) knownSlugToId.put(DEFAULT_CATEGORY_SLUG, defaultCatId);

        // Multi-pass resolution to honor parent dependencies
        Set<String> pending = new HashSet<>(bySlug.keySet());
        int created = 0, updated = 0, skipped = 0, errors = 0;
        int safety = rows.size() * 5;

        while (!pending.isEmpty() && safety-- > 0) {
            Iterator<String> it = pending.iterator();
            boolean progressed = false;

            while (it.hasNext()) {
                String slug = it.next();
                ObjectNode row = bySlug.get(slug);

                // Resolve parent first (used only on CREATE; categoryUpdate doesn't take parent)
                String parentSlug = row.path("parentSlug").isNull() ? null : row.path("parentSlug").asText(null);
                if (DEFAULT_CATEGORY_SLUG.equals(parentSlug)) parentSlug = null;

                String parentId = null;
                if (parentSlug != null) {
                    parentId = knownSlugToId.get(parentSlug);
                    if (parentId == null) {
                        parentId = categoryIdBySlug(parentSlug);
                        if (parentId != null) knownSlugToId.put(parentSlug, parentId);
                        else continue; // wait until parent is created/resolved
                    }
                }

                // Check if this slug already exists in Saleor
                String existingId = knownSlugToId.get(slug);
                if (existingId == null) {
                    existingId = categoryIdBySlug(slug);
                    if (existingId != null) knownSlugToId.put(slug, existingId);
                }

                // Build CategoryInput (common fields)
                ObjectNode input = mapper().createObjectNode();
                String name = row.path("name").asText();
                if (name != null && !name.isBlank()) input.put("name", name);

                if (row.hasNonNull("slug")) input.put("slug", slug);

                String desc = row.path("description").isNull() ? null : row.path("description").asText(null);
                if (desc != null && !desc.isBlank()) input.put("description", desc);

                if (existingId == null) {
                    // CREATE (parent is a separate arg)
                    JsonNode resp = call(M_CATEGORY_CREATE, vars("input", input, "parent", parentId));
                    JsonNode payload = resp.path("data").path("categoryCreate");
                    JsonNode errs = payload.path("errors");
                    if (errs.isArray() && errs.size() > 0) {
                        System.out.println("❌ categoryCreate error for slug=" + slug + " : " + errs);
                        errors++;
                    } else {
                        String newId = payload.path("category").path("id").asText();
                        knownSlugToId.put(slug, newId);
                        created++;
                    }
                } else {
                    // UPDATE (cannot change parent here)
                    JsonNode resp = call(M_CATEGORY_UPDATE, vars("id", existingId, "input", input));
                    JsonNode payload = resp.path("data").path("categoryUpdate");
                    JsonNode errs = payload.path("errors");
                    if (errs.isArray() && errs.size() > 0) {
                        System.out.println("❌ categoryUpdate error for slug=" + slug + " : " + errs);
                        errors++;
                    } else {
                        updated++;
                    }
                }

                it.remove();
                progressed = true;
            }

            if (!progressed) break; // unresolved parents / bad refs
        }

        if (!pending.isEmpty()) {
            System.out.println("⚠️ Could not process some categories due to missing parents: " + pending);
        }

        return "OK  ";
    }
}
