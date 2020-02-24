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
import java.util.Map;

import org.eclipse.hawkbit.ui.common.data.aware.DescriptionAware;
import org.eclipse.hawkbit.ui.common.data.aware.DsIdAware;

/**
 * Proxy entity representing rollout popup window bean.
 */
public class ProxyBulkUploadWindow implements Serializable, DescriptionAware, DsIdAware {
    private static final long serialVersionUID = 1L;

    private Long distributionSetId;
    private Map<Long, String> tagIdsWithNameToAssign;
    private String description;

    public Long getDistributionSetId() {
        return distributionSetId;
    }

    public void setDistributionSetId(final Long distributionSetId) {
        this.distributionSetId = distributionSetId;
    }

    public Map<Long, String> getTagIdsWithNameToAssign() {
        return tagIdsWithNameToAssign;
    }

    public void setTagIdsWithNameToAssign(final Map<Long, String> tagIdsWithNameToAssign) {
        this.tagIdsWithNameToAssign = tagIdsWithNameToAssign;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
