/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.providers;

import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.common.data.mappers.TypeToProxyTypeMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Data provider for {@link SoftwareModuleType}, which dynamically loads a batch
 * of {@link SoftwareModuleType} entities from backend and maps them to
 * corresponding {@link ProxyType} entities.
 */
public class SoftwareModuleTypeDataProvider extends ProxyDataProvider<ProxyType, SoftwareModuleType, String> {

    private static final long serialVersionUID = 1L;

    private final transient SoftwareModuleTypeManagement softwareModuleTypeManagement;

    public SoftwareModuleTypeDataProvider(final SoftwareModuleTypeManagement softwareModuleTypeManagement,
            final TypeToProxyTypeMapper<SoftwareModuleType> mapper) {
        super(mapper, new Sort(Direction.ASC, "name"));
        this.softwareModuleTypeManagement = softwareModuleTypeManagement;
    }

    @Override
    protected Slice<SoftwareModuleType> loadBackendEntities(final PageRequest pageRequest, final String filter) {
        return softwareModuleTypeManagement.findAll(pageRequest);
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final String filter) {
        return softwareModuleTypeManagement.count();
    }

}
