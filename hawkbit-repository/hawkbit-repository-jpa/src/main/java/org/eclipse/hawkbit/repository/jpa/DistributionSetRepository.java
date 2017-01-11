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

import org.eclipse.hawkbit.repository.jpa.model.JpaDistributionSet;
import org.eclipse.hawkbit.repository.jpa.model.JpaDistributionSetTag;
import org.eclipse.hawkbit.repository.jpa.model.JpaSoftwareModule;
import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.repository.model.Tag;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link DistributionSet} repository.
 *
 */
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
public interface DistributionSetRepository
        extends BaseEntityRepository<JpaDistributionSet, Long>, JpaSpecificationExecutor<JpaDistributionSet> {

    /**
     * Finds {@link DistributionSet}s by assigned {@link Tag}.
     *
     * @param tag
     *            to be found
     * @return list of found {@link DistributionSet}s
     */
    @Query(value = "Select Distinct ds from JpaDistributionSet ds join ds.tags dst where dst = :tag")
    List<JpaDistributionSet> findByTag(@Param("tag") final JpaDistributionSetTag tag);

    /**
     * deletes the {@link DistributionSet}s with the given IDs.
     * 
     * @param ids
     *            to be deleted
     */
    @Modifying
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Query("update JpaDistributionSet d set d.deleted = 1 where d.id in :ids")
    void deleteDistributionSet(@Param("ids") Long... ids);

    /**
     * deletes {@link DistributionSet}s by the given IDs.
     *
     * @param ids
     *            List of IDs of {@link DistributionSet}s to be deleted
     * @return number of affected/deleted records
     */
    @Modifying
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    // Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=349477
    @Query("DELETE FROM JpaDistributionSet d WHERE d.id IN ?1")
    int deleteByIdIn(Collection<Long> ids);

    /**
     * Finds {@link DistributionSet}s where given {@link SoftwareModule} is
     * assigned.
     *
     * @param module
     *            to search for
     * @return {@link List} of found {@link DistributionSet}s
     */
    Long countByModules(JpaSoftwareModule module);

    /**
     * Finds {@link DistributionSet}s based on given ID that are assigned yet to
     * an {@link Action}, i.e. in use.
     *
     * @param ids
     *            to search for
     * @return list of {@link DistributionSet#getId()}
     */
    @Query("select ac.distributionSet.id from JpaAction ac where ac.distributionSet.id in :ids")
    List<Long> findAssignedToTargetDistributionSetsById(@Param("ids") Collection<Long> ids);

    /**
     * Finds {@link DistributionSet}s based on given ID that are assigned yet to
     * an {@link Rollout}, i.e. in use.
     *
     * @param ids
     *            to search for
     * @return list of {@link DistributionSet#getId()}
     */
    @Query("select ra.distributionSet.id from JpaRollout ra where ra.distributionSet.id in :ids")
    List<Long> findAssignedToRolloutDistributionSetsById(@Param("ids") Collection<Long> ids);

    /**
     * Finds the distribution set for a specific action.
     * 
     * @param action
     *            the action associated with the distribution set to find
     * @return the distribution set associated with the given action
     */
    @Query("select DISTINCT d from JpaDistributionSet d join fetch d.modules m join d.actions a where a.id = :action")
    JpaDistributionSet findByActionId(@Param("action") Long action);

    /**
     * Counts {@link DistributionSet} instances of given type in the repository.
     *
     * @param typeId
     *            to search for
     * @return number of found {@link DistributionSet}s
     */
    long countByTypeId(Long typeId);

    /**
     * Counts {@link DistributionSet} with given
     * {@link DistributionSet#getName()} and
     * {@link DistributionSet#getVersion()}.
     *
     * @param name
     *            to search for
     * @param version
     *            to search for
     * @return number of found {@link DistributionSet}s
     */
    long countByNameAndVersion(String name, String version);
}
