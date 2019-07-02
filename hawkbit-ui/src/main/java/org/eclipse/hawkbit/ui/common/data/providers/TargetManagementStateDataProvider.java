/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.providers;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.hawkbit.repository.FilterParams;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.entity.DistributionSetIdName;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Data provider for {@link Target}, which dynamically loads a batch of
 * {@link Target} entities from backend and maps them to corresponding
 * {@link ProxyTarget} entities.
 */
public class TargetManagementStateDataProvider extends ProxyDataProvider<ProxyTarget, Target, String> {

    private static final long serialVersionUID = 1L;

    private final transient TargetManagement targetManagement;
    private final ManagementUIState managementUIState;

    public TargetManagementStateDataProvider(final TargetManagement targetManagement,
            final ManagementUIState managementUIState, final TargetToProxyTargetMapper entityMapper) {
        super(entityMapper);

        this.targetManagement = targetManagement;
        this.managementUIState = managementUIState;
    }

    @Override
    protected Optional<Slice<Target>> loadBackendEntities(final PageRequest pageRequest, final String filter) {
        final Long pinnedDistId = getPinnedDistIdFromUiState();
        final String searchText = getSearchTextFromUiState();
        final Collection<TargetUpdateStatus> status = getTargetUpdateStatusFromUiState();
        final Boolean overdueState = getOverdueStateFromUiState();
        final Long distributionId = getDistributionIdFromUiState();
        final Boolean noTagClicked = isNoTagClickedFromUiState();
        final String[] targetTags = getTargetTagsFromUiState();
        final Long targetFilterQueryId = getTargetFilterQueryIdFromUiState();

        final boolean isTagSelected = targetTags != null || noTagClicked;
        final boolean isOverdueFilterEnabled = Boolean.TRUE.equals(overdueState);
        final boolean isFilterSelected = isTagSelected || isOverdueFilterEnabled;
        final boolean isAnyFilterSelected = isFilterSelected || !CollectionUtils.isEmpty(status)
                || distributionId != null || !StringUtils.isEmpty(searchText);

        if (pinnedDistId != null) {
            return Optional.of(targetManagement.findByFilterOrderByLinkedDistributionSet(pageRequest, pinnedDistId,
                    new FilterParams(status, overdueState, searchText, distributionId, noTagClicked, targetTags)));
        } else if (targetFilterQueryId != null) {
            return Optional.of(targetManagement.findByTargetFilterQuery(pageRequest, targetFilterQueryId));
        } else if (!isAnyFilterSelected) {
            return Optional.of(targetManagement.findAll(pageRequest));
        } else {
            return Optional.of(targetManagement.findByFilters(pageRequest,
                    new FilterParams(status, overdueState, searchText, distributionId, noTagClicked, targetTags)));
        }
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final String filter) {
        final String searchText = getSearchTextFromUiState();
        final Collection<TargetUpdateStatus> status = getTargetUpdateStatusFromUiState();
        final Boolean overdueState = getOverdueStateFromUiState();
        final Long distributionId = getDistributionIdFromUiState();
        final Boolean noTagClicked = isNoTagClickedFromUiState();
        final String[] targetTags = getTargetTagsFromUiState();
        final Long targetFilterQueryId = getTargetFilterQueryIdFromUiState();

        final boolean isTagSelected = targetTags != null || noTagClicked;
        final boolean isOverdueFilterEnabled = Boolean.TRUE.equals(overdueState);
        final boolean isFilterSelected = isTagSelected || isOverdueFilterEnabled;
        final boolean isAnyFilterSelected = isFilterSelected || !CollectionUtils.isEmpty(status)
                || distributionId != null || !StringUtils.isEmpty(searchText);

        final long totSize = targetManagement.count();
        long size;

        if (targetFilterQueryId != null) {
            size = targetManagement.countByTargetFilterQuery(targetFilterQueryId);
        } else if (!isAnyFilterSelected) {
            size = totSize;
        } else {
            size = targetManagement.countByFilters(status, overdueState, searchText, distributionId, noTagClicked,
                    targetTags);
        }

        managementUIState.setTargetsCountAll(totSize);
        if (size > SPUIDefinitions.MAX_TABLE_ENTRIES) {
            managementUIState.setTargetsTruncated(size - SPUIDefinitions.MAX_TABLE_ENTRIES);
            size = SPUIDefinitions.MAX_TABLE_ENTRIES;
        } else {
            managementUIState.setTargetsTruncated(null);
        }

        return size;
    }

    private String getSearchTextFromUiState() {
        return managementUIState.getTargetTableFilters().getSearchText()
                .filter(searchText -> !StringUtils.isEmpty(searchText)).map(value -> String.format("%%%s%%", value))
                .orElse(null);
    }

    private Long getPinnedDistIdFromUiState() {
        return managementUIState.getTargetTableFilters().getPinnedDistId().orElse(null);
    }

    private Long getTargetFilterQueryIdFromUiState() {
        return managementUIState.getTargetTableFilters().getTargetFilterQuery().orElse(null);
    }

    private String[] getTargetTagsFromUiState() {
        return managementUIState.getTargetTableFilters().getClickedTargetTags().stream().toArray(String[]::new);
    }

    private Boolean isNoTagClickedFromUiState() {
        return managementUIState.getTargetTableFilters().isNoTagSelected();
    }

    private Long getDistributionIdFromUiState() {
        return managementUIState.getTargetTableFilters().getDistributionSet().map(DistributionSetIdName::getId)
                .orElse(null);
    }

    private Boolean getOverdueStateFromUiState() {
        return managementUIState.getTargetTableFilters().isOverdueFilterEnabled();
    }

    private Collection<TargetUpdateStatus> getTargetUpdateStatusFromUiState() {
        return managementUIState.getTargetTableFilters().getClickedStatusTargetTags();
    }
}
