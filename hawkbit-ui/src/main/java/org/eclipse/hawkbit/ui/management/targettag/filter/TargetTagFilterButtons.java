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
import java.util.Map;
import java.util.Set;

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.repository.model.TargetTag;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.TagToProxyTagMapper;
import org.eclipse.hawkbit.ui.common.data.providers.TargetTagDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.NoTagFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.TagFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtonClickBehaviour;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtonClickBehaviour.ClickBehaviourType;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtons;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.TargetsToTagAssignmentSupport;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUITagButtonStyle;
import org.eclipse.hawkbit.ui.management.targettag.TargetTagWindowBuilder;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Target Tag filter buttons table.
 */
public class TargetTagFilterButtons extends AbstractFilterButtons<ProxyTag, Void> {
    private static final long serialVersionUID = 1L;

    private final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState;
    private final UINotification uiNotification;

    private final Button noTagButton;

    private final transient TargetTagManagement targetTagManagement;
    private final transient TargetTagFilterButtonClick targetTagFilterButtonClickBehaviour;
    private final transient TargetTagWindowBuilder targetTagWindowBuilder;

    private final ConfigurableFilterDataProvider<ProxyTag, Void, Void> targetTagDataProvider;
    private final transient TagToProxyTagMapper<TargetTag> targetTagMapper;

    private final transient DragAndDropSupport<ProxyTag> dragAndDropSupport;

    TargetTagFilterButtons(final VaadinMessageSource i18n, final UIEventBus eventBus, final UINotification notification,
            final SpPermissionChecker permChecker, final TargetTagManagement targetTagManagement,
            final TargetManagement targetManagement, final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState,
            final TargetTagWindowBuilder targetTagWindowBuilder) {
        super(eventBus, i18n, notification, permChecker);

        this.targetTagFilterLayoutUiState = targetTagFilterLayoutUiState;
        this.uiNotification = notification;
        this.targetTagManagement = targetTagManagement;
        this.targetTagWindowBuilder = targetTagWindowBuilder;

        this.noTagButton = buildNoTagButton();

        this.targetTagFilterButtonClickBehaviour = new TargetTagFilterButtonClick(this::publishFilterChangedEvent,
                this::publishNoTagChangedEvent);
        this.targetTagMapper = new TagToProxyTagMapper<>();
        this.targetTagDataProvider = new TargetTagDataProvider(targetTagManagement, targetTagMapper)
                .withConfigurableFilter();

        final TargetsToTagAssignmentSupport targetsToTagAssignment = new TargetsToTagAssignmentSupport(notification,
                i18n, eventBus, permChecker, targetManagement, targetTagFilterLayoutUiState);

        this.dragAndDropSupport = new DragAndDropSupport<>(this, i18n, notification,
                Collections.singletonMap(UIComponentIdProvider.TARGET_TABLE_ID, targetsToTagAssignment), eventBus);
        this.dragAndDropSupport.addDragAndDrop();

        init();
    }

    private Button buildNoTagButton() {
        final Button noTag = SPUIComponentProvider.getButton(
                UIComponentIdProvider.TARGET_TAG_ID_PREFIXS + SPUIDefinitions.NO_TAG_BUTTON_ID,
                i18n.getMessage(UIMessageIdProvider.LABEL_NO_TAG),
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_CLICK_TO_FILTER), null, false, null,
                SPUITagButtonStyle.class);

        final ProxyTag dummyNoTag = new ProxyTag();
        dummyNoTag.setNoTag(true);

        noTag.addClickListener(event -> targetTagFilterButtonClickBehaviour.processFilterClick(dummyNoTag));

        return noTag;
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

    private void publishFilterChangedEvent(final Map<Long, String> activeTagIdsWithName) {
        // TODO: somehow move it to abstract class/TypeFilterButtonClick
        // needed to trigger style generator
        getDataCommunicator().reset();

        eventBus.publish(EventTopics.TAG_FILTER_CHANGED, this, new TagFilterChangedEventPayload(
                activeTagIdsWithName.values(), Layout.TARGET_TAG_FILTER, View.DEPLOYMENT));

        targetTagFilterLayoutUiState.setClickedTargetTagIdsWithName(activeTagIdsWithName);
    }

