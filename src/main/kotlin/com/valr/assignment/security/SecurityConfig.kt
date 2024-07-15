// package com.valr.assignment.security
//
// import org.springframework.context.annotation.Bean
// import org.springframework.context.annotation.Configuration
// import org.springframework.http.HttpMethod
// import org.springframework.security.config.annotation.web.builders.HttpSecurity
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
// import org.springframework.security.config.http.SessionCreationPolicy
// import org.springframework.security.core.userdetails.User
// import org.springframework.security.core.userdetails.UserDetailsService
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
// import org.springframework.security.crypto.password.PasswordEncoder
// import org.springframework.security.provisioning.InMemoryUserDetailsManager
// import org.springframework.security.web.SecurityFilterChain
//
//
// //@Configuration
// //@EnableWebSecurity
// class SecurityConfig {
//
//    @Bean
//    fun passwordEncoder(): PasswordEncoder {
//        return BCryptPasswordEncoder()
//    }
//
//    @Bean
//    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
//        http
//            .csrf { it.disable() }
//            .authorizeHttpRequests {
//                it
//                    .requestMatchers("/api/orderbook/**")
//                    .permitAll()
//                    .requestMatchers(HttpMethod.POST, "/api/orderbook/orders")
//                    .hasRole("USER")
//                    .anyRequest()
//                    .fullyAuthenticated()
//            }
//            .sessionManagement {
//                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            }
// //            .authenticationProvider(authenticationProvider)
// //            .addFilterBefore(
// //                jwtAuthenticationFilter,
// //                UsernamePasswordAuthenticationFilter::class.java
// //            )
//        return http.build()
//    }
//
//    @Bean
//    fun users(): UserDetailsService {
//        // The builder will ensure the passwords are encoded before saving in memory
//        val user = User.builder()
//            .username("user")
//            .password(passwordEncoder().encode("password"))
//            .roles("USER")
//            .build()
//        val admin = User.builder()
//            .username("admin")
//            .password(passwordEncoder().encode("admin"))
//            .roles("USER", "ADMIN")
//            .build()
//        return InMemoryUserDetailsManager(user, admin)
//    }
// }
