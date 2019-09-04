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
import java.util.EnumSet;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleTypeEvent;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleTypeEvent.SoftwareModuleTypeEnum;
import org.eclipse.hawkbit.ui.artifacts.event.UploadArtifactUIEvent;
import org.eclipse.hawkbit.ui.artifacts.smtype.UpdateSoftwareModuleTypeLayout;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.common.data.mappers.TypeToProxyTypeMapper;
import org.eclipse.hawkbit.ui.common.data.providers.SoftwareModuleTypeDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.event.FilterHeaderEvent.FilterHeaderEnum;
import org.eclipse.hawkbit.ui.common.event.SoftwareModuleTypeFilterHeaderEvent;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtonClickBehaviour;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtons;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.hateoas.Identifiable;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;

/**
 * Software module type filter buttons.
 *
 */
public class SMTypeFilterButtons extends AbstractFilterButtons<ProxyType, String> {
    private static final long serialVersionUID = 1L;

    private final ArtifactUploadState artifactUploadState;
    private final UINotification uiNotification;
    private final SpPermissionChecker permChecker;

    private final transient EntityFactory entityFactory;
    private final transient SoftwareModuleTypeManagement softwareModuleTypeManagement;
    private final transient SMTypeFilterButtonClick sMTypeFilterButtonClickBehaviour;

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
     * @param entityFactory
     *            EntityFactory
     * @param permChecker
     *            SpPermissionChecker
     * @param uiNotification
     *            UINotification
     */
    public SMTypeFilterButtons(final UIEventBus eventBus, final ArtifactUploadState artifactUploadState,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement, final VaadinMessageSource i18n,
            final EntityFactory entityFactory, final SpPermissionChecker permChecker,
            final UINotification uiNotification) {
        super(eventBus, i18n, uiNotification, permChecker);

        this.artifactUploadState = artifactUploadState;
        this.uiNotification = uiNotification;
        this.permChecker = permChecker;
        this.entityFactory = entityFactory;
        this.softwareModuleTypeManagement = softwareModuleTypeManagement;

        this.sMTypeFilterButtonClickBehaviour = new SMTypeFilterButtonClick(eventBus, artifactUploadState,
                softwareModuleTypeManagement);
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

    @Override
    protected void deleteFilterButtons(final Collection<ProxyType> filterButtonsToDelete) {
        // TODO: we do not allow multiple deletion yet
        final ProxyType distSMTypeToDelete = filterButtonsToDelete.iterator().next();
        final String distSMTypeToDeleteName = distSMTypeToDelete.getName();
        final Long distSMTypeToDeleteId = distSMTypeToDelete.getId();

        final Long clickedDistSMTypeId = artifactUploadState.getSoftwareModuleFilters().getSoftwareModuleType()
                .map(Identifiable::getId).orElse(null);

        if (clickedDistSMTypeId != null && clickedDistSMTypeId.equals(distSMTypeToDeleteId)) {
            uiNotification.displayValidationError(i18n.getMessage("message.tag.delete", distSMTypeToDeleteName));
        } else {
            softwareModuleTypeManagement.delete(distSMTypeToDeleteId);
            eventBus.publish(this, UploadArtifactUIEvent.DELETED_ALL_SOFTWARE_TYPE);
            // TODO: check if it is needed
            hideActionColumns();
            eventBus.publish(this, new SoftwareModuleTypeFilterHeaderEvent(FilterHeaderEnum.SHOW_MENUBAR));
        }
    }

    @Override
    protected void editButtonClickListener(final ProxyType clickedFilter) {
        new UpdateSoftwareModuleTypeLayout(i18n, entityFactory, eventBus, permChecker, uiNotification,
                softwareModuleTypeManagement, clickedFilter.getName(), closeEvent -> {
                    // TODO: check if it is needed
                    hideActionColumns();
                    eventBus.publish(this, new SoftwareModuleTypeFilterHeaderEvent(FilterHeaderEnum.SHOW_MENUBAR));
                });
    }

    @Override
    protected boolean isClickedByDefault(final String typeName) {
        return artifactUploadState.getSoftwareModuleFilters().getSoftwareModuleType()
                .map(type -> type.getName().equals(typeName)).orElse(false);
    }

    @Override
    protected String getFilterButtonIdPrefix() {
        return UIComponentIdProvider.UPLOAD_TYPE_BUTTON_PREFIX;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final SoftwareModuleTypeEvent event) {
        if (event.getSoftwareModuleType() != null
                && EnumSet.allOf(SoftwareModuleTypeEnum.class).contains(event.getSoftwareModuleTypeEnum())) {
            refreshContainer();
        }

        if (isUpdate(event)) {
            eventBus.publish(this, new SoftwareModuleTypeFilterHeaderEvent(FilterHeaderEnum.SHOW_MENUBAR));
        }
    }

    private static boolean isUpdate(final SoftwareModuleTypeEvent event) {
        return event.getSoftwareModuleTypeEnum() == SoftwareModuleTypeEnum.UPDATE_SOFTWARE_MODULE_TYPE;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final UploadArtifactUIEvent event) {
        if (event == UploadArtifactUIEvent.DELETED_ALL_SOFTWARE_TYPE) {
            refreshContainer();
        }
    }
}
