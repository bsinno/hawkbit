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

import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.smtype.filter.TypeFilterButtonClick;
import org.eclipse.hawkbit.ui.common.data.mappers.TypeToProxyTypeMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetTypeDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload.TypeFilterChangedEventType;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtonClickBehaviour;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtonClickBehaviour.ClickBehaviourType;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtons;
import org.eclipse.hawkbit.ui.distributions.disttype.DsTypeWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Distribution Set Type filter buttons.
 */
public class DSTypeFilterButtons extends AbstractFilterButtons<ProxyType, String> {
    private static final long serialVersionUID = 1L;

    private final DSTypeFilterLayoutUiState dSTypeFilterLayoutUiState;
    private final UINotification uiNotification;

    private final transient DistributionSetTypeManagement distributionSetTypeManagement;
    private final transient TypeFilterButtonClick typeFilterButtonClickBehaviour;
    private final transient DsTypeWindowBuilder dsTypeWindowBuilder;
    private final transient SystemManagement systemManagement;

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

        this.dSTypeFilterLayoutUiState = dSTypeFilterLayoutUiState;
        this.uiNotification = uiNotification;
        this.distributionSetTypeManagement = distributionSetTypeManagement;
        this.dsTypeWindowBuilder = dsTypeWindowBuilder;
        this.systemManagement = systemManagement;

        this.typeFilterButtonClickBehaviour = new TypeFilterButtonClick(this::publishFilterChangedEvent);
        this.dsTypeDataProvider = new DistributionSetTypeDataProvider(distributionSetTypeManagement,
                new TypeToProxyTypeMapper<>()).withConfigurableFilter();

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
        // TODO: use constant
        return i18n.getMessage("caption.entity.distribution.type");
    }

    @Override
    protected AbstractFilterButtonClickBehaviour<ProxyType> getFilterButtonClickBehaviour() {
        return typeFilterButtonClickBehaviour;
    }

    private void publishFilterChangedEvent(final ProxyType typeFilter, final ClickBehaviourType clickType) {
        distributionSetTypeManagement.getByName(typeFilter.getName()).ifPresent(dsType -> {
            // TODO: somehow move it to abstract class/TypeFilterButtonClick
            // needed to trigger style generator
            getDataCommunicator().reset();

            eventBus.publish(EventTopics.TYPE_FILTER_CHANGED, this,
                    new TypeFilterChangedEventPayload<DistributionSetType>(
                            ClickBehaviourType.CLICKED == clickType ? TypeFilterChangedEventType.TYPE_CLICKED
                                    : TypeFilterChangedEventType.TYPE_UNCLICKED,
                            dsType, EventLayout.DS_TYPE_FILTER, EventView.DISTRIBUTIONS));

            dSTypeFilterLayoutUiState
                    .setClickedDsTypeId(ClickBehaviourType.CLICKED == clickType ? dsType.getId() : null);
        });
    }

    @Override
    protected void deleteFilterButtons(final Collection<ProxyType> filterButtonsToDelete) {
        // we do not allow multiple deletion yet
        final ProxyType dsTypeToDelete = filterButtonsToDelete.iterator().next();
        final String dsTypeToDeleteName = dsTypeToDelete.getName();
        final Long dsTypeToDeleteId = dsTypeToDelete.getId();

        final Long clickedDsTypeId = dSTypeFilterLayoutUiState.getClickedDsTypeId();

        if (clickedDsTypeId != null && clickedDsTypeId.equals(dsTypeToDeleteId)) {
            uiNotification.displayValidationError(i18n.getMessage("message.tag.delete", dsTypeToDeleteName));
        } else if (isDefaultDsType(dsTypeToDeleteName)) {
            uiNotification.displayValidationError(i18n.getMessage("message.cannot.delete.default.dstype"));
        } else {
            distributionSetTypeManagement.delete(dsTypeToDeleteId);

            eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                    new EntityModifiedEventPayload(EntityModifiedEventType.ENTITY_REMOVED, ProxyDistributionSet.class,
                            ProxyType.class, dsTypeToDeleteId));
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

    @Override
    protected String getFilterButtonIdPrefix() {
        return UIComponentIdProvider.DISTRIBUTION_SET_TYPE_ID_PREFIXS;
    }

    public void restoreState() {
        final Long lastClickedTypeId = dSTypeFilterLayoutUiState.getClickedDsTypeId();

        if (lastClickedTypeId != null) {
            typeFilterButtonClickBehaviour.setPreviouslyClickedFilterId(lastClickedTypeId);
            // TODO: should we reset data communicator here for styling update
        }
    }
}
