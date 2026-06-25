package com.kikyosoft.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.basjes.parse.useragent.utils.springframework.core.io.ResourceLoader;

@Component
public class SaleorConfig {
	
/*
 @Value("${saleor.api.url}")
 private String saleorApiUrl;

 @Value("${saleor.api.token}")
 private String saleorApiToken;
*/
	  // Reusable ObjectMapper for controllers
 private final ObjectMapper mapper = new ObjectMapper()
	.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

 public String getApiUrl() {
//     return saleorApiUrl;
//    return("http://192.168.46.104:8000/graphql/");
    return("http://192.168.19.212:8000/graphql/");
 }

 public String getAuthHeader() {
     return "Bearer " + getToken();
 }
 public String getToken() {
//     return "vdbkrq6BwIwH0ko6p4TFFrrrJjzHkM";
     return "80ipBW8C2AoqObq8qaWYzpU2yDIT7E";
 }
 public ObjectMapper getMapper() {
	    return mapper;
	  }
 
}
