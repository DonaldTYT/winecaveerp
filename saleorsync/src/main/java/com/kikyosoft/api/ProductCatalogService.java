package com.kikyosoft.api;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

@Service
public class ProductCatalogService {
    private static final int MAX_PAGE_SIZE = 100;

    private static final String PRODUCT_FIELDS =
            "id name slug seoTitle seoDescription created updatedAt " +
            "productType { id name slug kind hasVariants } " +
            "category { id name slug } " +
            "channelListings { channel { id name slug } } " +
            "productVariants(first: 100) { totalCount edges { node { id name sku } } } " +
            "pricing { displayGrossPrices priceRange { " +
            "start { gross { amount currency } net { amount currency } } " +
            "stop { gross { amount currency } net { amount currency } } } } " +
            "thumbnail(size: 512) { url alt } metadata { key value }";

    private static final String ATTRIBUTE_VALUE_FIELDS =
            "id name slug value inputType reference plainText richText boolean date dateTime externalReference " +
            "file { url contentType }";

    private static final String ATTRIBUTE_FIELDS =
            "id name slug type inputType entityType unit valueRequired visibleInStorefront " +
            "filterableInDashboard withChoices externalReference metadata { key value } " +
            "choices(first: 100) { pageInfo { hasNextPage endCursor } edges { node { " +
            ATTRIBUTE_VALUE_FIELDS + " } } }";

    private static final String VARIANT_FIELDS =
            "id name sku trackInventory quantityLimitPerCustomer quantityAvailable created updatedAt externalReference " +
            "product { id name slug metadata { key value } channelListings { channel { id name slug } } } metadata { key value } " +
            "attributes { attribute { id name slug inputType entityType } values { " + ATTRIBUTE_VALUE_FIELDS + " } } " +
            "channelListings { channel { id name slug currencyCode } price { amount currency } " +
            "costPrice { amount currency } priorPrice { amount currency } margin preorderThreshold { quantity soldUnits } } " +
            "stocks { id quantity quantityAllocated quantityReserved warehouse { id name slug } } " +
            "media { id alt type url(size: 1024) }";

    private static final String LIST_PRODUCTS =
            "query ProductApiListProducts($first:Int!,$after:String,$search:String,$channel:String){" +
            "products(first:$first,after:$after,search:$search,channel:$channel){" +
            "totalCount pageInfo{hasNextPage endCursor} edges{cursor node{" + PRODUCT_FIELDS + "}}}}";

    private static final String GET_PRODUCT =
            "query ProductApiGetProduct($id:ID,$slug:String,$channel:String){" +
            "product(id:$id,slug:$slug,channel:$channel){" + PRODUCT_FIELDS +
            " attributes { attribute { id name slug inputType entityType } values { " + ATTRIBUTE_VALUE_FIELDS + " } }}}";

    private static final String LIST_VARIANTS =
            "query ProductApiListVariants($first:Int!,$after:String,$channel:String){" +
            "productVariants(first:$first,after:$after,channel:$channel){" +
            "totalCount pageInfo{hasNextPage endCursor} edges{cursor node{" + VARIANT_FIELDS + "}}}}";

    private static final String GET_VARIANT =
            "query ProductApiGetVariant($id:ID,$sku:String,$channel:String){" +
            "productVariant(id:$id,sku:$sku,channel:$channel){" + VARIANT_FIELDS + "}}";

    private static final String PRODUCT_TYPE_FIELDS =
            "id name slug kind hasVariants isShippingRequired isDigital weight { value unit } metadata { key value } " +
            "productAttributes { id name slug type inputType entityType unit valueRequired visibleInStorefront withChoices } " +
            "assignedVariantAttributes { attribute { id name slug type inputType entityType unit valueRequired visibleInStorefront withChoices } variantSelection }";

    private static final String LIST_PRODUCT_TYPES =
            "query ProductApiListProductTypes($first:Int!,$after:String){" +
            "productTypes(first:$first,after:$after){totalCount pageInfo{hasNextPage endCursor} " +
            "edges{cursor node{" + PRODUCT_TYPE_FIELDS + "}}}}";

    private static final String GET_PRODUCT_TYPE =
            "query ProductApiGetProductType($id:ID!){productType(id:$id){" + PRODUCT_TYPE_FIELDS + "}}";

    private static final String LIST_ATTRIBUTES =
            "query ProductApiListAttributes($first:Int!,$after:String,$search:String,$channel:String){" +
            "attributes(first:$first,after:$after,search:$search,channel:$channel){" +
            "totalCount pageInfo{hasNextPage endCursor} edges{cursor node{" + ATTRIBUTE_FIELDS + "}}}}";

    private static final String GET_ATTRIBUTE =
            "query ProductApiGetAttribute($id:ID,$slug:String){attribute(id:$id,slug:$slug){" + ATTRIBUTE_FIELDS + "}}";

    private final SaleorProductClient client;
    private final ProductApiProperties properties;

