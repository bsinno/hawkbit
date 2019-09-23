/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.tagdetails;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyNamedEntity;
import org.eclipse.hawkbit.ui.common.tagdetails.TagPanelLayout.TagAssignmentListener;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.hateoas.Identifiable;
import org.springframework.util.CollectionUtils;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

/**
 * Abstract class for target/ds tag token layout.
 *
 * @param <T>
 *            the special entity
 */
public abstract class AbstractTagToken<T extends ProxyNamedEntity> extends CustomField<T>
        implements TagAssignmentListener {
    private static final long serialVersionUID = 1L;

    protected static final int MAX_TAG_QUERY = 1000;

    protected TagPanelLayout tagPanelLayout;

    protected SpPermissionChecker checker;

    protected VaadinMessageSource i18n;

    protected UINotification uinotification;

    protected transient EventBus.UIEventBus eventBus;

    protected ManagementUIState managementUIState;

    protected T selectedEntity;

    protected AbstractTagToken(final SpPermissionChecker checker, final VaadinMessageSource i18n,
            final UINotification uinotification, final UIEventBus eventBus, final ManagementUIState managementUIState) {
        this.checker = checker;
        this.i18n = i18n;
        this.uinotification = uinotification;
        this.eventBus = eventBus;
        this.managementUIState = managementUIState;
        createTagPanel();
        if (doSubscribeToEventBus()) {
            eventBus.subscribe(this);
        }
    }

    /**
     * Subscribes the view to the eventBus. Method has to be overriden (return
     * false) if the view does not contain any listener to avoid Vaadin blowing
     * up our logs with warnings.
     */
    protected boolean doSubscribeToEventBus() {
        return true;
    }

    @Override
    protected Component initContent() {
        return tagPanelLayout;
    }

    @Override
    protected void doSetValue(final T value) {
        selectedEntity = value;
        repopulateTags();
    }

    private void createTagPanel() {
        tagPanelLayout = new TagPanelLayout(i18n, !isToggleTagAssignmentAllowed());
        tagPanelLayout.addTagAssignmentListener(this);
        tagPanelLayout.setSizeFull();
    }

    protected void repopulateTags() {
        if (selectedEntity == null) {
            tagPanelLayout.setVisible(false);
        } else {
            tagPanelLayout.setVisible(true);
            tagPanelLayout.initializeTags(getAllTags(), getAssignedTags());
        }
    }

    protected void tagCreated(final TagData tagData) {
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

    protected abstract List<TagData> getAllTags();

    protected abstract List<TagData> getAssignedTags();

    public TagPanelLayout getTagPanel() {
        return tagPanelLayout;
    }
}
