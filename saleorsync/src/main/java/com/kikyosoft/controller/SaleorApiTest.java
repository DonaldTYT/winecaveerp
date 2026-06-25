package com.kikyosoft.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
public class SaleorApiTest {

    private static final String SALEOR_GRAPHQL_URL = "http://192.168.46.104:8000/graphql/";
    private static final String APP_TOKEN = "vdbkrq6BwIwH0ko6p4TFFrrrJjzHkM";
//    private static final String SALEOR_GRAPHQL_URL = "http://192.168.19.212:8000/graphql/";
//    private static final String APP_TOKEN = "80ipBW8C2AoqObq8qaWYzpU2yDIT7E";

    @GetMapping("/test-saleor")
    public ResponseEntity<String> testSaleorApi() {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // GraphQL query
            String graphqlQuery = "{ \"query\": \"{ shop { name domain { host } } }\" }";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + APP_TOKEN);

            HttpEntity<String> entity = new HttpEntity<>(graphqlQuery, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    SALEOR_GRAPHQL_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Return Saleor API response to browser
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error calling Saleor API: " + e.getMessage());
        }
    }
}
