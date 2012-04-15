package com.jnispace.isll.facade;

import java.util.Hashtable;

import javax.naming.Context;

import org.apache.log4j.Logger;

import com.jnispace.isll.config.ISLLConfig;
import com.jnispace.isll.facade.dto.ResponseDTO;
import com.jnispace.isll.services.LdapAuthentifier;
import com.jnispace.isll.services.exceptions.LdapAuthException;

public class ISLLLoginFacade {
    protected final static String LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    protected final static String LDAP_AUTH_METHOD = "simple";
    protected final static String LDAP_TIMEOUT = "2000";

    public final static String ERROR_INVALID_CREDENTIALS = "Invalid credentials";
    public final static String ERROR_UNKNOWN = "An unknown error happened";

    private ISLLConfig config;
    private Logger logger;

    public ISLLLoginFacade(ISLLConfig config) {
        this.config = config;
        this.logger = Logger.getLogger("file");
    }

    public ResponseDTO authenticate(String username, String password) {
        LdapAuthentifier auth = getLdapAuthentifier(username, password);
        try {
            auth.authenticate();
            return handleSuccess();
        } catch (LdapAuthException e) {
            return handleLdapAuthException(username, e);
        } catch (Exception e) {
            return handleGenericException(e);
        }
    }

    protected LdapAuthentifier getLdapAuthentifier(String username, String password) {
        Hashtable<String, String> env = getEnvironment(username, password);
        return new LdapAuthentifier(env, username, password, config);
    }

    protected Hashtable<String, String> getEnvironment(String username, String password) {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, config.ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, LDAP_AUTH_METHOD);
        env.put(Context.SECURITY_PRINCIPAL, config.principalIdentifierAttribute + "=" + username + "," + config.ldapBase);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put("com.sun.jndi.ldap.read.timeout", LDAP_TIMEOUT);
        return env;
    }

    private ResponseDTO handleSuccess() {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.type = ResponseDTO.SUCCESS;
        return responseDTO;
    }

    private ResponseDTO handleLdapAuthException(String username, LdapAuthException e) {
        logger.error("Invalid credentials for `" + username + "`", e.getCause());

        ResponseDTO responseDTO = new ResponseDTO();
        if (config.strict) {
            responseDTO.type = ResponseDTO.ERROR;
            responseDTO.message = ERROR_INVALID_CREDENTIALS;
        } else {
            responseDTO.type = ResponseDTO.PASS_THROUGH;
        }
        return responseDTO;
    }

    private ResponseDTO handleGenericException(Exception e) {
        logger.error("Error got to the catch-all (aka 'the fuck happened?') logger", e);

        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.type = ResponseDTO.ERROR;
        responseDTO.message = ERROR_UNKNOWN;
        return responseDTO;
    }

    protected ISLLLoginFacade(ISLLConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }
}
