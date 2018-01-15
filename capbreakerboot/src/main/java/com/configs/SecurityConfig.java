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

@EnableWebSecurity
public class SecurityConfig {

	@Value("${spring.queries.users-query}")
	private String usersQuery;
	@Value("${spring.queries.roles-query}")
	private String rolesQuery;
	@Value("${spring.login.username}")
	private String masterUsername;
	@Value("${spring.login.password}")
	private String masterPassword;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth, DataSource dataSource) throws Exception {
		auth.jdbcAuthentication().dataSource(dataSource).usersByUsernameQuery(usersQuery)
				.authoritiesByUsernameQuery(rolesQuery).and().inMemoryAuthentication().withUser(masterUsername)
				.password(masterPassword).roles("ADMIN");
	}

	@Configuration
	@Order(1)
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.antMatcher("/client/**").authorizeRequests().anyRequest().hasAnyRole("ADMIN", "USER").and()
					.httpBasic();
		}
	}

	@Configuration
	public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.authorizeRequests().antMatchers("/admin/**").hasRole("ADMIN").and().formLogin().and().logout();
		}
	}
}
