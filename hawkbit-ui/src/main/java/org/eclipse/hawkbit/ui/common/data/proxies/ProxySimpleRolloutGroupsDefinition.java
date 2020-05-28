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
 * Proxy for simple rollout group definition.
 */
public class ProxySimpleRolloutGroupsDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer numberOfGroups;
    private String triggerThresholdPercentage;
    private String errorThresholdPercentage;

    public Integer getNumberOfGroups() {
        return numberOfGroups;
    }

    public void setNumberOfGroups(final Integer numberOfGroups) {
        this.numberOfGroups = numberOfGroups;
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
