/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.filters;

import java.io.Serializable;

import org.eclipse.hawkbit.ui.common.data.providers.SoftwareModuleDistributionsStateDataProvider;

/**
 * Filter params for {@link SoftwareModuleDistributionsStateDataProvider}.
 */
public class SwFilterParams implements Serializable {
    private static final long serialVersionUID = 1L;

    private String searchText;
    private Long softwareModuleTypeId;
    private Long lastSelectedDistributionId;

    public SwFilterParams() {
        this(null, null, null);
    }

    /**
     * Constructor.
     * 
     * @param searchText
     * @param softwareModuleTypeId
     * @param lastSelectedDistributionId
     */
    public SwFilterParams(final String searchText, final Long softwareModuleTypeId,
            final Long lastSelectedDistributionId) {
        this.searchText = searchText;
        this.softwareModuleTypeId = softwareModuleTypeId;
        this.lastSelectedDistributionId = lastSelectedDistributionId;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(final String searchText) {
        this.searchText = searchText;
    }

    public Long getSoftwareModuleTypeId() {
        return softwareModuleTypeId;
    }

    public void setSoftwareModuleTypeId(final Long softwareModuleTypeId) {
        this.softwareModuleTypeId = softwareModuleTypeId;
    }

    public Long getLastSelectedDistributionId() {
        return lastSelectedDistributionId;
    }

    public void setLastSelectedDistributionId(final Long lastSelectedDistributionId) {
        this.lastSelectedDistributionId = lastSelectedDistributionId;
    }
}
