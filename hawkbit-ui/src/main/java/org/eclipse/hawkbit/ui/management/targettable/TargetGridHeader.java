/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.Arrays;
import java.util.concurrent.Executor;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.AddHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.BulkUploadHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.DistributionSetFilterDropAreaSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.FilterButtonsHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.ResizeHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.SearchHeaderSupport;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.management.ManagementUIState;
import org.eclipse.hawkbit.ui.management.event.BulkUploadPopupEvent;
import org.eclipse.hawkbit.ui.management.event.BulkUploadValidationMessageEvent;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.event.TargetFilterEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent.TargetComponentEvent;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Target table header layout.
 */
public class TargetGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final UINotification notification;
    private final ManagementUIState managementUIState;

    private final TargetWindowBuilder targetWindowBuilder;
    private final TargetBulkUpdateWindowLayout targetBulkUpdateWindow;

    private final transient SearchHeaderSupport searchHeaderSupport;
    private final transient FilterButtonsHeaderSupport filterButtonsHeaderSupport;
    private final transient AddHeaderSupport addHeaderSupport;
    private final transient BulkUploadHeaderSupport bulkUploadHeaderSupport;
    private final transient ResizeHeaderSupport resizeHeaderSupport;
    private final transient DistributionSetFilterDropAreaSupport distributionSetFilterDropAreaSupport;

    TargetGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker, final UIEventBus eventBus,
            final UINotification notification, final ManagementUIState managementUIState,
            final TargetManagement targetManagement, final DeploymentManagement deploymentManagement,
            final UiProperties uiproperties, final EntityFactory entityFactory, final UINotification uiNotification,
            final TargetTagManagement tagManagement, final DistributionSetManagement distributionSetManagement,
            final Executor uiExecutor, final TargetWindowBuilder targetWindowBuilder) {
        super(i18n, permChecker, eventBus);

        this.notification = notification;
        this.managementUIState = managementUIState;

        this.targetWindowBuilder = targetWindowBuilder;
        this.targetBulkUpdateWindow = new TargetBulkUpdateWindowLayout(i18n, targetManagement, eventBus,
                managementUIState, deploymentManagement, uiproperties, permChecker, uiNotification, tagManagement,
                distributionSetManagement, entityFactory, uiExecutor);

        this.searchHeaderSupport = new SearchHeaderSupport(i18n, UIComponentIdProvider.TARGET_TEXT_FIELD,
                UIComponentIdProvider.TARGET_TBL_SEARCH_RESET_ID, this::getSearchTextFromUiState, this::searchBy,
                this::resetSearchText);
        this.filterButtonsHeaderSupport = new FilterButtonsHeaderSupport(i18n, UIComponentIdProvider.SHOW_TARGET_TAGS,
                this::showFilterButtonsLayout, this::onLoadIsShowFilterButtonDisplayed);
        // TODO: consider moving permission check to header support or parent
        // header
        if (permChecker.hasCreateTargetPermission()) {
            this.addHeaderSupport = new AddHeaderSupport(i18n, UIComponentIdProvider.TARGET_TBL_ADD_ICON_ID,
                    this::addNewItem, this::onLoadIsTableMaximized);
            this.bulkUploadHeaderSupport = new BulkUploadHeaderSupport(i18n, this::bulkUpload,
                    this::isBulkUploadInProgress, this::onLoadIsTableMaximized);
        } else {
            this.addHeaderSupport = null;
            this.bulkUploadHeaderSupport = null;
        }

        this.resizeHeaderSupport = new ResizeHeaderSupport(i18n, UIComponentIdProvider.TARGET_MAX_MIN_TABLE_ICON,
                this::maximizeTable, this::minimizeTable, this::onLoadIsTableMaximized);
        addHeaderSupports(Arrays.asList(searchHeaderSupport, filterButtonsHeaderSupport, addHeaderSupport,
                bulkUploadHeaderSupport, resizeHeaderSupport));

        restoreHeaderState();
        buildHeader();

        // DistributionSetFilterDropArea is only available in TargetTableHeader
        this.distributionSetFilterDropAreaSupport = new DistributionSetFilterDropAreaSupport(i18n, eventBus,
                uiNotification, managementUIState, distributionSetManagement);
        final Component distributionSetFilterDropArea = distributionSetFilterDropAreaSupport.getHeaderComponent();
        addComponent(distributionSetFilterDropArea);
        setComponentAlignment(distributionSetFilterDropArea, Alignment.TOP_CENTER);
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage("header.target.table")).buildCaptionLabel();
    }

    @Override
    protected void restoreHeaderState() {
        super.restoreHeaderState();

        if (managementUIState.isCustomFilterSelected()) {
            onSimpleFilterReset();
        }
    }

    private void onSimpleFilterReset() {
        searchHeaderSupport.resetSearch();
        searchHeaderSupport.disableSearch();

        if (managementUIState.getTargetTableFilters().getDistributionSet().isPresent()) {
            distributionSetFilterDropAreaSupport.restoreState();
        }
    }

    private String getSearchTextFromUiState() {
        return managementUIState.getTargetTableFilters().getSearchText().orElse(null);
    }

    private void searchBy(final String newSearchText) {
        managementUIState.getTargetTableFilters().setSearchText(newSearchText);
        eventBus.publish(this, TargetFilterEvent.FILTER_BY_TEXT);
    }

    // TODO: check if needed or can be done by searchBy
    private void resetSearchText() {
        if (managementUIState.getTargetTableFilters().getSearchText().isPresent()) {
            managementUIState.getTargetTableFilters().setSearchText(null);
            eventBus.publish(this, TargetFilterEvent.REMOVE_FILTER_BY_TEXT);
        }
    }

    private void showFilterButtonsLayout() {
        managementUIState.setTargetTagFilterClosed(false);
        eventBus.publish(this, ManagementUIEvent.SHOW_TARGET_TAG_LAYOUT);
    }

    private Boolean onLoadIsShowFilterButtonDisplayed() {
        return managementUIState.isTargetTagFilterClosed();
    }

    private void addNewItem() {
        final Window addWindow = targetWindowBuilder.getWindowForAddTarget();

        addWindow.setCaption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.target")));
        UI.getCurrent().addWindow(addWindow);
        addWindow.setVisible(Boolean.TRUE);
    }

    private Boolean onLoadIsTableMaximized() {
        return managementUIState.isTargetTableMaximized();
    }

    // TODO: refactor window handling
    private void bulkUpload() {
        targetBulkUpdateWindow.resetComponents();

        final Window bulkUploadTargetWindow = targetBulkUpdateWindow.getWindow();
        UI.getCurrent().addWindow(bulkUploadTargetWindow);
        bulkUploadTargetWindow.setVisible(true);
    }

    private Boolean isBulkUploadInProgress() {
        return managementUIState.getTargetTableFilters().getBulkUpload().getSucessfulUploadCount() != 0
                || managementUIState.getTargetTableFilters().getBulkUpload().getFailedUploadCount() != 0;
    }

    private void maximizeTable() {
        if (bulkUploadHeaderSupport != null) {
            bulkUploadHeaderSupport.hideBulkUpload();
        }

        if (addHeaderSupport != null) {
            addHeaderSupport.hideAddIcon();
        }

        managementUIState.setTargetTableMaximized(Boolean.TRUE);
        eventBus.publish(this, new TargetTableEvent(BaseEntityEventType.MAXIMIZED));
    }

    private void minimizeTable() {
        if (bulkUploadHeaderSupport != null) {
            bulkUploadHeaderSupport.showBulkUpload();
        }

        if (addHeaderSupport != null) {
            addHeaderSupport.showAddIcon();
        }

        managementUIState.setTargetTableMaximized(Boolean.FALSE);
        eventBus.publish(this, new TargetTableEvent(BaseEntityEventType.MINIMIZED));
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ManagementUIEvent event) {
        if (event == ManagementUIEvent.HIDE_TARGET_TAG_LAYOUT) {
            filterButtonsHeaderSupport.showFilterButtonsIcon();
        } else if (event == ManagementUIEvent.SHOW_TARGET_TAG_LAYOUT) {
            filterButtonsHeaderSupport.hideFilterButtonsIcon();
        } else if (event == ManagementUIEvent.RESET_SIMPLE_FILTERS) {
            UI.getCurrent().access(this::onSimpleFilterReset);
        } else if (event == ManagementUIEvent.RESET_TARGET_FILTER_QUERY) {
            UI.getCurrent().access(searchHeaderSupport::enableSearch);
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final BulkUploadPopupEvent event) {
        if (BulkUploadPopupEvent.MAXIMIMIZED == event) {
            bulkUpload();
            targetBulkUpdateWindow.restoreComponentsValue();
        } else if (BulkUploadPopupEvent.CLOSED == event) {
            UI.getCurrent().access(bulkUploadHeaderSupport::showBulkUpload);
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final BulkUploadValidationMessageEvent event) {
        this.getUI().access(() -> notification.displayValidationError(event.getValidationErrorMessage()));
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final TargetTableEvent event) {
        if (TargetComponentEvent.BULK_TARGET_CREATED == event.getTargetComponentEvent()) {
            this.getUI().access(() -> targetBulkUpdateWindow.setProgressBarValue(
                    managementUIState.getTargetTableFilters().getBulkUpload().getProgressBarCurrentValue()));
        } else if (TargetComponentEvent.BULK_UPLOAD_COMPLETED == event.getTargetComponentEvent()) {
            this.getUI().access(targetBulkUpdateWindow::onUploadCompletion);
        } else if (TargetComponentEvent.BULK_TARGET_UPLOAD_STARTED == event.getTargetComponentEvent()) {
            this.getUI().access(this::onStartOfBulkUpload);
        } else if (TargetComponentEvent.BULK_UPLOAD_PROCESS_STARTED == event.getTargetComponentEvent()) {
            this.getUI().access(() -> targetBulkUpdateWindow.getBulkUploader().getUpload().setEnabled(false));
        }
    }

    private void onStartOfBulkUpload() {
        bulkUploadHeaderSupport.disableBulkUpload();
        targetBulkUpdateWindow.onStartOfUpload();
    }

    public void showTargetTagIcon() {
        filterButtonsHeaderSupport.showFilterButtonsIcon();
    }

    public void hideTargetTagIcon() {
        filterButtonsHeaderSupport.hideFilterButtonsIcon();
    }
}
