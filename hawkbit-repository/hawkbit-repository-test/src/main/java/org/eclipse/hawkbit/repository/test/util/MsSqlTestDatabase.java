/**
 * Copyright (c) 2018 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.test.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.util.StringUtils;

/**
 * A {@link TestExecutionListener} for creating and dropping MS SQL Server
 * schemas if tests are setup with MS SQL Server.
 */
public class MsSqlTestDatabase extends AbstractSqlTestDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MsSqlTestDatabase.class);
    private static final String MSSQL_URI_PATTERN = "jdbc:sqlserver://{host}:{port};database={db}*";

    @Override
    protected boolean isApplicable() {
        return "SQL_SERVER".equals(System.getProperty("spring.jpa.database")) //
                && MATCHER.match(MSSQL_URI_PATTERN, URI) //
                && !StringUtils.isEmpty(getSchemaName());
    }

    @Override
    public String createSchema() {
        final String schemaName = URI.split("database=")[1].split(";")[0];
        LOGGER.info("Creating mssql schema {} if not existing", schemaName);

        executeStatement(URI.split(";database=")[0], schemaName, "CREATE DATABASE IF NOT EXISTS " + schemaName + ";");
        return schemaName;
    }

    private static String getSchemaName() {
        return MATCHER.extractUriTemplateVariables(MSSQL_URI_PATTERN, URI).get("db");
    }
}
