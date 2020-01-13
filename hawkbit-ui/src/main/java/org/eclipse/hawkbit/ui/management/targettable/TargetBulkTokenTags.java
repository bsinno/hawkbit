/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.repository.model.TargetTag;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.TagToProxyTagMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.tagdetails.AbstractTagToken;
import org.eclipse.hawkbit.ui.management.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.PageRequest;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Target tag layout in bulk upload popup.
 *
 */
public class TargetBulkTokenTags extends AbstractTagToken<ProxyTarget> {
    private final TargetTagManagement tagManagement;

    private final TagToProxyTagMapper<TargetTag> tagMapper;

    private final ManagementUIState managementUIState;

    TargetBulkTokenTags(final SpPermissionChecker checker, final VaadinMessageSource i18n,
            final UINotification uinotification, final UIEventBus eventBus, final ManagementUIState managementUIState,
            final TargetTagManagement tagManagement) {
        super(checker, i18n, uinotification, eventBus);

        this.tagManagement = tagManagement;
        this.managementUIState = managementUIState;

        this.tagMapper = new TagToProxyTagMapper<>();
    }

    @Override
    public void assignTag(final ProxyTag tagData) {
        managementUIState.getTargetTableFilters().getBulkUpload().getAssignedTagNames().add(tagData.getName());
        tagPanelLayout.setAssignedTag(tagData);
    }

    @Override
    public void unassignTag(final ProxyTag tagData) {
        managementUIState.getTargetTableFilters().getBulkUpload().getAssignedTagNames().remove(tagData.getName());
        tagPanelLayout.removeAssignedTag(tagData);
    }

    @Override
    protected Boolean isToggleTagAssignmentAllowed() {
        return checker.hasCreateTargetPermission();
    }

    public boolean isTagSelectedForAssignment() {
        return !tagPanelLayout.getAssignedTags().isEmpty();
    }

    @Override
    protected List<ProxyTag> getAllTags() {
        return tagManagement.findAll(PageRequest.of(0, MAX_TAG_QUERY)).stream()
                .map(tag -> new ProxyTag(tag.getId(), tag.getName(), tag.getColour())).collect(Collectors.toList());
    }

    @Override
    protected List<ProxyTag> getAssignedTags() {
        // this view doesn't belong to a specific target, so the current
        // selected target in the target table is ignored and therefore there
        // are no assigned tags
        return Collections.emptyList();
    }

    public List<ProxyTag> getSelectedTagsForAssignment() {
        return tagPanelLayout.getAssignedTags();
    }

    @Override
    protected List<ProxyTag> getTagsById(final Collection<Long> entityIds) {
        return tagManagement.get(entityIds).stream().map(tagMapper::map).collect(Collectors.toList());
    }
}
