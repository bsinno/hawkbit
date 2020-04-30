/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import org.eclipse.hawkbit.ui.common.data.filters.TargetManagementFilterParams;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdNameVersion;
import org.eclipse.hawkbit.ui.common.state.GridLayoutUiState;

public class TargetGridLayoutUiState extends GridLayoutUiState {
    private static final long serialVersionUID = 1L;

    private Long pinnedTargetId;
    // TODO: check if we could only keep pinned targetId or controllerId
    private String pinnedControllerId;
    private ProxyIdNameVersion filterDsIdNameVersion;

    // TODO: check if it is right, make sure to update it in TargetGrid
    private TargetManagementFilterParams targetManagementFilterParams;

    public TargetManagementFilterParams getTargetManagementFilterParams() {
        return targetManagementFilterParams;
    }

    public void setTargetManagementFilterParams(final TargetManagementFilterParams targetManagementFilterParams) {
        this.targetManagementFilterParams = targetManagementFilterParams;
    }

    public String getPinnedControllerId() {
        return pinnedControllerId;
    }

    public void setPinnedControllerId(final String pinnedControllerId) {
        this.pinnedControllerId = pinnedControllerId;
    }

    public Long getPinnedTargetId() {
        return pinnedTargetId;
    }

    public void setPinnedTargetId(final Long pinnedTargetId) {
        this.pinnedTargetId = pinnedTargetId;
    }

    public ProxyIdNameVersion getFilterDsIdNameVersion() {
        return filterDsIdNameVersion;
    }

    public void setFilterDsIdNameVersion(final ProxyIdNameVersion filterDsIdNameVersion) {
        this.filterDsIdNameVersion = filterDsIdNameVersion;
    }
}
