package com.kikyosoft.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    http
    	.headers(headers -> headers
    	      .frameOptions(frame -> frame.sameOrigin())
    	    )
      // Ignore CSRF for GraphQL and ERP login endpoint (POSTs without token)
      .csrf(csrf -> csrf.ignoringAntMatchers(
          "/graphql",
          "/ai/chat",
          "/erp/login",
          "/erp/logout",
          "/erp/register",
          "/erp/createOrder",
          "/erp/commitOrder",
          "/erp/updateConsignment",
          "/erp/clientTransfer",
          "/erp/sendMessage",
          "/erp/getloginid",
          "/erp/getDocument",
          "/erp/saveprofile"
      ))

      // Allow public access to GraphQL, static assets, and the ERP login endpoint
      .authorizeRequests(auth -> auth
        // ERP login endpoint (both GET and POST)
        .antMatchers(HttpMethod.GET, "/erp/login").permitAll()
        .antMatchers(HttpMethod.POST, "/erp/login").permitAll()
        .antMatchers(HttpMethod.GET, "/erp/logout").permitAll()
        .antMatchers(HttpMethod.POST, "/erp/logout").permitAll()
        .antMatchers(HttpMethod.GET, "/erp/register").permitAll()
        .antMatchers(HttpMethod.POST, "/erp/register").permitAll()
        .antMatchers(HttpMethod.GET, "/erp/getloginid").permitAll()
        .antMatchers(HttpMethod.POST, "/erp/createOrder").permitAll()
        .antMatchers(HttpMethod.POST, "/erp/commitOrder").permitAll()
        .antMatchers(HttpMethod.POST, "/erp/updateConsignment").permitAll()
        .antMatchers(HttpMethod.POST, "/erp/clientTransfer").permitAll()
        .antMatchers(HttpMethod.POST, "/erp/sendMessage").permitAll()
        .antMatchers(HttpMethod.POST, "/ai/chat").permitAll()
        .antMatchers(HttpMethod.GET, "/erp/getDocument").permitAll()
        .antMatchers(HttpMethod.POST, "/erp/saveprofile").permitAll()

        // GraphQL + dev assets you already exposed
        .antMatchers(
          "/graphql",
          "/graphiql.html",
          "/webjars/**",
          "/static/**",
          "/",
          "/index.html"
        ).permitAll()

        // keep open for dev; tighten later
        .anyRequest().permitAll()
      )

      // No basic-auth popup
      .httpBasic().disable();

    return http.build();
  }
}
