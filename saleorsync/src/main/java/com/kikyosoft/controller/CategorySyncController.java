package com.kikyosoft.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.kikyosoft.config.SaleorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class CategorySyncController {
	@Configuration
	public class HttpClientConfig {

	    @Bean
	    public RestTemplate restTemplate(RestTemplateBuilder builder) {
	        return builder
	            .setConnectTimeout(Duration.ofSeconds(10))
	            .setReadTimeout(Duration.ofSeconds(60))
	            .build();
	    }
	}
    private static final String DEFAULT_CATEGORY_SLUG = "default-category";

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

    private static final String M_CATEGORY_CREATE = ""
            + "mutation($input: CategoryInput!){\n"
            + "  categoryCreate(input:$input){ category{ id slug } errors{ field message code } }\n"
            + "}";

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
    @GetMapping("/GetCategoryJson")
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

        File f = new File("/tmp/categories.json");
        mapper().writerWithDefaultPrettyPrinter().writeValue(f, out);
        return "Exported " + out.size() + " categories (skipped default-category) → " + f.getAbsolutePath();
    }

    /* --------------------------- 2) DELETE ALL SAFE -------------------------- */
    @GetMapping("/DeleteAllCategory")
    public String deleteAllCategories() {
        List<ObjectNode> cats = listAllCategories();
        if (cats.isEmpty()) return "No categories to delete.";

        // Build parent map
        Map<String, String> idToParentId = new HashMap<>();
        for (ObjectNode c : cats) {
            String id = c.path("id").asText();
            String parentId = c.path("parent").isNull() ? null : c.path("parent").path("id").asText(null);
            idToParentId.put(id, parentId);
        }

        // Compute depth (distance to root) to delete deepest first
        Map<String, Integer> depth = new HashMap<>();
        for (ObjectNode c : cats) {
            String id = c.path("id").asText();
            int d = 0; String cur = id;
            while (idToParentId.get(cur) != null) { d++; cur = idToParentId.get(cur); }
            depth.put(id, d);
        }

        cats.sort(Comparator.comparingInt(a -> -depth.getOrDefault(a.path("id").asText(), 0)));

        int deleted = 0, skipped = 0, errors = 0;
        for (ObjectNode c : cats) {
            String id = c.path("id").asText();
            String slug = c.path("slug").asText();

            // ✅ Skip built-in default category
            if (DEFAULT_CATEGORY_SLUG.equals(slug)) {
                System.out.println("⚠️ Skipping built-in default category");
                skipped++;
                continue;
            }

            JsonNode r = call(M_CATEGORY_DELETE, vars("id", id));
            JsonNode errs = r.path("data").path("categoryDelete").path("errors");
            if (errs.isArray() && errs.size() > 0) {
                System.out.println("⚠️ categoryDelete error for slug=" + slug + " : " + errs);
                errors++;
            } else {
                deleted++;
            }
        }
        return String.format("Deleted %d categories. Skipped(default): %d, Errors: %d", deleted, skipped, errors);
    }

    /* --------------------------- 3) IMPORT from JSON ------------------------- */
    @GetMapping("/PutCategoryJson")
    public String importCategories() throws Exception {
        File f = new File("/tmp/categories.json");
        if (!f.exists()) return "File not found: " + f.getAbsolutePath();

        JsonNode root = mapper().readTree(Files.readAllBytes(f.toPath()));
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
                .collect(Collectors.toMap(n -> n.path("slug").asText(), n -> n));

        Map<String, String> createdSlugToId = new HashMap<>();
        int created = 0, skipped = 0, errors = 0;

        // Seed map with existing default-category id if present
        String defaultCatId = categoryIdBySlug(DEFAULT_CATEGORY_SLUG);
        if (defaultCatId != null) createdSlugToId.put(DEFAULT_CATEGORY_SLUG, defaultCatId);

        Set<String> pending = new HashSet<>(bySlug.keySet());
        int safety = rows.size() * 5;

        while (!pending.isEmpty() && safety-- > 0) {
            Iterator<String> it = pending.iterator();
            boolean progressed = false;

            while (it.hasNext()) {
                String slug = it.next();
                ObjectNode row = bySlug.get(slug);

                // Parent resolution
                String parentSlug = row.path("parentSlug").isNull() ? null : row.path("parentSlug").asText(null);
                if (DEFAULT_CATEGORY_SLUG.equals(parentSlug)) parentSlug = null; // never attach to default on import

                if (parentSlug != null && !createdSlugToId.containsKey(parentSlug)) {
                    String existingParentId = categoryIdBySlug(parentSlug);
                    if (existingParentId != null) {
                        createdSlugToId.put(parentSlug, existingParentId);
                    } else {
                        continue; // wait until parent is created
                    }
                }

                // Skip if already exists (by slug)
                String existingId = categoryIdBySlug(slug);
                if (existingId != null) {
                    createdSlugToId.put(slug, existingId);
                    skipped++;
                    it.remove();
                    progressed = true;
                    continue;
                }

                // Build input
                ObjectNode input = mapper().createObjectNode();
                input.put("name", row.path("name").asText());
                input.put("slug", slug);

                String desc = row.path("description").isNull() ? null : row.path("description").asText(null);
                if (desc != null && !desc.isBlank()) input.put("description", desc);
                if (parentSlug != null) input.put("parent", createdSlugToId.get(parentSlug));

                JsonNode resp = call(M_CATEGORY_CREATE, vars("input", input));
                JsonNode payload = resp.path("data").path("categoryCreate");
                JsonNode errs = payload.path("errors");
                if (errs.isArray() && errs.size() > 0) {
                    System.out.println("❌ categoryCreate error for slug=" + slug + " : " + errs);
                    errors++;
                } else {
                    String newId = payload.path("category").path("id").asText();
                    createdSlugToId.put(slug, newId);
                    created++;
                }

                it.remove();
                progressed = true;
            }

            if (!progressed) break; // unresolved parents / bad refs
        }

        if (!pending.isEmpty()) {
            System.out.println("⚠️ Could not create some categories due to missing parents: " + pending);
        }

        return String.format(
                "Import done. Created: %d, Skipped(existing/default): %d, Errors: %d, Remaining: %d",
                created, skipped, errors, pending.size()
        );
    }
}
