package com.kikyosoft.api;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class ProductRestController {
    private final ProductCatalogService catalog;
    private final ProductApiAccess access;

    public ProductRestController(ProductCatalogService catalog, ProductApiAccess access) {
        this.catalog = catalog; this.access = access;
    }

    @GetMapping("/products")
    public JsonNode products(@RequestParam(required=false) Integer first, @RequestParam(required=false) String after,
            @RequestParam(required=false) String search, @RequestParam(required=false) String channel,
            HttpServletRequest request) {
        access.requireClientAccess(request); return catalog.listProducts(first, after, search, channel);
    }

    @GetMapping("/product")
    public JsonNode product(@RequestParam(required=false) String id, @RequestParam(required=false) String slug,
            @RequestParam(required=false) String channel, HttpServletRequest request) {
        access.requireClientAccess(request); return catalog.getProduct(id, slug, channel);
    }

    @GetMapping("/variants")
    public JsonNode variants(@RequestParam(required=false) Integer first, @RequestParam(required=false) String after,
            @RequestParam(required=false) String channel, HttpServletRequest request) {
        access.requireClientAccess(request); return catalog.listVariants(first, after, channel);
    }

    @GetMapping("/variant")
    public JsonNode variant(@RequestParam(required=false) String id, @RequestParam(required=false) String sku,
            @RequestParam(required=false) String channel, HttpServletRequest request) {
        access.requireClientAccess(request); return catalog.getVariant(id, sku, channel);
    }

    @GetMapping("/product-types")
    public JsonNode productTypes(@RequestParam(required=false) Integer first, @RequestParam(required=false) String after,
            HttpServletRequest request) {
        access.requireClientAccess(request); return catalog.listProductTypes(first, after);
    }

    @GetMapping("/product-type")
    public JsonNode productType(@RequestParam String id, HttpServletRequest request) {
        access.requireClientAccess(request); return catalog.getProductType(id);
    }

    @GetMapping("/attributes")
    public JsonNode attributes(@RequestParam(required=false) Integer first, @RequestParam(required=false) String after,
            @RequestParam(required=false) String search, @RequestParam(required=false) String channel,
            HttpServletRequest request) {
        access.requireClientAccess(request); return catalog.listAttributes(first, after, search, channel);
    }

    @GetMapping("/attribute")
    public JsonNode attribute(@RequestParam(required=false) String id, @RequestParam(required=false) String slug,
            HttpServletRequest request) {
        access.requireClientAccess(request); return catalog.getAttribute(id, slug);
    }
}
