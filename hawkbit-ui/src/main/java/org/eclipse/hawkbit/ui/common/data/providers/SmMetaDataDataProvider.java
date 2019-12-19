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
import org.eclipse.hawkbit.repository.model.SoftwareModuleMetadata;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Data provider for {@link SoftwareModuleMetadata}, which dynamically loads a
 * batch of {@link SoftwareModuleMetadata} entities from backend and maps them
 * to corresponding {@link ProxyMetaData} entities.
 */
public class SmMetaDataDataProvider extends AbstractMetaDataDataProvider<SoftwareModuleMetadata, Long> {
    private static final long serialVersionUID = 1L;

    private final transient SoftwareModuleManagement softwareModuleManagement;

    public SmMetaDataDataProvider(final SoftwareModuleManagement softwareModuleManagement) {
        this.softwareModuleManagement = softwareModuleManagement;
    }

    @Override
    protected Optional<Page<SoftwareModuleMetadata>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<Long> currentlySelectedSmId) {
        return currentlySelectedSmId
                .map(id -> softwareModuleManagement.findMetaDataBySoftwareModuleId(pageRequest, id));
    }

    @Override
    protected ProxyMetaData createProxyMetaData(final SoftwareModuleMetadata smMetadata) {
        final ProxyMetaData proxyMetaData = super.createProxyMetaData(smMetadata);
        proxyMetaData.setTargetVisible(smMetadata.isTargetVisible());

        return proxyMetaData;
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<Long> currentlySelectedSmId) {
        return currentlySelectedSmId
                .map(id -> softwareModuleManagement.findMetaDataBySoftwareModuleId(pageRequest, id).getTotalElements())
                .orElse(0L);
    }
}
