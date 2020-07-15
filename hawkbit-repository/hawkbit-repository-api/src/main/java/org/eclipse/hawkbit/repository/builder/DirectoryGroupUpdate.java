/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.builder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.hawkbit.repository.model.DirectoryGroup;
import org.eclipse.hawkbit.repository.model.NamedEntity;

/**
 * Builder to update an existing {@link DirectoryGroup} entry. Defines all
 * fields that can be updated.
 *
 */
public interface DirectoryGroupUpdate {
    /**
     * @param name
     *            for {@link DirectoryGroup#getName()}
     * @return updated builder instance
     */
    DirectoryGroupUpdate name(@Size(min = 1, max = NamedEntity.NAME_MAX_SIZE) @NotNull String name);

    /**
     * @param directoryParent
     *            for {@link DirectoryGroup#getDirectoryParent()} ()}
     * @return updated builder instance
     */
    DirectoryGroupUpdate directoryParent(DirectoryGroup directoryParent);
}
