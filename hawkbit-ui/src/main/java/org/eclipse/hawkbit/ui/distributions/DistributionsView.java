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
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.AbstractHawkbitUI;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.distributions.disttype.filter.DSTypeFilterLayout;
import org.eclipse.hawkbit.ui.distributions.dstable.DistributionSetGridLayout;
import org.eclipse.hawkbit.ui.distributions.smtable.SwModuleGridLayout;
import org.eclipse.hawkbit.ui.distributions.smtype.filter.DistSMTypeFilterLayout;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.annotations.JavaScript;
import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Manage distributions and distributions type view.
 */
@UIScope
@SpringView(name = DistributionsView.VIEW_NAME, ui = AbstractHawkbitUI.class)
@JavaScript("vaadin://js/dynamicStylesheet.js")
public class DistributionsView extends VerticalLayout implements View, BrowserWindowResizeListener {
    private static final long serialVersionUID = 1L;

    public static final String VIEW_NAME = "distributions";

    private final SpPermissionChecker permChecker;
    private final ManageDistUIState manageDistUIState;

    private final DSTypeFilterLayout dsTypeFilterLayout;
    private final DistributionSetGridLayout distributionSetGridLayout;
    private final SwModuleGridLayout swModuleGridLayout;
    private final DistSMTypeFilterLayout distSMTypeFilterLayout;

    private HorizontalLayout mainLayout;

    private final transient DistributionsViewEventListener eventListener;

    @Autowired
    DistributionsView(final SpPermissionChecker permChecker, final UIEventBus eventBus, final VaadinMessageSource i18n,
            final UINotification uiNotification, final ManageDistUIState manageDistUIState,
            final SoftwareModuleManagement softwareModuleManagement,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement,
            final DistributionSetManagement distributionSetManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement, final TargetManagement targetManagement,
            final EntityFactory entityFactory, final DistributionSetTagManagement distributionSetTagManagement,
            final TargetFilterQueryManagement targetFilterQueryManagement, final SystemManagement systemManagement,
            final ArtifactManagement artifactManagement, final TenantConfigurationManagement configManagement,
            final SystemSecurityContext systemSecurityContext) {
        this.permChecker = permChecker;
        this.manageDistUIState = manageDistUIState;

        if (permChecker.hasReadRepositoryPermission()) {
            this.dsTypeFilterLayout = new DSTypeFilterLayout(i18n, permChecker, eventBus, entityFactory, uiNotification,
                    softwareModuleTypeManagement, distributionSetTypeManagement, distributionSetManagement,
                    systemManagement, manageDistUIState.getDSTypeFilterLayoutUiState());
            this.distributionSetGridLayout = new DistributionSetGridLayout(i18n, eventBus, permChecker, uiNotification,
                    entityFactory, targetManagement, targetFilterQueryManagement, distributionSetManagement,
                    softwareModuleManagement, distributionSetTypeManagement, distributionSetTagManagement,
                    softwareModuleTypeManagement, systemManagement, configManagement, systemSecurityContext,
                    manageDistUIState.getDSTypeFilterLayoutUiState(),
                    manageDistUIState.getDistributionSetGridLayoutUiState());
            this.swModuleGridLayout = new SwModuleGridLayout(i18n, uiNotification, eventBus, softwareModuleManagement,
                    softwareModuleTypeManagement, entityFactory, permChecker, artifactManagement,
                    manageDistUIState.getDistSMTypeFilterLayoutUiState(),
                    manageDistUIState.getSwModuleGridLayoutUiState());
            this.distSMTypeFilterLayout = new DistSMTypeFilterLayout(eventBus, i18n, permChecker, entityFactory,
                    uiNotification, softwareModuleTypeManagement, manageDistUIState.getDistSMTypeFilterLayoutUiState());
            this.eventListener = new DistributionsViewEventListener(this, eventBus);
        } else {
            this.dsTypeFilterLayout = null;
            this.distributionSetGridLayout = null;
            this.swModuleGridLayout = null;
            this.distSMTypeFilterLayout = null;
            this.eventListener = null;
        }
    }

    @PostConstruct
    void init() {
        if (permChecker.hasReadRepositoryPermission()) {
            buildLayout();
            restoreState();
            Page.getCurrent().addBrowserWindowResizeListener(this);
        }
    }

    private void buildLayout() {
        setMargin(false);
        setSpacing(false);
        setSizeFull();

        createMainLayout();

        addComponent(mainLayout);
        setExpandRatio(mainLayout, 1.0F);
    }

