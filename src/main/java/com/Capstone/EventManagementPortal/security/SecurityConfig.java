package com.Capstone.EventManagementPortal.security;

import com.Capstone.EventManagementPortal.security.jwt.JwtAuthenticationFilter;
import com.Capstone.EventManagementPortal.security.jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtUtil jwtUtil) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/login.html",
                                "/register.html",
                                "/css/**",  // ✅ Allow CSS
                                "/js/**",   // ✅ Allow JS
                                "/images/**",  // ✅ Allow Images
                                "/static/**",  // ✅ Allow Static Files
                                "/favicon.ico" // ✅ Allow favicon
                        ).permitAll()
                        .requestMatchers("/organizer-dashboard.html").hasAuthority("ORGANIZER")
                        .requestMatchers("/user-dashboard.html").hasAuthority("ATTENDEE")
                        .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/events/**").hasAuthority("ORGANIZER")
                        .requestMatchers(HttpMethod.PUT, "/api/events/**").hasAuthority("ORGANIZER")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasAuthority("ORGANIZER")
                        .requestMatchers(HttpMethod.POST, "/api/bookings/**").hasAuthority("ATTENDEE")  // Only attendees can book
                        .requestMatchers(HttpMethod.GET, "/api/bookings/**").authenticated()  // Allow authenticated users to fetch bookings
                        .requestMatchers(HttpMethod.DELETE, "/api/bookings/**").authenticated()  // Only authenticated users can cancel

                        .requestMatchers("/**/*.html").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, customUserDetailsService);
    }
}
