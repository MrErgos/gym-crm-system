package io.github.mrergos.workinghours.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionLoggingFilter")
class TransactionLoggingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private final TransactionLoggingFilter filter = new TransactionLoggingFilter();

    @Test
    @DisplayName("reuses incoming X-Transaction-Id header when present")
    void reusesIncomingTransactionId() throws Exception {
        when(request.getHeader("X-Transaction-Id")).thenReturn("existing-tx-id");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/trainers/John.Doe/workload");
        when(response.getStatus()).thenReturn(200);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(eq("X-Transaction-Id"), eq("existing-tx-id"));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("generates a new transaction id when header is missing")
    void generatesNewTransactionIdWhenMissing() throws Exception {
        when(request.getHeader("X-Transaction-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/trainers/workload");
        when(response.getStatus()).thenReturn(200);

        ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(eq("X-Transaction-Id"), headerValueCaptor.capture());
        String generatedId = headerValueCaptor.getValue();
        assertThat(generatedId).isNotBlank();
        assertThat(generatedId).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("generates a new transaction id when header is blank")
    void generatesNewTransactionIdWhenBlank() throws Exception {
        when(request.getHeader("X-Transaction-Id")).thenReturn("   ");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/trainers/John.Doe/workload");
        when(response.getStatus()).thenReturn(200);

        ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(eq("X-Transaction-Id"), headerValueCaptor.capture());
        assertThat(headerValueCaptor.getValue()).isNotEqualTo("   ");
    }

    @Test
    @DisplayName("continues the filter chain regardless of transaction id resolution")
    void alwaysContinuesFilterChain() throws Exception {
        when(request.getHeader("X-Transaction-Id")).thenReturn("tx-1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/trainers/John.Doe/workload");
        when(response.getStatus()).thenReturn(200);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("still continues chain and cleans up when downstream filter throws")
    void continuesAndCleansUpOnDownstreamException() throws Exception {
        when(request.getHeader("X-Transaction-Id")).thenReturn("tx-err");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/trainers/John.Doe/workload");
        org.mockito.Mockito.doThrow(new RuntimeException("downstream failure"))
                .when(filterChain).doFilter(request, response);

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> filter.doFilterInternal(request, response, filterChain));

        // MDC should be cleaned up even when an exception is thrown (finally block)
        assertThat(org.slf4j.MDC.get("transactionId")).isNull();
    }
}
