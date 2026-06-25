package com.kikyosoft.auth.dto;

import java.util.List;

public class TokenAuthResponse {

    private Data data;
    private List<GraphQLError> errors; // top-level GraphQL errors (if any)

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
    public List<GraphQLError> getErrors() { return errors; }
    public void setErrors(List<GraphQLError> errors) { this.errors = errors; }

    public static class Data {
        private TokenCreate tokenCreate;
        public TokenCreate getTokenCreate() { return tokenCreate; }
        public void setTokenCreate(TokenCreate tokenCreate) { this.tokenCreate = tokenCreate; }
    }

    public static class TokenCreate {
        private String token;
        private List<FieldError> errors;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public List<FieldError> getErrors() { return errors; }
        public void setErrors(List<FieldError> errors) { this.errors = errors; }
    }

    public static class FieldError {
        private String field;
        private String message;

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class GraphQLError {
        private String message;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}

