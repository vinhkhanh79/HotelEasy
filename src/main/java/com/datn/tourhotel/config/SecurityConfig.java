package com.datn.tourhotel.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.datn.tourhotel.security.CustomAuthenticationFailureHandler;
import com.datn.tourhotel.security.CustomAuthenticationSuccessHandler;
import com.datn.tourhotel.security.CustomLogoutSuccessHandler;
import com.datn.tourhotel.service.impl.CustomOAuth2UserServiceImpl;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomOAuth2UserServiceImpl customOAuth2UserService;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomAuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }
    
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new CustomLogoutSuccessHandler();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // In case CSRF disabling is needed for testing
        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests((authorize) ->
                        authorize.requestMatchers("/css/**", "/js/**", "/webjars/**" , "/search/**" , "/search-results/**" , "/hotel-details/**" , "/img/**", "/language/**","/post/**").permitAll()
                                .requestMatchers("/home/**", "/index/**", "/register/**", "/forgotPass/**", "/login/**").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                                .requestMatchers("/manager/**").hasRole("HOTEL_MANAGER")
                                .anyRequest().authenticated())
                .formLogin(
                        form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .successHandler(customAuthenticationSuccessHandler)
                                .failureHandler(customAuthenticationFailureHandler())
                                .permitAll())
                .oauth2Login(
                        oauth->oauth
                                .loginPage("/login")// Redirect to custom login page
                                .defaultSuccessUrl("/", true)
                                .successHandler(customAuthenticationSuccessHandler)
                                .failureHandler(customAuthenticationFailureHandler())
                                .userInfoEndpoint(userinfo -> userinfo.userService(customOAuth2UserService))
                                .permitAll()
                )                
                .logout(
                        logout -> logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .logoutSuccessHandler(logoutSuccessHandler())
                                .permitAll());
        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
    }
}
