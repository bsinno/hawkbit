/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

import java.util.Arrays;
import java.util.List;

import org.eclipse.hawkbit.amqp.AmqpMessageHandlerService;
import org.eclipse.hawkbit.amqp.AmqpProperties;
import org.eclipse.hawkbit.repository.ControllerManagement;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetCreatedEvent;
import org.eclipse.hawkbit.repository.exception.EntityAlreadyExistsException;
import org.eclipse.hawkbit.repository.test.matcher.Expect;
import org.eclipse.hawkbit.repository.test.matcher.ExpectEvents;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature("Component Tests - Device Management Federation API")
@Story("Amqp Message Handler Service")
public class AmqpProblematicMessageHandlerServiceIntegrationTest extends AbstractAmqpServiceIntegrationTest {

    @Autowired
    private AmqpProperties amqpProperties;

    @Autowired
    private AmqpMessageHandlerService amqpMessageHandlerService;

    @Mock
    private ControllerManagement mockedControllerManagement;

    @Before
    public void injectMockedControllerManagement() {
        amqpMessageHandlerService.setControllerManagement(mockedControllerManagement);
    }

    @After
    public void resetControllerManagement() {
        amqpMessageHandlerService.setControllerManagement(controllerManagement);
    }

    @Test
    @Description("Messages that result into certain exceptions being raised should not be requeued. This message should forwarded to the deadletter queue")
    @ExpectEvents({@Expect(type = TargetCreatedEvent.class, count = 0)})
    public void ignoredExceptionTypesShouldNotBeRequeued() {

        final List<Class<? extends RuntimeException>> exceptionsThatShouldNotBeRequeued = Arrays
                .asList(IllegalArgumentException.class, EntityAlreadyExistsException.class);
        final String controllerId = "dummy_target";

        for (Class<? extends RuntimeException> exceptionClass : exceptionsThatShouldNotBeRequeued) {
            doThrow(exceptionClass).when(mockedControllerManagement)
                    .findOrRegisterTargetIfItDoesNotExist(eq(controllerId), any());

            createAndSendThingCreated(controllerId, tenantAware.getCurrentTenant());
            verifyOneDeadLetterMessage();
            assertThat(targetManagement.getByControllerID(controllerId)).isEmpty();
        }

    }

    private int getAuthenticationMessageCount() {
        return Integer.parseInt(getRabbitAdmin().getQueueProperties(amqpProperties.getReceiverQueue())
                .get(RabbitAdmin.QUEUE_MESSAGE_COUNT).toString());
    }

    private void assertEmptyReceiverQueueCount() {
        assertThat(getAuthenticationMessageCount()).isEqualTo(0);
    }

    private void verifyOneDeadLetterMessage() {
        assertEmptyReceiverQueueCount();
        createConditionFactory().untilAsserted(
                () -> assertThat(deadletterListener.getMessageCount()).isEqualTo(1));
        deadletterListener.reset();
    }

}
