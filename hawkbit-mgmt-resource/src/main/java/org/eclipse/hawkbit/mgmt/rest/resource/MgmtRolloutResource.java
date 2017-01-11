/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.mgmt.rest.resource;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.mgmt.json.model.PagedList;
import org.eclipse.hawkbit.mgmt.json.model.rollout.MgmtRolloutResponseBody;
import org.eclipse.hawkbit.mgmt.json.model.rollout.MgmtRolloutRestRequestBody;
import org.eclipse.hawkbit.mgmt.json.model.rolloutgroup.MgmtRolloutGroupResponseBody;
import org.eclipse.hawkbit.mgmt.json.model.target.MgmtTarget;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtRestConstants;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtRolloutRestApi;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.OffsetBasedPageRequest;
import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.builder.RolloutCreate;
import org.eclipse.hawkbit.repository.builder.RolloutGroupCreate;
import org.eclipse.hawkbit.repository.exception.ConstraintViolationException;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditions;
import org.eclipse.hawkbit.repository.model.Target;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Resource handling rollout CRUD operations.
 *
 */
@RestController
public class MgmtRolloutResource implements MgmtRolloutRestApi {

    private static final String DOES_NOT_EXIST = "} does not exist";

    @Autowired
    private RolloutManagement rolloutManagement;

    @Autowired
    private RolloutGroupManagement rolloutGroupManagement;

    @Autowired
    private DistributionSetManagement distributionSetManagement;

    @Autowired
    private TargetFilterQueryManagement targetFilterQueryManagement;

    @Autowired
    private EntityFactory entityFactory;

