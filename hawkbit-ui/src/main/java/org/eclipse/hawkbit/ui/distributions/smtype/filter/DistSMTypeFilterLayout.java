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
import org.eclipse.hawkbit.ui.artifacts.smtype.SmTypeWindowBuilder;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterLayout;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * Software Module Type filter layout.
 */
public class DistSMTypeFilterLayout extends AbstractFilterLayout {

    private static final long serialVersionUID = 1L;

    private final DistSMTypeFilterHeader distSMTypeFilterHeader;
    private final DistSMTypeFilterButtons distSMTypeFilterButtons;

    private final DistSMTypeFilterLayoutEventListener eventListener;

    /**
     * Constructor
     * 
     * @param eventBus
     *            UIEventBus
     * @param i18n
     *            VaadinMessageSource
     * @param permChecker
     *            SpPermissionChecker
     * @param entityFactory
     *            EntityFactory
     * @param uiNotification
     *            UINotification
     * @param softwareModuleTypeManagement
     *            SoftwareModuleTypeManagement
     */
    public DistSMTypeFilterLayout(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permChecker, final EntityFactory entityFactory,
            final UINotification uiNotification, final SoftwareModuleTypeManagement softwareModuleTypeManagement,
            final DistSMTypeFilterLayoutUiState distSMTypeFilterLayoutUiState) {
        super(eventBus);

        final SmTypeWindowBuilder smTypeWindowBuilder = new SmTypeWindowBuilder(i18n, entityFactory, eventBus,
                uiNotification, softwareModuleTypeManagement);

        this.distSMTypeFilterHeader = new DistSMTypeFilterHeader(i18n, permChecker, eventBus,
                distSMTypeFilterLayoutUiState, smTypeWindowBuilder);
        this.distSMTypeFilterButtons = new DistSMTypeFilterButtons(eventBus, softwareModuleTypeManagement, i18n,
                entityFactory, permChecker, uiNotification, distSMTypeFilterLayoutUiState, smTypeWindowBuilder);

        this.eventListener = new DistSMTypeFilterLayoutEventListener(this, eventBus);

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

    public void showFilterButtonsEditIcon() {
        distSMTypeFilterButtons.showEditColumn();
    }

    public void showFilterButtonsDeleteIcon() {
        distSMTypeFilterButtons.showDeleteColumn();
    }

    public void hideFilterButtonsActionIcons() {
        distSMTypeFilterButtons.hideActionColumns();
    }

    public void refreshFilterButtons() {
        distSMTypeFilterButtons.refreshContainer();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }
}
