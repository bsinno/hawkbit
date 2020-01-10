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
import org.eclipse.hawkbit.ui.common.event.ChangeUiElementPayload;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
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

    private final TargetFilterDetailsLayoutUiState uiState;

    private final TargetFilterDetailsGridHeader targetFilterDetailsGridHeader;
    private final TargetFilterTargetGrid targetFilterTargetGrid;
    private final transient TargetFilterCountMessageLabel targetFilterCountMessageLabel;

    private final transient UIEventBus eventBus;
    private final transient TargetFilterDetailsLayoutEventListener eventListener;

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
        this.uiState = uiState;
        this.eventBus = eventBus;
        this.eventListener = new TargetFilterDetailsLayoutEventListener(this, eventBus);

        final TargetFilterAddUpdateLayout targetFilterAddUpdateLayout = new TargetFilterAddUpdateLayout(i18n,
                uiProperties, uiState, eventBus, rsqlValidationOracle);
        final AddTargetFilterController addTargetFilterController = new AddTargetFilterController(i18n, entityFactory,
                eventBus, uiNotification, targetFilterManagement, targetFilterAddUpdateLayout);
        final UpdateTargetFilterController updateTargetFilterController = new UpdateTargetFilterController(i18n,
                entityFactory, eventBus, uiNotification, targetFilterManagement, targetFilterAddUpdateLayout);

        this.targetFilterDetailsGridHeader = new TargetFilterDetailsGridHeader(i18n, eventBus,
                targetFilterAddUpdateLayout, addTargetFilterController, updateTargetFilterController, uiState);

        this.targetFilterTargetGrid = new TargetFilterTargetGrid(i18n, eventBus, targetManagement, uiState);

        this.targetFilterCountMessageLabel = new TargetFilterCountMessageLabel(i18n);

        buildLayout(targetFilterDetailsGridHeader, targetFilterTargetGrid, targetFilterCountMessageLabel);
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
        uiState.setTargetFilterQueryforEdit(proxyEntity);
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
     * Update number of targets footer
     * 
     * @param totalTargetCount
     *            number to display
     */
    public void setFilteredTargetsCount(final long totalTargetCount) {
        targetFilterCountMessageLabel.updateTotalFilteredTargetsCount(totalTargetCount);
    }

    /**
     * Publish an event that the request of closing this layout was received
     */
    public void sendCloseRequestedEvent() {
        // TODO is it OK to just republish the Event so it comes from this
        // class? Maybe put this layout in the original event to avoid the need
        // of republishing
        eventBus.publish(EventTopics.CHANGE_UI_ELEMENT_STATE, this, ChangeUiElementPayload.CLOSE);
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
    }
}
