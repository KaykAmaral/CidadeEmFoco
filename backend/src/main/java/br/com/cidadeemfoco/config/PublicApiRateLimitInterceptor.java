package br.com.cidadeemfoco.config;

import br.com.cidadeemfoco.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PublicApiRateLimitInterceptor implements HandlerInterceptor {

    private final boolean enabled;
    private final int requestsPerMinute;
    private final JsonMapper jsonMapper;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public PublicApiRateLimitInterceptor(
            @Value("${app.public-api.rate-limit.enabled:true}") boolean enabled,
            @Value("${app.public-api.rate-limit.requests-per-minute:60}") int requestsPerMinute,
            JsonMapper jsonMapper
    ) {
        if (requestsPerMinute < 1) {
            throw new IllegalArgumentException("PUBLIC_API_RATE_LIMIT_PER_MINUTE deve ser maior que zero");
        }
        this.enabled = enabled;
        this.requestsPerMinute = requestsPerMinute;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!enabled) {
            return true;
        }

        long minute = Instant.now().getEpochSecond() / 60;
        Window window = windows.compute(request.getRemoteAddr(), (key, current) ->
                current == null || current.minute() != minute
                        ? new Window(minute, new AtomicInteger(1))
                        : increment(current)
        );
        int used = window.count().get();
        response.setHeader("X-RateLimit-Limit", Integer.toString(requestsPerMinute));
        response.setHeader("X-RateLimit-Remaining", Integer.toString(Math.max(0, requestsPerMinute - used)));
        response.setHeader("X-RateLimit-Reset", Long.toString((minute + 1) * 60));

        if (used <= requestsPerMinute) {
            return true;
        }

        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", Long.toString(60 - (Instant.now().getEpochSecond() % 60)));
        jsonMapper.writeValue(response.getOutputStream(), new ApiErrorResponse(
                Instant.now(), 429, "Too Many Requests",
                "Limite de requisicoes da API publica excedido",
                request.getRequestURI(), Map.of()
        ));
        return false;
    }

    private Window increment(Window window) {
        window.count().incrementAndGet();
        return window;
    }

    private record Window(long minute, AtomicInteger count) {
    }
}
