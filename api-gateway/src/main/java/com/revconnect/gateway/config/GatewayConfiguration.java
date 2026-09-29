package com.revconnect.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfiguration {
    @Bean
    KeyResolver clientKeyResolver() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("X-User-Id"))
                .switchIfEmpty(Mono.justOrEmpty(exchange.getRequest().getRemoteAddress()).map(Object::toString))
                .defaultIfEmpty("anonymous");
    }
}
