/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.test.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.hawkbit.repository.event.TenantAwareEvent;
import org.springframework.context.event.EventListener;

public class TenantEventCounter {

    private static final Map<String, Set<TenantAwareEvent>> TENANT_EVENTS_COUNT = new ConcurrentHashMap<>();
    private static final TenantEventCounter INSTANCE = new TenantEventCounter();

    public static TenantEventCounter instance() {
        return INSTANCE;
    }

    @EventListener(TenantAwareEvent.class)
    public void onApplicationEvent(final TenantAwareEvent event) {
        assertThat(event.getTenant()).isNotBlank();
        final Set<TenantAwareEvent> eventsCount = TENANT_EVENTS_COUNT.getOrDefault(event.getTenant(), new HashSet<>());
        eventsCount.add(event);
        TENANT_EVENTS_COUNT.put(event.getTenant(), eventsCount);
    }

    public Map<Class<? extends TenantAwareEvent>, Integer> getEventsCount(final String tenant) {
        final Map<Class<? extends TenantAwareEvent>, Integer> events = new ConcurrentHashMap<>();
        for (TenantAwareEvent event : TENANT_EVENTS_COUNT.getOrDefault(tenant, Collections.emptySet())) {
            events.merge(event.getClass(), 1, Integer::sum);
        }
        return events;
    }
}
