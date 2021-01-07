/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa;

import org.eclipse.hawkbit.repository.ControllerManagement;
import org.eclipse.hawkbit.repository.RepositoryProperties;
import org.eclipse.hawkbit.repository.event.remote.TargetPollEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetCreatedEvent;
import org.eclipse.hawkbit.repository.test.matcher.Expect;
import org.eclipse.hawkbit.repository.test.matcher.ExpectEvents;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestPropertySource;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature("Component Tests - Repository")
@Story("Controller Management")
// explicitly redeclaring the context hirarchy, in order to "enforce"
// a fresh context creation with the empty Config class
@ContextHierarchy({ //
    @ContextConfiguration(name = "base"), //
    @ContextConfiguration(name = "jpa", classes = { ControllerManagementDisabledPollEventTest.Config.class}) //
})
@TestPropertySource(properties = "hawkbit.server.repository.publishTargetPollEvent=false")
public class ControllerManagementDisabledPollEventTest extends AbstractJpaIntegrationTest {

    /**
     * Dummy config class to 'force' spring to create a new context for this test,
     * and not reuse the cached 'jpa' context
     */
    @Configuration
    public static class Config {
    }

    @Autowired
    private ControllerManagement controllerManagement;

    @Autowired
    private RepositoryProperties repositoryProperties;

    @Test
    @Description("Verify that controller registration does not result in a TargetPollEvent if feature is disabled")
    @ExpectEvents({ @Expect(type = TargetCreatedEvent.class, count = 1),
                    @Expect(type = TargetPollEvent.class, count = 0) })
    public void targetPollEventNotSendIfDisabled() {
        try {
            repositoryProperties.setPublishTargetPollEvent(false);
            controllerManagement.findOrRegisterTargetIfItDoesNotExist("AA", LOCALHOST);
        } finally {
            repositoryProperties.setPublishTargetPollEvent(true);
        }
    }
}
