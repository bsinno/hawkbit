/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.providers;

import java.util.List;
import java.util.Optional;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.DistributionSetFilter.DistributionSetFilterBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.entity.TargetIdName;
import org.eclipse.hawkbit.ui.management.state.DistributionTableFilters;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.util.StringUtils;

/**
 * Data provider for {@link DistributionSet}, which dynamically loads a batch of
 * {@link DistributionSet} entities from backend and maps them to corresponding
 * {@link ProxyDistributionSet} entities.
 */
public class DistributionSetManagementStateDataProvider
        extends ProxyDataProvider<ProxyDistributionSet, DistributionSet, String> {

    private static final long serialVersionUID = 1L;

    private final transient DistributionSetManagement distributionSetManagement;
    private final DistributionTableFilters managementUiState;

    public DistributionSetManagementStateDataProvider(final DistributionSetManagement distributionSetManagement,
            final DistributionTableFilters managementUiState,
            final DistributionSetToProxyDistributionMapper entityMapper) {
        super(entityMapper);

        this.distributionSetManagement = distributionSetManagement;
        this.managementUiState = managementUiState;
    }

    @Override
    protected Optional<Slice<DistributionSet>> loadBackendEntities(final PageRequest pageRequest, final String filter) {
        final TargetIdName pinnedTarget = getPinnedTargetFromUiState();
        final DistributionSetFilterBuilder distributionSetFilterBuilder = getDistributionSetFilterBuilder(pinnedTarget);

        if (pinnedTarget != null) {
            return Optional.of(distributionSetManagement.findByFilterAndAssignedInstalledDsOrderedByLinkTarget(
                    pageRequest, distributionSetFilterBuilder, pinnedTarget.getControllerId()));
        }

        return Optional.of(distributionSetManagement.findByDistributionSetFilter(pageRequest,
                distributionSetFilterBuilder.build()));
    }

    private TargetIdName getPinnedTargetFromUiState() {
        return managementUiState.getPinnedTarget().orElse(null);
    }

    private DistributionSetFilterBuilder getDistributionSetFilterBuilder(final TargetIdName pinnedTarget) {
        final String searchText = getSearchTextFromUiState();
        final Boolean noTagClicked = isNoTagClickedFromUiState();
        final List<String> distributionTags = getDistributionTagsFromUiState();

        final DistributionSetFilterBuilder distributionSetFilterBuilder = new DistributionSetFilterBuilder()
                .setIsDeleted(false).setIsComplete(true);

        if (pinnedTarget != null || !distributionTags.isEmpty() || !StringUtils.isEmpty(searchText) || noTagClicked) {
            return distributionSetFilterBuilder.setSearchText(searchText).setSelectDSWithNoTag(noTagClicked)
                    .setTagNames(distributionTags);
        }

        return distributionSetFilterBuilder;

    }

    private String getSearchTextFromUiState() {
        return managementUiState.getSearchText().filter(searchText -> !StringUtils.isEmpty(searchText))
                .map(value -> String.format("%%%s%%", value)).orElse(null);
    }

    private Boolean isNoTagClickedFromUiState() {
        return managementUiState.isNoTagSelected();
    }

    private List<String> getDistributionTagsFromUiState() {
        return managementUiState.getDistSetTags();
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final String filter) {
        final TargetIdName pinnedTarget = getPinnedTargetFromUiState();
        final DistributionSetFilterBuilder distributionSetFilterBuilder = getDistributionSetFilterBuilder(pinnedTarget);

        if (pinnedTarget != null) {
            return distributionSetManagement.findByFilterAndAssignedInstalledDsOrderedByLinkTarget(pageRequest,
                    distributionSetFilterBuilder, pinnedTarget.getControllerId()).getTotalElements();
        }

        return distributionSetManagement.findByDistributionSetFilter(pageRequest, distributionSetFilterBuilder.build())
                .getTotalElements();
    }

}
