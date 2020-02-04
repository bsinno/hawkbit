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

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutGroup;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
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
import org.eclipse.hawkbit.ui.push.RolloutChangedEventContainer;
import org.eclipse.hawkbit.ui.push.RolloutCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.RolloutDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.RolloutGroupChangedEventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleTypeCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleTypeDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleTypeUpdatedEventContainer;
import org.eclipse.hawkbit.ui.push.SoftwareModuleUpdatedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetFilterQueryCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetFilterQueryDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetFilterQueryUpdatedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetTagCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetTagDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetTagUpdatedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetUpdatedEventContainer;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;

public class RemoteEventsMatcher {
    private static final Map<Class<? extends EventContainer<?>>, EntityModifiedEventPayloadIdentifier> EVENT_MATCHERS = Maps
            .newHashMapWithExpectedSize(28);

    private RemoteEventsMatcher() {
    }

    static {
        EVENT_MATCHERS.put(TargetCreatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyTarget.class, EntityModifiedEventType.ENTITY_ADDED, "target.created"));
        EVENT_MATCHERS.put(TargetUpdatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyTarget.class, EntityModifiedEventType.ENTITY_UPDATED, "target.updated"));
        EVENT_MATCHERS.put(TargetDeletedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyTarget.class, EntityModifiedEventType.ENTITY_REMOVED, "target.deleted"));

        EVENT_MATCHERS.put(TargetTagCreatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyTarget.class, ProxyTag.class, EntityModifiedEventType.ENTITY_ADDED, "target.tag.created"));
        EVENT_MATCHERS.put(TargetTagUpdatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyTarget.class, ProxyTag.class, EntityModifiedEventType.ENTITY_UPDATED, "target.tag.updated"));
        EVENT_MATCHERS.put(TargetTagDeletedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyTarget.class, ProxyTag.class, EntityModifiedEventType.ENTITY_REMOVED, "target.tag.deleted"));

        EVENT_MATCHERS.put(DistributionSetCreatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyDistributionSet.class, EntityModifiedEventType.ENTITY_ADDED, "ds.created"));
        EVENT_MATCHERS.put(DistributionSetUpdatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyDistributionSet.class, EntityModifiedEventType.ENTITY_UPDATED, "ds.updated"));
        EVENT_MATCHERS.put(DistributionSetDeletedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyDistributionSet.class, EntityModifiedEventType.ENTITY_REMOVED, "ds.deleted"));

        EVENT_MATCHERS.put(SoftwareModuleCreatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxySoftwareModule.class, EntityModifiedEventType.ENTITY_ADDED, "sm.created"));
        EVENT_MATCHERS.put(SoftwareModuleUpdatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxySoftwareModule.class, EntityModifiedEventType.ENTITY_UPDATED, "sm.updated"));
        EVENT_MATCHERS.put(SoftwareModuleDeletedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxySoftwareModule.class, EntityModifiedEventType.ENTITY_REMOVED, "sm.deleted"));

        EVENT_MATCHERS.put(DistributionSetTypeCreatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyDistributionSet.class, ProxyType.class, EntityModifiedEventType.ENTITY_ADDED, "ds.type.created"));
        EVENT_MATCHERS.put(DistributionSetTypeUpdatedEventContainer.class,
                new EntityModifiedEventPayloadIdentifier(ProxyDistributionSet.class, ProxyType.class,
                        EntityModifiedEventType.ENTITY_UPDATED, "ds.type.updated"));
        EVENT_MATCHERS.put(DistributionSetTypeDeletedEventContainer.class,
                new EntityModifiedEventPayloadIdentifier(ProxyDistributionSet.class, ProxyType.class,
                        EntityModifiedEventType.ENTITY_REMOVED, "ds.type.deleted"));

        EVENT_MATCHERS.put(DistributionSetTagCreatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyDistributionSet.class, ProxyTag.class, EntityModifiedEventType.ENTITY_ADDED, "ds.tag.created"));
        EVENT_MATCHERS.put(DistributionSetTagUpdatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyDistributionSet.class, ProxyTag.class, EntityModifiedEventType.ENTITY_UPDATED, "ds.tag.updated"));
        EVENT_MATCHERS.put(DistributionSetTagDeletedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyDistributionSet.class, ProxyTag.class, EntityModifiedEventType.ENTITY_REMOVED, "ds.tag.deleted"));

        EVENT_MATCHERS.put(SoftwareModuleTypeCreatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxySoftwareModule.class, ProxyType.class, EntityModifiedEventType.ENTITY_ADDED, "sm.type.created"));
        EVENT_MATCHERS.put(SoftwareModuleTypeUpdatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxySoftwareModule.class, ProxyType.class, EntityModifiedEventType.ENTITY_UPDATED, "sm.type.updated"));
        EVENT_MATCHERS.put(SoftwareModuleTypeDeletedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxySoftwareModule.class, ProxyType.class, EntityModifiedEventType.ENTITY_REMOVED, "sm.type.deleted"));

        EVENT_MATCHERS.put(TargetFilterQueryCreatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyTargetFilterQuery.class, EntityModifiedEventType.ENTITY_ADDED, "tqf.created"));
        EVENT_MATCHERS.put(TargetFilterQueryUpdatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyTargetFilterQuery.class, EntityModifiedEventType.ENTITY_UPDATED, "tqf.updated"));
        EVENT_MATCHERS.put(TargetFilterQueryDeletedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyTargetFilterQuery.class, EntityModifiedEventType.ENTITY_REMOVED, "tqf.deleted"));

        EVENT_MATCHERS.put(RolloutCreatedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyRollout.class, EntityModifiedEventType.ENTITY_ADDED, "rollout.created"));
        EVENT_MATCHERS.put(RolloutChangedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyRollout.class, EntityModifiedEventType.ENTITY_UPDATED, "rollout.updated"));
        EVENT_MATCHERS.put(RolloutDeletedEventContainer.class, new EntityModifiedEventPayloadIdentifier(
                ProxyRollout.class, EntityModifiedEventType.ENTITY_REMOVED, "rollout.deleted"));

        EVENT_MATCHERS.put(RolloutGroupChangedEventContainer.class,
                new EntityModifiedEventPayloadIdentifier(ProxyRollout.class, ProxyRolloutGroup.class,
                        EntityModifiedEventType.ENTITY_UPDATED, "rollout.group.updated"));
    }

    public static Map<Class<? extends EventContainer<?>>, EntityModifiedEventPayloadIdentifier> getEventMatchers() {
        return EVENT_MATCHERS;
    }

    public static class EntityModifiedEventPayloadIdentifier {
        private final Class<? extends ProxyIdentifiableEntity> parentType;
        private final Class<? extends ProxyIdentifiableEntity> entityType;
        private final EntityModifiedEventType modifiedEventType;
        private final String eventTypeMessageKey;

        public EntityModifiedEventPayloadIdentifier(final Class<? extends ProxyIdentifiableEntity> entityType,
                final EntityModifiedEventType modifiedEventType) {
            this(entityType, modifiedEventType, null);
        }

        public EntityModifiedEventPayloadIdentifier(final Class<? extends ProxyIdentifiableEntity> entityType,
                final EntityModifiedEventType modifiedEventType, final String eventTypeMessageKey) {
            this(null, entityType, modifiedEventType, eventTypeMessageKey);
        }

        public EntityModifiedEventPayloadIdentifier(final Class<? extends ProxyIdentifiableEntity> parentType,
                final Class<? extends ProxyIdentifiableEntity> entityType,
                final EntityModifiedEventType modifiedEventType) {
            this(parentType, entityType, modifiedEventType, null);
        }

        public EntityModifiedEventPayloadIdentifier(final Class<? extends ProxyIdentifiableEntity> parentType,
                final Class<? extends ProxyIdentifiableEntity> entityType,
                final EntityModifiedEventType modifiedEventType, final String eventTypeMessageKey) {
            this.parentType = parentType;
            this.entityType = entityType;
            this.modifiedEventType = modifiedEventType;
            this.eventTypeMessageKey = eventTypeMessageKey;
        }

        public Class<? extends ProxyIdentifiableEntity> getParentType() {
            return parentType;
        }

        public Class<? extends ProxyIdentifiableEntity> getEntityType() {
            return entityType;
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
            return getParentType() != null
                    ? Objects.hash(getParentType().getName(), getEntityType().getName(), modifiedEventType)
                    : Objects.hash(getEntityType().getName(), modifiedEventType);
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
            return Objects.equals(this.getParentType(), other.getParentType())
                    && Objects.equals(this.getEntityType(), other.getEntityType())
                    && Objects.equals(this.modifiedEventType, other.modifiedEventType);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("Parent Type", getParentType() != null ? getParentType().getName() : "-")
                    .add("Entity Type", getEntityType().getName()).add("ModifiedEventType", modifiedEventType.name())
                    .toString();
        }
    }
}
