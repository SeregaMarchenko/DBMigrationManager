package org.example;

import org.example.service.MigrationHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MigrationHistoryServiceTest {

    private MigrationHistoryService historyService;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    public void setUp() throws SQLException {
        historyService = new MigrationHistoryService();

        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    public void testGetMigrationsToRollback() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("script_name")).thenReturn("V1__Initial_Setup.sql");

        List<String> migrations = historyService.getMigrationsToRollback(connection, "1");
        assertEquals(1, migrations.size());
        assertEquals("V1__Initial_Setup.sql", migrations.get(0));
    }

    @Test
    public void testRecordMigration() throws SQLException {
        String migrationFile = "V1__Initial_Setup.sql";

        historyService.recordMigration(connection, migrationFile);

        verify(preparedStatement).setString(1, "1");
        verify(preparedStatement).setString(2, migrationFile);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void testRemoveMigrationRecord() throws SQLException {
        String migrationFile = "V1__Initial_Setup.sql";

        historyService.removeMigrationRecord(connection, migrationFile);

        verify(preparedStatement).setString(1, migrationFile);
        verify(preparedStatement).executeUpdate();
    }
}
