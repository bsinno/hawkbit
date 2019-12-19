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

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.TargetMetadata;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Data provider for {@link TargetMetadata}, which dynamically loads a batch of
 * {@link TargetMetadata} entities from backend and maps them to corresponding
 * {@link ProxyMetaData} entities.
 */
public class TargetMetaDataDataProvider extends AbstractMetaDataDataProvider<TargetMetadata, String> {
    private static final long serialVersionUID = 1L;

    private final transient TargetManagement targetManagement;

    public TargetMetaDataDataProvider(final TargetManagement targetManagement) {
        this.targetManagement = targetManagement;
    }

    @Override
    protected Optional<Page<TargetMetadata>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<String> currentlySelectedControllerId) {
        return currentlySelectedControllerId
                .map(controllerId -> targetManagement.findMetaDataByControllerId(pageRequest, controllerId));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<String> currentlySelectedControllerId) {
        return currentlySelectedControllerId.map(controllerId -> targetManagement
                .findMetaDataByControllerId(pageRequest, controllerId).getTotalElements()).orElse(0L);
    }
}
