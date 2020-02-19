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

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.event.BulkUploadEventPayload;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.LayoutResizeEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutResizeEventPayload.ResizeType;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.event.SearchFilterEventPayload;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.AddHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.BulkUploadHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.DistributionSetFilterDropAreaSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.FilterButtonsHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.ResizeHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.SearchHeaderSupport;
import org.eclipse.hawkbit.ui.management.bulkupload.BulkUploadWindowBuilder;
import org.eclipse.hawkbit.ui.management.bulkupload.TargetBulkUploadUiState;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Target table header layout.
 */
public class TargetGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState;
    private final TargetGridLayoutUiState targetGridLayoutUiState;
    private final TargetBulkUploadUiState targetBulkUploadUiState;

    private final transient TargetWindowBuilder targetWindowBuilder;
    private final transient BulkUploadWindowBuilder bulkUploadWindowBuilder;

    private final transient SearchHeaderSupport searchHeaderSupport;
    private final transient FilterButtonsHeaderSupport filterButtonsHeaderSupport;
    private final transient AddHeaderSupport addHeaderSupport;
    private final transient BulkUploadHeaderSupport bulkUploadHeaderSupport;
    private final transient ResizeHeaderSupport resizeHeaderSupport;
    private final transient DistributionSetFilterDropAreaSupport distributionSetFilterDropAreaSupport;

    TargetGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker, final UIEventBus eventBus,
            final UINotification notification, final TargetWindowBuilder targetWindowBuilder,
            final BulkUploadWindowBuilder bulkUploadWindowBuilder,
            final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState,
            final TargetGridLayoutUiState targetGridLayoutUiState,
            final TargetBulkUploadUiState targetBulkUploadUiState) {
        super(i18n, permChecker, eventBus);

        this.targetTagFilterLayoutUiState = targetTagFilterLayoutUiState;
        this.targetGridLayoutUiState = targetGridLayoutUiState;
        this.targetBulkUploadUiState = targetBulkUploadUiState;

        this.targetWindowBuilder = targetWindowBuilder;
        this.bulkUploadWindowBuilder = bulkUploadWindowBuilder;

        this.searchHeaderSupport = new SearchHeaderSupport(i18n, UIComponentIdProvider.TARGET_TEXT_FIELD,
                UIComponentIdProvider.TARGET_TBL_SEARCH_RESET_ID, this::getSearchTextFromUiState, this::searchBy);
        this.filterButtonsHeaderSupport = new FilterButtonsHeaderSupport(i18n, UIComponentIdProvider.SHOW_TARGET_TAGS,
                this::showFilterButtonsLayout, this::onLoadIsShowFilterButtonDisplayed);
        // TODO: consider moving permission check to header support or parent
        // header
        if (permChecker.hasCreateTargetPermission()) {
            this.addHeaderSupport = new AddHeaderSupport(i18n, UIComponentIdProvider.TARGET_TBL_ADD_ICON_ID,
                    this::addNewItem, this::onLoadIsTableMaximized);
            this.bulkUploadHeaderSupport = new BulkUploadHeaderSupport(i18n, this::showBulkUploadWindow,
                    this::isBulkUploadInProgress, this::onLoadIsTableMaximized);
        } else {
            this.addHeaderSupport = null;
            this.bulkUploadHeaderSupport = null;
        }

        this.resizeHeaderSupport = new ResizeHeaderSupport(i18n, UIComponentIdProvider.TARGET_MAX_MIN_TABLE_ICON,
                this::maximizeTable, this::minimizeTable, this::onLoadIsTableMaximized);
        addHeaderSupports(Arrays.asList(searchHeaderSupport, filterButtonsHeaderSupport, addHeaderSupport,
                bulkUploadHeaderSupport, resizeHeaderSupport));

        buildHeader();

        // DistributionSetFilterDropArea is only available in TargetTableHeader
        this.distributionSetFilterDropAreaSupport = new DistributionSetFilterDropAreaSupport(i18n, eventBus,
                notification, targetGridLayoutUiState);
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

        if (isBulkUploadInProgress()) {
            bulkUploadWindowBuilder.restoreState();
        }

        if (targetGridLayoutUiState.getFilterDsIdNameVersion() != null) {
            distributionSetFilterDropAreaSupport.restoreState();
        }
    }

    public void onSimpleFilterReset() {
        searchHeaderSupport.resetSearch();
        searchHeaderSupport.disableSearch();

        distributionSetFilterDropAreaSupport.reset();
    }

    private String getSearchTextFromUiState() {
        return targetGridLayoutUiState.getSearchFilter();
    }

    private void searchBy(final String newSearchText) {
        eventBus.publish(EventTopics.SEARCH_FILTER_CHANGED, this,
                new SearchFilterEventPayload(newSearchText, Layout.TARGET_LIST, View.DEPLOYMENT));

        targetGridLayoutUiState.setSearchFilter(newSearchText);
    }

    private void showFilterButtonsLayout() {
        eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this,
                new LayoutVisibilityEventPayload(VisibilityType.SHOW, Layout.TARGET_TAG_FILTER, View.DEPLOYMENT));

        targetTagFilterLayoutUiState.setHidden(false);
    }

    private Boolean onLoadIsShowFilterButtonDisplayed() {
        return targetTagFilterLayoutUiState.isHidden();
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
        eventBus.publish(CommandTopics.RESIZE_LAYOUT, this,
                new LayoutResizeEventPayload(ResizeType.MAXIMIZE, Layout.TARGET_LIST, View.DEPLOYMENT));

        if (addHeaderSupport != null) {
            addHeaderSupport.hideAddIcon();
        }

        if (bulkUploadHeaderSupport != null) {
            bulkUploadHeaderSupport.hideBulkUpload();
        }

        targetGridLayoutUiState.setMaximized(true);
    }

    private void minimizeTable() {
        eventBus.publish(CommandTopics.RESIZE_LAYOUT, this,
                new LayoutResizeEventPayload(ResizeType.MINIMIZE, Layout.TARGET_LIST, View.DEPLOYMENT));

        if (addHeaderSupport != null) {
            addHeaderSupport.showAddIcon();
        }

        if (bulkUploadHeaderSupport != null) {
            bulkUploadHeaderSupport.showBulkUpload();
        }

        targetGridLayoutUiState.setMaximized(false);
    }

    private void showBulkUploadWindow() {
        final Window bulkUploadTargetWindow = bulkUploadWindowBuilder.getWindowForTargetBulkUpload();
        UI.getCurrent().addWindow(bulkUploadTargetWindow);
        bulkUploadTargetWindow.setVisible(true);
    }

    private Boolean isBulkUploadInProgress() {
        return targetBulkUploadUiState.isInProgress();
    }

    public void onBulkUploadChanged(final BulkUploadEventPayload eventPayload) {
        bulkUploadWindowBuilder.getLayout().ifPresent(layout -> {
            switch (eventPayload.getBulkUploadState()) {
            case UPLOAD_STARTED:
                adaptbulkUploadHeaderAndUiState(true);
                layout.onStartOfUpload();
                break;
            case UPLOAD_FAILED:
                adaptbulkUploadHeaderAndUiState(false);
                layout.onUploadFailure(eventPayload.getFailureReason());
                break;
            case TARGET_PROVISIONING_STARTED:
                layout.onStartOfProvisioning();
                break;
            case TARGET_PROVISIONING_PROGRESS_UPDATED:
                layout.setProgressBarValue(eventPayload.getBulkUploadProgress());
                break;
            case TAGS_AND_DS_ASSIGNMENT_STARTED:
                layout.onStartOfAssignment();
                break;
            case TAGS_AND_DS_ASSIGNMENT_FAILED:
                layout.onAssignmentFailure(eventPayload.getFailureReason());
                break;
            case BULK_UPLOAD_COMPLETED:
                adaptbulkUploadHeaderAndUiState(false);
                layout.onUploadCompletion(eventPayload.getSuccessBulkUploadCount(),
                        eventPayload.getFailBulkUploadCount());
                break;
            }
        });
    }

    private void adaptbulkUploadHeaderAndUiState(final boolean isInProgress) {
        if (isInProgress) {
            bulkUploadHeaderSupport.showProgressIndicator();
        } else {
            bulkUploadHeaderSupport.hideProgressIndicator();
        }
        targetBulkUploadUiState.setInProgress(isInProgress);
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
