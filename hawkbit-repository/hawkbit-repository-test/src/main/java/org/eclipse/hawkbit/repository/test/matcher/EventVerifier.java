/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.hawkbit.repository.test.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.in;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.hawkbit.repository.event.TenantAwareEvent;
import org.eclipse.hawkbit.repository.event.remote.RemoteIdEvent;
import org.eclipse.hawkbit.repository.event.remote.RemoteTenantAwareEvent;
import org.eclipse.hawkbit.repository.event.remote.TargetAssignDistributionSetEvent;
import org.eclipse.hawkbit.repository.test.TestConfiguration;
import org.eclipse.hawkbit.repository.test.util.AbstractIntegrationTest;
import org.eclipse.hawkbit.repository.test.util.TenantEventCounter;
import org.eclipse.hawkbit.tenancy.TenantAware;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;

/**
 * Test rule to setup and verify the event count for a method.
 */
public class EventVerifier extends AbstractTestExecutionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventVerifier.class);

    @Override
    public void beforeTestExecution(final TestContext testContext) throws Exception {
        final Map<Class<?>, Integer> expectedEvents = getExpectationsFrom(testContext);
        if (expectedEvents.isEmpty()) {
            return;
        }
        final String tenant = testContext.getApplicationContext().getBean(TenantAware.class).getCurrentTenant();
        LOGGER.info("Counting events for tenant {}", tenant);
    }

    @Override
    public void afterTestExecution(final TestContext testContext) throws Exception {
        final Map<Class<?>, Integer> expectedEvents = getExpectationsFrom(testContext);
        if (expectedEvents.isEmpty()) {
            return;
        }

        final String tenant = testContext.getApplicationContext().getBean(TenantAware.class).getCurrentTenant();
        LOGGER.info("Verifying events for tenant {}", tenant);

        final Map<Class<? extends TenantAwareEvent>, Integer> receivedEvents = testContext.getApplicationContext()
                .getBean(TenantEventCounter.class).getEventsCount(tenant);

        verifyRightCountOfEvents(receivedEvents, expectedEvents);
        verifyAllEventsCounted(receivedEvents, expectedEvents);
    }

    private static Map<Class<?>, Integer> getExpectationsFrom(final TestContext testContext) {
        final ExpectEvents methodAnnotation = testContext.getTestMethod().getAnnotation(ExpectEvents.class);

        return methodAnnotation == null ?
                Collections.emptyMap() :
                asMap(methodAnnotation.value(), getBeforeMethodExpects(testContext));
    }

    private static Map<Class<?>, Integer> asMap(final Expect[]... expectsArray) {
        final Map<Class<?>, Integer> expectsMap = new HashMap<>();
        for (final Expect[] expects : expectsArray) {
            for (Expect expect : expects) {
                expectsMap.merge(expect.type(), expect.count(), Integer::sum);
            }
        }
        return expectsMap;
    }

    private static Expect[] getBeforeMethodExpects(final TestContext testContext) {
        Class<?> clazz = testContext.getTestClass();
        do {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Before.class) && method.isAnnotationPresent(ExpectEvents.class)) {
                    return method.getAnnotation(ExpectEvents.class).value();
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);

        return new Expect[0];
    }

    private static void verifyRightCountOfEvents(final Map<Class<? extends TenantAwareEvent>, Integer> receivedEvents,
            final Map<Class<?>, Integer> expectedEvents) {

        System.out.println("\n\n*************** Actual **************");
        receivedEvents.forEach((aClass, integer) -> System.out.println(aClass.getSimpleName() + " -> " + integer));
        System.out.println("*****************************\n\n");

        System.out.println("\n\n=============== Expects ===============");
        expectedEvents.forEach((aClass, integer) -> System.out.println(aClass.getSimpleName() + " -> " + integer));
        System.out.println("==============================\n\n");

        Awaitility.await().atMost(5, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            for (Map.Entry<Class<?>, Integer> expected : expectedEvents.entrySet()) {
                final Class<?> eventType = expected.getKey();
                final Integer expectedCount = expected.getValue();
                final Integer actualCount = receivedEvents.getOrDefault(eventType, 0);

                assertThat(actualCount).as("Did not receive the expected amount of %s events. Expected: %d but was: %d",
                        eventType.getSimpleName(), expectedCount, actualCount).isEqualTo(expectedCount);
            }
        });
    }

    private static void verifyAllEventsCounted(final Map<Class<? extends TenantAwareEvent>, Integer> receivedEvents,
            final Map<Class<?>, Integer> expectedEvents) {
        final StringBuilder failMessage = new StringBuilder();

        for (Map.Entry<Class<? extends TenantAwareEvent>, Integer> received : receivedEvents.entrySet()) {
            if (!expectedEvents.containsKey(received.getKey())) {
                failMessage.append(received.getKey()).append(" with count: ").append(received.getValue());
            }
        }

        if (!failMessage.toString().isEmpty()) {
            Assert.fail("Missing event verification for [" + failMessage.append("]").toString());
        }

    }

}
