package com.example.gateway.config;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

@Configuration
public class CorrelationFilter {
    private static final String CID = "X-Correlation-Id";

    @Bean
    public GlobalFilter correlationIdFilter() {
        return (exchange, chain) -> {
            String cid = exchange.getRequest().getHeaders().getFirst(CID);
            if (cid == null || cid.isBlank()) {
                cid = UUID.randomUUID().toString();
            }
            final String correlationId = cid;
            exchange = exchange.mutate().request(r -> r.headers(h -> h.add(CID, correlationId))).build();
            MDC.put(CID, correlationId);
            return chain.filter(exchange).then(Mono.fromRunnable(MDC::clear));
        };
    }
}