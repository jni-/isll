package com.jnispace.isll.services;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.jnispace.isll.config.ISLLConfig;
import com.jnispace.isll.services.exceptions.LdapAuthException;

public class LdapAccessorTest {
    private final static String EMPTY_STRING = "";
    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";
    private final static String PASSWORD_SHA256 = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8";

    private final static String CONFIG_LDAP_BASE = "ldap base";
    private final static String CONFIG_LDAP_USERNAME_ATTR = "uid";
    private final static String CONFIG_LDAP_EMAIL_ATTR = "email";
    private final static String CONFIG_LDAP_FIRSTNAME_ATTR = "givenName";
    private final static String CONFIG_LDAP_LASTNAME_ATTR = "cn";

    private final static String EMAIL_VALUE = "some@email";
    private final static String FIRSTNAME_VALUE = "first name";
    private final static String LASTNAME_VALUE = "last name";

    private final static String FILTER_FORMAT = "(%s=%s)";

    private DirContext context;
    private NamingEnumeration<SearchResult> searchResultEnum;
    private SearchResult searchResult;
    private Attributes attributes;
    private Attribute emailAttribute;
    private Attribute firstnameAttribute;
    private Attribute lastnameAttribute;
    private Logger logger;

    private LdapAccessor ldap;
    private ISLLConfig config;

    @Before
    public void setUp() throws NamingException {
        mockDirContext();
        initConfigs();
        mockLogger();
    }

    @Test
    public void searchIsCreatedProperly() throws Exception {
        initLdapAccessor();
        verify(context).search(eq(config.ldapBase), eq(String.format(FILTER_FORMAT, config.usernameAttribute, USERNAME)),
                any(SearchControls.class));
    }

    @Test
    public void ifAllLdapAttributesAreProvidedThenTheyAreAllFetched() throws LdapAuthException {
        initLdapAccessor();
        verify(attributes).get(config.emailAttribute);
        verify(attributes).get(config.firstNameAttribute);
        verify(attributes).get(config.lastNameAttribute);
        verifyNoMoreInteractions(attributes);
    }

    @Test
    public void ifAnLdapAttributeIsNotInTheConfigThenItReturnsAnEmptyStringAndAWarnIsLogged() throws LdapAuthException {
        config.emailAttribute = null;
        initLdapAccessor();
        assertEquals(EMPTY_STRING, ldap.getEmail());
        verify(logger).warn(anyString());
    }

    @Test(expected = LdapAuthException.class)
    @SuppressWarnings("unchecked")
    public void ifANameExceptionIsThrownThenAuthFails() throws Exception {
        when(context.search(anyString(), anyString(), any(SearchControls.class))).thenThrow(NamingException.class);
        initLdapAccessor();
    }

    @Test
    public void ifAnAttributeCannotBeMappedThenAWarnIsLogged() throws LdapAuthException {
        when(attributes.get(CONFIG_LDAP_EMAIL_ATTR)).thenReturn(null);
        initLdapAccessor();
        verify(logger).warn(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ifAValueCannotBeFetchedThenAnErrorIsLogged() throws Exception {
        when(emailAttribute.get()).thenThrow(NamingException.class);
        initLdapAccessor();
        verify(logger).error(anyString());
    }

    @Test
    public void fieldsAreMappedProperly() throws LdapAuthException {
        initLdapAccessor();

        assertEquals(USERNAME, ldap.getUsername());
        assertEquals(EMAIL_VALUE, ldap.getEmail());
        assertEquals(FIRSTNAME_VALUE, ldap.getFirstname());
        assertEquals(LASTNAME_VALUE, ldap.getLastname());
    }

    @Test
    public void passwordIsHashedWithSha256() throws LdapAuthException {
        initLdapAccessor();
        assertEquals(PASSWORD_SHA256, ldap.getPasswordHash());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ifNextResultThrowsAnExceptionThenANullObjectIsUsedAndAllFieldsAreEmptyStrings() throws Exception {
        when(searchResultEnum.next()).thenThrow(NamingException.class);
        initLdapAccessor();

        assertEquals(USERNAME, ldap.getUsername());
        assertEquals(EMPTY_STRING, ldap.getEmail());
        assertEquals(EMPTY_STRING, ldap.getFirstname());
        assertEquals(EMPTY_STRING, ldap.getLastname());
    }

    private void initLdapAccessor() throws LdapAuthException {
        ldap = new LdapAccessor(context, USERNAME, PASSWORD, config, logger);
    }

    private void mockDirContext() throws NamingException {
        mockSearchResult();
        context = mock(DirContext.class);
        when(context.search(anyString(), anyString(), any(SearchControls.class))).thenReturn(searchResultEnum);
    }

    @SuppressWarnings("unchecked")
    private void mockSearchResult() throws NamingException {
        mockAttributes();
        searchResultEnum = mock(NamingEnumeration.class);
        searchResult = mock(SearchResult.class);

        when(searchResultEnum.hasMore()).thenReturn(true).thenReturn(false);
        when(searchResultEnum.next()).thenReturn(searchResult);

        when(searchResult.getAttributes()).thenReturn(attributes);
    }

    private void mockAttributes() throws NamingException {
        mockAttribute();
        attributes = mock(Attributes.class);
        when(attributes.get(CONFIG_LDAP_EMAIL_ATTR)).thenReturn(emailAttribute);
        when(attributes.get(CONFIG_LDAP_FIRSTNAME_ATTR)).thenReturn(firstnameAttribute);
        when(attributes.get(CONFIG_LDAP_LASTNAME_ATTR)).thenReturn(lastnameAttribute);
    }

    private void mockAttribute() throws NamingException {
        emailAttribute = mock(Attribute.class);
        when(emailAttribute.get()).thenReturn(EMAIL_VALUE);

        firstnameAttribute = mock(Attribute.class);
        when(firstnameAttribute.get()).thenReturn(FIRSTNAME_VALUE);

        lastnameAttribute = mock(Attribute.class);
        when(lastnameAttribute.get()).thenReturn(LASTNAME_VALUE);
    }

    private void initConfigs() {
        config = new ISLLConfig();
        config.ldapBase = CONFIG_LDAP_BASE;
        config.usernameAttribute = CONFIG_LDAP_USERNAME_ATTR;
        config.firstNameAttribute = CONFIG_LDAP_FIRSTNAME_ATTR;
        config.emailAttribute = CONFIG_LDAP_EMAIL_ATTR;
        config.lastNameAttribute = CONFIG_LDAP_LASTNAME_ATTR;
    }

    private void mockLogger() {
        logger = mock(Logger.class);
    }
}
