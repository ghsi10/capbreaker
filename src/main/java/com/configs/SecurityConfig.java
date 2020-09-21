package com.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${login.master.username:admin}")
    private String masterUsername;
    @Value("${login.master.password:admin}")
    private String masterPassword;

    private final DataSource dataSource;
    private final UserDetailsService loginUserDetailsService;

    @Autowired
    public SecurityConfig(DataSource dataSource, UserDetailsService loginUserDetailsService) {
        this.dataSource = dataSource;
        this.loginUserDetailsService = loginUserDetailsService;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new NoOpPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(loginUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser(masterUsername).password(masterPassword).roles("ADMIN");
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().frameOptions().disable().and().csrf().disable().authorizeRequests()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                .antMatchers("/agent/**").hasAnyRole("ADMIN", "USER")
                .and().formLogin().loginPage("/signin").and().logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .and().httpBasic();
    }

    public static class NoOpPasswordEncoder implements PasswordEncoder {

        public NoOpPasswordEncoder() {
        }

        @Override
        public String encode(CharSequence rawPassword) {
            return rawPassword.toString();
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return rawPassword.toString().equals(encodedPassword);
        }
    }
}
