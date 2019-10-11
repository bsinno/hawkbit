/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.disttype.filter;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.TypeToProxyTypeMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetTypeDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload.TypeFilterChangedEventType;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtonClickBehaviour;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtons;
import org.eclipse.hawkbit.ui.distributions.disttype.UpdateDistributionSetTypeLayout;
import org.eclipse.hawkbit.ui.distributions.event.DistributionSetTypeEvent;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.hateoas.Identifiable;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;

/**
 * Distribution Set Type filter buttons.
 */
public class DSTypeFilterButtons extends AbstractFilterButtons<ProxyType, String> {
    private static final long serialVersionUID = 1L;

    private final UINotification uiNotification;
    private final SpPermissionChecker permChecker;

    private final transient EntityFactory entityFactory;
    private final transient SoftwareModuleTypeManagement softwareModuleTypeManagement;
    private final transient DistributionSetManagement distributionSetManagement;
    private final transient DistributionSetTypeManagement distributionSetTypeManagement;
    private final transient SystemManagement systemManagement;
    private final DSTypeFilterLayoutUiState dSTypeFilterLayoutUiState;

    private final transient DSTypeFilterButtonClick dsTypeFilterButtonClickBehaviour;
    private final ConfigurableFilterDataProvider<ProxyType, Void, String> dsTypeDataProvider;

    /**
     * Constructor
     * 
     * @param eventBus
     *            UIEventBus
     * @param manageDistUIState
     *            ManageDistUIState
     * @param distributionSetTypeManagement
     *            DistributionSetTypeManagement
     * @param i18n
     *            VaadinMessageSource
     * @param entityFactory
     *            EntityFactory
     * @param permChecker
     *            SpPermissionChecker
     * @param uiNotification
     *            UINotification
     * @param softwareModuleTypeManagement
     *            SoftwareModuleTypeManagement
     * @param distributionSetManagement
     *            DistributionSetManagement
     * @param systemManagement
     *            SystemManagement
     */
    public DSTypeFilterButtons(final UIEventBus eventBus,
            final DistributionSetTypeManagement distributionSetTypeManagement, final VaadinMessageSource i18n,
            final EntityFactory entityFactory, final SpPermissionChecker permChecker,
            final UINotification uiNotification, final SoftwareModuleTypeManagement softwareModuleTypeManagement,
            final DistributionSetManagement distributionSetManagement, final SystemManagement systemManagement,
            final DSTypeFilterLayoutUiState dSTypeFilterLayoutUiState) {
        super(eventBus, i18n, uiNotification, permChecker);

        this.uiNotification = uiNotification;
        this.permChecker = permChecker;
        this.entityFactory = entityFactory;
        this.softwareModuleTypeManagement = softwareModuleTypeManagement;
        this.distributionSetManagement = distributionSetManagement;
        this.distributionSetTypeManagement = distributionSetTypeManagement;
        this.systemManagement = systemManagement;
        this.dSTypeFilterLayoutUiState = dSTypeFilterLayoutUiState;

        this.dsTypeFilterButtonClickBehaviour = new DSTypeFilterButtonClick(this::publishFilterChangedEvent);
        this.dsTypeDataProvider = new DistributionSetTypeDataProvider(distributionSetTypeManagement,
                new TypeToProxyTypeMapper<DistributionSetType>()).withConfigurableFilter();

        init();
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.DISTRIBUTION_SET_TYPE_TABLE_ID;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyType, Void, String> getFilterDataProvider() {
        return dsTypeDataProvider;
    }

    @Override
    protected String getFilterButtonsType() {
        return i18n.getMessage("caption.entity.distribution.type");
    }

    @Override
    protected AbstractFilterButtonClickBehaviour<ProxyType> getFilterButtonClickBehaviour() {
        return dsTypeFilterButtonClickBehaviour;
    }

    private void publishFilterChangedEvent(final ProxyType typeFilter, final TypeFilterChangedEventType eventType) {
        distributionSetTypeManagement.getByName(typeFilter.getName()).ifPresent(dsType -> {
            eventBus.publish(EventTopics.TYPE_FILTER_CHANGED, this,
                    new TypeFilterChangedEventPayload<DistributionSetType>(eventType, dsType));

            dSTypeFilterLayoutUiState
                    .setClickedDsType(TypeFilterChangedEventType.TYPE_CLICKED == eventType ? dsType : null);
        });
    }

    @Override
    protected void deleteFilterButtons(final Collection<ProxyType> filterButtonsToDelete) {
        // We do not allow multiple deletion yet
        final ProxyType dsTypeToDelete = filterButtonsToDelete.iterator().next();
        final String dsTypeToDeleteName = dsTypeToDelete.getName();
        final Long dsTypeToDeleteId = dsTypeToDelete.getId();

        final Long clickedDsTypeId = Optional.ofNullable(dSTypeFilterLayoutUiState.getClickedDsType())
                .map(Identifiable::getId).orElse(null);

        if (clickedDsTypeId != null && clickedDsTypeId.equals(dsTypeToDeleteId)) {
            uiNotification.displayValidationError(i18n.getMessage("message.tag.delete", dsTypeToDeleteName));
        } else if (isDefaultDsType(dsTypeToDeleteName)) {
            uiNotification.displayValidationError(i18n.getMessage("message.cannot.delete.default.dstype"));
        } else {
            distributionSetTypeManagement.delete(dsTypeToDeleteId);
            // We do not publish an event here, because deletion is managed by
            // the grid itself
            refreshContainer();
        }
    }

    private boolean isDefaultDsType(final String dsTypeName) {
        return getCurrentDistributionSetType() != null && getCurrentDistributionSetType().getName().equals(dsTypeName);
    }

    private DistributionSetType getCurrentDistributionSetType() {
        return systemManagement.getTenantMetadata().getDefaultDsType();
    }

    @Override
    protected void editButtonClickListener(final ProxyType clickedFilter) {
        new UpdateDistributionSetTypeLayout(i18n, entityFactory, eventBus, permChecker, uiNotification,
                softwareModuleTypeManagement, distributionSetTypeManagement, distributionSetManagement,
                // TODO: check if we can get rid of closeListener
                clickedFilter.getName(), closeEvent -> {
                });
    }

    @Override
    protected boolean isClickedByDefault(final String typeName) {
        return dSTypeFilterLayoutUiState.getClickedDsType() != null
                && dSTypeFilterLayoutUiState.getClickedDsType().getName().equals(typeName);
    }

    @Override
    protected String getFilterButtonIdPrefix() {
        return SPUIDefinitions.DISTRIBUTION_SET_TYPE_ID_PREFIXS;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final DistributionSetTypeEvent event) {
        if (event.getDistributionSetTypeEnum() == DistributionSetTypeEvent.DistributionSetTypeEnum.ADD_DIST_SET_TYPE
                || event.getDistributionSetTypeEnum() == DistributionSetTypeEvent.DistributionSetTypeEnum.UPDATE_DIST_SET_TYPE) {
            refreshContainer();
        }
    }
}
