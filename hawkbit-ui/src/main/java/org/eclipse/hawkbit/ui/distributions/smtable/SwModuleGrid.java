/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.smtable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.event.remote.entity.RemoteEntityEvent;
import org.eclipse.hawkbit.repository.model.AssignedSoftwareModule;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.event.RefreshSoftwareModuleByFilterEvent;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent;
import org.eclipse.hawkbit.ui.common.data.filters.SwFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.AssignedSoftwareModuleToProxyMapper;
import org.eclipse.hawkbit.ui.common.data.mappers.SoftwareModuleToProxyMapper;
import org.eclipse.hawkbit.ui.common.data.providers.SoftwareModuleDistributionsStateDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.ResizeSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.distributions.event.DistributionsUIEvent;
import org.eclipse.hawkbit.ui.distributions.event.SaveActionWindowEvent;
import org.eclipse.hawkbit.ui.distributions.state.ManageDistUIState;
import org.eclipse.hawkbit.ui.management.event.RefreshDistributionTableByFilterEvent;
import org.eclipse.hawkbit.ui.push.SoftwareModuleUpdatedEventContainer;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.eclipse.hawkbit.ui.view.filter.OnlyEventsFromDistributionsViewFilter;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.UI;

/**
 * Software Module grid which is shown on the Distributions View.
 */
public class SwModuleGrid extends AbstractGrid<ProxySoftwareModule, SwFilterParams> {
    private static final long serialVersionUID = 1L;

    private static final String SM_NAME_ID = "smName";
    private static final String SM_VERSION_ID = "smVersion";
    private static final String SM_CREATED_BY_ID = "smCreatedBy";
    private static final String SM_CREATED_DATE_ID = "smCreatedDate";
    private static final String SM_MODIFIED_BY_ID = "smModifiedBy";
    private static final String SM_MODIFIED_DATE_ID = "smModifiedDate";
    private static final String SM_DESC_ID = "smDescription";
    private static final String SM_VENDOR_ID = "smDescription";
    private static final String SM_DELETE_BUTTON_ID = "smDeleteButton";

    private final ManageDistUIState manageDistUIState;
    private final transient SoftwareModuleManagement softwareModuleManagement;

    private final ConfigurableFilterDataProvider<ProxySoftwareModule, Void, SwFilterParams> swModuleDataProvider;
    private final AssignedSoftwareModuleToProxyMapper assignedSoftwareModuleToProxyMapper;
    private final DeleteSupport<ProxySoftwareModule> swModuleDeleteSupport;
    private final DragAndDropSupport<ProxySoftwareModule> dragAndDropSupport;

    public SwModuleGrid(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final ManageDistUIState manageDistUIState, final SoftwareModuleManagement softwareModuleManagement) {
        super(i18n, eventBus, permissionChecker);

        this.manageDistUIState = manageDistUIState;
        this.softwareModuleManagement = softwareModuleManagement;

        this.assignedSoftwareModuleToProxyMapper = new AssignedSoftwareModuleToProxyMapper(
                new SoftwareModuleToProxyMapper());
        this.swModuleDataProvider = new SoftwareModuleDistributionsStateDataProvider(softwareModuleManagement,
                assignedSoftwareModuleToProxyMapper).withConfigurableFilter();

        setResizeSupport(new SwModuleResizeSupport());

        setSelectionSupport(new SelectionSupport<ProxySoftwareModule>(this));
        if (manageDistUIState.isSwModuleTableMaximized()) {
            getSelectionSupport().disableSelection();
        } else {
            getSelectionSupport().enableMultiSelection();
        }

        this.swModuleDeleteSupport = new DeleteSupport<>(this, i18n, i18n.getMessage("caption.software.module"),
                permissionChecker, notification, this::swModuleIdsDeletionCallback);

        this.dragAndDropSupport = new DragAndDropSupport<>(this, i18n, notification, Collections.emptyMap());
        this.dragAndDropSupport.addDragSource();

        init();
    }

