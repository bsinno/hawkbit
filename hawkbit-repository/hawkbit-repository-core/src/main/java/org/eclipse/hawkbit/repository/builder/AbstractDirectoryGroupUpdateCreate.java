/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.builder;

import java.util.Optional;

import org.eclipse.hawkbit.repository.model.DirectoryGroup;

/**
 * Create and update builder DTO.
 *
 * @param <T> update or create builder interface
 */
public class AbstractDirectoryGroupUpdateCreate<T> extends AbstractNamedEntityBuilder<T> {

    protected DirectoryGroup directoryParent;

    public T directoryParent(final DirectoryGroup directoryParent) {
        this.directoryParent = directoryParent;
        return (T) this;
    }

    public Optional<DirectoryGroup> getDirectoryParent() {
        return Optional.ofNullable(directoryParent);
    }

}