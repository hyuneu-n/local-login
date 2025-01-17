package com.jjery.login.config;

import com.jjery.login.filter.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtRequestFilter jwtRequestFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .csrf(CsrfConfigurer::disable)
        // Bearer 방식을 사용할건데 스프링 시큐리티는 httpBasic 이 잡혀있음
        .httpBasic(HttpBasicConfigurer::disable)
        // Session 방식은 사용안할거여서 끔(stateless)
        .sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // Requests 경로별 요청 인가
        .authorizeHttpRequests(
            request ->
                request
                    .requestMatchers(
                        "/",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/api/users/register",
                        "/api/users/login")
                    .permitAll()
                    .requestMatchers("/api/v1/user/*")
                    .hasRole("USER")
                    .requestMatchers("/api/v1/admin/*")
                    .hasRole("ADMIN")
                    // 열어준 요청 외에는 모두 권한 필요
                    .anyRequest()
                    .authenticated())
        .cors(Customizer.withDefaults());

    // JWT 필터 추가
    httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    return httpSecurity.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOriginPattern("*"); // 모든 도메인 허용
    configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
    configuration.addAllowedHeader("*"); // 모든 헤더 허용
    configuration.setAllowCredentials(true); // 자격 증명 허용

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
