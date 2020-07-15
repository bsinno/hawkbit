/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.eclipse.hawkbit.repository.model.DirectoryGroup;
import org.eclipse.hawkbit.repository.model.DirectoryTree;

/**
 * JpaDirectoryTree entries reflect a closure table for the DirectoryTree
 * hierarchy
 */
@IdClass(DirectoryTreeId.class)
@Entity
@Table(name = "sp_directory_tree", uniqueConstraints = @UniqueConstraint(columnNames = { "ancestor", "descendant",
        "tenant" }, name = "uk_directory_tree"))
// exception squid:S2160 - BaseEntity equals/hashcode is handling correctly for
// sub entities
@SuppressWarnings("squid:S2160")
public class JpaDirectoryTree implements DirectoryTree {
    private static final long serialVersionUID = 1L;

    @Id
    @JoinColumn(name = "ancestor", nullable = false, updatable = false)
    @ManyToOne(targetEntity = JpaDirectoryGroup.class)
    private DirectoryGroup ancestor;

    @Id
    @JoinColumn(name = "descendant", nullable = false, updatable = false)
    @ManyToOne(targetEntity = JpaDirectoryGroup.class)
    private DirectoryGroup descendant;

    @Column(nullable = false)
    private int depth;

    /**
     * Public constructor.
     *
     * @param ancestor
     *            the ancestor {@link DirectoryGroup}
     * @param descendant
     *            the descendant {@link DirectoryGroup}
     * @param depth
     *            depth of the group relation
     */
    public JpaDirectoryTree(final DirectoryGroup ancestor, final DirectoryGroup descendant, final int depth) {
        this.ancestor = ancestor;
        this.descendant = descendant;
        this.depth = depth;
    }

    public JpaDirectoryTree() {
    }

    @Override
    public DirectoryGroup getAncestor() {
        return ancestor;
    }

    @Override
    public void setAncestor(final DirectoryGroup ancestor) {
        this.ancestor = ancestor;
    }

    @Override
    public DirectoryGroup getDescendant() {
        return descendant;
    }

    @Override
    public void setDescendant(final DirectoryGroup descendant) {
        this.descendant = descendant;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public void setDepth(final int depth) {
        this.depth = depth;
    }

}