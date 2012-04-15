package com.jnispace.isll.services;

import static org.mockito.Mockito.*;

import java.util.Hashtable;

import javax.naming.directory.InitialDirContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.jnispace.isll.config.ISLLConfig;
import com.jnispace.isll.services.exceptions.LdapAuthException;

public class LdapAuthentifierTest {
    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";

    private Hashtable<String, String> hashtable;
    private ISLLConfig config;
    private InitialDirContext context;

    private LdapAuthentifier authentifier;

    @Before
    public void setUp() throws LdapAuthException {
        hashtable = new Hashtable<String, String>();
        config = new ISLLConfig();
        context = mock(InitialDirContext.class);
        initAuthentifierSpy();
    }

    @Test
    public void nothingIsThrownIfLdapAndIcescrumPartsDontThrow() throws Exception {
        authentifier.authenticate();

        InOrder order = inOrder(authentifier, context);
        order.verify(authentifier).ldapLogin();
        order.verify(authentifier).updateIcescrum(context);
        order.verify(context).close();
    }

    @Test(expected = LdapAuthException.class)
    public void authFailsIfLdapConnectionFailsAndContextIsntClosedSinceItWasntCreated() throws Exception {
        doThrow(LdapAuthException.class).when(authentifier).ldapLogin();

        try {
            authentifier.authenticate();
        } catch (LdapAuthException e) {
            verify(context, times(0)).close();
            throw e;
        }
    }

    @Test(expected = LdapAuthException.class)
    public void authFailsIfIcescrumWasNotUpdatedButContextIsStillClosed() throws Exception {
        doThrow(LdapAuthException.class).when(authentifier).updateIcescrum(context);

        try {
            authentifier.authenticate();
        } catch (LdapAuthException e) {
            verify(context).close();
            throw e;
        }
    }

    private void initAuthentifierSpy() throws LdapAuthException {
        LdapAuthentifier toSpy = new LdapAuthentifier(hashtable, USERNAME, PASSWORD, config);
        authentifier = spy(toSpy);
        doReturn(context).when(authentifier).ldapLogin();
        doNothing().when(authentifier).updateIcescrum(context);
    }
}
