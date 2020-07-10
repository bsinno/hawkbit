/**
 * Copyright (c) 2020 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa.model;

import java.io.Serializable;
import java.util.Objects;

import org.eclipse.hawkbit.repository.model.DirectoryGroup;
import org.eclipse.hawkbit.repository.model.DirectoryTree;

/**
 * Combined unique key of the table {@link DirectoryTree}.
 */
public class DirectoryTreeId implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ancestor;
    private Long descendant;

    /**
     * default constructor necessary for JPA.
     */
    public DirectoryTreeId() {
        // default constructor necessary for JPA, empty.
    }

    public DirectoryTreeId(final DirectoryGroup ancestor, final DirectoryGroup descendant) {
        this.ancestor = ancestor.getId();
        this.descendant = descendant.getId();
    }

    public DirectoryTreeId(final Long ancestor, final Long descendant) {
        this.ancestor = ancestor;
        this.descendant = descendant;
    }

    public Long getAncestor() {
        return ancestor;
    }

    public Long getDescendant() {
        return descendant;
    }

    public void setAncestor(final Long ancestor) {
        this.ancestor = ancestor;
    }

    public void setDescendant(final Long descendant) {
        this.descendant = descendant;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DirectoryTreeId that = (DirectoryTreeId) o;
        return ancestor.equals(that.ancestor) &&
                descendant.equals(that.descendant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ancestor, descendant);
    }
}
