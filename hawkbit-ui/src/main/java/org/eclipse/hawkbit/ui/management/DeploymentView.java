/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.AbstractHawkbitUI;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.management.actionhistory.ActionHistoryGridLayout;
import org.eclipse.hawkbit.ui.management.dstable.DistributionGridLayout;
import org.eclipse.hawkbit.ui.management.dstag.filter.DistributionTagLayout;
import org.eclipse.hawkbit.ui.management.targettable.TargetGridLayout;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterLayout;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Target status and deployment management view
 */
@UIScope
@SpringView(name = DeploymentView.VIEW_NAME, ui = AbstractHawkbitUI.class)
public class DeploymentView extends VerticalLayout implements View, BrowserWindowResizeListener {
    private static final long serialVersionUID = 1L;

    public static final String VIEW_NAME = "deployment";

    private final SpPermissionChecker permChecker;
    private final ManagementUIState managementUIState;

    private final TargetTagFilterLayout targetTagFilterLayout;
    private final TargetGridLayout targetGridLayout;
    private final DistributionGridLayout distributionGridLayout;
    private final DistributionTagLayout distributionTagLayout;
    private final ActionHistoryGridLayout actionHistoryLayout;

    private GridLayout mainLayout;

    private final transient DeploymentViewEventListener eventListener;

    @Autowired
    DeploymentView(final UIEventBus eventBus, final SpPermissionChecker permChecker, final VaadinMessageSource i18n,
            final UINotification uiNotification, final ManagementUIState managementUIState,
            final DeploymentManagement deploymentManagement, final DistributionSetManagement distributionSetManagement,
            final SoftwareModuleManagement smManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement, final TargetManagement targetManagement,
            final EntityFactory entityFactory, final UiProperties uiProperties,
            final TargetTagManagement targetTagManagement,
            final DistributionSetTagManagement distributionSetTagManagement,
            final TargetFilterQueryManagement targetFilterQueryManagement, final SystemManagement systemManagement,
            final TenantConfigurationManagement configManagement, final SystemSecurityContext systemSecurityContext,
            @Qualifier("uiExecutor") final Executor uiExecutor) {
        this.permChecker = permChecker;
        this.managementUIState = managementUIState;

        if (permChecker.hasTargetReadPermission()) {
            this.targetTagFilterLayout = new TargetTagFilterLayout(i18n, managementUIState, permChecker, eventBus,
                    uiNotification, entityFactory, targetFilterQueryManagement, targetTagManagement, targetManagement,
                    managementUIState.getTargetTagFilterLayoutUiState());

            this.targetGridLayout = new TargetGridLayout(eventBus, targetManagement, entityFactory, i18n,
                    uiNotification, managementUIState, deploymentManagement, uiProperties, permChecker,
                    targetTagManagement, distributionSetManagement, uiExecutor, configManagement, systemSecurityContext,
                    managementUIState.getTargetTagFilterLayoutUiState(), managementUIState.getTargetGridLayoutUiState(),
                    managementUIState.getTargetBulkUploadUiState());

            this.actionHistoryLayout = new ActionHistoryGridLayout(i18n, deploymentManagement, eventBus, uiNotification,
                    managementUIState, permChecker, managementUIState.getActionHistoryGridLayoutUiState());
        } else {
            this.targetTagFilterLayout = null;
            this.targetGridLayout = null;
            this.actionHistoryLayout = null;
        }

        if (permChecker.hasReadRepositoryPermission()) {
            this.distributionTagLayout = new DistributionTagLayout(eventBus, managementUIState, i18n, permChecker,
                    distributionSetTagManagement, entityFactory, uiNotification, distributionSetManagement,
                    managementUIState.getDistributionTagLayoutUiState());
            this.distributionGridLayout = new DistributionGridLayout(i18n, eventBus, permChecker, entityFactory,
                    uiNotification, managementUIState, targetManagement, distributionSetManagement, smManagement,
                    distributionSetTypeManagement, distributionSetTagManagement, systemManagement, deploymentManagement,
                    configManagement, systemSecurityContext, uiProperties,
                    managementUIState.getDistributionGridLayoutUiState());
        } else {
            this.distributionTagLayout = null;
            this.distributionGridLayout = null;
        }

        if (permChecker.hasTargetReadPermission() || permChecker.hasReadRepositoryPermission()) {
            this.eventListener = new DeploymentViewEventListener(this, eventBus);
        } else {
            this.eventListener = null;
        }
    }

