/**
 * Copyright (c) 2021 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.test.util;

/**
 * Holds the schema name for the current thread/test execution to avoid creating multiple schemas
 */
public class SchemaNameHolder {
    private static SchemaNameHolder INSTANCE = new SchemaNameHolder();

    private SchemaNameHolder() {
    }

    private String schemaName;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(final String dbName) {
        this.schemaName = dbName;
    }

    public static SchemaNameHolder getInstance() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return String.format("[schemaName: %s]", schemaName);
    }
}
