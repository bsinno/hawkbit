/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.model;

import java.util.Collection;

/**
 * {@link DirectoryGroup} entry.
 *
 */
public interface DirectoryGroup extends NamedEntity {

    /**
     * @return direct parent of this group within the directory path
     */
    DirectoryGroup getDirectoryParent();

    /**
     * @return children this group is parent of
     */
    Collection<DirectoryGroup> getDirectoryChildren();

    /**
     * @return all ancestors tree entries of this group
     */
    Collection<DirectoryTree> getAncestorTree();

    /**
     * @return all descendant tree entries of this group
     */
    Collection<DirectoryTree> getDescendantTree();


}
