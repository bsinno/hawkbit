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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.filters.TargetManagementFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.providers.TargetManagementStateDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.TargetModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.PinSupport;
import org.eclipse.hawkbit.ui.common.grid.support.ResizeSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.AssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.DistributionSetsToTargetAssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.TargetTagsToTargetAssignmentSupport;
import org.eclipse.hawkbit.ui.management.DeploymentView;
import org.eclipse.hawkbit.ui.management.dstable.DistributionGridLayoutUiState;
import org.eclipse.hawkbit.ui.management.event.PinUnpinEvent;
import org.eclipse.hawkbit.ui.management.miscs.DeploymentAssignmentWindowController;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterLayoutUiState;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
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
    private final transient TargetManagement targetManagement;

    private final Map<TargetUpdateStatus, ProxyFontIcon> targetStatusIconMap = new EnumMap<>(TargetUpdateStatus.class);

    private final ConfigurableFilterDataProvider<ProxyTarget, Void, TargetManagementFilterParams> targetDataProvider;
    private final transient TargetToProxyTargetMapper targetToProxyTargetMapper;
    private final TargetManagementFilterParams targetFilter;

    private final transient PinSupport<ProxyTarget> pinSupport;
    private final transient DeleteSupport<ProxyTarget> targetDeleteSupport;
    private final transient DragAndDropSupport<ProxyTarget> dragAndDropSupport;

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

        this.targetToProxyTargetMapper = new TargetToProxyTargetMapper(i18n);
        this.targetDataProvider = new TargetManagementStateDataProvider(targetManagement, targetToProxyTargetMapper)
                .withConfigurableFilter();
        this.targetFilter = new TargetManagementFilterParams();

        setResizeSupport(new TargetResizeSupport());

        setSelectionSupport(new SelectionSupport<ProxyTarget>(this, eventBus, DeploymentView.VIEW_NAME,
                this::updateLastSelectedTargetUiState));
        if (targetGridLayoutUiState.isMaximized()) {
            getSelectionSupport().disableSelection();
        } else {
            getSelectionSupport().enableMultiSelection();
        }

        this.pinSupport = new PinSupport<>(eventBus, PinUnpinEvent.PIN_TARGET, PinUnpinEvent.UNPIN_TARGET,
                () -> setStyleGenerator(item -> null), targetGridLayoutUiState::getPinnedTargetId,
                targetGridLayoutUiState::setPinnedTargetId);

        this.targetDeleteSupport = new DeleteSupport<>(this, i18n, i18n.getMessage("target.details.header"),
                permChecker, notification, this::deleteTargets);

        final Map<String, AssignmentSupport<?, ProxyTarget>> sourceTargetAssignmentStrategies = new HashMap<>();

        final DeploymentAssignmentWindowController assignmentController = new DeploymentAssignmentWindowController(i18n,
                uiProperties, eventBus, notification, deploymentManagement, targetGridLayoutUiState,
                distributionGridLayoutUiState);
        final DistributionSetsToTargetAssignmentSupport distributionsToTargetAssignment = new DistributionSetsToTargetAssignmentSupport(
                notification, i18n, systemSecurityContext, configManagement, permChecker, assignmentController);
        final TargetTagsToTargetAssignmentSupport targetTagsToTargetAssignment = new TargetTagsToTargetAssignmentSupport(
                notification, i18n, targetManagement, eventBus, targetTagFilterLayoutUiState);

        sourceTargetAssignmentStrategies.put(UIComponentIdProvider.DIST_TABLE_ID, distributionsToTargetAssignment);
        sourceTargetAssignmentStrategies.put(UIComponentIdProvider.TARGET_TAG_TABLE_ID, targetTagsToTargetAssignment);

        this.dragAndDropSupport = new DragAndDropSupport<>(this, i18n, notification, sourceTargetAssignmentStrategies);
        this.dragAndDropSupport.addDragAndDrop();

        initTargetStatusIconMap();
        init();
    }

    private void updateLastSelectedTargetUiState(final SelectionChangedEventType type,
            final ProxyTarget selectedTarget) {
        if (type == SelectionChangedEventType.ENTITY_DESELECTED) {
            targetGridLayoutUiState.setSelectedTargetId(null);
        } else {
            targetGridLayoutUiState.setSelectedTargetId(selectedTarget.getId());
        }
    }

    private void deleteTargets(final Collection<ProxyTarget> targetsToBeDeleted) {
        final Collection<Long> targetToBeDeletedIds = targetsToBeDeleted.stream().map(ProxyIdentifiableEntity::getId)
                .collect(Collectors.toList());
        targetManagement.delete(targetToBeDeletedIds);

        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new TargetModifiedEventPayload(EntityModifiedEventType.ENTITY_REMOVED, targetToBeDeletedIds));

        final Long pinnedTargetId = targetGridLayoutUiState.getPinnedTargetId();
        if (pinnedTargetId != null) {
            pinSupport.unPinItemAfterDeletion(pinnedTargetId, targetToBeDeletedIds);
        }
    }

    // TODO: check if icons are correct
    private void initTargetStatusIconMap() {
        targetStatusIconMap.put(TargetUpdateStatus.ERROR, new ProxyFontIcon(VaadinIcons.EXCLAMATION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_RED, getTargetStatusDescription(TargetUpdateStatus.ERROR)));
        targetStatusIconMap.put(TargetUpdateStatus.UNKNOWN, new ProxyFontIcon(VaadinIcons.QUESTION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_BLUE, getTargetStatusDescription(TargetUpdateStatus.UNKNOWN)));
        targetStatusIconMap.put(TargetUpdateStatus.IN_SYNC, new ProxyFontIcon(VaadinIcons.CHECK_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getTargetStatusDescription(TargetUpdateStatus.IN_SYNC)));
        targetStatusIconMap.put(TargetUpdateStatus.PENDING, new ProxyFontIcon(VaadinIcons.DOT_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_YELLOW, getTargetStatusDescription(TargetUpdateStatus.PENDING)));
        targetStatusIconMap.put(TargetUpdateStatus.REGISTERED,
                new ProxyFontIcon(VaadinIcons.DOT_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_LIGHT_BLUE,
                        getTargetStatusDescription(TargetUpdateStatus.REGISTERED)));
    }

    private String getTargetStatusDescription(final TargetUpdateStatus targetStatus) {
        return i18n
                .getMessage(UIMessageIdProvider.TOOLTIP_TARGET_STATUS_PREFIX + targetStatus.toString().toLowerCase());
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.TARGET_TABLE_ID;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyTarget, Void, TargetManagementFilterParams> getFilterDataProvider() {
        return targetDataProvider;
    }

    public void updateSearchFilter(final String searchFilter) {
        targetFilter.setSearchText(!StringUtils.isEmpty(searchFilter) ? String.format("%%%s%%", searchFilter) : null);
        getFilterDataProvider().setFilter(targetFilter);
    }

    public void updateTagFilter(final Collection<String> tagFilterNames) {
        targetFilter.setTargetTags(tagFilterNames.toArray(new String[tagFilterNames.size()]));
        getFilterDataProvider().setFilter(targetFilter);
    }

    public void updateNoTagFilter(final boolean isActive) {
        targetFilter.setNoTagClicked(isActive);
        getFilterDataProvider().setFilter(targetFilter);
    }

    public void updateStatusFilter(final List<TargetUpdateStatus> statusFilters) {
        targetFilter.setTargetUpdateStatusList(statusFilters);
        getFilterDataProvider().setFilter(targetFilter);
    }

    public void updateOverdueFilter(final boolean isOverdue) {
        targetFilter.setOverdueState(isOverdue);
        getFilterDataProvider().setFilter(targetFilter);
    }

    public void updateCustomFilter(final Long customFilterId) {
        targetFilter.setTargetFilterQueryId(customFilterId);
        getFilterDataProvider().setFilter(targetFilter);
    }

    public void updatePinnedDsFilter(final Long pinnedDsId) {
        targetFilter.setPinnedDistId(pinnedDsId);
        getFilterDataProvider().setFilter(targetFilter);
    }

    public void updateDsFilter(final Long dsId) {
        targetFilter.setDistributionId(dsId);
        getFilterDataProvider().setFilter(targetFilter);
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
        // TODO: check width
        addColumn(ProxyTarget::getName).setId(TARGET_NAME_ID).setCaption(i18n.getMessage("header.name"))
                .setMinimumWidth(100d).setExpandRatio(1);

        addComponentColumn(this::buildTargetPollingStatusIcon).setId(TARGET_POLLING_STATUS_ID).setMinimumWidth(50d)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);

        addComponentColumn(this::buildTargetStatusIcon).setId(TARGET_STATUS_ID).setMinimumWidth(50d)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);

        getDefaultHeaderRow().join(TARGET_POLLING_STATUS_ID, TARGET_STATUS_ID)
                .setText(i18n.getMessage("header.status"));

        addActionColumns();

        addColumn(ProxyTarget::getCreatedBy).setId(TARGET_CREATED_BY_ID).setCaption(i18n.getMessage("header.createdBy"))
                .setHidden(true);

        addColumn(ProxyTarget::getCreatedDate).setId(TARGET_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate")).setHidden(true);

        addColumn(ProxyTarget::getLastModifiedBy).setId(TARGET_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy")).setHidden(true);

        addColumn(ProxyTarget::getModifiedDate).setId(TARGET_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setHidden(true);

        addColumn(ProxyTarget::getDescription).setId(TARGET_DESC_ID).setCaption(i18n.getMessage("header.description"))
                .setHidden(true);
    }

    private Label buildTargetStatusIcon(final ProxyTarget target) {
        final ProxyFontIcon targetStatusFontIcon = Optional
                .ofNullable(targetStatusIconMap.get(target.getUpdateStatus()))
                .orElse(new ProxyFontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String targetStatusId = new StringBuilder(UIComponentIdProvider.TARGET_TABLE_STATUS_LABEL_ID).append(".")
                .append(target.getId()).toString();

        return buildLabelIcon(targetStatusFontIcon, targetStatusId);
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

        return buildLabelIcon(pollStatusFontIcon, pollStatusId);
    }

    private void addActionColumns() {
        addComponentColumn(target -> {
            final Button pinBtn = buildActionButton(event -> pinSupport.pinItemListener(target, event.getButton()),
                    VaadinIcons.PIN, UIMessageIdProvider.TOOLTIP_TARGET_PIN, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                    UIComponentIdProvider.TARGET_PIN_ICON + "." + target.getId(), true);

            return pinSupport.buildPinActionButton(pinBtn, target);
        }).setId(TARGET_PIN_BUTTON_ID).setMinimumWidth(50d);

        addComponentColumn(target -> buildActionButton(
                clickEvent -> targetDeleteSupport.openConfirmationWindowDeleteAction(target, target.getName()),
                VaadinIcons.TRASH, UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.TARGET_DELET_ICON + "." + target.getId(),
                targetDeleteSupport.hasDeletePermission())).setId(TARGET_DELETE_BUTTON_ID).setMinimumWidth(50d);

        getDefaultHeaderRow().join(TARGET_PIN_BUTTON_ID, TARGET_DELETE_BUTTON_ID)
                .setText(i18n.getMessage("header.action"));
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
     * Adds support to resize the target grid.
     */
    class TargetResizeSupport implements ResizeSupport {

        private final String[] maxColumnOrder = new String[] { TARGET_NAME_ID, TARGET_CREATED_BY_ID,
                TARGET_CREATED_DATE_ID, TARGET_MODIFIED_BY_ID, TARGET_MODIFIED_DATE_ID, TARGET_DESC_ID,
                TARGET_DELETE_BUTTON_ID };

        private final String[] minColumnOrder = new String[] { TARGET_NAME_ID, TARGET_POLLING_STATUS_ID,
                TARGET_STATUS_ID, TARGET_PIN_BUTTON_ID, TARGET_DELETE_BUTTON_ID };

        @Override
        public void setMaximizedColumnOrder() {
            clearSortOrder();
            setColumnOrder(maxColumnOrder);
        }

        @Override
        public void setMaximizedHiddenColumns() {
            getColumn(TARGET_POLLING_STATUS_ID).setHidden(true);
            getColumn(TARGET_STATUS_ID).setHidden(true);
            getColumn(TARGET_PIN_BUTTON_ID).setHidden(true);

            getColumn(TARGET_CREATED_BY_ID).setHidden(false);
            getColumn(TARGET_CREATED_DATE_ID).setHidden(false);
            getColumn(TARGET_MODIFIED_BY_ID).setHidden(false);
            getColumn(TARGET_MODIFIED_DATE_ID).setHidden(false);
            getColumn(TARGET_DESC_ID).setHidden(false);

            getColumns().forEach(column -> column.setHidable(true));
        }

        @Override
        public void setMaximizedColumnExpandRatio() {
            getColumns().forEach(column -> column.setExpandRatio(0));

            getColumn(TARGET_NAME_ID).setExpandRatio(1);
            getColumn(TARGET_DESC_ID).setExpandRatio(1);
        }

        @Override
        public void setMinimizedColumnOrder() {
            clearSortOrder();
            setColumnOrder(minColumnOrder);
        }

        @Override
        public void setMinimizedHiddenColumns() {
            getColumn(TARGET_POLLING_STATUS_ID).setHidden(false);
            getColumn(TARGET_STATUS_ID).setHidden(false);
            getColumn(TARGET_PIN_BUTTON_ID).setHidden(false);

            getColumn(TARGET_CREATED_BY_ID).setHidden(true);
            getColumn(TARGET_CREATED_DATE_ID).setHidden(true);
            getColumn(TARGET_MODIFIED_BY_ID).setHidden(true);
            getColumn(TARGET_MODIFIED_DATE_ID).setHidden(true);
            getColumn(TARGET_DESC_ID).setHidden(true);

            getColumns().forEach(column -> column.setHidable(false));
        }

        @Override
        public void setMinimizedColumnExpandRatio() {
            getColumns().forEach(column -> column.setExpandRatio(0));

            getColumn(TARGET_NAME_ID).setExpandRatio(1);
        }
    }
}