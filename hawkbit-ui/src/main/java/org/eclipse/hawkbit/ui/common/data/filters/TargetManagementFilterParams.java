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

import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.ui.common.data.providers.TargetManagementStateDataProvider;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Filter params for {@link TargetManagementStateDataProvider}.
 */
public class TargetManagementFilterParams implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long pinnedDistId;
    private String searchText;
    private Collection<TargetUpdateStatus> targetUpdateStatusList;
    private boolean overdueState;
    private Long distributionId;
    private boolean noTagClicked;
    private String[] targetTags;
    private Long targetFilterQueryId;

    public TargetManagementFilterParams() {
        this(null, null, new ArrayList<>(), false, null, false, new String[] {}, null);
    }

    /**
     * Constructor.
     * 
     * @param pinnedDistId
     * @param searchText
     * @param targetUpdateStatusList
     * @param overdueState
     * @param distributionId
     * @param noTagClicked
     * @param targetTags
     * @param targetFilterQueryId
     */
    public TargetManagementFilterParams(final Long pinnedDistId, final String searchText,
            final Collection<TargetUpdateStatus> targetUpdateStatusList, final boolean overdueState,
            final Long distributionId, final boolean noTagClicked, final String[] targetTags,
            final Long targetFilterQueryId) {
        this.pinnedDistId = pinnedDistId;
        this.searchText = searchText;
        this.targetUpdateStatusList = targetUpdateStatusList;
        this.setOverdueState(overdueState);
        this.distributionId = distributionId;
        this.setNoTagClicked(noTagClicked);
        this.targetTags = targetTags;
        this.targetFilterQueryId = targetFilterQueryId;
    }

    public boolean isAnyFilterSelected() {
        return isTagSelected() || isOverdueState() || !CollectionUtils.isEmpty(getTargetUpdateStatusList())
                || getDistributionId() != null || !StringUtils.isEmpty(getSearchText());
    }

    private boolean isTagSelected() {
        return (getTargetTags() != null && getTargetTags().length > 0) || isNoTagClicked();
    }

    public Long getPinnedDistId() {
        return pinnedDistId;
    }

    public void setPinnedDistId(final Long pinnedDistId) {
        this.pinnedDistId = pinnedDistId;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(final String searchText) {
        this.searchText = searchText;
    }

    public Collection<TargetUpdateStatus> getTargetUpdateStatusList() {
        return targetUpdateStatusList;
    }

    public void setTargetUpdateStatusList(final Collection<TargetUpdateStatus> targetUpdateStatusList) {
        this.targetUpdateStatusList = targetUpdateStatusList;
    }

    public Long getDistributionId() {
        return distributionId;
    }

    public void setDistributionId(final Long distributionId) {
        this.distributionId = distributionId;
    }

    public String[] getTargetTags() {
        return targetTags;
    }

    public void setTargetTags(final String[] targetTags) {
        this.targetTags = targetTags;
    }

    public Long getTargetFilterQueryId() {
        return targetFilterQueryId;
    }

    public void setTargetFilterQueryId(final Long targetFilterQueryId) {
        this.targetFilterQueryId = targetFilterQueryId;
    }

    public boolean isOverdueState() {
        return overdueState;
    }

    public void setOverdueState(final boolean overdueState) {
        this.overdueState = overdueState;
    }

    public boolean isNoTagClicked() {
        return noTagClicked;
    }

    public void setNoTagClicked(final boolean noTagClicked) {
        this.noTagClicked = noTagClicked;
    }
}
