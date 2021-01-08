/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.hawkbit.repository.test.matcher;

import static org.hamcrest.Matchers.equalTo;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.eclipse.hawkbit.repository.event.TenantAwareEvent;
import org.eclipse.hawkbit.repository.test.util.TenantEventCounter;
import org.eclipse.hawkbit.tenancy.TenantAware;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Test rule to setup and verify the event count for a method.
 */
public class EventVerifier extends AbstractTestExecutionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventVerifier.class);
//    private final TenantEventCounter eventCounter = new TenantEventCounter();
//
//    @Override
//    public void beforeTestMethod(final TestContext testContext) throws Exception {
//        ((ConfigurableApplicationContext) testContext.getApplicationContext()).addApplicationListener(eventCounter);
//    }
//
//    @Override
//    public void afterTestMethod(final TestContext testContext) throws Exception {
//        testContext.getApplicationContext().getBean(ApplicationEventMulticaster.class).removeApplicationListener(eventCounter);
//    }

    @Override
    public void afterTestExecution(final TestContext testContext) {
        final Map<Class<?>, Integer> expectedEvents = getExpectations(testContext);
        if (expectedEvents.isEmpty()) {
            return;
        }
        final TenantEventCounter eventCounter = testContext.getApplicationContext().getBean(TenantEventCounter.class);
        final String tenant = testContext.getApplicationContext().getBean(TenantAware.class).getCurrentTenant();
        logEvents(eventCounter.getEventsCount(tenant), expectedEvents);
        verifyRightCountOfEvents(tenant, eventCounter, expectedEvents);
        verifyAllEventsCounted(eventCounter.getEventsCount(tenant), expectedEvents);
    }

    private static Map<Class<?>, Integer> getExpectations(final TestContext testContext) {
        final ExpectEvents methodAnnotation = testContext.getTestMethod().getAnnotation(ExpectEvents.class);

        if (methodAnnotation == null) {
            return Collections.emptyMap();
        }
        return methodAnnotation.inheritExpects() ?
                asMap(methodAnnotation.value(), getBeforeMethodExpects(testContext)) :
                asMap(methodAnnotation.value());
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

    private static void verifyRightCountOfEvents(final String tenant, final TenantEventCounter eventCounter,
            final Map<Class<?>, Integer> expectedEvents) {

        final StringBuilder failMessage = new StringBuilder();

        for (Map.Entry<Class<?>, Integer> expected : expectedEvents.entrySet()) {
            try {
                Awaitility.await().atMost(3, TimeUnit.SECONDS).pollInterval(50, TimeUnit.MILLISECONDS)
                        .until(() -> eventCounter.getEventsCount(tenant).getOrDefault(expected.getKey(), 0),
                                equalTo(expected.getValue()));
            } catch (final ConditionTimeoutException e) {
                LOGGER.trace("", e);
                failMessage.append(expected.getKey().getSimpleName())
                        .append("\t\t[expected: ")
                        .append(expected.getValue())
                        .append(", but was: ")
                        .append(eventCounter.getEventsCount(tenant).getOrDefault(expected.getKey(), 0))
                        .append("]\n");
            }
        }

        if (!failMessage.toString().isEmpty()) {
            Assert.fail("Did not receive the expected amount of events.\n" + failMessage.toString());
        }
    }

    private static void logEvents(final Map<Class<? extends TenantAwareEvent>, Integer> receivedEvents,
            final Map<Class<?>, Integer> expectedEvents) {
        LOGGER.trace("=============== Actual (Observed) Events ===============");
        receivedEvents.forEach((aClass, integer) -> LOGGER.trace("{} -> {}", aClass.getSimpleName(), integer));
        LOGGER.trace("===============================================\n\n");

        LOGGER.trace("=============== Expected Events ===============");
        expectedEvents.forEach((aClass, integer) -> LOGGER.trace("{} -> {}", aClass.getSimpleName(), integer));
        LOGGER.trace("===============================================\n\n");
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
