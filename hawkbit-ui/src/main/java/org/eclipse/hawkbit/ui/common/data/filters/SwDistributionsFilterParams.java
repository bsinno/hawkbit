/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.filters;

import org.eclipse.hawkbit.ui.common.data.providers.SoftwareModuleDistributionsStateDataProvider;

/**
 * Filter params for {@link SoftwareModuleDistributionsStateDataProvider}.
 */
public class SwDistributionsFilterParams {
    private final String searchText;
    private final Long softwareModuleTypeId;
    private final Long lastSelectedDistributionId;

    /**
     * Constructor.
     * 
     * @param searchText
     * @param softwareModuleTypeId
     * @param lastSelectedDistributionId
     */
    public SwDistributionsFilterParams(final String searchText, final Long softwareModuleTypeId,
            final Long lastSelectedDistributionId) {
        this.searchText = searchText;
        this.softwareModuleTypeId = softwareModuleTypeId;
        this.lastSelectedDistributionId = lastSelectedDistributionId;
    }

    public String getSearchText() {
        return searchText;
    }

    public Long getSoftwareModuleTypeId() {
        return softwareModuleTypeId;
    }

    public Long getLastSelectedDistributionId() {
        return lastSelectedDistributionId;
    }
}
