package com.jnispace.isll.facade;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Hashtable;

import javax.naming.Context;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.jnispace.isll.config.ISLLConfig;
import com.jnispace.isll.facade.dto.ResponseDTO;
import com.jnispace.isll.services.LdapAuthentifier;
import com.jnispace.isll.services.exceptions.LdapAuthException;

public class ISLLLoginFacadeTest {
    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";
    private final static String LDAP_URL = "ldap://some.url";
    private final static String LDAP_BASE = "ou=people,dc=some,dc=url";
    private final static String LDAP_PRINCIPALE_ATTRIBUTE = "uid";
    private final static String USER_DN = LDAP_PRINCIPALE_ATTRIBUTE + "=" + USERNAME + "," + LDAP_BASE;

    private LdapAuthentifier authentifier;
    private ISLLConfig config;
    private Logger logger;
    private ISLLLoginFacade facade;

    @Before
    public void setUp() {
        authentifier = mock(LdapAuthentifier.class);
        logger = mock(Logger.class);
        createConfig();
        createFacade();

    }

    @Test
    public void environmentIsSetProperly() {
        Hashtable<String, String> env = facade.getEnvironment(USERNAME, PASSWORD);

        assertEquals(ISLLLoginFacade.LDAP_CONTEXT_FACTORY, env.get(Context.INITIAL_CONTEXT_FACTORY));
        assertEquals(ISLLLoginFacade.LDAP_AUTH_METHOD, env.get(Context.SECURITY_AUTHENTICATION));
        assertEquals(LDAP_URL, env.get(Context.PROVIDER_URL));
        assertEquals(USER_DN, env.get(Context.SECURITY_PRINCIPAL));
        assertEquals(PASSWORD, env.get(Context.SECURITY_CREDENTIALS));
    }

    @Test
    public void properDTOIsReturnedOnSuccess() {
        ResponseDTO dto = facade.authenticate(USERNAME, PASSWORD);

        assertEquals(ResponseDTO.SUCCESS, dto.type);
        assertNull(dto.message);
    }

    @Test
    public void passthroughIsReturnedInNonStrictModeWhenAnLdapErrorOccurs() throws Exception {
        config.strict = false;
        doThrow(LdapAuthException.class).when(authentifier).authenticate();

        ResponseDTO dto = facade.authenticate(USERNAME, PASSWORD);

        assertEquals(ResponseDTO.PASS_THROUGH, dto.type);
        assertNull(dto.message);
    }

    @Test
    public void errorIsReturnedInStrickModeWhenAnLdapErrorOccurs() throws Exception {
        config.strict = true;
        doThrow(LdapAuthException.class).when(authentifier).authenticate();

        ResponseDTO dto = facade.authenticate(USERNAME, PASSWORD);

        assertEquals(ResponseDTO.ERROR, dto.type);
        assertEquals(ISLLLoginFacade.ERROR_INVALID_CREDENTIALS, dto.message);
    }

    @Test
    public void errorIsReturnedIfAnErrorOtherThanLdapAuthIsThrownInStrictMode() throws Exception {
        config.strict = true;
        doThrow(Exception.class).when(authentifier).authenticate();

        ResponseDTO dto = facade.authenticate(USERNAME, PASSWORD);

        assertEquals(ResponseDTO.ERROR, dto.type);
        assertEquals(ISLLLoginFacade.ERROR_UNKNOWN, dto.message);
    }

    @Test
    public void errorIsReturnedIfAnErrorOtherThanLdapAuthIsThrownInNonStrictMode() throws Exception {
        config.strict = false;
        doThrow(Exception.class).when(authentifier).authenticate();

        ResponseDTO dto = facade.authenticate(USERNAME, PASSWORD);

        assertEquals(ResponseDTO.ERROR, dto.type);
        assertEquals(ISLLLoginFacade.ERROR_UNKNOWN, dto.message);
    }

    @Test
    public void anErrorIsLoggedIfAnErrorOtherThanLdapAuthOccurs() throws Exception {
        doThrow(Exception.class).when(authentifier).authenticate();

        facade.authenticate(USERNAME, PASSWORD);

        verify(logger).error(anyString(), any(Throwable.class));
    }

    private void createFacade() {
        facade = spy(new ISLLLoginFacade(config, logger));
        doReturn(authentifier).when(facade).getLdapAuthentifier(USERNAME, PASSWORD);
    }

    private void createConfig() {
        config = new ISLLConfig();
        config.ldapUrl = LDAP_URL;
        config.ldapBase = LDAP_BASE;
        config.principalIdentifierAttribute = LDAP_PRINCIPALE_ATTRIBUTE;
    }
}
