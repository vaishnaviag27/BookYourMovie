package com.ebooking.BookUrMovie.accounts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security picks up Security Configuration from this class
 * Enabling Global Method Security to allow access to methods based on
 * the user's role.
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    UserDetailsService userDetailsService;

    /**
     * Method used to configure the type of Spring Security using AuthenticationManagerBuilder
     *
     * @param auth configures Spring Security to use authentication type as UserDetailsService
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    /**
     * Method used to configure authorization of urls using HttpSecurity
     *
     * @param http security type to configure web based security for specific http request
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //Order from Most restricted to least restricted access
        http.httpBasic().and().authorizeRequests()
            .antMatchers("/login").permitAll()
            .antMatchers(HttpMethod.POST, "/signup").permitAll()
            .and().formLogin().defaultSuccessUrl("/home", true)
            .and().logout().logoutSuccessUrl("/login").permitAll()
            .and().exceptionHandling().accessDeniedPage("/access-denied")
            .and().csrf().disable();
    }

    /**
     * Bean used by Spring Security to configure Password Encryption Mechanism
     *
     * @return Instance of BcryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
