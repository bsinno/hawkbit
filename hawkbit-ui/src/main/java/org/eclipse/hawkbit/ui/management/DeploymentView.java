/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management;

import java.util.Map;
import java.util.concurrent.Executor;

import javax.annotation.PostConstruct;

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
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.AbstractHawkbitUI;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.components.AbstractNotificationView;
import org.eclipse.hawkbit.ui.components.NotificationUnreadButton;
import org.eclipse.hawkbit.ui.components.RefreshableContainer;
import org.eclipse.hawkbit.ui.dd.criteria.ManagementViewClientCriterion;
import org.eclipse.hawkbit.ui.management.actionhistory.ActionHistoryGridLayout;
import org.eclipse.hawkbit.ui.management.actionhistory.ActionStatusGridLayout;
import org.eclipse.hawkbit.ui.management.actionhistory.ActionStatusMsgGridLayout;
import org.eclipse.hawkbit.ui.management.dstable.DistributionGridLayout;
import org.eclipse.hawkbit.ui.management.dstag.filter.DistributionTagLayout;
import org.eclipse.hawkbit.ui.management.event.DistributionTableEvent;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent;
import org.eclipse.hawkbit.ui.management.state.DistributionTableFilters;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.management.targettable.TargetGridLayout;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterLayout;
import org.eclipse.hawkbit.ui.menu.DashboardMenuItem;
import org.eclipse.hawkbit.ui.push.DistributionSetCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetTagCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetTagDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.DistributionSetTagUpdatedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetTagCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetTagDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.TargetTagUpdatedEventContainer;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventBusListenerMethodFilter;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.google.common.collect.Maps;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.UI;

/**
 * Target status and deployment management view
 */
@UIScope
@SpringView(name = DeploymentView.VIEW_NAME, ui = AbstractHawkbitUI.class)
public class DeploymentView extends AbstractNotificationView implements BrowserWindowResizeListener {
    private static final long serialVersionUID = 1L;

    public static final String VIEW_NAME = "deployment";

    private final SpPermissionChecker permChecker;

    private final ManagementUIState managementUIState;

    private final TargetTagFilterLayout targetTagFilterLayout;
    private final TargetGridLayout targetGridLayout;
    private final DistributionGridLayout distributionGridLayout;
    private final DistributionTagLayout distributionTagLayout;
    private final ActionHistoryGridLayout actionHistoryLayout;
    private final ActionStatusGridLayout actionStatusLayout;
    private final ActionStatusMsgGridLayout actionStatusMsgLayout;

    private GridLayout mainLayout;

    private final DeploymentViewMenuItem deploymentViewMenuItem;
    private final CountMessageLabel countMessageLabel;