    @Override
    public ResponseEntity<PagedList<MgmtRolloutResponseBody>> getRollouts(
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_OFFSET, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_OFFSET) final int pagingOffsetParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_LIMIT, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_LIMIT) final int pagingLimitParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SORTING, required = false) final String sortParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SEARCH, required = false) final String rsqlParam) {

        final int sanitizedOffsetParam = PagingUtility.sanitizeOffsetParam(pagingOffsetParam);
        final int sanitizedLimitParam = PagingUtility.sanitizePageLimitParam(pagingLimitParam);
        final Sort sorting = PagingUtility.sanitizeRolloutSortParam(sortParam);

        final Pageable pageable = new OffsetBasedPageRequest(sanitizedOffsetParam, sanitizedLimitParam, sorting);

        final Page<Rollout> findModulesAll;
        if (rsqlParam != null) {
            findModulesAll = this.rolloutManagement.findAllByPredicate(rsqlParam, pageable);
        } else {
            findModulesAll = this.rolloutManagement.findAll(pageable);
        }

        final List<MgmtRolloutResponseBody> rest = MgmtRolloutMapper.toResponseRollout(findModulesAll.getContent());
        return new ResponseEntity<>(new PagedList<>(rest, findModulesAll.getTotalElements()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<MgmtRolloutResponseBody> getRollout(@PathVariable("rolloutId") final Long rolloutId) {
        final Rollout findRolloutById = findRolloutOrThrowException(rolloutId);
        return new ResponseEntity<>(MgmtRolloutMapper.toResponseRollout(findRolloutById, true), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<MgmtRolloutResponseBody> create(
            @RequestBody final MgmtRolloutRestRequestBody rolloutRequestBody) {

        // first check the given RSQL query if it's well formed, otherwise and
        // exception is thrown
        targetFilterQueryManagement.verifyTargetFilterQuerySyntax(rolloutRequestBody.getTargetFilterQuery());

        final DistributionSet distributionSet = findDistributionSetOrThrowException(rolloutRequestBody);
        final RolloutGroupConditions rolloutGroupConditions = MgmtRolloutMapper.fromRequest(rolloutRequestBody, true);

        final RolloutCreate create = MgmtRolloutMapper.fromRequest(entityFactory, rolloutRequestBody, distributionSet);

        Rollout rollout;
        if (rolloutRequestBody.getGroups() != null) {
            final List<RolloutGroupCreate> rolloutGroups = rolloutRequestBody.getGroups().stream()
                    .map(mgmtRolloutGroup -> MgmtRolloutMapper.fromRequest(entityFactory, mgmtRolloutGroup))
                    .collect(Collectors.toList());
            rollout = rolloutManagement.createRollout(create, rolloutGroups, rolloutGroupConditions);

        } else if (rolloutRequestBody.getAmountGroups() != null) {
            rollout = rolloutManagement.createRollout(create, rolloutRequestBody.getAmountGroups(),
                    rolloutGroupConditions);

        } else {
            throw new ConstraintViolationException("Either 'amountGroups' or 'groups' must be defined in the request");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(MgmtRolloutMapper.toResponseRollout(rollout, true));
    }

    @Override
    public ResponseEntity<Void> start(@PathVariable("rolloutId") final Long rolloutId) {
        this.rolloutManagement.startRollout(rolloutId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> pause(@PathVariable("rolloutId") final Long rolloutId) {
        this.rolloutManagement.pauseRollout(rolloutId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> resume(@PathVariable("rolloutId") final Long rolloutId) {
        this.rolloutManagement.resumeRollout(rolloutId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<PagedList<MgmtRolloutGroupResponseBody>> getRolloutGroups(
            @PathVariable("rolloutId") final Long rolloutId,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_OFFSET, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_OFFSET) final int pagingOffsetParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_LIMIT, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_LIMIT) final int pagingLimitParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SORTING, required = false) final String sortParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SEARCH, required = false) final String rsqlParam) {
        final int sanitizedOffsetParam = PagingUtility.sanitizeOffsetParam(pagingOffsetParam);
        final int sanitizedLimitParam = PagingUtility.sanitizePageLimitParam(pagingLimitParam);
        final Sort sorting = PagingUtility.sanitizeRolloutGroupSortParam(sortParam);

        final Pageable pageable = new OffsetBasedPageRequest(sanitizedOffsetParam, sanitizedLimitParam, sorting);

        final Page<RolloutGroup> findRolloutGroupsAll;
        if (rsqlParam != null) {
            findRolloutGroupsAll = this.rolloutGroupManagement.findRolloutGroupsAll(rolloutId, rsqlParam, pageable);
        } else {
            findRolloutGroupsAll = this.rolloutGroupManagement.findRolloutGroupsByRolloutId(rolloutId, pageable);
        }

        final List<MgmtRolloutGroupResponseBody> rest = MgmtRolloutMapper
                .toResponseRolloutGroup(findRolloutGroupsAll.getContent());
        return new ResponseEntity<>(new PagedList<>(rest, findRolloutGroupsAll.getTotalElements()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<MgmtRolloutGroupResponseBody> getRolloutGroup(@PathVariable("rolloutId") final Long rolloutId,
            @PathVariable("groupId") final Long groupId) {
        findRolloutOrThrowException(rolloutId);
        final RolloutGroup rolloutGroup = findRolloutGroupOrThrowException(groupId);
        return ResponseEntity.ok(MgmtRolloutMapper.toResponseRolloutGroup(rolloutGroup, true));
    }

    @Override
    public ResponseEntity<PagedList<MgmtTarget>> getRolloutGroupTargets(@PathVariable("rolloutId") final Long rolloutId,
            @PathVariable("groupId") final Long groupId,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_OFFSET, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_OFFSET) final int pagingOffsetParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_LIMIT, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_LIMIT) final int pagingLimitParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SORTING, required = false) final String sortParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SEARCH, required = false) final String rsqlParam) {
        findRolloutOrThrowException(rolloutId);
        final int sanitizedOffsetParam = PagingUtility.sanitizeOffsetParam(pagingOffsetParam);
        final int sanitizedLimitParam = PagingUtility.sanitizePageLimitParam(pagingLimitParam);
        final Sort sorting = PagingUtility.sanitizeTargetSortParam(sortParam);

        final Pageable pageable = new OffsetBasedPageRequest(sanitizedOffsetParam, sanitizedLimitParam, sorting);

        final Page<Target> rolloutGroupTargets;
        if (rsqlParam != null) {
            rolloutGroupTargets = this.rolloutGroupManagement.findRolloutGroupTargets(groupId, rsqlParam, pageable);
        } else {
            final Page<Target> pageTargets = this.rolloutGroupManagement.findRolloutGroupTargets(groupId, pageable);
            rolloutGroupTargets = pageTargets;
        }
        final List<MgmtTarget> rest = MgmtTargetMapper.toResponse(rolloutGroupTargets.getContent());
        return new ResponseEntity<>(new PagedList<>(rest, rolloutGroupTargets.getTotalElements()), HttpStatus.OK);
    }

    private Rollout findRolloutOrThrowException(final Long rolloutId) {
        final Rollout rollout = this.rolloutManagement.findRolloutWithDetailedStatus(rolloutId);
        if (rollout == null) {
            throw new EntityNotFoundException("Rollout with Id {" + rolloutId + DOES_NOT_EXIST);
        }
        return rollout;
    }

    private RolloutGroup findRolloutGroupOrThrowException(final Long rolloutGroupId) {
        final RolloutGroup rolloutGroup = this.rolloutGroupManagement
                .findRolloutGroupWithDetailedStatus(rolloutGroupId);
        if (rolloutGroup == null) {
            throw new EntityNotFoundException("Group with Id {" + rolloutGroupId + DOES_NOT_EXIST);
        }
        return rolloutGroup;
    }

    private DistributionSet findDistributionSetOrThrowException(final MgmtRolloutRestRequestBody rolloutRequestBody) {
        final DistributionSet ds = this.distributionSetManagement
                .findDistributionSetById(rolloutRequestBody.getDistributionSetId());
        if (ds == null) {
            throw new EntityNotFoundException(
                    "DistributionSet with Id {" + rolloutRequestBody.getDistributionSetId() + DOES_NOT_EXIST);
        }
        return ds;
    }
}
