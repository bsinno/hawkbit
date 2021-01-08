/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
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

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.util.StringUtils;

/**
 * A {@link TestExecutionListener} for creating and dropping MySql schemas if
 * tests are setup with MySql.
 */
public class MySqlTestDatabase extends DatabaseRule {

    private static final Logger LOG = LoggerFactory.getLogger(MySqlTestDatabase.class);
    private String uri;
    private String username;
    private String password;
    private final String schemaName;

    public MySqlTestDatabase(final String schemaName) {
        super(schemaName);
        this.schemaName = schemaName;
    }

    @Override
    public void before() throws Exception {
        if (!StringUtils.isEmpty(SchemaNameHolder.getInstance().getSchemaName())) {
            return;
        }
        LOG.info("Setting up mssql schema {}", schemaName);
        SchemaNameHolder.getInstance().setSchemaName(schemaName);
        this.username = System.getProperty("spring.datasource.username");
        this.password = System.getProperty("spring.datasource.password");
        this.uri = System.getProperty("spring.datasource.url");
        assert !StringUtils.isEmpty(username);
        assert !StringUtils.isEmpty(uri);
        createSchemaUri();
        createSchema();
    }

    @Override
    public void after() {
//        if (StringUtils.isEmpty(SchemaNameHolder.getInstance().getSchemaName())) {
//            return;
//        }
//        dropSchema();
    }

    private void createSchemaUri() {
        this.uri = this.uri.substring(0, uri.lastIndexOf('/') + 1);

        System.setProperty("spring.datasource.url", uri + schemaName);
    }

    private void createSchema() {
        try (Connection connection = DriverManager.getConnection(uri, username, password)) {
            try (PreparedStatement statement = connection.prepareStatement("CREATE SCHEMA " + schemaName + ";")) {
                LOG.info("Creating schema {} on uri {}", schemaName, uri);
                statement.execute();
                LOG.info("Created schema {} on uri {}", schemaName, uri);
            }
        } catch (final SQLException e) {
            LOG.error("Schema creation failed!", e);
        }

    }

    private void dropSchema() {
        try (Connection connection = DriverManager.getConnection(uri, username, password)) {
            try (PreparedStatement statement = connection.prepareStatement("DROP SCHEMA " + schemaName + ";")) {
                LOG.info("Dropping schema {} on uri {}", schemaName, uri);
                statement.execute();
                LOG.info("Dropped schema {} on uri {}", schemaName, uri);
            }
        } catch (final SQLException e) {
            LOG.error("Schema drop failed!", e);
        }
    }
}
