/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.actionhistory;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyActionStatus;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.grid.support.MasterDetailsSupport;
import org.eclipse.hawkbit.ui.common.grid.support.MasterDetailsSupportIdentifiable;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Layout responsible for messages-grid and the corresponding header.
 */
public class ActionStatusMsgGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final ActionStatusMsgGridHeader actionStatusMsgHeader;
    private final ActionStatusMsgGrid actionStatusMsgGrid;

    private final MasterDetailsSupport<ProxyActionStatus, Long> masterDetailsSupport;

    /**
     * Constructor.
     *
     * @param i18n
     * @param eventBus
     * @param managementUIState
     * @param deploymentManagement
     */
    public ActionStatusMsgGridLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final ManagementUIState managementUIState, final DeploymentManagement deploymentManagement) {
        this.actionStatusMsgHeader = new ActionStatusMsgGridHeader(i18n);
        this.actionStatusMsgGrid = new ActionStatusMsgGrid(i18n, eventBus, deploymentManagement);

        this.masterDetailsSupport = new MasterDetailsSupportIdentifiable<>(actionStatusMsgGrid);

        buildLayout(actionStatusMsgHeader, actionStatusMsgGrid);
    }

    public MasterDetailsSupport<ProxyActionStatus, Long> getMasterDetailsSupport() {
        return masterDetailsSupport;
    }
}
