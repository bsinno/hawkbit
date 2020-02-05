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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.hawkbit.repository.event.remote.RemoteIdEvent;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutGroup;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.RemoteEventsMatcher;
import org.eclipse.hawkbit.ui.common.event.RemoteEventsMatcher.EntityModifiedEventPayloadIdentifier;
import org.eclipse.hawkbit.ui.components.NotificationUnreadButton;
import org.eclipse.hawkbit.ui.push.DistributionSetCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetTagCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetTagDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetTagUpdatedEventContainer;
import org.eclipse.hawkbit.ui.push.EventContainer;
import org.eclipse.hawkbit.ui.push.RolloutGroupChangedEventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetTagCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetTagDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetTagUpdatedEventContainer;
import org.eclipse.hawkbit.ui.push.event.RolloutGroupChangedEvent;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.vaadin.ui.UI;

public class RemoteEventsListener {
    private final UIEventBus eventBus;
    private final NotificationUnreadButton notificationUnreadButton;

    private final Cache<EntityModifiedEventPayloadIdentifier, Collection<Long>> uiOriginatedEventsCache;
    private final List<Object> eventListeners;

    private final Set<Class<? extends EventContainer<?>>> supportedEvents = RemoteEventsMatcher.getEventMatchers()
            .keySet();

    private static final EntityModifiedEventPayloadIdentifier rolloutGroupModifiedEvent = RemoteEventsMatcher
            .getEventMatchers().get(RolloutGroupChangedEventContainer.class);
    private static final Set<EntityModifiedEventPayloadIdentifier> eventsToBeDeferred = new HashSet<>();
    static {
        eventsToBeDeferred.add(RemoteEventsMatcher.getEventMatchers().get(TargetCreatedEventContainer.class));
        eventsToBeDeferred.add(RemoteEventsMatcher.getEventMatchers().get(TargetDeletedEventContainer.class));

        eventsToBeDeferred.add(RemoteEventsMatcher.getEventMatchers().get(DistributionSetCreatedEventContainer.class));
        eventsToBeDeferred.add(RemoteEventsMatcher.getEventMatchers().get(DistributionSetDeletedEventContainer.class));

        eventsToBeDeferred.add(RemoteEventsMatcher.getEventMatchers().get(SoftwareModuleCreatedEventContainer.class));
        eventsToBeDeferred.add(RemoteEventsMatcher.getEventMatchers().get(SoftwareModuleDeletedEventContainer.class));

        eventsToBeDeferred.add(RemoteEventsMatcher.getEventMatchers().get(TargetTagCreatedEventContainer.class));
        eventsToBeDeferred.add(RemoteEventsMatcher.getEventMatchers().get(TargetTagUpdatedEventContainer.class));
        eventsToBeDeferred.add(RemoteEventsMatcher.getEventMatchers().get(TargetTagDeletedEventContainer.class));

        eventsToBeDeferred
                .add(RemoteEventsMatcher.getEventMatchers().get(DistributionSetTagCreatedEventContainer.class));
        eventsToBeDeferred
                .add(RemoteEventsMatcher.getEventMatchers().get(DistributionSetTagUpdatedEventContainer.class));
        eventsToBeDeferred
                .add(RemoteEventsMatcher.getEventMatchers().get(DistributionSetTagDeletedEventContainer.class));
    }

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
                    .merge(new EntityModifiedEventPayloadIdentifier(eventPayload.getParentType(),
                            eventPayload.getEntityType(), eventPayload.getEntityModifiedEventType()),
                            eventPayload.getEntityIds(), (oldEntityIds, newEntityIds) -> Stream
                                    .concat(oldEntityIds.stream(), newEntityIds.stream()).collect(Collectors.toList()));
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

            // TODO: should we make parent aware events (with parent id) in
            // general (e.g. add parentId to
            // EntityModifiedEventPayloadIdentifier)?
            // should be treated specially due to rollout id:
            if (rolloutGroupModifiedEvent.equals(matchingEventPayloadIdentifier)) {
                dispatchRolloutGroupModifiedEvent(eventContainer.getEvents());
                return;
            }

            final Collection<Long> remotelyModifiedEntityIds = filterRemoteOriginatedEvents(
                    matchingEventPayloadIdentifier, eventContainer.getEvents());

            if (!remotelyModifiedEntityIds.isEmpty()) {
                if (eventsToBeDeferred.contains(matchingEventPayloadIdentifier)) {
                    notificationUnreadButton.incrementUnreadNotification(matchingEventPayloadIdentifier,
                            remotelyModifiedEntityIds);
                } else {
                    dispatchEntityModifiedEvent(matchingEventPayloadIdentifier, remotelyModifiedEntityIds);
                }
            }
        }

        private boolean isEventContainerTypeSupported(final Class<?> eventContainerType) {
            return supportedEvents.contains(eventContainerType);
        }

        private void dispatchRolloutGroupModifiedEvent(final List<?> events) {
            final Map<Long, List<Long>> rolloutIdWithGroupIds = events.stream()
                    .map(event -> (RolloutGroupChangedEvent) event)
                    .collect(Collectors.groupingBy(RolloutGroupChangedEvent::getRolloutId,
                            Collectors.mapping(RolloutGroupChangedEvent::getEntityId, Collectors.toList())));

            rolloutIdWithGroupIds.entrySet().forEach(entry -> {
                final EntityModifiedEventPayload eventPayload = new EntityModifiedEventPayload(
                        EntityModifiedEventType.ENTITY_UPDATED, ProxyRollout.class, entry.getKey(),
                        ProxyRolloutGroup.class, entry.getValue());

                eventBus.publish(EventTopics.ENTITY_MODIFIED, UI.getCurrent(), eventPayload);
            });
        }

        private Collection<Long> filterRemoteOriginatedEvents(
                final EntityModifiedEventPayloadIdentifier eventPayloadIdentifier, final List<?> remoteEvents) {
            final Collection<Long> cachedEventEntityIds = uiOriginatedEventsCache.getIfPresent(eventPayloadIdentifier);
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

        // TODO: remove duplication with NotificationUnreadButton
        private void dispatchEntityModifiedEvent(final EntityModifiedEventPayloadIdentifier eventPayloadIdentifier,
                final Collection<Long> eventEntityIds) {
            // TODO: should we somehow add parentId here?
            final EntityModifiedEventPayload eventPayload = new EntityModifiedEventPayload(
                    eventPayloadIdentifier.getModifiedEventType(), eventPayloadIdentifier.getParentType(),
                    eventPayloadIdentifier.getEntityType(), eventEntityIds);

            eventBus.publish(EventTopics.ENTITY_MODIFIED, UI.getCurrent(), eventPayload);
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
