package com.kikyosoft.controller;

import com.kikyosoft.config.SaleorConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;

@RestController
public class PutProductAttributeJsonController {
	@Autowired
	private SaleorConfig config;
//    private static final String DEV_GRAPHQL_URL = "http://192.168.46.104:8000/graphql/";
//    private static final String DEV_APP_TOKEN = "vdbkrq6BwIwH0ko6p4TFFrrrJjzHkM"; // with MANAGE_PRODUCT_TYPES_AND_ATTRIBUTES
    private static final String INPUT_FILE = "/tmp/product_attributes.json";

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    private static final String MUTATION = " mutation CreateAttr($input: AttributeCreateInput!) { attributeCreate(input: $input) { attribute { id name slug } errors { field message code } } } ";

    @GetMapping("/oadmin/PutProductAttributeJson")
    public ResponseEntity<String> importAttributes() {
        try {
            File file = new File(INPUT_FILE);
            if (!file.exists()) {
                return ResponseEntity.status(404).body("File not found: " + INPUT_FILE);
            }

            JsonNode list = om.readTree(file);
            int created = 0;

            for (JsonNode attr : list) {
                String name = attr.path("name").asText();
                String slug = attr.path("slug").asText();
                String type = attr.path("type").asText();
                String inputType = attr.path("inputType").asText();

                // Collect choice values
                List<Map<String, Object>> values = new ArrayList<>();
                JsonNode edges = attr.path("choices").path("edges");
                if (edges.isArray()) {
                    for (JsonNode edge : edges) {
                        JsonNode node = edge.path("node");
                        values.add(Map.of("name", node.path("name").asText()));
                    }
                }

                Map<String, Object> input = new HashMap<>();
                input.put("name", name);
                input.put("slug", slug);
                input.put("type", type);
                input.put("inputType", inputType);
                input.put("values", values);

                JsonNode res = execGraphQL(/* DEV_GRAPHQL_URL, DEV_APP_TOKEN, */ MUTATION, Map.of("input", input));
                JsonNode errors = res.path("data").path("attributeCreate").path("errors");

                if (errors.isArray() && errors.size() > 0) {
                    System.err.println("[!] Failed for " + name + " → " + errors);
                } else {
                    System.out.println("[+] Created " + name);
                    created++;
                }
            }

            return ResponseEntity.ok("Imported " + created + " attributes from " + INPUT_FILE);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error importing attributes: " + e.getMessage());
        }
    }

    private JsonNode execGraphQL(/* String url, String token, */ String query, Map<String, Object> vars) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("query", query);
        body.put("variables", vars);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", config.getAuthHeader());

        ResponseEntity<String> res = rest.exchange(config.getApiUrl(), HttpMethod.POST,
                new HttpEntity<>(om.writeValueAsString(body), headers), String.class);

        return om.readTree(res.getBody());
    }
}
