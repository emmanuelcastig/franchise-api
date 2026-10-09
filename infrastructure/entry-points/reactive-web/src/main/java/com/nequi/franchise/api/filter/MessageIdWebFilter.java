package com.nequi.franchise.api.filter;

import io.micrometer.tracing.BaggageInScope;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import static com.nequi.franchise.api.utils.Constants.Headers.X_MESSAGE_ID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class MessageIdWebFilter implements WebFilter {

    private final Tracer tracer;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String messageId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(X_MESSAGE_ID))
                .filter(Predicate.not(String::isBlank))
                .orElseGet(() -> UUID.randomUUID().toString());
        exchange.getResponse().getHeaders().set(X_MESSAGE_ID, messageId);
        return Mono.using(
                        () -> tracer.createBaggageInScope(X_MESSAGE_ID, messageId),
                        baggage -> chain.filter(exchange),
                        BaggageInScope::close)
                .contextWrite(context -> context.put(X_MESSAGE_ID, messageId));
    }
}