    private void publishNoTagChangedEvent(final ClickBehaviourType clickType) {
        final boolean isNoTagActivated = ClickBehaviourType.CLICKED == clickType;

        if (isNoTagActivated) {
            noTagButton.addStyleName(SPUIStyleDefinitions.SP_NO_TAG_BTN_CLICKED_STYLE);
        } else {
            noTagButton.removeStyleName(SPUIStyleDefinitions.SP_NO_TAG_BTN_CLICKED_STYLE);
        }

        eventBus.publish(EventTopics.NO_TAG_FILTER_CHANGED, this,
                new NoTagFilterChangedEventPayload(isNoTagActivated, Layout.TARGET_TAG_FILTER, View.DEPLOYMENT));

        targetTagFilterLayoutUiState.setNoTagClicked(isNoTagActivated);
    }

    @Override
    protected void deleteFilterButtons(final Collection<ProxyTag> filterButtonsToDelete) {
        // we do not allow multiple deletion yet
        final ProxyTag targetTagToDelete = filterButtonsToDelete.iterator().next();
        final String targetTagToDeleteName = targetTagToDelete.getName();
        final Long targetTagToDeleteId = targetTagToDelete.getId();

        final Set<Long> clickedTargetTagIds = targetTagFilterLayoutUiState.getClickedTargetTagIdsWithName().keySet();

        if (!CollectionUtils.isEmpty(clickedTargetTagIds) && clickedTargetTagIds.contains(targetTagToDeleteId)) {
            uiNotification.displayValidationError(i18n.getMessage("message.tag.delete", targetTagToDeleteName));
        } else {
            targetTagManagement.delete(targetTagToDeleteName);

            eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                    EntityModifiedEventType.ENTITY_REMOVED, ProxyTarget.class, ProxyTag.class, targetTagToDeleteId));
        }
    }

    @Override
    protected void editButtonClickListener(final ProxyTag clickedFilter) {
        final Window updateWindow = targetTagWindowBuilder.getWindowForUpdateTargetTag(clickedFilter);

        updateWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.tag")));
        UI.getCurrent().addWindow(updateWindow);
        updateWindow.setVisible(Boolean.TRUE);
    }

    @Override
    protected String getFilterButtonIdPrefix() {
        return UIComponentIdProvider.TARGET_TAG_ID_PREFIXS;
    }

    public void clearTargetTagFilters() {
        if (targetTagFilterButtonClickBehaviour.getPreviouslyClickedFiltersSize() > 0) {
            if (targetTagFilterLayoutUiState.isNoTagClicked()) {
                targetTagFilterLayoutUiState.setNoTagClicked(false);
                noTagButton.removeStyleName(SPUIStyleDefinitions.SP_NO_TAG_BTN_CLICKED_STYLE);
            }

            targetTagFilterButtonClickBehaviour.clearPreviouslyClickedFilters();
            targetTagFilterLayoutUiState.setClickedTargetTagIdsWithName(Collections.emptyMap());
            // TODO: should we reset data communicator here for styling update
        }
    }

    public Button getNoTagButton() {
        return noTagButton;
    }

    public void restoreState() {
        final Map<Long, String> tagsToRestore = targetTagFilterLayoutUiState.getClickedTargetTagIdsWithName();

        if (!CollectionUtils.isEmpty(tagsToRestore)) {
            targetTagFilterButtonClickBehaviour.setPreviouslyClickedFilterIdsWithName(tagsToRestore);
            // TODO: should we reset data communicator here for styling update
        }

        if (targetTagFilterLayoutUiState.isNoTagClicked()) {
            noTagButton.addStyleName(SPUIStyleDefinitions.SP_NO_TAG_BTN_CLICKED_STYLE);
        }
    }
}