    @Autowired
    DeploymentView(final UIEventBus eventBus, final SpPermissionChecker permChecker, final VaadinMessageSource i18n,
            final UINotification uiNotification, final ManagementUIState managementUIState,
            final DeploymentManagement deploymentManagement, final DistributionTableFilters distFilterParameters,
            final DistributionSetManagement distributionSetManagement, final SoftwareModuleManagement smManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement, final TargetManagement targetManagement,
            final EntityFactory entityFactory, final UiProperties uiProperties,
            final ManagementViewClientCriterion managementViewClientCriterion,
            final TargetTagManagement targetTagManagement,
            final DistributionSetTagManagement distributionSetTagManagement,
            final TargetFilterQueryManagement targetFilterQueryManagement, final SystemManagement systemManagement,
            final TenantConfigurationManagement configManagement, final SystemSecurityContext systemSecurityContext,
            final NotificationUnreadButton notificationUnreadButton,
            final DeploymentViewMenuItem deploymentViewMenuItem, @Qualifier("uiExecutor") final Executor uiExecutor) {
        super(eventBus, notificationUnreadButton);

        this.permChecker = permChecker;
        this.managementUIState = managementUIState;

        this.deploymentViewMenuItem = deploymentViewMenuItem;

        if (permChecker.hasTargetReadPermission()) {
            this.targetTagFilterLayout = new TargetTagFilterLayout(i18n, managementUIState, permChecker, eventBus,
                    uiNotification, entityFactory, targetFilterQueryManagement, targetTagManagement, targetManagement);

            this.targetGridLayout = new TargetGridLayout(eventBus, targetManagement, entityFactory, i18n,
                    uiNotification, managementUIState, deploymentManagement, uiProperties, permChecker,
                    targetTagManagement, distributionSetManagement, uiExecutor, configManagement,
                    systemSecurityContext);

            this.actionHistoryLayout = new ActionHistoryGridLayout(i18n, deploymentManagement, eventBus, uiNotification,
                    managementUIState, permChecker);
            this.actionStatusLayout = new ActionStatusGridLayout(i18n, eventBus, managementUIState,
                    deploymentManagement);
            this.actionStatusMsgLayout = new ActionStatusMsgGridLayout(i18n, eventBus, managementUIState,
                    deploymentManagement);

            this.countMessageLabel = new CountMessageLabel(eventBus, targetManagement, i18n, managementUIState,
                    targetGridLayout.getTargetGrid().getDataCommunicator());
        } else {
            this.targetTagFilterLayout = null;
            this.targetGridLayout = null;
            this.actionHistoryLayout = null;
            this.actionStatusLayout = null;
            this.actionStatusMsgLayout = null;
            this.countMessageLabel = null;
        }

        if (permChecker.hasReadRepositoryPermission()) {
            this.distributionTagLayout = new DistributionTagLayout(eventBus, managementUIState, i18n, permChecker,
                    distributionSetTagManagement, entityFactory, uiNotification, distributionSetManagement);
            this.distributionGridLayout = new DistributionGridLayout(i18n, eventBus, permChecker, entityFactory,
                    uiNotification, managementUIState, targetManagement, distributionSetManagement, smManagement,
                    distributionSetTypeManagement, distributionSetTagManagement, systemManagement, deploymentManagement,
                    configManagement, systemSecurityContext, uiProperties);
        } else {
            this.distributionTagLayout = null;
            this.distributionGridLayout = null;
        }
    }

    @Override
    protected DashboardMenuItem getDashboardMenuItem() {
        return deploymentViewMenuItem;
    }

    @PostConstruct
    void init() {
        buildLayout();
        restoreState();
        Page.getCurrent().addBrowserWindowResizeListener(this);
        showOrHideFilterButtons(Page.getCurrent().getBrowserWindowWidth());
        eventBus.publish(this, ManagementUIEvent.SHOW_COUNT_MESSAGE);
    }

    private void buildLayout() {
        if (permChecker.hasTargetReadPermission() || permChecker.hasReadRepositoryPermission()) {
            setSizeFull();
            createMainLayout();
            addComponent(mainLayout, 0);
            setExpandRatio(mainLayout, 1.0F);
        }
    }

    private void createMainLayout() {
        mainLayout = new GridLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setStyleName("fullSize");

        mainLayout.setRowExpandRatio(0, 1.0F);

        layoutWidgets();
    }

    private void layoutWidgets() {
        mainLayout.removeAllComponents();
        if (permChecker.hasReadRepositoryPermission() && permChecker.hasTargetReadPermission()) {
            displayAllWidgets();
        } else if (permChecker.hasReadRepositoryPermission()) {
            displayDistributionWidgetsOnly();
        } else if (permChecker.hasTargetReadPermission()) {
            displayTargetWidgetsOnly();
        }
    }

    private void displayAllWidgets() {
        mainLayout.setColumns(5);
        mainLayout.setRows(1);
        mainLayout.addComponent(targetTagFilterLayout, 0, 0);
        mainLayout.addComponent(targetGridLayout, 1, 0);
        mainLayout.addComponent(distributionGridLayout, 2, 0);
        mainLayout.addComponent(distributionTagLayout, 3, 0);
        mainLayout.addComponent(actionHistoryLayout, 4, 0);
        showTargetCount();
        mainLayout.setColumnExpandRatio(0, 0F);
        mainLayout.setColumnExpandRatio(1, 0.275F);
        mainLayout.setColumnExpandRatio(2, 0.275F);
        mainLayout.setColumnExpandRatio(3, 0F);
        mainLayout.setColumnExpandRatio(4, 0.45F);
    }

