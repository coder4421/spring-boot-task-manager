package com.vrund.tasks;

import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean UserDetailsService users(AccountRepository accounts) {
        return username -> accounts.findById(username)
            .map(a -> User.withUsername(a.username).password(a.passwordHash).roles("USER").build())
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }

    @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a.requestMatchers("/login", "/style.css", "/error").permitAll()
                .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
            .logout(l -> l.logoutSuccessUrl("/login?logout"))
            .exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), new AntPathRequestMatcher("/api/**")))
            .headers(h -> h.contentSecurityPolicy(c -> c.policyDirectives(
                "default-src 'self'; script-src 'self'; style-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'")));
        // Spring Security's default CSRF protection remains enabled for every mutation.
        return http.build();
    }

    @Bean ApplicationRunner bootstrap(AccountRepository accounts, PasswordEncoder encoder,
        @Value("${app.bootstrap.username}") String username,
        @Value("${app.bootstrap.password}") String password) {
        return args -> {
            if (!username.matches("[A-Za-z0-9_.-]{3,64}") || password.length() < 12
                    || password.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalStateException("Set APP_USER (3-64 letters, digits, _, ., -) and APP_PASSWORD (12+ characters, at most 72 UTF-8 bytes).");
            }
            if (!accounts.existsById(username)) accounts.save(new Account(username, encoder.encode(password)));
        };
    }
}
