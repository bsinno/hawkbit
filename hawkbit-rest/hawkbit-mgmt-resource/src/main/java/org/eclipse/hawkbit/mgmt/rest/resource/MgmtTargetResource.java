/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.mgmt.rest.resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.eclipse.hawkbit.mgmt.json.model.MgmtMaintenanceWindowRequestBody;
import org.eclipse.hawkbit.mgmt.json.model.MgmtMetadata;
import org.eclipse.hawkbit.mgmt.json.model.MgmtMetadataBodyPut;
import org.eclipse.hawkbit.mgmt.json.model.PagedList;
import org.eclipse.hawkbit.mgmt.json.model.action.MgmtAction;
import org.eclipse.hawkbit.mgmt.json.model.action.MgmtActionRequestBodyPut;
import org.eclipse.hawkbit.mgmt.json.model.action.MgmtActionStatus;
import org.eclipse.hawkbit.mgmt.json.model.distributionset.MgmtActionType;
import org.eclipse.hawkbit.mgmt.json.model.distributionset.MgmtDistributionSet;
import org.eclipse.hawkbit.mgmt.json.model.distributionset.MgmtTargetAssignmentResponseBody;
import org.eclipse.hawkbit.mgmt.json.model.target.MgmtDistributionSetAssignment;
import org.eclipse.hawkbit.mgmt.json.model.target.MgmtTarget;
import org.eclipse.hawkbit.mgmt.json.model.target.MgmtTargetAttributes;
import org.eclipse.hawkbit.mgmt.json.model.target.MgmtTargetRequestBody;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtRestConstants;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtTargetRestApi;
import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.MaintenanceScheduleHelper;
import org.eclipse.hawkbit.repository.OffsetBasedPageRequest;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.ActionStatus;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetMetadata;
import org.eclipse.hawkbit.repository.model.TargetWithActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Resource handling target CRUD operations.
 */
@RestController
public class MgmtTargetResource implements MgmtTargetRestApi {
    private static final Logger LOG = LoggerFactory.getLogger(MgmtTargetResource.class);

    @Autowired
    private TargetManagement targetManagement;

    @Autowired
    private DeploymentManagement deploymentManagement;

    @Autowired
    private EntityFactory entityFactory;

