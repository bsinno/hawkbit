/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtype.filter;

import java.util.Collection;

import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.smtype.SmTypeWindowBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.TypeToProxyTypeMapper;
import org.eclipse.hawkbit.ui.common.data.providers.SoftwareModuleTypeDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload.TypeFilterChangedEventType;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtonClickBehaviour;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtons;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Software module type filter buttons.
 *
 */
public class SMTypeFilterButtons extends AbstractFilterButtons<ProxyType, String> {
    private static final long serialVersionUID = 1L;

    private final SMTypeFilterLayoutUiState smTypeFilterLayoutUiState;
    private final UINotification uiNotification;

    private final transient SoftwareModuleTypeManagement softwareModuleTypeManagement;
    private final transient SMTypeFilterButtonClick sMTypeFilterButtonClickBehaviour;
    private final transient SmTypeWindowBuilder smTypeWindowBuilder;

    private final ConfigurableFilterDataProvider<ProxyType, Void, String> sMTypeDataProvider;

    /**
     * Constructor
     * 
     * @param eventBus
     *            UIEventBus
     * @param artifactUploadState
     *            ArtifactUploadState
     * @param softwareModuleTypeManagement
     *            SoftwareModuleTypeManagement
     * @param i18n
     *            VaadinMessageSource
     * @param permChecker
     *            SpPermissionChecker
     * @param uiNotification
     *            UINotification
     */
    public SMTypeFilterButtons(final UIEventBus eventBus, final SMTypeFilterLayoutUiState smTypeFilterLayoutUiState,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement, final VaadinMessageSource i18n,
            final SpPermissionChecker permChecker, final UINotification uiNotification,
            final SmTypeWindowBuilder smTypeWindowBuilder) {
        super(eventBus, i18n, uiNotification, permChecker);

        this.smTypeFilterLayoutUiState = smTypeFilterLayoutUiState;
        this.uiNotification = uiNotification;
        this.softwareModuleTypeManagement = softwareModuleTypeManagement;
        this.smTypeWindowBuilder = smTypeWindowBuilder;

        this.sMTypeFilterButtonClickBehaviour = new SMTypeFilterButtonClick(this::publishFilterChangedEvent);
        this.sMTypeDataProvider = new SoftwareModuleTypeDataProvider(softwareModuleTypeManagement,
                new TypeToProxyTypeMapper<SoftwareModuleType>()).withConfigurableFilter();

        init();
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.SW_MODULE_TYPE_TABLE_ID;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyType, Void, String> getFilterDataProvider() {
        return sMTypeDataProvider;
    }

    @Override
    protected String getFilterButtonsType() {
        return i18n.getMessage("caption.entity.software.module.type");
    }

    @Override
    protected AbstractFilterButtonClickBehaviour<ProxyType> getFilterButtonClickBehaviour() {
        return sMTypeFilterButtonClickBehaviour;
    }

    private void publishFilterChangedEvent(final ProxyType typeFilter, final TypeFilterChangedEventType eventType) {
        softwareModuleTypeManagement.getByName(typeFilter.getName()).ifPresent(smType -> {
            eventBus.publish(EventTopics.TYPE_FILTER_CHANGED, this,
                    new TypeFilterChangedEventPayload<SoftwareModuleType>(eventType, smType));

            smTypeFilterLayoutUiState
                    .setClickedSmTypeId(TypeFilterChangedEventType.TYPE_CLICKED == eventType ? smType.getId() : null);
        });
    }

    @Override
    protected void deleteFilterButtons(final Collection<ProxyType> filterButtonsToDelete) {
        // TODO: we do not allow multiple deletion yet
        final ProxyType distSMTypeToDelete = filterButtonsToDelete.iterator().next();
        final String distSMTypeToDeleteName = distSMTypeToDelete.getName();
        final Long distSMTypeToDeleteId = distSMTypeToDelete.getId();

        final Long clickedDistSMTypeId = smTypeFilterLayoutUiState.getClickedSmTypeId();

        if (clickedDistSMTypeId != null && clickedDistSMTypeId.equals(distSMTypeToDeleteId)) {
            uiNotification.displayValidationError(i18n.getMessage("message.tag.delete", distSMTypeToDeleteName));
        } else {
            softwareModuleTypeManagement.delete(distSMTypeToDeleteId);
            // We do not publish an event here, because deletion is managed by
            // the grid itself
            refreshContainer();
        }
    }

    @Override
    protected void editButtonClickListener(final ProxyType clickedFilter) {
        final Window updateWindow = smTypeWindowBuilder.getWindowForUpdateSmType(clickedFilter);

        updateWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.type")));
        UI.getCurrent().addWindow(updateWindow);
        updateWindow.setVisible(Boolean.TRUE);
    }

    @Override
    protected boolean isClickedByDefault(final Long filterButtonId) {
        return smTypeFilterLayoutUiState.getClickedSmTypeId() != null
                && smTypeFilterLayoutUiState.getClickedSmTypeId().equals(filterButtonId);
    }

    @Override
    protected String getFilterButtonIdPrefix() {
        return UIComponentIdProvider.UPLOAD_TYPE_BUTTON_PREFIX;
    }
}
