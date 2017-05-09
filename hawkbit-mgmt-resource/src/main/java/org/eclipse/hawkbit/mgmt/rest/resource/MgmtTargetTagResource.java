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
import org.eclipse.hawkbit.mgmt.json.model.tag.MgmtAssignedTargetRequestBody;
import org.eclipse.hawkbit.mgmt.json.model.tag.MgmtTag;
import org.eclipse.hawkbit.mgmt.json.model.tag.MgmtTagRequestBodyPut;
import org.eclipse.hawkbit.mgmt.json.model.tag.MgmtTargetTagAssigmentResult;
import org.eclipse.hawkbit.mgmt.json.model.target.MgmtTarget;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtRestConstants;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtTargetTagRestApi;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.OffsetBasedPageRequest;
import org.eclipse.hawkbit.repository.TagManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetTag;
import org.eclipse.hawkbit.repository.model.TargetTagAssignmentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Resource handling for tag CRUD operations.
 *
 */
@RestController
public class MgmtTargetTagResource implements MgmtTargetTagRestApi {
    private static final Logger LOG = LoggerFactory.getLogger(MgmtTargetTagResource.class);

    @Autowired
    private TagManagement tagManagement;

    @Autowired
    private TargetManagement targetManagement;

    @Autowired
    private EntityFactory entityFactory;

