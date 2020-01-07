/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.tagdetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetTag;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.TagToProxyTagMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.TargetModifiedEventPayload;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.PageRequest;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.google.common.collect.Lists;

/**
 * Implementation of Target tag token.
 *
 *
 */
public class TargetTagToken extends AbstractTagToken<ProxyTarget> {
    private final TargetTagManagement tagManagement;
    private final TargetManagement targetManagement;

    private final TagToProxyTagMapper<TargetTag> tagMapper;

    public TargetTagToken(final SpPermissionChecker checker, final VaadinMessageSource i18n,
            final UINotification uinotification, final UIEventBus eventBus, final TargetTagManagement tagManagement,
            final TargetManagement targetManagement) {
        super(checker, i18n, uinotification, eventBus);

        this.tagManagement = tagManagement;
        this.targetManagement = targetManagement;

        this.tagMapper = new TagToProxyTagMapper<>();
    }

    @Override
    public void assignTag(final ProxyTag tagData) {
        final List<Target> assignedTargets = targetManagement.assignTag(Arrays.asList(selectedEntity.getControllerId()),
                tagData.getId());
        if (checkAssignmentResult(assignedTargets, selectedEntity.getId())) {
            uinotification.displaySuccess(
                    i18n.getMessage("message.target.assigned.one", selectedEntity.getName(), tagData.getName()));
            eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                    new TargetModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, selectedEntity.getId()));

            // TODO: check if needed
            tagPanelLayout.setAssignedTag(tagData);
        }
    }

    @Override
    public void unassignTag(final ProxyTag tagData) {
        final Target unassignedTarget = targetManagement.unAssignTag(selectedEntity.getControllerId(), tagData.getId());
        if (checkUnassignmentResult(unassignedTarget, selectedEntity.getId())) {
            uinotification.displaySuccess(
                    i18n.getMessage("message.target.unassigned.one", selectedEntity.getName(), tagData.getName()));
            eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                    new TargetModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, selectedEntity.getId()));

            // TODO: check if needed
            tagPanelLayout.removeAssignedTag(tagData);
        }
    }

    @Override
    protected Boolean isToggleTagAssignmentAllowed() {
        return checker.hasUpdateTargetPermission();
    }

    @Override
    protected List<ProxyTag> getAllTags() {
        return tagManagement.findAll(PageRequest.of(0, MAX_TAG_QUERY)).stream()
                .map(tag -> new ProxyTag(tag.getId(), tag.getName(), tag.getColour())).collect(Collectors.toList());
    }

    @Override
    protected List<ProxyTag> getAssignedTags() {
        if (selectedEntity != null) {
            return tagManagement.findByTarget(PageRequest.of(0, MAX_TAG_QUERY), selectedEntity.getControllerId())
                    .stream().map(tag -> new ProxyTag(tag.getId(), tag.getName(), tag.getColour()))
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    protected List<ProxyTag> getTagsById(final Collection<Long> entityIds) {
        return tagManagement.get(entityIds).stream().map(tagMapper::map).collect(Collectors.toList());
    }
}
