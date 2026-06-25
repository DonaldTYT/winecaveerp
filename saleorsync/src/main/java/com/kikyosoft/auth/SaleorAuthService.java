package com.kikyosoft.auth;

import com.kikyosoft.auth.dto.GraphqlRequest;
import com.kikyosoft.auth.dto.TokenAuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class SaleorAuthService {

    private final WebClient webClient;
    private final String graphqlUrl;

    public SaleorAuthService(
            WebClient.Builder webClientBuilder,
            @Value("${saleor.graphql-url}") String graphqlUrl) {
        this.webClient = webClientBuilder.build();
        this.graphqlUrl = graphqlUrl;
    }

    public TokenAuthResponse tokenCreate(String email, String password) {
        String mutation = "mutation TokenAuth($email: String!, $password: String!) { " +
                          "  tokenCreate(email: $email, password: $password) { " +
                          "    token " +
                          "    errors { field message } " +
                          "  } " +
                          "}";

        GraphqlRequest request = new GraphqlRequest(
                mutation,
                Map.of("email", email, "password", password)
        );

        return webClient.post()
                .uri(graphqlUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TokenAuthResponse.class)
                .onErrorResume(ex -> {
                    TokenAuthResponse fallback = new TokenAuthResponse();
                    TokenAuthResponse.GraphQLError err = new TokenAuthResponse.GraphQLError();
                    err.setMessage("HTTP/IO error: " + ex.getMessage());
                    fallback.setErrors(java.util.List.of(err));
                    return Mono.just(fallback);
                })
                .block();
    }
}
