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
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.grid.support.MasterDetailsSupport;
import org.eclipse.hawkbit.ui.common.grid.support.MasterDetailsSupportIdentifiable;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Layout responsible for action-states-grid and the corresponding header.
 */
public class ActionStatusGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final ActionStatusGridHeader actionStatusGridHeader;
    private final ActionStatusGrid actionStatusGrid;

    private final MasterDetailsSupport<ProxyAction, Long> masterDetailsSupport;

    /**
     * Constructor.
     *
     * @param i18n
     * @param eventBus
     * @param managementUIState
     */
    public ActionStatusGridLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final ManagementUIState managementUIState, final DeploymentManagement deploymentManagement) {
        super(i18n, eventBus);

        this.actionStatusGridHeader = new ActionStatusGridHeader(i18n);
        this.actionStatusGrid = new ActionStatusGrid(i18n, eventBus, deploymentManagement, managementUIState);

        this.masterDetailsSupport = new MasterDetailsSupportIdentifiable<>(actionStatusGrid);

        buildLayout(actionStatusGridHeader, actionStatusGrid);
    }

    // TODO: check if it is correct
    @Override
    protected boolean doSubscribeToEventBus() {
        return false;
    }

    public MasterDetailsSupport<ProxyAction, Long> getMasterDetailsSupport() {
        return masterDetailsSupport;
    }
}
