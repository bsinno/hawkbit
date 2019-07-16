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

import org.eclipse.hawkbit.repository.ArtifactManagement;
import org.eclipse.hawkbit.repository.model.Artifact;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.ui.common.data.mappers.ArtifactToProxyArtifactMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyArtifact;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Data provider for {@link Artifact}, which dynamically loads a batch of
 * {@link Artifact} entities from backend and maps them to corresponding
 * {@link ProxyArtifact} entities.The filter is used for master-details
 * relationship with {@link SoftwareModule}, using its id.
 */
public class ArtifactDataProvider extends ProxyDataProvider<ProxyArtifact, Artifact, Long> {

    private static final long serialVersionUID = 1L;

    private final transient ArtifactManagement artifactManagement;

    public ArtifactDataProvider(final ArtifactManagement artifactManagement,
            final ArtifactToProxyArtifactMapper entityMapper) {
        super(entityMapper, new Sort(Direction.DESC, "filename"));

        this.artifactManagement = artifactManagement;
    }

    @Override
    protected Optional<Slice<Artifact>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<Long> filter) {
        return filter
                .map(selectedSwModuleId -> artifactManagement.findBySoftwareModule(pageRequest, selectedSwModuleId));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<Long> filter) {
        return filter.map(selectedSwModuleId -> artifactManagement.findBySoftwareModule(pageRequest, selectedSwModuleId)
                .getTotalElements()).orElse(0L);
    }
}
