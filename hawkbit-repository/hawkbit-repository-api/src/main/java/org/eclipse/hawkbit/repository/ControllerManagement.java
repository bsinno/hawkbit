/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.eclipse.hawkbit.im.authentication.SpPermission.SpringEvalExpressions;
import org.eclipse.hawkbit.repository.builder.ActionStatusCreate;
import org.eclipse.hawkbit.repository.event.remote.DownloadProgressEvent;
import org.eclipse.hawkbit.repository.exception.EntityAlreadyExistsException;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.ToManyAttributeEntriesException;
import org.eclipse.hawkbit.repository.exception.TooManyStatusEntriesException;
import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.Action.Status;
import org.eclipse.hawkbit.repository.model.ActionStatus;
import org.eclipse.hawkbit.repository.model.Artifact;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetInfo;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.tenancy.configuration.TenantConfigurationKey;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Service layer for all operations of the DDI API (with access permissions only
 * for the controller).
 *
 */
public interface ControllerManagement {

    /**
     * Adds an {@link ActionStatus} for a cancel {@link Action} including
     * potential state changes for the target and the {@link Action} itself.
     * 
     * @param create
     *            to be added
     * @return the updated {@link Action}
     * 
     * @throws EntityAlreadyExistsException
     *             if a given entity already exists
     * 
     * @throws TooManyStatusEntriesException
     *             if more than the allowed number of status entries are
     *             inserted
     * @throws EntityNotFoundException
     *             if given action does not exist
     * 
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    Action addCancelActionStatus(@NotNull ActionStatusCreate create);

    /**
     * Sends the download progress and notifies the event publisher with a
     * {@link DownloadProgressEvent}.
     * 
     * @param statusId
     *            the ID of the {@link ActionStatus}
     * @param requestedBytes
     *            requested bytes of the request
     * @param shippedBytesSinceLast
     *            since the last report
     * @param shippedBytesOverall
     *            for the {@link ActionStatus}
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    void downloadProgress(Long statusId, Long requestedBytes, Long shippedBytesSinceLast, Long shippedBytesOverall);

    /**
     * Simple addition of a new {@link ActionStatus} entry to the {@link Action}
     * . No state changes.
     * 
     * @param create
     *            to add to the action
     * 
     * @return created {@link ActionStatus} entity
     * 
     * @throws TooManyStatusEntriesException
     *             if more than the allowed number of status entries are
     *             inserted
     * @throws EntityNotFoundException
     *             if given action does not exist
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    ActionStatus addInformationalActionStatus(@NotNull ActionStatusCreate create);

    /**
     * Adds an {@link ActionStatus} entry for an update {@link Action} including
     * potential state changes for the target and the {@link Action} itself.
     *
     * @param create
     *            to be added
     * @return the updated {@link Action}
     *
     * @throws EntityAlreadyExistsException
     *             if a given entity already exists
     * @throws TooManyStatusEntriesException
     *             if more than the allowed number of status entries are
     *             inserted
     * @throws TooManyStatusEntriesException
     *             if more than the allowed number of status entries are
     *             inserted
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    Action addUpdateActionStatus(@NotNull ActionStatusCreate create);

    /**
     * Retrieves oldest {@link Action} that is active and assigned to a
     * {@link Target}.
     *
     * @param target
     *            the target to retrieve the actions from
     * @return a list of actions assigned to given target which are active
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    Optional<Action> findOldestActiveActionByTarget(@NotNull Target target);

    /**
     * Get the {@link Action} entity for given actionId with all lazy
     * attributes.
     *
     * @param actionId
     *            to be id of the action
     * @return the corresponding {@link Action}
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    Action findActionWithDetails(@NotNull Long actionId);

    /**
     * register new target in the repository (plug-and-play).
     *
     * @param controllerId
     *            reference
     * @param address
     *            the client IP address of the target, might be {@code null}
     * @return target reference
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    Target findOrRegisterTargetIfItDoesNotexist(@NotEmpty String controllerId, URI address);

    /**
     * Retrieves last {@link Action} for a download of an artifact of given
     * module and target.
     *
     * @param controllerId
     *            to look for
     * @param module
     *            that should be assigned to the target
     * @return last {@link Action} for given combination
     *
     * @throws EntityNotFoundException
     *             if action for given combination could not be found
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    Action getActionForDownloadByTargetAndSoftwareModule(@NotEmpty String controllerId, @NotNull SoftwareModule module);

    /**
     * @return current {@link TenantConfigurationKey#POLLING_TIME_INTERVAL}.
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    String getPollingTime();

    /**
     * Checks if a given target has currently or has even been assigned to the
     * given artifact through the action history list. This can e.g. indicate if
     * a target is allowed to download a given artifact because it has currently
     * assigned or had ever been assigned to the target and so it's visible to a
     * specific target e.g. for downloading.
     * 
     * @param controllerId
     *            the ID of the target to check
     * @param localArtifact
     *            the artifact to verify if the given target had even been
     *            assigned to
     * @return {@code true} if the given target has currently or had ever a
     *         relation to the given artifact through the action history,
     *         otherwise {@code false}
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    boolean hasTargetArtifactAssigned(@NotNull String controllerId, @NotNull Artifact localArtifact);

    /**
     * Checks if a given target has currently or has even been assigned to the
     * given artifact through the action history list. This can e.g. indicate if
     * a target is allowed to download a given artifact because it has currently
     * assigned or had ever been assigned to the target and so it's visible to a
     * specific target e.g. for downloading.
     * 
     * @param targetId
     *            the ID of the target to check
     * @param localArtifact
     *            the artifact to verify if the given target had even been
     *            assigned to
     * @return {@code true} if the given target has currently or had ever a
     *         relation to the given artifact through the action history,
     *         otherwise {@code false}
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    boolean hasTargetArtifactAssigned(@NotNull Long targetId, @NotNull Artifact localArtifact);

    /**
     * Registers retrieved status for given {@link Target} and {@link Action} if
     * it does not exist yet.
     *
     * @param action
     *            to the handle status for
     * @param message
     *            for the status
     * @return the update action in case the status has been changed to
     *         {@link Status#RETRIEVED}
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    Action registerRetrieved(@NotNull Action action, String message);

    /**
     * Updates attributes of the controller.
     *
     * @param controllerId
     *            to update
     * @param attributes
     *            to insert
     *
     * @return updated {@link Target}
     *
     * @throws EntityNotFoundException
     *             if target that has to be updated could not be found
     * @throws ToManyAttributeEntriesException
     *             if maximum
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    Target updateControllerAttributes(@NotEmpty String controllerId, @NotNull Map<String, String> attributes);

    /**
     * Refreshes the time of the last time the controller has been connected to
     * the server. Switches {@link TargetUpdateStatus#UNKNOWN} to
     * {@link TargetUpdateStatus#REGISTERED} if necessary.
     *
     * @param controllerId
     *            of the target to to update
     * @param address
     *            the client address of the target, might be {@code null}
     * @return the updated target
     *
     * @throws EntityNotFoundException
     *             if target with given ID could not be found
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    Target updateLastTargetQuery(@NotEmpty String controllerId, URI address);

    /**
     * Refreshes the time of the last time the controller has been connected to
     * the server.
     *
     * @param target
     *            to update
     * @param address
     *            the client address of the target, might be {@code null}
     * @return the updated target
     *
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    TargetInfo updateLastTargetQuery(@NotNull TargetInfo target, @NotNull URI address);

    /**
     * Update selective the target status of a given {@code target}.
     *
     * @param targetInfo
     *            the target to update the target status
     * @param status
     *            the status to be set of the target. Might be {@code null} if
     *            the target status should not be updated
     * @param lastTargetQuery
     *            the last target query to be set of the target. Might be
     *            {@code null} if the target lastTargetQuery should not be
     *            updated
     * @param address
     *            the client address of the target, might be {@code null}
     * @return the updated TargetInfo
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER)
    TargetInfo updateTargetStatus(@NotNull TargetInfo targetInfo, TargetUpdateStatus status, Long lastTargetQuery,
            URI address);

    /**
     * Finds {@link Target} based on given controller ID returns found Target
     * without details, i.e. NO {@link Target#getTags()} and
     * {@link Target#getActions()} possible.
     *
     * @param controllerId
     *            to look for.
     * @return {@link Target} or {@code null} if it does not exist
     * @see Target#getControllerId()
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER + SpringEvalExpressions.HAS_AUTH_OR
            + SpringEvalExpressions.IS_SYSTEM_CODE)
    Target findByControllerId(@NotEmpty final String controllerId);

    /**
     * Finds {@link Target} based on given ID returns found Target without
     * details, i.e. NO {@link Target#getTags()} and {@link Target#getActions()}
     * possible.
     *
     * @param targetId
     *            to look for.
     * @return {@link Target} or {@code null} if it does not exist
     * @see Target#getId()
     */
    @PreAuthorize(SpringEvalExpressions.IS_CONTROLLER + SpringEvalExpressions.HAS_AUTH_OR
            + SpringEvalExpressions.IS_SYSTEM_CODE)
    Target findByTargetId(final long targetId);

}
