package com.jnispace.isll.services;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.log4j.Logger;

import com.jnispace.isll.config.ISLLConfig;
import com.jnispace.isll.services.exceptions.LdapAuthException;

public class LdapAuthentifier {

    private Hashtable<String, String> env;
    private String username;
    private String password;
    private ISLLConfig config;

    public LdapAuthentifier(Hashtable<String, String> env, String username, String password, ISLLConfig config) {
        this.env = env;
        this.username = username;
        this.password = password;
        this.config = config;
    }

    public void authenticate() throws LdapAuthException, NamingException {
        DirContext ctx = null;
        try {
            ctx = ldapLogin();
            updateIcescrum(ctx);
        } finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    protected DirContext ldapLogin() throws LdapAuthException {
        DirContext ctx = null;
        try {
            Logger.getLogger("file").debug("Authenticating user : " + username);
            ctx = new InitialDirContext(env);
            return ctx;
        } catch (Exception e) {
            throw new LdapAuthException(e);
        }
    }

    protected void updateIcescrum(DirContext ctx) throws LdapAuthException {
        Logger.getLogger("file").info("Authenticated user : " + username);
        LdapAccessor accessor = new LdapAccessor(ctx, username, password, config);
        new LdapToIcescrumAdapter(accessor, config).insertOrUpdateUser();
        Logger.getLogger("file").info("Sync'd user with icescrum : " + username);
    }
}
