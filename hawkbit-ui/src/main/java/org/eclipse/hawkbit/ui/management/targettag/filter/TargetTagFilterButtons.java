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

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.repository.model.TargetTag;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.TagToProxyTagMapper;
import org.eclipse.hawkbit.ui.common.data.providers.TargetTagDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.FilterHeaderEvent.FilterHeaderEnum;
import org.eclipse.hawkbit.ui.common.event.TargetTagFilterHeaderEvent;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtonClickBehaviour;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtons;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.TargetsToTagAssignmentSupport;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTagTableEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.management.targettag.UpdateTargetTagLayout;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;

/**
 * Target Tag filter buttons table.
 */
public class TargetTagFilterButtons extends AbstractFilterButtons<ProxyTag, Void> {
    private static final long serialVersionUID = 1L;

    private final ManagementUIState managementUIState;
    private final UINotification uiNotification;
    private final SpPermissionChecker permChecker;

    private final transient EntityFactory entityFactory;
    private final transient TargetTagManagement targetTagManagement;
    private final transient TargetTagFilterButtonClick targetTagFilterButtonClickBehaviour;

    private final ConfigurableFilterDataProvider<ProxyTag, Void, Void> targetTagDataProvider;
    private final DragAndDropSupport<ProxyTag> dragAndDropSupport;

    TargetTagFilterButtons(final UIEventBus eventBus, final ManagementUIState managementUIState,
            final VaadinMessageSource i18n, final UINotification notification, final SpPermissionChecker permChecker,
            final EntityFactory entityFactory, final TargetTagManagement targetTagManagement,
            final TargetManagement targetManagement) {
        super(eventBus, i18n, notification);

        this.managementUIState = managementUIState;
        this.uiNotification = notification;
        this.permChecker = permChecker;
        this.entityFactory = entityFactory;
        this.targetTagManagement = targetTagManagement;

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
            eventBus.publish(this, new TargetTagTableEvent(BaseEntityEventType.REMOVE_ENTITY, targetTagToDelete));
            // TODO: check if it is needed
            hideActionColumns();
            eventBus.publish(this, new TargetTagFilterHeaderEvent(FilterHeaderEnum.SHOW_MENUBAR));
        }
    }

    @Override
    protected void editButtonClickListener(final ProxyTag clickedFilter) {
        new UpdateTargetTagLayout(i18n, targetTagManagement, entityFactory, eventBus, permChecker, uiNotification,
                clickedFilter.getName(), closeEvent -> {
                    // TODO: check if it is needed
                    hideActionColumns();
                    eventBus.publish(this, new TargetTagFilterHeaderEvent(FilterHeaderEnum.SHOW_MENUBAR));
                });
    }

    @Override
    protected boolean isClickedByDefault(final String tagName) {
        return managementUIState.getTargetTableFilters().getClickedTargetTags() != null
                && managementUIState.getTargetTableFilters().getClickedTargetTags().contains(tagName);
    }

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
