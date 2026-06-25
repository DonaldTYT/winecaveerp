package com.kikyosoft.controller;

import com.kikyosoft.config.SaleorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/oadmin")
public class GetProductTypeJsonController {

 @Autowired
 private SaleorConfig config;

 private final RestTemplate restTemplate = new RestTemplate();

 @GetMapping("/GetProductTypeJson")
 public ResponseEntity<String> getProductTypeJson() {
     try {
         String query = " query { productTypes(first: 100) { edges { node { id name slug hasVariants isShippingRequired taxClass { name } productAttributes { name slug inputType } variantAttributes { name slug inputType } } } } } ";

         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         headers.set("Authorization", config.getAuthHeader());

         HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("query", query), headers);
         ResponseEntity<Map> response = restTemplate.exchange(config.getApiUrl(), HttpMethod.POST, entity, Map.class);

         Object data = ((Map<String, Object>) response.getBody().get("data")).get("productTypes");

         ObjectMapper mapper = new ObjectMapper();
         mapper.writerWithDefaultPrettyPrinter().writeValue(new File("/tmp/product_types.json"), data);

         return ResponseEntity.ok("✅ Saved product types to product_types.json");
     } catch (Exception e) {
         return ResponseEntity.status(500).body("Error: " + e.getMessage());
     }
 }
}
