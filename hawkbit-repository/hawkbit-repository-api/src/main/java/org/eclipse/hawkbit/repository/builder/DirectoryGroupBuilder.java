/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.builder;

import javax.validation.constraints.NotEmpty;

import org.eclipse.hawkbit.repository.model.DirectoryGroup;

/**
 * Builder for {@link DirectoryGroup}.
 */
public interface DirectoryGroupBuilder {

    /**
     * @param groupId of the updatable entity
     * @return builder instance
     */
    DirectoryGroupUpdate update(@NotEmpty Long groupId);

    /**
     * @return builder instance
     */
    DirectoryGroupCreate create();
}