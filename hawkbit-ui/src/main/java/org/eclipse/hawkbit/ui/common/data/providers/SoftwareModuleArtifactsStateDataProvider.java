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
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.artifacts.state.SoftwareModuleFilters;
import org.eclipse.hawkbit.ui.common.data.mappers.SoftwareModuleToProxyMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.StringUtils;

/**
 * Data provider for {@link SoftwareModule}, which dynamically loads a batch of
 * {@link SoftwareModule} entities from backend and maps them to corresponding
 * {@link ProxySoftwareModule} entities.
 */
public class SoftwareModuleArtifactsStateDataProvider
        extends ProxyDataProvider<ProxySoftwareModule, SoftwareModule, String> {

    private static final long serialVersionUID = 1L;

    private final transient SoftwareModuleManagement softwareModuleManagement;
    private final SoftwareModuleFilters artifactsUiState;

    public SoftwareModuleArtifactsStateDataProvider(final SoftwareModuleManagement softwareModuleManagement,
            final SoftwareModuleFilters artifactsUiState, final SoftwareModuleToProxyMapper entityMapper) {
        super(entityMapper, new Sort(Direction.ASC, "name", "version"));

        this.softwareModuleManagement = softwareModuleManagement;
        this.artifactsUiState = artifactsUiState;
    }

    @Override
    protected Optional<Slice<SoftwareModule>> loadBackendEntities(final PageRequest pageRequest, final String filter) {
        final String searchText = getSearchTextFromUiState();
        final Long typeId = getSoftwareModuleTypeIdFromUiState();

        if (typeId == null && StringUtils.isEmpty(searchText)) {
            return Optional.of(softwareModuleManagement.findAll(pageRequest));
        } else {
            return Optional.of(softwareModuleManagement.findByTextAndType(pageRequest, searchText, typeId));
        }
    }

    private String getSearchTextFromUiState() {
        return artifactsUiState.getSearchText().filter(searchText -> !StringUtils.isEmpty(searchText))
                .map(value -> String.format("%%%s%%", value)).orElse(null);
    }

    private Long getSoftwareModuleTypeIdFromUiState() {
        return artifactsUiState.getSoftwareModuleType().map(SoftwareModuleType::getId).orElse(null);
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final String filter) {
        final String searchText = getSearchTextFromUiState();
        final Long typeId = getSoftwareModuleTypeIdFromUiState();

        if (typeId == null && StringUtils.isEmpty(searchText)) {
            return softwareModuleManagement.count();
        } else {
            return softwareModuleManagement.countByTextAndType(searchText, typeId);
        }
    }

}
