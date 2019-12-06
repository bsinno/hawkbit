/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag.filter;

import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterSingleButtonClick;
import org.eclipse.hawkbit.ui.management.event.TargetFilterEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Single button click behaviour of custom target filter buttons layout.
 *
 */
public class CustomTargetTagFilterButtonClick extends AbstractFilterSingleButtonClick<ProxyTargetFilterQuery> {

    private static final long serialVersionUID = 1L;

    private final transient EventBus.UIEventBus eventBus;

    private final ManagementUIState managementUIState;

    private final transient TargetFilterQueryManagement targetFilterQueryManagement;

    /**
     * Constructor
     * 
     * @param eventBus
     *            UIEventBus
     * @param managementUIState
     *            ManagementUIState
     * @param targetFilterQueryManagement
     *            TargetFilterQueryManagement
     */
    public CustomTargetTagFilterButtonClick(final UIEventBus eventBus, final ManagementUIState managementUIState,
            final TargetFilterQueryManagement targetFilterQueryManagement) {
        this.eventBus = eventBus;
        this.managementUIState = managementUIState;
        this.targetFilterQueryManagement = targetFilterQueryManagement;
    }

    @Override
    protected void filterUnClicked(final ProxyTargetFilterQuery clickedFilter) {
        this.managementUIState.getTargetTableFilters().setTargetFilterQuery(null);
        this.eventBus.publish(this, TargetFilterEvent.REMOVE_FILTER_BY_TARGET_FILTER_QUERY);
    }

    @Override
    protected void filterClicked(final ProxyTargetFilterQuery clickedFilter) {
        // TODO: check if we need to make the database call here
        targetFilterQueryManagement.get(clickedFilter.getId()).ifPresent(targetFilterQuery -> {
            this.managementUIState.getTargetTableFilters().setTargetFilterQuery(targetFilterQuery.getId());
            this.eventBus.publish(this, TargetFilterEvent.FILTER_BY_TARGET_FILTER_QUERY);
        });
    }

    protected void clearAppliedTargetFilterQuery() {
        // TODO
        // if (getAlreadyClickedButton() != null) {
        // getAlreadyClickedButton().removeStyleName(SPUIStyleDefinitions.SP_FILTER_BTN_CLICKED_STYLE);
        // setAlreadyClickedButton(null);
        // }
        this.managementUIState.getTargetTableFilters().setTargetFilterQuery(null);
        this.eventBus.publish(this, TargetFilterEvent.REMOVE_FILTER_BY_TARGET_FILTER_QUERY);
    }
}