    private void showTargetCount() {
        addComponent(countMessageLabel);
    }

    private void displayDistributionWidgetsOnly() {
        mainLayout.setColumns(2);
        mainLayout.setRows(1);
        mainLayout.addComponent(distributionGridLayout, 0, 0);
        mainLayout.addComponent(distributionTagLayout, 1, 0);
        mainLayout.setColumnExpandRatio(0, 1F);
    }

    private void displayTargetWidgetsOnly() {
        mainLayout.setColumns(3);
        mainLayout.setRows(1);
        mainLayout.addComponent(targetTagFilterLayout, 0, 0);
        mainLayout.addComponent(targetGridLayout, 1, 0);
        mainLayout.addComponent(actionHistoryLayout, 2, 0);
        showTargetCount();
        mainLayout.setColumnExpandRatio(1, 0.4F);
        mainLayout.setColumnExpandRatio(2, 0.6F);
    }

    private void restoreState() {
        if (managementUIState.isTargetTableMaximized()) {
            maximizeTargetTable();
        }
        if (managementUIState.isDsTableMaximized()) {
            maximizeDistTable();
        }
        if (managementUIState.isActionHistoryMaximized()) {
            maximizeActionHistory();
        }
    }

    private void maximizeTargetTable() {
        if (permChecker.hasReadRepositoryPermission()) {
            mainLayout.removeComponent(distributionGridLayout);
            mainLayout.removeComponent(distributionTagLayout);
        }
        mainLayout.removeComponent(actionHistoryLayout);
        removeComponent(countMessageLabel);
        mainLayout.setColumnExpandRatio(1, 1F);
        mainLayout.setColumnExpandRatio(2, 0F);
        mainLayout.setColumnExpandRatio(3, 0F);
        mainLayout.setColumnExpandRatio(4, 0F);
    }

    private void maximizeDistTable() {
        if (permChecker.hasTargetReadPermission()) {
            mainLayout.removeComponent(targetTagFilterLayout);
            mainLayout.removeComponent(targetGridLayout);
            mainLayout.removeComponent(actionHistoryLayout);
            removeComponent(countMessageLabel);
        }
        mainLayout.setColumnExpandRatio(0, 0F);
        mainLayout.setColumnExpandRatio(1, 0F);
        mainLayout.setColumnExpandRatio(2, 1F);
        mainLayout.setColumnExpandRatio(4, 0F);
    }

    private void maximizeActionHistory() {
        removeComponent(countMessageLabel);
        mainLayout.removeAllComponents();
        mainLayout.setColumns(3);
        mainLayout.setRows(1);
        mainLayout.addComponent(actionHistoryLayout, 0, 0);
        mainLayout.addComponent(actionStatusLayout, 1, 0);
        mainLayout.addComponent(actionStatusMsgLayout, 2, 0);
        mainLayout.setColumnExpandRatio(0, 0.55F);
        mainLayout.setColumnExpandRatio(1, 0.18F);
        mainLayout.setColumnExpandRatio(2, 0.27F);
        mainLayout.setComponentAlignment(actionHistoryLayout, Alignment.TOP_LEFT);
    }

    @Override
    public void browserWindowResized(final BrowserWindowResizeEvent event) {
        final int browserWidth = event.getWidth();
        showOrHideFilterButtons(browserWidth);
    }

    private void showOrHideFilterButtons(final int browserWidth) {
        if (browserWidth < SPUIDefinitions.REQ_MIN_BROWSER_WIDTH) {
            if (permChecker.hasTargetReadPermission()) {
                eventBus.publish(this, ManagementUIEvent.HIDE_TARGET_TAG_LAYOUT);
            }

            if (permChecker.hasReadRepositoryPermission()) {
                eventBus.publish(this, ManagementUIEvent.HIDE_DISTRIBUTION_TAG_LAYOUT);
            }
        } else {
            // TODO: check if managementUIState validation is correct here
            if (permChecker.hasTargetReadPermission() && !managementUIState.isTargetTagFilterClosed()) {
                eventBus.publish(this, ManagementUIEvent.SHOW_TARGET_TAG_LAYOUT);
            }
            // TODO: check if managementUIState validation is correct here
            if (permChecker.hasReadRepositoryPermission() && !managementUIState.isDistTagFilterClosed()) {
                eventBus.publish(this, ManagementUIEvent.SHOW_DISTRIBUTION_TAG_LAYOUT);
            }
        }
    }

