/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa.model;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.eclipse.hawkbit.repository.MaintenanceScheduleHelper;
import org.eclipse.hawkbit.repository.event.remote.entity.ActionCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.ActionUpdatedEvent;
import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.ActionStatus;
import org.eclipse.hawkbit.repository.model.BaseEntity;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.helper.EventPublisherHolder;
import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.eclipse.persistence.annotations.ConversionValue;
import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.ObjectTypeConverter;
import org.eclipse.persistence.descriptors.DescriptorEvent;

/**
 * JPA implementation of {@link Action}.
 */
@Table(name = "sp_action", indexes = { @Index(name = "sp_idx_action_01", columnList = "tenant,distribution_set"),
        @Index(name = "sp_idx_action_02", columnList = "tenant,target,active"),
        @Index(name = "sp_idx_action_prim", columnList = "tenant,id") })
@NamedEntityGraphs({ @NamedEntityGraph(name = "Action.ds", attributeNodes = { @NamedAttributeNode("distributionSet") }),
        @NamedEntityGraph(name = "Action.all", attributeNodes = { @NamedAttributeNode("distributionSet"),
                @NamedAttributeNode(value = "target", subgraph = "target.ds") }, subgraphs = @NamedSubgraph(name = "target.ds", attributeNodes = @NamedAttributeNode("assignedDistributionSet"))) })
