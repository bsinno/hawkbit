/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.entity.DistributionSetIdName;
import org.eclipse.hawkbit.ui.common.table.AbstractTableHeader;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.management.event.BulkUploadPopupEvent;
import org.eclipse.hawkbit.ui.management.event.BulkUploadValidationMessageEvent;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.event.TargetFilterEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent.TargetComponentEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUITargetDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.cronutils.utils.StringUtils;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.dnd.DropTargetExtension;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.Label;

/**
 * Target table header layout.
 */
public class TargetTableHeader extends AbstractTableHeader {

    private static final long serialVersionUID = 1L;

    private final UINotification notification;

    private final TargetAddUpdateWindowLayout targetAddUpdateWindow;

    private final TargetBulkUpdateWindowLayout targetBulkUpdateWindow;

    private final HorizontalLayout distributionSetFilterDropArea;

    private boolean isComplexFilterViewDisplayed;

    private final transient DistributionSetManagement distributionSetManagement;

    private final Button bulkUploadIcon;

    TargetTableHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker, final UIEventBus eventBus,
            final UINotification notification, final ManagementUIState managementUIState,
            final TargetManagement targetManagement, final DeploymentManagement deploymentManagement,
            final UiProperties uiproperties, final EntityFactory entityFactory, final UINotification uiNotification,
            final TargetTagManagement tagManagement, final DistributionSetManagement distributionSetManagement,
            final Executor uiExecutor) {
        super(i18n, permChecker, eventBus, managementUIState, null, null);

        this.notification = notification;
        this.distributionSetManagement = distributionSetManagement;

        this.targetAddUpdateWindow = new TargetAddUpdateWindowLayout(i18n, targetManagement, eventBus, uiNotification,
                entityFactory);
        this.targetBulkUpdateWindow = new TargetBulkUpdateWindowLayout(i18n, targetManagement, eventBus,
                managementUIState, deploymentManagement, uiproperties, permChecker, uiNotification, tagManagement,
                distributionSetManagement, entityFactory, uiExecutor);

        this.bulkUploadIcon = createBulkUploadIcon();
        // TODO: fix after headers unification
        if (hasCreatePermission()) {
            titleFilterIconsLayout.addComponent(bulkUploadIcon, titleFilterIconsLayout.getComponentCount() - 1);
            titleFilterIconsLayout.setComponentAlignment(bulkUploadIcon, Alignment.TOP_RIGHT);
        }

        this.distributionSetFilterDropArea = buildDistributionSetFilterDropArea();
        addDistributionSetFilterDropArea();

        onLoadRestoreState();
    }

    private Button createBulkUploadIcon() {
        final Button button = SPUIComponentProvider.getButton(UIComponentIdProvider.TARGET_TBL_BULK_UPLOAD_ICON_ID, "",
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_BULK_UPLOAD), null, false, FontAwesome.UPLOAD,
                SPUIButtonStyleNoBorder.class);
        button.addClickListener(this::bulkUpload);
        return button;
    }

    private void bulkUpload(final ClickEvent event) {
        targetBulkUpdateWindow.resetComponents();
        openBulkUploadWindow();
    }

    private void openBulkUploadWindow() {
        final Window bulkUploadTargetWindow = targetBulkUpdateWindow.getWindow();
        UI.getCurrent().addWindow(bulkUploadTargetWindow);
        bulkUploadTargetWindow.setVisible(true);
    }

    private HorizontalLayout buildDistributionSetFilterDropArea() {
        final HorizontalLayout dropArea = new HorizontalLayout();

        dropArea.setId(UIComponentIdProvider.TARGET_DROP_FILTER_ICON);
        dropArea.setStyleName("target-dist-filter-info");
        dropArea.setHeightUndefined();
        dropArea.setSizeUndefined();

        return dropArea;
    }

    private void addDistributionSetFilterDropArea() {
        final HorizontalLayout dropHintDropFilterLayout = new HorizontalLayout();

        dropHintDropFilterLayout.addStyleName("filter-drop-hint-layout");
        dropHintDropFilterLayout.setWidth(100, Unit.PERCENTAGE);

        // TODO: check if it works
        final DropTargetExtension<HorizontalLayout> dropTarget = new DropTargetExtension<>(
                distributionSetFilterDropArea);
        dropTarget.addDropListener(event -> {
            final String sourceId = event.getDataTransferData("source_id").orElse("");
            final Object sourceDragData = event.getDragData().orElse(null);

            if (!isDropValid(sourceId, sourceDragData)) {
                return;
            }

            if (sourceDragData instanceof List) {
                filterByDroppedDistSets((List<ProxyDistributionSet>) sourceDragData);
            } else {
                notification.displayValidationError(i18n.getMessage("message.action.did.not.work"));
            }
        });

        getManagementUIState().getTargetTableFilters().getDistributionSet()
                .ifPresent(this::addDsFilterDropAreaTextField);
        dropHintDropFilterLayout.addComponent(distributionSetFilterDropArea);
        dropHintDropFilterLayout.setComponentAlignment(distributionSetFilterDropArea, Alignment.TOP_CENTER);
        dropHintDropFilterLayout.setExpandRatio(distributionSetFilterDropArea, 1.0F);

        addComponent(dropHintDropFilterLayout);
        setComponentAlignment(dropHintDropFilterLayout, Alignment.TOP_CENTER);
    }

    private boolean isDropValid(final String sourceId, final Object sourceDragData) {
        // TODO: adapt message for isComplexFilterViewDisplayed case (e.g.
        // "Filter by DS is not allowed while Custom Filter is active")
        if (StringUtils.isEmpty(sourceId) || !sourceId.equals(UIComponentIdProvider.DIST_TABLE_ID)
                || isComplexFilterViewDisplayed || sourceDragData == null) {
            notification.displayValidationError(i18n.getMessage(UIMessageIdProvider.MESSAGE_ACTION_NOT_ALLOWED));
            return false;
        }

        return true;
    }

    private void filterByDroppedDistSets(final List<ProxyDistributionSet> droppedDistSets) {
        if (droppedDistSets.size() != 1) {
            notification.displayValidationError(i18n.getMessage("message.onlyone.distribution.dropallowed"));
            return;
        }

        final Long droppedDistSetId = droppedDistSets.get(0).getId();
        final Optional<DistributionSet> distributionSet = distributionSetManagement.get(droppedDistSetId);
        if (!distributionSet.isPresent()) {
            notification.displayWarning(i18n.getMessage("distributionset.not.exists"));
            return;
        }
        final DistributionSetIdName distributionSetIdName = new DistributionSetIdName(distributionSet.get());
        getManagementUIState().getTargetTableFilters().setDistributionSet(distributionSetIdName);

        addDsFilterDropAreaTextField(distributionSetIdName);
    }

    private void addDsFilterDropAreaTextField(final DistributionSetIdName distributionSetIdName) {
        final Button filterLabelClose = SPUIComponentProvider.getButton("drop.filter.close", "", "", "", true,
                FontAwesome.TIMES_CIRCLE, SPUIButtonStyleNoBorder.class);
        filterLabelClose.addClickListener(clickEvent -> closeFilterByDistribution());

        final Label filteredDistLabel = new Label();
        filteredDistLabel.setStyleName(ValoTheme.LABEL_COLORED + " " + ValoTheme.LABEL_SMALL);
        String name = HawkbitCommonUtil.getDistributionNameAndVersion(distributionSetIdName.getName(),
                distributionSetIdName.getVersion());
        if (name.length() > SPUITargetDefinitions.DISTRIBUTION_NAME_MAX_LENGTH_ALLOWED) {
            name = new StringBuilder(name.substring(0, SPUITargetDefinitions.DISTRIBUTION_NAME_LENGTH_ON_FILTER))
                    .append("...").toString();
        }
        filteredDistLabel.setValue(name);
        filteredDistLabel.setSizeUndefined();

        distributionSetFilterDropArea.removeAllComponents();
        distributionSetFilterDropArea.setSizeFull();
        distributionSetFilterDropArea.addComponent(filteredDistLabel);
        distributionSetFilterDropArea.addComponent(filterLabelClose);
        distributionSetFilterDropArea.setExpandRatio(filteredDistLabel, 1.0F);

        eventBus.publish(this, TargetFilterEvent.FILTER_BY_DISTRIBUTION);
    }

    private void closeFilterByDistribution() {

        /* Remove filter by distribution information. */
        distributionSetFilterDropArea.removeAllComponents();
        distributionSetFilterDropArea.setSizeUndefined();
        /* Remove distribution Id from target filter parameters */
        getManagementUIState().getTargetTableFilters().setDistributionSet(null);

        /* Reload the table */
        eventBus.publish(this, TargetFilterEvent.REMOVE_FILTER_BY_DISTRIBUTION);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ManagementUIEvent event) {
        if (event == ManagementUIEvent.HIDE_TARGET_TAG_LAYOUT) {
            setFilterButtonsIconVisible(true);
        } else if (event == ManagementUIEvent.SHOW_TARGET_TAG_LAYOUT) {
            setFilterButtonsIconVisible(false);
        } else if (event == ManagementUIEvent.RESET_SIMPLE_FILTERS) {
            UI.getCurrent().access(this::onSimpleFilterReset);
        } else if (event == ManagementUIEvent.RESET_TARGET_FILTER_QUERY) {
            UI.getCurrent().access(this::onCustomFilterReset);
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final BulkUploadPopupEvent event) {
        if (BulkUploadPopupEvent.MAXIMIMIZED == event) {
            targetBulkUpdateWindow.restoreComponentsValue();
            openBulkUploadWindow();
        } else if (BulkUploadPopupEvent.CLOSED == event) {
            UI.getCurrent().access(() -> bulkUploadIcon.setEnabled(true));
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
                    getManagementUIState().getTargetTableFilters().getBulkUpload().getProgressBarCurrentValue()));
        } else if (TargetComponentEvent.BULK_UPLOAD_COMPLETED == event.getTargetComponentEvent()) {
            this.getUI().access(targetBulkUpdateWindow::onUploadCompletion);
        } else if (TargetComponentEvent.BULK_TARGET_UPLOAD_STARTED == event.getTargetComponentEvent()) {
            this.getUI().access(this::onStartOfBulkUpload);
        } else if (TargetComponentEvent.BULK_UPLOAD_PROCESS_STARTED == event.getTargetComponentEvent()) {
            this.getUI().access(() -> targetBulkUpdateWindow.getBulkUploader().getUpload().setEnabled(false));
        }
    }

    private void onStartOfBulkUpload() {
        bulkUploadIcon.setEnabled(false);
        targetBulkUpdateWindow.onStartOfUpload();
    }

    private void onCustomFilterReset() {
        isComplexFilterViewDisplayed = Boolean.FALSE;
        reEnableSearch();
    }

    private void onLoadRestoreState() {
        if (getManagementUIState().isCustomFilterSelected()) {
            onSimpleFilterReset();
        }

        if (isBulkUploadInProgress()) {
            bulkUploadIcon.setEnabled(false);
        }

        if (onLoadIsTableMaximized()) {
            bulkUploadIcon.setVisible(false);
        }
    }

    private void onSimpleFilterReset() {
        isComplexFilterViewDisplayed = Boolean.TRUE;
        disableSearch();
        if (isSearchFieldOpen()) {
            resetSearch();
        }
        if (getManagementUIState().getTargetTableFilters().getDistributionSet().isPresent()) {
            closeFilterByDistribution();
        }
    }

    @Override
    protected String getHeaderCaption() {
        return i18n.getMessage("header.target.table");
    }

    @Override
    protected String getSearchBoxId() {
        return UIComponentIdProvider.TARGET_TEXT_FIELD;
    }

    @Override
    protected String getSearchRestIconId() {
        return UIComponentIdProvider.TARGET_TBL_SEARCH_RESET_ID;
    }

    @Override
    protected String getAddIconId() {
        return UIComponentIdProvider.TARGET_TBL_ADD_ICON_ID;
    }

    @Override
    protected String onLoadSearchBoxValue() {
        return getSearchText();
    }

    @Override
    protected boolean hasCreatePermission() {
        return permChecker.hasCreateTargetPermission();
    }

    @Override
    protected String getShowFilterButtonLayoutId() {
        return UIComponentIdProvider.SHOW_TARGET_TAGS;
    }

    @Override
    protected void showFilterButtonsLayout() {
        getManagementUIState().setTargetTagFilterClosed(false);
        eventBus.publish(this, ManagementUIEvent.SHOW_TARGET_TAG_LAYOUT);
    }

    @Override
    protected void resetSearchText() {
        if (getManagementUIState().getTargetTableFilters().getSearchText().isPresent()) {
            getManagementUIState().getTargetTableFilters().setSearchText(null);
            eventBus.publish(this, TargetFilterEvent.REMOVE_FILTER_BY_TEXT);
        }
    }

    private String getSearchText() {
        return getManagementUIState().getTargetTableFilters().getSearchText().orElse(null);
    }

    @Override
    protected String getMaxMinIconId() {
        return UIComponentIdProvider.TARGET_MAX_MIN_TABLE_ICON;
    }

    @Override
    public void maximizeTable() {
        bulkUploadIcon.setVisible(false);
        getManagementUIState().setTargetTableMaximized(Boolean.TRUE);
        eventBus.publish(this, new TargetTableEvent(BaseEntityEventType.MAXIMIZED));
    }

    @Override
    public void minimizeTable() {
        bulkUploadIcon.setVisible(true);
        getManagementUIState().setTargetTableMaximized(Boolean.FALSE);
        eventBus.publish(this, new TargetTableEvent(BaseEntityEventType.MINIMIZED));
    }

    @Override
    public Boolean onLoadIsTableMaximized() {
        return getManagementUIState().isTargetTableMaximized();
    }

    @Override
    public Boolean onLoadIsShowFilterButtonDisplayed() {
        return getManagementUIState().isTargetTagFilterClosed();
    }

    @Override
    protected void searchBy(final String newSearchText) {
        getManagementUIState().getTargetTableFilters().setSearchText(newSearchText);
        eventBus.publish(this, TargetFilterEvent.FILTER_BY_TEXT);
    }

    @Override
    protected void addNewItem(final ClickEvent event) {
        targetAddUpdateWindow.resetComponents();
        final Window addTargetWindow = targetAddUpdateWindow.createNewWindow();
        addTargetWindow.setCaption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.target")));
        UI.getCurrent().addWindow(addTargetWindow);
        addTargetWindow.setVisible(Boolean.TRUE);
    }

    private boolean isBulkUploadInProgress() {
        return getManagementUIState().getTargetTableFilters().getBulkUpload().getSucessfulUploadCount() != 0
                || getManagementUIState().getTargetTableFilters().getBulkUpload().getFailedUploadCount() != 0;
    }

    @Override
    protected Boolean isAddNewItemAllowed() {
        return Boolean.TRUE;
    }
}
