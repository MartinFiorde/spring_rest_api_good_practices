package com.maf.cashcard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    static final String ROLE_CARD_OWNER = "CARD-OWNER";
    static final String ROLE_ADMIN = "ADMIN";
    static final String ROLE_NONE = "NON-OWNER";
    static final String TEST_PASS = "admin123";

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // FIRST ITERATION: all tests work except POST
        // return http.build()

        http
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/cashcards/**")
                        //.authenticated()) // SECOND ITERATION: only check authenticacion, no authorization
                        .hasRole(ROLE_CARD_OWNER) // THIRD ITERATION: enable Role-Based Access Control (RBAC)
                        .requestMatchers("/cashcards/audittrail/**").hasRole(ROLE_ADMIN))
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable); //IDEM: csrf -> csrf.disable()
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService testOnlyUsers(PasswordEncoder passwordEncoder) {
        User.UserBuilder users = User.builder();
        UserDetails sarah = users
                .username("sarah1")
                .password(passwordEncoder.encode(TEST_PASS))
                //.roles() // SECOND ITERATION: no roles used
                .roles(ROLE_CARD_OWNER) // THIRD ITERATION: new role
                .build();
        UserDetails kumar = users
                .username("kumar2")
                .password(passwordEncoder.encode(TEST_PASS))
                .roles(ROLE_CARD_OWNER)
                .build();
        UserDetails admin = users
                .username("admin3")
                .password(passwordEncoder.encode(TEST_PASS))
                .roles(ROLE_ADMIN)
                .build();
        UserDetails hankOwnsNoCards = users
                .username("hank-owns-no-cards")
                .password(passwordEncoder.encode(TEST_PASS))
                .roles(ROLE_NONE) // THIRD ITERATION: new role
                .build();
        return new InMemoryUserDetailsManager(sarah, kumar, admin, hankOwnsNoCards);
    }
}
