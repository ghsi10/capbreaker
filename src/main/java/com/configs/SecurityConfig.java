package com.configs;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
public class SecurityConfig {

	@Value("${spring.queries.users-query}")
	private String USERS_QUERY;
	@Value("${spring.queries.roles-query}")
	private String ROLES_QUERY;
	@Value("${spring.login.username}")
	private String MASTER_USERNAME;
	@Value("${spring.login.password}")
	private String MASTER_PASSWORD;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth, DataSource dataSource) throws Exception {
		auth.jdbcAuthentication().dataSource(dataSource).usersByUsernameQuery(USERS_QUERY)
				.authoritiesByUsernameQuery(ROLES_QUERY).and().inMemoryAuthentication().withUser(MASTER_USERNAME)
				.password(MASTER_PASSWORD).roles("ADMIN");
	}

	@Configuration
	@Order(1)
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.csrf().disable().antMatcher("/agent/**").authorizeRequests().anyRequest().hasAnyRole("ADMIN", "USER")
					.and().httpBasic();
		}
	}

	@Configuration
	public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.headers().frameOptions().disable().and().csrf().disable().authorizeRequests().antMatchers("/admin/**")
					.hasRole("ADMIN").antMatchers("/user/**").hasAnyRole("ADMIN", "USER").and().formLogin()
					.loginPage("/login").and().logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
		}
	}
}
