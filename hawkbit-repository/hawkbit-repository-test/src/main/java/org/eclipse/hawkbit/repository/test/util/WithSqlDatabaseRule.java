/**
 * Copyright (c) 2021 Bosch Software Innovations GmbH and others.
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

    @Override
    public void before() {
        final String username = System.getProperty("spring.datasource.username");
        final String password = System.getProperty("spring.datasource.password");
        final String uri = System.getProperty("spring.datasource.url");

        assert !StringUtils.isEmpty(username);
        assert !StringUtils.isEmpty(uri);

        if (isRunningWithMySql(uri)) {
            createMySqlSchemaIfNotExists(username, password, uri);
        }

        if (isRunningWithMsSql(uri)) {
            createMsSqlSchemaIfNotExists(username, password, uri);
        }
    }

    private static boolean isRunningWithMySql(final String uri) {
        return "MYSQL".equals(System.getProperty("spring.jpa.database")) && MATCHER.match(MYSQL_URI_PATTERN, uri)
                && !StringUtils.isEmpty(MATCHER.extractUriTemplateVariables(MYSQL_URI_PATTERN, uri).get("db"));
    }

    private static void createMySqlSchemaIfNotExists(final String username, final String password, final String uri) {
        final String schemaName = MATCHER.extractUriTemplateVariables(MYSQL_URI_PATTERN, uri).get("db");
        LOG.info("Creating mysql schema {} if not existing", schemaName);

        executeStatement(username, password, uri.split("/" + schemaName)[0], schemaName,
                "CREATE SCHEMA IF NOT EXISTS " + schemaName + ";");
    }

    private static boolean isRunningWithMsSql(final String uri) {
        return "SQL_SERVER".equals(System.getProperty("spring.jpa.database")) && uri.contains("database=");
    }

    public static void createMsSqlSchemaIfNotExists(final String username, final String password, final String uri) {
        final String schemaName = uri.split("database=")[1].split(";")[0];
        LOG.info("Creating mssql schema {} if not existing", schemaName);

        executeStatement(username, password, uri, schemaName, "CREATE DATABASE IF NOT EXISTS " + schemaName + ";");
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