    public ProductCatalogService(SaleorProductClient client, ProductApiProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    public JsonNode listProducts(Integer first, String after, String search, String channel) {
        Map<String, Object> vars = page(first, after);
        put(vars, "search", search); vars.put("channel", customerChannel());
        return filterConnection(client.execute("ProductApiListProducts", LIST_PRODUCTS, vars).path("products"), true);
    }

    public JsonNode getProduct(String id, String slug, String channel) {
        requireOne("id", id, "slug", slug);
        Map<String, Object> vars = new LinkedHashMap<>();
        put(vars, "id", id); put(vars, "slug", slug); vars.put("channel", customerChannel());
        JsonNode product = client.execute("ProductApiGetProduct", GET_PRODUCT, vars).path("product");
        return isCustomerVisibleProduct(product, customerChannel()) ? withProductCode(product) : NullNode.instance;
    }

    public JsonNode listVariants(Integer first, String after, String channel) {
        Map<String, Object> vars = page(first, after); vars.put("channel", customerChannel());
        return filterConnection(client.execute("ProductApiListVariants", LIST_VARIANTS, vars).path("productVariants"), false);
    }

    public JsonNode getVariant(String id, String sku, String channel) {
        requireOne("id", id, "sku", sku);
        Map<String, Object> vars = new LinkedHashMap<>();
        put(vars, "id", id); put(vars, "sku", sku); vars.put("channel", customerChannel());
        JsonNode variant = client.execute("ProductApiGetVariant", GET_VARIANT, vars).path("productVariant");
        return isCustomerVisibleVariant(variant, customerChannel()) ? withVariantProductCode(variant) : NullNode.instance;
    }

    public JsonNode listProductTypes(Integer first, String after) {
        return client.execute("ProductApiListProductTypes", LIST_PRODUCT_TYPES, page(first, after)).path("productTypes");
    }

    public JsonNode getProductType(String id) {
        require("id", id);
        return client.execute("ProductApiGetProductType", GET_PRODUCT_TYPE, Map.of("id", id)).path("productType");
    }

    public JsonNode listAttributes(Integer first, String after, String search, String channel) {
        Map<String, Object> vars = page(first, after);
        put(vars, "search", search); vars.put("channel", customerChannel());
        return client.execute("ProductApiListAttributes", LIST_ATTRIBUTES, vars).path("attributes");
    }

    public JsonNode getAttribute(String id, String slug) {
        requireOne("id", id, "slug", slug);
        Map<String, Object> vars = new LinkedHashMap<>(); put(vars, "id", id); put(vars, "slug", slug);
        return client.execute("ProductApiGetAttribute", GET_ATTRIBUTE, vars).path("attribute");
    }

    /**
     * Removes ERP-only catalogue records before they reach REST, MCP or chatbot callers.
     * Saleor's cursor is retained so callers can continue paging past filtered records.
     */
    private JsonNode filterConnection(JsonNode connection, boolean products) {
        if (!connection.isObject() || !connection.path("edges").isArray()) return connection;
        ObjectNode filtered = ((ObjectNode) connection).deepCopy();
        ArrayNode visibleEdges = filtered.arrayNode();
        for (JsonNode edge : connection.path("edges")) {
            JsonNode node = edge.path("node");
            boolean visible = products
                    ? isCustomerVisibleProduct(node, customerChannel())
                    : isCustomerVisibleVariant(node, customerChannel());
            if (visible) {
                ObjectNode edgeCopy = (ObjectNode) edge.deepCopy();
                JsonNode copiedNode = edgeCopy.path("node");
                if (products) withProductCode(copiedNode);
                else withVariantProductCode(copiedNode);
                visibleEdges.add(edgeCopy);
            }
        }
        filtered.set("edges", visibleEdges);
        // Do not reveal a total that includes hidden ERP-only products.
        filtered.put("totalCount", visibleEdges.size());
        return filtered;
    }

    private static boolean isCustomerVisibleProduct(JsonNode product, String channel) {
        return product.isObject()
                && hasChannelListing(product, channel)
                && product.path("productVariants").path("totalCount").asInt(0) > 0;
    }

    private static boolean isCustomerVisibleVariant(JsonNode variant, String channel) {
        JsonNode product = variant.path("product");
        return variant.isObject()
                && hasChannelListing(product, channel);
    }

    private static boolean hasChannelListing(JsonNode product, String channel) {
        JsonNode listings = product.path("channelListings");
        if (!listings.isArray()) return false;
        for (JsonNode listing : listings) {
            if (channel.equalsIgnoreCase(listing.path("channel").path("slug").asText())) return true;
        }
        return false;
    }

    private String customerChannel() {
        String channel = properties.getChannel();
        if (blank(channel)) throw new ProductApiException(500, "saleor.product-api.channel is not configured");
        return channel.trim();
    }

    /** Adds an explicit business name so consumers never confuse product code with variant SKU. */
    private static JsonNode withProductCode(JsonNode product) {
        if (!product.isObject()) return product;
        ObjectNode object = (ObjectNode) product;
        String productCode = metadataValue(object.path("metadata"), "icode");
        if (productCode != null) object.put("productCode", productCode);
        return object;
    }

    private static JsonNode withVariantProductCode(JsonNode variant) {
        if (variant.isObject()) withProductCode(variant.path("product"));
        return variant;
    }

    private static String metadataValue(JsonNode metadata, String key) {
        if (!metadata.isArray()) return null;
        for (JsonNode item : metadata) {
            if (key.equals(item.path("key").asText())) {
                String value = item.path("value").asText("").trim();
                return value.isEmpty() ? null : value;
            }
        }
        return null;
    }

    private static Map<String, Object> page(Integer requested, String after) {
        int first = requested == null ? 20 : requested;
        if (first < 1 || first > MAX_PAGE_SIZE) {
            throw new ProductApiException(400, "first must be between 1 and " + MAX_PAGE_SIZE);
        }
        Map<String, Object> vars = new LinkedHashMap<>(); vars.put("first", first); put(vars, "after", after);
        return vars;
    }

    private static void requireOne(String aName, String a, String bName, String b) {
        boolean hasA = !blank(a); boolean hasB = !blank(b);
        if (hasA == hasB) throw new ProductApiException(400, "Provide exactly one of " + aName + " or " + bName);
    }

    private static void require(String name, String value) {
        if (blank(value)) throw new ProductApiException(400, name + " is required");
    }

    private static void put(Map<String, Object> target, String key, String value) {
        if (!blank(value)) target.put(key, value.trim());
    }

    private static boolean blank(String value) { return value == null || value.trim().isEmpty(); }
}
