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

import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.smtype.filter.TypeFilterButtonClick;
import org.eclipse.hawkbit.ui.common.data.mappers.TypeToProxyTypeMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetTypeDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.event.DsTypeModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload.TypeFilterChangedEventType;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtonClickBehaviour;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtons;
import org.eclipse.hawkbit.ui.distributions.disttype.DsTypeWindowBuilder;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.hateoas.Identifiable;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Distribution Set Type filter buttons.
 */
public class DSTypeFilterButtons extends AbstractFilterButtons<ProxyType, String> {
    private static final long serialVersionUID = 1L;

    private final UINotification uiNotification;
    private final transient DistributionSetTypeManagement distributionSetTypeManagement;
    private final transient SystemManagement systemManagement;
    private final DSTypeFilterLayoutUiState dSTypeFilterLayoutUiState;
    private final transient DsTypeWindowBuilder dsTypeWindowBuilder;

    private final transient TypeFilterButtonClick typeFilterButtonClickBehaviour;
    private final ConfigurableFilterDataProvider<ProxyType, Void, String> dsTypeDataProvider;

    /**
     * Constructor
     * 
     * @param eventBus
     *            UIEventBus
     * @param distributionSetTypeManagement
     *            DistributionSetTypeManagement
     * @param i18n
     *            VaadinMessageSource
     * @param permChecker
     *            SpPermissionChecker
     * @param uiNotification
     *            UINotification
     * @param systemManagement
     *            SystemManagement
     */
    public DSTypeFilterButtons(final UIEventBus eventBus,
            final DistributionSetTypeManagement distributionSetTypeManagement, final VaadinMessageSource i18n,
            final SpPermissionChecker permChecker, final UINotification uiNotification,
            final SystemManagement systemManagement, final DSTypeFilterLayoutUiState dSTypeFilterLayoutUiState,
            final DsTypeWindowBuilder dsTypeWindowBuilder) {
        super(eventBus, i18n, uiNotification, permChecker);

        this.uiNotification = uiNotification;
        this.distributionSetTypeManagement = distributionSetTypeManagement;
        this.systemManagement = systemManagement;
        this.dSTypeFilterLayoutUiState = dSTypeFilterLayoutUiState;
        this.dsTypeWindowBuilder = dsTypeWindowBuilder;

        this.typeFilterButtonClickBehaviour = new TypeFilterButtonClick(this::publishFilterChangedEvent);
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
        return typeFilterButtonClickBehaviour;
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

            eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                    new DsTypeModifiedEventPayload(EntityModifiedEventType.ENTITY_REMOVED, dsTypeToDeleteId));
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
        final Window updateWindow = dsTypeWindowBuilder.getWindowForUpdateDsType(clickedFilter);

        updateWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.type")));
        UI.getCurrent().addWindow(updateWindow);
        updateWindow.setVisible(Boolean.TRUE);
    }
    // TODO
    // @Override
    // protected boolean isClickedByDefault(final Long filterButtonId) {
    // return dSTypeFilterLayoutUiState.getClickedDsType() != null
    // &&
    // dSTypeFilterLayoutUiState.getClickedDsType().getId().equals(filterButtonId);
    // }

    @Override
    protected String getFilterButtonIdPrefix() {
        return SPUIDefinitions.DISTRIBUTION_SET_TYPE_ID_PREFIXS;
    }
}
