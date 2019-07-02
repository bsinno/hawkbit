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
import org.eclipse.hawkbit.ui.common.data.mappers.ArtifactToProxyArtifactMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyArtifact;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Data provider for {@link Artifact}, which dynamically loads a batch of
 * {@link Artifact} entities from backend and maps them to corresponding
 * {@link ProxyArtifact} entities.
 */
public class ArtifactDataProvider extends ProxyDataProvider<ProxyArtifact, Artifact, String> {

    private static final long serialVersionUID = 1L;

    private final transient ArtifactManagement artifactManagement;
    private final Long selectedSwModuleId;

    public ArtifactDataProvider(final ArtifactManagement artifactManagement, final Long selectedSwModuleId,
            final ArtifactToProxyArtifactMapper entityMapper) {
        super(entityMapper, new Sort(Direction.DESC, "filename"));

        this.artifactManagement = artifactManagement;
        this.selectedSwModuleId = selectedSwModuleId;
    }

    @Override
    protected Optional<Slice<Artifact>> loadBackendEntities(final PageRequest pageRequest, final String filter) {
        return selectedSwModuleId != null
                ? Optional.of(artifactManagement.findBySoftwareModule(pageRequest, selectedSwModuleId))
                : Optional.empty();
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final String filter) {
        return selectedSwModuleId != null
                ? artifactManagement.findBySoftwareModule(pageRequest, selectedSwModuleId).getTotalElements()
                : 0L;
    }
}
