package com.jnispace.isll.services;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;

import com.jnispace.isll.config.ISLLConfig;
import com.jnispace.isll.services.exceptions.ConnectionToIcescrumException;

public class LdapToIcescrumAdapterTest {
    private static final String USERNAME = "username from ldap";
    private static final String FIRST_NAME = "first name from ldap";
    private static final String LAST_NAME = "last name from ldap";
    private static final String EMAIL = "email from ldap";
    private static final String PASSWORD_HASH = "password hash from ldap";
    private static final String USER_UID_MD5 = "91b41ab81bfd696af363fe9238b027ad";

    private static final int NO_ROW_AFFECTED = 0;
    private static final int ONE_ROW_AFFECTED = 1;

    private LdapAccessor accessor;
    private ISLLConfig config;
    private Connection conn;
    private Logger logger;
    private PreparedStatement selectStatement;
    private PreparedStatement updateStatement;
    private PreparedStatement insertUserStatement;
    private PreparedStatement insertPreferencesStatement;
    private ResultSet selectResult;
    private ResultSet preferencesGeneratedKeys;

    private LdapToIcescrumAdapter adapter;

    @Before
    public void setUp() throws SQLException {
        accessor = mock(LdapAccessor.class);
        config = new ISLLConfig();
        conn = mock(Connection.class);
        logger = mock(Logger.class);
        adapter = new LdapToIcescrumAdapter(accessor, config, conn, logger);
        configureSqlMocks();
        configureAccessor();
    }

    @Test
    public void insertOrUpdateFirstChecksIfTheUserExists() throws SQLException {
        adapter.insertOrUpdateUser();

        InOrder order = inOrder(conn, selectStatement);
        order.verify(conn).prepareStatement(anySelectStatement());
        order.verify(selectStatement).setString(1, accessor.getUsername());
        order.verify(selectStatement).executeQuery();
    }

    @Test
    public void insertOrUpdateChosesUpdateIfARowAlreadyExists() throws SQLException {
        userAlreadyExists();
        adapter = spy(adapter);

        adapter.insertOrUpdateUser();

        verify(adapter).updateUser();
    }

    @Test
    public void insertOrUpdateChosesInsertIfARowDoesntExist() throws SQLException {
        userDoesntExist();
        adapter = spy(adapter);

        adapter.insertOrUpdateUser();

        verify(adapter).insertUser();
    }

    @Test
    public void updateAlwaysSetsPasswordNotExpired() throws SQLException {
        userAlreadyExists();

        adapter.updateUser();

        InOrder order = inOrder(conn, updateStatement);
        order.verify(conn, atLeastOnce()).prepareStatement(contains(LdapToIcescrumAdapter.FIELD_PASSWORD_EXPIRED));
    }

    @Test
    public void updateAlwaysSetsPassword() throws SQLException {
        adapter.updateUser();

        verify(conn, atLeastOnce()).prepareStatement(contains(LdapToIcescrumAdapter.FIELD_PASSWORD));
    }

    @Test
    public void updateSetsEmailIfItsInTheConfig() throws SQLException {
        config.emailAttribute = "email";

        adapter.updateUser();

        verify(conn, atLeastOnce()).prepareStatement(contains(LdapToIcescrumAdapter.FIELD_EMAIL));
    }

    @Test
    public void updateDoesntSetEmailIfItsNotInTheConfig() throws SQLException {
        adapter.updateUser();

        verify(conn, atLeastOnce()).prepareStatement(argThat(new DoesNotMatch(LdapToIcescrumAdapter.FIELD_EMAIL)));
    }

    @Test
    public void updateSetsFirstNameIfItsInTheConfig() throws SQLException {
        config.firstNameAttribute = "firstName";

        adapter.updateUser();

        verify(conn, atLeastOnce()).prepareStatement(contains(LdapToIcescrumAdapter.FIELD_FIRST_NAME));
    }

    @Test
    public void updateDoesntSetFirstNameIfItsNotInTheConfig() throws SQLException {
        adapter.updateUser();

        verify(conn, atLeastOnce()).prepareStatement(argThat(new DoesNotMatch(LdapToIcescrumAdapter.FIELD_FIRST_NAME)));
    }

    @Test
    public void updateSetsLastNameIfItsInTheConfig() throws SQLException {
        config.lastNameAttribute = "last name";

        adapter.updateUser();

        verify(conn, atLeastOnce()).prepareStatement(contains(LdapToIcescrumAdapter.FIELD_LAST_NAME));
    }

    @Test
    public void updateDoesntSetLastNameIfItsNotInTheConfig() throws SQLException {
        adapter.updateUser();

        verify(conn, atLeastOnce()).prepareStatement(argThat(new DoesNotMatch(LdapToIcescrumAdapter.FIELD_LAST_NAME)));
    }

    @Test
    public void insertFirstCreateANewPreference() throws SQLException {
        adapter.insertUser();

        InOrder order = inOrder(insertUserStatement, insertPreferencesStatement);
        order.verify(insertPreferencesStatement).executeUpdate();
        order.verify(insertUserStatement).executeUpdate();
    }

