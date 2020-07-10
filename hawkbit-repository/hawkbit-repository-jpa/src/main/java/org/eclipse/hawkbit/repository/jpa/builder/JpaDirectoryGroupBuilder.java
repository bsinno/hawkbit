/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa.builder;

import javax.validation.constraints.NotEmpty;

import org.eclipse.hawkbit.repository.builder.DirectoryGroupBuilder;
import org.eclipse.hawkbit.repository.builder.DirectoryGroupCreate;
import org.eclipse.hawkbit.repository.builder.DirectoryGroupUpdate;
import org.eclipse.hawkbit.repository.model.DirectoryGroup;

/**
 * Builder implementation for {@link DirectoryGroup}.
 */
public class JpaDirectoryGroupBuilder implements DirectoryGroupBuilder {

    @Override
    public DirectoryGroupUpdate update(@NotEmpty final Long groupId) {
        return new JpaDirectoryGroupUpdate(groupId);
    }

    @Override
    public DirectoryGroupCreate create() {
        return new JpaDirectoryGroupCreate();
    }

}