/**
 * Copyright (c) 2021 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.test.util;

import org.junit.rules.ExternalResource;
import org.springframework.util.StringUtils;

/**
 * Represents a randomized database test execution rule
 */
public abstract class DatabaseRule extends ExternalResource {

    DatabaseRule(final String schemaName) {

    }

    @Override
    public void before() throws Throwable {
        if (!StringUtils.isEmpty(SchemaNameHolder.getInstance().getSchemaName())) {
            return;
        }
        super.before();
    }

    @Override
    public void after() {
        super.after();
    }
}
