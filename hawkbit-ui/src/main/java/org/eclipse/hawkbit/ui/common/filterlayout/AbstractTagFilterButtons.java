/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.filterlayout;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.FilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtonClickBehaviour.ClickBehaviourType;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUITagButtonStyle;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Class for defining the tag filter buttons.
 */
public abstract class AbstractTagFilterButtons extends AbstractFilterButtons<ProxyTag, Void> {
    private static final long serialVersionUID = 1L;

    private final UINotification uiNotification;
    private final Button noTagButton;
    private final transient TagFilterButtonClick tagFilterButtonClick;

    public AbstractTagFilterButtons(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final UINotification uiNotification, final SpPermissionChecker permChecker) {
        super(eventBus, i18n, uiNotification, permChecker);

        this.uiNotification = uiNotification;
        this.noTagButton = buildNoTagButton();
        this.tagFilterButtonClick = new TagFilterButtonClick(this::publishFilterChangedEvent,
                this::publishNoTagChangedEvent);
    }

    private Button buildNoTagButton() {
        final Button noTag = SPUIComponentProvider.getButton(
                getFilterButtonIdPrefix() + SPUIDefinitions.NO_TAG_BUTTON_ID,
                i18n.getMessage(UIMessageIdProvider.LABEL_NO_TAG),
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_CLICK_TO_FILTER), null, false, null,
                SPUITagButtonStyle.class);

        final ProxyTag dummyNoTag = new ProxyTag();
        dummyNoTag.setNoTag(true);

        noTag.addClickListener(event -> getFilterButtonClickBehaviour().processFilterClick(dummyNoTag));

        return noTag;
    }

    @Override
    protected TagFilterButtonClick getFilterButtonClickBehaviour() {
        return tagFilterButtonClick;
    }

    private void publishFilterChangedEvent(final Map<Long, String> activeTagIdsWithName) {
        // TODO: somehow move it to abstract class/TypeFilterButtonClick
        // needed to trigger style generator
        getDataCommunicator().reset();

        eventBus.publish(EventTopics.FILTER_CHANGED, this, new FilterChangedEventPayload<>(getFilterMasterEntityType(),
                FilterType.TAG, activeTagIdsWithName.values(), getView()));

        updateClickedTagsUiState(activeTagIdsWithName);
    }

    protected abstract Class<? extends ProxyIdentifiableEntity> getFilterMasterEntityType();

    protected abstract EventView getView();

    protected abstract void updateClickedTagsUiState(final Map<Long, String> activeTagIdsWithName);

    private void publishNoTagChangedEvent(final ClickBehaviourType clickType) {
        final boolean isNoTagActivated = ClickBehaviourType.CLICKED == clickType;

        if (isNoTagActivated) {
            getNoTagButton().addStyleName(SPUIStyleDefinitions.SP_NO_TAG_BTN_CLICKED_STYLE);
        } else {
            getNoTagButton().removeStyleName(SPUIStyleDefinitions.SP_NO_TAG_BTN_CLICKED_STYLE);
        }

        eventBus.publish(EventTopics.FILTER_CHANGED, this, new FilterChangedEventPayload<>(getFilterMasterEntityType(),
                FilterType.NO_TAG, isNoTagActivated, getView()));

        updateClickedNoTagUiState(isNoTagActivated);
    }

    protected abstract void updateClickedNoTagUiState(boolean isNoTagActivated);

    @Override
    protected void deleteFilterButtons(final Collection<ProxyTag> filterButtonsToDelete) {
        // We do not allow multiple deletion of tags yet
        final ProxyTag tagToDelete = filterButtonsToDelete.iterator().next();
        final String tagToDeleteName = tagToDelete.getName();
        final Long tagToDeleteId = tagToDelete.getId();

        final Set<Long> clickedTagIds = getClickedTagIdsWithNameFromUiState().keySet();

        if (!CollectionUtils.isEmpty(clickedTagIds) && clickedTagIds.contains(tagToDeleteId)) {
            uiNotification.displayValidationError(i18n.getMessage("message.tag.delete", tagToDeleteName));
        } else {
            deleteTag(tagToDelete);

            eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                    new EntityModifiedEventPayload(EntityModifiedEventType.ENTITY_REMOVED, getFilterMasterEntityType(),
                            ProxyTag.class, tagToDeleteId));
        }
    }

    protected abstract Map<Long, String> getClickedTagIdsWithNameFromUiState();

    protected abstract boolean getClickedNoTagFromUiState();

    protected abstract void deleteTag(final ProxyTag tagToDelete);

    @Override
    protected void editButtonClickListener(final ProxyTag clickedFilter) {
        final Window updateWindow = getUpdateWindow(clickedFilter);

        updateWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.tag")));
        UI.getCurrent().addWindow(updateWindow);
        updateWindow.setVisible(Boolean.TRUE);
    }

    protected abstract Window getUpdateWindow(final ProxyTag clickedFilter);

    public Button getNoTagButton() {
        return noTagButton;
    }

    public void restoreState() {
        final Map<Long, String> tagsToRestore = getClickedTagIdsWithNameFromUiState();

        if (!CollectionUtils.isEmpty(tagsToRestore)) {
            getFilterButtonClickBehaviour().setPreviouslyClickedFilterIdsWithName(tagsToRestore);
            // TODO: should we reset data communicator here for styling update
        }

        if (getClickedNoTagFromUiState()) {
            getNoTagButton().addStyleName(SPUIStyleDefinitions.SP_NO_TAG_BTN_CLICKED_STYLE);
        }
    }
}
