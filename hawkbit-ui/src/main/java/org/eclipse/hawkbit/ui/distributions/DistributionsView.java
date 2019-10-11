/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.hawkbit.repository.ArtifactManagement;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.AbstractHawkbitUI;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.components.NotificationUnreadButton;
import org.eclipse.hawkbit.ui.dd.criteria.DistributionsViewClientCriterion;
import org.eclipse.hawkbit.ui.distributions.disttype.filter.DSTypeFilterLayout;
import org.eclipse.hawkbit.ui.distributions.dstable.DistributionSetGridLayout;
import org.eclipse.hawkbit.ui.distributions.smtable.SwModuleGridLayout;
import org.eclipse.hawkbit.ui.distributions.smtype.filter.DistSMTypeFilterLayout;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Manage distributions and distributions type view.
 */
@UIScope
@SpringView(name = DistributionsView.VIEW_NAME, ui = AbstractHawkbitUI.class)
public class DistributionsView extends VerticalLayout implements View, BrowserWindowResizeListener {
    private static final long serialVersionUID = 1L;

    public static final String VIEW_NAME = "distributions";

    private final SpPermissionChecker permChecker;
    private final ManageDistUIState manageDistUIState;

    private final DSTypeFilterLayout dsTypeFilterLayout;
    private final DistributionSetGridLayout distributionSetGridLayout;
    private final SwModuleGridLayout swModuleGridLayout;
    private final DistSMTypeFilterLayout distSMTypeFilterLayout;

    private GridLayout mainLayout;

    private final DistributionsViewEventListener eventListener;
    private final DistributionsViewRemoteEventListener remoteEventListener;

    @Autowired
    DistributionsView(final SpPermissionChecker permChecker, final UIEventBus eventBus, final VaadinMessageSource i18n,
            final UINotification uiNotification, final ManagementUIState managementUIState,
            final ManageDistUIState manageDistUIState, final SoftwareModuleManagement softwareModuleManagement,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement,
            final DistributionSetManagement distributionSetManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement, final TargetManagement targetManagement,
            final EntityFactory entityFactory, final DistributionSetTagManagement distributionSetTagManagement,
            final DistributionsViewClientCriterion distributionsViewClientCriterion,
            final ArtifactUploadState artifactUploadState, final SystemManagement systemManagement,
            final ArtifactManagement artifactManagement, final NotificationUnreadButton notificationUnreadButton,
            final DistributionsViewMenuItem distributionsViewMenuItem,
            final TenantConfigurationManagement configManagement, final SystemSecurityContext systemSecurityContext) {
        this.permChecker = permChecker;
        this.manageDistUIState = manageDistUIState;

        if (permChecker.hasReadRepositoryPermission()) {
            this.dsTypeFilterLayout = new DSTypeFilterLayout(i18n, permChecker, eventBus, entityFactory, uiNotification,
                    softwareModuleTypeManagement, distributionSetTypeManagement, distributionSetManagement,
                    systemManagement, manageDistUIState.getDSTypeFilterLayoutUiState());
            this.distributionSetGridLayout = new DistributionSetGridLayout(i18n, eventBus, permChecker,
                    distributionSetManagement, distributionSetTypeManagement, targetManagement, entityFactory,
                    uiNotification, distributionSetTagManagement, systemManagement, configManagement,
                    systemSecurityContext, manageDistUIState.getDSTypeFilterLayoutUiState(),
                    manageDistUIState.getDistributionSetGridLayoutUiState());
            this.swModuleGridLayout = new SwModuleGridLayout(i18n, uiNotification, eventBus, softwareModuleManagement,
                    softwareModuleTypeManagement, entityFactory, permChecker, artifactUploadState, artifactManagement,
                    manageDistUIState.getDistSMTypeFilterLayoutUiState(),
                    manageDistUIState.getSwModuleGridLayoutUiState());
            this.distSMTypeFilterLayout = new DistSMTypeFilterLayout(eventBus, i18n, permChecker, entityFactory,
                    uiNotification, softwareModuleTypeManagement, manageDistUIState.getDistSMTypeFilterLayoutUiState());
        } else {
            this.dsTypeFilterLayout = null;
            this.distributionSetGridLayout = null;
            this.swModuleGridLayout = null;
            this.distSMTypeFilterLayout = null;
        }

        this.eventListener = new DistributionsViewEventListener(this, eventBus);
        this.remoteEventListener = new DistributionsViewRemoteEventListener(eventBus, notificationUnreadButton);
    }

    @PostConstruct
    void init() {
        buildLayout();
        restoreState();
        Page.getCurrent().addBrowserWindowResizeListener(this);
        showOrHideFilterButtons(Page.getCurrent().getBrowserWindowWidth());
    }

    private void buildLayout() {
        if (permChecker.hasReadRepositoryPermission()) {
            setMargin(false);
            setSpacing(false);
            setSizeFull();

            createMainLayout();

            addComponent(mainLayout);
            setExpandRatio(mainLayout, 1.0F);
        }
    }

    private void createMainLayout() {
        mainLayout = new GridLayout(4, 1);
        mainLayout.setSizeFull();
        mainLayout.setMargin(false);
        mainLayout.setSpacing(true);
        mainLayout.setStyleName("fullSize");

        mainLayout.setRowExpandRatio(0, 1.0F);
        mainLayout.setColumnExpandRatio(1, 0.5F);
        mainLayout.setColumnExpandRatio(2, 0.5F);

        mainLayout.addComponent(dsTypeFilterLayout, 0, 0);
        mainLayout.addComponent(distributionSetGridLayout, 1, 0);
        mainLayout.addComponent(swModuleGridLayout, 2, 0);
        mainLayout.addComponent(distSMTypeFilterLayout, 3, 0);
    }