    // TODO: do we really need to set the selected DS here?
    @Override
    public void enter(final ViewChangeEvent event) {
        if (permChecker.hasReadRepositoryPermission()) {
            // TODO: refactor fields in managementUIState
            managementUIState.getLastSelectedDsIdName().ifPresent(lastSelectedDsId -> {
                final ProxyDistributionSet dsToSelect = new ProxyDistributionSet();
                dsToSelect.setId(lastSelectedDsId);

                distributionGridLayout.getDistributionGrid().select(dsToSelect);
            });
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final DistributionTableEvent event) {
        if (BaseEntityEventType.MINIMIZED == event.getEventType()) {
            minimizeDistTable();
        } else if (BaseEntityEventType.MAXIMIZED == event.getEventType()) {
            maximizeDistTable();
        }
    }

    private void minimizeDistTable() {
        layoutWidgets();
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final TargetTableEvent event) {
        if (BaseEntityEventType.MINIMIZED == event.getEventType()) {
            minimizeTargetTable();
        } else if (BaseEntityEventType.MAXIMIZED == event.getEventType()) {
            maximizeTargetTable();
        }
    }

    private void minimizeTargetTable() {
        layoutWidgets();
    }

    // TODO: rethink eventing and check if ui.access is neccessary here
    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ManagementUIEvent mgmtUIEvent) {
        if (mgmtUIEvent == ManagementUIEvent.MIN_ACTION_HISTORY) {
            UI.getCurrent().access(this::minimizeActionHistory);
        } else if (mgmtUIEvent == ManagementUIEvent.MAX_ACTION_HISTORY) {
            UI.getCurrent().access(this::maximizeActionHistory);
        }
    }

    private void minimizeActionHistory() {
        layoutWidgets();
    }

    @Override
    protected Map<Class<?>, RefreshableContainer> getSupportedPushEvents() {
        final Map<Class<?>, RefreshableContainer> supportedEvents = Maps.newHashMapWithExpectedSize(10);

        // TODO: what about TargetUpdatedEventContainer?
        if (permChecker.hasTargetReadPermission()) {
            supportedEvents.put(TargetCreatedEventContainer.class, targetGridLayout.getTargetGrid());
            supportedEvents.put(TargetDeletedEventContainer.class, targetGridLayout.getTargetGrid());
        }

        // TODO: what about DistributionSetUpdatedEventContainer?
        if (permChecker.hasReadRepositoryPermission()) {
            supportedEvents.put(DistributionSetCreatedEventContainer.class,
                    distributionGridLayout.getDistributionGrid());
            supportedEvents.put(DistributionSetDeletedEventContainer.class,
                    distributionGridLayout.getDistributionGrid());
        }

        supportedEvents.put(TargetTagCreatedEventContainer.class, targetTagFilterLayout);
        supportedEvents.put(TargetTagDeletedEventContainer.class, targetTagFilterLayout);
        supportedEvents.put(TargetTagUpdatedEventContainer.class, targetTagFilterLayout);

        supportedEvents.put(DistributionSetTagCreatedEventContainer.class, distributionTagLayout);
        supportedEvents.put(DistributionSetTagDeletedEventContainer.class, distributionTagLayout);
        supportedEvents.put(DistributionSetTagUpdatedEventContainer.class, distributionTagLayout);

        return supportedEvents;
    }

    public static class DeploymentViewEventFilter implements EventBusListenerMethodFilter {

        @Override
        public boolean filter(final org.vaadin.spring.events.Event<?> event) {
            return DeploymentView.VIEW_NAME.equals(event.getSource());
        }

    }
}
