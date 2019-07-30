/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.ds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.hawkbit.im.authentication.SpPermission;
import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.RemoteEntityEvent;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.DistributionSetTagAssignmentResult;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.ConfirmationDialog;
import org.eclipse.hawkbit.ui.common.data.filters.DsManagementFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetManagementStateDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.entity.TargetIdName;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.PinSupport;
import org.eclipse.hawkbit.ui.common.grid.support.ResizeSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.dd.criteria.ManagementViewClientCriterion;
import org.eclipse.hawkbit.ui.management.TargetAssignmentOperations;
import org.eclipse.hawkbit.ui.management.event.DistributionTableEvent;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.event.PinUnpinEvent;
import org.eclipse.hawkbit.ui.management.event.RefreshDistributionTableByFilterEvent;
import org.eclipse.hawkbit.ui.management.miscs.ActionTypeOptionGroupAssignmentLayout;
import org.eclipse.hawkbit.ui.management.miscs.MaintenanceWindowLayout;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.push.DistributionSetUpdatedEventContainer;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.eclipse.hawkbit.ui.view.filter.OnlyEventsFromDeploymentViewFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.grid.DropMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.UI;
import com.vaadin.ui.components.grid.GridDragSource;
import com.vaadin.ui.components.grid.GridDropTarget;

/**
 * Distribution set grid which is shown on the Deployment View.
 */
public class DistributionGrid extends AbstractGrid<ProxyDistributionSet, DsManagementFilterParams> {
    private static final long serialVersionUID = 1L;

    private static final String DS_NAME_ID = "dsName";
    private static final String DS_VERSION_ID = "dsVersion";
    private static final String DS_CREATED_BY_ID = "dsCreatedBy";
    private static final String DS_CREATED_DATE_ID = "dsCreatedDate";
    private static final String DS_MODIFIED_BY_ID = "dsModifiedBy";
    private static final String DS_MODIFIED_DATE_ID = "dsModifiedDate";
    private static final String DS_DESC_ID = "dsDescription";
    private static final String DS_PIN_BUTTON_ID = "dsPinnButton";
    private static final String DS_DELETE_BUTTON_ID = "dsDeleteButton";

    private final SpPermissionChecker permissionChecker;
    private final ManagementUIState managementUIState;
    private final transient TargetManagement targetManagement;
    private final transient DistributionSetManagement distributionSetManagement;
    private final transient DeploymentManagement deploymentManagement;
    private final transient UINotification notification;
    private final UiProperties uiProperties;

    // TODO: Remove after restructuring if possible
    private ConfirmationDialog confirmAssignDialog;

    private final ActionTypeOptionGroupAssignmentLayout actionTypeOptionGroupLayout;
    private final MaintenanceWindowLayout maintenanceWindowLayout;

    private final ConfigurableFilterDataProvider<ProxyDistributionSet, Void, DsManagementFilterParams> dsDataProvider;
    private final DistributionSetToProxyDistributionMapper distributionSetToProxyDistributionMapper;
    private final DistributionPinSupport pinSupport;
    private final DeleteSupport<ProxyDistributionSet> distributionDeleteSupport;

    DistributionGrid(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final ManagementUIState managementUIState,
            final ManagementViewClientCriterion managementViewClientCriterion, final TargetManagement targetManagement,
            final DistributionSetManagement distributionSetManagement, final DeploymentManagement deploymentManagement,
            final TargetTagManagement targetTagManagement, final UiProperties uiProperties) {
        super(i18n, eventBus, permissionChecker);

        this.permissionChecker = permissionChecker;
        this.managementUIState = managementUIState;
        this.targetManagement = targetManagement;
        this.distributionSetManagement = distributionSetManagement;
        this.deploymentManagement = deploymentManagement;
        this.actionTypeOptionGroupLayout = new ActionTypeOptionGroupAssignmentLayout(i18n);
        this.maintenanceWindowLayout = new MaintenanceWindowLayout(i18n);
        this.uiProperties = uiProperties;
        this.notification = notification;

        this.distributionSetToProxyDistributionMapper = new DistributionSetToProxyDistributionMapper();
        this.dsDataProvider = new DistributionSetManagementStateDataProvider(distributionSetManagement,
                distributionSetToProxyDistributionMapper).withConfigurableFilter();

        setResizeSupport(new DistributionResizeSupport());
        setSelectionSupport(new SelectionSupport<ProxyDistributionSet>(this));
        if (managementUIState.isDsTableMaximized()) {
            getSelectionSupport().disableSelection();
        } else {
            getSelectionSupport().enableMultiSelection();
        }
        this.pinSupport = new DistributionPinSupport();
        this.distributionDeleteSupport = new DeleteSupport<>(this, i18n, i18n.getMessage("distribution.details.header"),
                permissionChecker, notification, this::dsIdsDeletionCallback);

        init();

        addDragAndDrop();
    }

