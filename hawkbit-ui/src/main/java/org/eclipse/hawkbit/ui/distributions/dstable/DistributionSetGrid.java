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
import java.util.stream.Stream;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetUpdatedEvent;
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
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.distributions.DistributionsView;
import org.eclipse.hawkbit.ui.management.event.DistributionTableEvent;
import org.eclipse.hawkbit.ui.management.event.SaveActionWindowEvent;
import org.eclipse.hawkbit.ui.push.DistributionSetUpdatedEventContainer;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.UI;

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
    private final transient DistributionSetManagement distributionSetManagement;

    private final ConfigurableFilterDataProvider<ProxyDistributionSet, Void, DsDistributionsFilterParams> dsDataProvider;
    private final DsDistributionsFilterParams dsFilter;

    private final transient DistributionSetToProxyDistributionMapper distributionSetToProxyDistributionMapper;
    private final transient DeleteSupport<ProxyDistributionSet> distributionDeleteSupport;
    private final transient DragAndDropSupport<ProxyDistributionSet> dragAndDropSupport;

    public DistributionSetGrid(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final TargetManagement targetManagement, final DistributionSetManagement distributionSetManagement,
            final DistributionSetGridLayoutUiState distributionSetGridLayoutUiState) {
        super(i18n, eventBus, permissionChecker);

        this.distributionSetGridLayoutUiState = distributionSetGridLayoutUiState;
        this.distributionSetManagement = distributionSetManagement;

        this.distributionSetToProxyDistributionMapper = new DistributionSetToProxyDistributionMapper();
        this.dsDataProvider = new DistributionSetDistributionsStateDataProvider(distributionSetManagement,
                distributionSetToProxyDistributionMapper).withConfigurableFilter();
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
                notification, i18n, targetManagement, distributionSetManagement, eventBus, permissionChecker);

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
        distributionSetManagement.delete(dsToBeDeletedIds);

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
        dsFilter.setClickedDistSetType(typeFilter);
        getFilterDataProvider().setFilter(dsFilter);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final SaveActionWindowEvent event) {
        if (event == SaveActionWindowEvent.SAVED_ASSIGNMENTS) {
            UI.getCurrent().access(this::refreshContainer);
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final DistributionTableEvent event) {
        if (BaseEntityEventType.ADD_ENTITY == event.getEventType()
                || BaseEntityEventType.REMOVE_ENTITY == event.getEventType()) {
            UI.getCurrent().access(this::refreshContainer);
        } else if (BaseEntityEventType.UPDATED_ENTITY == event.getEventType()) {
            UI.getCurrent().access(() -> updateDistributionSet(event.getEntity()));
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onDistributionSetUpdateEvents(final DistributionSetUpdatedEventContainer eventContainer) {
        deselectIncompleteDs(eventContainer.getEvents().stream());

        if (!eventContainer.getEvents().isEmpty()) {
            // TODO: Consider updating only corresponding distribution sets with
            // dataProvider.refreshItem() based on distribution set ids instead
            // of full refresh (evaluate getDataCommunicator().getKeyMapper())
            refreshContainer();
        }

        // TODO: Reselect previously selected entity after refresh?
    }

    private void deselectIncompleteDs(final Stream<DistributionSetUpdatedEvent> dsEntityUpdateEventStream) {
        if (dsEntityUpdateEventStream.filter(event -> !event.isComplete()).map(DistributionSetUpdatedEvent::getEntityId)
                .anyMatch(dsId -> dsId.equals(distributionSetGridLayoutUiState.getSelectedDsId()))) {
            // TODO: should we deselect row here?
            distributionSetGridLayoutUiState.setSelectedDsId(null);
        }
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

    /**
     * To update distribution set details in the grid.
     *
     * @param updatedDs
     *            as reference
     */
    public void updateDistributionSet(final ProxyDistributionSet updatedDs) {
        if (updatedDs != null) {
            getDataProvider().refreshItem(updatedDs);
        }
    }

    @Override
    public void addColumns() {
        // TODO: check width
        addColumn(ProxyDistributionSet::getName).setId(DS_NAME_ID).setCaption(i18n.getMessage("header.name"))
                .setMinimumWidth(100d).setMaximumWidth(150d).setHidable(false).setHidden(false);

        addColumn(ProxyDistributionSet::getVersion).setId(DS_VERSION_ID).setCaption(i18n.getMessage("header.version"))
                .setMinimumWidth(50d).setMaximumWidth(100d).setHidable(false).setHidden(false);

        addActionColumns();

        addColumn(ProxyDistributionSet::getCreatedBy).setId(DS_CREATED_BY_ID)
                .setCaption(i18n.getMessage("header.createdBy")).setHidable(true).setHidden(true);

        addColumn(ProxyDistributionSet::getCreatedDate).setId(DS_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate")).setHidable(true).setHidden(true);

        addColumn(ProxyDistributionSet::getLastModifiedBy).setId(DS_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy")).setHidable(true).setHidden(true);

        addColumn(ProxyDistributionSet::getModifiedDate).setId(DS_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setHidable(true).setHidden(true);

        addColumn(ProxyDistributionSet::getDescription).setId(DS_DESC_ID)
                .setCaption(i18n.getMessage("header.description")).setHidable(true).setHidden(true);
    }

    private void addActionColumns() {
        addComponentColumn(ds -> buildActionButton(
                clickEvent -> distributionDeleteSupport.openConfirmationWindowDeleteAction(ds, ds.getNameVersion()),
                VaadinIcons.TRASH, UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.DIST_DELET_ICON + "." + ds.getId(),
                distributionDeleteSupport.hasDeletePermission())).setId(DS_DELETE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.delete")).setMinimumWidth(50d).setMaximumWidth(50d)
                        .setHidable(false).setHidden(false);
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
            getColumn(DS_CREATED_BY_ID).setHidden(true);
            getColumn(DS_CREATED_DATE_ID).setHidden(true);
            getColumn(DS_MODIFIED_BY_ID).setHidden(true);
            getColumn(DS_MODIFIED_DATE_ID).setHidden(true);
            getColumn(DS_DESC_ID).setHidden(true);
        }

        @Override
        public void setMinimizedColumnExpandRatio() {
        }
    }
}
