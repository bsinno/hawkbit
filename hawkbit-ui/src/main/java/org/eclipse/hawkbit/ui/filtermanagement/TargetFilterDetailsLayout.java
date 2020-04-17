/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.repository.rsql.RsqlValidationOracle;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.layout.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.layout.listener.SearchFilterListener;
import org.eclipse.hawkbit.ui.filtermanagement.event.TargetFilterDetailsLayoutEventListener;
import org.eclipse.hawkbit.ui.filtermanagement.state.TargetFilterDetailsLayoutUiState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * DistributionSet table layout.
 */
public class TargetFilterDetailsLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final TargetFilterDetailsGridHeader targetFilterDetailsGridHeader;
    private final TargetFilterTargetGrid targetFilterTargetGrid;
    private final transient TargetFilterCountMessageLabel targetFilterCountMessageLabel;

    private final transient TargetFilterDetailsLayoutEventListener eventListener;

    private final transient SearchFilterListener searchFilterListener;

    /**
     * TargetFilterDetailsLayout constructor
     * 
     * @param i18n
     *            MessageSource
     * @param eventBus
     *            Bus to publish UI events
     * @param uiNotification
     *            helper to display messages
     * @param uiProperties
     *            properties
     * @param entityFactory
     *            entity factory
     * @param rsqlValidationOracle
     *            to get RSQL validation and suggestions
     * @param targetManagement
     *            management to get targets matching the filters
     * @param targetFilterManagement
     *            management to CRUD target filters
     * @param uiState
     *            to persist the user interaction
     */
    public TargetFilterDetailsLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final UINotification uiNotification, final UiProperties uiProperties, final EntityFactory entityFactory,
            final RsqlValidationOracle rsqlValidationOracle, final TargetManagement targetManagement,
            final TargetFilterQueryManagement targetFilterManagement, final TargetFilterDetailsLayoutUiState uiState) {

        this.targetFilterDetailsGridHeader = new TargetFilterDetailsGridHeader(i18n, eventBus, uiNotification,
                entityFactory, targetFilterManagement, uiProperties, rsqlValidationOracle, uiState);
        this.targetFilterTargetGrid = new TargetFilterTargetGrid(i18n, eventBus, targetManagement, uiState);
        this.targetFilterCountMessageLabel = new TargetFilterCountMessageLabel(i18n);

        initGridDataUpdatedListener();

        this.eventListener = new TargetFilterDetailsLayoutEventListener(this, eventBus);

        this.searchFilterListener = new SearchFilterListener(eventBus, this::filterGridByQuery, getView(), getLayout());

        buildLayout(targetFilterDetailsGridHeader, targetFilterTargetGrid, targetFilterCountMessageLabel);
    }

    private void initGridDataUpdatedListener() {
        targetFilterTargetGrid.getFilterDataProvider().addDataProviderListener(event -> targetFilterCountMessageLabel
                .updateTotalFilteredTargetsCount(targetFilterTargetGrid.getDataSize()));
    }

    /**
     * Change UI content to create a {@link TargetFilterQuery}
     */
    public void showAddFilterUi() {
        targetFilterDetailsGridHeader.showAddFilterLayout();
        targetFilterTargetGrid.updateTargetFilterQueryFilter(null);
    }

    /**
     * Change UI content to modify a {@link TargetFilterQuery}
     * 
     * @param proxyEntity
     *            the filter to modify
     * 
     */
    public void showEditFilterUi(final ProxyTargetFilterQuery proxyEntity) {
        targetFilterDetailsGridHeader.showEditFilterLayout(proxyEntity);
        targetFilterTargetGrid.updateTargetFilterQueryFilter(proxyEntity.getQuery());
    }

    /**
     * Update the grid with {@link Target}s that match a query
     * 
     * @param newFilterquery
     *            the RSQL query to match the targets against
     */
    public void filterGridByQuery(final String newFilterquery) {
        targetFilterTargetGrid.updateTargetFilterQueryFilter(newFilterquery);
    }

    /**
     * restore the saved state
     */
    public void restoreState() {
        targetFilterDetailsGridHeader.restoreState();
        targetFilterTargetGrid.restoreState();
    }

    /**
     * unsubscribe all listener
     */
    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();

        searchFilterListener.unsubscribe();
    }

    public Layout getLayout() {
        return Layout.TARGET_FILTER_QUERY_FORM;
    }

    public View getView() {
        return View.TARGET_FILTER;
    }
}
