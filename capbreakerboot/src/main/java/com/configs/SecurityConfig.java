package com.configs;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${spring.queries.users-query}")
	private String usersQuery;
	@Value("${spring.queries.roles-query}")
	private String rolesQuery;
	
	@Autowired
	private DataSource ds;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().antMatchers("/client/**").authenticated().and().httpBasic()
				.realmName("client").authenticationEntryPoint(getBasicAuthEntryPoint()).and().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication().dataSource(ds).usersByUsernameQuery(usersQuery)
				.authoritiesByUsernameQuery(rolesQuery);
	}

	@Bean
	public BasicAuthenticationEntryPoint getBasicAuthEntryPoint() {
		return new BasicAuthenticationEntryPoint() {
			@Override
			public void commence(final HttpServletRequest request, final HttpServletResponse response,
					final AuthenticationException authException) throws IOException, ServletException {

				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName() + "");

				PrintWriter writer = response.getWriter();
				writer.println("HTTP Status 401 : " + authException.getMessage());
			}

			@Override
			public void afterPropertiesSet() throws Exception {
				setRealmName("client");
				super.afterPropertiesSet();
			}
		};
	}
}
