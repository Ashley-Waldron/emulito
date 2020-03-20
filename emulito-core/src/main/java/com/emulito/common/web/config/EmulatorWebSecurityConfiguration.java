package com.emulito.common.web.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@Configuration
@Order(50)
public class EmulatorWebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/*")
                .authorizeRequests().anyRequest().permitAll()
                .and()
                .headers().frameOptions().sameOrigin()
                .and()
                .csrf().disable();
    }

    /**
     * The following method is necessary due to a bug in spring whereby requests with content-type
     * 'application/x-www-form-urlencoded' are read by a filter which consumes the request body stream and leaves it
     * unusable in the emulator. See <a href='https://stackoverflow.com/a/32296864/7529139'>here<a/>
     * @param filter
     * @return
     */
    @Bean
    public FilterRegistrationBean registration(HiddenHttpMethodFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(filter);
        registration.setEnabled(false);
        return registration;
    }
}
