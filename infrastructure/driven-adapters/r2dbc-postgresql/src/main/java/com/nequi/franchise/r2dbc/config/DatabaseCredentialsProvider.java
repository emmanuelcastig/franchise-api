package com.nequi.franchise.r2dbc.config;

import reactor.core.publisher.Mono;

/**
 * Resolves database credentials lazily. The driver subscribes once per new physical connection, so rotated
 * credentials are picked up without restarting the service.
 */
@FunctionalInterface
public interface DatabaseCredentialsProvider {

    Mono<DatabaseCredentials> credentials();
}
