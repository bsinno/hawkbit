/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.disttype.filter;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterLayout;
import org.eclipse.hawkbit.ui.distributions.disttype.DsTypeWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * Distribution Set Type filter buttons layout.
 */
public class DSTypeFilterLayout extends AbstractFilterLayout {

    private static final long serialVersionUID = 1L;

    private final DSTypeFilterHeader dsTypeFilterHeader;
    private final DSTypeFilterButtons dSTypeFilterButtons;

    private final DSTypeFilterLayoutEventListener eventListener;

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
        super(eventBus);

        final DsTypeWindowBuilder dsTypeWindowBuilder = new DsTypeWindowBuilder(i18n, entityFactory, eventBus,
                uiNotification, distributionSetTypeManagement, distributionSetManagement, softwareModuleTypeManagement);

        this.dsTypeFilterHeader = new DSTypeFilterHeader(i18n, permChecker, eventBus, dSTypeFilterLayoutUiState,
                dsTypeWindowBuilder);
        this.dSTypeFilterButtons = new DSTypeFilterButtons(eventBus, distributionSetTypeManagement, i18n, permChecker,
                uiNotification, systemManagement, dSTypeFilterLayoutUiState, dsTypeWindowBuilder);

        this.eventListener = new DSTypeFilterLayoutEventListener(this, eventBus);

        buildLayout();
        restoreState();
    }

    @Override
    protected DSTypeFilterHeader getFilterHeader() {
        return dsTypeFilterHeader;
    }

    // TODO: remove duplication with other type layouts
    @Override
    protected Component getFilterButtons() {
        final VerticalLayout filterButtonsLayout = new VerticalLayout();
        filterButtonsLayout.setMargin(false);
        filterButtonsLayout.setSpacing(false);

        filterButtonsLayout.addComponent(dSTypeFilterButtons);
        filterButtonsLayout.setComponentAlignment(dSTypeFilterButtons, Alignment.TOP_LEFT);
        filterButtonsLayout.setExpandRatio(dSTypeFilterButtons, 1.0F);

        return filterButtonsLayout;
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
    }
}
