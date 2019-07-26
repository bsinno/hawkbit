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
import org.eclipse.hawkbit.ui.common.data.filters.DsManagementFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.entity.TargetIdName;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.util.StringUtils;

/**
 * Data provider for {@link DistributionSet}, which dynamically loads a batch of
 * {@link DistributionSet} entities from backend and maps them to corresponding
 * {@link ProxyDistributionSet} entities.
 */
public class DistributionSetManagementStateDataProvider
        extends ProxyDataProvider<ProxyDistributionSet, DistributionSet, DsManagementFilterParams> {

    private static final long serialVersionUID = 1L;

    private final transient DistributionSetManagement distributionSetManagement;

    public DistributionSetManagementStateDataProvider(final DistributionSetManagement distributionSetManagement,
            final DistributionSetToProxyDistributionMapper entityMapper) {
        super(entityMapper);

        this.distributionSetManagement = distributionSetManagement;
    }

    @Override
    protected Optional<Slice<DistributionSet>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<DsManagementFilterParams> filter) {
        return filter.map(filterParams -> {
            final TargetIdName pinnedTarget = filterParams.getPinnedTarget();
            final DistributionSetFilterBuilder distributionSetFilterBuilder = getDistributionSetFilterBuilder(
                    filterParams);

            if (pinnedTarget != null) {
                return distributionSetManagement.findByFilterAndAssignedInstalledDsOrderedByLinkTarget(pageRequest,
                        distributionSetFilterBuilder, pinnedTarget.getControllerId());
            }

            return distributionSetManagement.findByDistributionSetFilter(pageRequest,
                    distributionSetFilterBuilder.build());
        });
    }

    private DistributionSetFilterBuilder getDistributionSetFilterBuilder(final DsManagementFilterParams filterParams) {
        final TargetIdName pinnedTarget = filterParams.getPinnedTarget();
        final String searchText = filterParams.getSearchText();
        final Boolean noTagClicked = filterParams.getIsNoTagClicked();
        final List<String> distributionTags = filterParams.getDistributionSetTags();

        final DistributionSetFilterBuilder distributionSetFilterBuilder = new DistributionSetFilterBuilder()
                .setIsDeleted(false).setIsComplete(true);

        if (pinnedTarget != null || !distributionTags.isEmpty() || !StringUtils.isEmpty(searchText) || noTagClicked) {
            return distributionSetFilterBuilder.setSearchText(searchText).setSelectDSWithNoTag(noTagClicked)
                    .setTagNames(distributionTags);
        }

        return distributionSetFilterBuilder;

    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<DsManagementFilterParams> filter) {
        return filter.map(filterParams -> {
            final TargetIdName pinnedTarget = filterParams.getPinnedTarget();
            final DistributionSetFilterBuilder distributionSetFilterBuilder = getDistributionSetFilterBuilder(
                    filterParams);

            if (pinnedTarget != null) {
                return distributionSetManagement.findByFilterAndAssignedInstalledDsOrderedByLinkTarget(pageRequest,
                        distributionSetFilterBuilder, pinnedTarget.getControllerId()).getTotalElements();
            }

            return distributionSetManagement
                    .findByDistributionSetFilter(pageRequest, distributionSetFilterBuilder.build()).getTotalElements();
        }).orElse(0L);
    }

}
