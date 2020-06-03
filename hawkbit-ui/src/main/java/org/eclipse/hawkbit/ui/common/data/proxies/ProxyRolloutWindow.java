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
import java.util.List;

import org.eclipse.hawkbit.repository.builder.RolloutGroupCreate;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.Rollout.ApprovalDecision;
import org.eclipse.hawkbit.repository.model.Rollout.RolloutStatus;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AutoStartOptionGroupLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AutoStartOptionGroupLayout.AutoStartOption;

/**
 * Proxy entity representing rollout popup window bean.
 */
public class ProxyRolloutWindow implements Serializable {
    private static final long serialVersionUID = 1L;

    private RolloutStatus status;
    private Long totalTargets;
    private ProxyRolloutForm rolloutForm;
    private ProxySimpleRolloutGroupsDefinition simpleGroupsDefinition;
    private transient List<RolloutGroupCreate> advancedRolloutGroupDefinitions;
    private transient List<RolloutGroup> advancedRolloutGroups;
    private GroupDefinitionMode groupDefinitionMode;
    private ProxyRolloutApproval rolloutApproval;

    public ProxyRolloutWindow() {
        this.rolloutForm = new ProxyRolloutForm();
        this.simpleGroupsDefinition = new ProxySimpleRolloutGroupsDefinition();
        this.rolloutApproval = new ProxyRolloutApproval();
    }

    public ProxyRolloutWindow(final ProxyRollout rollout) {
        this();

        this.totalTargets = rollout.getTotalTargets();
        this.status = rollout.getStatus();
        setId(rollout.getId());
        setName(rollout.getName());
        setDescription(rollout.getDescription());
        setActionType(rollout.getActionType());
        setStartAt(rollout.getStartAt());
        setForcedTime(rollout.getForcedTime());
        setDistributionSetId(rollout.getDistributionSetId());
        setTargetFilterQuery(rollout.getTargetFilterQuery());
        setNumberOfGroups(rollout.getNumberOfGroups());
        setApprovalRemark(rollout.getApprovalRemark());
        setApprovalDecision(RolloutStatus.APPROVAL_DENIED == rollout.getStatus() ? ApprovalDecision.DENIED
                : ApprovalDecision.APPROVED);
    }

    public Long getId() {
        return rolloutForm.getId();
    }

    public void setId(final Long id) {
        rolloutForm.setId(id);
    }

    public ActionType getActionType() {
        return rolloutForm.getActionType();
    }

    public void setActionType(final ActionType actionType) {
        rolloutForm.setActionType(actionType);
    }

    /**
     * @return the numberOfGroups
     */
    public Integer getNumberOfGroups() {
        return simpleGroupsDefinition.getNumberOfGroups();
    }

    /**
     * @param numberOfGroups
     *            the numberOfGroups to set
     */
    public void setNumberOfGroups(final Integer numberOfGroups) {
        simpleGroupsDefinition.setNumberOfGroups(numberOfGroups);
    }

    public String getName() {
        return rolloutForm.getName();
    }

    public void setName(final String name) {
        rolloutForm.setName(name);
    }

    public String getDescription() {
        return rolloutForm.getDescription();
    }

    public void setDescription(final String description) {
        rolloutForm.setDescription(description);
    }

    public Long getForcedTime() {
        return rolloutForm.getForcedTime();
    }

    public void setForcedTime(final Long forcedTime) {
        rolloutForm.setForcedTime(forcedTime);
    }

    public RolloutStatus getStatus() {
        return status;
    }

    public void setStatus(final RolloutStatus status) {
        this.status = status;
    }

    public String getApprovalRemark() {
        return rolloutApproval.getApprovalRemark();
    }

    public void setApprovalRemark(final String approvalRemark) {
        rolloutApproval.setApprovalRemark(approvalRemark);
    }

    public String getTargetFilterQuery() {
        return rolloutForm.getTargetFilterQuery();
    }

    public void setTargetFilterQuery(final String targetFilterQuery) {
        rolloutForm.setTargetFilterQuery(targetFilterQuery);
    }

    public Long getTotalTargets() {
        return totalTargets;
    }

    public void setTotalTargets(final Long totalTargets) {
        this.totalTargets = totalTargets;
    }

