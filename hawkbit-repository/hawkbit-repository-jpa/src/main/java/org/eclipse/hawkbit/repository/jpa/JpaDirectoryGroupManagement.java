/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.eclipse.hawkbit.repository.DirectoryGroupFields;
import org.eclipse.hawkbit.repository.DirectoryGroupManagement;
import org.eclipse.hawkbit.repository.builder.DirectoryGroupCreate;
import org.eclipse.hawkbit.repository.builder.DirectoryGroupUpdate;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.InvalidDirectoryGroupAssignmentException;
import org.eclipse.hawkbit.repository.jpa.builder.JpaDirectoryGroupCreate;
import org.eclipse.hawkbit.repository.jpa.builder.JpaDirectoryGroupUpdate;
import org.eclipse.hawkbit.repository.jpa.configuration.Constants;
import org.eclipse.hawkbit.repository.jpa.model.DirectoryTreeId;
import org.eclipse.hawkbit.repository.jpa.model.JpaDirectoryGroup;
import org.eclipse.hawkbit.repository.jpa.rsql.RSQLUtility;
import org.eclipse.hawkbit.repository.model.DirectoryGroup;
import org.eclipse.hawkbit.repository.rsql.VirtualPropertyReplacer;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * JPA implementation of {@link DirectoryGroupManagement}.
 */
@Transactional(readOnly = true)
@Validated
public class JpaDirectoryGroupManagement implements DirectoryGroupManagement {

    private final DirectoryGroupRepository directoryGroupRepository;

    private final DirectoryTreeRepository directoryTreeRepository;

    private final VirtualPropertyReplacer virtualPropertyReplacer;

    private final Database database;

    public JpaDirectoryGroupManagement(final DirectoryGroupRepository directoryGroupRepository, final DirectoryTreeRepository directoryTreeRepository, final VirtualPropertyReplacer virtualPropertyReplacer, final Database database) {
        this.directoryGroupRepository = directoryGroupRepository;
        this.directoryTreeRepository = directoryTreeRepository;
        this.virtualPropertyReplacer = virtualPropertyReplacer;
        this.database = database;
    }

    @Override
    public Optional<DirectoryGroup> getByName(final String name) {
        return directoryGroupRepository.findByNameEquals(name);
    }

    @Override
    @Transactional
    @Retryable(include = {
            ConcurrencyFailureException.class}, maxAttempts = Constants.TX_RT_MAX, backoff = @Backoff(delay = Constants.TX_RT_DELAY))
    public DirectoryGroup create(final DirectoryGroupCreate c) {
        final JpaDirectoryGroupCreate create = (JpaDirectoryGroupCreate) c;

        return directoryGroupRepository.save(create.buildGroup());
    }

    @Override
    @Transactional
    @Retryable(include = {
            ConcurrencyFailureException.class}, maxAttempts = Constants.TX_RT_MAX, backoff = @Backoff(delay = Constants.TX_RT_DELAY))
    public List<DirectoryGroup> create(final Collection<DirectoryGroupCreate> tt) {
        @SuppressWarnings({"unchecked", "rawtypes"}) final Collection<JpaDirectoryGroupCreate> groups = (Collection) tt;

        return Collections.unmodifiableList(
                groups.stream().map(ttc -> directoryGroupRepository.save(ttc.buildGroup())).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    @Retryable(include = {
            ConcurrencyFailureException.class}, maxAttempts = Constants.TX_RT_MAX, backoff = @Backoff(delay = Constants.TX_RT_DELAY))
    public void delete(final String groupName) {
        if (!directoryGroupRepository.existsByName(groupName)) {
            throw new EntityNotFoundException(DirectoryGroup.class, groupName);
        }

        // finally delete the group itself
        directoryGroupRepository.deleteByName(groupName);
    }

    @Override
    @Transactional
    @Retryable(include = {
            ConcurrencyFailureException.class}, maxAttempts = Constants.TX_RT_MAX, backoff = @Backoff(delay = Constants.TX_RT_DELAY))
    public void deleteById(final Long groupId) {
        if (!directoryGroupRepository.existsById(groupId)) {
            throw new EntityNotFoundException(DirectoryGroup.class, groupId);
        }

        // finally delete the group itself
        directoryGroupRepository.deleteById(groupId);
    }

    @Override
    public Page<DirectoryGroup> findByRsql(final Pageable pageable, final String rsqlParam) {

        final Specification<JpaDirectoryGroup> spec = RSQLUtility.parse(rsqlParam, DirectoryGroupFields.class, virtualPropertyReplacer,
                database);
        return convertTPage(directoryGroupRepository.findAll(spec, pageable), pageable);
    }

    private static Page<DirectoryGroup> convertTPage(final Page<JpaDirectoryGroup> findAll, final Pageable pageable) {
        return new PageImpl<>(Collections.unmodifiableList(findAll.getContent()), pageable, findAll.getTotalElements());
    }

    @Override
    public long count() {
        return directoryGroupRepository.count();
    }

    @Override
    @Transactional
    @Retryable(include = {
            ConcurrencyFailureException.class}, maxAttempts = Constants.TX_RT_MAX, backoff = @Backoff(delay = Constants.TX_RT_DELAY))
    public DirectoryGroup update(final DirectoryGroupUpdate u) {
        final JpaDirectoryGroupUpdate update = (JpaDirectoryGroupUpdate) u;

        final JpaDirectoryGroup group = directoryGroupRepository.findById(update.getId())
                .orElseThrow(() -> new EntityNotFoundException(DirectoryGroup.class, update.getId()));

        update.getName().ifPresent(group::setName);
        update.getDirectoryParent().ifPresent((DirectoryGroup g) -> {
            // ensure no reverted parent child relationship exists to prevent loops
            if (directoryTreeRepository.existsById(new DirectoryTreeId(group, g))) {
                group.setDirectoryParent(g);
            }
        });

        return directoryGroupRepository.save(group);
    }

    @Override
    public DirectoryGroup assignDirectoryParent(@NotNull final Long id, @NotNull final Long groupId) {
        final JpaDirectoryGroup parentGroup = directoryGroupRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException(DirectoryGroup.class, groupId));
        final JpaDirectoryGroup group = directoryGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(DirectoryGroup.class, id));

        // ensure no reverted parent child relationship exists to prevent loops
        if (directoryTreeRepository.existsById(new DirectoryTreeId(group, parentGroup))) {
            throw new InvalidDirectoryGroupAssignmentException(id, groupId, "Causing a loop.");
        }

        group.setDirectoryParent(parentGroup);
        return directoryGroupRepository.save(group);
    }

    @Override
    public Optional<DirectoryGroup> get(final long id) {
        return directoryGroupRepository.findById(id).map(tt -> (DirectoryGroup) tt);
    }

    @Override
    public Page<DirectoryGroup> findAll(final Pageable pageable) {
        return convertTPage(directoryGroupRepository.findAll(pageable), pageable);
    }

}
