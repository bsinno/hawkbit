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
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.Rollout.ApprovalDecision;
import org.eclipse.hawkbit.repository.model.Rollout.RolloutStatus;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowLayoutComponentBuilder.ERROR_THRESHOLD_OPTIONS;

import com.vaadin.icons.VaadinIcons;

/**
 * Proxy entity representing rollout popup window bean.
 */
public class ProxyRolloutWindow implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private ActionType actionType;
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
    private ERROR_THRESHOLD_OPTIONS errorThresholdOption;

    public ProxyRolloutWindow() {
    }

    public ProxyRolloutWindow(final Rollout rollout) {
        id = rollout.getId();
        name = rollout.getName();
        description = rollout.getDescription();
        actionType = rollout.getActionType();
        startAt = rollout.getStartAt();
        forcedTime = rollout.getForcedTime() > 0 ? rollout.getForcedTime() : null;
        totalTargets = rollout.getTotalTargets();
        distributionSetId = rollout.getDistributionSet().getId();
        targetFilterQuery = rollout.getTargetFilterQuery();
        numberOfGroups = rollout.getRolloutGroupsCreated() > 0 ? rollout.getRolloutGroupsCreated() : null;
        status = rollout.getStatus();
        approvalRemark = rollout.getApprovalRemark();
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
    }

    public ActionType getActionType() {
        return actionType;
    }

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

    public String getAction() {
        return VaadinIcons.SPINNER.getHtml();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Long getForcedTime() {
        return forcedTime;
    }

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

    public Long getDistributionSetId() {
        return distributionSetId;
    }

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

    public ERROR_THRESHOLD_OPTIONS getErrorThresholdOption() {
        return errorThresholdOption;
    }

    public void setErrorThresholdOption(final ERROR_THRESHOLD_OPTIONS errorThresholdOption) {
        this.errorThresholdOption = errorThresholdOption;
    }

}
