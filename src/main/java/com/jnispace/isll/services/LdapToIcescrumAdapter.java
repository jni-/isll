package com.jnispace.isll.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.jnispace.isll.config.ConfigFieldValidator;
import com.jnispace.isll.config.ISLLConfig;
import com.jnispace.isll.services.exceptions.ConnectionToIcescrumException;

public class LdapToIcescrumAdapter {
    public static final int USER_VERSION = 4;
    public static final String ALL_TASKS = "allTasks";
    public static final String DEFAULT_USER_LANGUAGE = "en";

    public static final String USERS_TABLE = "icescrum2_user";
    public static final String USER_PREFERENCES_TABLE = "icescrum2_user_preferences";

    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_PASSWORD_EXPIRED = "password_expired";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_FIRST_NAME = "first_name";
    public static final String FIELD_LAST_NAME = "last_name";
    public static final String FIELD_PASSWORD = "passwd";
    public static final String FIELD_USER_VERSION = "version";
    public static final String FIELD_USER_PREF_VERSION = "version";
    public static final String FIELD_ACCOUNT_EXPIRED = "account_expired";
    public static final String FIELD_ACCOUNT_LOCKED = "account_locked";
    public static final String FIELD_DATE_CREATED = "date_created";
    public static final String FIELD_LAST_UPDATED = "last_updated";
    public static final String FIELD_ENABLED = "enabled";
    public static final String FIELD_PREFERENCES_ID = "preferences_id";
    public static final String FIELD_UID = "uid";
    public static final String FIELD_FILTER_TASK = "filter_task";
    public static final String FIELD_HIDE_DONE_STATE = "hide_done_state";
    public static final String FIELD_LANGUAGE = "language";

    private ISLLConfig config;
    private Connection connection;
    private LdapAccessor ldap;
    private Logger logger;

    public LdapToIcescrumAdapter(LdapAccessor accessor, ISLLConfig config) {
        this.config = config;
        this.ldap = accessor;
        this.connection = ConnectionProvider.getInstance().get(config);
        this.logger = Logger.getLogger("file");
    }

    public void insertOrUpdateUser() {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " + USERS_TABLE + " where " + FIELD_USERNAME + " = ?");
            stmt.setString(1, ldap.getUsername());
            ResultSet res = stmt.executeQuery();
            if (res.isBeforeFirst()) {
                updateUser();
            } else {
                insertUser();
            }
        } catch (SQLException e) {
            throw new ConnectionToIcescrumException("Could not locate / query the users table " + e.getMessage());
        }

    }

    protected void updateUser() throws SQLException {
        logger.debug("Updating user `" + ldap.getUsername() + "`");
        StringBuffer sql = new StringBuffer("UPDATE " + USERS_TABLE + " SET " + FIELD_PASSWORD_EXPIRED + " = 0");
        List<String> parameters = new LinkedList<String>();

        if (ConfigFieldValidator.shouldInclude(config.emailAttribute)) {
            sql.append(", " + FIELD_EMAIL + " = ?");
            parameters.add(ldap.getEmail());
        }

        if (ConfigFieldValidator.shouldInclude(config.firstNameAttribute)) {
            sql.append(", " + FIELD_FIRST_NAME + " = ?");
            parameters.add(ldap.getFirstname());
        }

        if (ConfigFieldValidator.shouldInclude(config.lastNameAttribute)) {
            sql.append(", " + FIELD_LAST_NAME + " = ?");
            parameters.add(ldap.getLastname());
        }

        sql.append(", " + FIELD_PASSWORD + " = ?");
        parameters.add(ldap.getPasswordHash());

        sql.append(" WHERE " + FIELD_USERNAME + "  = ?");
        parameters.add(ldap.getUsername());

        PreparedStatement stmt = connection.prepareStatement(sql.toString());
        for (int i = 0; i < parameters.size(); i++) {
            stmt.setString(i + 1, parameters.get(i));
        }

        stmt.executeUpdate();
    }

    protected void insertUser() throws SQLException {
        logger.debug("Inserting user `" + ldap.getUsername() + "`");

        StringBuffer sql = new StringBuffer("INSERT INTO "
                + USERS_TABLE
                + " ("
                + joinStrings(FIELD_USER_VERSION, FIELD_ACCOUNT_EXPIRED, FIELD_ACCOUNT_LOCKED, FIELD_DATE_CREATED, FIELD_LAST_UPDATED,
                        FIELD_ENABLED, FIELD_PASSWORD_EXPIRED, FIELD_USERNAME, FIELD_PREFERENCES_ID, FIELD_PASSWORD, FIELD_EMAIL,
                        FIELD_FIRST_NAME, FIELD_LAST_NAME, FIELD_UID) + ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        logger.debug(sql);
        PreparedStatement stmt = connection.prepareStatement(sql.toString());
        stmt.setInt(1, USER_VERSION);
        stmt.setInt(2, 0);
        stmt.setInt(3, 0);
        stmt.setString(4, getNowDateTime());
        stmt.setString(5, getNowDateTime());
        stmt.setInt(6, 1);
        stmt.setInt(7, 0);
        stmt.setString(8, ldap.getUsername());
        stmt.setInt(9, createPreferences());
        stmt.setString(10, ldap.getPasswordHash());
        stmt.setString(11, ldap.getEmail());
        stmt.setString(12, ldap.getFirstname());
        stmt.setString(13, ldap.getLastname());
        stmt.setString(14, computeUid());

        if (stmt.executeUpdate() == 0) {
            throw new ConnectionToIcescrumException("Could not create user " + ldap.getUsername()
                    + " for icescrum, which doesn't already exist.");
        }
    }

    private String joinStrings(String... strings) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(strings[i]);
        }
        return sb.toString();
    }

    private String getNowDateTime() {
        Calendar cal = Calendar.getInstance();
        String year = Integer.toString(cal.get(Calendar.YEAR));
        String month = pad(cal.get(Calendar.MONTH) + 1);
        String day = pad(cal.get(Calendar.DATE));
        String hour = pad(cal.get(Calendar.HOUR) + ((cal.get(Calendar.AM_PM) == Calendar.PM) ? 12 : 0));
        String minute = pad(cal.get(Calendar.MINUTE));
        String second = pad(cal.get(Calendar.SECOND));

        return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
    }

    private String pad(int i) {
        if (i < 10) {
            return "0" + i;
        } else {
            return Integer.toString(i);
        }
    }

    private int createPreferences() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO icescrum2_user_preferences("
                        + joinStrings(FIELD_USER_PREF_VERSION, FIELD_FILTER_TASK, FIELD_HIDE_DONE_STATE, FIELD_LANGUAGE)
                        + ") VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

        stmt.setInt(1, USER_VERSION);
        stmt.setString(2, ALL_TASKS);
        stmt.setInt(3, 0);
        stmt.setString(4, DEFAULT_USER_LANGUAGE);

        if (stmt.executeUpdate() == 0) {
            throw new ConnectionToIcescrumException("Could not init user preferences");
        }
        ResultSet keys = stmt.getGeneratedKeys();
        keys.next();
        return keys.getInt(1);
    }

    private String computeUid() {
        String toHash = ldap.getUsername() + ldap.getPasswordHash();
        return DigestUtils.md5Hex(toHash);
    }

    protected LdapToIcescrumAdapter(LdapAccessor accessor, ISLLConfig config, Connection conn, Logger logger) {
        this.config = config;
        this.ldap = accessor;
        this.connection = conn;
        this.logger = logger;
    }
}
