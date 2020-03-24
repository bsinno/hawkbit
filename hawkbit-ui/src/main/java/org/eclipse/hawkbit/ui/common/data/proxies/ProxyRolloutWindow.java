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
import org.eclipse.hawkbit.repository.model.Rollout.ApprovalDecision;
import org.eclipse.hawkbit.repository.model.Rollout.RolloutStatus;
import org.eclipse.hawkbit.ui.common.data.aware.ActionTypeAware;
import org.eclipse.hawkbit.ui.common.data.aware.DescriptionAware;
import org.eclipse.hawkbit.ui.common.data.aware.DsIdAware;
import org.eclipse.hawkbit.ui.common.data.aware.NameAware;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AutoStartOptionGroupLayout.AutoStartOption;

/**
 * Proxy entity representing rollout popup window bean.
 */
public class ProxyRolloutWindow implements Serializable, NameAware, DescriptionAware, ActionTypeAware, DsIdAware {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private ActionType actionType;
    private AutoStartOption autoStartOption;
    private Long startAt;
    private Long forcedTime;
    private Long totalTargets;
    private Long distributionSetId;
    private Long targetFilterId;
    private String targetFilterQuery;
    private Integer numberOfGroups;
    private String triggerThresholdPercentage;
    private String errorThresholdPercentage;
    private RolloutStatus status;
    private String approvalRemark;
    private ApprovalDecision approvalDecision;

    public ProxyRolloutWindow() {
    }

    public ProxyRolloutWindow(final ProxyRollout rollout) {
        id = rollout.getId();
        name = rollout.getName();
        description = rollout.getDescription();
        actionType = rollout.getActionType();
        startAt = rollout.getStartAt();
        forcedTime = rollout.getForcedTime();
        totalTargets = rollout.getTotalTargets();
        distributionSetId = rollout.getDistributionSetId();
        targetFilterQuery = rollout.getTargetFilterQuery();
        numberOfGroups = rollout.getNumberOfGroups();
        status = rollout.getStatus();
        approvalRemark = rollout.getApprovalRemark();
        approvalDecision = RolloutStatus.APPROVAL_DENIED == rollout.getStatus() ? ApprovalDecision.DENIED
                : ApprovalDecision.APPROVED;
        autoStartOption = AutoStartOption.MANUAL;
    }

    @Override
    public ActionType getActionType() {
        return actionType;
    }

    @Override
    public void setActionType(final ActionType actionType) {
        this.actionType = actionType;
    }

    /**
     * @return the numberOfGroups
     */
    public Integer getNumberOfGroups() {
        return numberOfGroups;
    }

    /**
     * @param numberOfGroups
     *            the numberOfGroups to set
     */
    public void setNumberOfGroups(final Integer numberOfGroups) {
        this.numberOfGroups = numberOfGroups;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public Long getForcedTime() {
        return forcedTime;
    }

    @Override
    public void setForcedTime(final Long forcedTime) {
        this.forcedTime = forcedTime;
    }

    public RolloutStatus getStatus() {
        return status;
    }

    public void setStatus(final RolloutStatus status) {
        this.status = status;
    }

    public String getApprovalRemark() {
        return approvalRemark;
    }

    public void setApprovalRemark(final String approvalRemark) {
        this.approvalRemark = approvalRemark;
    }

    public String getTargetFilterQuery() {
        return targetFilterQuery;
    }

    public void setTargetFilterQuery(final String targetFilterQuery) {
        this.targetFilterQuery = targetFilterQuery;
    }

    public Long getTotalTargets() {
        return totalTargets;
    }

    public void setTotalTargets(final Long totalTargets) {
        this.totalTargets = totalTargets;
    }

    public Long getStartAt() {
        return startAt;
    }

    public void setStartAt(final Long startAt) {
        this.startAt = startAt;
    }

    @Override
    public Long getDistributionSetId() {
        return distributionSetId;
    }

    @Override
    public void setDistributionSetId(final Long distributionSetId) {
        this.distributionSetId = distributionSetId;
    }

    public Long getTargetFilterId() {
        return targetFilterId;
    }

    public void setTargetFilterId(final Long targetFilterId) {
        this.targetFilterId = targetFilterId;
    }

    public String getTriggerThresholdPercentage() {
        return triggerThresholdPercentage;
    }

    public void setTriggerThresholdPercentage(final String triggerThresholdPercentage) {
        this.triggerThresholdPercentage = triggerThresholdPercentage;
    }

    public String getErrorThresholdPercentage() {
        return errorThresholdPercentage;
    }

    public void setErrorThresholdPercentage(final String errorThresholdPercentage) {
        this.errorThresholdPercentage = errorThresholdPercentage;
    }

    public ApprovalDecision getApprovalDecision() {
        return approvalDecision;
    }

    public void setApprovalDecision(final ApprovalDecision approvalDecision) {
        this.approvalDecision = approvalDecision;
    }

    public AutoStartOption getAutoStartOption() {
        return autoStartOption;
    }

    public void setAutoStartOption(final AutoStartOption autoStartOption) {
        this.autoStartOption = autoStartOption;
    }
}
