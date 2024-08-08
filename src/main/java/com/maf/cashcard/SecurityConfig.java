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

    static final String CARD_OWNER = "CARD-OWNER";

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // FIRST ITERATION: all tests work except POST
        // return http.build()

        http
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/cashcards/**")
                        //.authenticated()) // SECOND ITERATION: only check authenticacion, no authorization
                        .hasRole(CARD_OWNER)) // THIRD ITERATION: enable Role-Based Access Control (RBAC)
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
                .password(passwordEncoder.encode("abc123"))
                //.roles() // SECOND ITERATION: no roles used
                .roles(CARD_OWNER) // THIRD ITERATION: new role
                .build();
        UserDetails kumar = users
                .username("kumar2")
                .password(passwordEncoder.encode("xyz789"))
                .roles(CARD_OWNER)
                .build();
        UserDetails hankOwnsNoCards = users
                .username("hank-owns-no-cards")
                .password(passwordEncoder.encode("qrs890"))
                .roles("NON-OWNER") // THIRD ITERATION: new role
                .build();
        return new InMemoryUserDetailsManager(sarah, kumar, hankOwnsNoCards);
    }
}
