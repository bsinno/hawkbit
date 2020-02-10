/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.io.Serializable;

import org.eclipse.hawkbit.ui.common.data.filters.TargetManagementFilterParams;
import org.eclipse.hawkbit.ui.common.entity.DistributionSetIdName;

public class TargetGridLayoutUiState implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean maximized;
    private String searchFilter;
    private Long selectedTargetId;
    private DistributionSetIdName filterDsIdNameVersion;
    private String pinnedControllerId;

    // TODO: check if it is right, make sure to update it in TargetGrid
    private TargetManagementFilterParams targetManagementFilterParams;

    public boolean isMaximized() {
        return maximized;
    }

    public void setMaximized(final boolean maximized) {
        this.maximized = maximized;
    }

    public String getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(final String searchFilter) {
        this.searchFilter = searchFilter;
    }

    public Long getSelectedTargetId() {
        return selectedTargetId;
    }

    public void setSelectedTargetId(final Long selectedTargetId) {
        this.selectedTargetId = selectedTargetId;
    }

    public DistributionSetIdName getFilterDsIdNameVersion() {
        return filterDsIdNameVersion;
    }

    public void setFilterDsIdNameVersion(final DistributionSetIdName filterDsIdNameVersion) {
        this.filterDsIdNameVersion = filterDsIdNameVersion;
    }

    public TargetManagementFilterParams getTargetManagementFilterParams() {
        return targetManagementFilterParams;
    }

    public void setTargetManagementFilterParams(final TargetManagementFilterParams targetManagementFilterParams) {
        this.targetManagementFilterParams = targetManagementFilterParams;
    }

    public String getPinnedControllerId() {
        return pinnedControllerId;
    }

    public void setPinnedControllerId(String pinnedControllerId) {
        this.pinnedControllerId = pinnedControllerId;
    }
}
