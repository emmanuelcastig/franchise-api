package com.nequi.franchise.r2dbc.config;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface DatabaseCredentialsProvider {

    Mono<DatabaseCredentials> credentials();
}
