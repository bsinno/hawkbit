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
import org.eclipse.hawkbit.ui.common.data.filters.SwFilterParams;
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
        extends ProxyDataProvider<ProxySoftwareModule, SoftwareModule, SwFilterParams> {

    private static final long serialVersionUID = 1L;

    private final transient SoftwareModuleManagement softwareModuleManagement;

    public SoftwareModuleArtifactsStateDataProvider(final SoftwareModuleManagement softwareModuleManagement,
            final SoftwareModuleToProxyMapper entityMapper) {
        super(entityMapper, new Sort(Direction.ASC, "name", "version"));

        this.softwareModuleManagement = softwareModuleManagement;
    }

    @Override
    protected Optional<Slice<SoftwareModule>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<SwFilterParams> filter) {
        if (!filter.isPresent()) {
            return Optional.of(softwareModuleManagement.findAll(pageRequest));
        }

        return filter.map(filterParams -> {
            final String searchText = filterParams.getSearchText();
            final Long typeId = filterParams.getSoftwareModuleTypeId();

            if (typeId != null || !StringUtils.isEmpty(searchText)) {
                return softwareModuleManagement.findByTextAndType(pageRequest, searchText, typeId);
            }

            return softwareModuleManagement.findAll(pageRequest);
        });
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<SwFilterParams> filter) {
        return filter.map(filterParams -> {
            final Long typeId = filterParams.getSoftwareModuleTypeId();
            final String searchText = filterParams.getSearchText();

            if (typeId == null && StringUtils.isEmpty(searchText)) {
                return softwareModuleManagement.count();
            } else {
                return softwareModuleManagement.countByTextAndType(searchText, typeId);
            }
        }).orElse(softwareModuleManagement.count());
    }

}
