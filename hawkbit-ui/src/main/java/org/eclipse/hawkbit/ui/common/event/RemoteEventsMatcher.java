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
import org.eclipse.hawkbit.ui.push.DistributionSetTagCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetTagDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetTagUpdatedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetTypeCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetTypeDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetTypeUpdatedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetUpdatedEventContainer;
import org.eclipse.hawkbit.ui.push.EventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleTypeCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleTypeDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleTypeUpdatedEventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleUpdatedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetFilterQueryCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetFilterQueryDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetFilterQueryUpdatedEventContainer;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;

public class RemoteEventsMatcher {
    private static final Map<Class<? extends EventContainer<?>>, EntityModifiedEventPayloadIdentifier> EVENT_MATCHERS = Maps
            .newHashMapWithExpectedSize(15);

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

        EVENT_MATCHERS.put(DistributionSetTypeCreatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                DsTypeModifiedEventPayload.class, EntityModifiedEventType.ENTITY_ADDED, "ds.type.created"));
        EVENT_MATCHERS.put(DistributionSetTypeUpdatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                DsTypeModifiedEventPayload.class, EntityModifiedEventType.ENTITY_UPDATED, "ds.type.updated"));
        EVENT_MATCHERS.put(DistributionSetTypeDeletedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                DsTypeModifiedEventPayload.class, EntityModifiedEventType.ENTITY_REMOVED, "ds.type.deleted"));

        EVENT_MATCHERS.put(DistributionSetTagCreatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                DsTagModifiedEventPayload.class, EntityModifiedEventType.ENTITY_ADDED, "ds.tag.created"));
        EVENT_MATCHERS.put(DistributionSetTagUpdatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                DsTagModifiedEventPayload.class, EntityModifiedEventType.ENTITY_UPDATED, "ds.tag.updated"));
        EVENT_MATCHERS.put(DistributionSetTagDeletedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                DsTagModifiedEventPayload.class, EntityModifiedEventType.ENTITY_REMOVED, "ds.tag.deleted"));

        EVENT_MATCHERS.put(SoftwareModuleTypeCreatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                SmTypeModifiedEventPayload.class, EntityModifiedEventType.ENTITY_ADDED, "sm.type.created"));
        EVENT_MATCHERS.put(SoftwareModuleTypeUpdatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                SmTypeModifiedEventPayload.class, EntityModifiedEventType.ENTITY_UPDATED, "sm.type.updated"));
        EVENT_MATCHERS.put(SoftwareModuleTypeDeletedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                SmTypeModifiedEventPayload.class, EntityModifiedEventType.ENTITY_REMOVED, "sm.type.deleted"));

        EVENT_MATCHERS.put(TargetFilterQueryCreatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                TargetFilterModifiedEventPayload.class, EntityModifiedEventType.ENTITY_ADDED, "tqf.created"));
        EVENT_MATCHERS.put(TargetFilterQueryUpdatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                TargetFilterModifiedEventPayload.class, EntityModifiedEventType.ENTITY_UPDATED, "tqf.updated"));
        EVENT_MATCHERS.put(TargetFilterQueryDeletedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                TargetFilterModifiedEventPayload.class, EntityModifiedEventType.ENTITY_REMOVED, "tqf.deleted"));
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
