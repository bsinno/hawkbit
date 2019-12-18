/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.filters.DsDistributionsFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetDistributionsStateDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.DsModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.ResizeSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.AssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.SwModulesToDistributionSetAssignmentSupport;
import org.eclipse.hawkbit.ui.distributions.DistributionsView;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
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

//TODO: remove duplication with DistributionGrid
/**
 * Distribution set grid which is shown on the Distributions View.
 */
public class DistributionSetGrid extends AbstractGrid<ProxyDistributionSet, DsDistributionsFilterParams> {
    private static final long serialVersionUID = 1L;

    private static final String DS_NAME_ID = "dsName";
    private static final String DS_VERSION_ID = "dsVersion";
    private static final String DS_CREATED_BY_ID = "dsCreatedBy";
    private static final String DS_CREATED_DATE_ID = "dsCreatedDate";
    private static final String DS_MODIFIED_BY_ID = "dsModifiedBy";
    private static final String DS_MODIFIED_DATE_ID = "dsModifiedDate";
    private static final String DS_DESC_ID = "dsDescription";
    private static final String DS_DELETE_BUTTON_ID = "dsDeleteButton";

    private final DistributionSetGridLayoutUiState distributionSetGridLayoutUiState;
    private final transient DistributionSetManagement dsManagement;

    private final ConfigurableFilterDataProvider<ProxyDistributionSet, Void, DsDistributionsFilterParams> dsDataProvider;
    private final DsDistributionsFilterParams dsFilter;

    private final transient DeleteSupport<ProxyDistributionSet> distributionDeleteSupport;
    private final transient DragAndDropSupport<ProxyDistributionSet> dragAndDropSupport;

    public DistributionSetGrid(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final TargetManagement targetManagement, final DistributionSetManagement dsManagement,
            final SoftwareModuleManagement smManagement, final DistributionSetTypeManagement dsTypeManagement,
            final SoftwareModuleTypeManagement smTypeManagement,
            final DistributionSetGridLayoutUiState distributionSetGridLayoutUiState,
            final DistributionSetToProxyDistributionMapper dsToProxyDistributionMapper) {
        super(i18n, eventBus, permissionChecker);

        this.distributionSetGridLayoutUiState = distributionSetGridLayoutUiState;
        this.dsManagement = dsManagement;

        this.dsDataProvider = new DistributionSetDistributionsStateDataProvider(dsManagement, dsTypeManagement,
                dsToProxyDistributionMapper).withConfigurableFilter();
        this.dsFilter = new DsDistributionsFilterParams();

        setResizeSupport(new DistributionSetResizeSupport());

        setSelectionSupport(new SelectionSupport<ProxyDistributionSet>(this, eventBus, DistributionsView.VIEW_NAME,
                this::updateLastSelectedDsUiState));
        if (distributionSetGridLayoutUiState.isMaximized()) {
            getSelectionSupport().disableSelection();
        } else {
            getSelectionSupport().enableMultiSelection();
        }

        this.distributionDeleteSupport = new DeleteSupport<>(this, i18n, i18n.getMessage("distribution.details.header"),
                permissionChecker, notification, this::deleteDistributionSets);

        final Map<String, AssignmentSupport<?, ProxyDistributionSet>> sourceTargetAssignmentStrategies = new HashMap<>();
        final SwModulesToDistributionSetAssignmentSupport swModulesToDsAssignment = new SwModulesToDistributionSetAssignmentSupport(
                notification, i18n, eventBus, permissionChecker, targetManagement, dsManagement, smManagement,
                dsTypeManagement, smTypeManagement);
        sourceTargetAssignmentStrategies.put(UIComponentIdProvider.SOFTWARE_MODULE_TABLE, swModulesToDsAssignment);

        this.dragAndDropSupport = new DragAndDropSupport<>(this, i18n, notification, sourceTargetAssignmentStrategies);
        this.dragAndDropSupport.addDropTarget();

        initIsCompleteStyleGenerator();
        init();
    }

    private void updateLastSelectedDsUiState(final SelectionChangedEventType type,
            final ProxyDistributionSet selectedDs) {
        if (type == SelectionChangedEventType.ENTITY_DESELECTED) {
            distributionSetGridLayoutUiState.setSelectedDsId(null);
        } else {
            distributionSetGridLayoutUiState.setSelectedDsId(selectedDs.getId());
        }
    }

    private void deleteDistributionSets(final Collection<ProxyDistributionSet> setsToBeDeleted) {
        final Collection<Long> dsToBeDeletedIds = setsToBeDeleted.stream().map(ProxyIdentifiableEntity::getId)
                .collect(Collectors.toList());
        dsManagement.delete(dsToBeDeletedIds);

        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new DsModifiedEventPayload(EntityModifiedEventType.ENTITY_REMOVED, dsToBeDeletedIds));

