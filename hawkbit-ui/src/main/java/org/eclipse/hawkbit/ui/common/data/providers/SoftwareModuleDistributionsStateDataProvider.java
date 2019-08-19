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
import org.eclipse.hawkbit.ui.common.data.filters.SwDistributionsFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.AssignedSoftwareModuleToProxyMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAssignedSoftwareModule;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.util.StringUtils;

/**
 * Data provider for {@link AssignedSoftwareModule}, which dynamically loads a
 * batch of {@link AssignedSoftwareModule} entities from backend and maps them
 * to corresponding {@link ProxyAssignedSoftwareModule} entities.
 */
public class SoftwareModuleDistributionsStateDataProvider
        extends ProxyDataProvider<ProxyAssignedSoftwareModule, AssignedSoftwareModule, SwDistributionsFilterParams> {

    private static final long serialVersionUID = 1L;

    private final transient SoftwareModuleManagement softwareModuleManagement;

    public SoftwareModuleDistributionsStateDataProvider(final SoftwareModuleManagement softwareModuleManagement,
            final AssignedSoftwareModuleToProxyMapper entityMapper) {
        super(entityMapper);

        this.softwareModuleManagement = softwareModuleManagement;
    }

    @Override
    protected Optional<Slice<AssignedSoftwareModule>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<SwDistributionsFilterParams> filter) {
        return filter.map(
                filterParams -> softwareModuleManagement.findAllOrderBySetAssignmentAndModuleNameAscModuleVersionAsc(
                        pageRequest, filterParams.getLastSelectedDistributionId(), filterParams.getSearchText(),
                        filterParams.getSoftwareModuleTypeId()));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<SwDistributionsFilterParams> filter) {
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
