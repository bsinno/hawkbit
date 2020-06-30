/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.builder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.hawkbit.repository.model.BaseEntity;
import org.eclipse.hawkbit.repository.model.DirectoryGroup;
import org.eclipse.hawkbit.repository.model.NamedEntity;

/**
 * Builder to create a new {@link DirectoryGroup} entry. Defines all fields that can be set
 * at creation time. Other fields are set by the repository automatically, e.g.
 * {@link BaseEntity#getCreatedAt()}.
 */
public interface DirectoryGroupCreate {
    /**
     * @param name for {@link DirectoryGroup#getName()}
     * @return updated builder instance
     */
    DirectoryGroupCreate name(@Size(min = 1, max = NamedEntity.NAME_MAX_SIZE) @NotNull String name);

    /**
     * @param directoryParent for {@link DirectoryGroup#getDirectoryParent()} ()}
     * @return updated builder instance
     */
    DirectoryGroupCreate directoryParent(DirectoryGroup directoryParent);

    /**
     * @return peek on current state of {@link DirectoryGroup} in the builder
     */
    DirectoryGroup build();
}