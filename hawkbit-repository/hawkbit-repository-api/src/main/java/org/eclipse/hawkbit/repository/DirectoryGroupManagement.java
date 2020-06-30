/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.eclipse.hawkbit.im.authentication.SpPermission.SpringEvalExpressions;
import org.eclipse.hawkbit.repository.builder.DirectoryGroupCreate;
import org.eclipse.hawkbit.repository.builder.DirectoryGroupUpdate;
import org.eclipse.hawkbit.repository.exception.EntityAlreadyExistsException;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.RSQLParameterSyntaxException;
import org.eclipse.hawkbit.repository.exception.RSQLParameterUnsupportedFieldException;
import org.eclipse.hawkbit.repository.model.DirectoryGroup;
import org.eclipse.hawkbit.repository.model.Target;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Management service for {@link DirectoryGroup}s.
 */
public interface DirectoryGroupManagement {

    /**
     * count {@link DirectoryGroup}s.
     *
     * @return size of {@link DirectoryGroup}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    long count();

    /**
     * Creates a new {@link DirectoryGroup}.
     *
     * @param create to be created group
     * @return the new created {@link DirectoryGroup}
     * @throws EntityAlreadyExistsException if given object already exists
     * @throws ConstraintViolationException if fields are not filled as specified. Check
     *                                      {@link DirectoryGroupCreate} for field constraints.
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_CREATE_TARGET)
    DirectoryGroup create(@NotNull @Valid DirectoryGroupCreate create);

    /**
     * Created multiple {@link DirectoryGroup}s.
     *
     * @param creates to be created Groups
     * @return the new created {@link DirectoryGroup}s
     * @throws EntityAlreadyExistsException if given object has already an ID.
     * @throws ConstraintViolationException if fields are not filled as specified. Check
     *                                      {@link DirectoryGroupCreate} for field constraints.
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_CREATE_TARGET)
    List<DirectoryGroup> create(@NotNull @Valid Collection<DirectoryGroupCreate> creates);

    /**
     * Deletes {@link DirectoryGroup} with given name.
     *
     * @param groupName name of the {@link DirectoryGroup} to be deleted
     * @throws EntityNotFoundException if group with given name does not exist
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_DELETE_TARGET)
    void delete(@NotEmpty String groupName);

    /**
     * Deletes {@link DirectoryGroup} with given id.
     *
     * @param groupId ID of the {@link DirectoryGroup} to be deleted
     * @throws EntityNotFoundException if group with given id does not exist
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_DELETE_TARGET)
    void deleteById(@NotNull Long groupId);

    /**
     * returns all {@link DirectoryGroup}s.
     *
     * @param pageable page parameter
     * @return all {@link DirectoryGroup}s
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Page<DirectoryGroup> findAll(@NotNull Pageable pageable);


    /**
     * Retrieves all groups based on the given specification.
     *
     * @param pageable  pagination parameter
     * @param rsqlParam rsql query string
     * @return the found {@link DirectoryGroup}s, never {@code null}
     * @throws RSQLParameterUnsupportedFieldException if a field in the RSQL string is used but not provided by the
     *                                                given {@code fieldNameProvider}
     * @throws RSQLParameterSyntaxException           if the RSQL syntax is wrong
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Page<DirectoryGroup> findByRsql(@NotNull Pageable pageable, @NotNull String rsqlParam);

    /**
     * Find {@link DirectoryGroup} based on given Name.
     *
     * @param name to look for.
     * @return {@link DirectoryGroup}
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Optional<DirectoryGroup> getByName(@NotEmpty String name);

    /**
     * Finds {@link DirectoryGroup} by given id.
     *
     * @param id to search for
     * @return the found {@link DirectoryGroup}
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_READ_TARGET)
    Optional<DirectoryGroup> get(long id);

    /**
     * updates the {@link DirectoryGroup}.
     *
     * @param update the {@link DirectoryGroup} with updated values
     * @return the updated {@link DirectoryGroup}
     * @throws EntityNotFoundException      in case the {@link DirectoryGroup} does not exists and cannot be
     *                                      updated
     * @throws ConstraintViolationException if fields are not filled as specified. Check
     *                                      {@link DirectoryGroupUpdate} for field constraints.
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_UPDATE_TARGET)
    DirectoryGroup update(@NotNull @Valid DirectoryGroupUpdate update);

    /**
     * Assigns path parent {@link DirectoryGroup} to existing {@link DirectoryGroup}.
     *
     * @param id      to assign and update
     * @param groupId to get assigned
     * @return the updated {@link Target}.
     * @throws EntityNotFoundException if given group does not exist
     */
    @PreAuthorize(SpringEvalExpressions.HAS_AUTH_UPDATE_REPOSITORY)
    DirectoryGroup assignDirectoryParent(@NotNull Long id, @NotNull Long groupId);
}
