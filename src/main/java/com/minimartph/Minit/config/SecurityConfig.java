package com.minimartph.Minit.config;

import com.minimartph.Minit.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Autowired private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Public
                    .requestMatchers("/api/auth/login", "/uploads/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()

                    // User management – only ADMIN
                    .requestMatchers(HttpMethod.GET, "/uploads/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/users/*/photo")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/users/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/users/**")
                    .hasRole("ADMIN")

                    // Product modification – only ADMIN
                    .requestMatchers(HttpMethod.POST, "/api/products")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/products/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/products/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/products/restock", "/api/products/*/image")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/ai/**")
                    .hasRole("ADMIN")

                    // Sales calculation and completion – ADMIN and CASHIER
                    .requestMatchers(HttpMethod.POST, "/api/sales/calculate", "/api/sales")
                    .hasAnyRole("ADMIN", "CASHIER")

                    // Reports (GET sales) – only ADMIN
                    .requestMatchers(HttpMethod.GET, "/api/sales/**")
                    .hasRole("ADMIN")

                    // Product viewing – ADMIN and CASHIER
                    .requestMatchers(HttpMethod.GET, "/api/products/**")
                    .hasAnyRole("ADMIN", "CASHIER")

                    // All other requests require authentication
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }
}
