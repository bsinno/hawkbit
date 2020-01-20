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
import org.eclipse.hawkbit.ui.common.data.filters.TargetManagementFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Data provider for {@link Target}, which dynamically loads a batch of
 * {@link Target} entities from backend and maps them to corresponding
 * {@link ProxyTarget} entities.
 */
public class TargetManagementStateDataProvider
        extends ProxyDataProvider<ProxyTarget, Target, TargetManagementFilterParams> {

    private static final long serialVersionUID = 1L;

    private final transient TargetManagement targetManagement;

    public TargetManagementStateDataProvider(final TargetManagement targetManagement,
            final TargetToProxyTargetMapper entityMapper) {
        super(entityMapper, new Sort(Direction.DESC, "lastModifiedAt"));

        this.targetManagement = targetManagement;
    }

    @Override
    protected Optional<Slice<Target>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<TargetManagementFilterParams> filter) {
        if (!filter.isPresent()) {
            return Optional.of(targetManagement.findAll(pageRequest));
        }

        return filter.map(filterParams -> {
            final Long pinnedDistId = filterParams.getPinnedDistId();
            final String searchText = filterParams.getSearchText();
            final Collection<TargetUpdateStatus> targetUpdateStatusList = filterParams.getTargetUpdateStatusList();
            final Boolean overdueState = filterParams.getOverdueState();
            final Long distributionId = filterParams.getDistributionId();
            final Boolean noTagClicked = filterParams.getNoTagClicked();
            final String[] targetTags = filterParams.getTargetTags();
            final Long targetFilterQueryId = filterParams.getTargetFilterQueryId();

            if (pinnedDistId != null) {
                return targetManagement.findByFilterOrderByLinkedDistributionSet(pageRequest, pinnedDistId,
                        new FilterParams(targetUpdateStatusList, overdueState, searchText, distributionId, noTagClicked,
                                targetTags));
            } else if (targetFilterQueryId != null) {
                return targetManagement.findByTargetFilterQuery(pageRequest, targetFilterQueryId);
            } else if (filterParams.isAnyFilterSelected()) {
                return targetManagement.findByFilters(pageRequest, new FilterParams(targetUpdateStatusList,
                        overdueState, searchText, distributionId, noTagClicked, targetTags));
            } else {
                return targetManagement.findAll(pageRequest);
            }
        });
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<TargetManagementFilterParams> filter) {
        return filter.map(filterParams -> {
            final String searchText = filterParams.getSearchText();
            final Collection<TargetUpdateStatus> targetUpdateStatusList = filterParams.getTargetUpdateStatusList();
            final Boolean overdueState = filterParams.getOverdueState();
            final Long distributionId = filterParams.getDistributionId();
            final Boolean noTagClicked = filterParams.getNoTagClicked();
            final String[] targetTags = filterParams.getTargetTags();
            final Long targetFilterQueryId = filterParams.getTargetFilterQueryId();

            if (targetFilterQueryId != null) {
                return targetManagement.countByTargetFilterQuery(targetFilterQueryId);
            } else if (filterParams.isAnyFilterSelected()) {
                return targetManagement.countByFilters(targetUpdateStatusList, overdueState, searchText, distributionId,
                        noTagClicked, targetTags);
            } else {
                return targetManagement.count();
            }
        }).orElse(targetManagement.count());
    }
}
