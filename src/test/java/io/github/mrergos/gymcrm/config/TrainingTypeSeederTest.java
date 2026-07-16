package io.github.mrergos.gymcrm.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingTypeSeeder tests")
class TrainingTypeSeederTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Resource dataSqlResource;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    private TrainingTypeSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new TrainingTypeSeeder(dataSource, dataSqlResource);
    }

    private void mockScript(String content) throws IOException {
        when(dataSqlResource.getInputStream())
                .thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("start: executes non-empty, non-comment statements and marks running")
    void start_validScript_shouldExecuteStatementsAndSetRunning() throws Exception {
        //given
        mockScript("-- comment line\nINSERT INTO training_types (training_type_name) VALUES ('Cardio');\nINSERT INTO training_types (training_type_name) VALUES ('Yoga');");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);

        //when
        seeder.start();

        //then
        assertTrue(seeder.isRunning());
        verify(statement).execute("INSERT INTO training_types (training_type_name) VALUES ('Cardio')");
        verify(statement).execute("INSERT INTO training_types (training_type_name) VALUES ('Yoga')");
    }

    @Test
    @DisplayName("start: skips blank and comment-only statements")
    void start_blankAndCommentStatements_shouldSkipThem() throws Exception {
        //given
        mockScript("   ;\n-- just a comment;\nINSERT INTO training_types (training_type_name) VALUES ('Cardio');");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);

        //when
        seeder.start();

        //then
        verify(statement).execute("INSERT INTO training_types (training_type_name) VALUES ('Cardio')");
    }

    @Test
    @DisplayName("start: SQLException during execution wraps into IllegalStateException")
    void start_sqlException_shouldThrowIllegalStateException() throws Exception {
        //given
        mockScript("INSERT INTO training_types (training_type_name) VALUES ('Cardio');");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.execute("INSERT INTO training_types (training_type_name) VALUES ('Cardio')"))
                .thenThrow(new SQLException("DB error"));

        //when //then
        assertThrows(IllegalStateException.class, () -> seeder.start());
        assertFalse(seeder.isRunning());
    }

    @Test
    @DisplayName("start: IOException reading script wraps into IllegalStateException")
    void start_ioExceptionReadingScript_shouldThrowIllegalStateException() throws IOException {
        //given
        when(dataSqlResource.getInputStream()).thenThrow(new IOException("Cannot read"));

        //when //then
        assertThrows(IllegalStateException.class, () -> seeder.start());
    }

    @Test
    @DisplayName("stop: sets running to false")
    void stop_shouldSetRunningFalse() throws Exception {
        //given
        mockScript("INSERT INTO training_types (training_type_name) VALUES ('Cardio');");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        seeder.start();

        //when
        seeder.stop();

        //then
        assertFalse(seeder.isRunning());
    }

    @Test
    @DisplayName("isRunning: false before start is called")
    void isRunning_beforeStart_shouldBeFalse() {
        //when //then
        assertFalse(seeder.isRunning());
    }

    @Test
    @DisplayName("getPhase: returns Integer.MIN_VALUE")
    void getPhase_shouldReturnMinValue() {
        //when //then
        assertTrue(seeder.getPhase() == Integer.MIN_VALUE);
    }
}