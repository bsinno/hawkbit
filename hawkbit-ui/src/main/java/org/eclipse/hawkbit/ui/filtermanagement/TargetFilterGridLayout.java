/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.layout.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.layout.listener.SearchFilterListener;
import org.eclipse.hawkbit.ui.filtermanagement.event.TargetFilterGridLayoutEventListener;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * TargetFilter table layout.
 */
public class TargetFilterGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final TargetFilterGridHeader targetFilterGridHeader;
    private final TargetFilterGrid targetFilterGrid;

    private final transient TargetFilterGridLayoutEventListener eventListener;

    private final transient SearchFilterListener searchFilterListener;

    /**
     * TargetFilterGridLayout constructor
     * 
     * @param i18n
     *            MessageSource
     * @param eventBus
     *            Bus to publish UI events
     * @param permissionChecker
     *            Checker for user permissions
     * @param notification
     *            helper to display messages
     * @param entityFactory
     *            entity factory
     * @param targetFilterQueryManagement
     *            management to CRUD target filters
     * @param targetManagement
     *            management to get targets matching the filters
     * @param distributionSetManagement
     *            management to get distribution sets for auto-assignment
     * @param filterManagementUIState
     *            to persist the user interaction
     */
    public TargetFilterGridLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final EntityFactory entityFactory, final TargetFilterQueryManagement targetFilterQueryManagement,
            final TargetManagement targetManagement, final DistributionSetManagement distributionSetManagement,
            final FilterManagementUIState filterManagementUIState) {
        this.targetFilterGridHeader = new TargetFilterGridHeader(eventBus,
                filterManagementUIState.getGridLayoutUiState(), permissionChecker, i18n);

        final AutoAssignmentWindowBuilder autoAssignmentWindowBuilder = new AutoAssignmentWindowBuilder(i18n, eventBus,
                notification, entityFactory, targetManagement, targetFilterQueryManagement, distributionSetManagement);

        this.targetFilterGrid = new TargetFilterGrid(i18n, notification, eventBus,
                filterManagementUIState.getGridLayoutUiState(), targetFilterQueryManagement, permissionChecker,
                autoAssignmentWindowBuilder);

        this.eventListener = new TargetFilterGridLayoutEventListener(this, eventBus);

        this.searchFilterListener = new SearchFilterListener(eventBus, this::filterGridByName, getView(), getLayout());

        buildLayout(targetFilterGridHeader, targetFilterGrid);
    }

    /**
     * Only display filters with matching name
     * 
     * @param namePart
     *            filters containing this string in the name are displayed
     */
    public void filterGridByName(final String namePart) {
        targetFilterGrid.setFilter(namePart);
    }

    /**
     * Reload the data shown by the grid. Call this when a resource changed.
     */
    public void refreshGrid() {
        targetFilterGrid.getFilterDataProvider().refreshAll();
    }

    /**
     * restore the saved state
     */
    public void restoreState() {
        targetFilterGridHeader.restoreState();
        targetFilterGrid.restoreState();
    }

    /**
     * unsubscribe all listener
     */
    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();

        searchFilterListener.unsubscribe();
    }

    public Layout getLayout() {
        return Layout.TARGET_FILTER_QUERY_LIST;
    }

    public View getView() {
        return View.TARGET_FILTER;
    }
}
