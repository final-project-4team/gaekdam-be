package com.gaekdam.gaekdambe.global.config.security;

import com.gaekdam.gaekdambe.global.config.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtFilter jwtFilter;
  private final CustomAuthenticationEntryPoint authenticationEntryPoint;
  private final CustomAccessDeniedHandler accessDeniedHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
        // CORS
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // JWT 구조 = 세션 비활성화 + CSRF 비활성화
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )



        // 권한 설정
        .authorizeHttpRequests(auth -> auth
                // 우선 개발초기랑 다 열어둠
                 //      .requestMatchers("/**").permitAll()//주석 처리 후 preAuthorize확인
                // 로그인/회원가입 공개
              //  .requestMatchers("api/v1/employee/add").permitAll()
  /*              .requestMatchers(
                    "/api/v1/auth/**"
                ).permitAll()*/
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/health/**").permitAll()
                // Swagger 공개
                .requestMatchers(
                  //  "/swagger-ui.html",
                   // "/swagger-ui/**",
                    "/v3/api-docs/**",
                   // "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()

                .requestMatchers("/api/v1/auth/**").permitAll()

                // API 보호

                .anyRequest().authenticated()

        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(authenticationEntryPoint)   // 401
            .accessDeniedHandler(accessDeniedHandler)             // 403
        )

        // JWT 필터 추가
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  // CORS 기본 구성
 @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    var c = new CorsConfiguration();
    // VSCode Live Server / 로컬 프론트들
    c.setAllowedOriginPatterns(java.util.List.of(
        "http://localhost:*",//
        "http://127.0.0.1:*",//
        "http://localhost:5173",//
        "https://gaekdam.cloud"
    ));
    c.setAllowedMethods(java.util.List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    c.setAllowedHeaders(java.util.List.of("Authorization","Content-Type","X-Requested-With","Origin"));
    c.setExposedHeaders(java.util.List.of("Authorization", "Set-Cookie"));
    c.setAllowCredentials(true);
    c.setMaxAge(3600L);

    var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", c);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}