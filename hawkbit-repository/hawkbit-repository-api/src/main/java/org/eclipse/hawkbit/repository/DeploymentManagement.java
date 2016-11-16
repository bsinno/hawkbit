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
import org.eclipse.hawkbit.repository.exception.CancelActionNotAllowedException;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.RSQLParameterSyntaxException;
import org.eclipse.hawkbit.repository.exception.RSQLParameterUnsupportedFieldException;
import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.Action.Status;
import org.eclipse.hawkbit.repository.model.ActionStatus;
import org.eclipse.hawkbit.repository.model.ActionWithStatusCount;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.DistributionSetAssignmentResult;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetWithActionType;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * A DeploymentManagement service provides operations for the deployment of
 * {@link DistributionSet}s to {@link Target}s.
 *
 */
public interface DeploymentManagement {

    /**
     * method assigns the {@link DistributionSet} to all {@link Target}s.
     *
     * @param pset
     *            {@link DistributionSet} which is assigned to the
     *            {@link Target}s
     * @param targets
     *            the {@link Target}s which should obtain the
     *            {@link DistributionSet}
     *
     * @return the changed targets
     *
     * @throw IncompleteDistributionSetException if mandatory
     *        {@link SoftwareModuleType} are not assigned as define by the
     *        {@link DistributionSetType}. *
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_REPOSITORY_AND_UPDATE_TARGET)
    DistributionSetAssignmentResult assignDistributionSet(@NotNull DistributionSet pset,
            @NotEmpty List<Target> targets);

    /**
     * method assigns the {@link DistributionSet} to all {@link Target}s by
     * their IDs with a specific {@link ActionType} and {@code forcetime}.
     *
     * @param dsID
     *            the ID of the distribution set to assign
     * @param actionType
     *            the type of the action to apply on the assignment
     * @param forcedTimestamp
     *            the time when the action should be forced, only necessary for
     *            {@link ActionType#TIMEFORCED}
     * @param targetIDs
     *            the IDs of the target to assign the distribution set
     * @return the assignment result
     *
     * @throw IncompleteDistributionSetException if mandatory
     *        {@link SoftwareModuleType} are not assigned as define by the
     *        {@link DistributionSetType}.
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_REPOSITORY_AND_UPDATE_TARGET)
    DistributionSetAssignmentResult assignDistributionSet(@NotNull final Long dsID, final ActionType actionType,
            final long forcedTimestamp, @NotEmpty final String... targetIDs);

    /**
     * method assigns the {@link DistributionSet} to all {@link Target}s by
     * their IDs with a specific {@link ActionType} and {@code forcetime}.
     *
     * @param dsID
     *            the ID of the distribution set to assign
     * @param targets
     *            a list of all targets and their action type
     * @return the assignment result
     *
     * @throw IncompleteDistributionSetException if mandatory
     *        {@link SoftwareModuleType} are not assigned as define by the
     *        {@link DistributionSetType}.
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_REPOSITORY_AND_UPDATE_TARGET)
    DistributionSetAssignmentResult assignDistributionSet(@NotNull Long dsID,
            @NotEmpty Collection<TargetWithActionType> targets);

    /**
     * method assigns the {@link DistributionSet} to all {@link Target}s by
     * their IDs with a specific {@link ActionType} and an action message.
     *
     * @param dsID
     *            the ID of the distribution set to assign
     * @param targets
     *            a list of all targets and their action type
     * @param actionMessage
     *            an optional message for the action status
     * @return the assignment result
     *
     * @throw IncompleteDistributionSetException if mandatory
     *        {@link SoftwareModuleType} are not assigned as define by the
     *        {@link DistributionSetType}.
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_REPOSITORY_AND_UPDATE_TARGET)
    DistributionSetAssignmentResult assignDistributionSet(@NotNull Long dsID,
                                                          @NotEmpty Collection<TargetWithActionType> targets,
                                                          String actionMessage);

    /**
     * method assigns the {@link DistributionSet} to all {@link Target}s by
     * their IDs with a specific {@link ActionType} and {@code forcetime}.
     *
     * @param dsID
     *            the ID of the distribution set to assign
     * @param targets
     *            a list of all targets and their action type
     * @param rollout
     *            the rollout for this assignment
     * @param rolloutGroup
     *            the rollout group for this assignment
     * @return the assignment result
     *
     * @throw IncompleteDistributionSetException if mandatory
     *        {@link SoftwareModuleType} are not assigned as define by the
     *        {@link DistributionSetType}.
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_REPOSITORY_AND_UPDATE_TARGET)
    DistributionSetAssignmentResult assignDistributionSet(@NotNull Long dsID,
            @NotEmpty Collection<TargetWithActionType> targets, Rollout rollout, RolloutGroup rolloutGroup);

    /**
     * method assigns the {@link DistributionSet} to all {@link Target}s by
     * their IDs.
     *
     * @param dsID
     *            {@link DistributionSet} which is assigned to the
     *            {@link Target}s
     * @param targetIDs
     *            IDs of the {@link Target}s which should obtain the
     *            {@link DistributionSet}
     *
     * @return the changed targets
     *
     * @throws EntityNotFoundException
     *             if {@link DistributionSet} does not exist.
     *
     * @throw IncompleteDistributionSetException if mandatory
     *        {@link SoftwareModuleType} are not assigned as define by the
     *        {@link DistributionSetType}.
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_REPOSITORY_AND_UPDATE_TARGET)
    DistributionSetAssignmentResult assignDistributionSet(@NotNull Long dsID, @NotEmpty String... targetIDs);

    /**
     * Cancels given {@link Action} for given {@link Target}. The method will
     * immediately add a {@link Status#CANCELED} status to the action. However,
     * it might be possible that the controller will continue to work on the
     * cancellation.
     *
     * @param action
     *            to be canceled
     * @param target
     *            for which the action needs cancellation
     *
     * @return generated {@link Action} or <code>null</code> if not active on
     *         given {@link Target}.
     * @throws CancelActionNotAllowedException
     *             in case the given action is not active or is already a cancel
     *             action
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_UPDATE_TARGET)
    Action cancelAction(@NotNull Action action, @NotNull Target target);

    /**
     * counts all actions associated to a specific target.
     *
     * @param rsqlParam
     *            rsql query string
     * @param target
     *            the target associated to the actions to count
     * @return the count value of found actions associated to the target
     * 
     * @throws RSQLParameterUnsupportedFieldException
     *             if a field in the RSQL string is used but not provided by the
     *             given {@code fieldNameProvider}
     * @throws RSQLParameterSyntaxException
     *             if the RSQL syntax is wrong
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Long countActionsByTarget(@NotNull String rsqlParam, @NotNull Target target);

    /**
     * @return the total amount of stored action status
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Long countActionStatusAll();

    /**
     * @return the total amount of stored actions
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Long countActionsAll();

    /**
     * counts all actions associated to a specific target.
     *
     * @param target
     *            the target associated to the actions to count
     * @return the count value of found actions associated to the target
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Long countActionsByTarget(@NotNull Target target);

    /**
     * Creates an action entry into the action repository. In case of existing
     * scheduled actions the scheduled actions gets canceled. A scheduled action
     * is created in-active.
     *
     * @param targets
     *            the targets to create scheduled actions for
     * @param distributionSet
     *            the distribution set for the actions
     * @param actionType
     *            the action type for the action
     * @param forcedTime
     *            the forcedTime of the action
     * @param rollout
     *            the roll out for this action
     * @param rolloutGroup
     *            the roll out group for this action
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_UPDATE_TARGET)
    void createScheduledAction(@NotEmpty Collection<Target> targets, @NotNull DistributionSet distributionSet,
            @NotNull ActionType actionType, Long forcedTime, @NotNull Rollout rollout,
            @NotNull RolloutGroup rolloutGroup);

    /**
     * Get the {@link Action} entity for given actionId.
     *
     * @param actionId
     *            to be id of the action
     * @return the corresponding {@link Action}
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Action findAction(@NotNull Long actionId);

    /**
     * Retrieves all actions for a specific rollout and in a specific status.
     *
     * @param rollout
     *            the rollout the actions beglong to
     * @param actionStatus
     *            the status of the actions
     * @return the actions referring a specific rollout an in a specific status
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    List<Action> findActionsByRolloutAndStatus(@NotNull Rollout rollout, @NotNull Action.Status actionStatus);

    /**
     * Retrieves all {@link Action}s of a specific target.
     *
     * @param pageable
     *            pagination parameter
     * @param target
     *            of which the actions have to be searched
     * @return a paged list of actions associated with the given target
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Slice<Action> findActionsByTarget(@NotNull Pageable pageable, @NotNull Target target);

    /**
     * Retrieves all {@link Action}s from repository.
     *
     * @param pageable
     *            pagination parameter
     * @return a paged list of {@link Action}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Slice<Action> findActionsAll(@NotNull Pageable pageable);

    /**
     * Retrieves all {@link Action} which assigned to a specific
     * {@link DistributionSet}.
     * 
     * @param pageable
     *            the page request parameter for paging and sorting the result
     * @param distributionSet
     *            the distribution set which should be assigned to the actions
     *            in the result
     * @return a list of {@link Action} which are assigned to a specific
     *         {@link DistributionSet}
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Slice<Action> findActionsByDistributionSet(@NotNull Pageable pageable, @NotNull DistributionSet distributionSet);

    /**
     * Retrieves all {@link Action}s assigned to a specific {@link Target} and a
     * given specification.
     *
     * @param rsqlParam
     *            rsql query string
     * @param target
     *            the target which must be assigned to the actions
     * @param pageable
     *            the page request
     * @return a slice of actions assigned to the specific target and the
     *         specification
     * 
     * @throws RSQLParameterUnsupportedFieldException
     *             if a field in the RSQL string is used but not provided by the
     *             given {@code fieldNameProvider}
     * @throws RSQLParameterSyntaxException
     *             if the RSQL syntax is wrong
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Slice<Action> findActionsByTarget(@NotNull String rsqlParam, @NotNull Target target, @NotNull Pageable pageable);

    /**
     * Retrieves all {@link Action}s of a specific target ordered by action ID.
     *
     * @param target
     *            the target associated with the actions
     * @return a list of actions associated with the given target ordered by
     *         action ID
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    List<Action> findActionsByTarget(@NotNull Target target);

    /**
     * Retrieves all {@link Action}s which are referring the given
     * {@link Target}.
     *
     * @param foundTarget
     *            the target to find actions for
     * @param pageable
     *            the pageable request to limit, sort the actions
     * @return a slice of actions found for a specific target
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Slice<Action> findActionsByTarget(@NotNull Target foundTarget, @NotNull Pageable pageable);

    /**
     * Retrieves all the {@link ActionStatus} entries of the given
     * {@link Action} and {@link Target}.
     *
     * @param pageReq
     *            pagination parameter
     * @param action
     *            to be filtered on
     * @return the corresponding {@link Page} of {@link ActionStatus}
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Page<ActionStatus> findActionStatusByAction(@NotNull Pageable pageReq, @NotNull Action action);

    /**
     * Retrieves all {@link ActionStatus} inclusive their messages by a specific
     * {@link Action}.
     * 
     * @param pageable
     *            the page request parameter for paging and sorting the result
     * @param action
     *            the {@link Action} to retrieve the {@link ActionStatus} from
     * @return a page of {@link ActionStatus} by a speciifc {@link Action}
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Page<ActionStatus> findActionStatusByActionWithMessages(@NotNull Pageable pageable, @NotNull Action action);

    /**
     * Retrieves all {@link Action}s of a specific target ordered by action ID.
     *
     * @param target
     *            the target associated with the actions
     * @return a list of actions associated with the given target ordered by
     *         action ID
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    List<ActionWithStatusCount> findActionsWithStatusCountByTargetOrderByIdDesc(@NotNull Target target);

    /**
     * Get the {@link Action} entity for given actionId with all lazy attributes
     * (i.e. distributionSet, target, target.assignedDs).
     *
     * @param actionId
     *            to be id of the action
     * @return the corresponding {@link Action}
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Action findActionWithDetails(@NotNull Long actionId);

    /**
     * Retrieves all active {@link Action}s of a specific target ordered by
     * action ID.
     *
     * @param pageable
     *            the pagination parameter
     * @param target
     *            the target associated with the actions
     * @return a paged list of actions associated with the given target
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Page<Action> findActiveActionsByTarget(@NotNull Pageable pageable, @NotNull Target target);

    /**
     * Retrieves all active {@link Action}s of a specific target ordered by
     * action ID.
     *
     * @param target
     *            the target associated with the actions
     * @return a list of actions associated with the given target
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    List<Action> findActiveActionsByTarget(@NotNull Target target);

    /**
     * Retrieves all inactive {@link Action}s of a specific target ordered by
     * action ID.
     *
     * @param pageable
     *            the pagination parameter
     * @param target
     *            the target associated with the actions
     * @return a paged list of actions associated with the given target
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Page<Action> findInActiveActionsByTarget(@NotNull Pageable pageable, @NotNull Target target);

    /**
     * Retrieves all inactive {@link Action}s of a specific target ordered by
     * action ID.
     *
     * @param target
     *            the target associated with the actions
     * @return a list of actions associated with the given target
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    List<Action> findInActiveActionsByTarget(@NotNull Target target);

    /**
     * Force cancels given {@link Action} for given {@link Target}. Force
     * canceling means that the action is marked as canceled on the SP server
     * and a cancel request is sent to the target. But however it's not tracked,
     * if the targets handles the cancel request or not.
     *
     * @param action
     *            to be canceled
     *
     * @return generated {@link Action} or <code>null</code> if not active on
     *         {@link Target}.
     * @throws CancelActionNotAllowedException
     *             in case the given action is not active
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_UPDATE_TARGET)
    Action forceQuitAction(@NotNull Action action);

    /**
     * Updates a {@link Action} and forces the {@link Action} if it's not
     * already forced.
     *
     * @param actionId
     *            the ID of the action
     * @return the updated or the found {@link Action}
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_UPDATE_TARGET)
    Action forceTargetAction(@NotNull Long actionId);

    /**
     * Starting an action which is scheduled, e.g. in case of roll out a
     * scheduled action must be started now.
     *
     * @param actionId
     *            the the ID of the action to start now.
     * @return the action which has been started
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Action startScheduledAction(@NotNull Long actionId);

    /**
     * Starts all scheduled actions of an RolloutGroup parent.
     *
     * @param rollout
     *            the rollout the actions belong to
     * @param rolloutGroupParent
     *            the parent rollout group the actions should reference. null
     *            references the first group
     * @return the amount of started actions
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    long startScheduledActionsByRolloutGroupParent(@NotNull Rollout rollout, RolloutGroup rolloutGroupParent);

    /**
     * All {@link ActionStatus} entries in the repository.
     * 
     * @param pageable
     *            the pagination parameter
     * @return {@link Page} of {@link ActionStatus} entries
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Page<ActionStatus> findActionStatusAll(@NotNull Pageable pageable);
}