    private void swModuleIdsDeletionCallback(final Collection<Long> swModuleToBeDeletedIds) {
        softwareModuleManagement.delete(swModuleToBeDeletedIds);

        // TODO: should we really pass the swModuleToBeDeletedIds? We call
        // dataprovider refreshAll anyway after receiving the event
        eventBus.publish(this, new SoftwareModuleEvent(BaseEntityEventType.REMOVE_ENTITY, swModuleToBeDeletedIds));

        manageDistUIState.getSelectedSoftwareModules().clear();
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.SOFTWARE_MODULE_TABLE;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxySoftwareModule, Void, SwFilterParams> getFilterDataProvider() {
        return swModuleDataProvider;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onDistributionSetUpdateEvents(final SoftwareModuleUpdatedEventContainer eventContainer) {
        if (!eventContainer.getEvents().isEmpty()) {
            // TODO: Consider updating only corresponding distribution sets with
            // dataProvider.refreshItem() based on distribution set ids instead
            // of full refresh (evaluate getDataCommunicator().getKeyMapper())
            refreshContainer();
        }

        // TODO: do we really need it?
        publishSmSelectedEntityForRefresh(eventContainer.getEvents().stream());
    }

    private void publishSmSelectedEntityForRefresh(
            final Stream<? extends RemoteEntityEvent<SoftwareModule>> smEntityEventStream) {
        // TODO: check if we should work with AssignedSoftwareModule here
        // directly from the event
        smEntityEventStream.filter(event -> isLastSelectedSm(event.getEntityId())).filter(Objects::nonNull).findAny()
                .ifPresent(event -> eventBus.publish(this,
                        new SoftwareModuleEvent(BaseEntityEventType.SELECTED_ENTITY, assignedSoftwareModuleToProxyMapper
                                .map(new AssignedSoftwareModule(event.getEntity(), false)))));
    }

    private boolean isLastSelectedSm(final Long swModuleId) {
        return manageDistUIState.getLastSelectedSoftwareModule()
                .map(lastSelectedSmId -> lastSelectedSmId.equals(swModuleId)).orElse(false);
    }

    /**
     * DistributionTableFilterEvent.
     *
     * @param filterEvent
     *            as instance of {@link RefreshDistributionTableByFilterEvent}
     */
    @EventBusListenerMethod(scope = EventScope.UI, filter = OnlyEventsFromDistributionsViewFilter.class)
    void onEvent(final RefreshSoftwareModuleByFilterEvent filterEvent) {
        UI.getCurrent().access(() -> {
            refreshFilter();
            styleRowOnDistSelection();
        });
    }

    private void styleRowOnDistSelection() {
        setStyleGenerator(sm -> {
            // TODO: do something with color: {background-color:" + color + "
            // !important;background-image:none !important }
            // final String color = sm.getType().getColour() != null ?
            // sm.getType().getColour() :
            // SPUIDefinitions.DEFAULT_COLOR;
            // https://vaadin.com/docs/v8/framework/articles/DynamicallyInjectingCSS.html

            if (sm.isAssigned()) {
                return "distribution-upload-type-" + sm.getType().getId();
            }

            return null;
        });
    }

    private void refreshFilter() {
        final SwFilterParams filterParams = new SwFilterParams(getSearchTextFromUiState(),
                getSwModuleTypeIdFromUiState(), getLastSelectedDistributionIdFromUiState());

        getFilterDataProvider().setFilter(filterParams);
    }

    private String getSearchTextFromUiState() {
        return manageDistUIState.getSoftwareModuleFilters().getSearchText()
                .filter(searchText -> !StringUtils.isEmpty(searchText)).map(value -> String.format("%%%s%%", value))
                .orElse(null);
    }

    private Long getSwModuleTypeIdFromUiState() {
        return manageDistUIState.getSoftwareModuleFilters().getSoftwareModuleType().map(SoftwareModuleType::getId)
                .orElse(null);
    }

    private Long getLastSelectedDistributionIdFromUiState() {
        return manageDistUIState.getLastSelectedDistribution().orElse(null);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final SaveActionWindowEvent event) {
        // TODO: should we also check
        // SaveActionWindowEvent.SAVED_ASSIGNMENTS event?
        // TODO: is it sufficient to call refreshContainer?
        if (event == SaveActionWindowEvent.DELETE_ALL_SOFWARE) {
            UI.getCurrent().access(this::refreshFilter);
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final SoftwareModuleEvent event) {
        if (BaseEntityEventType.MINIMIZED == event.getEventType()) {
            UI.getCurrent().access(this::createMinimizedContent);
        } else if (BaseEntityEventType.MAXIMIZED == event.getEventType()) {
            UI.getCurrent().access(this::createMaximizedContent);
        } else if (BaseEntityEventType.ADD_ENTITY == event.getEventType()
                || BaseEntityEventType.REMOVE_ENTITY == event.getEventType()) {
            UI.getCurrent().access(this::refreshContainer);
        }

        if (BaseEntityEventType.UPDATED_ENTITY != event.getEventType()) {
            return;
        }
        UI.getCurrent().access(() -> updateSwModule(event.getEntity()));
    }

    /**
     * Creates the grid content for maximized-state.
     */
    private void createMaximizedContent() {
        getSelectionSupport().disableSelection();
        getResizeSupport().createMaximizedContent();
        recalculateColumnWidths();
    }

    /**
     * Creates the grid content for normal (minimized) state.
     */
    private void createMinimizedContent() {
        getSelectionSupport().enableMultiSelection();
        getResizeSupport().createMinimizedContent();
        recalculateColumnWidths();
    }

    /**
     * To update software module details in the grid.
     *
     * @param updatedSwModule
     *            as reference
     */
    public void updateSwModule(final ProxySoftwareModule updatedSwModule) {
        if (updatedSwModule != null) {
            getDataProvider().refreshItem(updatedSwModule);
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final DistributionsUIEvent event) {
        UI.getCurrent().access(() -> {
            if (event == DistributionsUIEvent.ORDER_BY_DISTRIBUTION) {
                refreshFilter();
                styleRowOnDistSelection();
            }
        });
    }

    @Override
    public void addColumns() {
        // TODO: check width
        addColumn(ProxySoftwareModule::getName).setId(SM_NAME_ID).setCaption(i18n.getMessage("header.name"))
                .setMinimumWidth(100d).setMaximumWidth(150d).setHidable(false).setHidden(false);

        addColumn(ProxySoftwareModule::getVersion).setId(SM_VERSION_ID).setCaption(i18n.getMessage("header.version"))
                .setMinimumWidth(50d).setMaximumWidth(100d).setHidable(false).setHidden(false);

        addActionColumns();

        addColumn(ProxySoftwareModule::getCreatedBy).setId(SM_CREATED_BY_ID)
                .setCaption(i18n.getMessage("header.createdBy")).setHidable(true).setHidden(true);

        addColumn(ProxySoftwareModule::getCreatedDate).setId(SM_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate")).setHidable(true).setHidden(true);

        addColumn(ProxySoftwareModule::getLastModifiedBy).setId(SM_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy")).setHidable(true).setHidden(true);

        addColumn(ProxySoftwareModule::getModifiedDate).setId(SM_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setHidable(true).setHidden(true);

        addColumn(ProxySoftwareModule::getDescription).setId(SM_DESC_ID)
                .setCaption(i18n.getMessage("header.description")).setHidable(true).setHidden(true);

        addColumn(ProxySoftwareModule::getVendor).setId(SM_VENDOR_ID).setCaption(i18n.getMessage("header.vendor"))
                .setHidable(true).setHidden(true);
    }

    private void addActionColumns() {
        addComponentColumn(sm -> buildActionButton(
                clickEvent -> swModuleDeleteSupport.openConfirmationWindowDeleteAction(sm, sm.getNameAndVersion()),
                VaadinIcons.TRASH, UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.SM_DELET_ICON + "." + sm.getId(), swModuleDeleteSupport.hasDeletePermission()))
                        .setId(SM_DELETE_BUTTON_ID).setCaption(i18n.getMessage("header.action.delete"))
                        .setMinimumWidth(50d).setMaximumWidth(50d).setHidable(false).setHidden(false);
    }

    private Button buildActionButton(final ClickListener clickListener, final VaadinIcons icon,
            final String descriptionProperty, final String style, final String buttonId, final boolean enabled) {
        final Button actionButton = new Button();

        actionButton.addClickListener(clickListener);
        actionButton.setIcon(icon);
        actionButton.setDescription(i18n.getMessage(descriptionProperty));
        actionButton.setEnabled(enabled);
        actionButton.setId(buttonId);
        actionButton.addStyleName("tiny");
        actionButton.addStyleName("borderless");
        actionButton.addStyleName("button-no-border");
        actionButton.addStyleName("action-type-padding");
        actionButton.addStyleName(style);

        return actionButton;
    }

    /**
     * Adds support to resize the SwModuleGrid grid.
     */
    class SwModuleResizeSupport implements ResizeSupport {

        private final String[] maxColumnOrder = new String[] { SM_NAME_ID, SM_CREATED_BY_ID, SM_CREATED_DATE_ID,
                SM_MODIFIED_BY_ID, SM_MODIFIED_DATE_ID, SM_DESC_ID, SM_VERSION_ID, SM_VENDOR_ID, SM_DELETE_BUTTON_ID };

        private final String[] minColumnOrder = new String[] { SM_NAME_ID, SM_VERSION_ID, SM_DELETE_BUTTON_ID };

        @Override
        public void setMaximizedColumnOrder() {
            clearSortOrder();
            setColumnOrder(maxColumnOrder);
        }

        @Override
        public void setMaximizedHiddenColumns() {
            getColumn(SM_CREATED_BY_ID).setHidden(false);
            getColumn(SM_CREATED_DATE_ID).setHidden(false);
            getColumn(SM_MODIFIED_BY_ID).setHidden(false);
            getColumn(SM_MODIFIED_DATE_ID).setHidden(false);
            getColumn(SM_DESC_ID).setHidden(false);
            getColumn(SM_VENDOR_ID).setHidden(false);
        }

        @Override
        public void setMaximizedColumnExpandRatio() {
        }

        @Override
        public void setMinimizedColumnOrder() {
            clearSortOrder();
            setColumnOrder(minColumnOrder);
        }

        @Override
        public void setMinimizedHiddenColumns() {
            getColumn(SM_CREATED_BY_ID).setHidden(true);
            getColumn(SM_CREATED_DATE_ID).setHidden(true);
            getColumn(SM_MODIFIED_BY_ID).setHidden(true);
            getColumn(SM_MODIFIED_DATE_ID).setHidden(true);
            getColumn(SM_DESC_ID).setHidden(true);
            getColumn(SM_VENDOR_ID).setHidden(false);
        }

        @Override
        public void setMinimizedColumnExpandRatio() {
        }
    }
}
