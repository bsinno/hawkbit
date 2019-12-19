/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.hawkbit.repository.event.remote.RemoteIdEvent;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.RemoteEventsMatcher;
import org.eclipse.hawkbit.ui.common.event.RemoteEventsMatcher.EntityModifiedEventPayloadIdentifier;
import org.eclipse.hawkbit.ui.components.NotificationUnreadButton;
import org.eclipse.hawkbit.ui.push.EventContainer;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class RemoteEventsListener {
    private final UIEventBus eventBus;
    private final NotificationUnreadButton notificationUnreadButton;

    private final Cache<EntityModifiedEventPayloadIdentifier, Collection<Long>> uiOriginatedEventsCache;
    private final List<Object> eventListeners;

    private final Set<Class<? extends EventContainer<?>>> supportedEvents = RemoteEventsMatcher.getEventMatchers()
            .keySet();

    RemoteEventsListener(final UIEventBus eventBus, final NotificationUnreadButton notificationUnreadButton) {
        this.eventBus = eventBus;
        this.notificationUnreadButton = notificationUnreadButton;

        this.uiOriginatedEventsCache = CacheBuilder.newBuilder().expireAfterAccess(10, SECONDS).build();

        this.eventListeners = new ArrayList<>();
        registerEventListeners();

    }

    private void registerEventListeners() {
        eventListeners.add(new EntityModifiedListener());
        eventListeners.add(new RemoteEventListener());
    }

    private class EntityModifiedListener {

        public EntityModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onEntityModifiedEvent(final EntityModifiedEventPayload eventPayload) {
            uiOriginatedEventsCache.asMap()
                    .merge(new EntityModifiedEventPayloadIdentifier(eventPayload.getClass(),
                            eventPayload.getEntityModifiedEventType()), eventPayload.getEntityIds(),
                            (oldEntityIds, newEntityIds) -> Stream.concat(oldEntityIds.stream(), newEntityIds.stream())
                                    .collect(Collectors.toList()));
        }
    }

    private class RemoteEventListener {

        public RemoteEventListener() {
            eventBus.subscribe(this, EventTopics.REMOTE_EVENT_RECEIVED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onRemoteEventReceived(final EventContainer<?> eventContainer) {
            if (!isEventContainerTypeSupported(eventContainer.getClass()) || eventContainer.getEvents().isEmpty()) {
                return;
            }

            final EntityModifiedEventPayloadIdentifier matchingEventPayloadIdentifier = RemoteEventsMatcher
                    .getEventMatchers().get(eventContainer.getClass());
            final Collection<Long> remotelyModifiedEntityIds = filterRemoteOriginatedEvents(
                    matchingEventPayloadIdentifier, eventContainer.getEvents());

            if (!remotelyModifiedEntityIds.isEmpty()) {
                notificationUnreadButton.incrementUnreadNotification(matchingEventPayloadIdentifier,
                        remotelyModifiedEntityIds);
            }
        }

        private boolean isEventContainerTypeSupported(final Class<?> eventContainerType) {
            return supportedEvents.contains(eventContainerType);
        }

        private Collection<Long> filterRemoteOriginatedEvents(
                final EntityModifiedEventPayloadIdentifier eventPayloadIdentifier, final List<?> remoteEvents) {
            final Collection<Long> cachedEventEntityIds = uiOriginatedEventsCache.getIfPresent(eventPayloadIdentifier);
            // TODO: events are not always RemoteIdEvent (e.g. rollout events)
            final List<Long> remoteEventEntityIds = remoteEvents.stream().filter(Objects::nonNull)
                    .map(event -> (RemoteIdEvent) event).map(RemoteIdEvent::getEntityId).collect(Collectors.toList());

            if (cachedEventEntityIds == null || cachedEventEntityIds.isEmpty()) {
                return remoteEventEntityIds;
            }

            final Collection<Long> commonEntityIds = getCommonEntityIds(cachedEventEntityIds, remoteEventEntityIds);
            if (!commonEntityIds.isEmpty()) {
                updateCache(eventPayloadIdentifier, cachedEventEntityIds, commonEntityIds);
                remoteEventEntityIds.removeAll(commonEntityIds);
            }

            return remoteEventEntityIds;
        }

        private Collection<Long> getCommonEntityIds(final Collection<Long> matchingCachedEventEntityIds,
                final List<Long> remoteEventEntityIds) {
            final List<Long> commonEntityIds = new ArrayList<>(matchingCachedEventEntityIds);
            commonEntityIds.retainAll(remoteEventEntityIds);

            return commonEntityIds;
        }

        private void updateCache(final EntityModifiedEventPayloadIdentifier matchingUiEventPayloadIdentifier,
                final Collection<Long> matchingCachedEventEntityIds, final Collection<Long> commonEntityIds) {
            final List<Long> updatedMatchingCachedEventEntityIds = new ArrayList<>(matchingCachedEventEntityIds);
            updatedMatchingCachedEventEntityIds.removeAll(commonEntityIds);

            if (updatedMatchingCachedEventEntityIds.isEmpty()) {
                uiOriginatedEventsCache.invalidate(matchingUiEventPayloadIdentifier);
            } else {
                uiOriginatedEventsCache.put(matchingUiEventPayloadIdentifier, updatedMatchingCachedEventEntityIds);
            }
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