    private void createMainLayout() {
        mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setMargin(false);
        mainLayout.setSpacing(true);

        mainLayout.addComponent(dsTypeFilterLayout);
        mainLayout.addComponent(distributionSetGridLayout);
        mainLayout.addComponent(swModuleGridLayout);
        mainLayout.addComponent(distSMTypeFilterLayout);

        mainLayout.setExpandRatio(dsTypeFilterLayout, 0F);
        mainLayout.setExpandRatio(distributionSetGridLayout, 0.5F);
        mainLayout.setExpandRatio(swModuleGridLayout, 0.5F);
        mainLayout.setExpandRatio(distSMTypeFilterLayout, 0F);
    }

    private void restoreState() {
        if (manageDistUIState.getDSTypeFilterLayoutUiState().isHidden()) {
            hideDsTypeLayout();
        } else {
            showDsTypeLayout();
        }
        dsTypeFilterLayout.restoreState();

        if (manageDistUIState.getDistributionSetGridLayoutUiState().isMaximized()) {
            maximizeDsGridLayout();
        }
        distributionSetGridLayout.restoreState();

        if (manageDistUIState.getSwModuleGridLayoutUiState().isMaximized()) {
            maximizeSmGridLayout();
        }
        swModuleGridLayout.restoreState();

        if (manageDistUIState.getDistSMTypeFilterLayoutUiState().isHidden()) {
            hideSmTypeLayout();
        } else {
            showSmTypeLayout();
        }
        distSMTypeFilterLayout.restoreState();
    }

    void hideDsTypeLayout() {
        dsTypeFilterLayout.setVisible(false);
        distributionSetGridLayout.showDsTypeHeaderIcon();
    }

    void showDsTypeLayout() {
        dsTypeFilterLayout.setVisible(true);
        distributionSetGridLayout.hideDsTypeHeaderIcon();
    }

    void maximizeDsGridLayout() {
        swModuleGridLayout.setVisible(false);
        distSMTypeFilterLayout.setVisible(false);

        mainLayout.setExpandRatio(dsTypeFilterLayout, 0F);
        mainLayout.setExpandRatio(distributionSetGridLayout, 1.0F);
        mainLayout.setExpandRatio(swModuleGridLayout, 0F);
        mainLayout.setExpandRatio(distSMTypeFilterLayout, 0F);

        distributionSetGridLayout.maximize();
    }

    void maximizeSmGridLayout() {
        dsTypeFilterLayout.setVisible(false);
        distributionSetGridLayout.setVisible(false);

        mainLayout.setExpandRatio(dsTypeFilterLayout, 0F);
        mainLayout.setExpandRatio(distributionSetGridLayout, 0F);
        mainLayout.setExpandRatio(swModuleGridLayout, 1.0F);
        mainLayout.setExpandRatio(distSMTypeFilterLayout, 0F);

        swModuleGridLayout.maximize();
    }

    void hideSmTypeLayout() {
        distSMTypeFilterLayout.setVisible(false);
        swModuleGridLayout.showSmTypeHeaderIcon();
    }

    void showSmTypeLayout() {
        distSMTypeFilterLayout.setVisible(true);
        swModuleGridLayout.hideSmTypeHeaderIcon();
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

    void minimizeDsGridLayout() {
        swModuleGridLayout.setVisible(true);
        if (!manageDistUIState.getDistSMTypeFilterLayoutUiState().isHidden()) {
            distSMTypeFilterLayout.setVisible(true);
        }

        mainLayout.setExpandRatio(dsTypeFilterLayout, 0F);
        mainLayout.setExpandRatio(distributionSetGridLayout, 0.5F);
        mainLayout.setExpandRatio(swModuleGridLayout, 0.5F);
        mainLayout.setExpandRatio(distSMTypeFilterLayout, 0F);

        distributionSetGridLayout.minimize();
    }

    void minimizeSmGridLayout() {
        if (!manageDistUIState.getDSTypeFilterLayoutUiState().isHidden()) {
            dsTypeFilterLayout.setVisible(true);
        }
        distributionSetGridLayout.setVisible(true);

        mainLayout.setExpandRatio(dsTypeFilterLayout, 0F);
        mainLayout.setExpandRatio(distributionSetGridLayout, 0.5F);
        mainLayout.setExpandRatio(swModuleGridLayout, 0.5F);
        mainLayout.setExpandRatio(distSMTypeFilterLayout, 0F);

        swModuleGridLayout.minimize();
    }

    @PreDestroy
    void destroy() {
        if (dsTypeFilterLayout != null) {
            dsTypeFilterLayout.unsubscribeListener();
        }
        if (distributionSetGridLayout != null) {
            distributionSetGridLayout.unsubscribeListener();
        }
        if (swModuleGridLayout != null) {
            swModuleGridLayout.unsubscribeListener();
        }
        if (distSMTypeFilterLayout != null) {
            distSMTypeFilterLayout.unsubscribeListener();
        }
        if (eventListener != null) {
            eventListener.unsubscribeListeners();
        }
    }
}
