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

    private final Long pinnedDistId;
    private final String searchText;
    private final Collection<TargetUpdateStatus> targetUpdateStatusList;
    private final Boolean overdueState;
    private final Long distributionId;
    private final Boolean noTagClicked;
    private final String[] targetTags;
    private final Long targetFilterQueryId;

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

    public Long getTargetFilterQueryId() {
        return targetFilterQueryId;
    }

    public String getSearchText() {
        return searchText;
    }

    public Collection<TargetUpdateStatus> getTargetUpdateStatusList() {
        return targetUpdateStatusList;
    }

    public Boolean getOverdueState() {
        return overdueState;
    }

    public Long getDistributionId() {
        return distributionId;
    }

    public Boolean getNoTagClicked() {
        return noTagClicked;
    }

    public String[] getTargetTags() {
        return targetTags;
    }
}
