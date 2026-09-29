package zz.sc.infrastructure.rest.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import zz.sc.infrastructure.persistence.entity.IdempotencyRecordEntity;
import zz.sc.infrastructure.persistence.repository.JpaIdempotencyRecordRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private final JpaIdempotencyRecordRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKeyStr = request.getHeader("X-Idempotency-Key");
        if (idempotencyKeyStr == null || idempotencyKeyStr.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        UUID idempotencyKey;
        try {
            idempotencyKey = UUID.fromString(idempotencyKeyStr);
        } catch (IllegalArgumentException e) {
            sendProblemDetail(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "X-Idempotency-Key must be a valid UUID");
            return;
        }

        Optional<IdempotencyRecordEntity> existingRecordOpt = repository.findById(idempotencyKey);
        if (existingRecordOpt.isPresent()) {
            IdempotencyRecordEntity existingRecord = existingRecordOpt.get();
            if (existingRecord.getResponseStatus() == null) {
                sendProblemDetail(response, HttpServletResponse.SC_CONFLICT, "Conflict", "Request is currently being processed");
                return;
            }
            
            response.setStatus(existingRecord.getResponseStatus());
            response.setContentType("application/json");
            if (existingRecord.getResponseBody() != null) {
                response.getWriter().write(existingRecord.getResponseBody());
            }
            return;
        }

        try {
            IdempotencyRecordEntity record = IdempotencyRecordEntity.builder()
                    .idempotencyKey(idempotencyKey)
                    .createdAt(Instant.now())
                    .build();
            repository.saveAndFlush(record);
        } catch (DataIntegrityViolationException e) {
            sendProblemDetail(response, HttpServletResponse.SC_CONFLICT, "Conflict", "Request is currently being processed");
            return;
        }

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(request, responseWrapper);
        } finally {
            int status = responseWrapper.getStatus();
            byte[] responseArray = responseWrapper.getContentAsByteArray();
            String responseBody = new String(responseArray, responseWrapper.getCharacterEncoding());

            repository.findById(idempotencyKey).ifPresent(record -> {
                record.setResponseStatus(status);
                record.setResponseBody(responseBody);
                repository.save(record);
            });

            responseWrapper.copyBodyToResponse();
        }
    }

    private void sendProblemDetail(HttpServletResponse response, int status, String title, String detail) throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        String json = String.format("{\"status\":%d,\"title\":\"%s\",\"detail\":\"%s\"}", status, title, detail);
        response.getWriter().write(json);
    }
}
