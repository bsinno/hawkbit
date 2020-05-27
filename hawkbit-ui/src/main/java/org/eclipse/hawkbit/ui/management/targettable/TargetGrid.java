/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.builder.GridComponentBuilder;
import org.eclipse.hawkbit.ui.common.builder.IconBuilder;
import org.eclipse.hawkbit.ui.common.data.filters.TargetManagementFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.providers.TargetManagementStateDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.event.PinningChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.PinningChangedEventPayload.PinningChangedEventType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.FilterSupport;
import org.eclipse.hawkbit.ui.common.grid.support.PinSupport;
import org.eclipse.hawkbit.ui.common.grid.support.PinSupport.PinBehaviourType;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.AssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.DistributionSetsToTargetAssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.TargetTagsToTargetAssignmentSupport;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.management.dstable.DistributionGridLayoutUiState;
import org.eclipse.hawkbit.ui.management.miscs.DeploymentAssignmentWindowController;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterLayoutUiState;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;

/**
 * Concrete implementation of Target grid which is displayed on the Deployment
 * View.
 */
public class TargetGrid extends AbstractGrid<ProxyTarget, TargetManagementFilterParams> {
    private static final long serialVersionUID = 1L;

    private static final String TARGET_STATUS_ID = "targetStatus";
    private static final String TARGET_NAME_ID = "targetName";
    private static final String TARGET_POLLING_STATUS_ID = "targetPolling";
    private static final String TARGET_CREATED_BY_ID = "targetCreatedBy";
    private static final String TARGET_CREATED_DATE_ID = "targetCreatedDate";
    private static final String TARGET_MODIFIED_BY_ID = "targetModifiedBy";
    private static final String TARGET_MODIFIED_DATE_ID = "targetModifiedDate";
    private static final String TARGET_DESC_ID = "targetDescription";
    private static final String TARGET_PIN_BUTTON_ID = "targetPinButton";
    private static final String TARGET_DELETE_BUTTON_ID = "targetDeleteButton";

    private final TargetGridLayoutUiState targetGridLayoutUiState;
    private final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState;
    private final DistributionGridLayoutUiState distributionGridLayoutUiState;
    private final transient TargetManagement targetManagement;

    private final Map<TargetUpdateStatus, ProxyFontIcon> targetStatusIconMap;

    private final transient TargetToProxyTargetMapper targetToProxyTargetMapper;

    private final transient PinSupport<ProxyTarget, Long> pinSupport;
    private final transient DeleteSupport<ProxyTarget> targetDeleteSupport;

