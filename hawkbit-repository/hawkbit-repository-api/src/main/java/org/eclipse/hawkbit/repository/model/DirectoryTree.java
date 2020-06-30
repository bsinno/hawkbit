/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.model;

/**
 * {@link DirectoryTree} entry.
 *
 */
public interface DirectoryTree {

    /**
     * @return return ancestor of the directory tree node
     */
    public DirectoryGroup getAncestor();

    /**
     * set the ancestor of the directory tree node
     *
     * @param ancestor the ancestor node
     */
    public void setAncestor(DirectoryGroup ancestor);

    /**
     * @return return descendant of the directory tree node
     */
    public DirectoryGroup getDescendant();

    /**
     * set the descendant of the directory tree node
     *
     * @param descendant the descendant node
     */
    public void setDescendant(DirectoryGroup descendant);

    /**
     * @return return the depth of the directory tree node
     */
    public int getDepth();

    /**
     * set the depth of the directory tree node
     *
     * @param depth the depth of the tree path
     */
    public void setDepth(int depth);

}
