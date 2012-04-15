package com.jnispace.isll.services;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.jnispace.isll.config.ISLLConfig;
import com.jnispace.isll.services.exceptions.ConnectionToIcescrumException;

public class ConnectionProviderTest {
    private final static String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    private final static String INEXISTANT_DRIVER = "inexistant";
    private final static String OTHER_CONNECTION_STRING = "asdf";

    private ConnectionProvider provider;

    private Connection conn;
    private ISLLConfig config;

    @Before
    public void setUp() throws SQLException {
        createConnectionProvider();
        config = new ISLLConfig();
        config.jdbcClass = MYSQL_DRIVER;
    }

    @Test
    public void canGetConnectionFromConfig() {
        assertSame(conn, provider.get(config));
    }

    @Test(expected = ConnectionToIcescrumException.class)
    public void ifConnectionFailsThenAnotherExceptionIsThrown() throws SQLException {
        doThrow(SQLException.class).when(provider).getConnectionFromConfig(any(ISLLConfig.class));

        provider.get(config);
    }

    @Test(expected = ConnectionToIcescrumException.class)
    public void exceptionIsThrownIfDriverDoesNotExist() {
        config.jdbcClass = INEXISTANT_DRIVER;

        provider.get(config);
    }

    @Test
    public void connectionIsCachedAnOnlyCreatedOnce() throws SQLException {
        provider.get(config);
        provider.get(config);

        verify(provider).getConnectionFromConfig(config);
    }

    @Test
    public void connectionIsRecreatedIfItsClosed() throws SQLException {
        provider.get(config);

        when(conn.isClosed()).thenReturn(true);
        provider.get(config);

        verify(provider, times(2)).getConnectionFromConfig(config);
    }

    @Test(expected = ConnectionToIcescrumException.class)
    public void anExceptionIsThrownIfIsClosedThrowsOne() throws SQLException {
        doThrow(SQLException.class).when(conn).isClosed();
        provider.get(config);
    }

    @Test
    public void connectionsAreCachedByConfigJdbcConnectionString() throws SQLException {
        ISLLConfig config2 = new ISLLConfig();
        config2.jdbcClass = MYSQL_DRIVER;
        config2.jdbcConnection = OTHER_CONNECTION_STRING;

        provider.get(config);
        provider.get(config2);

        InOrder inOrder = inOrder(provider);
        inOrder.verify(provider).getConnectionFromConfig(config);
        inOrder.verify(provider).getConnectionFromConfig(config2);
    }

    @Test
    public void connectionProviderIsASingleton() {
        assertSame(ConnectionProvider.getInstance(), ConnectionProvider.getInstance());
    }

    private void createConnectionProvider() throws SQLException {
        provider = spy(new ConnectionProvider());
        conn = mock(Connection.class);
        doReturn(conn).when(provider).getConnectionFromConfig(any(ISLLConfig.class));
    }

}
