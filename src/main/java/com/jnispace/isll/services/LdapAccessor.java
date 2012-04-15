package com.jnispace.isll.services;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.jnispace.isll.config.ConfigFieldValidator;
import com.jnispace.isll.config.ISLLConfig;
import com.jnispace.isll.services.exceptions.LdapAuthException;
import com.jnispace.isll.services.nullobjects.SearchResultNullObject;

public class LdapAccessor {

    private DirContext ctx;
    private ISLLConfig config;
    private Logger logger;

    private String passwordHash;
    private String username;
    private String firstname;
    private String lastname;
    private String email;

    public LdapAccessor(DirContext ctx, String username, String password, ISLLConfig config) throws LdapAuthException {
        this.ctx = ctx;
        this.username = username;
        this.config = config;
        this.passwordHash = DigestUtils.sha256Hex(password);
        logger = Logger.getLogger("file");
        mapFields();
    }

    private void mapFields() throws LdapAuthException {
        NamingEnumeration<SearchResult> results = null;
        try {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            logger.debug("ldapsearch -b " + config.ldapBase + " -x (" + config.usernameAttribute + "=" + username + ")");

            results = ctx.search(config.ldapBase, "(" + config.usernameAttribute + "=" + username + ")", controls);
            while (results.hasMore()) {
                SearchResult searchResult = getNextResult(results);
                mapAttributes(searchResult.getAttributes());
            }
        } catch (NamingException e) {
            throw new LdapAuthException(e);
        }
    }

    private SearchResult getNextResult(NamingEnumeration<SearchResult> results) {
        try {
            return results.next();
        } catch (NamingException e) {
            // silence this, it just can't happen here.
            return new SearchResultNullObject();
        }
    }

    private void mapAttributes(Attributes attributes) {
        firstname = safeGet(attributes, config.firstNameAttribute);
        lastname = safeGet(attributes, config.lastNameAttribute);
        email = safeGet(attributes, config.emailAttribute);
    }

    private String safeGet(Attributes attributes, String attrID) {
        if (!ConfigFieldValidator.shouldInclude(attrID)) {
            logger.warn("Skipping mapping of `"
                    + attrID
                    + "` because you left it empty. If user does not exist, this field will be inserted blank and can cause icescrum to hate your face.");
            return "";
        }

        Attribute attribute = attributes.get(attrID);
        if (attribute != null) {
            String value;
            try {
                value = (String) attribute.get();
                logger.debug("LDAP: Mapping `" + attrID + "` to `" + value + "` for user `" + username + "`");
                return value;
            } catch (NamingException e) {
                logger.error("No such attribute `" + attrID + "` for user `" + username + "`");
            }
        } else {
            logger.warn("Could not map `" + attrID + "` for user `" + username + "`");
        }
        return "";
    }

    public String getUsername() {
        return username;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    protected LdapAccessor(DirContext ctx, String username, String password, ISLLConfig config, Logger logger) throws LdapAuthException {
        this.ctx = ctx;
        this.username = username;
        this.config = config;
        this.passwordHash = DigestUtils.sha256Hex(password);
        this.logger = logger;
        mapFields();
    }

}
