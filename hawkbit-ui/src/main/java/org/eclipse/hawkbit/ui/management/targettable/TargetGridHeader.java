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
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.LayoutResizedEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityChangedEventPayload;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.AddHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.BulkUploadHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.DistributionSetFilterDropAreaSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.FilterButtonsHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.ResizeHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.SearchHeaderSupport;
import org.eclipse.hawkbit.ui.management.event.BulkUploadPopupEvent;
import org.eclipse.hawkbit.ui.management.event.BulkUploadValidationMessageEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent.TargetComponentEvent;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterLayoutUiState;
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

    private final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState;
    private final TargetGridLayoutUiState targetGridLayoutUiState;
    private final TargetBulkUploadUiState targetBulkUploadUiState;

    private final transient TargetWindowBuilder targetWindowBuilder;
    private final TargetBulkUpdateWindowLayout targetBulkUpdateWindow;

    private final transient SearchHeaderSupport searchHeaderSupport;
    private final transient FilterButtonsHeaderSupport filterButtonsHeaderSupport;
    private final transient AddHeaderSupport addHeaderSupport;
    private final transient BulkUploadHeaderSupport bulkUploadHeaderSupport;
    private final transient ResizeHeaderSupport resizeHeaderSupport;
    private final transient DistributionSetFilterDropAreaSupport distributionSetFilterDropAreaSupport;

    TargetGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker, final UIEventBus eventBus,
            final UINotification notification, final TargetManagement targetManagement,
            final DeploymentManagement deploymentManagement, final UiProperties uiproperties,
            final EntityFactory entityFactory, final UINotification uiNotification,
            final TargetTagManagement tagManagement, final DistributionSetManagement distributionSetManagement,
            final Executor uiExecutor, final TargetWindowBuilder targetWindowBuilder,
            final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState,
            final TargetGridLayoutUiState targetGridLayoutUiState,
            final TargetBulkUploadUiState targetBulkUploadUiState) {
        super(i18n, permChecker, eventBus);

        this.notification = notification;
        this.targetTagFilterLayoutUiState = targetTagFilterLayoutUiState;
        this.targetGridLayoutUiState = targetGridLayoutUiState;
        this.targetBulkUploadUiState = targetBulkUploadUiState;

        this.targetWindowBuilder = targetWindowBuilder;
        this.targetBulkUpdateWindow = new TargetBulkUpdateWindowLayout(i18n, eventBus, permChecker, notification,
                targetManagement, deploymentManagement, tagManagement, distributionSetManagement, entityFactory,
                uiproperties, uiExecutor, targetBulkUploadUiState);

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

        restoreState();
        buildHeader();

        // DistributionSetFilterDropArea is only available in TargetTableHeader
        this.distributionSetFilterDropAreaSupport = new DistributionSetFilterDropAreaSupport(i18n, eventBus,
                uiNotification, distributionSetManagement, targetTagFilterLayoutUiState, targetGridLayoutUiState);
        final Component distributionSetFilterDropArea = distributionSetFilterDropAreaSupport.getHeaderComponent();
        addComponent(distributionSetFilterDropArea);
        setComponentAlignment(distributionSetFilterDropArea, Alignment.TOP_CENTER);
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage("header.target.table")).buildCaptionLabel();
    }

    @Override
    public void restoreState() {
        super.restoreState();

        if (targetTagFilterLayoutUiState.isCustomFilterTabSelected()) {
            onSimpleFilterReset();
        }
    }

    public void onSimpleFilterReset() {
        searchHeaderSupport.resetSearch();
        searchHeaderSupport.disableSearch();

        if (targetGridLayoutUiState.getFilterDsIdNameVersion() != null) {
            distributionSetFilterDropAreaSupport.restoreState();
        }
    }

    private String getSearchTextFromUiState() {
        return targetGridLayoutUiState.getSearchFilter();
    }

    private void searchBy(final String newSearchText) {
        eventBus.publish(EventTopics.SEARCH_FILTER_CHANGED, this, newSearchText);

        targetGridLayoutUiState.setSearchFilter(newSearchText);
    }

    // TODO: check if needed or can be done by searchBy
    private void resetSearchText() {
        eventBus.publish(EventTopics.SEARCH_FILTER_CHANGED, this, "");

        targetGridLayoutUiState.setSearchFilter(null);
    }

    private void showFilterButtonsLayout() {
        eventBus.publish(EventTopics.LAYOUT_VISIBILITY_CHANGED, this, LayoutVisibilityChangedEventPayload.LAYOUT_SHOWN);

        targetTagFilterLayoutUiState.setHidden(false);
    }

    private Boolean onLoadIsShowFilterButtonDisplayed() {
        return !targetTagFilterLayoutUiState.isHidden();
    }

    private void addNewItem() {
        final Window addWindow = targetWindowBuilder.getWindowForAddTarget();

        addWindow.setCaption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.target")));
        UI.getCurrent().addWindow(addWindow);
        addWindow.setVisible(Boolean.TRUE);
    }

    private Boolean onLoadIsTableMaximized() {
        return targetGridLayoutUiState.isMaximized();
    }

    private void maximizeTable() {
        eventBus.publish(EventTopics.LAYOUT_RESIZED, this, LayoutResizedEventPayload.LAYOUT_MAXIMIZED);

        if (addHeaderSupport != null) {
            addHeaderSupport.hideAddIcon();
        }

        if (bulkUploadHeaderSupport != null) {
            bulkUploadHeaderSupport.hideBulkUpload();
        }

        targetGridLayoutUiState.setMaximized(true);
    }

    private void minimizeTable() {
        eventBus.publish(EventTopics.LAYOUT_RESIZED, this, LayoutResizedEventPayload.LAYOUT_MINIMIZED);

        if (addHeaderSupport != null) {
            addHeaderSupport.showAddIcon();
        }

        if (bulkUploadHeaderSupport != null) {
            bulkUploadHeaderSupport.showBulkUpload();
        }

        targetGridLayoutUiState.setMaximized(false);
    }

    // TODO: refactor window handling
    private void bulkUpload() {
        targetBulkUpdateWindow.resetComponents();

        final Window bulkUploadTargetWindow = targetBulkUpdateWindow.getWindow();
        UI.getCurrent().addWindow(bulkUploadTargetWindow);
        bulkUploadTargetWindow.setVisible(true);
    }

    private Boolean isBulkUploadInProgress() {
        return targetBulkUploadUiState.getSucessfulUploadCount() != 0
                || targetBulkUploadUiState.getFailedUploadCount() != 0;
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
            this.getUI().access(() -> targetBulkUpdateWindow
                    .setProgressBarValue(targetBulkUploadUiState.getProgressBarCurrentValue()));
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

    public void enableSearchIcon() {
        searchHeaderSupport.enableSearch();
    }
}
