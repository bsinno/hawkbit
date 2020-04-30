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
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.GridComponentBuilder;
import org.eclipse.hawkbit.ui.common.data.filters.SwFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.AssignedSoftwareModuleToProxyMapper;
import org.eclipse.hawkbit.ui.common.data.mappers.SoftwareModuleToProxyMapper;
import org.eclipse.hawkbit.ui.common.data.providers.SoftwareModuleDistributionsStateDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.FilterSupport;
import org.eclipse.hawkbit.ui.common.grid.support.ResizeSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.common.state.TypeFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;

/**
 * Software Module grid which is shown on the Distributions View.
 */
public class SwModuleGrid extends AbstractGrid<ProxySoftwareModule, SwFilterParams>
        implements MasterEntityAwareComponent<ProxyDistributionSet> {
    private static final long serialVersionUID = 1L;

    private static final String SM_NAME_ID = "smName";
    private static final String SM_VERSION_ID = "smVersion";
    private static final String SM_CREATED_BY_ID = "smCreatedBy";
    private static final String SM_CREATED_DATE_ID = "smCreatedDate";
    private static final String SM_MODIFIED_BY_ID = "smModifiedBy";
    private static final String SM_MODIFIED_DATE_ID = "smModifiedDate";
    private static final String SM_DESC_ID = "smDescription";
    private static final String SM_VENDOR_ID = "smVendor";
    private static final String SM_DELETE_BUTTON_ID = "smDeleteButton";

    private final TypeFilterLayoutUiState smTypeFilterLayoutUiState;
    private final SwModuleGridLayoutUiState swModuleGridLayoutUiState;
    private final transient SoftwareModuleManagement softwareModuleManagement;
    private final transient SoftwareModuleToProxyMapper softwareModuleToProxyMapper;
    private final transient AssignedSoftwareModuleToProxyMapper assignedSoftwareModuleToProxyMapper;

    private final transient DeleteSupport<ProxySoftwareModule> swModuleDeleteSupport;
    private final transient DragAndDropSupport<ProxySoftwareModule> dragAndDropSupport;
    private final transient FilterSupport<ProxySoftwareModule, SwFilterParams> filterSupport;

    public SwModuleGrid(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final SoftwareModuleManagement softwareModuleManagement,
            final TypeFilterLayoutUiState smTypeFilterLayoutUiState,
            final SwModuleGridLayoutUiState swModuleGridLayoutUiState) {
        super(i18n, eventBus, permissionChecker);

        this.smTypeFilterLayoutUiState = smTypeFilterLayoutUiState;
        this.swModuleGridLayoutUiState = swModuleGridLayoutUiState;
        this.softwareModuleManagement = softwareModuleManagement;
        this.softwareModuleToProxyMapper = new SoftwareModuleToProxyMapper();
        this.assignedSoftwareModuleToProxyMapper = new AssignedSoftwareModuleToProxyMapper(softwareModuleToProxyMapper);

        setResizeSupport(new SwModuleResizeSupport());

        setSelectionSupport(new SelectionSupport<ProxySoftwareModule>(this, eventBus, EventLayout.SM_LIST,
                EventView.DISTRIBUTIONS, this::mapIdToProxyEntity, this::getSelectedEntityIdFromUiState,
                this::setSelectedEntityIdToUiState));
        if (swModuleGridLayoutUiState.isMaximized()) {
            getSelectionSupport().disableSelection();
        } else {
            getSelectionSupport().enableMultiSelection();
        }

        this.swModuleDeleteSupport = new DeleteSupport<>(this, i18n, notification,
                i18n.getMessage("caption.software.module"), ProxySoftwareModule::getNameAndVersion,
                this::deleteSoftwareModules, UIComponentIdProvider.SM_DELETE_CONFIRMATION_DIALOG);

        this.dragAndDropSupport = new DragAndDropSupport<>(this, i18n, notification, Collections.emptyMap(), eventBus);
        if (!swModuleGridLayoutUiState.isMaximized()) {
            this.dragAndDropSupport.addDragSource();
        }

        this.filterSupport = new FilterSupport<>(new SoftwareModuleDistributionsStateDataProvider(
                softwareModuleManagement, assignedSoftwareModuleToProxyMapper), getSelectionSupport()::deselectAll);
        this.filterSupport.setFilter(new SwFilterParams());

        initFilterMappings();
        initMasterDsStyleGenerator();
        init();
    }

    private void initFilterMappings() {
        filterSupport.addMapping(FilterType.SEARCH, SwFilterParams::setSearchText);
        filterSupport.addMapping(FilterType.TYPE, SwFilterParams::setSoftwareModuleTypeId);
    }

    @Override
    protected void init() {
        super.init();

        addStyleName("grid-row-border");
    }

    public Optional<ProxySoftwareModule> mapIdToProxyEntity(final long entityId) {
        return softwareModuleManagement.get(entityId).map(softwareModuleToProxyMapper::map);
    }

    private Optional<Long> getSelectedEntityIdFromUiState() {
        return Optional.ofNullable(swModuleGridLayoutUiState.getSelectedSmId());
    }

    private void setSelectedEntityIdToUiState(final Optional<Long> entityId) {
        swModuleGridLayoutUiState.setSelectedSmId(entityId.orElse(null));
    }

    private void deleteSoftwareModules(final Collection<ProxySoftwareModule> swModulesToBeDeleted) {
        final Collection<Long> swModuleToBeDeletedIds = swModulesToBeDeleted.stream()
                .map(ProxyIdentifiableEntity::getId).collect(Collectors.toList());
        softwareModuleManagement.delete(swModuleToBeDeletedIds);

        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                EntityModifiedEventType.ENTITY_REMOVED, ProxySoftwareModule.class, swModuleToBeDeletedIds));
    }

    private void initMasterDsStyleGenerator() {
        setStyleGenerator(sm -> {
            if (getFilter().getLastSelectedDistributionId() == null || !sm.isAssigned()) {
                return null;
            }

            return String.join("-", UIComponentIdProvider.SM_TYPE_COLOR_CLASS,
                    String.valueOf(sm.getProxyType().getId()));
        });
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.SOFTWARE_MODULE_TABLE;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxySoftwareModule, Void, SwFilterParams> getFilterDataProvider() {
        return filterSupport.getFilterDataProvider();
    }

    @Override
    public void masterEntityChanged(final ProxyDistributionSet masterEntity) {
        if (masterEntity == null && getMasterEntityId() == null) {
            return;
        }

        final Long masterEntityId = masterEntity != null ? masterEntity.getId() : null;
        getFilter().setLastSelectedDistributionId(masterEntityId);
        filterSupport.refreshFilter();

        getSelectionSupport().deselectAll();
    }

    public Long getMasterEntityId() {
        return getFilter().getLastSelectedDistributionId();
    }

    /**
     * Creates the grid content for maximized-state.
     */
    public void createMaximizedContent() {
        getSelectionSupport().disableSelection();
        dragAndDropSupport.removeDragSource();
        getResizeSupport().createMaximizedContent();
        recalculateColumnWidths();
    }

    /**
     * Creates the grid content for normal (minimized) state.
     */
    public void createMinimizedContent() {
        getSelectionSupport().enableMultiSelection();
        dragAndDropSupport.addDragSource();
        getResizeSupport().createMinimizedContent();
        recalculateColumnWidths();
    }

    @Override
    public void addColumns() {
        addColumn(ProxySoftwareModule::getName).setId(SM_NAME_ID).setCaption(i18n.getMessage("header.name"))
                .setMinimumWidth(100d).setExpandRatio(1);

        addColumn(ProxySoftwareModule::getVersion).setId(SM_VERSION_ID).setCaption(i18n.getMessage("header.version"))
                .setMinimumWidth(100d);

        addActionColumns();

        addColumn(ProxySoftwareModule::getCreatedBy).setId(SM_CREATED_BY_ID)
                .setCaption(i18n.getMessage("header.createdBy")).setHidden(true);

        addColumn(ProxySoftwareModule::getCreatedDate).setId(SM_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate")).setHidden(true);

        addColumn(ProxySoftwareModule::getLastModifiedBy).setId(SM_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy")).setHidden(true);

        addColumn(ProxySoftwareModule::getModifiedDate).setId(SM_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setHidden(true);

        addColumn(ProxySoftwareModule::getDescription).setId(SM_DESC_ID)
                .setCaption(i18n.getMessage("header.description")).setHidden(true);

        addColumn(ProxySoftwareModule::getVendor).setId(SM_VENDOR_ID).setCaption(i18n.getMessage("header.vendor"))
                .setHidden(true);
    }

    private void addActionColumns() {
        addComponentColumn(sm -> GridComponentBuilder.buildActionButton(i18n,
                clickEvent -> swModuleDeleteSupport.openConfirmationWindowDeleteAction(sm), VaadinIcons.TRASH,
                UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.SM_DELET_ICON + "." + sm.getId(),
                permissionChecker.hasDeleteRepositoryPermission())).setId(SM_DELETE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.delete")).setMinimumWidth(80d);
    }

    public void restoreState() {
        getFilter().setSearchText(swModuleGridLayoutUiState.getSearchFilter());
        getFilter().setSoftwareModuleTypeId(smTypeFilterLayoutUiState.getClickedTypeId());

        filterSupport.refreshFilter();
        getSelectionSupport().restoreSelection();
    }

    public SwFilterParams getFilter() {
        return filterSupport.getFilter();
    }

    public FilterSupport<ProxySoftwareModule, SwFilterParams> getFilterSupport() {
        return filterSupport;
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

            getColumns().forEach(column -> column.setHidable(true));
        }

        @Override
        public void setMaximizedColumnExpandRatio() {
            getColumns().forEach(column -> column.setExpandRatio(0));

            getColumn(SM_NAME_ID).setExpandRatio(1);
            getColumn(SM_DESC_ID).setExpandRatio(1);
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
            getColumn(SM_VENDOR_ID).setHidden(true);

            getColumns().forEach(column -> column.setHidable(false));
        }

        @Override
        public void setMinimizedColumnExpandRatio() {
            getColumns().forEach(column -> column.setExpandRatio(0));

            getColumn(SM_NAME_ID).setExpandRatio(1);
        }
    }
}
