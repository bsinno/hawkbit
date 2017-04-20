/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.hawkbit.repository.jpa.model.JpaRollout;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.Rollout.RolloutStatus;
import org.eclipse.hawkbit.repository.model.TenantAwareBaseEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * The repository interface for the {@link Rollout} model.
 */
@Transactional(readOnly = true)
public interface RolloutRepository
        extends BaseEntityRepository<JpaRollout, Long>, JpaSpecificationExecutor<JpaRollout> {

    /**
     * Retrieves all {@link Rollout} for given status.
     * 
     * @param status
     *            the status of the rollouts to find
     * @return the list of {@link Rollout} for specific status
     */
    // Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=349477
    @Query("SELECT sm.id FROM JpaRollout sm WHERE sm.status IN ?1")
    List<Long> findByStatusIn(Collection<RolloutStatus> status);

    /**
     * Retrieves all {@link Rollout} for a specific {@code name}
     * 
     * @param name
     *            the rollout name
     * @return {@link Rollout} for specific name
     */
    Optional<Rollout> findByName(String name);

    /**
     * Deletes all {@link TenantAwareBaseEntity} of a given tenant.
     *
     * @param tenant
     *            to delete data from
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM JpaRollout t WHERE UPPER(t.tenant) = UPPER(:tenant)")
    void deleteByTenantIgnoreCase(@Param("tenant") String tenant);
}
