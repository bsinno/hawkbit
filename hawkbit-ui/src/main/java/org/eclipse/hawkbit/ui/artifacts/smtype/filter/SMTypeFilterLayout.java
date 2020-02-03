/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtype.filter;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.smtype.SmTypeWindowBuilder;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterLayout;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.ComponentContainer;

/**
 * Software module type filter buttons layout.
 */
public class SMTypeFilterLayout extends AbstractFilterLayout {
    private static final long serialVersionUID = 1L;

    private final SMTypeFilterHeader smTypeFilterHeader;
    private final SMTypeFilterButtons sMTypeFilterButtons;

    private final transient SMTypeFilterLayoutEventListener eventListener;

    /**
     * Constructor
     * 
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
     * @param smTypeFilterLayoutUiState
     *            SMTypeFilterLayoutUiState
     */
    public SMTypeFilterLayout(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final EntityFactory entityFactory, final UINotification uiNotification,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement,
            final SMTypeFilterLayoutUiState smTypeFilterLayoutUiState) {
        final SmTypeWindowBuilder smTypeWindowBuilder = new SmTypeWindowBuilder(i18n, entityFactory, eventBus,
                uiNotification, softwareModuleTypeManagement);

        this.smTypeFilterHeader = new SMTypeFilterHeader(i18n, permChecker, eventBus, smTypeFilterLayoutUiState,
                smTypeWindowBuilder);
        this.sMTypeFilterButtons = new SMTypeFilterButtons(eventBus, smTypeFilterLayoutUiState,
                softwareModuleTypeManagement, i18n, permChecker, uiNotification, smTypeWindowBuilder);

        this.eventListener = new SMTypeFilterLayoutEventListener(this, eventBus);

        buildLayout();
    }

    @Override
    protected SMTypeFilterHeader getFilterHeader() {
        return smTypeFilterHeader;
    }

    @Override
    protected ComponentContainer getFilterContent() {
        return wrapFilterContent(sMTypeFilterButtons);
    }

    public void restoreState() {
        sMTypeFilterButtons.restoreState();
    }

    public void showFilterButtonsEditIcon() {
        sMTypeFilterButtons.showEditColumn();
    }

    public void showFilterButtonsDeleteIcon() {
        sMTypeFilterButtons.showDeleteColumn();
    }

    public void hideFilterButtonsActionIcons() {
        sMTypeFilterButtons.hideActionColumns();
    }

    public void refreshFilterButtons() {
        sMTypeFilterButtons.refreshContainer();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }
}
