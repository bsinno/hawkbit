/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.test.util;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class SqlTestDatabase extends ExternalResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlTestDatabase.class);
    private static volatile String SCHEMA_NAME;

    private static final AbstractSqlTestDatabase[] DATABASES = new AbstractSqlTestDatabase[] { new MsSqlTestDatabase(),
            new MySqlTestDatabase(), new PostgreSqlTestDatabase() };

    @Override
    public void before() {
        if (!StringUtils.isEmpty(SCHEMA_NAME)) {
            LOGGER.trace("Skipping creation of schema {}, since it's already created...", SCHEMA_NAME);
            return;
        }

        if (StringUtils.isEmpty(System.getProperty("spring.jpa.database"))) {
            LOGGER.info("No database uri configured. Skipping...");
            return;
        }

        if ("H2".equals(System.getProperty("spring.jpa.database"))) {
            LOGGER.trace("H2 database detected, skipping sql schema creation");
            return;
        }

        createSchema();
    }

    private static void createSchema() {
        for (final AbstractSqlTestDatabase database : DATABASES) {
            if (database.isApplicable()) {
                SCHEMA_NAME = database.createSchema();
                return;
            }
        }
        throw new IllegalStateException("Expected to create schema on startup, found no supported database matching"
                + " uri " + System.getProperty("spring.datasource.url"));
    }
}
