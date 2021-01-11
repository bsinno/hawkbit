/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.test.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

public class WithSqlDatabaseRule extends ExternalResource {

    private static final Logger LOG = LoggerFactory.getLogger(WithSqlDatabaseRule.class);
    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    private static final String MYSQL_URI_PATTERN = "jdbc:mysql://{host}:{port}/{db}*";
    private static volatile String databaseName;

    @Override
    public void before() {
        if (!StringUtils.isEmpty(databaseName) || !isRunningWithMsSql() && !isRunningWithMySql()) {
            return;
        }

        final String username = System.getProperty("spring.datasource.username");
        final String password = System.getProperty("spring.datasource.password");
        final String uri = System.getProperty("spring.datasource.url");

        if (isRunningWithMySql()) {
            createMySqlSchemaIfNotExists(username, password, uri);
        }

        if (isRunningWithMsSql()) {
            createMsSqlSchemaIfNotExists(username, password, uri);
        }
    }

    private static boolean isRunningWithMySql() {
        return "MYSQL".equals(System.getProperty("spring.jpa.database"));
    }

    private static void createMySqlSchemaIfNotExists(final String username, final String password, final String uri) {
        if (!MATCHER.match(MYSQL_URI_PATTERN, uri) || StringUtils.isEmpty(
                MATCHER.extractUriTemplateVariables(MYSQL_URI_PATTERN, uri).get("db"))) {
            throw new AssertionError("Expected database uri to be in the format " + MYSQL_URI_PATTERN);
        }
        final String schemaName = MATCHER.extractUriTemplateVariables(MYSQL_URI_PATTERN, uri).get("db");
        LOG.info("Creating mysql schema {} if not existing", schemaName);

        executeStatement(username, password, uri.split("/" + schemaName)[0], schemaName,
                "CREATE SCHEMA IF NOT EXISTS " + schemaName + ";");
        databaseName = schemaName;
    }

    private static boolean isRunningWithMsSql() {
        return "SQL_SERVER".equals(System.getProperty("spring.jpa.database"));
    }

    public static void createMsSqlSchemaIfNotExists(final String username, final String password, final String uri) {
        if (!uri.contains("database=")) {
            throw new AssertionError("Expected uri to include database name 'database=test-db'");
        }
        final String schemaName = uri.split("database=")[1].split(";")[0];
        LOG.info("Creating mssql schema {} if not existing", schemaName);

        executeStatement(username, password, uri, schemaName, "CREATE DATABASE IF NOT EXISTS " + schemaName + ";");
        databaseName = schemaName;
    }

    private static void executeStatement(final String username, final String password, final String uri,
            final String schemaName, final String statement) {
        try (Connection connection = DriverManager.getConnection(uri, username, password)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                LOG.info("Creating schema {} on uri {}", schemaName, uri);
                preparedStatement.execute();
                LOG.info("Created schema {} on uri {}", schemaName, uri);
            }
        } catch (final SQLException e) {
            LOG.error("Schema creation failed!", e);
        }
    }
}
