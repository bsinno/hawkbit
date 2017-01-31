/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository;

import java.util.Collection;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.eclipse.hawkbit.im.authentication.SpPermission.SpringEvalExpressions;
import org.eclipse.hawkbit.repository.builder.TargetCreate;
import org.eclipse.hawkbit.repository.builder.TargetUpdate;
import org.eclipse.hawkbit.repository.exception.EntityAlreadyExistsException;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.RSQLParameterSyntaxException;
import org.eclipse.hawkbit.repository.exception.RSQLParameterUnsupportedFieldException;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.Tag;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.repository.model.TargetInfo;
import org.eclipse.hawkbit.repository.model.TargetTag;
import org.eclipse.hawkbit.repository.model.TargetTagAssignmentResult;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Management service for {@link Target}s.
 *
 */
public interface TargetManagement {

    /**
     * Assign a {@link TargetTag} assignment to given {@link Target}s.
     *
     * @param controllerIds
     *            to assign for
     * @param tagId
     *            to assign
     * @return list of assigned targets
     * 
     * @throws EntityNotFoundException
     *             if given tagId does not exist
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_REPOSITORY_AND_UPDATE_TARGET)
    List<Target> assignTag(@NotEmpty Collection<String> controllerIds, @NotNull Long tagId);

    /**
     * Counts number of targets with given
     * {@link Target#getAssignedDistributionSet()}.
     *
     * @param distId
     *            to search for
     *
     * @return number of found {@link Target}s.
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET + SpringEvalExpressions.HAS_AUTH_OR
            + SpringEvalExpressions.HAS_AUTH_READ_REPOSITORY)
    Long countTargetByAssignedDistributionSet(@NotNull Long distId);

    /**
     * Count {@link Target}s for all the given filter parameters.
     *
     * @param status
     *            find targets having one of these {@link TargetUpdateStatus}s.
     *            Set to <code>null</code> in case this is not required.
     * @param overdueState
     *            find targets that are overdue (targets that did not respond
     *            during the configured intervals: poll_itvl + overdue_itvl).
     *            Set to <code>null</code> in case this is not required.
     * @param searchText
     *            to find targets having the text anywhere in name or
     *            description. Set <code>null</code> in case this is not
     *            required.
     * @param installedOrAssignedDistributionSetId
     *            to find targets having the {@link DistributionSet} as
     *            installed or assigned. Set to <code>null</code> in case this
     *            is not required.
     * @param tagNames
     *            to find targets which are having any one in this tag names.
     *            Set <code>null</code> in case this is not required.
     * @param selectTargetWithNoTag
     *            flag to select targets with no tag assigned
     *
     * @return the found number {@link Target}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Long countTargetByFilters(Collection<TargetUpdateStatus> status, Boolean overdueState, String searchText,
            Long installedOrAssignedDistributionSetId, Boolean selectTargetWithNoTag, String... tagNames);

    /**
     * Counts number of targets with given
     * {@link TargetInfo#getInstalledDistributionSet()}.
     *
     * @param distId
     *            to search for
     * @return number of found {@link Target}s.
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET + SpringEvalExpressions.HAS_AUTH_OR
            + SpringEvalExpressions.HAS_AUTH_READ_REPOSITORY)
    Long countTargetByInstalledDistributionSet(@NotNull Long distId);

    /**
     * Count {@link TargetFilterQuery}s for given target filter query.
     *
     * @param rsqlParam
     *            filter definition in RSQL syntax
     * @return the found number {@link Target}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Long countTargetByTargetFilterQuery(@NotEmpty String rsqlParam);

    /**
     * Count {@link TargetFilterQuery}s for given target filter query.
     *
     * @param targetFilterQueryId
     *            {@link TargetFilterQuery#getId()}
     * @return the found number {@link Target}s
     * 
     * @throws EntityNotFoundException
     *             if {@link TargetFilterQuery} with given ID does not exist
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Long countTargetByTargetFilterQuery(@NotNull Long targetFilterQueryId);

    /**
     * Counts all {@link Target}s in the repository.
     *
     * @return number of targets
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Long countTargetsAll();

    /**
     * creating a new {@link Target}.
     *
     * @param create
     *            to be created
     * @return the created {@link Target}
     *
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_CREATE_TARGET + SpringEvalExpressions.HAS_AUTH_OR
            + SpringEvalExpressions.IS_CONTROLLER)
    Target createTarget(@NotNull TargetCreate create);

    /**
     * creates multiple {@link Target}s. If some of the given {@link Target}s
     * already exists in the DB a {@link EntityAlreadyExistsException} is
     * thrown. {@link Target}s contain all objects of the parameter targets,
     * including duplicates.
     *
     * @param creates
     *            to be created.
     * @return the created {@link Target}s
     *
     * @throws EntityAlreadyExistsException
     *             of one of the given targets already exist.
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_CREATE_TARGET)
    List<Target> createTargets(@NotNull Collection<TargetCreate> creates);

    /**
     * Deletes all targets with the given IDs.
     *
     * @param targetIDs
     *            the IDs of the targets to be deleted
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_DELETE_TARGET)
    void deleteTargets(@NotEmpty Collection<Long> targetIDs);
    
    /**
     * Deletes target with the given IDs.
     *
     * @param controllerID
     *            the ID of the targets to be deleted
     * 
     * @throws EntityNotFoundException
     *             if target with given ID does not exist
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_DELETE_TARGET)
    void deleteTarget(@NotEmpty String controllerID);

    /**
     * Finds all targets for all the given parameter {@link TargetFilterQuery}
     * and that don't have the specified distribution set in their action
     * history.
     *
     * @param pageRequest
     *            the pageRequest to enhance the query for paging and sorting
     * @param distributionSetId
     *            id of the {@link DistributionSet}
     * @param rsqlParam
     *            filter definition in RSQL syntax
     * @return a page of the found {@link Target}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Page<Target> findAllTargetsByTargetFilterQueryAndNonDS(@NotNull Pageable pageRequest, Long distributionSetId,
            @NotNull String rsqlParam);

    /**
     * Counts all targets for all the given parameter {@link TargetFilterQuery}
     * and that don't have the specified distribution set in their action
     * history.
     *
     * @param distributionSetId
     *            id of the {@link DistributionSet}
     * @param rsqlParam
     *            filter definition in RSQL syntax
     * @return the count of found {@link Target}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Long countTargetsByTargetFilterQueryAndNonDS(@NotNull Long distributionSetId, @NotNull String rsqlParam);

    /**
     * Finds all targets for all the given parameter {@link TargetFilterQuery}
     * and that are not assigned to one of the {@link RolloutGroup}s
     *
     * @param pageRequest
     *            the pageRequest to enhance the query for paging and sorting
     * @param groups
     *            the list of {@link RolloutGroup}s
     * @param rsqlParam
     *            filter definition in RSQL syntax
     * @return a page of the found {@link Target}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Page<Target> findAllTargetsByTargetFilterQueryAndNotInRolloutGroups(@NotNull Pageable pageRequest,
            @NotEmpty Collection<Long> groups, @NotNull String rsqlParam);

    /**
     * Counts all targets for all the given parameter {@link TargetFilterQuery}
     * and that are not assigned to one of the {@link RolloutGroup}s
     *
     * @param groups
     *            the list of {@link RolloutGroup}s
     * @param rsqlParam
     *            filter definition in RSQL syntax
     * @return count of the found {@link Target}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Long countAllTargetsByTargetFilterQueryAndNotInRolloutGroups(@NotEmpty Collection<Long> groups,
            @NotNull String rsqlParam);

    /**
     * Finds all targets of the provided {@link RolloutGroup} that have no
     * Action for the RolloutGroup.
     *
     * @param pageRequest
     *            the pageRequest to enhance the query for paging and sorting
     * @param group
     *            the {@link RolloutGroup}
     * @return the found {@link Target}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Page<Target> findAllTargetsInRolloutGroupWithoutAction(@NotNull Pageable pageRequest, @NotNull Long group);

    /**
     * retrieves {@link Target}s by the assigned {@link DistributionSet} without
     * details, i.e. NO {@link Target#getTags()} and {@link Target#getActions()}
     * possible.
     *
     *
     * @param distributionSetID
     *            the ID of the {@link DistributionSet}
     * @param pageReq
     *            page parameter
     * @return the found {@link Target}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_REPOSITORY_AND_READ_TARGET)
    Page<Target> findTargetByAssignedDistributionSet(@NotNull Long distributionSetID, @NotNull Pageable pageReq);

    /**
     * Retrieves {@link Target}s by the assigned {@link DistributionSet} without
     * details, i.e. NO {@link Target#getTags()} and {@link Target#getActions()}
     * possible including additional filtering based on the given {@code spec}.
     *
     * @param distributionSetID
     *            the ID of the {@link DistributionSet}
     * @param rsqlParam
     *            the specification to filter the result set
     * @param pageReq
     *            page parameter
     * @return the found {@link Target}s, never {@code null}
     * @throws RSQLParameterUnsupportedFieldException
     *             if a field in the RSQL string is used but not provided by the
     *             given {@code fieldNameProvider}
     * @throws RSQLParameterSyntaxException
     *             if the RSQL syntax is wrong
     *
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_REPOSITORY_AND_READ_TARGET)
    Page<Target> findTargetByAssignedDistributionSet(@NotNull Long distributionSetID, @NotNull String rsqlParam,
            @NotNull Pageable pageReq);

    /**
     * Find {@link Target}s based a given IDs. The returned target will not
     * contain details (e.g {@link Target#getTags()} and
     * {@link Target#getActions()})
     *
     * @param controllerIDs
     *            to look for.
     * @return List of found{@link Target}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    List<Target> findTargetByControllerID(@NotEmpty Collection<String> controllerIDs);

    /**
     * Find a {@link Target} based a given ID. The returned target will not
     * contain details (e.g {@link Target#getTags()} and
     * {@link Target#getActions()})
     *
     * @param controllerId
     *            to look for.
     * @return {@link Target} or <code>null</code> if it does not exist
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Target findTargetByControllerID(@NotEmpty String controllerId);

    /**
     * Find {@link Target} based on given ID returns found Target with details,
     * i.e. {@link Target#getTags()} and {@link Target#getActions()} are
     * possible.
     *
     * Note: try to use {@link #findTargetByControllerID(String)} as much as
     * possible.
     *
     * @param controllerId
     *            to look for.
     * @return {@link Target} or <code>null</code> if it does not exist
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Target findTargetByControllerIDWithDetails(@NotEmpty String controllerId);

    /**
     * Filter {@link Target}s for all the given parameters. If all parameters
     * except pageable are null, all available {@link Target}s are returned.
     *
     * @param pageable
     *            page parameters
     * @param status
     *            find targets having this {@link TargetUpdateStatus}s. Set to
     *            <code>null</code> in case this is not required.
     * @param overdueState
     *            find targets that are overdue (targets that did not respond
     *            during the configured intervals: poll_itvl + overdue_itvl).
     * @param searchText
     *            to find targets having the text anywhere in name or
     *            description. Set <code>null</code> in case this is not
     *            required.
     * @param installedOrAssignedDistributionSetId
     *            to find targets having the {@link DistributionSet} as
     *            installed or assigned. Set to <code>null</code> in case this
     *            is not required.
     * @param tagNames
     *            to find targets which are having any one in this tag names.
     *            Set <code>null</code> in case this is not required.
     * @param selectTargetWithNoTag
     *            flag to select targets with no tag assigned
     *
     * @return the found {@link Target}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Slice<Target> findTargetByFilters(@NotNull Pageable pageable, Collection<TargetUpdateStatus> status,
            Boolean overdueState, String searchText, Long installedOrAssignedDistributionSetId,
            Boolean selectTargetWithNoTag, String... tagNames);

    /**
     * retrieves {@link Target}s by the installed {@link DistributionSet}without
     * details, i.e. NO {@link Target#getTags()} and {@link Target#getActions()}
     * possible.
     *
     * @param distributionSetID
     *            the ID of the {@link DistributionSet}
     * @param pageReq
     *            page parameter
     * @return the found {@link Target}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_REPOSITORY_AND_READ_TARGET)
    Page<Target> findTargetByInstalledDistributionSet(@NotNull Long distributionSetID, @NotNull Pageable pageReq);

    /**
     * retrieves {@link Target}s by the installed {@link DistributionSet}without
     * details, i.e. NO {@link Target#getTags()} and {@link Target#getActions()}
     * possible including additional filtering based on the given {@code spec}.
     *
     * @param distributionSetId
     *            the ID of the {@link DistributionSet}
     * @param rsqlParam
     *            the specification to filter the result
     * @param pageable
     *            page parameter
     * @return the found {@link Target}s, never {@code null}
     *
     * @throws RSQLParameterUnsupportedFieldException
     *             if a field in the RSQL string is used but not provided by the
     *             given {@code fieldNameProvider}
     * @throws RSQLParameterSyntaxException
     *             if the RSQL syntax is wrong
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_REPOSITORY_AND_READ_TARGET)
    Page<Target> findTargetByInstalledDistributionSet(@NotNull Long distributionSetId, @NotNull String rsqlParam,
            @NotNull Pageable pageable);

    /**
     * Retrieves the {@link Target} which have a certain
     * {@link TargetUpdateStatus} without details, i.e. NO
     * {@link Target#getTags()} and {@link Target#getActions()} possible.
     *
     * @param pageable
     *            page parameter
     * @param status
     *            the {@link TargetUpdateStatus} to be filtered on
     * @return the found {@link Target}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Page<Target> findTargetByUpdateStatus(@NotNull Pageable pageable, @NotNull TargetUpdateStatus status);

    /**
     * Retrieves all targets without details, i.e. NO {@link Target#getTags()}
     * and {@link Target#getActions()} possible
     *
     * @param pageable
     *            pagination parameter
     * @return the found {@link Target}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Slice<Target> findTargetsAll(@NotNull Pageable pageable);

    /**
     * Retrieves all targets without details, i.e. NO {@link Target#getTags()}
     * and {@link Target#getActions()} possible based on
     * {@link TargetFilterQuery#getQuery()}
     *
     * @param rsqlParam
     *            in RSQL notation
     * 
     * @param pageable
     *            pagination parameter
     *
     * @return the found {@link Target}s, never {@code null}
     * 
     * 
     * @throws RSQLParameterUnsupportedFieldException
     *             if a field in the RSQL string is used but not provided by the
     *             given {@code fieldNameProvider}
     * @throws RSQLParameterSyntaxException
     *             if the RSQL syntax is wrong
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Page<Target> findTargetsAll(@NotNull String rsqlParam, @NotNull Pageable pageable);

    /**
     * Retrieves all targets without details, i.e. NO {@link Target#getTags()}
     * and {@link Target#getActions()} possible based on
     * {@link TargetFilterQuery#getQuery()}
     *
     * @param targetFilterQueryId
     *            {@link TargetFilterQuery#getId()}
     * @param pageable
     *            pagination parameter
     *
     * @return the found {@link Target}s, never {@code null}
     *
     * @throws EntityNotFoundException
     *             if {@link TargetFilterQuery} with given ID does not exist.
     * @throws RSQLParameterUnsupportedFieldException
     *             if a field in the RSQL string is used but not provided by the
     *             given {@code fieldNameProvider}
     * @throws RSQLParameterSyntaxException
     *             if the RSQL syntax is wrong
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Slice<Target> findTargetsByTargetFilterQuery(@NotNull Long targetFilterQueryId, @NotNull Pageable pageable);

    /**
     * method retrieves all {@link Target}s from the repo in the following
     * order:
     * <p>
     * 1) {@link Target}s which have the given {@link DistributionSet} as
     * {@link Target#getTargetInfo()}
     * {@link TargetInfo#getInstalledDistributionSet()}
     * <p>
     * 2) {@link Target}s which have the given {@link DistributionSet} as
     * {@link Target#getAssignedDistributionSet()}
     * <p>
     * 3) {@link Target}s which have no connection to the given
     * {@link DistributionSet}.
     *
     * @param pageable
     *            the page request to page the result set
     * @param orderByDistributionId
     *            {@link DistributionSet#getId()} to be ordered by
     * @param filterParams
     *            the filters to apply; only filters are enabled that have
     *            non-null value; filters are AND-gated
     * @return a paged result {@link Page} of the {@link Target}s in a defined
     *         order.
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Slice<Target> findTargetsAllOrderByLinkedDistributionSet(@NotNull Pageable pageable,
            @NotNull Long orderByDistributionId, FilterParams filterParams);

    /**
     * retrieves a list of {@link Target}s by their controller ID with details,
     * i.e. {@link Target#getTags()} are possible.
     *
     * Note: try to use {@link #findTargetByControllerID(String)} as much as
     * possible.
     *
     * @param controllerIDs
     *            {@link Target}s Names parameter
     * @return the found {@link Target}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    List<Target> findTargetsByControllerIDsWithTags(@NotNull List<String> controllerIDs);

    /**
     * Find targets by tag name.
     *
     * @param tagName
     *            tag name
     * @return list of matching targets
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    List<Target> findTargetsByTag(@NotEmpty String tagName);

    /**
     * Toggles {@link TargetTag} assignment to given {@link Target}s by means
     * that if some (or all) of the targets in the list have the {@link Tag} not
     * yet assigned, they will be. If all of theme have the tag already assigned
     * they will be removed instead.
     *
     * @param targetIds
     *            to toggle for
     * @param tagName
     *            to toggle
     * @return TagAssigmentResult with all meta data of the assignment outcome.
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_UPDATE_TARGET)
    TargetTagAssignmentResult toggleTagAssignment(@NotEmpty Collection<String> targetIds, @NotEmpty String tagName);

    /**
     * Un-assign all {@link Target} from a given {@link TargetTag} .
     *
     * @param targetTagId
     *            to un-assign all targets
     * @return list of unassigned targets
     * 
     * @throws EntityNotFoundException
     *             if TAG with given ID does not exist
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_UPDATE_TARGET)
    List<Target> unAssignAllTargetsByTag(@NotNull Long targetTagId);

    /**
     * Un-assign a {@link TargetTag} assignment to given {@link Target}.
     *
     * @param controllerID
     *            to un-assign for
     * @param targetTagId
     *            to un-assign
     * @return the unassigned target or <null> if no target is unassigned
     * 
     * @throws EntityNotFoundException
     *             if TAG with given ID does not exist
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_UPDATE_TARGET)
    Target unAssignTag(@NotEmpty String controllerID, @NotNull Long targetTagId);

    /**
     * updates the {@link Target}.
     *
     * @param update
     *            to be updated
     * 
     * @return the updated {@link Target}
     * 
     * @throws EntityNotFoundException
     *             if given target does not exist
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_UPDATE_TARGET + SpringEvalExpressions.HAS_AUTH_OR
            + SpringEvalExpressions.IS_CONTROLLER)
    Target updateTarget(@NotNull TargetUpdate update);

    /**
     * Find a {@link Target} based a given ID. The returned target will not
     * contain details (e.g {@link Target#getTags()} and
     * {@link Target#getActions()})
     * 
     * @param id
     *            to look for
     * @return {@link Target} or <code>null</code> if it does not exist
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Target findTargetById(Long id);

    /**
     * Retrieves all targets without details, i.e. NO {@link Target#getTags()}
     * and {@link Target#getActions()} possible
     *
     * @param ids
     *            the ids to for
     * @return the found {@link Target}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    List<Target> findTargetAllById(@NotNull Collection<Long> ids);
}
