/**
 * Copyright (c) 2020 Microsoft and others.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.AntPathMatcher;

/**
 * A {@link TestExecutionListener} for creating schemas if
 * tests are setup with a non-H2 database.
 */
public abstract class AbstractSqlTestDatabase extends AbstractTestExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSqlTestDatabase.class);
    protected static final AntPathMatcher MATCHER = new AntPathMatcher();

    protected static final String USERNAME;
    protected static final String PASSWORD;
    protected static final String URI;

    static {
        USERNAME = System.getProperty("spring.datasource.username");
        PASSWORD = System.getProperty("spring.datasource.password");
        URI = System.getProperty("spring.datasource.url");
    }

    protected abstract boolean isApplicable();

    protected abstract String createSchema();

    protected static void executeStatement(final String uri, final String schemaName, final String statement) {
        try (final Connection connection = DriverManager.getConnection(uri, AbstractSqlTestDatabase.USERNAME,
                AbstractSqlTestDatabase.PASSWORD)) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                LOGGER.info("Creating schema {} on uri {}", schemaName, uri);
                preparedStatement.execute();
                LOGGER.info("Created schema {} on uri {}", schemaName, uri);
            }
        } catch (final SQLException e) {
            LOGGER.error("Schema creation failed!", e);
        }
    }
}