@Entity
// exception squid:S2160 - BaseEntity equals/hashcode is handling correctly for
// sub entities
@SuppressWarnings("squid:S2160")
public class JpaAction extends AbstractJpaTenantAwareBaseEntity implements Action, EventAwareEntity {
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "distribution_set", nullable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_action_ds"))
    @NotNull
    private JpaDistributionSet distributionSet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target", nullable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_targ_act_hist_targ"))
    @NotNull
    private JpaTarget target;

    @Column(name = "active")
    private boolean active;

    @Column(name = "action_type", nullable = false)
    @ObjectTypeConverter(name = "actionType", objectType = Action.ActionType.class, dataType = Integer.class, conversionValues = {
            @ConversionValue(objectValue = "FORCED", dataValue = "0"),
            @ConversionValue(objectValue = "SOFT", dataValue = "1"),
            @ConversionValue(objectValue = "TIMEFORCED", dataValue = "2") })
    @Convert("actionType")
    @NotNull
    private ActionType actionType;

    @Column(name = "forced_time")
    private long forcedTime;

    @Column(name = "status", nullable = false)
    @ObjectTypeConverter(name = "status", objectType = Action.Status.class, dataType = Integer.class, conversionValues = {
            @ConversionValue(objectValue = "FINISHED", dataValue = "0"),
            @ConversionValue(objectValue = "ERROR", dataValue = "1"),
            @ConversionValue(objectValue = "WARNING", dataValue = "2"),
            @ConversionValue(objectValue = "RUNNING", dataValue = "3"),
            @ConversionValue(objectValue = "CANCELED", dataValue = "4"),
            @ConversionValue(objectValue = "CANCELING", dataValue = "5"),
            @ConversionValue(objectValue = "RETRIEVED", dataValue = "6"),
            @ConversionValue(objectValue = "DOWNLOAD", dataValue = "7"),
            @ConversionValue(objectValue = "SCHEDULED", dataValue = "8"),
            @ConversionValue(objectValue = "CANCEL_REJECTED", dataValue = "9"),
            @ConversionValue(objectValue = "DOWNLOADED", dataValue = "10")})
    @Convert("status")
    @NotNull
    private Status status;

    @CascadeOnDelete
    @OneToMany(mappedBy = "action", targetEntity = JpaActionStatus.class, fetch = FetchType.LAZY)
    private List<JpaActionStatus> actionStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rolloutgroup", updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_action_rolloutgroup"))
    private JpaRolloutGroup rolloutGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rollout", updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_action_rollout"))
    private JpaRollout rollout;

    @Column(name = "maintenance_cron_schedule", length = Action.MAINTENANCE_SCHEDULE_CRON_LENGTH)
    private String maintenanceSchedule;

    @Column(name = "maintenance_duration", length = Action.MAINTENANCE_WINDOW_DURATION_LENGTH)
    private String maintenanceWindowDuration;

    @Column(name = "maintenance_time_zone", length = Action.MAINTENANCE_WINDOW_TIMEZONE_LENGTH)
    private String maintenanceWindowTimeZone;

    /**
     * A transient (non serialized) maintenance schedule helper.
     */
    private transient MaintenanceScheduleHelper scheduleHelper = null;

    @Override
    public DistributionSet getDistributionSet() {
        return distributionSet;
    }

    public void setDistributionSet(final DistributionSet distributionSet) {
        this.distributionSet = (JpaDistributionSet) distributionSet;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActionType(final ActionType actionType) {
        this.actionType = actionType;
    }

    @Override
    public ActionType getActionType() {
        return actionType;
    }

    public List<ActionStatus> getActionStatus() {
        if (actionStatus == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(actionStatus);
    }

    public void setTarget(final Target target) {
        this.target = (JpaTarget) target;
    }

    @Override
    public Target getTarget() {
        return target;
    }

    @Override
    public long getForcedTime() {
        return forcedTime;
    }

    public void setForcedTime(final long forcedTime) {
        this.forcedTime = forcedTime;
    }

    @Override
    public RolloutGroup getRolloutGroup() {
        return rolloutGroup;
    }

    public void setRolloutGroup(final RolloutGroup rolloutGroup) {
        this.rolloutGroup = (JpaRolloutGroup) rolloutGroup;
    }

    @Override
    public Rollout getRollout() {
        return rollout;
    }

    public void setRollout(final Rollout rollout) {
        this.rollout = (JpaRollout) rollout;
    }

    @Override
    public String toString() {
        return "JpaAction [distributionSet=" + distributionSet.getId() + ", version=" + getOptLockRevision() + ", id="
                + getId() + "]";
    }

    @Override
    public void fireCreateEvent(final DescriptorEvent descriptorEvent) {
        EventPublisherHolder.getInstance().getEventPublisher()
                .publishEvent(new ActionCreatedEvent(this, BaseEntity.getIdOrNull(rollout),
                        BaseEntity.getIdOrNull(rolloutGroup), EventPublisherHolder.getInstance().getApplicationId()));
    }

    @Override
    public void fireUpdateEvent(final DescriptorEvent descriptorEvent) {
        EventPublisherHolder.getInstance().getEventPublisher()
                .publishEvent(new ActionUpdatedEvent(this, BaseEntity.getIdOrNull(rollout),
                        BaseEntity.getIdOrNull(rolloutGroup), EventPublisherHolder.getInstance().getApplicationId()));
    }

    @Override
    public void fireDeleteEvent(final DescriptorEvent descriptorEvent) {
        // there is no action deletion
    }

    /**
     * Sets the maintenance schedule.
     *
     * @param maintenanceSchedule
     *            is a cron expression to be used for scheduling.
     */
    public void setMaintenanceSchedule(String maintenanceSchedule) {
        this.maintenanceSchedule = maintenanceSchedule;
    }

    /**
     * Sets the maintenance window duration.
     *
     * @param maintenanceWindowDuration
     *            is the duration of an available maintenance schedule in
     *            HH:mm:ss format.
     */
    public void setMaintenanceWindowDuration(String maintenanceWindowDuration) {
        this.maintenanceWindowDuration = maintenanceWindowDuration;
    }

    /**
     * Sets the time zone to be used for maintenance window.
     *
     * @param maintenanceWindowTimeZone
     *            is the time zone specified as +/-hh:mm offset from UTC for
     *            example +02:00 for CET summer time and +00:00 for UTC. The
     *            start time of a maintenance window calculated based on the
     *            cron expression is relative to this time zone.
     */
    public void setMaintenanceWindowTimeZone(String maintenanceWindowTimeZone) {
        this.maintenanceWindowTimeZone = maintenanceWindowTimeZone;
    }

    /**
     * Get the transient schedule helper. Instantiate one if not already done
     * after deserialization.
     *
     * @return the {@link MaintenanceScheduleHelper} object.
     */
    MaintenanceScheduleHelper getScheduler() {
        if (this.scheduleHelper == null) {
            this.scheduleHelper = new MaintenanceScheduleHelper(maintenanceSchedule, maintenanceWindowDuration,
                    maintenanceWindowTimeZone);
        }
        return this.scheduleHelper;
    }

    /**
     * Returns the duration of each maintenance window in ISO 8601 format.
     *
     * @return the {@link Duration} of each maintenance window.
     */
    Duration getMaintenanceWindowDuration() {
        return Duration.parse(MaintenanceScheduleHelper.convertToISODuration(this.maintenanceWindowDuration));
    }

    /**
     * Returns the start time of next available maintenance window for the
     * {@link Action} as {@link ZonedDateTime}. If a maintenance window is
     * already active, the start time of currently active window is returned.
     *
     * @return the start time as {@link Optional<ZonedDateTime>}.
     */
    public Optional<ZonedDateTime> getMaintenanceWindowStartTime() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.of(maintenanceWindowTimeZone));
        return getScheduler().nextExecution(now.minus(getMaintenanceWindowDuration()));
    }

    /**
     * Returns the end time of next available or active maintenance window for
     * the {@link Action} as {@link ZonedDateTime}. If a maintenance window is
     * already active, the end time of currently active window is returned.
     *
     * @return the end time of window as {@link Optional<ZonedDateTime>}.
     */
    public Optional<ZonedDateTime> getMaintenanceWindowEndTime() {
        if (getMaintenanceWindowStartTime().isPresent()) {
            return Optional.of(getMaintenanceWindowStartTime().get().plus(getMaintenanceWindowDuration()));
        }
        return Optional.empty();
    }

    /**
     * The method checks whether the action has a maintenance schedule defined
     * for it. A maintenance schedule defines a set of maintenance windows
     * during which actual update can be performed. A valid schedule defines at
     * least one maintenance window.
     *
     * @return true if action has a maintenance schedule, else false.
     */
    @Override
    public boolean hasMaintenanceSchedule() {
        return this.maintenanceSchedule != null;
    }

    /**
     * The method checks whether the maintenance schedule has already lapsed for
     * the action, i.e. there are no more windows available for maintenance.
     * Controller manager uses the method to check if the maintenance schedule
     * has lapsed, and automatically cancels the action if it is lapsed.
     *
     * @return true if maintenance schedule has lapsed, else false.
     */
    @Override
    public boolean isMaintenanceScheduleLapsed() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.of(maintenanceWindowTimeZone));
        return !getScheduler().nextExecution(now.minus(getMaintenanceWindowDuration())).isPresent();
    }

    /**
     * The method checks whether a maintenance window is available for the
     * action to proceed. If it is available, a 'true' value is returned. The
     * maintenance window is considered available: 1) If there is no maintenance
     * schedule at all, in which case device can start update any time after
     * download is finished; or 2) the current time is within a scheduled
     * maintenance window start and end time.
     *
     * @return true if maintenance window is available, else false.
     */
    @Override
    public boolean isMaintenanceWindowAvailable() {
        if (!hasMaintenanceSchedule()) {
            // if there is no defined maintenance schedule, a window is always
            // available.
            return true;
        } else if (isMaintenanceScheduleLapsed()) {
            // if a defined maintenance schedule has lapsed, a window is never
            // available.
            return false;
        } else {
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.of(maintenanceWindowTimeZone));
            if (this.getMaintenanceWindowStartTime().isPresent() && this.getMaintenanceWindowEndTime().isPresent()) {
                return now.isAfter(this.getMaintenanceWindowStartTime().get())
                        && now.isBefore(this.getMaintenanceWindowEndTime().get());
            } else {
                return false;
            }
        }
    }
}
