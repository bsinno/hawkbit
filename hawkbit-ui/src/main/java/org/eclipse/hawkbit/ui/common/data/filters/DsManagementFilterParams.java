/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.filters;

import java.util.List;

import org.eclipse.hawkbit.ui.common.data.providers.TargetManagementStateDataProvider;
import org.eclipse.hawkbit.ui.common.entity.TargetIdName;

/**
 * Filter params for {@link TargetManagementStateDataProvider}.
 */
public class DsManagementFilterParams {
    private final String searchText;
    private final Boolean isNoTagClicked;
    private final List<String> distributionSetTags;
    private final TargetIdName pinnedTarget;

    /**
     * Constructor.
     * 
     * @param searchText
     * @param isNoTagClicked
     * @param distributionSetTags
     * @param pinnedTarget
     */
    public DsManagementFilterParams(final String searchText, final Boolean isNoTagClicked,
            final List<String> distributionSetTags, final TargetIdName pinnedTarget) {
        this.searchText = searchText;
        this.isNoTagClicked = isNoTagClicked;
        this.distributionSetTags = distributionSetTags;
        this.pinnedTarget = pinnedTarget;
    }

    public List<String> getDistributionSetTags() {
        return distributionSetTags;
    }

    public Boolean getIsNoTagClicked() {
        return isNoTagClicked;
    }

    public String getSearchText() {
        return searchText;
    }

    public TargetIdName getPinnedTarget() {
        return pinnedTarget;
    }
}
