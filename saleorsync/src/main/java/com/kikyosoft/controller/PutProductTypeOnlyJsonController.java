package com.kikyosoft.controller;

import com.kikyosoft.config.SaleorConfig;
import com.fasterxml.jackson.databind.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/oadmin")
public class PutProductTypeOnlyJsonController {

 @Autowired
 private SaleorConfig config;

 private final RestTemplate restTemplate = new RestTemplate();

 @GetMapping("/PutProductTypeOnlyJson")
 public ResponseEntity<String> putProductTypes() {
     try {
         ObjectMapper mapper = new ObjectMapper();
         JsonNode root = mapper.readTree(new File("/tmp/product_types.json"));
         JsonNode edges = root.get("edges");

         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         headers.set("Authorization", config.getAuthHeader());

         for (JsonNode edge : edges) {
             JsonNode node = edge.get("node");
             String name = node.get("name").asText();
             String slug = node.get("slug").asText();
             boolean hasVariants = node.get("hasVariants").asBoolean();
             boolean isShippingRequired = node.get("isShippingRequired").asBoolean();

             String mutation = String.format(" mutation { productTypeCreate(input: { name: \"%s\", slug: \"%s\", hasVariants: %s, isShippingRequired: %s }) { productType { id name slug } errors { field message } } } ", name, slug, hasVariants, isShippingRequired);

             HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("query", mutation), headers);
             restTemplate.exchange(config.getApiUrl(), HttpMethod.POST, entity, String.class);
         }

         return ResponseEntity.ok("✅ Reimported all product types from product_types.json");
     } catch (Exception e) {
         return ResponseEntity.status(500).body("Error importing product types: " + e.getMessage());
     }
 }
}
