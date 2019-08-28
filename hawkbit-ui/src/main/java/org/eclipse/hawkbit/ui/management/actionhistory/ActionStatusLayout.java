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
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyActionStatus;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.grid.DefaultGridHeader;
import org.eclipse.hawkbit.ui.common.grid.support.MasterDetailsSupport;
import org.eclipse.hawkbit.ui.common.grid.support.MasterDetailsSupportIdentifiable;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Layout responsible for action-states-grid and the corresponding header.
 */
public class ActionStatusLayout extends AbstractGridComponentLayout<ProxyActionStatus> {
    private static final long serialVersionUID = 1L;

    private final DefaultGridHeader actionStatusGridHeader;
    private final ActionStatusGrid actionStatusGrid;

    private final MasterDetailsSupport<ProxyAction, Long> masterDetailsSupport;

    /**
     * Constructor.
     *
     * @param i18n
     * @param eventBus
     * @param managementUIState
     */
    public ActionStatusLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final ManagementUIState managementUIState, final DeploymentManagement deploymentManagement) {
        super(i18n, eventBus);

        this.actionStatusGridHeader = new DefaultGridHeader(getI18n().getMessage("caption.action.states"), getI18n())
                .init();
        this.actionStatusGrid = new ActionStatusGrid(getI18n(), getEventBus(), deploymentManagement);

        this.masterDetailsSupport = new MasterDetailsSupportIdentifiable<>(actionStatusGrid);

        init();
    }

    @Override
    protected boolean doSubscribeToEventBus() {
        return false;
    }

    @Override
    public DefaultGridHeader getGridHeader() {
        return actionStatusGridHeader;
    }

    @Override
    public ActionStatusGrid getGrid() {
        return actionStatusGrid;
    }

    public MasterDetailsSupport<ProxyAction, Long> getMasterDetailsSupport() {
        return masterDetailsSupport;
    }
}
