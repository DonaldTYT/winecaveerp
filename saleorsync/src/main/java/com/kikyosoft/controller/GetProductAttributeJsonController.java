package com.kikyosoft.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.kikyosoft.config.SaleorConfig;

import java.io.File;
import java.nio.file.Files;
import java.util.*;


@RestController
public class GetProductAttributeJsonController {
	@Autowired
	private SaleorConfig config;

//    private static final String LIVE_GRAPHQL_URL = "http://192.168.19.212:8000/graphql/";
//   private static final String LIVE_APP_TOKEN = "4AMKpceD8GuqpSMKGNR5w2Gb5GzApi"; // with MANAGE_PRODUCT_TYPES_AND_ATTRIBUTES
//    private static final String LIVE_GRAPHQL_URL = "http://192.168.19.212:8000/graphql/";
//    private static final String LIVE_APP_TOKEN = "80ipBW8C2AoqObq8qaWYzpU2yDIT7E";
    private static final String OUTPUT_FILE = "/tmp/product_attributes.json";

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    private static final String QUERY = " query { attributes(first: 100) { edges { node { id name slug type inputType entityType valueRequired visibleInStorefront filterableInStorefront filterableInDashboard choices(first: 100) { edges { node { name slug } } } } } } } ";

    @GetMapping("/oadmin/GetProductAttributeJson")
    public ResponseEntity<String> exportAttributes() {
        try {
            JsonNode data = execGraphQL(/* LIVE_GRAPHQL_URL  , LIVE_APP_TOKEN */ QUERY, Map.of());
            JsonNode edges = data.path("data").path("attributes").path("edges");

            if (!edges.isArray()) {
                return ResponseEntity.status(500).body("Unexpected response: " + data);
            }

            List<JsonNode> attributes = new ArrayList<>();
            for (JsonNode edge : edges) {
                attributes.add(edge.path("node"));
            }

            // Save JSON to file
            File file = new File(OUTPUT_FILE);
            om.writerWithDefaultPrettyPrinter().writeValue(file, attributes);

            return ResponseEntity.ok("Exported " + attributes.size() + " attributes to " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error exporting attributes: " + e.getMessage());
        }
    }

    private JsonNode execGraphQL(/* String url, String token, */ String query, Map<String, Object> vars) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("query", query);
        body.put("variables", vars);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "Bearer " + token);
        headers.set("Authorization", config.getAuthHeader());

        ResponseEntity<String> res = rest.exchange(config.getApiUrl(), HttpMethod.POST,
                new HttpEntity<>(om.writeValueAsString(body), headers), String.class);

        return om.readTree(res.getBody());
    }
}
