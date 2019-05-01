package com.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.queries.users-query}")
    private String USERS_QUERY;
    @Value("${spring.queries.roles-query}")
    private String ROLES_QUERY;
    @Value("${spring.login.username}")
    private String MASTER_USERNAME;
    @Value("${spring.login.password}")
    private String MASTER_PASSWORD;

    private final DataSource dataSource;

    @Autowired
    public SecurityConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public static NoOpPasswordEncoder passwordEncoder() {
        return new NoOpPasswordEncoder();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser(MASTER_USERNAME).password(MASTER_PASSWORD).roles("ADMIN");
        auth.jdbcAuthentication().dataSource(dataSource).usersByUsernameQuery(USERS_QUERY).authoritiesByUsernameQuery(ROLES_QUERY);
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

        public String encode(CharSequence rawPassword) {
            return rawPassword.toString();
        }

        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            PasswordEncoder encoder = new BCryptPasswordEncoder();
            return rawPassword.toString().equals(encodedPassword)
                    || encoder.matches(encodedPassword, rawPassword.toString());
        }

        private NoOpPasswordEncoder() {
        }
    }

}
