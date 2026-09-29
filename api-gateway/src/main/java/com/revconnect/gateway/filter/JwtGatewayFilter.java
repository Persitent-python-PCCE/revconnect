package com.revconnect.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtGatewayFilter implements org.springframework.cloud.gateway.filter.GlobalFilter {
    private static final List<String> PUBLIC_PATHS = List.of("/api/auth/", "/actuator", "/fallback/");
    private final SecretKey key;

    public JwtGatewayFilter(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (exchange.getRequest().getMethod() != null && exchange.getRequest().getMethod().name().equals("OPTIONS")
                || PUBLIC_PATHS.stream().anyMatch(path::startsWith)
                || (exchange.getRequest().getMethod() != null && exchange.getRequest().getMethod().name().equals("GET"))) {
            return chain.filter(exchange);
        }
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return unauthorized(exchange);
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(header.substring(7)).getPayload();
            String userId = String.valueOf(claims.get("userId"));
            ServerWebExchange authenticated = exchange.mutate().request(request -> request.headers(headers -> headers.set("X-User-Id", userId))).build();
            return chain.filter(authenticated);
        } catch (Exception ignored) { return unauthorized(exchange); }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
