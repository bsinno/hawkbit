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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetManagementStateDataProvider;

/**
 * Filter params for {@link DistributionSetManagementStateDataProvider}.
 */
public class DsManagementFilterParams implements Serializable {
    private static final long serialVersionUID = 1L;

    private String searchText;
    private boolean noTagClicked;
    private Collection<String> distributionSetTags;
    private String pinnedTargetControllerId;

    /**
     * Constructor.
     * 
     * @param searchText
     * @param noTagClicked
     * @param distributionSetTags
     * @param pinnedTargetControllerId
     */
    public DsManagementFilterParams(final String searchText, final boolean noTagClicked,
            final List<String> distributionSetTags, final String pinnedTargetControllerId) {
        this.searchText = searchText;
        this.noTagClicked = noTagClicked;
        this.distributionSetTags = distributionSetTags;
        this.pinnedTargetControllerId = pinnedTargetControllerId;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(final String searchText) {
        this.searchText = searchText;
    }

    public Collection<String> getDistributionSetTags() {
        return distributionSetTags;
    }

    public void setDistributionSetTags(final Collection<String> distributionSetTags) {
        this.distributionSetTags = distributionSetTags;
    }

    public String getPinnedTargetControllerId() {
        return pinnedTargetControllerId;
    }

    public void setPinnedTargetControllerId(final String pinnedTargetControllerId) {
        this.pinnedTargetControllerId = pinnedTargetControllerId;
    }

    public DsManagementFilterParams() {
        this("", false, new ArrayList<>(), "");
    }

    public boolean isNoTagClicked() {
        return noTagClicked;
    }

    public void setNoTagClicked(final boolean noTagClicked) {
        this.noTagClicked = noTagClicked;
    }
}
