package com.jnispace.isll.servlet;

import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.jnispace.isll.config.ISLLConfig;

public class ConfiguredServletBaseTest {

    private Logger logger;
    private ISLLConfig config;
    private ConfiguredServletBase servletBase;

    @Before
    public void setUp() {
        logger = mock(Logger.class);
        config = new ISLLConfig();
        configureServletBase();
    }

    @Test
    public void configuringARequestSetConfigArgument() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        servletBase.configure(request);

        verify(request).setAttribute(ConfiguredServletBase.CONFIG_ATTRIBUTE_NAME, config);
    }

    @Test
    public void configureSetsLoggerLevelFromConfig() {
        config.logging = "debug";

        servletBase.configure(mock(HttpServletRequest.class));

        verify(logger).setLevel(Level.toLevel(config.logging));
    }

    private void configureServletBase() {
        servletBase = new ConfiguredServletBase(logger) {
            @Override
            public ISLLConfig getConfig() {
                return config;
            }

            private static final long serialVersionUID = 1L;
        };
    }
}