    @Override
    public ResponseEntity<MgmtTarget> getTarget(@PathVariable("targetId") final String targetId) {
        final Target findTarget = findTargetWithExceptionIfNotFound(targetId);
        // to single response include poll status
        final MgmtTarget response = MgmtTargetMapper.toResponse(findTarget);
        MgmtTargetMapper.addPollStatus(findTarget, response);
        MgmtTargetMapper.addTargetLinks(response);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PagedList<MgmtTarget>> getTargets(
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_OFFSET, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_OFFSET) final int pagingOffsetParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_LIMIT, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_LIMIT) final int pagingLimitParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SORTING, required = false) final String sortParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SEARCH, required = false) final String rsqlParam) {

        final int sanitizedOffsetParam = PagingUtility.sanitizeOffsetParam(pagingOffsetParam);
        final int sanitizedLimitParam = PagingUtility.sanitizePageLimitParam(pagingLimitParam);
        final Sort sorting = PagingUtility.sanitizeTargetSortParam(sortParam);

        final Pageable pageable = new OffsetBasedPageRequest(sanitizedOffsetParam, sanitizedLimitParam, sorting);
        final Slice<Target> findTargetsAll;
        final long countTargetsAll;
        if (rsqlParam != null) {
            final Page<Target> findTargetPage = this.targetManagement.findByRsql(pageable, rsqlParam);
            countTargetsAll = findTargetPage.getTotalElements();
            findTargetsAll = findTargetPage;
        } else {
            findTargetsAll = this.targetManagement.findAll(pageable);
            countTargetsAll = this.targetManagement.count();
        }

        final List<MgmtTarget> rest = MgmtTargetMapper.toResponse(findTargetsAll.getContent());
        return ResponseEntity.ok(new PagedList<>(rest, countTargetsAll));
    }

    @Override
    public ResponseEntity<List<MgmtTarget>> createTargets(@RequestBody final List<MgmtTargetRequestBody> targets) {
        LOG.debug("creating {} targets", targets.size());
        final Collection<Target> createdTargets = this.targetManagement
                .create(MgmtTargetMapper.fromRequest(entityFactory, targets));
        LOG.debug("{} targets created, return status {}", targets.size(), HttpStatus.CREATED);
        return new ResponseEntity<>(MgmtTargetMapper.toResponse(createdTargets), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<MgmtTarget> updateTarget(@PathVariable("targetId") final String targetId,
            @RequestBody final MgmtTargetRequestBody targetRest) {

        if (targetRest.isRequestAttributes() != null) {
            if (targetRest.isRequestAttributes()) {
                targetManagement.requestControllerAttributes(targetId);
            } else {
                return ResponseEntity.badRequest().build();
            }
        }

        final Target updateTarget = this.targetManagement.update(entityFactory.target().update(targetId)
                .name(targetRest.getName()).description(targetRest.getDescription()).address(targetRest.getAddress())
                .securityToken(targetRest.getSecurityToken()).requestAttributes(targetRest.isRequestAttributes()));

        final MgmtTarget response = MgmtTargetMapper.toResponse(updateTarget);
        MgmtTargetMapper.addPollStatus(updateTarget, response);
        MgmtTargetMapper.addTargetLinks(response);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteTarget(@PathVariable("targetId") final String targetId) {
        this.targetManagement.deleteByControllerID(targetId);
        LOG.debug("{} target deleted, return status {}", targetId, HttpStatus.OK);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<MgmtTargetAttributes> getAttributes(@PathVariable("targetId") final String targetId) {
        final Map<String, String> controllerAttributes = targetManagement.getControllerAttributes(targetId);
        if (controllerAttributes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        final MgmtTargetAttributes result = new MgmtTargetAttributes();
        result.putAll(controllerAttributes);

        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<PagedList<MgmtAction>> getActionHistory(
            @PathVariable("targetId") final String targetId,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_OFFSET, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_OFFSET) final int pagingOffsetParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_LIMIT, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_LIMIT) final int pagingLimitParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SORTING, required = false) final String sortParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SEARCH, required = false) final String rsqlParam) {

        findTargetWithExceptionIfNotFound(targetId);

        final int sanitizedOffsetParam = PagingUtility.sanitizeOffsetParam(pagingOffsetParam);
        final int sanitizedLimitParam = PagingUtility.sanitizePageLimitParam(pagingLimitParam);
        final Sort sorting = PagingUtility.sanitizeActionSortParam(sortParam);
        final Pageable pageable = new OffsetBasedPageRequest(sanitizedOffsetParam, sanitizedLimitParam, sorting);

        final Slice<Action> activeActions;
        final Long totalActionCount;
        if (rsqlParam != null) {
            activeActions = this.deploymentManagement.findActionsByTarget(rsqlParam, targetId, pageable);
            totalActionCount = this.deploymentManagement.countActionsByTarget(rsqlParam, targetId);
        } else {
            activeActions = this.deploymentManagement.findActionsByTarget(targetId, pageable);
            totalActionCount = this.deploymentManagement.countActionsByTarget(targetId);
        }

        return ResponseEntity.ok(
                new PagedList<>(MgmtTargetMapper.toResponse(targetId, activeActions.getContent()),
                totalActionCount));
    }

    @Override
    public ResponseEntity<MgmtAction> getAction(@PathVariable("targetId") final String targetId,
            @PathVariable("actionId") final Long actionId) {

        final Action action = deploymentManagement.findAction(actionId)
                .orElseThrow(() -> new EntityNotFoundException(Action.class, actionId));
        if (!action.getTarget().getControllerId().equals(targetId)) {
            LOG.warn("given action ({}) is not assigned to given target ({}).", action.getId(), targetId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(MgmtTargetMapper.toResponseWithLinks(targetId, action));
    }

    @Override
    public ResponseEntity<Void> cancelAction(@PathVariable("targetId") final String targetId,
            @PathVariable("actionId") final Long actionId,
            @RequestParam(value = "force", required = false, defaultValue = "false") final boolean force) {
        final Action action = deploymentManagement.findAction(actionId)
                .orElseThrow(() -> new EntityNotFoundException(Action.class, actionId));

        if (!action.getTarget().getControllerId().equals(targetId)) {
            LOG.warn("given action ({}) is not assigned to given target ({}).", actionId, targetId);
            return ResponseEntity.notFound().build();
        }

        if (force) {
            this.deploymentManagement.forceQuitAction(actionId);
        } else {
            this.deploymentManagement.cancelAction(actionId);
        }
        // both functions will throw an exception, when action is in wrong
        // state, which is mapped by MgmtResponseExceptionHandler.

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PagedList<MgmtActionStatus>> getActionStatusList(
            @PathVariable("targetId") final String targetId, @PathVariable("actionId") final Long actionId,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_OFFSET, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_OFFSET) final int pagingOffsetParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_LIMIT, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_LIMIT) final int pagingLimitParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SORTING, required = false) final String sortParam) {

        final Target target = findTargetWithExceptionIfNotFound(targetId);

        final Action action = deploymentManagement.findAction(actionId)
                .orElseThrow(() -> new EntityNotFoundException(Action.class, actionId));

        if (!action.getTarget().getId().equals(target.getId())) {
            LOG.warn("given action ({}) is not assigned to given target ({}).", action.getId(), target.getId());
            return ResponseEntity.notFound().build();
        }

        final int sanitizedOffsetParam = PagingUtility.sanitizeOffsetParam(pagingOffsetParam);
        final int sanitizedLimitParam = PagingUtility.sanitizePageLimitParam(pagingLimitParam);
        final Sort sorting = PagingUtility.sanitizeActionStatusSortParam(sortParam);

        final Page<ActionStatus> statusList = this.deploymentManagement.findActionStatusByAction(
                new OffsetBasedPageRequest(sanitizedOffsetParam, sanitizedLimitParam, sorting), action.getId());

        return ResponseEntity.ok(new PagedList<>(
                MgmtTargetMapper.toActionStatusRestResponse(statusList.getContent(), deploymentManagement),
                statusList.getTotalElements()));

    }

    @Override
    public ResponseEntity<MgmtDistributionSet> getAssignedDistributionSet(
            @PathVariable("targetId") final String targetId) {
        final MgmtDistributionSet distributionSetRest = deploymentManagement.getAssignedDistributionSet(targetId)
                .map(ds -> {
                    final MgmtDistributionSet response = MgmtDistributionSetMapper.toResponse(ds);
                    MgmtDistributionSetMapper.addLinks(ds, response);

                    return response;
                }).orElse(null);

        if (distributionSetRest == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(distributionSetRest);
    }

    @Override
    public ResponseEntity<MgmtTargetAssignmentResponseBody> postAssignedDistributionSet(
            @PathVariable("targetId") final String targetId,
            @RequestBody final MgmtDistributionSetAssignment dsId,
            @RequestParam(value = "offline", required = false) final boolean offline) {

        if (offline) {
            return ResponseEntity.ok(MgmtDistributionSetMapper.toResponse(
                    deploymentManagement.offlineAssignedDistributionSet(dsId.getId(), Arrays.asList(targetId))));
        }

        findTargetWithExceptionIfNotFound(targetId);
        final MgmtMaintenanceWindowRequestBody maintenanceWindow = dsId.getMaintenanceWindow();

        if (maintenanceWindow == null) {
            return ResponseEntity.ok(MgmtDistributionSetMapper.toResponse(this.deploymentManagement
                    .assignDistributionSet(dsId.getId(), Arrays.asList(new TargetWithActionType(targetId,
                            MgmtRestModelMapper.convertActionType(dsId.getType()), dsId.getForcetime())))));
        }

        final String cronSchedule = maintenanceWindow.getSchedule();
        final String duration = maintenanceWindow.getDuration();
        final String timezone = maintenanceWindow.getTimezone();

        MaintenanceScheduleHelper.validateMaintenanceSchedule(cronSchedule, duration, timezone);

        return ResponseEntity
                .ok(MgmtDistributionSetMapper.toResponse(this.deploymentManagement.assignDistributionSet(dsId.getId(),
                        Arrays.asList(new TargetWithActionType(targetId,
                                MgmtRestModelMapper.convertActionType(dsId.getType()), dsId.getForcetime(),
                                cronSchedule, duration, timezone)))));

    }

    @Override
    public ResponseEntity<MgmtDistributionSet> getInstalledDistributionSet(
            @PathVariable("targetId") final String targetId) {
        final MgmtDistributionSet distributionSetRest = deploymentManagement.getInstalledDistributionSet(targetId)
                .map(set -> {
                    final MgmtDistributionSet response = MgmtDistributionSetMapper.toResponse(set);
                    MgmtDistributionSetMapper.addLinks(set, response);

                    return response;
                }).orElse(null);

        if (distributionSetRest == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(distributionSetRest);
    }

    private Target findTargetWithExceptionIfNotFound(final String targetId) {
        return targetManagement.getByControllerID(targetId)
                .orElseThrow(() -> new EntityNotFoundException(Target.class, targetId));
    }

    @Override
    public ResponseEntity<MgmtAction> updateAction(@PathVariable("targetId") final String targetId,
            @PathVariable("actionId") final Long actionId, @RequestBody final MgmtActionRequestBodyPut actionUpdate) {

        Action action = deploymentManagement.findAction(actionId)
                .orElseThrow(() -> new EntityNotFoundException(Action.class, actionId));
        if (!action.getTarget().getControllerId().equals(targetId)) {
            LOG.warn("given action ({}) is not assigned to given target ({}).", action.getId(), targetId);
            return ResponseEntity.notFound().build();
        }

        if (!MgmtActionType.FORCED.equals(actionUpdate.getForceType())) {
            throw new ValidationException("Resource supports only switch to FORCED.");
        }

        action = deploymentManagement.forceTargetAction(actionId);

        return ResponseEntity.ok(MgmtTargetMapper.toResponseWithLinks(targetId, action));
    }

    @Override
    public ResponseEntity<PagedList<MgmtMetadata>> getMetadata(@PathVariable("targetId") final String targetId,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_OFFSET, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_OFFSET) final int pagingOffsetParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_LIMIT, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_LIMIT) final int pagingLimitParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SORTING, required = false) final String sortParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SEARCH, required = false) final String rsqlParam) {

        final int sanitizedOffsetParam = PagingUtility.sanitizeOffsetParam(pagingOffsetParam);
        final int sanitizedLimitParam = PagingUtility.sanitizePageLimitParam(pagingLimitParam);
        final Sort sorting = PagingUtility.sanitizeDistributionSetMetadataSortParam(sortParam);

        final Pageable pageable = new OffsetBasedPageRequest(sanitizedOffsetParam, sanitizedLimitParam, sorting);
        final Page<TargetMetadata> metaDataPage;

        if (rsqlParam != null) {
            metaDataPage = targetManagement.findMetaDataByControllerIdAndRsql(pageable, targetId, rsqlParam);
        } else {
            metaDataPage = targetManagement.findMetaDataByControllerId(pageable, targetId);
        }

        return ResponseEntity.ok(new PagedList<>(MgmtTargetMapper.toResponseTargetMetadata(metaDataPage.getContent()),
                metaDataPage.getTotalElements()));
    }

    @Override
    public ResponseEntity<MgmtMetadata> getMetadataValue(@PathVariable("targetId") final String targetId,
            @PathVariable("metadataKey") final String metadataKey) {
        final TargetMetadata findOne = targetManagement.getMetaDataByControllerId(targetId, metadataKey)
                .orElseThrow(() -> new EntityNotFoundException(TargetMetadata.class, targetId, metadataKey));
        return ResponseEntity.ok(MgmtTargetMapper.toResponseTargetMetadata(findOne));
    }

    @Override
    public ResponseEntity<MgmtMetadata> updateMetadata(@PathVariable("targetId") final String targetId,
            @PathVariable("metadataKey") final String metadataKey, @RequestBody final MgmtMetadataBodyPut metadata) {
        final TargetMetadata updated = targetManagement.updateMetaData(targetId,
                entityFactory.generateTargetMetadata(metadataKey, metadata.getValue()));
        return ResponseEntity.ok(MgmtTargetMapper.toResponseTargetMetadata(updated));
    }

    @Override
    public ResponseEntity<Void> deleteMetadata(@PathVariable("targetId") final String targetId,
            @PathVariable("metadataKey") final String metadataKey) {
        targetManagement.deleteMetaData(targetId, metadataKey);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<MgmtMetadata>> createMetadata(@PathVariable("targetId") final String targetId,
            @RequestBody final List<MgmtMetadata> metadataRest) {
        final List<TargetMetadata> created = targetManagement.createMetaData(targetId,
                MgmtTargetMapper.fromRequestTargetMetadata(metadataRest, entityFactory));
        return new ResponseEntity<>(MgmtTargetMapper.toResponseTargetMetadata(created), HttpStatus.CREATED);
    }

}
