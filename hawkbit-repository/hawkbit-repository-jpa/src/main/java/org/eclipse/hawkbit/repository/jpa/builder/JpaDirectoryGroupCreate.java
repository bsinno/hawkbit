/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa.builder;

import org.eclipse.hawkbit.repository.builder.AbstractDirectoryGroupUpdateCreate;
import org.eclipse.hawkbit.repository.builder.DirectoryGroupCreate;
import org.eclipse.hawkbit.repository.jpa.model.JpaDirectoryGroup;
import org.eclipse.hawkbit.repository.model.DirectoryGroup;

/**
 * Create/build implementation for DirectoryGroup.
 */
public class JpaDirectoryGroupCreate extends AbstractDirectoryGroupUpdateCreate<DirectoryGroupCreate>
        implements DirectoryGroupCreate {
    JpaDirectoryGroupCreate() {

    }

    public JpaDirectoryGroup buildGroup() {
        return new JpaDirectoryGroup(name, description, directoryParent);
    }

    @Override
    public DirectoryGroup build() {
        return new JpaDirectoryGroup(name, description, directoryParent);
    }
}
