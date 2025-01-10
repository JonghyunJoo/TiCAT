package com.example.apigatewayservice.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Base64;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    Environment environment;

    public AuthorizationHeaderFilter(Environment environment) {
        super(Config.class);
        this.environment = environment;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "No authorization header");
            }

            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = authorizationHeader.replace("Bearer ", "");

            String subject = getSubjectFromJwt(jwt);
            if (subject == null || subject.isEmpty()) {
                return onError(exchange, "JWT token is not valid");
            }

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", subject)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

            return chain.filter(mutatedExchange);
        };
    }

    private String getSubjectFromJwt(String jwt) {
        byte[] secretKeyBytes = Base64.getEncoder().encode(environment.getProperty("token.secret").getBytes());
        SecretKey signingKey = Keys.hmacShaKeyFor(secretKeyBytes);

        try {
            // JWT 유효성 검증
            JwtParser jwtParser = Jwts.parser().
                    verifyWith(signingKey).build();

            Jws<Claims> claimsJws = jwtParser.parseSignedClaims(jwt);
            Claims payload = claimsJws.getPayload();

            return payload.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }


    private Mono<Void> onError(ServerWebExchange exchange, String noAuthorizationHeader) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        log.error(noAuthorizationHeader);
        return response.setComplete();
    }

    public static class Config {

    }
}
