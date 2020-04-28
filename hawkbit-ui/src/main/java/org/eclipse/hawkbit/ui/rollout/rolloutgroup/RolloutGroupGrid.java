/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rolloutgroup;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupStatus;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.RolloutGroupToProxyRolloutGroupMapper;
import org.eclipse.hawkbit.ui.common.data.providers.RolloutGroupDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutGroup;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.rollout.DistributionBarHelper;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
import org.eclipse.hawkbit.ui.rollout.RolloutManagementUIState;
import org.eclipse.hawkbit.ui.utils.SPUILabelDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.google.common.base.Predicates;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.renderers.HtmlRenderer;

/**
 * Rollout group list grid component.
 */
public class RolloutGroupGrid extends AbstractGrid<ProxyRolloutGroup, Long>
        implements MasterEntityAwareComponent<ProxyRollout> {
    private static final long serialVersionUID = 1L;

    private static final String ROLLOUT_GROUP_LINK_ID = "rolloutGroup";

    private final RolloutManagementUIState rolloutManagementUIState;
    private final transient RolloutGroupManagement rolloutGroupManagement;
    private final transient RolloutGroupToProxyRolloutGroupMapper rolloutGroupMapper;

    private final Map<RolloutGroupStatus, ProxyFontIcon> statusIconMap = new EnumMap<>(RolloutGroupStatus.class);

    private Long masterId;

    private final ConfigurableFilterDataProvider<ProxyRolloutGroup, Void, Long> rolloutGroupDataProvider;

    public RolloutGroupGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final RolloutGroupManagement rolloutGroupManagement,
            final RolloutManagementUIState rolloutManagementUIState) {
        super(i18n, eventBus, permissionChecker);

        this.rolloutManagementUIState = rolloutManagementUIState;
        this.rolloutGroupManagement = rolloutGroupManagement;
        this.rolloutGroupMapper = new RolloutGroupToProxyRolloutGroupMapper();

        this.rolloutGroupDataProvider = new RolloutGroupDataProvider(rolloutGroupManagement, rolloutGroupMapper)
                .withConfigurableFilter();

        setSelectionSupport(new SelectionSupport<>(this, eventBus, EventLayout.ROLLOUT_GROUP_LIST, EventView.ROLLOUT,
                this::mapIdToProxyEntity, this::getSelectedEntityIdFromUiState, this::setSelectedEntityIdToUiState));
        getSelectionSupport().disableSelection();

        initStatusIconMap();
        init();
    }

    public Optional<ProxyRolloutGroup> mapIdToProxyEntity(final long entityId) {
        return rolloutGroupManagement.get(entityId).map(rolloutGroupMapper::map);
    }

    private Optional<Long> getSelectedEntityIdFromUiState() {
        return Optional.ofNullable(rolloutManagementUIState.getSelectedRolloutGroupId());
    }

    private void setSelectedEntityIdToUiState(final Optional<Long> entityId) {
        rolloutManagementUIState.setSelectedRolloutGroupId(entityId.orElse(null));
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyRolloutGroup, Void, Long> getFilterDataProvider() {
        return rolloutGroupDataProvider;
    }

    private void initStatusIconMap() {
        statusIconMap.put(RolloutGroupStatus.FINISHED, new ProxyFontIcon(VaadinIcons.CHECK_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getStatusDescription(RolloutGroupStatus.FINISHED)));
        statusIconMap.put(RolloutGroupStatus.SCHEDULED, new ProxyFontIcon(VaadinIcons.HOURGLASS_START,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getStatusDescription(RolloutGroupStatus.SCHEDULED)));
        statusIconMap.put(RolloutGroupStatus.RUNNING, new ProxyFontIcon(VaadinIcons.ADJUST,
                SPUIStyleDefinitions.STATUS_ICON_YELLOW, getStatusDescription(RolloutGroupStatus.RUNNING)));
        statusIconMap.put(RolloutGroupStatus.READY, new ProxyFontIcon(VaadinIcons.BULLSEYE,
                SPUIStyleDefinitions.STATUS_ICON_LIGHT_BLUE, getStatusDescription(RolloutGroupStatus.READY)));
        statusIconMap.put(RolloutGroupStatus.ERROR, new ProxyFontIcon(VaadinIcons.EXCLAMATION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_RED, getStatusDescription(RolloutGroupStatus.ERROR)));
    }

    private String getStatusDescription(final RolloutGroupStatus groupStatus) {
        return i18n.getMessage(
                UIMessageIdProvider.TOOLTIP_ROLLOUT_GROUP_STATUS_PREFIX + groupStatus.toString().toLowerCase());
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.ROLLOUT_GROUP_LIST_GRID_ID;
    }

    @Override
    public void addColumns() {
        addComponentColumn(this::buildRolloutGroupLink).setId(ROLLOUT_GROUP_LINK_ID)
                .setCaption(i18n.getMessage("header.name")).setMinimumWidth(40).setMaximumWidth(200).setHidable(true);

        addComponentColumn(this::buildStatusIcon).setId(SPUILabelDefinitions.VAR_STATUS)
                .setCaption(i18n.getMessage("header.status")).setMinimumWidth(75).setMaximumWidth(75).setHidable(true)
                .setStyleGenerator(item -> "v-align-center");

        addColumn(rolloutGroup -> DistributionBarHelper
                .getDistributionBarAsHTMLString(rolloutGroup.getTotalTargetCountStatus().getStatusTotalCountMap()),
                new HtmlRenderer()).setId(SPUILabelDefinitions.VAR_TOTAL_TARGETS_COUNT_STATUS)
                        .setCaption(i18n.getMessage("header.detail.status"))
                        .setDescriptionGenerator(
                                rolloutGroup -> DistributionBarHelper.getTooltip(
                                        rolloutGroup.getTotalTargetCountStatus().getStatusTotalCountMap(), i18n),
                                ContentMode.HTML)
                        .setMinimumWidth(280).setHidable(true);

        addColumn(ProxyRolloutGroup::getFinishedPercentage)
                .setId(SPUILabelDefinitions.ROLLOUT_GROUP_INSTALLED_PERCENTAGE)
                .setCaption(i18n.getMessage("header.rolloutgroup.installed.percentage")).setMinimumWidth(40)
                .setMaximumWidth(100).setHidable(true);

        addColumn(ProxyRolloutGroup::getErrorConditionExp).setId(SPUILabelDefinitions.ROLLOUT_GROUP_ERROR_THRESHOLD)
                .setCaption(i18n.getMessage("header.rolloutgroup.threshold.error")).setMinimumWidth(40)
                .setMaximumWidth(100).setHidable(true);

        addColumn(ProxyRolloutGroup::getSuccessConditionExp).setId(SPUILabelDefinitions.ROLLOUT_GROUP_THRESHOLD)
                .setCaption(i18n.getMessage("header.rolloutgroup.threshold")).setMinimumWidth(40).setMaximumWidth(100)
                .setHidable(true);

        addColumn(ProxyRolloutGroup::getCreatedBy).setId(SPUILabelDefinitions.VAR_CREATED_USER)
                .setCaption(i18n.getMessage("header.createdBy")).setHidable(true).setHidden(true);

        addColumn(ProxyRolloutGroup::getCreatedDate).setId(SPUILabelDefinitions.VAR_CREATED_DATE)
                .setCaption(i18n.getMessage("header.createdDate")).setHidable(true).setHidden(true);

        addColumn(ProxyRolloutGroup::getModifiedDate).setId(SPUILabelDefinitions.VAR_MODIFIED_DATE)
                .setCaption(i18n.getMessage("header.modifiedDate")).setHidable(true).setHidden(true);

        addColumn(ProxyRolloutGroup::getLastModifiedBy).setId(SPUILabelDefinitions.VAR_MODIFIED_BY)
                .setCaption(i18n.getMessage("header.modifiedBy")).setHidable(true).setHidden(true);

        addColumn(ProxyRolloutGroup::getDescription).setId(SPUILabelDefinitions.VAR_DESC)
                .setCaption(i18n.getMessage("header.description")).setHidable(true).setHidden(true);

        addColumn(ProxyRolloutGroup::getTotalTargetsCount).setId(SPUILabelDefinitions.VAR_TOTAL_TARGETS)
                .setCaption(i18n.getMessage("header.total.targets")).setMinimumWidth(40).setMaximumWidth(100)
                .setHidable(true);
    }

    private Label buildStatusIcon(final ProxyRolloutGroup rolloutGroup) {
        final ProxyFontIcon statusFontIcon = Optional.ofNullable(statusIconMap.get(rolloutGroup.getStatus()))
                .orElse(new ProxyFontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String statusId = new StringBuilder(UIComponentIdProvider.ROLLOUT_GROUP_STATUS_LABEL_ID).append(".")
                .append(rolloutGroup.getId()).toString();

        return SPUIComponentProvider.getLabelIcon(statusFontIcon, statusId);
    }

    private Button buildRolloutGroupLink(final ProxyRolloutGroup rolloutGroup) {
        final Button rolloutGroupLink = new Button();

        if (permissionChecker.hasRolloutTargetsReadPermission()) {
            rolloutGroupLink.addClickListener(clickEvent -> onClickOfRolloutGroupName(rolloutGroup));
        }

        rolloutGroupLink.setId(new StringBuilder("rolloutgroup.link.").append(rolloutGroup.getId()).toString());
        rolloutGroupLink.addStyleName("borderless");
        rolloutGroupLink.addStyleName("small");
        rolloutGroupLink.addStyleName("on-focus-no-border");
        rolloutGroupLink.addStyleName("link");
        rolloutGroupLink.setCaption(rolloutGroup.getName());
        // this is to allow the button to disappear, if the text is null
        rolloutGroupLink.setVisible(rolloutGroup.getName() != null);

        /*
         * checking RolloutGroup Status for applying button style. If
         * RolloutGroup status is not "CREATING", then the RolloutGroup button
         * is applying hyperlink style
         */
        final boolean isStatusCreating = rolloutGroup.getStatus() != null
                && RolloutGroupStatus.CREATING.equals(rolloutGroup.getStatus());
        if (isStatusCreating) {
            rolloutGroupLink.addStyleName("boldhide");
            rolloutGroupLink.setEnabled(false);
        } else {
            rolloutGroupLink.setEnabled(true);
        }

        return rolloutGroupLink;
    }

    private void onClickOfRolloutGroupName(final ProxyRolloutGroup rolloutGroup) {
        getSelectionSupport().sendSelectionChangedEvent(SelectionChangedEventType.ENTITY_SELECTED, rolloutGroup);
        rolloutManagementUIState.setSelectedRolloutGroupName(rolloutGroup.getName());

        eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this, new LayoutVisibilityEventPayload(
                VisibilityType.SHOW, EventLayout.ROLLOUT_GROUP_TARGET_LIST, EventView.ROLLOUT));
    }

    @Override
    public void masterEntityChanged(final ProxyRollout masterEntity) {
        if (masterEntity == null && masterId == null) {
            return;
        }

        final Long masterEntityId = masterEntity != null ? masterEntity.getId() : null;
        getFilterDataProvider().setFilter(masterEntityId);
        masterId = masterEntityId;
    }

    public Long getMasterEntityId() {
        return masterId;
    }

    public void updateGridItems(final Collection<Long> ids) {
        ids.stream().filter(Predicates.notNull()).map(rolloutGroupManagement::getWithDetailedStatus)
                .forEach(rolloutGroup -> rolloutGroup.ifPresent(this::updateGridItem));
    }

    private void updateGridItem(final RolloutGroup rolloutGroup) {
        final ProxyRolloutGroup proxyRolloutGroup = RolloutGroupToProxyRolloutGroupMapper.mapGroup(rolloutGroup);
        getDataProvider().refreshItem(proxyRolloutGroup);
    }

    public void restoreState() {
        final Long masterEntityId = rolloutManagementUIState.getSelectedRolloutId();
        if (masterEntityId != null) {
            masterEntityChanged(new ProxyRollout(masterEntityId));
        }
    }
}
