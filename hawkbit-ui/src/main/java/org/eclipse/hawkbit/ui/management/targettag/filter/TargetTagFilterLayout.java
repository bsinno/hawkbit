/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag.filter;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterLayout;
import org.eclipse.hawkbit.ui.management.ManagementUIState;
import org.eclipse.hawkbit.ui.management.targettag.TargetTagWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.ComponentContainer;

/**
 * Target Tag filter layout.
 */
public class TargetTagFilterLayout extends AbstractFilterLayout {
    private static final long serialVersionUID = 1L;

    private final TargetTagFilterHeader targetTagFilterHeader;
    private final MultipleTargetFilter multipleTargetFilter;

    private final transient TargetTagFilterLayoutEventListener eventListener;

    /**
     * Constructor
     * 
     * @param i18n
     *            VaadinMessageSource
     * @param managementUIState
     *            ManagementUIState
     * @param permChecker
     *            SpPermissionChecker
     * @param eventBus
     *            UIEventBus
     * @param notification
     *            UINotification
     * @param entityFactory
     *            EntityFactory
     * @param targetFilterQueryManagement
     *            TargetFilterQueryManagement
     * @param targetTagManagement
     *            TargetTagManagement
     */
    public TargetTagFilterLayout(final VaadinMessageSource i18n, final ManagementUIState managementUIState,
            final SpPermissionChecker permChecker, final UIEventBus eventBus, final UINotification notification,
            final EntityFactory entityFactory, final TargetFilterQueryManagement targetFilterQueryManagement,
            final TargetTagManagement targetTagManagement, final TargetManagement targetManagement,
            final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState) {
        final TargetTagWindowBuilder targetTagWindowBuilder = new TargetTagWindowBuilder(i18n, entityFactory, eventBus,
                notification, targetTagManagement);

        this.targetTagFilterHeader = new TargetTagFilterHeader(i18n, permChecker, eventBus,
                targetTagFilterLayoutUiState, targetTagWindowBuilder);
        this.multipleTargetFilter = new MultipleTargetFilter(permChecker, i18n, eventBus, notification,
                targetFilterQueryManagement, targetTagManagement, targetManagement, targetTagFilterLayoutUiState,
                targetTagWindowBuilder);

        this.eventListener = new TargetTagFilterLayoutEventListener(this, eventBus);

        buildLayout();
    }

    @Override
    protected TargetTagFilterHeader getFilterHeader() {
        return targetTagFilterHeader;
    }

    @Override
    protected ComponentContainer getFilterContent() {
        return multipleTargetFilter;
    }

    public void showFilterButtonsEditIcon() {
        multipleTargetFilter.getTargetTagFilterButtons().showEditColumn();
    }

    public void showFilterButtonsDeleteIcon() {
        multipleTargetFilter.getTargetTagFilterButtons().showDeleteColumn();
    }

    public void hideFilterButtonsActionIcons() {
        multipleTargetFilter.getTargetTagFilterButtons().hideActionColumns();
    }

    public void refreshFilterButtons() {
        multipleTargetFilter.getTargetTagFilterButtons().refreshContainer();
    }

    void onTargetFilterTabChanged(final boolean isCustomFilterTabSelected) {
        if (isCustomFilterTabSelected) {
            targetTagFilterHeader.disableCrudMenu();
        } else {
            targetTagFilterHeader.enableCrudMenu();
        }
    }

    public void restoreState() {
        targetTagFilterHeader.restoreState();
        multipleTargetFilter.restoreState();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }
}
