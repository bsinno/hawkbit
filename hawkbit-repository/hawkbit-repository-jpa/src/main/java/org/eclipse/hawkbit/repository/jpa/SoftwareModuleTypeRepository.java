/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.eclipse.hawkbit.repository.jpa.model.JpaSoftwareModuleType;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for {@link SoftwareModuleType}.
 *
 */
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
public interface SoftwareModuleTypeRepository
        extends BaseEntityRepository<JpaSoftwareModuleType, Long>, JpaSpecificationExecutor<JpaSoftwareModuleType> {

    /**
     * @param pageable
     * @param isDeleted
     *            to <code>true</code> if only marked as deleted have to be
     *            count or all undeleted.
     * @return found {@link SoftwareModuleType}s.
     */
    Page<SoftwareModuleType> findByDeleted(Pageable pageable, boolean isDeleted);

    /**
     * @param isDeleted
     *            to <code>true</code> if only marked as deleted have to be
     *            count or all undeleted.
     * @return number of {@link SoftwareModuleType}s in the repository.
     */
    Long countByDeleted(boolean isDeleted);

    /**
     *
     * @param key
     *            to search for
     * @return all {@link SoftwareModuleType}s in the repository with given
     *         {@link SoftwareModuleType#getKey()}
     */
    Optional<SoftwareModuleType> findByKey(String key);

    /**
     *
     * @param name
     *            to search for
     * @return all {@link SoftwareModuleType}s in the repository with given
     *         {@link SoftwareModuleType#getName()}
     */
    Optional<SoftwareModuleType> findByName(String name);

    /**
     * retrieves all software module types with a given
     * {@link SoftwareModuleType#getId()}.
     *
     * @param ids
     *            to search for
     * @return {@link List} of found {@link SoftwareModule}s
     */
    // Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=349477
    @Query("SELECT sm FROM JpaSoftwareModuleType sm WHERE sm.id IN ?1")
    List<JpaSoftwareModuleType> findByIdIn(Iterable<Long> ids);
}