    public TargetGrid(final UIEventBus eventBus, final VaadinMessageSource i18n, final UINotification notification,
            final TargetManagement targetManagement, final SpPermissionChecker permChecker,
            final DeploymentManagement deploymentManagement, final TenantConfigurationManagement configManagement,
            final SystemSecurityContext systemSecurityContext, final UiProperties uiProperties,
            final TargetGridLayoutUiState targetGridLayoutUiState,
            final DistributionGridLayoutUiState distributionGridLayoutUiState,
            final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState) {
        super(i18n, eventBus, permChecker);

        this.targetManagement = targetManagement;
        this.targetGridLayoutUiState = targetGridLayoutUiState;
        this.targetTagFilterLayoutUiState = targetTagFilterLayoutUiState;
        this.distributionGridLayoutUiState = distributionGridLayoutUiState;
        this.targetToProxyTargetMapper = new TargetToProxyTargetMapper(i18n);

        setSelectionSupport(new SelectionSupport<ProxyTarget>(this, eventBus, EventLayout.TARGET_LIST,
                EventView.DEPLOYMENT, this::mapIdToProxyEntity, this::getSelectedEntityIdFromUiState,
                this::setSelectedEntityIdToUiState));
        if (targetGridLayoutUiState.isMaximized()) {
            getSelectionSupport().disableSelection();
        } else {
            getSelectionSupport().enableMultiSelection();
        }

        this.targetDeleteSupport = new DeleteSupport<>(this, i18n, notification,
                i18n.getMessage("target.details.header"), ProxyTarget::getName, this::deleteTargets,
                UIComponentIdProvider.TARGET_DELETE_CONFIRMATION_DIALOG);

        this.pinSupport = new PinSupport<>(this::refreshItem, this::publishPinningChangedEvent,
                this::updatePinnedUiState, this::getPinFilter, this::updatePinFilter, this::getAssignedToDsTargetIds,
                this::getInstalledToDsTargetIds);

        final Map<String, AssignmentSupport<?, ProxyTarget>> sourceTargetAssignmentStrategies = new HashMap<>();

        final DeploymentAssignmentWindowController assignmentController = new DeploymentAssignmentWindowController(i18n,
                uiProperties, eventBus, notification, deploymentManagement);
        final DistributionSetsToTargetAssignmentSupport distributionsToTargetAssignment = new DistributionSetsToTargetAssignmentSupport(
                notification, i18n, systemSecurityContext, configManagement, permChecker, assignmentController);
        final TargetTagsToTargetAssignmentSupport targetTagsToTargetAssignment = new TargetTagsToTargetAssignmentSupport(
                notification, i18n, targetManagement, eventBus);

        sourceTargetAssignmentStrategies.put(UIComponentIdProvider.DIST_TABLE_ID, distributionsToTargetAssignment);
        sourceTargetAssignmentStrategies.put(UIComponentIdProvider.TARGET_TAG_TABLE_ID, targetTagsToTargetAssignment);

        setDragAndDropSupportSupport(
                new DragAndDropSupport<>(this, i18n, notification, sourceTargetAssignmentStrategies, eventBus));
        if (!targetGridLayoutUiState.isMaximized()) {
            getDragAndDropSupportSupport().addDragAndDrop();
        }

        setFilterSupport(
                new FilterSupport<>(new TargetManagementStateDataProvider(targetManagement, targetToProxyTargetMapper),
                        getSelectionSupport()::deselectAll));
        initFilterMappings();
        getFilterSupport().setFilter(new TargetManagementFilterParams());

        initDsPinningStyleGenerator();
        targetStatusIconMap = IconBuilder.generateTargetStatusIcons(i18n);
        init();
    }

    @Override
    public void init() {
        super.init();

        addStyleName("grid-row-border");
    }

    public Optional<ProxyTarget> mapIdToProxyEntity(final long entityId) {
        return targetManagement.get(entityId).map(targetToProxyTargetMapper::map);
    }

    private Long getSelectedEntityIdFromUiState() {
        return targetGridLayoutUiState.getSelectedEntityId();
    }

    private void setSelectedEntityIdToUiState(final Long entityId) {
        targetGridLayoutUiState.setSelectedEntityId(entityId);
    }

