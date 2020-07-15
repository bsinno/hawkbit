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
import org.eclipse.hawkbit.repository.builder.DirectoryGroupUpdate;

/**
 * Update implementation for DirectoryGroups.
 */
public class JpaDirectoryGroupUpdate extends AbstractDirectoryGroupUpdateCreate<DirectoryGroupUpdate>
        implements DirectoryGroupUpdate {
    public JpaDirectoryGroupUpdate(final Long id) {
        super.id = id;
    }
}