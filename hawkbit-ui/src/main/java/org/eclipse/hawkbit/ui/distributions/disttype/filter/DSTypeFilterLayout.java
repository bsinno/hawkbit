/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.disttype.filter;

import java.util.Collections;
import java.util.List;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterLayout;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener.EntityModifiedAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.support.EntityModifiedGridRefreshAwareSupport;
import org.eclipse.hawkbit.ui.distributions.disttype.DsTypeWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.ComponentContainer;

/**
 * Distribution Set Type filter buttons layout.
 */
public class DSTypeFilterLayout extends AbstractFilterLayout {
    private static final long serialVersionUID = 1L;

    private final DSTypeFilterHeader dsTypeFilterHeader;
    private final DSTypeFilterButtons dSTypeFilterButtons;

    private final transient DSTypeFilterLayoutEventListener eventListener;

    private final transient EntityModifiedListener<ProxyType> entityModifiedListener;

    /**
     * Constructor
     * 
     * @param i18n
     *            VaadinMessageSource
     * @param permChecker
     *            SpPermissionChecker
     * @param eventBus
     *            UIEventBus
     * @param entityFactory
     *            EntityFactory
     * @param uiNotification
     *            UINotification
     * @param softwareModuleTypeManagement
     *            SoftwareModuleTypeManagement
     * @param distributionSetTypeManagement
     *            DistributionSetTypeManagement
     */
    public DSTypeFilterLayout(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final EntityFactory entityFactory, final UINotification uiNotification,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement,
            final DistributionSetManagement distributionSetManagement, final SystemManagement systemManagement,
            final DSTypeFilterLayoutUiState dSTypeFilterLayoutUiState) {
        final DsTypeWindowBuilder dsTypeWindowBuilder = new DsTypeWindowBuilder(i18n, entityFactory, eventBus,
                uiNotification, distributionSetTypeManagement, distributionSetManagement, softwareModuleTypeManagement);

        this.dsTypeFilterHeader = new DSTypeFilterHeader(i18n, permChecker, eventBus, dSTypeFilterLayoutUiState,
                dsTypeWindowBuilder);
        this.dSTypeFilterButtons = new DSTypeFilterButtons(eventBus, distributionSetTypeManagement, i18n, permChecker,
                uiNotification, systemManagement, dSTypeFilterLayoutUiState, dsTypeWindowBuilder);

        this.eventListener = new DSTypeFilterLayoutEventListener(this, eventBus);

        this.entityModifiedListener = new EntityModifiedListener.Builder<>(eventBus, ProxyType.class)
                .entityModifiedAwareSupports(getEntityModifiedAwareSupports())
                .parentEntityType(ProxyDistributionSet.class).build();

        buildLayout();
    }

    private List<EntityModifiedAwareSupport> getEntityModifiedAwareSupports() {
        return Collections
                .singletonList(EntityModifiedGridRefreshAwareSupport.of(dSTypeFilterButtons::refreshContainer));
    }

    @Override
    protected DSTypeFilterHeader getFilterHeader() {
        return dsTypeFilterHeader;
    }

    @Override
    protected ComponentContainer getFilterContent() {
        return wrapFilterContent(dSTypeFilterButtons);
    }

    public void restoreState() {
        dSTypeFilterButtons.restoreState();
    }

    public void showFilterButtonsEditIcon() {
        dSTypeFilterButtons.showEditColumn();
    }

    public void showFilterButtonsDeleteIcon() {
        dSTypeFilterButtons.showDeleteColumn();
    }

    public void hideFilterButtonsActionIcons() {
        dSTypeFilterButtons.hideActionColumns();
    }

    public void refreshFilterButtons() {
        dSTypeFilterButtons.refreshContainer();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();

        entityModifiedListener.unsubscribe();
    }

    public EventLayout getLayout() {
        return EventLayout.DS_TYPE_FILTER;
    }
}
