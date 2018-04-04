/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.model;

import org.eclipse.hawkbit.repository.exception.InvalidMaintenanceScheduleException;
import org.eclipse.hawkbit.repository.model.Action.ActionType;

/**
 * A custom view on {@link Target} with {@link ActionType}.
 *
 */
public class TargetWithActionType {

    private final String controllerId;
    private final ActionType actionType;
    private final long forceTime;
    private String maintenanceSchedule;
    private String maintenanceWindowDuration;
    private String maintenanceWindowTimeZone;

    public TargetWithActionType(final String controllerId) {
        this.controllerId = controllerId;
        this.actionType = ActionType.FORCED;
        this.forceTime = 0;
    }

    public TargetWithActionType(final String controllerId, final ActionType actionType, final long forceTime) {
        this.controllerId = controllerId;
        this.actionType = actionType != null ? actionType : ActionType.FORCED;
        this.forceTime = forceTime;
    }

    /**
     * Constructor that also accepts maintenance schedule parameters and checks
     * for validity of the specified maintenance schedule.
     *
     * @param controllerId
     *            for which the action is created.
     * @param actionType
     *            specified for the action.
     * @param maintenanceSchedule
     *            is the cron expression to be used for scheduling maintenance
     *            windows. Expression has 6 mandatory fields and 1 last optional
     *            field: "second minute hour dayofmonth month weekday year"
     * @param maintenanceWindowDuration
     *            in HH:mm:ss format specifying the duration of a maintenance
     *            window, for example 00:30:00 for 30 minutes
     * @param maintenanceWindowTimeZone
     *            is the time zone specified as +/-hh:mm offset from UTC, for
     *            example +02:00 for CET summer time and +00:00 for UTC. The
     *            start time of a maintenance window calculated based on the
     *            cron expression is relative to this time zone.
     *
     * @throws InvalidMaintenanceScheduleException
     *             if the parameters do not define a valid maintenance schedule.
     */
    public TargetWithActionType(final String controllerId, final ActionType actionType, final long forceTime,
            final String maintenanceSchedule, final String maintenanceWindowDuration,
            final String maintenanceWindowTimeZone) {
        this(controllerId, actionType, forceTime);

        this.maintenanceSchedule = maintenanceSchedule;
        this.maintenanceWindowDuration = maintenanceWindowDuration;
        this.maintenanceWindowTimeZone = maintenanceWindowTimeZone;
    }

    public ActionType getActionType() {
        if (actionType != null) {
            return actionType;
        }
        // default value
        return ActionType.FORCED;
    }

    public long getForceTime() {
        if (actionType == ActionType.TIMEFORCED) {
            return forceTime;
        }
        return RepositoryModelConstants.NO_FORCE_TIME;
    }

    public String getControllerId() {
        return controllerId;
    }

    /**
     * Returns the maintenance schedule for the {@link Action}.
     *
     * @return cron expression as {@link String}.
     */
    public String getMaintenanceSchedule() {
        return this.maintenanceSchedule;
    }

    /**
     * Returns the duration of maintenance window for the {@link Action}.
     *
     * @return duration in HH:mm:ss format as {@link String}.
     */
    public String getMaintenanceWindowDuration() {
        return maintenanceWindowDuration;
    }

    /**
     * Returns the timezone of maintenance window for the {@link Action}.
     *
     * @return the timezone offset from UTC in +/-hh:mm as {@link String}.
     */
    public String getMaintenanceWindowTimeZone() {
        return maintenanceWindowTimeZone;
    }
}
