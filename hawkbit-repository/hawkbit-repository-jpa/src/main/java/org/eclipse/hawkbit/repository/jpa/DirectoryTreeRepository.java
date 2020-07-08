/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa;

import org.eclipse.hawkbit.repository.jpa.model.DirectoryTreeId;
import org.eclipse.hawkbit.repository.jpa.model.JpaDirectoryGroup;
import org.eclipse.hawkbit.repository.jpa.model.JpaDirectoryTree;
import org.eclipse.hawkbit.repository.model.DirectoryGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link JpaDirectoryTree} repository.
 */
@Transactional(readOnly = true)
public interface DirectoryTreeRepository
        extends JpaRepository<JpaDirectoryTree, DirectoryTreeId>, JpaSpecificationExecutor<JpaDirectoryTree> {

    /**
     * Checks if group with given ancestor exists.
     *
     * @param ancestorId to check for
     * @return <code>true</code> is group with given ancestor exists
     */
    @Query("SELECT CASE WHEN COUNT(t)>0 THEN 'true' ELSE 'false' END FROM JpaDirectoryTree t WHERE t.ancestor=:ancestorId")
    boolean existsByAncestorId(@Param("ancestorId") JpaDirectoryGroup ancestorId);

    /**
     * Checks if group with given ancestor exists.
     *
     * @param descendantId to check for
     * @return <code>true</code> is group with given ancestor exists
     */
    @Query("SELECT CASE WHEN COUNT(t)>0 THEN 'true' ELSE 'false' END FROM JpaDirectoryTree t WHERE t.descendant=:descendantId")
    boolean existsByDescendantId(@Param("descendantId") JpaDirectoryGroup descendantId);

    boolean existsByAncestorAndDescendantAndDepth(DirectoryGroup ancestor, DirectoryGroup descendant, int depth);

}
