package org.example;

import org.example.service.MigrationLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MigrationLockServiceTest {

    private MigrationLockService lockService;
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    @BeforeEach
    public void setUp() throws SQLException {
        lockService = new MigrationLockService();

        connection = mock(Connection.class);
        statement = mock(Statement.class);
        resultSet = mock(ResultSet.class);

        when(connection.createStatement()).thenReturn(statement);
    }

    @Test
    public void testIsLocked() throws SQLException {
        when(statement.executeQuery("SELECT locked FROM migration_lock WHERE id = 1")).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean("locked")).thenReturn(true);

        boolean isLocked = lockService.isLocked(connection);

        assertTrue(isLocked);
        verify(statement).executeQuery("SELECT locked FROM migration_lock WHERE id = 1");
        verify(resultSet).next();
        verify(resultSet).getBoolean("locked");
    }

    @Test
    public void testIsNotLocked() throws SQLException {
        when(statement.executeQuery("SELECT locked FROM migration_lock WHERE id = 1")).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean("locked")).thenReturn(false);

        boolean isLocked = lockService.isLocked(connection);

        assertFalse(isLocked);
        verify(statement).executeQuery("SELECT locked FROM migration_lock WHERE id = 1");
        verify(resultSet).next();
        verify(resultSet).getBoolean("locked");
    }

    @Test
    public void testIsLockedSQLException() throws SQLException {
        when(statement.executeQuery("SELECT locked FROM migration_lock WHERE id = 1")).thenThrow(new SQLException());

        assertThrows(RuntimeException.class, () -> lockService.isLocked(connection));
        verify(statement).executeQuery("SELECT locked FROM migration_lock WHERE id = 1");
    }

    @Test
    public void testLock() throws SQLException {
        lockService.lock(connection);

        verify(statement).execute("INSERT INTO migration_lock (id, locked) VALUES (1, TRUE) " +
                "ON CONFLICT (id) DO UPDATE SET locked = EXCLUDED.locked, locked_at = CURRENT_TIMESTAMP");
    }

    @Test
    public void testLockSQLException() throws SQLException {
        doThrow(new SQLException()).when(statement).execute(anyString());

        assertThrows(RuntimeException.class, () -> lockService.lock(connection));
        verify(statement).execute("INSERT INTO migration_lock (id, locked) VALUES (1, TRUE) " +
                "ON CONFLICT (id) DO UPDATE SET locked = EXCLUDED.locked, locked_at = CURRENT_TIMESTAMP");
    }

    @Test
    public void testUnlock() throws SQLException {
        lockService.unlock(connection);

        verify(statement).execute("UPDATE migration_lock SET locked = FALSE, locked_at = CURRENT_TIMESTAMP WHERE id = 1");
    }

    @Test
    public void testUnlockSQLException() throws SQLException {
        doThrow(new SQLException()).when(statement).execute(anyString());

        assertThrows(RuntimeException.class, () -> lockService.unlock(connection));
        verify(statement).execute("UPDATE migration_lock SET locked = FALSE, locked_at = CURRENT_TIMESTAMP WHERE id = 1");
    }
}
