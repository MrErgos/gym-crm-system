package io.github.mrergos.workinghours.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TransactionLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(TransactionLoggingFilter.class);
    private static final String TRANSACTION_ID_MDC_KEY = "transactionId";
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String transactionId = resolveTransactionId(request);
        MDC.put(TRANSACTION_ID_MDC_KEY, transactionId);
        response.setHeader(TRANSACTION_ID_HEADER, transactionId);

        long startTime = System.currentTimeMillis();
        log.info("Request - {} {}", request.getMethod(), request.getRequestURI());
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("Response - {} {} status={} duration={}ms",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            MDC.remove(TRANSACTION_ID_MDC_KEY);
        }
    }

    private String resolveTransactionId(HttpServletRequest request) {
        String incomingTransactionId = request.getHeader(TRANSACTION_ID_HEADER);
        return (incomingTransactionId != null && !incomingTransactionId.isBlank())
                ? incomingTransactionId
                : UUID.randomUUID().toString();
    }
}
