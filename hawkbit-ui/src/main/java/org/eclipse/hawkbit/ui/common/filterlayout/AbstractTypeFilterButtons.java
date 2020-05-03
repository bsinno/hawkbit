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

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.FilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtonClickBehaviour.ClickBehaviourType;
import org.eclipse.hawkbit.ui.common.state.TypeFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Class for defining the type filter buttons.
 */
public abstract class AbstractTypeFilterButtons extends AbstractFilterButtons<ProxyType, String> {
    private static final long serialVersionUID = 1L;

    private final TypeFilterLayoutUiState typeFilterLayoutUiState;

    private final UINotification uiNotification;
    private final transient TypeFilterButtonClick typeFilterButtonClick;

    public AbstractTypeFilterButtons(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final UINotification uiNotification, final SpPermissionChecker permChecker,
            final TypeFilterLayoutUiState typeFilterLayoutUiState) {
        super(eventBus, i18n, uiNotification, permChecker);

        this.uiNotification = uiNotification;
        this.typeFilterLayoutUiState = typeFilterLayoutUiState;
        this.typeFilterButtonClick = new TypeFilterButtonClick(this::publishFilterChangedEvent);
    }

    @Override
    protected TypeFilterButtonClick getFilterButtonClickBehaviour() {
        return typeFilterButtonClick;
    }

    private void publishFilterChangedEvent(final ProxyType typeFilter, final ClickBehaviourType clickType) {
        // TODO: somehow move it to abstract class/TypeFilterButtonClick
        // needed to trigger style generator
        getDataCommunicator().reset();

        final Long typeId = ClickBehaviourType.CLICKED == clickType ? typeFilter.getId() : null;

        eventBus.publish(EventTopics.FILTER_CHANGED, this,
                new FilterChangedEventPayload<>(getFilterMasterEntityType(), FilterType.TYPE, typeId, getView()));

        typeFilterLayoutUiState.setClickedTypeId(typeId);
    }

    protected abstract Class<? extends ProxyIdentifiableEntity> getFilterMasterEntityType();

    protected abstract EventView getView();

    @Override
    protected boolean deleteFilterButtons(final Collection<ProxyType> filterButtonsToDelete) {
        // We do not allow multiple deletion yet
        final ProxyType typeToDelete = filterButtonsToDelete.iterator().next();
        final String typeToDeleteName = typeToDelete.getName();
        final Long typeToDeleteId = typeToDelete.getId();

        final Long clickedTypeId = getClickedTypeIdFromUiState();

        if (clickedTypeId != null && clickedTypeId.equals(typeToDeleteId)) {
            uiNotification.displayValidationError(i18n.getMessage("message.type.delete", typeToDeleteName));

            return false;
        } else if (isDefaultType(typeToDelete)) {
            uiNotification.displayValidationError(i18n.getMessage("message.cannot.delete.default.dstype"));

            return false;
        } else {
            deleteType(typeToDelete);

            eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                    new EntityModifiedEventPayload(EntityModifiedEventType.ENTITY_REMOVED, getFilterMasterEntityType(),
                            ProxyType.class, typeToDeleteId));

            return true;
        }
    }

    protected Long getClickedTypeIdFromUiState() {
        return typeFilterLayoutUiState.getClickedTypeId();
    }

    protected abstract boolean isDefaultType(final ProxyType typeToDelete);

    protected abstract void deleteType(final ProxyType typeToDelete);

    @Override
    protected boolean isDeletionAllowed() {
        return permissionChecker.hasDeleteRepositoryPermission();
    }

    @Override
    protected void editButtonClickListener(final ProxyType clickedFilter) {
        final Window updateWindow = getUpdateWindow(clickedFilter);

        updateWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.type")));
        UI.getCurrent().addWindow(updateWindow);
        updateWindow.setVisible(Boolean.TRUE);
    }

    protected abstract Window getUpdateWindow(final ProxyType clickedFilter);

    public void restoreState() {
        final Long lastClickedTypeId = getClickedTypeIdFromUiState();

        if (lastClickedTypeId != null) {
            getFilterButtonClickBehaviour().setPreviouslyClickedFilterId(lastClickedTypeId);
            // TODO: should we reset data communicator here for styling update
        }
    }
}
