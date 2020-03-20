package com.emulito.common.web.config;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import static org.hamcrest.CoreMatchers.is;

public class EmulatorWebSecurityConfigurationTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private EmulatorWebSecurityConfiguration emulatorWebSecurityConfiguration = new EmulatorWebSecurityConfiguration();

    @Test
    public void registration() {
        FilterRegistrationBean registration = emulatorWebSecurityConfiguration.registration(new HiddenHttpMethodFilter());
        Assert.assertThat(registration.isEnabled(), is(false));
    }
}
