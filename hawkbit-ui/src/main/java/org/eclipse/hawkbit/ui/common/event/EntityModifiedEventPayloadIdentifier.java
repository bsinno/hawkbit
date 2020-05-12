package org.eclipse.hawkbit.ui.common.event;

import java.util.Objects;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.springframework.util.StringUtils;

import com.google.common.base.MoreObjects;

public class EntityModifiedEventPayloadIdentifier {
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
            final Class<? extends ProxyIdentifiableEntity> entityType, final EntityModifiedEventType modifiedEventType,
            final String eventTypeMessageKey) {
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
