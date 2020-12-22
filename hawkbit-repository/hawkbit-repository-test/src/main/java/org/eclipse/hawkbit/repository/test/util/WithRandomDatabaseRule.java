/**
 * Copyright (c) 2021 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.test.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.rules.ExternalResource;

public class WithRandomDatabaseRule extends ExternalResource {

    private final DatabaseRule databaseTestListener;

    public WithRandomDatabaseRule() {
        this("");
    }

    public WithRandomDatabaseRule(final String prefix) {
        final String schemaName = String.format("HAWKBIT_TEST_%s_%s", prefix, RandomStringUtils.randomAlphanumeric(10));

        if (isRunningWithMySql()) {
            databaseTestListener = new MySqlTestDatabase(schemaName);
        } else if (isRunningWithMsSql()) {
            databaseTestListener = new MsSqlTestDatabase(schemaName);
        } else {
            // noop
            databaseTestListener = new DatabaseRule("") {};
        }
    }

    @Override
    protected void before() throws Throwable {
        databaseTestListener.before();
    }

    @Override
    protected void after() {
        databaseTestListener.after();
    }

    private static boolean isRunningWithMySql() {
        return "MYSQL".equals(System.getProperty("spring.jpa.database"));
    }

    private static boolean isRunningWithMsSql() {
        return "SQL_SERVER".equals(System.getProperty("spring.jpa.database"));
    }

}
