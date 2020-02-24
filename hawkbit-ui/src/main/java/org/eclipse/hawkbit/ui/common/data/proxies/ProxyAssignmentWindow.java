/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import java.io.Serializable;

import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.ui.common.data.aware.ActionTypeAware;

/**
 * Proxy entity representing assignment popup window bean.
 */
public class ProxyAssignmentWindow implements Serializable, ActionTypeAware {

    private static final long serialVersionUID = 1L;

    private ActionType actionType;
    private Long forcedTime;
    private boolean isMaintenanceWindowEnabled;
    private String maintenanceSchedule;
    private String maintenanceDuration;
    private String maintenanceTimeZone;

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(final ActionType actionType) {
        this.actionType = actionType;
    }

    public Long getForcedTime() {
        return forcedTime;
    }

    public void setForcedTime(final Long forcedTime) {
        this.forcedTime = forcedTime;
    }

    public boolean isMaintenanceWindowEnabled() {
        return isMaintenanceWindowEnabled;
    }

    public void setMaintenanceWindowEnabled(boolean isMaintenanceWindowEnabled) {
        this.isMaintenanceWindowEnabled = isMaintenanceWindowEnabled;
    }

    public String getMaintenanceSchedule() {
        return maintenanceSchedule;
    }

    public void setMaintenanceSchedule(String maintenanceSchedule) {
        this.maintenanceSchedule = maintenanceSchedule;
    }

    public String getMaintenanceDuration() {
        return maintenanceDuration;
    }

    public void setMaintenanceDuration(String maintenanceDuration) {
        this.maintenanceDuration = maintenanceDuration;
    }

    public String getMaintenanceTimeZone() {
        return maintenanceTimeZone;
    }

    public void setMaintenanceTimeZone(String maintenanceTimeZone) {
        this.maintenanceTimeZone = maintenanceTimeZone;
    }
}
