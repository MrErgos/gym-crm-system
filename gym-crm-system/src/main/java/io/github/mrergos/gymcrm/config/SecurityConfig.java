package io.github.mrergos.gymcrm.config;

import io.github.mrergos.gymcrm.security.GymUserDetailsService;
import io.github.mrergos.gymcrm.security.jwt.JwtAuthenticationFilter;
import io.github.mrergos.gymcrm.security.jwt.JwtService;
import io.github.mrergos.gymcrm.security.jwt.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.PathPatternRequestMatcherFactoryBean;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/v1/trainers/training-types"
    };

    private static final String[] PUBLIC_POST_ENDPOINTS = {
            "/api/v1/trainees",
            "/api/v1/trainers",
            "/api/v1/auth/login"
    };

    private static final String[] PUBLIC_DOC_ENDPOINTS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(GymUserDetailsService userDetailsService,
                                                             PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public RequestMatcher publicEndpointsMatcher() {
        List<RequestMatcher> matchers = new ArrayList<>();

        PathPatternRequestMatcher.Builder patternMatcher = PathPatternRequestMatcher.withDefaults();
        for (String pattern : PUBLIC_GET_ENDPOINTS) {
            matchers.add(patternMatcher.matcher(pattern));
        }
        for (String pattern : PUBLIC_POST_ENDPOINTS) {
            matchers.add(patternMatcher.matcher(HttpMethod.POST,pattern));
        }
        for (String pattern : PUBLIC_DOC_ENDPOINTS) {
            matchers.add(patternMatcher.matcher(pattern));
        }
        return new OrRequestMatcher(matchers);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService,
                                                           GymUserDetailsService userDetailsService,
                                                           TokenBlacklistService tokenBlacklistService,
                                                           RequestMatcher publicEndpointsMatcher,
                                                           TransactionTemplate transactionTemplate) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService,
                tokenBlacklistService, publicEndpointsMatcher, transactionTemplate);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    JwtAuthenticationFilter jwtAuthenticationFilter,
                                                    CorsConfigurationSource corsConfigurationSource) throws Exception {
        log.info("Configuring Spring Security filter chain");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_GET_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
                        .requestMatchers(PUBLIC_DOC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${security.cors.allowed-origins}") List<String> allowedOrigins,
            @Value("${security.cors.allowed-methods}") List<String> allowedMethods) {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("X-Transaction-Id"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS configured, allowedOrigins={}, allowedMethods={}", allowedOrigins, allowedMethods);
        return source;
    }
}
