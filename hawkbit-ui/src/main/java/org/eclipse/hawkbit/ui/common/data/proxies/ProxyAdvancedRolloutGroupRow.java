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

/**
 * Proxy for advanced rollout group row.
 */
public class ProxyAdvancedRolloutGroupRow implements Serializable {

    private static final long serialVersionUID = 1L;

    private String groupName;
    private String targetFilterQuery;
    private Long targetFilterId;
    private Float targetPercentage;
    private String triggerThresholdPercentage;
    private String errorThresholdPercentage;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }

    public String getTargetFilterQuery() {
        return targetFilterQuery;
    }

    public void setTargetFilterQuery(final String targetFilterQuery) {
        this.targetFilterQuery = targetFilterQuery;
    }

    public Long getTargetFilterId() {
        return targetFilterId;
    }

    public void setTargetFilterId(final Long targetFilterId) {
        this.targetFilterId = targetFilterId;
    }

    public Float getTargetPercentage() {
        return targetPercentage;
    }

    public void setTargetPercentage(final Float targetPercentage) {
        this.targetPercentage = targetPercentage;
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
}
