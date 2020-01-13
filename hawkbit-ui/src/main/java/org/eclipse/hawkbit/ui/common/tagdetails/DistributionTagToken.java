/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.tagdetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.DistributionSetTag;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.TagToProxyTagMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.DsModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.PageRequest;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.google.common.collect.Sets;

/**
 * Implementation of target/ds tag token layout.
 *
 */
public class DistributionTagToken extends AbstractTagToken<ProxyDistributionSet> {
    private final DistributionSetTagManagement distributionSetTagManagement;
    private final DistributionSetManagement distributionSetManagement;

    private final TagToProxyTagMapper<DistributionSetTag> tagMapper;

    public DistributionTagToken(final SpPermissionChecker checker, final VaadinMessageSource i18n,
            final UINotification uinotification, final UIEventBus eventBus,
            final DistributionSetTagManagement distributionSetTagManagement,
            final DistributionSetManagement distributionSetManagement) {
        super(checker, i18n, uinotification, eventBus);

        this.distributionSetTagManagement = distributionSetTagManagement;
        this.distributionSetManagement = distributionSetManagement;

        this.tagMapper = new TagToProxyTagMapper<>();
    }

    @Override
    public void assignTag(final ProxyTag tagData) {
        final List<DistributionSet> assignedDistributionSets = distributionSetManagement
                .assignTag(Sets.newHashSet(selectedEntity.getId()), tagData.getId());
        if (checkAssignmentResult(assignedDistributionSets, selectedEntity.getId())) {
            uinotification.displaySuccess(
                    i18n.getMessage("message.target.assigned.one", selectedEntity.getName(), tagData.getName()));
            eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                    new DsModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, selectedEntity.getId()));

            // TODO: check if needed
            tagPanelLayout.setAssignedTag(tagData);
        }
    }

    @Override
    public void unassignTag(final ProxyTag tagData) {
        final DistributionSet unAssignedDistributionSet = distributionSetManagement.unAssignTag(selectedEntity.getId(),
                tagData.getId());
        if (checkUnassignmentResult(unAssignedDistributionSet, selectedEntity.getId())) {
            uinotification.displaySuccess(
                    i18n.getMessage("message.target.unassigned.one", selectedEntity.getName(), tagData.getName()));
            eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                    new DsModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, selectedEntity.getId()));

            // TODO: check if needed
            tagPanelLayout.removeAssignedTag(tagData);
        }
    }

    @Override
    protected Boolean isToggleTagAssignmentAllowed() {
        return checker.hasUpdateRepositoryPermission();
    }

    @Override
    protected List<ProxyTag> getAllTags() {
        return distributionSetTagManagement.findAll(PageRequest.of(0, MAX_TAG_QUERY)).getContent().stream()
                .map(tagMapper::map).collect(Collectors.toList());
    }

    @Override
    protected List<ProxyTag> getAssignedTags() {
        if (selectedEntity != null) {
            return distributionSetTagManagement
                    .findByDistributionSet(PageRequest.of(0, MAX_TAG_QUERY), selectedEntity.getId()).getContent()
                    .stream().map(tagMapper::map).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    protected List<ProxyTag> getTagsById(final Collection<Long> entityIds) {
        return distributionSetTagManagement.get(entityIds).stream().map(tagMapper::map).collect(Collectors.toList());
    }
}
