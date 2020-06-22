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

    /**
     * Constructor
     */
    public ProxyRolloutWindow() {
        this.rolloutForm = new ProxyRolloutForm();
        this.simpleGroupsDefinition = new ProxySimpleRolloutGroupsDefinition();
        this.rolloutApproval = new ProxyRolloutApproval();
    }

    /**
     * Constructor for ProxyRolloutWindow
     *
     * @param rollout
     *          ProxyRollout
     */
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

    /**
     * Gets the rollout form id
     *
     * @return id
     */
    public Long getId() {
        return rolloutForm.getId();
    }

    /**
     * Sets the form id
     *
     * @param id
     *         rollout form id
     */
    public void setId(final Long id) {
        rolloutForm.setId(id);
    }

    /**
     * Gets the action type
     *
     * @return rollout action type
     */
    public ActionType getActionType() {
        return rolloutForm.getActionType();
    }

    /**
     * Sets the actionType
     *
     * @param actionType
     *          rollout action type
     */
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

    /**
     * Gets the rollout form name
     *
     * @return form name
     */
    public String getName() {
        return rolloutForm.getName();
    }

    /**
     * Sets the form name
     *
     * @param name
     *          rollout form name
     */
    public void setName(final String name) {
        rolloutForm.setName(name);
    }

    /**
     * Gets the rollout form description
     *
     * @return form description
     */
    public String getDescription() {
        return rolloutForm.getDescription();
    }

    /**
     * Sets the form description
     *
     * @param description
     *          rollout form description
     */
    public void setDescription(final String description) {
        rolloutForm.setDescription(description);
    }

    /**
     * Gets the rollout form forced time
     *
     * @return form forced time
     */
    public Long getForcedTime() {
        return rolloutForm.getForcedTime();
    }

    /**
     * Sets the form forcedTime
     *
     * @param forcedTime
     *          rollout form forced time
     */
    public void setForcedTime(final Long forcedTime) {
        rolloutForm.setForcedTime(forcedTime);
    }

    /**
     * Gets the rollout status
     *
     * @return rollout status
     */
    public RolloutStatus getStatus() {
        return status;
    }

    /**
     * Sets the rollout status
     *
     * @param status
 *              RolloutStatus
     */
    public void setStatus(final RolloutStatus status) {
        this.status = status;
    }

    /**
     * Gets the approvalRemark
     *
     * @return approvalRemark
     */
    public String getApprovalRemark() {
        return rolloutApproval.getApprovalRemark();
    }

    /**
     * Sets the approvalRemark
     *
     * @param approvalRemark
     *          Remark for approval
     */
    public void setApprovalRemark(final String approvalRemark) {
        rolloutApproval.setApprovalRemark(approvalRemark);
    }

    /**
     * Gets the rollout form targetFilterQuery
     *
     * @return targetFilterQuery
     */
    public String getTargetFilterQuery() {
        return rolloutForm.getTargetFilterQuery();
    }

    /**
     * Sets the targetFilterQuery
     *
     * @param targetFilterQuery
     *         Rollout form target filter query
     */
    public void setTargetFilterQuery(final String targetFilterQuery) {
        rolloutForm.setTargetFilterQuery(targetFilterQuery);
    }

    /**
     * Gets the total targets
     *
     * @return totalTargets
     */
    public Long getTotalTargets() {
        return totalTargets;
    }

    public void setTotalTargets(final Long totalTargets) {
        this.totalTargets = totalTargets;
    }

    /**
     * Gets the time rollout starts at
     *
     * @return startAt
     */
    public Long getStartAt() {
        return rolloutForm.getStartAt();
    }

    /**
     * Sets the startAt
     *
     * @param startAt
     *          time rollout starts at
     */
    public void setStartAt(final Long startAt) {
        rolloutForm.setStartAt(startAt);
    }

    /**
     * Gets the Id of rollout form distribution set
     *
     * @return distributionSetId
     */
    public Long getDistributionSetId() {
        return rolloutForm.getDistributionSetId();
    }

    /**
     * Sets the distributionSetId
     *
     * @param distributionSetId
     *              Id of rollout form distribution set
     */
    public void setDistributionSetId(final Long distributionSetId) {
        rolloutForm.setDistributionSetId(distributionSetId);
    }

    public Long getTargetFilterId() {
        return rolloutForm.getTargetFilterId();
    }

    /**
     * Sets the targetFilterId
     *
     * @param targetFilterId
     *              Id of rollout form targetFilter
     */
    public void setTargetFilterId(final Long targetFilterId) {
        rolloutForm.setTargetFilterId(targetFilterId);
    }

    /**
     * Gets the triggerThresholdPercentage
     *
     * @return triggerThresholdPercentage
     */
    public String getTriggerThresholdPercentage() {
        return simpleGroupsDefinition.getTriggerThresholdPercentage();
    }

    /**
     * Sets the triggerThresholdPercentage
     *
     * @param triggerThresholdPercentage
     *          triggerThresholdPercentage value of rollout simple group
     */
    public void setTriggerThresholdPercentage(final String triggerThresholdPercentage) {
        simpleGroupsDefinition.setTriggerThresholdPercentage(triggerThresholdPercentage);
    }

    /**
     * Gets the errorThresholdPercentage
     *
     * @return errorThresholdPercentage
     */
    public String getErrorThresholdPercentage() {
        return simpleGroupsDefinition.getErrorThresholdPercentage();
    }

    /**
     * Sets the errorThresholdPercentage
     *
     * @param errorThresholdPercentage
     *          errorThresholdPercentage value of rollout simple group
     */
    public void setErrorThresholdPercentage(final String errorThresholdPercentage) {
        simpleGroupsDefinition.setErrorThresholdPercentage(errorThresholdPercentage);
    }

    /**
     * Gets the rollout approval decision
     *
     * @return approvalDecision
     */
    public ApprovalDecision getApprovalDecision() {
        return rolloutApproval.getApprovalDecision();
    }

    /**
     * Sets the rollout approvalDecision
     *
     * @param approvalDecision
     *          Rollout decesion approval or deny
     */
    public void setApprovalDecision(final ApprovalDecision approvalDecision) {
        rolloutApproval.setApprovalDecision(approvalDecision);
    }

    /**
     * Gets the start option
     *
     * @return Rollout start options
     */
    public AutoStartOption getAutoStartOption() {
        return rolloutForm.getStartOption();
    }

    /**
     * Sets the autoStartOption
     *
     * @param autoStartOption
     *          Rollout start options
     */
    public void setAutoStartOption(final AutoStartOption autoStartOption) {
        rolloutForm.setStartOption(autoStartOption);
    }

    /**
     * Gets the rollout form
     *
     * @return rolloutForm
     */
    public ProxyRolloutForm getRolloutForm() {
        return rolloutForm;
    }

    /**
     * Sets the rolloutForm
     *
     * @param rolloutForm
     *          Rollout form
     */
    public void setRolloutForm(final ProxyRolloutForm rolloutForm) {
        this.rolloutForm = rolloutForm;
    }

    /**
     * Gets the rollout simpleGroupsDefinition
     *
     * @return ProxySimpleRolloutGroupsDefinition
     */
    public ProxySimpleRolloutGroupsDefinition getSimpleGroupsDefinition() {
        return simpleGroupsDefinition;
    }

    /**
     * Sets the rollout simpleRolloutGroupsDefinition
     *
     * @param simpleGroupsDefinition
     *          ProxySimpleRolloutGroupsDefinition
     */
    public void setSimpleGroupsDefinition(final ProxySimpleRolloutGroupsDefinition simpleGroupsDefinition) {
        this.simpleGroupsDefinition = simpleGroupsDefinition;
    }

    /**
     * @return Rollout start time in milliseconds
     */
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

    /**
     * @return Rollout auto start option
     */
    public AutoStartOption getOptionByStartAt() {
        if (getStartAt() == null) {
            return AutoStartOptionGroupLayout.AutoStartOption.MANUAL;
        } else if (getStartAt() < System.currentTimeMillis()) {
            return AutoStartOptionGroupLayout.AutoStartOption.AUTO_START;
        } else {
            return AutoStartOptionGroupLayout.AutoStartOption.SCHEDULED;
        }
    }

    /**
     * Gets the Rollout group definition mode
     *
     * @return groupDefinitionMode
     */
    public GroupDefinitionMode getGroupDefinitionMode() {
        return groupDefinitionMode;
    }

    /**
     * Sets the groupDefinitionMode
     *
     * @param groupDefinitionMode
     *          Rollout group definition mode
     */
    public void setGroupDefinitionMode(final GroupDefinitionMode groupDefinitionMode) {
        this.groupDefinitionMode = groupDefinitionMode;
    }

    /**
     * Gest the List of rolloutGroupDefinitions
     *
     * @return advancedRolloutGroupDefinitions
     */
    public List<RolloutGroupCreate> getAdvancedRolloutGroupDefinitions() {
        return advancedRolloutGroupDefinitions;
    }

    /**
     * Sets the advancedRolloutGroupDefinitions
     *
     * @param advancedRolloutGroupDefinitions
     *          List of rolloutGroupDefinitions
     */
    public void setAdvancedRolloutGroupDefinitions(final List<RolloutGroupCreate> advancedRolloutGroupDefinitions) {
        this.advancedRolloutGroupDefinitions = advancedRolloutGroupDefinitions;
    }

    /**
     * Gets the List of rollout groups
     *
     * @return advancedRolloutGroups
     */
    public List<RolloutGroup> getAdvancedRolloutGroups() {
        return advancedRolloutGroups;
    }

    /**
     * Sets the advancedRolloutGroups
     *
     * @param advancedRolloutGroups
     *          List of rollout groups
     */
    public void setAdvancedRolloutGroups(final List<RolloutGroup> advancedRolloutGroups) {
        this.advancedRolloutGroups = advancedRolloutGroups;
    }

    /**
     * Gets the rollout approval
     *
     * @return rolloutApproval
     */
    public ProxyRolloutApproval getRolloutApproval() {
        return rolloutApproval;
    }

    /**
     * Sets the rolloutApproval
     *
     * @param rolloutApproval
     *          Rollout approval
     */
    public void setRolloutApproval(final ProxyRolloutApproval rolloutApproval) {
        this.rolloutApproval = rolloutApproval;
    }

    /**
     * The rollout group definition
     */
    public enum GroupDefinitionMode {
        SIMPLE, ADVANCED;
    }
}
