package ubiqore.fhir.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import ubiqore.fhir.security.JwtAuthenticationEntryPoint;
//import ubiqore.fhir.security.JwtAuthenticationTokenFilter;


@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
//
//	@Override
//	  public void configure(AuthenticationManagerBuilder auth) throws Exception {
//
//	    auth.inMemoryAuthentication()
//	      .withUser("eric").password("roky").roles("USER").and()
//	      .withUser("roky").password("a").roles("USER", "ADMIN");
//	  }
//


	//@Autowired
	//private JwtAuthenticationEntryPoint unauthorizedHandler;

	//@Autowired
	//private UserDetailsService userDetailsService;

	//@Autowired
	/**public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
		authenticationManagerBuilder
				.userDetailsService(this.userDetailsService)
				.passwordEncoder(passwordEncoder());
	}**/

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

//	@Bean
//	public JwtAuthenticationTokenFilter authenticationTokenFilterBean() throws Exception {
//		return new JwtAuthenticationTokenFilter();
//	}


	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		// http.csrf().ignoringAntMatchers("/greeting");

		// ici tout est autorisé sauf : greeting POST et GET qui doit avoir
		/*	http.httpBasic().and().authorizeRequests()
							  .antMatchers(HttpMethod.GET, "/greeting").hasRole("ADMIN")
							  .antMatchers(HttpMethod.POST, "/greeting").hasRole("ADMIN").and()
							  .csrf().disable();*/


		httpSecurity
				// we don't need CSRF because our token is invulnerable
				.csrf().disable()

		//		.exceptionHandling().authenticationEntryPoint(unauthorizedHandler)

		//		.and()

				// don't create session
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

				.authorizeRequests()
				//.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()

				// allow anonymous resource requests
				.antMatchers(
						HttpMethod.GET,
						"/",
						"/*.html",
						"/favicon.ico",
						"/**/*.html",
						"/**/*.css",
						"/**/*.js"
				).permitAll()
                //
                //
				.antMatchers("/**").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/v2/api-docs**").permitAll()
				.antMatchers("/webjars/springfox-swagger-ui**").permitAll()
                .antMatchers("/swagger-ui.html/**").permitAll()
				.antMatchers("/auth/**").permitAll()
				.antMatchers("/auth/nu/**").permitAll()
				.antMatchers("/auth/fake/**").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.anyRequest().authenticated();

		// Custom JWT based security filter
//		httpSecurity
//				.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);

		// disable page caching
		httpSecurity.headers().cacheControl();

	 }
}