    private void restoreState() {
        // TODO: adapt
        if (manageDistUIState.getDistributionSetGridLayoutUiState().isMaximized()) {
            maximizeDsGridLayout();
        }
        if (manageDistUIState.getSwModuleGridLayoutUiState().isMaximized()) {
            maximizeSmGridLayout();
        }
    }

    @Override
    public void browserWindowResized(final BrowserWindowResizeEvent event) {
        showOrHideFilterButtons(event.getWidth());
    }

    private void showOrHideFilterButtons(final int browserWidth) {
        if (browserWidth < SPUIDefinitions.REQ_MIN_BROWSER_WIDTH) {
            if (!manageDistUIState.getDSTypeFilterLayoutUiState().isHidden()) {
                hideDsTypeLayout();
            }

            if (!manageDistUIState.getDistSMTypeFilterLayoutUiState().isHidden()) {
                hideSmTypeLayout();
            }
        } else {
            if (manageDistUIState.getDSTypeFilterLayoutUiState().isHidden()) {
                showDsTypeLayout();
            }

            if (manageDistUIState.getDistSMTypeFilterLayoutUiState().isHidden()) {
                showSmTypeLayout();
            }
        }
    }

    // TODO: move to grid layout restore state
    @Override
    public void enter(final ViewChangeEvent event) {
        if (permChecker.hasReadRepositoryPermission()) {
            final Long lastSelectedDsId = manageDistUIState.getDistributionSetGridLayoutUiState().getSelectedDsId();
            if (lastSelectedDsId != null) {
                final ProxyDistributionSet dsToSelect = new ProxyDistributionSet();
                dsToSelect.setId(lastSelectedDsId);

                distributionSetGridLayout.getDistributionSetGrid().select(dsToSelect);
            }

            final Long lastSelectedSmId = manageDistUIState.getSwModuleGridLayoutUiState().getSelectedSmId();
            if (lastSelectedSmId != null) {
                final ProxySoftwareModule smToSelect = new ProxySoftwareModule();
                smToSelect.setId(lastSelectedSmId);

                swModuleGridLayout.getSwModuleGrid().select(smToSelect);
            }
        }
    }

    void onDsSelected(final ProxyDistributionSet ds) {
        swModuleGridLayout.onDsSelected(ds);
    }

    void hideDsTypeLayout() {
        dsTypeFilterLayout.setVisible(false);
        distributionSetGridLayout.showDsTypeHeaderIcon();
    }

    void hideSmTypeLayout() {
        distSMTypeFilterLayout.setVisible(false);
        swModuleGridLayout.showSmTypeHeaderIcon();
    }

    void showDsTypeLayout() {
        dsTypeFilterLayout.setVisible(true);
        distributionSetGridLayout.hideDsTypeHeaderIcon();
    }

    void showSmTypeLayout() {
        distSMTypeFilterLayout.setVisible(true);
        swModuleGridLayout.hideSmTypeHeaderIcon();
    }

    void maximizeDsGridLayout() {
        mainLayout.removeComponent(swModuleGridLayout);
        mainLayout.removeComponent(distSMTypeFilterLayout);
        mainLayout.setColumnExpandRatio(2, 0F);
        mainLayout.setColumnExpandRatio(3, 0F);

        distributionSetGridLayout.maximize();
    }

    void maximizeSmGridLayout() {
        mainLayout.removeComponent(dsTypeFilterLayout);
        mainLayout.removeComponent(distributionSetGridLayout);
        mainLayout.setColumnExpandRatio(2, 1F);
        mainLayout.setColumnExpandRatio(0, 0F);
        mainLayout.setColumnExpandRatio(1, 0F);

        swModuleGridLayout.maximize();
    }

    void minimizeDsGridLayout() {
        mainLayout.addComponent(swModuleGridLayout, 2, 0);
        mainLayout.addComponent(distSMTypeFilterLayout, 3, 0);
        mainLayout.setColumnExpandRatio(1, 0.5F);
        mainLayout.setColumnExpandRatio(2, 0.5F);

        distributionSetGridLayout.minimize();
    }

    void minimizeSmGridLayout() {
        mainLayout.addComponent(dsTypeFilterLayout, 0, 0);
        mainLayout.addComponent(distributionSetGridLayout, 1, 0);
        mainLayout.setColumnExpandRatio(1, 0.5F);
        mainLayout.setColumnExpandRatio(2, 0.5F);

        swModuleGridLayout.minimize();
    }

    void filterDsGridByType(final DistributionSetType typeFilter) {
        distributionSetGridLayout.filterGridByType(typeFilter);
    }

    void filterSmGridByType(final SoftwareModuleType typeFilter) {
        swModuleGridLayout.filterGridByType(typeFilter);
    }

    @PreDestroy
    void destroy() {
        dsTypeFilterLayout.unsubscribeListener();
        distributionSetGridLayout.unsubscribeListener();
        swModuleGridLayout.unsubscribeListener();
        distSMTypeFilterLayout.unsubscribeListener();

        eventListener.unsubscribeListeners();
        remoteEventListener.unsubscribeListeners();
    }
}
