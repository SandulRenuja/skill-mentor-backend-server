package com.stemlink.skillmentor.configs;

import com.stemlink.skillmentor.security.AuthenticationFilter;
import com.stemlink.skillmentor.security.SkillMentorAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationFilter clerkAuthenticationFilter;
    private final SkillMentorAuthenticationEntryPoint skillMentorAuthenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(skillMentorAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        // Swagger / OpenAPI
                        .requestMatchers(
                                "/api/public/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-resources/**"
                        ).permitAll()

                        // Public read access to mentors (home page)
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/mentors",
                                "/api/v1/mentors/{id}"
                        ).permitAll()

                        // Admin-only: create/update/delete mentors
                        .requestMatchers(HttpMethod.POST, "/api/v1/mentors")
                        .hasAnyRole("ADMIN", "MENTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/mentors/{id}")
                        .hasAnyRole("ADMIN", "MENTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/mentors/{id}")
                        .hasRole("ADMIN")

                        // Admin-only: full session management
                        .requestMatchers(HttpMethod.GET, "/api/v1/sessions")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/sessions/{id}")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/sessions/{id}")
                        .hasRole("ADMIN")

                        // Admin-only: subject management
                        .requestMatchers(HttpMethod.POST, "/api/v1/subjects")
                        .hasAnyRole("ADMIN", "MENTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/subjects/{id}")
                        .hasAnyRole("ADMIN", "MENTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/subjects/{id}")
                        .hasRole("ADMIN")

                        // Student + Admin: enroll and view own sessions
                        .requestMatchers(HttpMethod.POST, "/api/v1/sessions/enroll")
                        .hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/sessions/my-sessions")
                        .hasAnyRole("STUDENT", "ADMIN")

                        // Everything else requires auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(clerkAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}