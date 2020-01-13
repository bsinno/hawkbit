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

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyNamedEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.tagdetails.TagPanelLayout.TagAssignmentListener;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.hateoas.Identifiable;
import org.springframework.util.CollectionUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Abstract class for target/ds tag token layout.
 *
 * @param <T>
 *            the special entity
 */
public abstract class AbstractTagToken<T extends ProxyNamedEntity> implements TagAssignmentListener {
    protected static final int MAX_TAG_QUERY = 1000;

    protected TagPanelLayout tagPanelLayout;

    protected final SpPermissionChecker checker;
    protected final VaadinMessageSource i18n;
    protected final UINotification uinotification;
    protected final UIEventBus eventBus;

    protected T selectedEntity;

    protected AbstractTagToken(final SpPermissionChecker checker, final VaadinMessageSource i18n,
            final UINotification uinotification, final UIEventBus eventBus) {
        this.checker = checker;
        this.i18n = i18n;
        this.uinotification = uinotification;
        this.eventBus = eventBus;

        buildTagPanel();
        tagPanelLayout.setVisible(false);
    }

    private void buildTagPanel() {
        tagPanelLayout = new TagPanelLayout(i18n, !isToggleTagAssignmentAllowed());
        tagPanelLayout.addTagAssignmentListener(this);

        tagPanelLayout.setSpacing(false);
        tagPanelLayout.setMargin(false);
        tagPanelLayout.setSizeFull();
    }

    public void updateMasterEntityFilter(final T value) {
        selectedEntity = value;

        if (selectedEntity == null) {
            tagPanelLayout.initializeTags(Collections.emptyList(), Collections.emptyList());
            tagPanelLayout.setVisible(false);
        } else {
            tagPanelLayout.initializeTags(getAllTags(), getAssignedTags());
            tagPanelLayout.setVisible(true);
        }
    }

    protected void tagCreated(final ProxyTag tagData) {
        tagPanelLayout.tagCreated(tagData);
    }

    protected void tagDeleted(final Long tagId) {
        tagPanelLayout.tagDeleted(tagId);
    }

    protected boolean checkAssignmentResult(final List<? extends Identifiable<Long>> assignedEntities,
            final Long expectedAssignedEntityId) {
        if (!CollectionUtils.isEmpty(assignedEntities) && expectedAssignedEntityId != null) {
            final List<Long> assignedDsIds = assignedEntities.stream().map(Identifiable::getId)
                    .collect(Collectors.toList());
            if (assignedDsIds.contains(expectedAssignedEntityId)) {
                return true;
            }
        }
        return false;
    }

    protected boolean checkUnassignmentResult(final Identifiable<Long> unAssignedEntity,
            final Long expectedUnAssignedEntityId) {
        return unAssignedEntity != null && expectedUnAssignedEntityId != null
                && unAssignedEntity.getId().equals(expectedUnAssignedEntityId);
    }

    protected abstract Boolean isToggleTagAssignmentAllowed();

    protected abstract List<ProxyTag> getAllTags();

    protected abstract List<ProxyTag> getAssignedTags();

    public void onTagsModified(final Collection<Long> entityIds, final EntityModifiedEventType entityModifiedType) {
        if (entityModifiedType == EntityModifiedEventType.ENTITY_ADDED) {
            onTagsCreated(entityIds);
        } else if (entityModifiedType == EntityModifiedEventType.ENTITY_UPDATED) {
            onTagsUpdated();
        } else {
            onTagsDeleted(entityIds);
        }
    }

    private void onTagsCreated(final Collection<Long> entityIds) {
        getTagsById(entityIds).forEach(this::tagCreated);
    }

    protected abstract List<ProxyTag> getTagsById(final Collection<Long> entityIds);

    private void onTagsUpdated() {
        updateMasterEntityFilter(selectedEntity);
    }

    private void onTagsDeleted(final Collection<Long> entityIds) {
        entityIds.forEach(this::tagDeleted);
    }

    public TagPanelLayout getTagPanel() {
        return tagPanelLayout;
    }
}