    @PostConstruct
    void init() {
        if (permChecker.hasTargetReadPermission() || permChecker.hasReadRepositoryPermission()) {
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
        mainLayout = new GridLayout();
        mainLayout.setSizeFull();
        mainLayout.setMargin(false);
        mainLayout.setSpacing(true);
        mainLayout.setStyleName("fullSize");

        mainLayout.setRowExpandRatio(0, 1.0F);

        if (permChecker.hasReadRepositoryPermission() && permChecker.hasTargetReadPermission()) {
            addAllWidgets();
        } else if (permChecker.hasReadRepositoryPermission()) {
            addDistributionWidgetsOnly();
        } else if (permChecker.hasTargetReadPermission()) {
            addTargetWidgetsOnly();
        }
    }

    private void addAllWidgets() {
        mainLayout.setColumns(5);

        mainLayout.setColumnExpandRatio(0, 0F);
        mainLayout.setColumnExpandRatio(1, 0.275F);
        mainLayout.setColumnExpandRatio(2, 0.275F);
        mainLayout.setColumnExpandRatio(3, 0F);
        mainLayout.setColumnExpandRatio(4, 0.45F);

        mainLayout.addComponent(targetTagFilterLayout, 0, 0);
        mainLayout.addComponent(targetGridLayout, 1, 0);
        mainLayout.addComponent(distributionGridLayout, 2, 0);
        mainLayout.addComponent(distributionTagLayout, 3, 0);
        mainLayout.addComponent(actionHistoryLayout, 4, 0);
    }

    private void addDistributionWidgetsOnly() {
        mainLayout.setColumns(2);

        mainLayout.setColumnExpandRatio(0, 1F);

        mainLayout.addComponent(distributionGridLayout, 0, 0);
        mainLayout.addComponent(distributionTagLayout, 1, 0);
    }

    private void addTargetWidgetsOnly() {
        mainLayout.setColumns(3);

        mainLayout.setColumnExpandRatio(1, 0.4F);
        mainLayout.setColumnExpandRatio(2, 0.6F);

        mainLayout.addComponent(targetTagFilterLayout, 0, 0);
        mainLayout.addComponent(targetGridLayout, 1, 0);
        mainLayout.addComponent(actionHistoryLayout, 2, 0);
    }

    private void restoreState() {
        if (permChecker.hasTargetReadPermission()) {
            if (managementUIState.getTargetTagFilterLayoutUiState().isHidden()) {
                hideTargetTagLayout();
            } else {
                showTargetTagLayout();
            }
            targetTagFilterLayout.restoreState();

            if (managementUIState.getTargetGridLayoutUiState().isMaximized()) {
                maximizeTargetGridLayout();
            }
            targetGridLayout.restoreState();

            if (managementUIState.getActionHistoryGridLayoutUiState().isMaximized()) {
                maximizeActionHistoryGridLayout();
            }
            actionHistoryLayout.restoreState();
        }

        if (permChecker.hasReadRepositoryPermission()) {
            if (managementUIState.getDistributionTagLayoutUiState().isHidden()) {
                hideDsTagLayout();
            } else {
                showDsTagLayout();
            }
            distributionTagLayout.restoreState();

            if (managementUIState.getDistributionGridLayoutUiState().isMaximized()) {
                maximizeDsGridLayout();
            }
            distributionGridLayout.restoreState();
        }
    }

    void hideTargetTagLayout() {
        targetTagFilterLayout.setVisible(false);
        targetGridLayout.showTargetTagHeaderIcon();
    }

    void showTargetTagLayout() {
        targetTagFilterLayout.setVisible(true);
        targetGridLayout.hideTargetTagHeaderIcon();
    }

    void maximizeTargetGridLayout() {
        if (distributionGridLayout != null) {
            distributionGridLayout.setVisible(false);
        }
        if (distributionTagLayout != null) {
            distributionTagLayout.setVisible(false);
        }
        actionHistoryLayout.setVisible(false);

        mainLayout.setColumnExpandRatio(0, 0F);
        mainLayout.setColumnExpandRatio(1, 1F);
        mainLayout.setColumnExpandRatio(2, 0F);
        mainLayout.setColumnExpandRatio(3, 0F);
        mainLayout.setColumnExpandRatio(4, 0F);

        targetGridLayout.maximize();
    }

    void maximizeActionHistoryGridLayout() {
        targetTagFilterLayout.setVisible(false);
        targetGridLayout.setVisible(false);
        if (distributionGridLayout != null) {
            distributionGridLayout.setVisible(false);
        }
        if (distributionTagLayout != null) {
            distributionTagLayout.setVisible(false);
        }

        mainLayout.setColumnExpandRatio(0, 0F);
        mainLayout.setColumnExpandRatio(1, 0F);
        mainLayout.setColumnExpandRatio(2, 0F);
        mainLayout.setColumnExpandRatio(3, 0F);
        mainLayout.setColumnExpandRatio(4, 1F);

        actionHistoryLayout.maximize();
    }
    // TODO
    // private void maximizeActionHistory() {
    // removeComponent(countMessageLabel);
    // mainLayout.removeAllComponents();
    // mainLayout.setColumns(3);
    // mainLayout.setRows(1);
    // mainLayout.addComponent(actionHistoryLayout, 0, 0);
    // mainLayout.addComponent(actionStatusLayout, 1, 0);
    // mainLayout.addComponent(actionStatusMsgLayout, 2, 0);
    // mainLayout.setColumnExpandRatio(0, 0.55F);
    // mainLayout.setColumnExpandRatio(1, 0.18F);
    // mainLayout.setColumnExpandRatio(2, 0.27F);
    // mainLayout.setComponentAlignment(actionHistoryLayout,
    // Alignment.TOP_LEFT);
    // }

    void hideDsTagLayout() {
        distributionTagLayout.setVisible(false);
        distributionGridLayout.showDsTagHeaderIcon();
    }

    void showDsTagLayout() {
        distributionTagLayout.setVisible(true);
        distributionGridLayout.hideDsTagHeaderIcon();
    }

    void maximizeDsGridLayout() {
        if (targetTagFilterLayout != null) {
            targetTagFilterLayout.setVisible(false);
        }
        if (targetGridLayout != null) {
            targetGridLayout.setVisible(false);
        }
        if (actionHistoryLayout != null) {
            actionHistoryLayout.setVisible(false);
        }

        mainLayout.setColumnExpandRatio(0, 0F);
        mainLayout.setColumnExpandRatio(1, 0F);
        mainLayout.setColumnExpandRatio(2, 1F);
        mainLayout.setColumnExpandRatio(3, 0F);
        mainLayout.setColumnExpandRatio(4, 0F);

        targetGridLayout.maximize();
    }

    @Override
    public void browserWindowResized(final BrowserWindowResizeEvent event) {
        showOrHideFilterButtons(event.getWidth());
    }

    private void showOrHideFilterButtons(final int browserWidth) {
        if (browserWidth < SPUIDefinitions.REQ_MIN_BROWSER_WIDTH) {
            if (permChecker.hasTargetReadPermission()
                    && !managementUIState.getTargetTagFilterLayoutUiState().isHidden()) {
                hideTargetTagLayout();
            }

            if (permChecker.hasReadRepositoryPermission()
                    && !managementUIState.getDistributionTagLayoutUiState().isHidden()) {
                hideDsTagLayout();
            }
        } else {
            if (permChecker.hasTargetReadPermission()
                    && managementUIState.getTargetTagFilterLayoutUiState().isHidden()) {
                showTargetTagLayout();
            }

            if (permChecker.hasReadRepositoryPermission()
                    && managementUIState.getDistributionTagLayoutUiState().isHidden()) {
                showDsTagLayout();
            }
        }
    }

    void onTargetSelected(final ProxyTarget target) {
        actionHistoryLayout.onTargetSelected(target);
    }

    void onTargetUpdated(final Collection<Long> entityIds) {
        final Long lastSelectedTargetId = managementUIState.getTargetGridLayoutUiState().getSelectedTargetId();

        if (lastSelectedTargetId != null && entityIds.contains(lastSelectedTargetId)) {
            // TODO: think over
            actionHistoryLayout.onTargetUpdated(lastSelectedTargetId);
        }
    }

    void minimizeTargetGridLayout() {
        if (distributionGridLayout != null) {
            distributionGridLayout.setVisible(true);
        }
        if (distributionTagLayout != null && !managementUIState.getDistributionTagLayoutUiState().isHidden()) {
            distributionTagLayout.setVisible(true);
        }
        actionHistoryLayout.setVisible(true);

        // TODO: adapt expand ratios according to permissions
        mainLayout.setColumnExpandRatio(0, 0F);
        mainLayout.setColumnExpandRatio(1, 0.275F);
        mainLayout.setColumnExpandRatio(2, 0.275F);
        mainLayout.setColumnExpandRatio(3, 0F);
        mainLayout.setColumnExpandRatio(4, 0.45F);

        targetGridLayout.minimize();
    }

    void minimizeDsGridLayout() {
        if (targetTagFilterLayout != null && !managementUIState.getTargetTagFilterLayoutUiState().isHidden()) {
            targetTagFilterLayout.setVisible(true);
        }
        if (targetGridLayout != null) {
            targetGridLayout.setVisible(true);
        }
        if (actionHistoryLayout != null) {
            actionHistoryLayout.setVisible(true);
        }

        // TODO: adapt expand ratios according to permissions
        mainLayout.setColumnExpandRatio(0, 0F);
        mainLayout.setColumnExpandRatio(1, 0.275F);
        mainLayout.setColumnExpandRatio(2, 0.275F);
        mainLayout.setColumnExpandRatio(3, 0F);
        mainLayout.setColumnExpandRatio(4, 0.45F);

        distributionGridLayout.minimize();
    }

    void minimizeActionHistoryGridLayout() {
        if (!managementUIState.getTargetTagFilterLayoutUiState().isHidden()) {
            targetTagFilterLayout.setVisible(true);
        }
        targetGridLayout.setVisible(true);
        if (distributionGridLayout != null) {
            distributionGridLayout.setVisible(true);
        }
        if (distributionTagLayout != null && !managementUIState.getDistributionTagLayoutUiState().isHidden()) {
            distributionTagLayout.setVisible(true);
        }

        // TODO: adapt expand ratios according to permissions
        mainLayout.setColumnExpandRatio(0, 0F);
        mainLayout.setColumnExpandRatio(1, 0.275F);
        mainLayout.setColumnExpandRatio(2, 0.275F);
        mainLayout.setColumnExpandRatio(3, 0F);
        mainLayout.setColumnExpandRatio(4, 0.45F);

        actionHistoryLayout.minimize();
    }

    void onTargetFilterTabChanged(final boolean isCustomFilterTabSelected) {
        targetGridLayout.onTargetFilterTabChanged(isCustomFilterTabSelected);
    }

    void filterTargetGridByTags(final Collection<String> tagFilterNames) {
        targetGridLayout.filterGridByTags(tagFilterNames);
    }

    void filterTargetGridByNoTag(final boolean isActive) {
        targetGridLayout.filterGridByNoTag(isActive);
    }

    void filterTargetGridByStatus(final List<TargetUpdateStatus> statusFilters) {
        targetGridLayout.filterGridByStatus(statusFilters);
    }

    void filterTargetGridByOverdue(final boolean isOverdue) {
        targetGridLayout.filterGridByOverdue(isOverdue);
    }

    void filterTargetGridByCustomFilter(final Long customFilterId) {
        targetGridLayout.filterGridByCustomFilter(customFilterId);
    }

    void filterDsGridByTags(final Collection<String> tagFilterNames) {
        distributionGridLayout.filterGridByTags(tagFilterNames);
    }

    void filterDsGridByNoTag(final boolean isActive) {
        distributionGridLayout.filterGridByNoTag(isActive);
    }

    @PreDestroy
    void destroy() {
        if (targetTagFilterLayout != null) {
            targetTagFilterLayout.unsubscribeListener();
        }
        if (targetGridLayout != null) {
            targetGridLayout.unsubscribeListener();
        }
        if (distributionGridLayout != null) {
            distributionGridLayout.unsubscribeListener();
        }
        if (distributionTagLayout != null) {
            distributionTagLayout.unsubscribeListener();
        }
        if (actionHistoryLayout != null) {
            actionHistoryLayout.unsubscribeListener();
        }
        if (eventListener != null) {
            eventListener.unsubscribeListeners();
        }
    }
}
