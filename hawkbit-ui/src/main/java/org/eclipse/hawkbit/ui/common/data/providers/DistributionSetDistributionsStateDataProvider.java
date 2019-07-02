/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.providers;

import java.util.Optional;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.DistributionSetFilter;
import org.eclipse.hawkbit.repository.model.DistributionSetFilter.DistributionSetFilterBuilder;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.distributions.state.ManageDistFilters;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.util.StringUtils;

/**
 * Data provider for {@link DistributionSet}, which dynamically loads a batch of
 * {@link DistributionSet} entities from backend and maps them to corresponding
 * {@link ProxyDistributionSet} entities.
 */
public class DistributionSetDistributionsStateDataProvider
        extends ProxyDataProvider<ProxyDistributionSet, DistributionSet, String> {

    private static final long serialVersionUID = 1L;

    private final transient DistributionSetManagement distributionSetManagement;
    private final ManageDistFilters distributionsUiState;

    public DistributionSetDistributionsStateDataProvider(final DistributionSetManagement distributionSetManagement,
            final ManageDistFilters distributionsUiState, final DistributionSetToProxyDistributionMapper entityMapper) {
        super(entityMapper);

        this.distributionSetManagement = distributionSetManagement;
        this.distributionsUiState = distributionsUiState;
    }

    @Override
    protected Optional<Slice<DistributionSet>> loadBeans(final PageRequest pageRequest, final String filter) {
        return Optional
                .of(distributionSetManagement.findByDistributionSetFilter(pageRequest, getDistributionSetFilter()));
    }

    private DistributionSetFilter getDistributionSetFilter() {
        final String searchText = getSearchTextFromUiState();
        final DistributionSetType distributionSetType = getDistributionSetTypeFromUiState();

        final DistributionSetFilterBuilder distributionSetFilterBuilder = new DistributionSetFilterBuilder()
                .setIsDeleted(false);

        if (!StringUtils.isEmpty(searchText) || distributionSetType != null) {
            distributionSetFilterBuilder.setSearchText(searchText).setSelectDSWithNoTag(false)
                    .setType(distributionSetType);
        }

        return distributionSetFilterBuilder.build();
    }

    private String getSearchTextFromUiState() {
        return distributionsUiState.getSearchText().filter(searchText -> !StringUtils.isEmpty(searchText))
                .map(value -> String.format("%%%s%%", value)).orElse(null);
    }

    private DistributionSetType getDistributionSetTypeFromUiState() {
        return distributionsUiState.getClickedDistSetType();
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final String filter) {
        return distributionSetManagement.findByDistributionSetFilter(pageRequest, getDistributionSetFilter())
                .getTotalElements();
    }

}
