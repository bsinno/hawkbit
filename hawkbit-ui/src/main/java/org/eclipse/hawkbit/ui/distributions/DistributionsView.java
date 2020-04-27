/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

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
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.EventViewAware;
import org.eclipse.hawkbit.ui.common.layout.listener.LayoutResizeListener;
import org.eclipse.hawkbit.ui.common.layout.listener.LayoutVisibilityListener;
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

    private final transient LayoutVisibilityListener layoutVisibilityListener;
    private final transient LayoutResizeListener layoutResizeListener;

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

            final Map<EventLayout, Consumer<Boolean>> layoutVisibilityHandlers = new EnumMap<>(EventLayout.class);
            layoutVisibilityHandlers.put(EventLayout.DS_TYPE_FILTER, this::changeDsTypeLayoutVisibility);
            layoutVisibilityHandlers.put(EventLayout.SM_TYPE_FILTER, this::changeSmTypeLayoutVisibility);
            this.layoutVisibilityListener = new LayoutVisibilityListener(eventBus,
                    new EventViewAware(EventView.DISTRIBUTIONS), layoutVisibilityHandlers);

            final Map<EventLayout, Consumer<Boolean>> layoutResizeHandlers = new EnumMap<>(EventLayout.class);
            layoutResizeHandlers.put(EventLayout.DS_LIST, this::resizeDsGridLayout);
            layoutResizeHandlers.put(EventLayout.SM_LIST, this::resizeSmGridLayout);
            this.layoutResizeListener = new LayoutResizeListener(eventBus, new EventViewAware(EventView.DISTRIBUTIONS),
                    layoutResizeHandlers);
        } else {
            this.dsTypeFilterLayout = null;
            this.distributionSetGridLayout = null;
            this.swModuleGridLayout = null;
            this.distSMTypeFilterLayout = null;
            this.layoutVisibilityListener = null;
            this.layoutResizeListener = null;
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
        changeDsTypeLayoutVisibility(!manageDistUIState.getDSTypeFilterLayoutUiState().isHidden()
                && !manageDistUIState.getSwModuleGridLayoutUiState().isMaximized());
        dsTypeFilterLayout.restoreState();

        if (manageDistUIState.getDistributionSetGridLayoutUiState().isMaximized()) {
            resizeDsGridLayout(true);
        }
        distributionSetGridLayout.restoreState();

        if (manageDistUIState.getSwModuleGridLayoutUiState().isMaximized()) {
            resizeSmGridLayout(true);
        }
        swModuleGridLayout.restoreState();

        changeSmTypeLayoutVisibility(!manageDistUIState.getDistSMTypeFilterLayoutUiState().isHidden()
                && !manageDistUIState.getDistributionSetGridLayoutUiState().isMaximized());
        distSMTypeFilterLayout.restoreState();
    }

    private void changeDsTypeLayoutVisibility(final boolean shouldShow) {
        if (shouldShow) {
            dsTypeFilterLayout.setVisible(true);
            distributionSetGridLayout.hideDsTypeHeaderIcon();
        } else {
            dsTypeFilterLayout.setVisible(false);
            distributionSetGridLayout.showDsTypeHeaderIcon();
        }
    }

    private void resizeDsGridLayout(final boolean shouldMaximize) {
        if (shouldMaximize) {
            swModuleGridLayout.setVisible(false);
            changeSmTypeLayoutVisibility(false);

            mainLayout.setExpandRatio(dsTypeFilterLayout, 0F);
            mainLayout.setExpandRatio(distributionSetGridLayout, 1.0F);
            mainLayout.setExpandRatio(swModuleGridLayout, 0F);
            mainLayout.setExpandRatio(distSMTypeFilterLayout, 0F);

            distributionSetGridLayout.maximize();
        } else {
            swModuleGridLayout.setVisible(true);
            if (!manageDistUIState.getDistSMTypeFilterLayoutUiState().isHidden()) {
                changeSmTypeLayoutVisibility(true);
            }

            mainLayout.setExpandRatio(dsTypeFilterLayout, 0F);
            mainLayout.setExpandRatio(distributionSetGridLayout, 0.5F);
            mainLayout.setExpandRatio(swModuleGridLayout, 0.5F);
            mainLayout.setExpandRatio(distSMTypeFilterLayout, 0F);

            distributionSetGridLayout.minimize();
        }
    }

    private void resizeSmGridLayout(final boolean shouldMaximize) {
        if (shouldMaximize) {
            distributionSetGridLayout.setVisible(false);
            changeDsTypeLayoutVisibility(false);

            mainLayout.setExpandRatio(dsTypeFilterLayout, 0F);
            mainLayout.setExpandRatio(distributionSetGridLayout, 0F);
            mainLayout.setExpandRatio(swModuleGridLayout, 1.0F);
            mainLayout.setExpandRatio(distSMTypeFilterLayout, 0F);

            swModuleGridLayout.maximize();
        } else {
            distributionSetGridLayout.setVisible(true);
            if (!manageDistUIState.getDSTypeFilterLayoutUiState().isHidden()) {
                changeDsTypeLayoutVisibility(true);
            }

            mainLayout.setExpandRatio(dsTypeFilterLayout, 0F);
            mainLayout.setExpandRatio(distributionSetGridLayout, 0.5F);
            mainLayout.setExpandRatio(swModuleGridLayout, 0.5F);
            mainLayout.setExpandRatio(distSMTypeFilterLayout, 0F);

            swModuleGridLayout.minimize();
        }
    }

    private void changeSmTypeLayoutVisibility(final boolean shouldShow) {
        if (shouldShow) {
            distSMTypeFilterLayout.setVisible(true);
            swModuleGridLayout.hideSmTypeHeaderIcon();
        } else {
            distSMTypeFilterLayout.setVisible(false);
            swModuleGridLayout.showSmTypeHeaderIcon();
        }
    }

    @Override
    public void browserWindowResized(final BrowserWindowResizeEvent event) {
        showOrHideFilterButtons(event.getWidth());
    }

    private void showOrHideFilterButtons(final int browserWidth) {
        if (browserWidth < SPUIDefinitions.REQ_MIN_BROWSER_WIDTH) {
            if (!manageDistUIState.getDSTypeFilterLayoutUiState().isHidden()) {
                changeDsTypeLayoutVisibility(false);
            }

            if (!manageDistUIState.getDistSMTypeFilterLayoutUiState().isHidden()) {
                changeSmTypeLayoutVisibility(false);
            }
        } else {
            if (manageDistUIState.getDSTypeFilterLayoutUiState().isHidden()) {
                changeDsTypeLayoutVisibility(true);
            }

            if (manageDistUIState.getDistSMTypeFilterLayoutUiState().isHidden()) {
                changeSmTypeLayoutVisibility(true);
            }
        }
    }

    @PreDestroy
    void destroy() {
        if (permChecker.hasReadRepositoryPermission()) {
            layoutVisibilityListener.unsubscribe();
            layoutResizeListener.unsubscribe();

            dsTypeFilterLayout.unsubscribeListener();
            distributionSetGridLayout.unsubscribeListener();
            swModuleGridLayout.unsubscribeListener();
            distSMTypeFilterLayout.unsubscribeListener();
        }
    }
}
