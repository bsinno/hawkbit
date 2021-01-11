/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.rabbitmq.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * Makes sure the configured virtual host is created before the test class execution begins,
 * and deleted after the test class execution is finished
 */
public class RandomVirtualHostTestExecutionListener implements TestExecutionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomVirtualHostTestExecutionListener.class);

    @Override
    public void beforeTestClass(final TestContext testContext) {
        final RabbitMqSetupService setupService = new RabbitMqSetupService(
                testContext.getApplicationContext().getBean(RabbitProperties.class));

        try {
            setupService.createVirtualHost();
        } catch (final Exception e) {
            LOGGER.warn("Virtual host was not created", e);
        }
    }

    @Override
    public void afterTestClass(final TestContext testContext) {
        final RabbitMqSetupService setupService = new RabbitMqSetupService(
                testContext.getApplicationContext().getBean(RabbitProperties.class));

        setupService.deleteVirtualHost();
    }
}
