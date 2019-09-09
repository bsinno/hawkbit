/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.smtype.filter;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterLayout;
import org.eclipse.hawkbit.ui.distributions.event.DistributionsUIEvent;
import org.eclipse.hawkbit.ui.distributions.state.ManageDistUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * Software Module Type filter layout.
 */
public class DistSMTypeFilterLayout extends AbstractFilterLayout {

    private static final long serialVersionUID = 1L;

    private final ManageDistUIState manageDistUIState;

    private final DistSMTypeFilterHeader distSMTypeFilterHeader;
    private final DistSMTypeFilterButtons distSMTypeFilterButtons;

    /**
     * Constructor
     * 
     * @param eventBus
     *            UIEventBus
     * @param i18n
     *            VaadinMessageSource
     * @param permChecker
     *            SpPermissionChecker
     * @param manageDistUIState
     *            ManageDistUIState
     * @param entityFactory
     *            EntityFactory
     * @param uiNotification
     *            UINotification
     * @param softwareModuleTypeManagement
     *            SoftwareModuleTypeManagement
     */
    public DistSMTypeFilterLayout(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permChecker, final ManageDistUIState manageDistUIState,
            final EntityFactory entityFactory, final UINotification uiNotification,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement) {
        super(eventBus);

        this.manageDistUIState = manageDistUIState;

        this.distSMTypeFilterButtons = new DistSMTypeFilterButtons(eventBus, manageDistUIState,
                softwareModuleTypeManagement, i18n, entityFactory, permChecker, uiNotification);
        this.distSMTypeFilterHeader = new DistSMTypeFilterHeader(i18n, permChecker, eventBus, manageDistUIState,
                entityFactory, uiNotification, softwareModuleTypeManagement, distSMTypeFilterButtons);

        buildLayout();
        restoreState();
    }

    @Override
    protected DistSMTypeFilterHeader getFilterHeader() {
        return distSMTypeFilterHeader;
    }

    // TODO: remove duplication with other type layouts
    @Override
    protected Component getFilterButtons() {
        final VerticalLayout filterButtonsLayout = new VerticalLayout();
        filterButtonsLayout.setMargin(false);
        filterButtonsLayout.setSpacing(false);

        filterButtonsLayout.addComponent(distSMTypeFilterButtons);
        filterButtonsLayout.setComponentAlignment(distSMTypeFilterButtons, Alignment.TOP_LEFT);
        filterButtonsLayout.setExpandRatio(distSMTypeFilterButtons, 1.0F);

        return filterButtonsLayout;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final DistributionsUIEvent event) {
        if (event == DistributionsUIEvent.HIDE_SM_FILTER_BY_TYPE) {
            setVisible(false);
        }
        if (event == DistributionsUIEvent.SHOW_SM_FILTER_BY_TYPE) {
            setVisible(true);
        }
    }

    @Override
    public Boolean isFilterLayoutClosedOnLoad() {
        return manageDistUIState.isSwTypeFilterClosed();
    }
}
