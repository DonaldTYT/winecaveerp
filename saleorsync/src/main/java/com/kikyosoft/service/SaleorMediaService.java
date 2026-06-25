package com.kikyosoft.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kikyosoft.config.SaleorConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * Service you can inject and call directly from your sync routine.
 *
 * addImage(product_slug, image_url, image_type)
 *  - First tries Saleor productMediaCreate(mediaUrl: <image_url>)
 *  - If Saleor rejects the URL ("Unsupported media provider or incorrect URL"),
 *    it falls back to downloading the bytes here and uploading via GraphQL Upload.
 *
 * application.properties required:
 *   saleor.graphql.url=http://127.0.0.1:8000/graphql/
 *   saleor.api.token=REDACTED
 */
@Service
public class SaleorMediaService {

    @Autowired
    private SaleorConfig config;

    @javax.annotation.PostConstruct
    void checkConfig() {
        String url = graphqlUrl();
        String token = apiToken();
        org.springframework.util.Assert.hasText(url, "config.getApiUrl() returned empty");
        org.springframework.util.Assert.hasText(token, "config.getToken() returned empty");
    }

    private final RestTemplate rest;
    private final ObjectMapper om = new ObjectMapper();

    
    
    public SaleorMediaService(RestTemplateBuilder builder) {
        this.rest = builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    // -- Config getters wired to your SaleorConfig
    // TODO: add the proper import for your SaleorConfig class if it's in a different package
    private String graphqlUrl() { return java.util.Objects.toString(config.getApiUrl(), ""); }
    private String apiToken()   { return java.util.Objects.toString(config.getToken(),   ""); }

    private static final String Q_PRODUCT_BY_SLUG =
            "query ($slug: String!) { product(slug: $slug) { id slug name } }";

    private static final String M_MEDIA_CREATE_BY_URL =
            "mutation ($productId: ID!, $imgUrl: String!, $alt: String) {\n" +
            "  productMediaCreate(input: { product: $productId, mediaUrl: $imgUrl, alt: $alt }) {\n" +
            "    media { id url type }\n" +
            "    errors { field message code }\n" +
            "  }\n" +
            "}";

    private static final String M_MEDIA_CREATE_BY_UPLOAD =
            "mutation ($productId: ID!, $file: Upload!, $alt: String) {\n" +
            "  productMediaCreate(input: { product: $productId, image: $file, alt: $alt }) {\n" +
            "    media { id url type }\n" +
            "    errors { field message code }\n" +
            "  }\n" +
            "}";
    
    
    private static final String Q_PRODUCT_MEDIA =
            "query ($id: ID!) {\n" +
            "  product(id: $id) {\n" +
            "    media {\n" +
            "      id\n" +
            "      url\n" +
            "      alt\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String M_MEDIA_UPDATE =
            "mutation ($id: ID!, $alt: String) {\n" +
            "  productMediaUpdate(id: $id, input: { alt: $alt }) {\n" +
            "    media { id alt }\n" +
            "    errors { field message code }\n" +
            "  }\n" +
            "}";

    public MediaResult addImage(String productSlug, String imageUrl, String imageType,String alt) {
        try {
            Assert.hasText(productSlug, "product_slug is required");
            Assert.hasText(imageUrl, "image_url is required");
            String ext = normalizeExt(imageType);

            // 1) Resolve product id
            String productId = fetchProductIdBySlug(productSlug);
            if (productId == null) {
                return MediaResult.fail("Product not found for slug: " + productSlug);
            }

//            String alt = buildAlt(productSlug, ext);

            // 2) Try URL-based create first (fast path, lets Saleor host thumbnails)
            MediaResult byUrl = createMediaByUrl(productId, imageUrl, alt);
            // If URL path succeeds, return; otherwise always try upload fallback
            if (byUrl.ok) {
                return byUrl.withProduct(productSlug, productId);
            }

            // 3) Fallback: download bytes and upload via multipart (works even if Saleor cannot reach imageUrl)
            byte[] bytes = downloadBytes(imageUrl);
            if (bytes == null || bytes.length == 0) {
                return MediaResult.fail("Failed to download image bytes from: " + imageUrl);
            }
            String filename = safeFilename(productSlug, ext);
            String contentType = contentTypeFor(ext);
            MediaResult byUpload = createMediaByUpload(productId, bytes, filename, contentType, alt);
            byUpload.uploadFallback = true;
            return byUpload.withProduct(productSlug, productId);

        } catch (HttpStatusCodeException sce) {
            return MediaResult.fail("HTTP " + sce.getStatusCode() + ": " + sce.getResponseBodyAsString());
        } catch (Exception e) {
            return MediaResult.fail(e.getMessage());
        }
    }
    
    public String queryMediaySlug(String productSlug) {
        try {
            Assert.hasText(productSlug, "productSlug is required");

            String productId = fetchProductIdBySlug(productSlug);
            if (productId == null) {
                return null;
            }

            List<JsonNode> mediaList = fetchProductMedia(productId);
            if (mediaList == null || mediaList.isEmpty()) {
                return null;
            }
            return(mediaList.toString());
        } catch (Exception e) {
            return null;
        }
    }
    public int updateAltBySlug(String productSlug, String newAlt, String matchUrl) {
        try {
            Assert.hasText(productSlug, "productSlug is required");

            String productId = fetchProductIdBySlug(productSlug);
            if (productId == null) {
                return 0;
            }

            List<JsonNode> mediaList = fetchProductMedia(productId);
            if (mediaList == null || mediaList.isEmpty()) {
                return 0;
            }

            String matchToken = (matchUrl != null && !matchUrl.trim().isEmpty())
                    ? matchUrl.trim()
                    : null;

            int updatedCount = 0;

            for (JsonNode media : mediaList) {
                String mediaId = media.path("id").asText();
                String url = media.path("url").asText("");

                boolean matched;
                if (matchToken != null) {
                    matched = url.contains(matchToken);
                } else {
                    matched = true; // ✅ update ALL media
                }

                if (matched) {
                    if (updateMediaAlt(mediaId, newAlt)) {
                        updatedCount++;
                    }
                }
            }

            return updatedCount;

        } catch (Exception e) {
            return 0;
        }
    }

    // ---- Helpers ----
    
    private String fetchProductIdBySlug(String slug) throws Exception {
        GraphQLRequest req = new GraphQLRequest(Q_PRODUCT_BY_SLUG, mapOf("slug", slug));
        ResponseEntity<String> raw = postGraphQL(req);
        JsonNode root = om.readTree(raw.getBody());
        if (root.has("errors")) {
            throw new RuntimeException("GraphQL errors: " + root.get("errors").toString());
        }
        JsonNode prod = root.path("data").path("product");
        return prod.isMissingNode() || prod.isNull() ? null : prod.path("id").asText();
    }

    private MediaResult createMediaByUrl(String productId, String imgUrl, String alt) throws Exception {
        GraphQLRequest req = new GraphQLRequest(M_MEDIA_CREATE_BY_URL, mapOf(
                "productId", productId,
                "imgUrl", imgUrl,
                "alt", alt
        ));
        ResponseEntity<String> raw = postGraphQL(req);
        return parseMediaCreate(raw.getBody());
    }

    private MediaResult createMediaByUpload(String productId, byte[] fileBytes, String filename, String contentType, String alt) throws Exception {
        // GraphQL multipart request per spec
        String operationsJson = om.writeValueAsString(mapOf(
                "query", M_MEDIA_CREATE_BY_UPLOAD,
                "variables", mapOf("productId", productId, "file", null, "alt", alt)
        ));
        String mapJson = om.writeValueAsString(mapOf("1", Collections.singletonList("variables.file")));

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("operations", new HttpEntity<>(operationsJson, jsonHeaders()));
        body.add("map", new HttpEntity<>(mapJson, jsonHeaders()));

        // File part with per-part headers (content type + filename)
        HttpHeaders fileHeaders = new HttpHeaders();
        if (contentType != null) fileHeaders.setContentType(MediaType.parseMediaType(contentType));
        fileHeaders.setContentDisposition(ContentDisposition.builder("form-data")
                .name("1")
                .filename(filename, StandardCharsets.UTF_8)
                .build());
        HttpEntity<org.springframework.core.io.ByteArrayResource> fileEntity = new HttpEntity<>(
                new ByteArrayResourceWithFilename(fileBytes, filename), fileHeaders);
        body.add("1", fileEntity);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(apiToken());

        ResponseEntity<String> raw = rest.exchange(graphqlUrl(), HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        return parseMediaCreate(raw.getBody());
    }

    private MediaResult parseMediaCreate(String json) throws Exception {
        JsonNode root = om.readTree(json);
        List<String> topErrors = new ArrayList<>();
        if (root.has("errors")) {
            for (JsonNode e : root.get("errors")) topErrors.add(e.path("message").asText());
        }
        JsonNode payload = root.path("data").path("productMediaCreate");
        if (payload.isMissingNode() || payload.isNull()) {
            return MediaResult.fail("Empty productMediaCreate payload" + (topErrors.isEmpty() ? "" : (": " + topErrors)));
        }
        List<String> fieldErrors = new ArrayList<>();
        if (payload.has("errors")) {
            for (JsonNode e : payload.get("errors")) fieldErrors.add(e.path("code").asText() + ": " + e.path("message").asText());
        }
        JsonNode media = payload.path("media");
        if (!fieldErrors.isEmpty()) {
            return MediaResult.errorList(fieldErrors);
        }
        if (media.isMissingNode() || media.isNull()) {
            return MediaResult.fail("Media not returned");
        }
        MediaResult r = new MediaResult();
        r.ok = true;
        r.mediaId = media.path("id").asText();
        r.mediaUrl = media.path("url").asText();
        r.mediaType = media.path("type").asText();
        return r;
    }
    
    private List<JsonNode> fetchProductMedia(String productId) throws Exception {
        GraphQLRequest req = new GraphQLRequest(Q_PRODUCT_MEDIA, mapOf("id", productId));
        ResponseEntity<String> raw = postGraphQL(req);

        JsonNode root = om.readTree(raw.getBody());
        if (root.has("errors")) {
            throw new RuntimeException("GraphQL errors: " + root.get("errors").toString());
        }

        JsonNode mediaArr = root.path("data").path("product").path("media");
        List<JsonNode> list = new ArrayList<>();

        if (mediaArr.isArray()) {
            for (JsonNode m : mediaArr) {
                list.add(m);
            }
        }
        return list;
    }
    
    private boolean updateMediaAlt(String mediaId, String newAlt) throws Exception {
        GraphQLRequest req = new GraphQLRequest(M_MEDIA_UPDATE, mapOf(
                "id", mediaId,
                "alt", newAlt
        ));

        ResponseEntity<String> raw = postGraphQL(req);
        JsonNode root = om.readTree(raw.getBody());

        if (root.has("errors")) {
            throw new RuntimeException("GraphQL errors: " + root.get("errors").toString());
        }

        JsonNode payload = root.path("data").path("productMediaUpdate");
        JsonNode errors = payload.path("errors");

        return errors.isMissingNode() || errors.isEmpty();
    }

    private ResponseEntity<String> postGraphQL(GraphQLRequest req) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(apiToken());
        return rest.exchange(graphqlUrl(), HttpMethod.POST, new HttpEntity<>(req, headers), String.class);
    }

    private byte[] downloadBytes(String url) {
        ResponseEntity<byte[]> resp = rest.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), byte[].class);
        if (resp.getStatusCode().is2xxSuccessful()) {
            return resp.getBody();
        }
        return null;
    }

