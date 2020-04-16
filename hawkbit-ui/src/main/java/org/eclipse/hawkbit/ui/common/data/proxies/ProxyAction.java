/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.Action.Status;

/**
 * Proxy for {@link Action}
 */
public class ProxyAction extends ProxyIdentifiableEntity {
    private static final long serialVersionUID = 1L;

    private ActionType actionType;
    private Action.Status status;
    private boolean isActive;
    private IsActiveDecoration isActiveDecoration;
    private String dsNameVersion;
    private Long lastModifiedAt;
    private String rolloutName;
    private String maintenanceWindow;
    private transient Optional<ZonedDateTime> maintenanceWindowStartTime;
    private long forcedTime;

    public String getMaintenanceWindow() {
        return maintenanceWindow;
    }

    public void setMaintenanceWindow(final String maintenanceWindow) {
        this.maintenanceWindow = maintenanceWindow;
    }

    /**
     * Gets the status literal.
     *
     * @return status literal
     */
    public Action.Status getStatus() {
        return status;
    }

    /**
     * Sets the status literal.
     *
     * @param status
     *            literal
     */
    public void setStatus(final Action.Status status) {
        this.status = status;
    }

    /**
     * Flag that indicates if the action is active.
     *
     * @return <code>true</code> if the action is active, otherwise
     *         <code>false</code>
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the flag that indicates if the action is active.
     *
     * @param isActive
     *            <code>true</code> if the action is active, otherwise
     *            <code>false</code>
     */
    public void setActive(final boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Gets a pre-calculated literal combining <code>ProxyAction#isActive</code>
     * and <code>ProxyAction#getStatus</code> states.
     *
     * @return pre-calculated literal
     */
    public IsActiveDecoration getIsActiveDecoration() {
        return isActiveDecoration;
    }

    /**
     * Sets the pre-calculated literal combining
     * <code>ProxyAction#isActive</code> and <code>ProxyAction#getStatus</code>
     * states.
     *
     * @param isActiveDecoration
     *            pre-calculated literal
     */
    public void setIsActiveDecoration(final IsActiveDecoration isActiveDecoration) {
        this.isActiveDecoration = isActiveDecoration;
    }

    /**
     * Pre-calculated value that is set up by distribution set name and version.
     *
     * @return pre-calculated value combining name and version
     */
    public String getDsNameVersion() {
        return dsNameVersion;
    }

    /**
     * Sets the pre-calculated value combining distribution set name and
     * version.
     *
     * @param dsNameVersion
     *            combined value
     */
    public void setDsNameVersion(final String dsNameVersion) {
        this.dsNameVersion = dsNameVersion;
    }

    /**
     * Get raw long-value for lastModifiedAt-date.
     *
     * @return raw long-value for lastModifiedAt-date
     */
    public Long getLastModifiedAt() {
        return lastModifiedAt;
    }

    /**
     * Set raw long-value for lastModifiedAt-date.
     *
     * @param lastModifiedAt
     *            raw long-value for lastModifiedAt-date
     */
    public void setLastModifiedAt(final Long lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    /**
     * Gets the rolloutName.
     *
     * @return rolloutName
     */
    public String getRolloutName() {
        return rolloutName;
    }

    /**
     * Sets the rolloutName.
     *
     * @param rolloutName
     */
    public void setRolloutName(final String rolloutName) {
        this.rolloutName = rolloutName;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(final ActionType actionType) {
        this.actionType = actionType;
    }

    public Optional<ZonedDateTime> getMaintenanceWindowStartTime() {
        return maintenanceWindowStartTime;
    }

    public void setMaintenanceWindowStartTime(final Optional<ZonedDateTime> maintenanceWindowStartTime) {
        this.maintenanceWindowStartTime = maintenanceWindowStartTime;
    }

    public long getForcedTime() {
        return forcedTime;
    }

    public void setForcedTime(final long forcedTime) {
        this.forcedTime = forcedTime;
    }

    public boolean isHitAutoForceTime(final long hitTimeMillis) {
        if (ActionType.TIMEFORCED == getActionType()) {
            return hitTimeMillis >= getForcedTime();
        }
        return false;
    }

    public boolean isCancelingOrCanceled() {
        return Status.CANCELING == getStatus() || Status.CANCELED == getStatus();
    }

    public boolean isForce() {
        switch (getActionType()) {
        case FORCED:
            return true;
        case TIMEFORCED:
            return isHitAutoForceTime(System.currentTimeMillis());
        default:
            return false;
        }
    }

    /**
     * Pre-calculated decoration value combining
     * <code>ProxyAction#isActive</code> and <code>ProxyAction#getStatus</code>
     * states.
     */
    public enum IsActiveDecoration {

        /**
         * Active label decoration type for {@code ProxyAction#isActive()==true}
         */
        ACTIVE("active"),

        /**
         * Active label decoration type for
         * {@code ProxyAction#isActive()==false}
         */
        IN_ACTIVE("inactive"),

        /**
         * Active label decoration type for {@code ProxyAction#isActive()==true}
         * AND {@code ProxyAction#getStatus()==Action.Status.ERROR}
         */
        IN_ACTIVE_ERROR("inactiveerror"),

        /**
         * {@code ProxyAction#getStatus()==Action.Status.SCHEDULED}
         */
        SCHEDULED("scheduled");

        private final String msgName;

        IsActiveDecoration(final String msgName) {
            this.msgName = msgName;
        }

        public String getMsgName() {
            return msgName;
        }
    }
}
