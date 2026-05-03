package com.origin.config;

import org.testcontainers.containers.PostgreSQLContainer;

public class CustomPostgresContainer extends PostgreSQLContainer<CustomPostgresContainer> {
    private static final String DB_IMAGE = "postgres:15-alpine";

    private static CustomPostgresContainer container;

    private CustomPostgresContainer() {
        super(DB_IMAGE);
        withDatabaseName("test_db");
        withUsername("test");
        withPassword("test");
    }

    public static synchronized CustomPostgresContainer getInstance() {
        if (container == null) {
            container = new CustomPostgresContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("TEST_DB_URL", getJdbcUrl());
        System.setProperty("TEST_DB_USERNAME", getUsername());
        System.setProperty("TEST_DB_PASSWORD", getPassword());
    }

    @Override
    public void stop() { }
}