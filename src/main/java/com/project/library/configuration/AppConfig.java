package com.project.library.configuration;


import com.project.library.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("!prod") // chỉ định ngoài prod
@RequiredArgsConstructor
public class AppConfig {

    private final UserService userService;
    private final Prefilter filter;
    private String[] WHITE_LIST = {"/auth/**"};
    @Bean
    public WebMvcConfigurer corsConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                registry.addMapping("**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET","PUT","DELETE","POST")
                        .allowedHeaders("*")
                        .allowCredentials(false)
                        .maxAge(3600);
            }
        };
    }
    @Bean
    public PasswordEncoder getPasswordEncoder() {
//        return new BCryptPasswordEncoder();
        return NoOpPasswordEncoder.getInstance();
    }


    @Bean
    public SecurityFilterChain configure(@NotNull HttpSecurity http) throws Exception { // thiết lập statless không lưu token ở server
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(WHITE_LIST).permitAll()

                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me").hasAnyRole("STUDENT", "LIBRARIAN", "ADMIN")

                        // Librarian + Admin quản lý student
                        .requestMatchers(HttpMethod.POST, "/api/v1/students").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/students/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/students/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/students/bulk-import").hasRole("ADMIN")

                        // Borrow - student tự mượn/trả
                        .requestMatchers(HttpMethod.POST, "/api/v1/borrows").hasAnyRole("STUDENT", "LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/borrows/*/return").hasAnyRole("LIBRARIAN", "ADMIN")

                        // Books - librarian quản lý
                        .requestMatchers(HttpMethod.GET, "/api/v1/books/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/books/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/books/**").hasAnyRole("LIBRARIAN", "ADMIN")

                        // Stats - admin only
                        .requestMatchers("/api/v1/stats/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(provider()).addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class); // đứng trước tất cả các API để lọc validate cái token đúng thì cho phép request
        return http.build();
    }
//@Bean
//public SecurityFilterChain configure(@NotNull HttpSecurity http) throws Exception {
//    http.csrf(AbstractHttpConfigurer::disable)
//            .authorizeHttpRequests(authorize -> authorize
//                    .requestMatchers(WHITE_LIST).permitAll()
//                    .anyRequest().authenticated()
//            )
//            .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//            .authenticationProvider(provider())
//            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
//
//    return http.build();
//}

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return webSecurity -> webSecurity.ignoring().requestMatchers(
                "/auth/**",
                "/v3/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/webjars/**",
                "actuator/**"
        );
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    @Bean
    public AuthenticationProvider provider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService.userDetailsService());
        provider.setPasswordEncoder(getPasswordEncoder());

        return provider;
    }
}
