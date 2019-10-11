/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

import java.util.Map;
import java.util.Objects;

import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.push.DistributionSetCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetUpdatedEventContainer;
import org.eclipse.hawkbit.ui.push.EventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleUpdatedEventContainer;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;

public class RemoteEventsMatcher {
    private static final Map<Class<? extends EventContainer<?>>, EntityModifiedEventPayloadIdentifier> EVENT_MATCHERS = Maps
            .newHashMapWithExpectedSize(6);

    private RemoteEventsMatcher() {
    }

    static {
        EVENT_MATCHERS.put(DistributionSetCreatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                DsModifiedEventPayload.class, EntityModifiedEventType.ENTITY_ADDED, "ds.created"));
        EVENT_MATCHERS.put(DistributionSetUpdatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                DsModifiedEventPayload.class, EntityModifiedEventType.ENTITY_UPDATED, "ds.updated"));
        EVENT_MATCHERS.put(DistributionSetDeletedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                DsModifiedEventPayload.class, EntityModifiedEventType.ENTITY_REMOVED, "ds.deleted"));

        EVENT_MATCHERS.put(SoftwareModuleCreatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                SmModifiedEventPayload.class, EntityModifiedEventType.ENTITY_ADDED, "sm.created"));
        EVENT_MATCHERS.put(SoftwareModuleUpdatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                SmModifiedEventPayload.class, EntityModifiedEventType.ENTITY_UPDATED, "sm.updated"));
        EVENT_MATCHERS.put(SoftwareModuleDeletedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                SmModifiedEventPayload.class, EntityModifiedEventType.ENTITY_REMOVED, "sm.deleted"));
    }

    public static Map<Class<? extends EventContainer<?>>, EntityModifiedEventPayloadIdentifier> getEventMatchers() {
        return EVENT_MATCHERS;
    }

    public static class EntityModifiedEventPayloadIdentifier {
        private final Class<? extends EntityModifiedEventPayload> eventPayloadType;
        private final EntityModifiedEventType modifiedEventType;
        private final String eventTypeMessageKey;

        public EntityModifiedEventPayloadIdentifier(final Class<? extends EntityModifiedEventPayload> eventPayloadType,
                final EntityModifiedEventType modifiedEventType) {
            this(eventPayloadType, modifiedEventType, null);
        }

        public EntityModifiedEventPayloadIdentifier(final Class<? extends EntityModifiedEventPayload> eventPayloadType,
                final EntityModifiedEventType modifiedEventType, final String eventTypeMessageKey) {
            this.eventPayloadType = eventPayloadType;
            this.modifiedEventType = modifiedEventType;
            this.eventTypeMessageKey = eventTypeMessageKey;
        }

        public Class<? extends EntityModifiedEventPayload> getEventPayloadType() {
            return eventPayloadType;
        }

        public EntityModifiedEventType getModifiedEventType() {
            return modifiedEventType;
        }

        public String getEventTypeMessageKey() {
            return eventTypeMessageKey;
        }

        @Override
        public int hashCode() {
            // eventTypeMessageKey is omitted intentionally, because it is not
            // relevant for event identification
            return Objects.hash(eventPayloadType.getName(), modifiedEventType);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EntityModifiedEventPayloadIdentifier other = (EntityModifiedEventPayloadIdentifier) obj;

            // eventTypeMessageKey is omitted intentionally, because it is not
            // relevant for event identification
            return Objects.equals(this.eventPayloadType, other.eventPayloadType)
                    && Objects.equals(this.modifiedEventType, other.modifiedEventType);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("EventPayloadType", eventPayloadType.getName())
                    .add("ModifiedEventType", modifiedEventType.name()).toString();
        }
    }
}
