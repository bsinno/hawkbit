/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import java.util.Map;

import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.Rollout.RolloutStatus;
import org.eclipse.hawkbit.repository.model.TotalTargetCountStatus.Status;

/**
 * Proxy for {@link Rollout} with custom properties.
 */
public class ProxyRollout extends ProxyNamedEntity {

    private static final long serialVersionUID = 1L;

    private String distributionSetNameVersion;

    private Integer numberOfGroups;

    private long totalTargets;

    private String targetFilterQuery;

    private Long forcedTime;

    private RolloutStatus status;

    private Map<Status, Long> statusTotalCountMap;

    private String approvalDecidedBy;

    private String approvalRemark;

    private Long startAt;

    private ActionType actionType;

    private Long distributionSetId;

    public ProxyRollout() {
    }

    public ProxyRollout(final Long id) {
        super(id);
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(final ActionType actionType) {
        this.actionType = actionType;
    }

    public String getDistributionSetNameVersion() {
        return distributionSetNameVersion;
    }

    public void setDistributionSetNameVersion(final String distributionSetNameVersion) {
        this.distributionSetNameVersion = distributionSetNameVersion;
    }

    public Integer getNumberOfGroups() {
        return numberOfGroups;
    }

    public void setNumberOfGroups(final Integer numberOfGroups) {
        this.numberOfGroups = numberOfGroups;
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

    public String getApprovalDecidedBy() {
        return approvalDecidedBy;
    }

    public void setApprovalDecidedBy(final String approvalDecidedBy) {
        this.approvalDecidedBy = approvalDecidedBy;
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

    public long getTotalTargets() {
        return totalTargets;
    }

    public void setTotalTargets(final long totalTargets) {
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

    public Map<Status, Long> getStatusTotalCountMap() {
        return statusTotalCountMap;
    }

    public void setStatusTotalCountMap(final Map<Status, Long> statusTotalCountMap) {
        this.statusTotalCountMap = statusTotalCountMap;
    }
}
