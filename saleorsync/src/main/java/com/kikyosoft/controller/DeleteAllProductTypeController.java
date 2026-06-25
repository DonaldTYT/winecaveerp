package com.kikyosoft.controller;

import com.kikyosoft.config.SaleorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@RequestMapping("/oadmin")
public class DeleteAllProductTypeController {

 @Autowired
 private SaleorConfig config;

 private final RestTemplate restTemplate = new RestTemplate();

 @GetMapping("/DeleteAllProductType")
 public ResponseEntity<String> deleteAllProductTypes() {
     try {
         // Step 1: Get all product type IDs
         String query = " query { productTypes(first: 100) { edges { node { id name } } } } ";

         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         headers.set("Authorization", config.getAuthHeader());
         HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("query", query), headers);

         ResponseEntity<Map> response = restTemplate.exchange(
                 config.getApiUrl(),
                 HttpMethod.POST,
                 entity,
                 Map.class
         );

         var edges = ((Map<String, Object>) ((Map<String, Object>) response.getBody().get("data")).get("productTypes")).get("edges");
         var jsonEdges = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().writeValueAsString(edges);
         var ids = new com.fasterxml.jackson.databind.ObjectMapper().readTree(jsonEdges);

         // Step 2: Delete all product types
         StringBuilder mutation = new StringBuilder("mutation {\n  productTypeBulkDelete(ids: [");
         for (var node : ids) {
             String id = node.get("node").get("id").asText();
             mutation.append("\"").append(id).append("\",");
         }
         mutation.append("]) { count errors { field message } }\n}");

         HttpEntity<Map<String, String>> deleteEntity = new HttpEntity<>(Map.of("query", mutation.toString()), headers);
         ResponseEntity<String> deleteResponse = restTemplate.exchange(config.getApiUrl(), HttpMethod.POST, deleteEntity, String.class);

         return ResponseEntity.ok(deleteResponse.getBody());
     } catch (Exception e) {
         return ResponseEntity.status(500).body("Error deleting product types: " + e.getMessage());
     }
 }
}