//    private static String buildAlt(String slug, String ext) {
//        return ext == null ? slug : (slug + "." + ext);
//    }

    private static String normalizeExt(String imageType) {
        if (imageType == null) return null;
        String s = imageType.trim().toLowerCase(Locale.ROOT);
        if (s.startsWith(".")) s = s.substring(1);
        switch (s) {
            case "jpeg": return "jpg";
            case "jpg":
            case "png":
            case "webp": return s;
            default: return s; // keep as-is for rare types
        }
    }

    private static String contentTypeFor(String ext) {
        if (ext == null) return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        switch (ext) {
            case "jpg": return MediaType.IMAGE_JPEG_VALUE;
            case "png": return MediaType.IMAGE_PNG_VALUE;
            case "webp": return "image/webp";
            default: return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    private static String safeFilename(String slug, String ext) {
        String base = slug.replaceAll("[^a-zA-Z0-9-_]", "-");
        if (ext == null || ext.isEmpty()) return base;
        return base + "." + ext;
    }

    private static HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        return h;
    }

    private static Map<String, Object> mapOf(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put(kv[i].toString(), kv[i + 1]);
        return m;
    }

    // --- DTOs ---
    static class GraphQLRequest {
        public String query;
        public Map<String, Object> variables;
        GraphQLRequest(String q, Map<String, Object> v) { this.query = q; this.variables = v; }
    }

    public static class MediaResult {
        public boolean ok;
        public boolean uploadFallback; // true if we used Upload after URL failed
        public String productSlug;
        public String productId;
        public String mediaId;
        public String mediaUrl;
        public String mediaType; // IMAGE or VIDEO
        public List<String> errors;

        static MediaResult fail(String msg) {
            MediaResult r = new MediaResult();
            r.ok = false; r.errors = Collections.singletonList(msg); return r;
        }
        static MediaResult errorList(List<String> errs) {
            MediaResult r = new MediaResult();
            r.ok = false; r.errors = errs; return r;
        }
        MediaResult withProduct(String slug, String id) { this.productSlug = slug; this.productId = id; return this; }
    }

    // ByteArrayResource with filename and content-type for multipart upload
    static class ByteArrayResourceWithFilename extends org.springframework.core.io.ByteArrayResource {
        private final String filename;
        ByteArrayResourceWithFilename(byte[] bytes, String filename) {
            super(bytes);
            this.filename = filename;
        }
        @Override public String getFilename() { return filename; }
        @Override public long contentLength() { return this.getByteArray().length; }
        @Override public String getDescription() { return "ByteArrayResource(" + filename + ")"; }
    }
}
