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
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.filters.SwFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.AssignedSoftwareModuleToProxyMapper;
import org.eclipse.hawkbit.ui.common.data.mappers.SoftwareModuleToProxyMapper;
import org.eclipse.hawkbit.ui.common.data.providers.SoftwareModuleDistributionsStateDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.SmModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.ResizeSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.distributions.DistributionsView;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;

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
    private static final String SM_VENDOR_ID = "smVendor";
    private static final String SM_DELETE_BUTTON_ID = "smDeleteButton";

    private final SwModuleGridLayoutUiState swModuleGridLayoutUiState;
    private final transient SoftwareModuleManagement softwareModuleManagement;

    private final transient AssignedSoftwareModuleToProxyMapper assignedSoftwareModuleToProxyMapper;
    private final ConfigurableFilterDataProvider<ProxySoftwareModule, Void, SwFilterParams> swModuleDataProvider;
    private final SwFilterParams smFilter;

    private final transient DeleteSupport<ProxySoftwareModule> swModuleDeleteSupport;
    private final transient DragAndDropSupport<ProxySoftwareModule> dragAndDropSupport;

    public SwModuleGrid(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final SoftwareModuleManagement softwareModuleManagement,
            final SwModuleGridLayoutUiState swModuleGridLayoutUiState,
            final SoftwareModuleToProxyMapper softwareModuleToProxyMapper) {
        super(i18n, eventBus, permissionChecker);

        this.swModuleGridLayoutUiState = swModuleGridLayoutUiState;
        this.softwareModuleManagement = softwareModuleManagement;

        this.assignedSoftwareModuleToProxyMapper = new AssignedSoftwareModuleToProxyMapper(softwareModuleToProxyMapper);
        this.swModuleDataProvider = new SoftwareModuleDistributionsStateDataProvider(softwareModuleManagement,
                assignedSoftwareModuleToProxyMapper).withConfigurableFilter();
        this.smFilter = new SwFilterParams();

        setResizeSupport(new SwModuleResizeSupport());

        setSelectionSupport(new SelectionSupport<ProxySoftwareModule>(this, eventBus, DistributionsView.VIEW_NAME,
                this::updateLastSelectedSmUiState));
        if (swModuleGridLayoutUiState.isMaximized()) {
            getSelectionSupport().disableSelection();
        } else {
            getSelectionSupport().enableMultiSelection();
        }

        this.swModuleDeleteSupport = new DeleteSupport<>(this, i18n, i18n.getMessage("caption.software.module"),
                permissionChecker, notification, this::deleteSoftwareModules);

        this.dragAndDropSupport = new DragAndDropSupport<>(this, i18n, notification, Collections.emptyMap());
        this.dragAndDropSupport.addDragSource();

        initMasterDsStyleGenerator();
        init();
    }

    private void updateLastSelectedSmUiState(final SelectionChangedEventType type,
            final ProxySoftwareModule selectedSm) {
        if (type == SelectionChangedEventType.ENTITY_DESELECTED) {
            swModuleGridLayoutUiState.setSelectedSmId(null);
        } else {
            swModuleGridLayoutUiState.setSelectedSmId(selectedSm.getId());
        }
    }

    private void deleteSoftwareModules(final Collection<ProxySoftwareModule> swModulesToBeDeleted) {
        final Collection<Long> swModuleToBeDeletedIds = swModulesToBeDeleted.stream()
                .map(ProxyIdentifiableEntity::getId).collect(Collectors.toList());
        softwareModuleManagement.delete(swModuleToBeDeletedIds);

        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new SmModifiedEventPayload(EntityModifiedEventType.ENTITY_REMOVED, swModuleToBeDeletedIds));
    }

    private void initMasterDsStyleGenerator() {
        setStyleGenerator(sm -> {
            if (smFilter.getLastSelectedDistributionId() == null) {
                return null;
            }
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

    @Override
    public String getGridId() {
        return UIComponentIdProvider.SOFTWARE_MODULE_TABLE;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxySoftwareModule, Void, SwFilterParams> getFilterDataProvider() {
        return swModuleDataProvider;
    }

    public void updateSearchFilter(final String searchFilter) {
        smFilter.setSearchText(!StringUtils.isEmpty(searchFilter) ? String.format("%%%s%%", searchFilter) : null);
        getFilterDataProvider().setFilter(smFilter);
    }

    public void updateTypeFilter(final SoftwareModuleType typeFilter) {
        smFilter.setSoftwareModuleTypeId(typeFilter != null ? typeFilter.getId() : null);
        getFilterDataProvider().setFilter(smFilter);
    }

    public void updateMasterEntityFilter(final Long masterEntityId) {
        smFilter.setLastSelectedDistributionId(masterEntityId);
        getFilterDataProvider().setFilter(smFilter);
    }

    /**
     * Creates the grid content for maximized-state.
     */
    public void createMaximizedContent() {
        getSelectionSupport().disableSelection();
        getResizeSupport().createMaximizedContent();
        recalculateColumnWidths();
    }

    /**
     * Creates the grid content for normal (minimized) state.
     */
    public void createMinimizedContent() {
        getSelectionSupport().enableMultiSelection();
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
        addComponentColumn(sm -> buildActionButton(
                clickEvent -> swModuleDeleteSupport.openConfirmationWindowDeleteAction(sm, sm.getNameAndVersion()),
                VaadinIcons.TRASH, UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.SM_DELET_ICON + "." + sm.getId(), swModuleDeleteSupport.hasDeletePermission()))
                        .setId(SM_DELETE_BUTTON_ID).setCaption(i18n.getMessage("header.action.delete"))
                        .setMinimumWidth(80d);
    }

    // TODO: remove duplication
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