    private void dsIdsDeletionCallback(final Collection<Long> dsToBeDeletedIds) {
        distributionSetManagement.delete(dsToBeDeletedIds);

        // TODO: should we really pass the dsToBeDeletedIds? We call
        // dataprovider refreshAll anyway after receiving the event
        eventBus.publish(this, new DistributionTableEvent(BaseEntityEventType.REMOVE_ENTITY, dsToBeDeletedIds));

        pinSupport.getPinnedItemIdFromUiState()
                .ifPresent(pinnedDsId -> pinSupport.unPinItemAfterDeletion(pinnedDsId, dsToBeDeletedIds));
        managementUIState.getSelectedDsIdName().clear();
    }

    @Override
    protected String getGridId() {
        return UIComponentIdProvider.DIST_TABLE_ID;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyDistributionSet, Void, DsManagementFilterParams> getFilterDataProvider() {
        return dsDataProvider;
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

        // TODO: do we really need it?
        publishDsSelectedEntityForRefresh(eventContainer.getEvents().stream());
    }

    private void deselectIncompleteDs(final Stream<DistributionSetUpdatedEvent> dsEntityUpdateEventStream) {
        if (dsEntityUpdateEventStream.filter(event -> !event.isComplete()).map(DistributionSetUpdatedEvent::getEntityId)
                .anyMatch(this::isLastSelectedDs)) {
            // TODO: consider renaming it to setLastSelectedDsIdName
            managementUIState.setLastSelectedEntityId(null);
        }
    }

    private void publishDsSelectedEntityForRefresh(
            final Stream<? extends RemoteEntityEvent<DistributionSet>> dsEntityEventStream) {
        dsEntityEventStream.filter(event -> isLastSelectedDs(event.getEntityId())).filter(Objects::nonNull).findAny()
                .ifPresent(event -> eventBus.publish(this,
                        new DistributionTableEvent(BaseEntityEventType.SELECTED_ENTITY, event.getEntity())));
    }

    private boolean isLastSelectedDs(final Long dsId) {
        return managementUIState.getLastSelectedDsIdName().map(lastSelectedDsId -> lastSelectedDsId.equals(dsId))
                .orElse(false);
    }

    /**
     * DistributionTableFilterEvent.
     *
     * @param filterEvent
     *            as instance of {@link RefreshDistributionTableByFilterEvent}
     */
    @EventBusListenerMethod(scope = EventScope.UI, filter = OnlyEventsFromDeploymentViewFilter.class)
    void onEvent(final RefreshDistributionTableByFilterEvent filterEvent) {
        UI.getCurrent().access(this::refreshFilter);
    }

    private void refreshFilter() {
        final DsManagementFilterParams filterParams = new DsManagementFilterParams(getSearchTextFromUiState(),
                isNoTagClickedFromUiState(), getDistributionTagsFromUiState(), getPinnedTargetFromUiState());

        getFilterDataProvider().setFilter(filterParams);
    }

    private String getSearchTextFromUiState() {
        return managementUIState.getDistributionTableFilters().getSearchText()
                .filter(searchText -> !StringUtils.isEmpty(searchText)).map(value -> String.format("%%%s%%", value))
                .orElse(null);
    }

    private Boolean isNoTagClickedFromUiState() {
        return managementUIState.getDistributionTableFilters().isNoTagSelected();
    }

    private List<String> getDistributionTagsFromUiState() {
        return managementUIState.getDistributionTableFilters().getDistSetTags();
    }

    private TargetIdName getPinnedTargetFromUiState() {
        return managementUIState.getDistributionTableFilters().getPinnedTarget().orElse(null);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final DistributionTableEvent event) {
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
        UI.getCurrent().access(() -> updateDistributionSet(event.getEntity()));
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
     * To update distribution set details in the grid.
     *
     * @param updatedDs
     *            as reference
     */
    public void updateDistributionSet(final DistributionSet updatedDs) {
        if (updatedDs != null) {
            getDataProvider().refreshItem(distributionSetToProxyDistributionMapper.map(updatedDs));
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final PinUnpinEvent pinUnpinEvent) {
        UI.getCurrent().access(() -> {
            if (pinUnpinEvent == PinUnpinEvent.PIN_TARGET) {
                /* if target is pinned, unpin distribution set if pinned */
                pinSupport.setPinnedItemIdInUiState(null);
                refreshFilter();
                // TODO: check if refreshFilter() shuld be called after
                pinSupport.styleRowOnPinning();
            } else if (pinUnpinEvent == PinUnpinEvent.UNPIN_TARGET) {
                refreshFilter();
                pinSupport.restoreRowStyle();
            }
        });
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ManagementUIEvent managementUIEvent) {
        UI.getCurrent().access(() -> {
            if (managementUIEvent == ManagementUIEvent.UNASSIGN_DISTRIBUTION_TAG
                    || managementUIEvent == ManagementUIEvent.ASSIGN_DISTRIBUTION_TAG) {
                refreshFilter();
            }
        });
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
        addComponentColumn(ds -> {
            final Button pinBtn = buildActionButton(event -> pinSupport.pinItemListener(ds, event.getButton()),
                    VaadinIcons.PIN, UIMessageIdProvider.TOOLTIP_DISTRIBUTION_SET_PIN,
                    SPUIStyleDefinitions.STATUS_ICON_NEUTRAL, UIComponentIdProvider.DIST_PIN_ICON + "." + ds.getId(),
                    true);

            return pinSupport.buildPinActionButton(pinBtn, ds);
        }).setId(DS_PIN_BUTTON_ID).setMinimumWidth(50d).setMaximumWidth(50d).setHidable(false).setHidden(false);

        addComponentColumn(
                ds -> buildActionButton(clickEvent -> distributionDeleteSupport.openConfirmationWindowDeleteAction(ds),
                        VaadinIcons.TRASH, UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                        UIComponentIdProvider.DIST_DELET_ICON + "." + ds.getId(),
                        distributionDeleteSupport.hasDeletePermission())).setId(DS_DELETE_BUTTON_ID)
                                .setMinimumWidth(50d).setMaximumWidth(50d).setHidable(false).setHidden(false);

        getDefaultHeaderRow().join(DS_PIN_BUTTON_ID, DS_DELETE_BUTTON_ID).setText(i18n.getMessage("header.action"));
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

    private Set<Long> getItemIdsToSelectFromUiState() {
        return managementUIState.getSelectedDsIdName().isEmpty() ? null : managementUIState.getSelectedDsIdName();
    }

    private void addDragAndDrop() {
        addDragSource();
        addDragTarget();
    }

    public void addDragSource() {
        final GridDragSource<ProxyDistributionSet> dragSource = new GridDragSource<>(this);

        dragSource.setDataTransferData("source_id", getGridId());
        dragSource.addGridDragStartListener(event -> dragSource.setDragData(event.getDraggedItems()));

        dragSource.addGridDragEndListener(event -> {
            if (event.isCanceled()) {
                notification.displayValidationError(i18n.getMessage(UIMessageIdProvider.MESSAGE_ACTION_NOT_ALLOWED));
            }
        });
    }

    public void addDragTarget() {
        final GridDropTarget<ProxyDistributionSet> dropTarget = new GridDropTarget<>(this, DropMode.ON_TOP);

        dropTarget.addGridDropListener(event -> {
            final String sourceId = event.getDataTransferData("source_id").orElse("");
            final ProxyDistributionSet dropDsItem = event.getDropTargetRow().orElse(null);

            if (!isDropValid(sourceId, dropDsItem)) {
                return;
            }

            event.getDragSourceExtension().ifPresent(source -> {
                if (source instanceof GridDragSource) {
                    if (sourceId.equals(UIComponentIdProvider.TARGET_TABLE_ID)) {
                        final List<ProxyTarget> targetsToAssign = (List<ProxyTarget>) source.getDragData();
                        assignTargetsToDistributionSet(targetsToAssign, dropDsItem);
                    } else if (sourceId.equals(UIComponentIdProvider.DISTRIBUTION_TAG_TABLE_ID)) {
                        final List<ProxyTag> dsTagsToAssign = (List<ProxyTag>) source.getDragData();
                        assignDsTagsToDs(dsTagsToAssign, dropDsItem);
                    } else if (sourceId.equals(UIComponentIdProvider.TARGET_TAG_TABLE_ID)) {
                        final List<ProxyTag> dsTagsToAssign = (List<ProxyTag>) source.getDragData();
                        assignTargetTagsToDs(dsTagsToAssign, dropDsItem);
                    } else {
                        notification.displayValidationError(
                                i18n.getMessage(UIMessageIdProvider.MESSAGE_ACTION_NOT_ALLOWED));
                    }
                }
            });
        });
    }

    private boolean isDropValid(final String sourceId, final ProxyDistributionSet dropTargetItem) {
        final List<String> allowedSourceIds = Arrays.asList(UIComponentIdProvider.TARGET_TABLE_ID,
                UIComponentIdProvider.DISTRIBUTION_TAG_TABLE_ID, UIComponentIdProvider.TARGET_TAG_TABLE_ID);

        if (!allowedSourceIds.contains(sourceId) || dropTargetItem == null) {
            notification.displayValidationError(i18n.getMessage(UIMessageIdProvider.MESSAGE_ACTION_NOT_ALLOWED));
            return false;
        }

        if (!permissionChecker.hasUpdateTargetPermission()) {
            notification.displayValidationError(
                    i18n.getMessage("message.permission.insufficient", Arrays.asList(SpPermission.UPDATE_TARGET)));
            return false;
        }

        return true;
    }

    private void assignTargetsToDistributionSet(final List<ProxyTarget> targetsToAssign,
            final ProxyDistributionSet dropDsItem) {
        if (targetsToAssign.isEmpty()) {
            notification.displayValidationError(i18n.getMessage("message.action.did.not.work"));
            return;
        }

        // TODO: check if needed
        // selectDraggedEntities(source, dsIds);
        // selectDroppedEntities(targetId);

        openConfirmationWindowForAssignments(dropDsItem, targetsToAssign);
    }

    private void openConfirmationWindowForAssignments(final ProxyDistributionSet dropDsItem,
            final List<ProxyTarget> targetsToAssign) {
        final String confirmationMessage = getConfirmationMessageForAssignments(dropDsItem, targetsToAssign);
        final String caption = i18n.getMessage(UIMessageIdProvider.CAPTION_ENTITY_ASSIGN_ACTION_CONFIRMBOX);
        final String okLabel = i18n.getMessage(UIMessageIdProvider.BUTTON_OK);
        final String cancelLabel = i18n.getMessage(UIMessageIdProvider.BUTTON_CANCEL);

        // TODO: use ProxyEntities directly by modifying the
        // saveAllAssignments() method
        final DistributionSet dsToAssign = distributionSetManagement.get(dropDsItem.getId())
                .orElseThrow(() -> new EntityNotFoundException(DistributionSet.class, dropDsItem.getName()));
        final List<Target> targets = targetManagement
                .get(targetsToAssign.stream().map(ProxyTarget::getId).collect(Collectors.toList()));

        // TODO: don't use confirmDialog class member variable here
        confirmAssignDialog = new ConfirmationDialog(caption, confirmationMessage, okLabel, cancelLabel, ok -> {
            if (ok && TargetAssignmentOperations.isMaintenanceWindowValid(maintenanceWindowLayout, notification)) {
                TargetAssignmentOperations.saveAllAssignments(targets, Collections.singletonList(dsToAssign),
                        managementUIState, actionTypeOptionGroupLayout, maintenanceWindowLayout, deploymentManagement,
                        notification, eventBus, i18n, this);
            }
        }, TargetAssignmentOperations.createAssignmentTab(actionTypeOptionGroupLayout, maintenanceWindowLayout,
                saveButtonToggle(), i18n, uiProperties),
                UIComponentIdProvider.DIST_SET_TO_TARGET_ASSIGNMENT_CONFIRM_ID);

        UI.getCurrent().addWindow(confirmAssignDialog.getWindow());
        confirmAssignDialog.getWindow().bringToFront();
    }

    private String getConfirmationMessageForAssignments(final ProxyDistributionSet dropDsItem,
            final List<ProxyTarget> targetsToAssign) {
        final String entityType = i18n.getMessage("distribution.details.header");
        final String dsName = dropDsItem.getName();
        final int targetsCount = targetsToAssign.size();
        if (targetsCount > 1) {
            return i18n.getMessage(UIMessageIdProvider.MESSAGE_CONFIRM_ASSIGN_MULTIPLE_ENTITIES, targetsCount,
                    entityType, dsName);
        }
        return i18n.getMessage(UIMessageIdProvider.MESSAGE_CONFIRM_ASSIGN_ENTITY, targetsToAssign.get(0).getName(),
                entityType, dsName);
    }

    private Consumer<Boolean> saveButtonToggle() {
        return isEnabled -> confirmAssignDialog.getOkButton().setEnabled(isEnabled);
    }

    // TODO: Implement multi-tag assignment
    // (DistributionSetManagement(toggleTagAssignment), createAssignmentMessage,
    // etc.)
    private void assignDsTagsToDs(final List<ProxyTag> dsTagsToAssign, final ProxyDistributionSet dropDsItem) {
        if (dsTagsToAssign.isEmpty()) {
            notification.displayValidationError(i18n.getMessage("message.action.did.not.work"));
            return;
        }

        if (isNoTagAssigned(dsTagsToAssign)) {
            notification.displayValidationError(
                    i18n.getMessage("message.tag.cannot.be.assigned", i18n.getMessage("label.no.tag.assigned")));
            return;
        }

        // TODO: fix (we are taking first tag because multi-tag assignment is
        // not supported)
        final String tagName = dsTagsToAssign.get(0).getName();
        final DistributionSetTagAssignmentResult tagsAssignmentResult = distributionSetManagement
                .toggleTagAssignment(Collections.singletonList(dropDsItem.getId()), tagName);

        notification.displaySuccess(HawkbitCommonUtil.createAssignmentMessage(tagName, tagsAssignmentResult, i18n));

        // TODO: why is it different to TargetGrid
        if (tagsAssignmentResult.getAssigned() > 0
                && managementUIState.getDistributionTableFilters().isNoTagSelected()) {
            refreshFilter();
        }
    }

    private boolean isNoTagAssigned(final List<ProxyTag> dsTagsToAssign) {
        return dsTagsToAssign.stream()
                .anyMatch(dsTag -> dsTag.getName().equals(i18n.getMessage(UIMessageIdProvider.CAPTION_TARGET_TAG)));
    }

    private void assignTargetTagsToDs(final List<ProxyTag> targetTagsToAssign, final ProxyDistributionSet dropDsItem) {
        if (targetTagsToAssign.isEmpty()) {
            notification.displayValidationError(i18n.getMessage("message.action.did.not.work"));
            return;
        }

        if (isNoTagAssigned(targetTagsToAssign)) {
            notification.displayValidationError(
                    i18n.getMessage("message.tag.cannot.be.assigned", i18n.getMessage("label.no.tag.assigned")));
            return;
        }

        // TODO: fix (we are taking first tag because multi-tag assignment is
        // not supported)
        final String tagName = targetTagsToAssign.get(0).getName();
        final Long tagId = targetTagsToAssign.get(0).getId();

        final List<Target> targetsToAssign = getTargetsAssignedToTag(tagId);

        if (targetsToAssign.isEmpty()) {
            notification.displayValidationError(i18n.getMessage("message.no.targets.assiged.fortag", tagName));
            return;
        }

        // TODO: try not to map here
        final List<ProxyTarget> proxyTargetsToAssign = targetsToAssign.stream().map(target -> {
            final ProxyTarget proxyTarget = new ProxyTarget();

            proxyTarget.setId(target.getId());
            proxyTarget.setName(target.getName());

            return proxyTarget;
        }).collect(Collectors.toList());

        assignTargetsToDistributionSet(proxyTargetsToAssign, dropDsItem);
    }

    private List<Target> getTargetsAssignedToTag(final Long tagId) {
        final List<Target> targetsAssignedToTag = new ArrayList<>();
        Pageable query = PageRequest.of(0, 50);
        Page<Target> targetsAssignedToTagPageable;

        do {
            targetsAssignedToTagPageable = targetManagement.findByTag(query, tagId);
            if (targetsAssignedToTagPageable.hasContent()) {
                targetsAssignedToTag.addAll(targetsAssignedToTagPageable.getContent());
            }
        } while ((query = targetsAssignedToTagPageable.nextPageable()) != Pageable.unpaged());

        return targetsAssignedToTag;
    }

    /**
     * Adds support to resize the target grid.
     */
    class DistributionResizeSupport implements ResizeSupport {

        private final String[] maxColumnOrder = new String[] { DS_NAME_ID, DS_CREATED_BY_ID, DS_CREATED_DATE_ID,
                DS_MODIFIED_BY_ID, DS_MODIFIED_DATE_ID, DS_DESC_ID, DS_VERSION_ID, DS_DELETE_BUTTON_ID };

        private final String[] minColumnOrder = new String[] { DS_NAME_ID, DS_VERSION_ID, DS_PIN_BUTTON_ID,
                DS_DELETE_BUTTON_ID };

        @Override
        public void setMaximizedColumnOrder() {
            clearSortOrder();
            setColumnOrder(maxColumnOrder);
        }

        @Override
        public void setMaximizedHiddenColumns() {
            getColumn(DS_PIN_BUTTON_ID).setHidden(true);

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
            getColumn(DS_PIN_BUTTON_ID).setHidden(false);

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

    /**
     * Adds support to pin the distribution sets in grid.
     */
    class DistributionPinSupport extends PinSupport<ProxyDistributionSet, Long> {

        @Override
        public void styleRowOnPinning() {
            // TODO: consider using ProxyTarget instead of parsing the database
            managementUIState.getDistributionTableFilters().getPinnedTarget().map(TargetIdName::getControllerId)
                    .ifPresent(controllerId -> {
                        final Long installedDistId = deploymentManagement.getInstalledDistributionSet(controllerId)
                                .map(DistributionSet::getId).orElse(null);
                        final Long assignedDistId = deploymentManagement.getAssignedDistributionSet(controllerId)
                                .map(DistributionSet::getId).orElse(null);
                        setStyleGenerator(item -> getRowStyleForPinning(assignedDistId, installedDistId, item.getId()));
                    });
        }

        @Override
        public void restoreRowStyle() {
            setStyleGenerator(item -> null);
        }

        @Override
        public Optional<Long> getPinnedItemIdFromUiState() {
            return managementUIState.getTargetTableFilters().getPinnedDistId();
        }

        @Override
        public void setPinnedItemIdInUiState(final Long pinnedItemId) {
            managementUIState.getTargetTableFilters().setPinnedDistId(pinnedItemId);
        }

        @Override
        protected Long getPinnedItemIdFromItem(final ProxyDistributionSet item) {
            return item.getId();
        }

        @Override
        protected void publishPinItem() {
            // TODO: check if the sender is correct or should we use grid
            // component here
            eventBus.publish(this, PinUnpinEvent.PIN_DISTRIBUTION);
        }

        @Override
        protected void publishUnPinItem() {
            // TODO: check if the sender is correct or should we use grid
            // component here
            eventBus.publish(this, PinUnpinEvent.UNPIN_DISTRIBUTION);
        }
    }
}
