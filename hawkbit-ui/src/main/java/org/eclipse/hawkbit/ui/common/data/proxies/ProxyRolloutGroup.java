/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupErrorAction;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupErrorCondition;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupStatus;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupSuccessAction;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupSuccessCondition;
import org.eclipse.hawkbit.repository.model.TotalTargetCountStatus;

/**
 * Proxy for {@link RolloutGroup} with custom properties.
 */
public class ProxyRolloutGroup extends ProxyNamedEntity {

    private static final long serialVersionUID = 1L;

    private String finishedPercentage;

    private Long runningTargetsCount;

    private Long scheduledTargetsCount;

    private Long cancelledTargetsCount;

    private Long errorTargetsCount;

    private Long finishedTargetsCount;

    private Long notStartedTargetsCount;

    private Boolean isActionRecieved = Boolean.FALSE;

    private String totalTargetsCount;

    private RolloutGroupStatus status;

    private transient TotalTargetCountStatus totalTargetCountStatus;

    private RolloutGroupSuccessCondition successCondition;
    private String successConditionExp;
    private RolloutGroupSuccessAction successAction;
    private String successActionExp;
    private RolloutGroupErrorCondition errorCondition;
    private String errorConditionExp;
    private RolloutGroupErrorAction errorAction;
    private String errorActionExp;

    public ProxyRolloutGroup() {
    }

    public ProxyRolloutGroup(final Long id) {
        super(id);
    }

    public String getFinishedPercentage() {
        return finishedPercentage;
    }

    public void setFinishedPercentage(final String finishedPercentage) {
        this.finishedPercentage = finishedPercentage;
    }

    public Long getRunningTargetsCount() {
        return runningTargetsCount;
    }

    public void setRunningTargetsCount(final Long runningTargetsCount) {
        this.runningTargetsCount = runningTargetsCount;
    }

    public Long getScheduledTargetsCount() {
        return scheduledTargetsCount;
    }

    public void setScheduledTargetsCount(final Long scheduledTargetsCount) {
        this.scheduledTargetsCount = scheduledTargetsCount;
    }

    public Long getCancelledTargetsCount() {
        return cancelledTargetsCount;
    }

    public void setCancelledTargetsCount(final Long cancelledTargetsCount) {
        this.cancelledTargetsCount = cancelledTargetsCount;
    }

    public Long getErrorTargetsCount() {
        return errorTargetsCount;
    }

    public void setErrorTargetsCount(final Long errorTargetsCount) {
        this.errorTargetsCount = errorTargetsCount;
    }

    public Long getFinishedTargetsCount() {
        return finishedTargetsCount;
    }

    public void setFinishedTargetsCount(final Long finishedTargetsCount) {
        this.finishedTargetsCount = finishedTargetsCount;
    }

    public Long getNotStartedTargetsCount() {
        return notStartedTargetsCount;
    }

    public void setNotStartedTargetsCount(final Long notStartedTargetsCount) {
        this.notStartedTargetsCount = notStartedTargetsCount;
    }

    public Boolean getIsActionRecieved() {
        return isActionRecieved;
    }

    public void setIsActionRecieved(final Boolean isActionRecieved) {
        this.isActionRecieved = isActionRecieved;
    }

    public String getTotalTargetsCount() {
        return totalTargetsCount;
    }

    public void setTotalTargetsCount(final String totalTargetsCount) {
        this.totalTargetsCount = totalTargetsCount;
    }

    public RolloutGroupStatus getStatus() {
        return status;
    }

    public void setStatus(final RolloutGroupStatus status) {
        this.status = status;
    }

    public TotalTargetCountStatus getTotalTargetCountStatus() {
        return totalTargetCountStatus;
    }

    public void setTotalTargetCountStatus(final TotalTargetCountStatus totalTargetCountStatus) {
        this.totalTargetCountStatus = totalTargetCountStatus;
    }

    public RolloutGroupSuccessCondition getSuccessCondition() {
        return successCondition;
    }

    public void setSuccessCondition(final RolloutGroupSuccessCondition successCondition) {
        this.successCondition = successCondition;
    }

    public String getSuccessConditionExp() {
        return successConditionExp;
    }

    public void setSuccessConditionExp(final String successConditionExp) {
        this.successConditionExp = successConditionExp;
    }

    public RolloutGroupSuccessAction getSuccessAction() {
        return successAction;
    }

    public void setSuccessAction(final RolloutGroupSuccessAction successAction) {
        this.successAction = successAction;
    }

    public String getSuccessActionExp() {
        return successActionExp;
    }

    public void setSuccessActionExp(final String successActionExp) {
        this.successActionExp = successActionExp;
    }

    public RolloutGroupErrorCondition getErrorCondition() {
        return errorCondition;
    }

    public void setErrorCondition(final RolloutGroupErrorCondition errorCondition) {
        this.errorCondition = errorCondition;
    }

    public String getErrorConditionExp() {
        return errorConditionExp;
    }

    public void setErrorConditionExp(final String errorConditionExp) {
        this.errorConditionExp = errorConditionExp;
    }

    public RolloutGroupErrorAction getErrorAction() {
        return errorAction;
    }

    public void setErrorAction(final RolloutGroupErrorAction errorAction) {
        this.errorAction = errorAction;
    }

    public String getErrorActionExp() {
        return errorActionExp;
    }

    public void setErrorActionExp(final String errorActionExp) {
        this.errorActionExp = errorActionExp;
    }

}
