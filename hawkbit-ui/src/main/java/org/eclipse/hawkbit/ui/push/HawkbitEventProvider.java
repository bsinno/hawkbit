/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.push;

import java.util.Map;
import java.util.Objects;

import org.eclipse.hawkbit.repository.event.entity.EntityIdEvent;
import org.eclipse.hawkbit.repository.event.remote.DistributionSetDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.DistributionSetTagDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.DistributionSetTypeDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.RolloutDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.SoftwareModuleDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.SoftwareModuleTypeDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.TargetDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.TargetFilterQueryDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.TargetTagDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetTagCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetTagUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetTypeCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetTypeUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.RolloutCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.SoftwareModuleCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.SoftwareModuleTypeCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.SoftwareModuleTypeUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.SoftwareModuleUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetFilterQueryCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetFilterQueryUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetTagCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetTagUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetUpdatedEvent;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutGroup;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.push.event.ActionChangedEvent;
import org.eclipse.hawkbit.ui.push.event.RolloutChangedEvent;
import org.eclipse.hawkbit.ui.push.event.RolloutGroupChangedEvent;

import com.cronutils.utils.StringUtils;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;

/**
 * The default hawkbit event provider.
 */
public class HawkbitEventProvider implements UIEventProvider {

    private static final Map<Class<? extends EntityIdEvent>, EntityModifiedEventPayloadIdentifier> EVENTS = Maps
            .newHashMapWithExpectedSize(29);

