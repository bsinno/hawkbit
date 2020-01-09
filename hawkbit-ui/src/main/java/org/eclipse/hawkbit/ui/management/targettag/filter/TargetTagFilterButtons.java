/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag.filter;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.repository.model.TargetTag;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.TagToProxyTagMapper;
import org.eclipse.hawkbit.ui.common.data.providers.TargetTagDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.TargetTagModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtonClickBehaviour;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtons;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.TargetsToTagAssignmentSupport;
import org.eclipse.hawkbit.ui.management.ManagementUIState;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.targettag.TargetTagWindowBuilder;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Target Tag filter buttons table.
 */
public class TargetTagFilterButtons extends AbstractFilterButtons<ProxyTag, Void> {
    private static final long serialVersionUID = 1L;

    private final ManagementUIState managementUIState;
    private final UINotification uiNotification;

    private final transient TargetTagManagement targetTagManagement;
    private final transient TargetTagFilterButtonClick targetTagFilterButtonClickBehaviour;
    private final transient TargetTagWindowBuilder targetTagWindowBuilder;

    private final ConfigurableFilterDataProvider<ProxyTag, Void, Void> targetTagDataProvider;
    private final DragAndDropSupport<ProxyTag> dragAndDropSupport;

    TargetTagFilterButtons(final UIEventBus eventBus, final ManagementUIState managementUIState,
            final VaadinMessageSource i18n, final UINotification notification, final SpPermissionChecker permChecker,
            final TargetTagManagement targetTagManagement, final TargetManagement targetManagement,
            final TargetTagWindowBuilder targetTagWindowBuilder) {
        super(eventBus, i18n, notification, permChecker);

        this.managementUIState = managementUIState;
        this.uiNotification = notification;
        this.targetTagManagement = targetTagManagement;
        this.targetTagWindowBuilder = targetTagWindowBuilder;

        this.targetTagFilterButtonClickBehaviour = new TargetTagFilterButtonClick(eventBus, managementUIState);
        this.targetTagDataProvider = new TargetTagDataProvider(targetTagManagement,
                new TagToProxyTagMapper<TargetTag>()).withConfigurableFilter();

        final TargetsToTagAssignmentSupport targetsToTagAssignment = new TargetsToTagAssignmentSupport(notification,
                i18n, targetManagement, managementUIState, eventBus, permChecker);

        this.dragAndDropSupport = new DragAndDropSupport<>(this, i18n, notification,
                Collections.singletonMap(UIComponentIdProvider.TARGET_TABLE_ID, targetsToTagAssignment));
        this.dragAndDropSupport.addDragAndDrop();

        init();
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.TARGET_TAG_TABLE_ID;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyTag, Void, Void> getFilterDataProvider() {
        return targetTagDataProvider;
    }

    @Override
    protected String getFilterButtonsType() {
        return i18n.getMessage(UIMessageIdProvider.CAPTION_TARGET_TAG);
    }

    @Override
    protected AbstractFilterButtonClickBehaviour<ProxyTag> getFilterButtonClickBehaviour() {
        return targetTagFilterButtonClickBehaviour;
    }

    @Override
    protected void deleteFilterButtons(final Collection<ProxyTag> filterButtonsToDelete) {
        // TODO: we do not allow multiple deletion yet
        final ProxyTag targetTagToDelete = filterButtonsToDelete.iterator().next();
        final String targetTagToDeleteName = targetTagToDelete.getName();

        if (managementUIState.getTargetTableFilters().getClickedTargetTags().contains(targetTagToDeleteName)) {
            uiNotification.displayValidationError(i18n.getMessage("message.tag.delete", targetTagToDeleteName));
        } else {
            targetTagManagement.delete(targetTagToDeleteName);

            eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new TargetTagModifiedEventPayload(
                    EntityModifiedEventType.ENTITY_REMOVED, targetTagToDelete.getId()));
        }
    }

    @Override
    protected void editButtonClickListener(final ProxyTag clickedFilter) {
        final Window updateWindow = targetTagWindowBuilder.getWindowForUpdateTargetTag(clickedFilter);

        updateWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.tag")));
        UI.getCurrent().addWindow(updateWindow);
        updateWindow.setVisible(Boolean.TRUE);
    }

    // TODO
    // @Override
    // protected boolean isClickedByDefault(final Long filterButtonId) {
    // return
    // managementUIState.getTargetTableFilters().getClickedTargetTags() !=
    // null
    // &&
    // managementUIState.getTargetTableFilters().getClickedTargetTags().contains(tagName);
    // }

    @Override
    protected String getFilterButtonIdPrefix() {
        return SPUIDefinitions.TARGET_TAG_ID_PREFIXS;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ManagementUIEvent event) {
        if (event == ManagementUIEvent.RESET_SIMPLE_FILTERS
                && !managementUIState.getTargetTableFilters().getClickedTargetTags().isEmpty()) {
            targetTagFilterButtonClickBehaviour.clearTargetTagFilters();
        }
    }
}
