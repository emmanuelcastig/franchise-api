package com.nequi.franchise.r2dbc.config;

/**
 * Shape of the RDS-managed master user secret stored in AWS Secrets Manager.
 */
public record DatabaseCredentials(String username, String password) {

    @Override
    public String toString() {
        return "DatabaseCredentials[username=" + username + ", password=****]";
    }
}
