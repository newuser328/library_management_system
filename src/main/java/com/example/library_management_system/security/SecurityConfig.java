package com.example.library_management_system.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // books：读者/管理员可读
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/books", "/api/books/*").authenticated()
                        // books：仅管理员可写
                        .requestMatchers("/api/books/**").hasRole("ADMIN")

                        // categories：读者/管理员可读
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/categories", "/api/categories/*").authenticated()
                        // categories：仅管理员可写
                        .requestMatchers("/api/categories/**").hasRole("ADMIN")

                        // files：图片读取不鉴权（img/预览请求无法携带 Authorization）；上传允许任意已登录用户（头像/封面等）
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/files/images/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/files/upload").authenticated()

                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin-register-tokens/**").hasRole("ADMIN")
                        .requestMatchers("/api/borrows/**").authenticated()
                        .requestMatchers("/api/me/**").authenticated()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