    @Override
    public ResponseEntity<PagedList<MgmtTag>> getTargetTags(
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_OFFSET, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_OFFSET) final int pagingOffsetParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_LIMIT, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_LIMIT) final int pagingLimitParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SORTING, required = false) final String sortParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SEARCH, required = false) final String rsqlParam) {

        final int sanitizedOffsetParam = PagingUtility.sanitizeOffsetParam(pagingOffsetParam);
        final int sanitizedLimitParam = PagingUtility.sanitizePageLimitParam(pagingLimitParam);
        final Sort sorting = PagingUtility.sanitizeTagSortParam(sortParam);

        final Pageable pageable = new OffsetBasedPageRequest(sanitizedOffsetParam, sanitizedLimitParam, sorting);
        Page<TargetTag> findTargetsAll;
        if (rsqlParam == null) {
            findTargetsAll = this.tagManagement.findAllTargetTags(pageable);

        } else {
            findTargetsAll = this.tagManagement.findAllTargetTags(rsqlParam, pageable);
        }

        final List<MgmtTag> rest = MgmtTagMapper.toResponse(findTargetsAll.getContent());
        return new ResponseEntity<>(new PagedList<>(rest, findTargetsAll.getTotalElements()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<MgmtTag> getTargetTag(@PathVariable("targetTagId") final Long targetTagId) {
        final TargetTag tag = findTargetTagById(targetTagId);
        return new ResponseEntity<>(MgmtTagMapper.toResponse(tag), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<MgmtTag>> createTargetTags(@RequestBody final List<MgmtTagRequestBodyPut> tags) {
        LOG.debug("creating {} target tags", tags.size());
        final List<TargetTag> createdTargetTags = this.tagManagement
                .createTargetTags(MgmtTagMapper.mapTagFromRequest(entityFactory, tags));
        return new ResponseEntity<>(MgmtTagMapper.toResponse(createdTargetTags), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<MgmtTag> updateTargetTag(@PathVariable("targetTagId") final Long targetTagId,
            @RequestBody final MgmtTagRequestBodyPut restTargetTagRest) {
        LOG.debug("update {} target tag", restTargetTagRest);

        final TargetTag updateTargetTag = tagManagement
                .updateTargetTag(entityFactory.tag().update(targetTagId).name(restTargetTagRest.getName())
                        .description(restTargetTagRest.getDescription()).colour(restTargetTagRest.getColour()));

        LOG.debug("target tag updated");

        return new ResponseEntity<>(MgmtTagMapper.toResponse(updateTargetTag), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteTargetTag(@PathVariable("targetTagId") final Long targetTagId) {
        LOG.debug("Delete {} target tag", targetTagId);
        final TargetTag targetTag = findTargetTagById(targetTagId);

        this.tagManagement.deleteTargetTag(targetTag.getName());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<MgmtTarget>> getAssignedTargets(@PathVariable("targetTagId") final Long targetTagId) {

        return new ResponseEntity<>(MgmtTargetMapper.toResponse(targetManagement
                .findTargetsByTag(new PageRequest(0, MgmtRestConstants.REQUEST_PARAMETER_PAGING_MAX_LIMIT), targetTagId)
                .getContent()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PagedList<MgmtTarget>> getAssignedTargets(@PathVariable("targetTagId") final Long targetTagId,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_OFFSET, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_OFFSET) final int pagingOffsetParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_LIMIT, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_LIMIT) final int pagingLimitParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SORTING, required = false) final String sortParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SEARCH, required = false) final String rsqlParam) {

        final int sanitizedOffsetParam = PagingUtility.sanitizeOffsetParam(pagingOffsetParam);
        final int sanitizedLimitParam = PagingUtility.sanitizePageLimitParam(pagingLimitParam);
        final Sort sorting = PagingUtility.sanitizeTargetSortParam(sortParam);

        final Pageable pageable = new OffsetBasedPageRequest(sanitizedOffsetParam, sanitizedLimitParam, sorting);
        Page<Target> findTargetsAll;
        if (rsqlParam == null) {
            findTargetsAll = targetManagement.findTargetsByTag(pageable, targetTagId);

        } else {
            findTargetsAll = targetManagement.findTargetsByTag(pageable, rsqlParam, targetTagId);
        }

        final Long countTargetsAll = findTargetsAll.getTotalElements();

        final List<MgmtTarget> rest = MgmtTargetMapper.toResponse(findTargetsAll.getContent());
        return new ResponseEntity<>(new PagedList<>(rest, countTargetsAll), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<MgmtTargetTagAssigmentResult> toggleTagAssignment(
            @PathVariable("targetTagId") final Long targetTagId,
            @RequestBody final List<MgmtAssignedTargetRequestBody> assignedTargetRequestBodies) {
        LOG.debug("Toggle Target assignment {} for target tag {}", assignedTargetRequestBodies.size(), targetTagId);

        final TargetTag targetTag = findTargetTagById(targetTagId);
        final TargetTagAssignmentResult assigmentResult = this.targetManagement
                .toggleTagAssignment(findTargetControllerIds(assignedTargetRequestBodies), targetTag.getName());

        final MgmtTargetTagAssigmentResult tagAssigmentResultRest = new MgmtTargetTagAssigmentResult();
        tagAssigmentResultRest.setAssignedTargets(MgmtTargetMapper.toResponse(assigmentResult.getAssignedEntity()));
        tagAssigmentResultRest.setUnassignedTargets(MgmtTargetMapper.toResponse(assigmentResult.getUnassignedEntity()));
        return new ResponseEntity<>(tagAssigmentResultRest, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<MgmtTarget>> assignTargets(@PathVariable("targetTagId") final Long targetTagId,
            @RequestBody final List<MgmtAssignedTargetRequestBody> assignedTargetRequestBodies) {
        LOG.debug("Assign Targets {} for target tag {}", assignedTargetRequestBodies.size(), targetTagId);
        final List<Target> assignedTarget = this.targetManagement
                .assignTag(findTargetControllerIds(assignedTargetRequestBodies), targetTagId);
        return new ResponseEntity<>(MgmtTargetMapper.toResponse(assignedTarget), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> unassignTarget(@PathVariable("targetTagId") final Long targetTagId,
            @PathVariable("controllerId") final String controllerId) {
        LOG.debug("Unassign target {} for target tag {}", controllerId, targetTagId);
        this.targetManagement.unAssignTag(controllerId, targetTagId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private TargetTag findTargetTagById(final Long targetTagId) {
        return tagManagement.findTargetTagById(targetTagId)
                .orElseThrow(() -> new EntityNotFoundException(TargetTag.class, targetTagId));
    }

    private List<String> findTargetControllerIds(
            final List<MgmtAssignedTargetRequestBody> assignedTargetRequestBodies) {
        return assignedTargetRequestBodies.stream().map(MgmtAssignedTargetRequestBody::getControllerId)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<MgmtTargetTagAssigmentResult> toggleTagAssignmentUnpaged(final Long targetTagId,
            final List<MgmtAssignedTargetRequestBody> assignedTargetRequestBodies) {
        return toggleTagAssignment(targetTagId, assignedTargetRequestBodies);
    }

    @Override
    public ResponseEntity<List<MgmtTarget>> assignTargetsUnpaged(final Long targetTagId,
            final List<MgmtAssignedTargetRequestBody> assignedTargetRequestBodies) {
        return assignTargets(targetTagId, assignedTargetRequestBodies);
    }

    @Override
    public ResponseEntity<Void> unassignTargetUnpaged(final Long targetTagId, final String controllerId) {
        return unassignTarget(targetTagId, controllerId);
    }

}
