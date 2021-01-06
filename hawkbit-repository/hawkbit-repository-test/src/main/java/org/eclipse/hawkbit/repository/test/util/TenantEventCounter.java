/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.test.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.hawkbit.repository.event.TenantAwareEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public class TenantEventCounter implements ApplicationListener<ApplicationEvent> {

    // Using static field to mitigate the spring context refresh, any changes written in this collection
    // will be persisted even between multiple beans construction/destruction
    private static final Map<String, Set<TenantAwareEvent>> TENANT_EVENTS_COUNT = new ConcurrentHashMap<>();

    public Map<Class<? extends TenantAwareEvent>, Integer> getEventsCount(final String tenant) {
        final Set<? extends TenantAwareEvent> events = TENANT_EVENTS_COUNT.getOrDefault(tenant, Collections.emptySet());
        final Map<Class<? extends TenantAwareEvent>, Integer> eventsCount = new HashMap<>();
        events.forEach(e -> eventsCount.merge(e.getClass(), 1, Integer::sum));
        return eventsCount;
    }

    @Override
    public void onApplicationEvent(final ApplicationEvent event) {
        if (event instanceof TenantAwareEvent) {
            assertThat(((TenantAwareEvent) event).getTenant()).isNotBlank();

            final Set<TenantAwareEvent> eventsCount = TENANT_EVENTS_COUNT.getOrDefault(
                    ((TenantAwareEvent) event).getTenant(), new HashSet<>());
            eventsCount.add((TenantAwareEvent) event);
            TENANT_EVENTS_COUNT.put(((TenantAwareEvent) event).getTenant(), eventsCount);
        }
    }
//    private static final Map<String, Map<Class<? extends TenantAwareEvent>, Integer>> TENANT_EVENTS_COUNT = new ConcurrentHashMap<>();
//
//    public void onApplicationEvent(final TenantAwareEvent event) {
//        assertThat(event.getTenant()).isNotBlank();
//
//        final Map<Class<? extends TenantAwareEvent>, Integer> eventsCount = TENANT_EVENTS_COUNT.getOrDefault(
//                event.getTenant(), new ConcurrentHashMap<>());
//        eventsCount.merge(event.getClass(), 1, Integer::sum);
//        TENANT_EVENTS_COUNT.put(event.getTenant(), eventsCount);
//    }
//
//    public Map<Class<? extends TenantAwareEvent>, Integer> getEventsCount(final String tenant) {
//        return TENANT_EVENTS_COUNT.getOrDefault(tenant, Collections.emptyMap());
//    }
//
//    @Override
//    public void onApplicationEvent(final ApplicationEvent event) {
//        if (event instanceof  TenantAwareEvent) {
//            onApplicationEvent((TenantAwareEvent) event);
//        }
//    }
}