    @Test
    public void insertAddsAllNecessaryUserFields() throws SQLException {
        final int preferenceKey = 123;
        when(preferencesGeneratedKeys.getInt(anyInt())).thenReturn(preferenceKey);

        adapter.insertUser();

        // expired, locked, password expired
        verify(insertUserStatement, times(3)).setInt(anyInt(), eq(0));
        // enabled
        verify(insertUserStatement).setInt(anyInt(), eq(1));
        verify(insertUserStatement).setInt(anyInt(), eq(LdapToIcescrumAdapter.USER_VERSION));
        verify(insertUserStatement).setInt(anyInt(), eq(1));
        // created_date, last_updated
        verify(insertUserStatement, times(2)).setString(anyInt(), anyMysqlDate());
        verify(insertUserStatement).setInt(anyInt(), eq(preferenceKey));
        verify(insertUserStatement).setString(anyInt(), eq(USERNAME));
        verify(insertUserStatement).setString(anyInt(), eq(FIRST_NAME));
        verify(insertUserStatement).setString(anyInt(), eq(LAST_NAME));
        verify(insertUserStatement).setString(anyInt(), eq(EMAIL));
        verify(insertUserStatement).setString(anyInt(), eq(PASSWORD_HASH));
        verify(insertUserStatement).setString(anyInt(), eq(USER_UID_MD5));
    }

    @Test
    public void insertAddsAllNecessaryUserPreferenceFields() throws SQLException {
        adapter.insertUser();

        verify(insertPreferencesStatement).setInt(anyInt(), eq(LdapToIcescrumAdapter.USER_VERSION));
        verify(insertPreferencesStatement).setString(anyInt(), eq(LdapToIcescrumAdapter.ALL_TASKS));
        verify(insertPreferencesStatement).setString(anyInt(), eq(LdapToIcescrumAdapter.DEFAULT_USER_LANGUAGE));
        verify(insertPreferencesStatement).setInt(anyInt(), eq(0)); // hide_done_state
    }

    @Test(expected = ConnectionToIcescrumException.class)
    public void exceptionIsThrownIfPreferencesCannotBeInserted() throws Exception {
        userDoesntExist();
        when(insertPreferencesStatement.executeUpdate()).thenReturn(NO_ROW_AFFECTED);

        adapter.insertOrUpdateUser();
    }

    @Test(expected = ConnectionToIcescrumException.class)
    public void exceptionIsThrownIfUserCannotBeInserted() throws Exception {
        userDoesntExist();
        when(insertUserStatement.executeUpdate()).thenReturn(NO_ROW_AFFECTED);

        adapter.insertOrUpdateUser();
    }

    @Test
    public void noExceptionIsThrownIfUserCannotBeUpdated() throws SQLException {
        userAlreadyExists();
        when(updateStatement.executeUpdate()).thenReturn(NO_ROW_AFFECTED);

        adapter.insertOrUpdateUser();
    }

    @Test(expected = ConnectionToIcescrumException.class)
    @SuppressWarnings("unchecked")
    public void connectionToIcescrumExceptionIsThrowIfAnSqlExceptionHappensSomewhere() throws SQLException {
        when(selectStatement.executeQuery()).thenThrow(SQLException.class);
        adapter.insertOrUpdateUser();
    }

    private String anyMysqlDate() {
        return matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }

    private void userAlreadyExists() throws SQLException {
        when(selectResult.isBeforeFirst()).thenReturn(true);
    }

    private void userDoesntExist() throws SQLException {
        when(selectResult.isBeforeFirst()).thenReturn(false);
    }

    private void configureSqlMocks() throws SQLException {
        configureConnection();
        configureStatements();
    }

    private void configureConnection() throws SQLException {
        selectStatement = mock(PreparedStatement.class);
        updateStatement = mock(PreparedStatement.class);
        insertUserStatement = mock(PreparedStatement.class);
        insertPreferencesStatement = mock(PreparedStatement.class);
        when(conn.prepareStatement(anySelectStatement())).thenReturn(selectStatement);
        when(conn.prepareStatement(anyUpdateStatement())).thenReturn(updateStatement);
        when(conn.prepareStatement(anyInsertStatement())).thenReturn(insertUserStatement);
        when(conn.prepareStatement(anyInsertStatement(), anyInt())).thenReturn(insertPreferencesStatement);
    }

    private void configureStatements() throws SQLException {
        selectResult = mock(ResultSet.class);
        preferencesGeneratedKeys = mock(ResultSet.class);
        when(selectStatement.executeQuery()).thenReturn(selectResult);
        when(updateStatement.executeUpdate()).thenReturn(ONE_ROW_AFFECTED);
        when(insertUserStatement.executeUpdate()).thenReturn(ONE_ROW_AFFECTED);
        when(insertPreferencesStatement.executeUpdate()).thenReturn(ONE_ROW_AFFECTED);
        when(insertPreferencesStatement.getGeneratedKeys()).thenReturn(preferencesGeneratedKeys);
    }

    private String anySelectStatement() {
        return startsWith("SELECT");
    }

    private String anyUpdateStatement() {
        return startsWith("UPDATE");
    }

    private String anyInsertStatement() {
        return startsWith("INSERT");
    }

    private void configureAccessor() {
        when(accessor.getEmail()).thenReturn(EMAIL);
        when(accessor.getFirstname()).thenReturn(FIRST_NAME);
        when(accessor.getLastname()).thenReturn(LAST_NAME);
        when(accessor.getUsername()).thenReturn(USERNAME);
        when(accessor.getPasswordHash()).thenReturn(PASSWORD_HASH);
    }

    class DoesNotMatch extends ArgumentMatcher<String> {
        private String without;

        public DoesNotMatch(String without) {
            this.without = without;
        }

        @Override
        public boolean matches(Object s) {
            return !((String) s).contains(without);
        }
    }
}
