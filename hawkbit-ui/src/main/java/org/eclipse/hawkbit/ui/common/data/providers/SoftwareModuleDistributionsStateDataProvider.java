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
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.ui.common.data.filters.SwFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.AssignedSoftwareModuleToProxyMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.util.StringUtils;

/**
 * Data provider for {@link AssignedSoftwareModule}, which dynamically loads a
 * batch of {@link AssignedSoftwareModule} entities from backend and maps them
 * to corresponding {@link ProxySoftwareModule} entities.
 */
public class SoftwareModuleDistributionsStateDataProvider
        extends ProxyDataProvider<ProxySoftwareModule, AssignedSoftwareModule, SwFilterParams> {

    private static final long serialVersionUID = 1L;

    private final transient SoftwareModuleManagement softwareModuleManagement;

    public SoftwareModuleDistributionsStateDataProvider(final SoftwareModuleManagement softwareModuleManagement,
            final AssignedSoftwareModuleToProxyMapper entityMapper) {
        super(entityMapper);

        this.softwareModuleManagement = softwareModuleManagement;
    }

    @Override
    protected Optional<Slice<AssignedSoftwareModule>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<SwFilterParams> filter) {
        if (!filter.isPresent()) {
            return Optional.of(mapToAssignedSoftwareModule(softwareModuleManagement.findAll(pageRequest)));
        }

        return filter.map(filterParams -> {
            final String searchText = filterParams.getSearchText();
            final Long typeId = filterParams.getSoftwareModuleTypeId();
            final Long selectedDsId = filterParams.getLastSelectedDistributionId();

            if (selectedDsId != null) {
                return softwareModuleManagement.findAllOrderBySetAssignmentAndModuleNameAscModuleVersionAsc(pageRequest,
                        selectedDsId, searchText, typeId);
            }

            if (typeId != null || !StringUtils.isEmpty(searchText)) {
                return mapToAssignedSoftwareModule(
                        softwareModuleManagement.findByTextAndType(pageRequest, searchText, typeId));
            }

            return mapToAssignedSoftwareModule(softwareModuleManagement.findAll(pageRequest));
        });
    }

    private Slice<AssignedSoftwareModule> mapToAssignedSoftwareModule(final Slice<SoftwareModule> smSlice) {
        return smSlice.map(sm -> new AssignedSoftwareModule(sm, false));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<SwFilterParams> filter) {
        return filter.map(filterParams -> {
            final String searchText = filterParams.getSearchText();
            final Long typeId = filterParams.getSoftwareModuleTypeId();

            if (typeId == null && StringUtils.isEmpty(searchText)) {
                return softwareModuleManagement.count();
            } else {
                return softwareModuleManagement.countByTextAndType(searchText, typeId);
            }
        }).orElse(softwareModuleManagement.count());
    }

}