    public Long getStartAt() {
        return rolloutForm.getStartAt();
    }

    public void setStartAt(final Long startAt) {
        rolloutForm.setStartAt(startAt);
    }

    public Long getDistributionSetId() {
        return rolloutForm.getDistributionSetId();
    }

    public void setDistributionSetId(final Long distributionSetId) {
        rolloutForm.setDistributionSetId(distributionSetId);
    }

    public Long getTargetFilterId() {
        return rolloutForm.getTargetFilterId();
    }

    public void setTargetFilterId(final Long targetFilterId) {
        rolloutForm.setTargetFilterId(targetFilterId);
    }

    public String getTriggerThresholdPercentage() {
        return simpleGroupsDefinition.getTriggerThresholdPercentage();
    }

    public void setTriggerThresholdPercentage(final String triggerThresholdPercentage) {
        simpleGroupsDefinition.setTriggerThresholdPercentage(triggerThresholdPercentage);
    }

    public String getErrorThresholdPercentage() {
        return simpleGroupsDefinition.getErrorThresholdPercentage();
    }

    public void setErrorThresholdPercentage(final String errorThresholdPercentage) {
        simpleGroupsDefinition.setErrorThresholdPercentage(errorThresholdPercentage);
    }

    public ApprovalDecision getApprovalDecision() {
        return rolloutApproval.getApprovalDecision();
    }

    public void setApprovalDecision(final ApprovalDecision approvalDecision) {
        rolloutApproval.setApprovalDecision(approvalDecision);
    }

    public AutoStartOption getAutoStartOption() {
        return rolloutForm.getStartOption();
    }

    public void setAutoStartOption(final AutoStartOption autoStartOption) {
        rolloutForm.setStartOption(autoStartOption);
    }

    public ProxyRolloutForm getRolloutForm() {
        return rolloutForm;
    }

    public void setRolloutForm(final ProxyRolloutForm rolloutForm) {
        this.rolloutForm = rolloutForm;
    }

    public ProxySimpleRolloutGroupsDefinition getSimpleGroupsDefinition() {
        return simpleGroupsDefinition;
    }

    public void setSimpleGroupsDefinition(final ProxySimpleRolloutGroupsDefinition simpleGroupsDefinition) {
        this.simpleGroupsDefinition = simpleGroupsDefinition;
    }

    public Long getStartAtByOption() {
        switch (getAutoStartOption()) {
        case AUTO_START:
            return System.currentTimeMillis();
        case SCHEDULED:
            return getStartAt();
        case MANUAL:
        default:
            return null;
        }
    }

    public AutoStartOption getOptionByStartAt() {
        if (getStartAt() == null) {
            return AutoStartOptionGroupLayout.AutoStartOption.MANUAL;
        } else if (getStartAt() < System.currentTimeMillis()) {
            return AutoStartOptionGroupLayout.AutoStartOption.AUTO_START;
        } else {
            return AutoStartOptionGroupLayout.AutoStartOption.SCHEDULED;
        }
    }

    public GroupDefinitionMode getGroupDefinitionMode() {
        return groupDefinitionMode;
    }

    public void setGroupDefinitionMode(final GroupDefinitionMode groupDefinitionMode) {
        this.groupDefinitionMode = groupDefinitionMode;
    }

    public List<RolloutGroupCreate> getAdvancedRolloutGroupDefinitions() {
        return advancedRolloutGroupDefinitions;
    }

    public void setAdvancedRolloutGroupDefinitions(final List<RolloutGroupCreate> advancedRolloutGroupDefinitions) {
        this.advancedRolloutGroupDefinitions = advancedRolloutGroupDefinitions;
    }

    public List<RolloutGroup> getAdvancedRolloutGroups() {
        return advancedRolloutGroups;
    }

    public void setAdvancedRolloutGroups(final List<RolloutGroup> advancedRolloutGroups) {
        this.advancedRolloutGroups = advancedRolloutGroups;
    }

    public ProxyRolloutApproval getRolloutApproval() {
        return rolloutApproval;
    }

    public void setRolloutApproval(final ProxyRolloutApproval rolloutApproval) {
        this.rolloutApproval = rolloutApproval;
    }

    public enum GroupDefinitionMode {
        SIMPLE, ADVANCED;
    }
}
