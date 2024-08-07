package com.dingyabin.springsecuritydemo.config.security;

import com.dingyabin.security.service.SysAuthorityService;
import com.dingyabin.springsecuritydemo.config.security.filter.JwtAuthenticationFilter;
import com.dingyabin.security.entity.SysAuthority;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author 丁亚宾
 * Date: 2024/7/30.
 * Time:22:11
 */
@Configuration
public class SecurityConfig {

    @Resource
    private CustomerAccessDeniedHandler customerAccessDeniedHandler;

    @Resource
    private CustomerAuthenticationEntryPoint customerAuthenticationEntryPoint;

    @Resource
    private SecurityUserDetailsService securityUserDetailsService;

    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Resource
    private SysAuthorityService sysAuthorityService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security, AuthenticationConfiguration configuration) throws Exception {
        security.csrf(AbstractHttpConfigurer::disable)
                //跨域
                .cors().configurationSource(corsConfigurationSource())
                .and()
                //不使用session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                //禁用表单登录
                .formLogin().disable()
                .logout().disable()
                .authenticationManager(authenticationManager(configuration))
                .authorizeHttpRequests()
                //放开登录接口
                .antMatchers("/login").permitAll()
                .antMatchers("/logout").authenticated()
                //.anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .accessDeniedHandler(customerAccessDeniedHandler)
                .authenticationEntryPoint(customerAuthenticationEntryPoint)
                .and()
                .addFilterBefore(jwtAuthenticationFilter, AuthorizationFilter.class);

        //加载权限信息
        List<SysAuthority> sysAuthorities = sysAuthorityService.selectAllSysAuthority();
        for (SysAuthority sysAuthority : sysAuthorities) {
            security.authorizeHttpRequests().antMatchers(sysAuthority.getPath()).hasAuthority(sysAuthority.getAuthority());
        }
        //其他的需要登录
        security.authorizeHttpRequests().anyRequest().authenticated();
        return security.build();
    }


    /**
     * 配置跨域请求
     *
     * @return CorsConfigurationSource
     */
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //允许跨域
        corsConfiguration.setAllowedOrigins(Collections.singletonList("*"));
        corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
        corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
        //预检请求的缓存有效期
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return corsConfigurationSource;
    }


    @Bean
    public PasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder());
        daoAuthenticationProvider.setUserDetailsService(securityUserDetailsService);
        return daoAuthenticationProvider;
    }



    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }


}
