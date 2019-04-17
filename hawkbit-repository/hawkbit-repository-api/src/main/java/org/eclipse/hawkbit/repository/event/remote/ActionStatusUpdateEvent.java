package org.eclipse.hawkbit.repository.event.remote;

import java.util.List;

import org.eclipse.hawkbit.repository.event.TenantAwareEvent;
import org.eclipse.hawkbit.repository.model.Action.Status;

/**
 * Information that represents status of a target for a given actionId.
 */
public class ActionStatusUpdateEvent implements TenantAwareEvent {
    private final Long actionId;
    private final String tenant;
    private final List<String> messages;
    private final Status status;

    public ActionStatusUpdateEvent(String tenant, Long actionId, Status status,
            List<String> messages) {
        this.actionId = actionId;
        this.tenant = tenant;
        this.messages = messages;
        this.status = status;
    }

    /**
     * @return the actionId for which the status is available.
     */
    public Long getActionId() {
        return actionId;
    }

    /**
     * @return the tenant under which the execution is happening.
     */
    public String getTenant() {
        return tenant;
    }

    /**
     * @return list of messages associated with the status.
     */
    public List<String> getMessages() {
        return messages;
    }

    /**
     * @return the status of the target in the context of distributionSetId.
     */
    public Status getStatus() {
        return status;
    }

}