    private boolean deleteTargets(final Collection<ProxyTarget> targetsToBeDeleted) {
        final Collection<Long> targetToBeDeletedIds = targetsToBeDeleted.stream().map(ProxyIdentifiableEntity::getId)
                .collect(Collectors.toList());
        targetManagement.delete(targetToBeDeletedIds);

        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                EntityModifiedEventType.ENTITY_REMOVED, ProxyTarget.class, targetToBeDeletedIds));

        return true;
    }

    private void publishPinningChangedEvent(final PinBehaviourType pinType, final ProxyTarget pinnedItem) {
        eventBus.publish(EventTopics.PINNING_CHANGED, this,
                new PinningChangedEventPayload<String>(
                        pinType == PinBehaviourType.PINNED ? PinningChangedEventType.ENTITY_PINNED
                                : PinningChangedEventType.ENTITY_UNPINNED,
                        ProxyTarget.class, pinnedItem.getControllerId()));
    }

    private void updatePinnedUiState(final ProxyTarget pinnedItem) {
        targetGridLayoutUiState.setPinnedTargetId(pinnedItem != null ? pinnedItem.getId() : null);
        targetGridLayoutUiState.setPinnedControllerId(pinnedItem != null ? pinnedItem.getControllerId() : null);
    }

    private Optional<Long> getPinFilter() {
        return getFilter().map(TargetManagementFilterParams::getPinnedDistId);
    }

    private void updatePinFilter(final Long pinnedDsId) {
        getFilterSupport().updateFilter(TargetManagementFilterParams::setPinnedDistId, pinnedDsId);
    }

    private Collection<Long> getAssignedToDsTargetIds(final Long pinnedDsId) {
        return getTargetIdsByFunction(query -> targetManagement.findByAssignedDistributionSet(query, pinnedDsId));
    }

    private Collection<Long> getTargetIdsByFunction(final Function<Pageable, Page<Target>> findTargetsFunction) {
        // TODO: check if it is possible to use lazy loading here, by only
        // loading targets that are relevant for data provider with filter,
        // offset and limit or alternatively only load the corresponding target
        // ids
        return HawkbitCommonUtil.getEntitiesByPageableProvider(findTargetsFunction::apply).stream().map(Target::getId)
                .collect(Collectors.toList());
    }

    private Collection<Long> getInstalledToDsTargetIds(final Long pinnedDsId) {
        return getTargetIdsByFunction(query -> targetManagement.findByInstalledDistributionSet(query, pinnedDsId));
    }

    private void initFilterMappings() {
        getFilterSupport().addMapping(FilterType.SEARCH, TargetManagementFilterParams::setSearchText,
                targetGridLayoutUiState.getSearchFilter());
        getFilterSupport().addMapping(FilterType.STATUS, TargetManagementFilterParams::setTargetUpdateStatusList,
                targetTagFilterLayoutUiState.getClickedTargetUpdateStatusFilters());
        getFilterSupport().addMapping(FilterType.OVERDUE, TargetManagementFilterParams::setOverdueState,
                targetTagFilterLayoutUiState.isOverdueFilterClicked());
        getFilterSupport().addMapping(FilterType.NO_TAG, TargetManagementFilterParams::setNoTagClicked,
                targetTagFilterLayoutUiState.isNoTagClicked());
        getFilterSupport().addMapping(FilterType.TAG, TargetManagementFilterParams::setTargetTags,
                targetTagFilterLayoutUiState.getClickedTagIdsWithName().values());
        getFilterSupport().addMapping(FilterType.QUERY, TargetManagementFilterParams::setTargetFilterQueryId,
                targetTagFilterLayoutUiState.getClickedTargetFilterQueryId());
        getFilterSupport().addMapping(FilterType.DISTRIBUTION, TargetManagementFilterParams::setDistributionId,
                targetGridLayoutUiState.getFilterDsIdNameVersion() != null
                        ? targetGridLayoutUiState.getFilterDsIdNameVersion().getId()
                        : null);
    }

    private void initDsPinningStyleGenerator() {
        setStyleGenerator(target -> pinSupport.getAssignedOrInstalledRowStyle(target.getId()));
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.TARGET_TABLE_ID;
    }

    public void onCustomTabSelected() {
        getFilter().ifPresent(filter -> {
            filter.setDistributionId(null);
            filter.setNoTagClicked(false);
            filter.setOverdueState(false);
            filter.setSearchText(null);
            filter.setTargetTags(Collections.emptyList());
            filter.setTargetUpdateStatusList(Collections.emptyList());

            getFilterSupport().refreshFilter();
        });
    }

    public void onSimpleTabSelected() {
        getFilterSupport().updateFilter(TargetManagementFilterParams::setTargetFilterQueryId, null);
    }

    @Override
    public void addColumns() {
        addNameColumn().setMinimumWidth(130d).setMaximumWidth(150d).setExpandRatio(1);

        addTargetPollingStatusColumn().setWidth(30d);
        addTargetStatusColumn().setWidth(30d);
        getDefaultHeaderRow().join(TARGET_POLLING_STATUS_ID, TARGET_STATUS_ID)
                .setText(i18n.getMessage("header.status"));

        addPinColumn().setWidth(25d);
        addDeleteColumn().setWidth(50d);
        getDefaultHeaderRow().join(TARGET_PIN_BUTTON_ID, TARGET_DELETE_BUTTON_ID)
                .setText(i18n.getMessage("header.action"));
    }

    private Column<ProxyTarget, String> addNameColumn() {
        return addColumn(ProxyTarget::getName).setId(TARGET_NAME_ID).setCaption(i18n.getMessage("header.name"));
    }

    private Column<ProxyTarget, Label> addTargetPollingStatusColumn() {
        return addComponentColumn(this::buildTargetPollingStatusIcon).setId(TARGET_POLLING_STATUS_ID)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);
    }

    private Label buildTargetPollingStatusIcon(final ProxyTarget target) {
        final String pollStatusToolTip = target.getPollStatusToolTip();

        final ProxyFontIcon pollStatusFontIcon = StringUtils.hasText(pollStatusToolTip)
                ? new ProxyFontIcon(VaadinIcons.EXCLAMATION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                        pollStatusToolTip)
                : new ProxyFontIcon(VaadinIcons.CLOCK, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                        i18n.getMessage(UIMessageIdProvider.TOOLTIP_IN_TIME));

        final String pollStatusId = new StringBuilder(UIComponentIdProvider.TARGET_TABLE_POLLING_STATUS_LABEL_ID)
                .append(".").append(target.getId()).toString();

        return SPUIComponentProvider.getLabelIcon(pollStatusFontIcon, pollStatusId);
    }

    private Column<ProxyTarget, Label> addTargetStatusColumn() {
        return addComponentColumn(this::buildTargetStatusIcon).setId(TARGET_STATUS_ID)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);
    }

    private Label buildTargetStatusIcon(final ProxyTarget target) {
        return IconBuilder.buildStatusIconLabel(i18n, targetStatusIconMap, ProxyTarget::getUpdateStatus,
                UIComponentIdProvider.TARGET_TABLE_STATUS_LABEL_ID, target);
    }

    private Column<ProxyTarget, Button> addPinColumn() {
        return addComponentColumn(target -> GridComponentBuilder.buildActionButton(i18n,
                clickEvent -> pinSupport.changeItemPinning(target), VaadinIcons.PIN,
                UIMessageIdProvider.TOOLTIP_TARGET_PIN, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.TARGET_PIN_ICON + "." + target.getId(), true)).setId(TARGET_PIN_BUTTON_ID)
                        .setStyleGenerator(pinSupport::getPinningStyle);
    }

    private Column<ProxyTarget, Button> addDeleteColumn() {
        return addComponentColumn(target -> GridComponentBuilder.buildActionButton(i18n,
                clickEvent -> targetDeleteSupport.openConfirmationWindowDeleteAction(target), VaadinIcons.TRASH,
                UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.TARGET_DELET_ICON + "." + target.getId(),
                permissionChecker.hasDeleteTargetPermission())).setId(TARGET_DELETE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.delete"));
    }

    @Override
    protected void addMaxColumns() {
        addNameColumn().setMinimumWidth(130d).setExpandRatio(7);

        addCreatedByColumn().setMinimumWidth(100d).setExpandRatio(1);
        addCreatedDateColumn().setMinimumWidth(100d).setExpandRatio(1);
        addModifiedByColumn().setMinimumWidth(100d).setExpandRatio(1);
        addModifiedDateColumn().setMinimumWidth(100d).setExpandRatio(1);

        addDescriptionColumn().setMinimumWidth(100d).setExpandRatio(5);

        addDeleteColumn().setWidth(75d);

        getColumns().forEach(column -> column.setHidable(true));
    }

    private Column<ProxyTarget, String> addCreatedByColumn() {
        return addColumn(ProxyTarget::getCreatedBy).setId(TARGET_CREATED_BY_ID)
                .setCaption(i18n.getMessage("header.createdBy"));
    }

    private Column<ProxyTarget, String> addCreatedDateColumn() {
        return addColumn(ProxyTarget::getCreatedDate).setId(TARGET_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate"));
    }

    private Column<ProxyTarget, String> addModifiedByColumn() {
        return addColumn(ProxyTarget::getLastModifiedBy).setId(TARGET_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy"));
    }

    private Column<ProxyTarget, String> addModifiedDateColumn() {
        return addColumn(ProxyTarget::getModifiedDate).setId(TARGET_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate"));
    }

    private Column<ProxyTarget, String> addDescriptionColumn() {
        return addColumn(ProxyTarget::getDescription).setId(TARGET_DESC_ID)
                .setCaption(i18n.getMessage("header.description"));
    }

    @Override
    public void restoreState() {
        final Long pinnedTargetId = targetGridLayoutUiState.getPinnedTargetId();
        if (pinnedTargetId != null) {
            final ProxyTarget pinnedTarget = new ProxyTarget();
            pinnedTarget.setId(pinnedTargetId);
            pinSupport.restorePinning(pinnedTarget);
        }

        final Long pinnedDsId = distributionGridLayoutUiState.getPinnedDsId();
        if (pinnedDsId != null) {
            pinSupport.repopulateAssignedAndInstalled(pinnedDsId);
            getFilter().ifPresent(filter -> filter.setPinnedDistId(pinnedDsId));
        }

        super.restoreState();
    }

    public PinSupport<ProxyTarget, Long> getPinSupport() {
        return pinSupport;
    }
}