package com.kikyosoft.controller;

import com.kikyosoft.auth.SaleorAuthService;
import com.kikyosoft.auth.dto.LoginRequest;
import com.kikyosoft.auth.dto.TokenAuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private final SaleorAuthService saleorAuthService;

    public LoginController(SaleorAuthService saleorAuthService) {
        this.saleorAuthService = saleorAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req.getEmail() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest().body(
                java.util.Map.of("error", "email and password are required")
            );
        }

        TokenAuthResponse resp = saleorAuthService.tokenCreate(req.getEmail(), req.getPassword());

        // If top-level GraphQL had errors
        if (resp.getErrors() != null && !resp.getErrors().isEmpty()) {
            return ResponseEntity.status(502).body(
                java.util.Map.of("graphQLErrors", resp.getErrors())
            );
        }

        TokenAuthResponse.TokenCreate tc = (resp.getData() != null) ? resp.getData().getTokenCreate() : null;

        if (tc == null) {
            return ResponseEntity.status(502).body(
                java.util.Map.of("error", "No tokenCreate payload in response")
            );
        }

        if (tc.getErrors() != null && !tc.getErrors().isEmpty()) {
            // Saleor returned field-level errors (e.g., bad credentials)
            return ResponseEntity.status(401).body(
                java.util.Map.of("errors", tc.getErrors())
            );
        }

        return ResponseEntity.ok(java.util.Map.of("token", tc.getToken()));
    }
}
