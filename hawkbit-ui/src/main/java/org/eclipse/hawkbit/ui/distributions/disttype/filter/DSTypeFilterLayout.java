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
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterHeader;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterLayout;
import org.eclipse.hawkbit.ui.distributions.event.DistributionsUIEvent;
import org.eclipse.hawkbit.ui.distributions.state.ManageDistUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.Component;

/**
 * Distribution Set Type filter buttons layout.
 */
public class DSTypeFilterLayout extends AbstractFilterLayout {

    private static final long serialVersionUID = 1L;

    private final ManageDistUIState manageDistUIState;

    private final DSTypeFilterHeader dsTypeFilterHeader;
    private final DSTypeFilterButtons dSTypeFilterButtons;

    /**
     * Constructor
     * 
     * @param manageDistUIState
     *            ManageDistUIState
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
    public DSTypeFilterLayout(final ManageDistUIState manageDistUIState, final VaadinMessageSource i18n,
            final SpPermissionChecker permChecker, final UIEventBus eventBus, final EntityFactory entityFactory,
            final UINotification uiNotification, final SoftwareModuleTypeManagement softwareModuleTypeManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement,
            final DistributionSetManagement distributionSetManagement, final SystemManagement systemManagement) {
        this.manageDistUIState = manageDistUIState;

        this.dSTypeFilterButtons = new DSTypeFilterButtons(eventBus, manageDistUIState, distributionSetTypeManagement,
                i18n, entityFactory, permChecker, uiNotification, softwareModuleTypeManagement,
                distributionSetManagement, systemManagement);
        this.dsTypeFilterHeader = new DSTypeFilterHeader(i18n, permChecker, eventBus, manageDistUIState, entityFactory,
                uiNotification, softwareModuleTypeManagement, distributionSetTypeManagement, dSTypeFilterButtons);

        buildLayout();

        restoreState();
    }

    @Override
    protected AbstractFilterHeader getFilterHeader() {
        return dsTypeFilterHeader;
    }

    @Override
    protected Component getFilterButtons() {
        return dSTypeFilterButtons;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final DistributionsUIEvent event) {
        if (event == DistributionsUIEvent.HIDE_DIST_FILTER_BY_TYPE) {
            setVisible(false);
        }
        if (event == DistributionsUIEvent.SHOW_DIST_FILTER_BY_TYPE) {
            setVisible(true);
        }
    }

    @Override
    public Boolean isTypeFilterClosedOnLoad() {
        return manageDistUIState.isDistTypeFilterClosed();
    }
}
