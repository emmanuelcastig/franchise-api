package com.nequi.franchise.r2dbc.config;

public record DatabaseCredentials(String username, String password) {

    @Override
    public String toString() {
        return "DatabaseCredentials[username=" + username + ", password=****]";
    }
}
