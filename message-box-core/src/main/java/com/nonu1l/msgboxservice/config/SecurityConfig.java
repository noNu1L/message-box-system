package com.nonu1l.msgboxservice.config;

import com.nonu1l.commons.utils.Result;
import com.nonu1l.msgboxservice.entity.UserExt;
import com.nonu1l.msgboxservice.service.NotLogInAuthenticationEntryPoint;
import com.nonu1l.msgboxservice.service.UserNamePasswordAuthenticationProvider;
import com.nonu1l.msgboxservice.service.UserService;
import com.nonu1l.msgboxservice.vo.UserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring Security配置
 *
 * @author zhonghanbo
 * @date 2024年11月19日 19:25
 */

@Configuration
public class SecurityConfig {

    private final UserService userService;

    private final UserDetailsService userDetailsService;

    public SecurityConfig(@Lazy UserDetailsService userDetailsService, @Lazy UserService userService) {
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @Bean
    public UserNamePasswordAuthenticationProvider userNamePasswordAuthenticationProvider(PasswordEncoder passwordEncoder) {
        return new UserNamePasswordAuthenticationProvider(userDetailsService, passwordEncoder);
    }

    @Bean
    public AuthenticationManager authenticationManager(UserNamePasswordAuthenticationProvider userNamePasswordAuthenticationProvider) {
        return new ProviderManager(Collections.singletonList(userNamePasswordAuthenticationProvider));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/public/**",
                                "/push/**",
                                "/user/login",
                                "/ws/**").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(customLogoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                                .accessDeniedHandler(customAccessDeniedHandler())
                                .authenticationEntryPoint(new NotLogInAuthenticationEntryPoint())
                )
                .httpBasic(withDefaults())
                .csrf(AbstractHttpConfigurer::disable) //跨域配置，测试环境可以禁用
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 注销成功处理
     *
     * @return {@link LogoutSuccessHandler }
     */
    private LogoutSuccessHandler customLogoutSuccessHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(Result.ok("注销成功").toString());
        };
    }

    /**
     * 已登录，但部分资源无权访问
     *
     * @return {@link AccessDeniedHandler }
     */
    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(403);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(Result.fail("无权访问").toString());
        };
    }

    /**
     * 身份验证失败处理
     *
     * @return {@link AuthenticationFailureHandler }
     */
    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(Result.fail("无效认证").toString());
        };
    }

    /**
     * 身份验证成功处理
     *
     * @return {@link AuthenticationSuccessHandler }
     */
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            String username = authentication.getName();
            UserExt user = (UserExt) authentication.getPrincipal();
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            response.getWriter().write(Result.ok(userVo).toString());
            userService.updateLastLoginTime(username);
        };
    }

}