        // TODO: check if we need to notify Deployment View if deleted DS was
        // pinned
        // getPinnedDsIdFromUiState()
        // .ifPresent(pinnedDsId ->
        // pinSupport.unPinItemAfterDeletion(pinnedDsId, dsToBeDeletedIds));
    }

    private void initIsCompleteStyleGenerator() {
        setStyleGenerator(ds -> ds.getIsComplete() ? null : SPUIDefinitions.DISABLE_DISTRIBUTION);
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.DIST_SET_TABLE_ID;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyDistributionSet, Void, DsDistributionsFilterParams> getFilterDataProvider() {
        return dsDataProvider;
    }

    public void updateSearchFilter(final String searchFilter) {
        dsFilter.setSearchText(!StringUtils.isEmpty(searchFilter) ? String.format("%%%s%%", searchFilter) : null);
        getFilterDataProvider().setFilter(dsFilter);
    }

    public void updateTypeFilter(final DistributionSetType typeFilter) {
        dsFilter.setDsTypeId(typeFilter != null ? typeFilter.getId() : null);
        getFilterDataProvider().setFilter(dsFilter);
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
        addColumn(ProxyDistributionSet::getName).setId(DS_NAME_ID).setCaption(i18n.getMessage("header.name"))
                .setMinimumWidth(100d).setExpandRatio(1);

        addColumn(ProxyDistributionSet::getVersion).setId(DS_VERSION_ID).setCaption(i18n.getMessage("header.version"))
                .setMinimumWidth(100d);

        addActionColumns();

        addColumn(ProxyDistributionSet::getCreatedBy).setId(DS_CREATED_BY_ID)
                .setCaption(i18n.getMessage("header.createdBy")).setHidden(true);

        addColumn(ProxyDistributionSet::getCreatedDate).setId(DS_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate")).setHidden(true);

        addColumn(ProxyDistributionSet::getLastModifiedBy).setId(DS_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy")).setHidden(true);

        addColumn(ProxyDistributionSet::getModifiedDate).setId(DS_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setHidden(true);

        addColumn(ProxyDistributionSet::getDescription).setId(DS_DESC_ID)
                .setCaption(i18n.getMessage("header.description")).setHidden(true);
    }

    private void addActionColumns() {
        addComponentColumn(ds -> buildActionButton(
                clickEvent -> distributionDeleteSupport.openConfirmationWindowDeleteAction(ds, ds.getNameVersion()),
                VaadinIcons.TRASH, UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.DIST_DELET_ICON + "." + ds.getId(),
                distributionDeleteSupport.hasDeletePermission())).setId(DS_DELETE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.delete")).setMinimumWidth(80d);
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
     * Adds support to resize the DistributionSet grid.
     */
    class DistributionSetResizeSupport implements ResizeSupport {

        private final String[] maxColumnOrder = new String[] { DS_NAME_ID, DS_CREATED_BY_ID, DS_CREATED_DATE_ID,
                DS_MODIFIED_BY_ID, DS_MODIFIED_DATE_ID, DS_DESC_ID, DS_VERSION_ID, DS_DELETE_BUTTON_ID };

        private final String[] minColumnOrder = new String[] { DS_NAME_ID, DS_VERSION_ID, DS_DELETE_BUTTON_ID };

        @Override
        public void setMaximizedColumnOrder() {
            clearSortOrder();
            setColumnOrder(maxColumnOrder);
        }

        @Override
        public void setMaximizedHiddenColumns() {
            getColumn(DS_CREATED_BY_ID).setHidden(false);
            getColumn(DS_CREATED_DATE_ID).setHidden(false);
            getColumn(DS_MODIFIED_BY_ID).setHidden(false);
            getColumn(DS_MODIFIED_DATE_ID).setHidden(false);
            getColumn(DS_DESC_ID).setHidden(false);

            getColumns().forEach(column -> column.setHidable(true));
        }

        @Override
        public void setMaximizedColumnExpandRatio() {
            getColumns().forEach(column -> column.setExpandRatio(0));

            getColumn(DS_NAME_ID).setExpandRatio(1);
            getColumn(DS_DESC_ID).setExpandRatio(1);
        }

        @Override
        public void setMinimizedColumnOrder() {
            clearSortOrder();
            setColumnOrder(minColumnOrder);
        }

        @Override
        public void setMinimizedHiddenColumns() {
            getColumn(DS_CREATED_BY_ID).setHidden(true);
            getColumn(DS_CREATED_DATE_ID).setHidden(true);
            getColumn(DS_MODIFIED_BY_ID).setHidden(true);
            getColumn(DS_MODIFIED_DATE_ID).setHidden(true);
            getColumn(DS_DESC_ID).setHidden(true);

            getColumns().forEach(column -> column.setHidable(false));
        }

        @Override
        public void setMinimizedColumnExpandRatio() {
            getColumns().forEach(column -> column.setExpandRatio(0));

            getColumn(DS_NAME_ID).setExpandRatio(1);
        }
    }
}
