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
    private Boolean overdueState;
    private Long distributionId;
    private Boolean noTagClicked;
    private String[] targetTags;
    private Long targetFilterQueryId;

    public TargetManagementFilterParams() {
        this(null, null, new ArrayList<>(), null, null, null, new String[] {}, null);
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
            final Collection<TargetUpdateStatus> targetUpdateStatusList, final Boolean overdueState,
            final Long distributionId, final Boolean noTagClicked, final String[] targetTags,
            final Long targetFilterQueryId) {
        this.pinnedDistId = pinnedDistId;
        this.searchText = searchText;
        this.targetUpdateStatusList = targetUpdateStatusList;
        this.overdueState = overdueState;
        this.distributionId = distributionId;
        this.noTagClicked = noTagClicked;
        this.targetTags = targetTags;
        this.targetFilterQueryId = targetFilterQueryId;
    }

    public boolean isAnyFilterSelected() {
        return isTagSelected() || getOverdueState() || !CollectionUtils.isEmpty(getTargetUpdateStatusList())
                || getDistributionId() != null || !StringUtils.isEmpty(getSearchText());
    }

    private boolean isTagSelected() {
        return getTargetTags() != null || getNoTagClicked();
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

    public Boolean getOverdueState() {
        return overdueState;
    }

    public void setOverdueState(final Boolean overdueState) {
        this.overdueState = overdueState;
    }

    public Long getDistributionId() {
        return distributionId;
    }

    public void setDistributionId(final Long distributionId) {
        this.distributionId = distributionId;
    }

    public Boolean getNoTagClicked() {
        return noTagClicked;
    }

    public void setNoTagClicked(final Boolean noTagClicked) {
        this.noTagClicked = noTagClicked;
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
}
