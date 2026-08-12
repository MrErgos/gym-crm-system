package io.github.mrergos.gymcrm.health;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseHealthIndicator tests")
class DatabaseHealthIndicatorTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private NativeQuery<?> nativeQuery;

    @InjectMocks
    private DatabaseHealthIndicator databaseHealthIndicator;

    @Test
    @DisplayName("health: return up when database is reachable")
    void health_whenDatabaseReachable_shouldReturnUp() {
        // given
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenAnswer(invocationOnMock -> 1);

        // when
        Health result = databaseHealthIndicator.health();

        // then
        assertThat(result.getStatus()).isEqualTo(Status.UP);
        assertThat(result.getDetails()).containsEntry("database", "reachable");
    }

    @Test
    @DisplayName("health: return down when session factory throws an exception")
    void health_whenSessionFactoryThrowsException_shouldReturnDown() {
        // given
        when(sessionFactory.openSession()).thenThrow(new RuntimeException("Connection refused"));

        // when
        Health result = databaseHealthIndicator.health();

        // then
        assertThat(result.getStatus()).isEqualTo(Status.DOWN);
        assertThat(result.getDetails()).containsEntry("database", "unreachable");
        assertThat(result.getDetails()).containsKey("error");
    }

    @Test
    @DisplayName("health: return down when query execution fails")
    void health_whenQueryExecutionFails_shouldReturnDown() {
        // given
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenThrow(new RuntimeException("Query failed"));

        // when
        Health result = databaseHealthIndicator.health();

        // then
        assertThat(result.getStatus()).isEqualTo(Status.DOWN);
        assertThat(result.getDetails()).containsEntry("database", "unreachable");
    }
}