    static {
        EVENTS.put(TargetCreatedEvent.class, new EntityModifiedEventPayloadIdentifier(ProxyTarget.class,
                EntityModifiedEventType.ENTITY_ADDED, "target.created.event.container.notifcation.message"));
        EVENTS.put(TargetUpdatedEvent.class,
                new EntityModifiedEventPayloadIdentifier(ProxyTarget.class, EntityModifiedEventType.ENTITY_UPDATED));
        EVENTS.put(TargetDeletedEvent.class, new EntityModifiedEventPayloadIdentifier(ProxyTarget.class,
                EntityModifiedEventType.ENTITY_REMOVED, "target.deleted.event.container.notifcation.message"));

        EVENTS.put(DistributionSetCreatedEvent.class,
                new EntityModifiedEventPayloadIdentifier(ProxyDistributionSet.class,
                        EntityModifiedEventType.ENTITY_ADDED,
                        "distribution.created.event.container.notifcation.message"));
        EVENTS.put(DistributionSetUpdatedEvent.class, new EntityModifiedEventPayloadIdentifier(
                ProxyDistributionSet.class, EntityModifiedEventType.ENTITY_UPDATED));
        EVENTS.put(DistributionSetDeletedEvent.class,
                new EntityModifiedEventPayloadIdentifier(ProxyDistributionSet.class,
                        EntityModifiedEventType.ENTITY_REMOVED,
                        "distribution.deleted.event.container.notifcation.message"));

        EVENTS.put(SoftwareModuleCreatedEvent.class, new EntityModifiedEventPayloadIdentifier(ProxySoftwareModule.class,
                EntityModifiedEventType.ENTITY_ADDED, "software.module.created.event.container.notifcation.message"));
        EVENTS.put(SoftwareModuleUpdatedEvent.class, new EntityModifiedEventPayloadIdentifier(ProxySoftwareModule.class,
                EntityModifiedEventType.ENTITY_UPDATED));
        EVENTS.put(SoftwareModuleDeletedEvent.class, new EntityModifiedEventPayloadIdentifier(ProxySoftwareModule.class,
                EntityModifiedEventType.ENTITY_REMOVED, "software.module.deleted.event.container.notifcation.message"));

        EVENTS.put(TargetTagCreatedEvent.class,
                new EntityModifiedEventPayloadIdentifier(ProxyTarget.class, ProxyTag.class,
                        EntityModifiedEventType.ENTITY_ADDED,
                        "target.tag.created.event.container.notifcation.message"));
        EVENTS.put(TargetTagUpdatedEvent.class,
                new EntityModifiedEventPayloadIdentifier(ProxyTarget.class, ProxyTag.class,
                        EntityModifiedEventType.ENTITY_UPDATED,
                        "target.tag.updated.event.container.notifcation.message"));
        EVENTS.put(TargetTagDeletedEvent.class,
                new EntityModifiedEventPayloadIdentifier(ProxyTarget.class, ProxyTag.class,
                        EntityModifiedEventType.ENTITY_REMOVED,
                        "target.tag.deleted.event.container.notifcation.message"));

        EVENTS.put(DistributionSetTagCreatedEvent.class,
                new EntityModifiedEventPayloadIdentifier(ProxyDistributionSet.class, ProxyTag.class,
                        EntityModifiedEventType.ENTITY_ADDED,
                        "distribution.set.tag.created.event.container.notifcation.message"));
        EVENTS.put(DistributionSetTagUpdatedEvent.class,
                new EntityModifiedEventPayloadIdentifier(ProxyDistributionSet.class, ProxyTag.class,
                        EntityModifiedEventType.ENTITY_UPDATED,
                        "distribution.set.tag.updated.event.container.notifcation.message"));
        EVENTS.put(DistributionSetTagDeletedEvent.class,
                new EntityModifiedEventPayloadIdentifier(ProxyDistributionSet.class, ProxyTag.class,
                        EntityModifiedEventType.ENTITY_REMOVED,
                        "distribution.set.tag.deleted.event.container.notifcation.message"));

        EVENTS.put(DistributionSetTypeCreatedEvent.class, new EntityModifiedEventPayloadIdentifier(
                ProxyDistributionSet.class, ProxyType.class, EntityModifiedEventType.ENTITY_ADDED));
        EVENTS.put(DistributionSetTypeUpdatedEvent.class, new EntityModifiedEventPayloadIdentifier(
                ProxyDistributionSet.class, ProxyType.class, EntityModifiedEventType.ENTITY_UPDATED));
        EVENTS.put(DistributionSetTypeDeletedEvent.class, new EntityModifiedEventPayloadIdentifier(
                ProxyDistributionSet.class, ProxyType.class, EntityModifiedEventType.ENTITY_REMOVED));

        EVENTS.put(SoftwareModuleTypeCreatedEvent.class, new EntityModifiedEventPayloadIdentifier(
                ProxySoftwareModule.class, ProxyType.class, EntityModifiedEventType.ENTITY_ADDED));
        EVENTS.put(SoftwareModuleTypeUpdatedEvent.class, new EntityModifiedEventPayloadIdentifier(
                ProxySoftwareModule.class, ProxyType.class, EntityModifiedEventType.ENTITY_UPDATED));
        EVENTS.put(SoftwareModuleTypeDeletedEvent.class, new EntityModifiedEventPayloadIdentifier(
                ProxySoftwareModule.class, ProxyType.class, EntityModifiedEventType.ENTITY_REMOVED));

        EVENTS.put(RolloutCreatedEvent.class,
                new EntityModifiedEventPayloadIdentifier(ProxyRollout.class, EntityModifiedEventType.ENTITY_ADDED));
        EVENTS.put(RolloutChangedEvent.class,
                new EntityModifiedEventPayloadIdentifier(ProxyRollout.class, EntityModifiedEventType.ENTITY_UPDATED));
        EVENTS.put(RolloutDeletedEvent.class,
                new EntityModifiedEventPayloadIdentifier(ProxyRollout.class, EntityModifiedEventType.ENTITY_REMOVED));

        EVENTS.put(RolloutGroupChangedEvent.class, new EntityModifiedEventPayloadIdentifier(ProxyRollout.class,
                ProxyRolloutGroup.class, EntityModifiedEventType.ENTITY_UPDATED));

        EVENTS.put(ActionChangedEvent.class, new EntityModifiedEventPayloadIdentifier(ProxyTarget.class,
                ProxyAction.class, EntityModifiedEventType.ENTITY_UPDATED));

        EVENTS.put(TargetFilterQueryCreatedEvent.class, new EntityModifiedEventPayloadIdentifier(
                ProxyTargetFilterQuery.class, EntityModifiedEventType.ENTITY_ADDED));
        EVENTS.put(TargetFilterQueryUpdatedEvent.class, new EntityModifiedEventPayloadIdentifier(
                ProxyTargetFilterQuery.class, EntityModifiedEventType.ENTITY_UPDATED));
        EVENTS.put(TargetFilterQueryDeletedEvent.class, new EntityModifiedEventPayloadIdentifier(
                ProxyTargetFilterQuery.class, EntityModifiedEventType.ENTITY_REMOVED));
    }

    @Override
    public Map<Class<? extends EntityIdEvent>, EntityModifiedEventPayloadIdentifier> getEvents() {
        return EVENTS;
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

        public boolean shouldBeDeffered() {
            return !StringUtils.isEmpty(eventTypeMessageKey);
        }

        public static EntityModifiedEventPayloadIdentifier of(final EntityModifiedEventPayload eventPayload) {
            return new EntityModifiedEventPayloadIdentifier(eventPayload.getParentType(), eventPayload.getEntityType(),
                    eventPayload.getEntityModifiedEventType());
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
