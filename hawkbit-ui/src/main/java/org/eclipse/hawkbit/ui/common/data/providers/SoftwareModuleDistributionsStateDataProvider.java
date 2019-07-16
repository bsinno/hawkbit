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

import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.model.AssignedSoftwareModule;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.common.data.mappers.AssignedSoftwareModuleToProxyMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAssignedSoftwareModule;
import org.eclipse.hawkbit.ui.distributions.state.ManageSoftwareModuleFilters;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.util.StringUtils;

/**
 * Data provider for {@link AssignedSoftwareModule}, which dynamically loads a
 * batch of {@link AssignedSoftwareModule} entities from backend and maps them
 * to corresponding {@link ProxyAssignedSoftwareModule} entities.
 */
public class SoftwareModuleDistributionsStateDataProvider
        extends ProxyDataProvider<ProxyAssignedSoftwareModule, AssignedSoftwareModule, Void> {

    private static final long serialVersionUID = 1L;

    private final transient SoftwareModuleManagement softwareModuleManagement;
    private final ManageSoftwareModuleFilters distributionsUiState;
    private final Long orderByDistId;

    public SoftwareModuleDistributionsStateDataProvider(final SoftwareModuleManagement softwareModuleManagement,
            final ManageSoftwareModuleFilters distributionsUiState, final Long lastSelectedDistribution,
            final AssignedSoftwareModuleToProxyMapper entityMapper) {
        super(entityMapper);

        this.softwareModuleManagement = softwareModuleManagement;
        this.distributionsUiState = distributionsUiState;
        this.orderByDistId = lastSelectedDistribution;
    }

    // TODO: use filter instead of uiState
    @Override
    protected Optional<Slice<AssignedSoftwareModule>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<Void> filter) {
        return Optional.of(softwareModuleManagement.findAllOrderBySetAssignmentAndModuleNameAscModuleVersionAsc(
                pageRequest, orderByDistId, getSearchTextFromUiState(), getSoftwareModuleTypeIdFromUiState()));
    }

    private String getSearchTextFromUiState() {
        return distributionsUiState.getSearchText().filter(searchText -> !StringUtils.isEmpty(searchText))
                .map(value -> String.format("%%%s%%", value)).orElse(null);
    }

    private Long getSoftwareModuleTypeIdFromUiState() {
        return distributionsUiState.getSoftwareModuleType().map(SoftwareModuleType::getId).orElse(null);
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<Void> filter) {
        final Long typeId = getSoftwareModuleTypeIdFromUiState();
        final String searchText = getSearchTextFromUiState();

        if (typeId == null && StringUtils.isEmpty(searchText)) {
            return softwareModuleManagement.count();
        } else {
            return softwareModuleManagement.countByTextAndType(searchText, typeId);
        }
    }

}
