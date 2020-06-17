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
import java.util.Optional;

import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupStatus;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.GridComponentBuilder;
import org.eclipse.hawkbit.ui.common.builder.StatusIconBuilder.RolloutGroupStatusIconSupplier;
import org.eclipse.hawkbit.ui.common.data.mappers.RolloutGroupToProxyRolloutGroupMapper;
import org.eclipse.hawkbit.ui.common.data.providers.RolloutGroupDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutGroup;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.FilterSupport;
import org.eclipse.hawkbit.ui.common.grid.support.MasterEntitySupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.rollout.DistributionBarHelper;
import org.eclipse.hawkbit.ui.rollout.RolloutManagementUIState;
import org.eclipse.hawkbit.ui.utils.SPUILabelDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.google.common.base.Predicates;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.renderers.HtmlRenderer;

/**
 * Rollout group list grid component.
 */
public class RolloutGroupGrid extends AbstractGrid<ProxyRolloutGroup, Long> {
    private static final long serialVersionUID = 1L;

    private static final String ROLLOUT_GROUP_LINK_ID = "rolloutGroup";

    private final RolloutManagementUIState rolloutManagementUIState;
    private final transient RolloutGroupManagement rolloutGroupManagement;
    private final transient RolloutGroupToProxyRolloutGroupMapper rolloutGroupMapper;

    private final RolloutGroupStatusIconSupplier<ProxyRolloutGroup> rolloutGroupStatusIconSupplier;

    private final transient MasterEntitySupport<ProxyRollout> masterEntitySupport;

    public RolloutGroupGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final RolloutGroupManagement rolloutGroupManagement,
            final RolloutManagementUIState rolloutManagementUIState) {
        super(i18n, eventBus, permissionChecker);

        this.rolloutManagementUIState = rolloutManagementUIState;
        this.rolloutGroupManagement = rolloutGroupManagement;
        this.rolloutGroupMapper = new RolloutGroupToProxyRolloutGroupMapper();

        setSelectionSupport(new SelectionSupport<>(this, eventBus, EventLayout.ROLLOUT_GROUP_LIST, EventView.ROLLOUT,
                this::mapIdToProxyEntity, this::getSelectedEntityIdFromUiState, this::setSelectedEntityIdToUiState));
        getSelectionSupport().disableSelection();

        setFilterSupport(new FilterSupport<>(new RolloutGroupDataProvider(rolloutGroupManagement, rolloutGroupMapper)));
        initFilterMappings();

        this.masterEntitySupport = new MasterEntitySupport<>(getFilterSupport());

        rolloutGroupStatusIconSupplier = new RolloutGroupStatusIconSupplier<>(i18n, ProxyRolloutGroup::getStatus,
                UIComponentIdProvider.ROLLOUT_GROUP_STATUS_LABEL_ID);
        init();
    }

    public Optional<ProxyRolloutGroup> mapIdToProxyEntity(final long entityId) {
        return rolloutGroupManagement.get(entityId).map(rolloutGroupMapper::map);
    }

    private Long getSelectedEntityIdFromUiState() {
        return rolloutManagementUIState.getSelectedRolloutGroupId();
    }

    private void setSelectedEntityIdToUiState(final Long entityId) {
        rolloutManagementUIState.setSelectedRolloutGroupId(entityId);
    }

    private void initFilterMappings() {
        getFilterSupport().<Long> addMapping(FilterType.MASTER,
                (filter, masterFilter) -> getFilterSupport().setFilter(masterFilter));
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.ROLLOUT_GROUP_LIST_GRID_ID;
    }

    @Override
    public void addColumns() {
        GridComponentBuilder.addComponentColumn(this, this::buildRolloutGroupLink).setId(ROLLOUT_GROUP_LINK_ID)
                .setCaption(i18n.getMessage("header.name")).setHidable(false).setExpandRatio(3);

        GridComponentBuilder.addIconColumn(this, rolloutGroupStatusIconSupplier::getLabel,
                SPUILabelDefinitions.VAR_STATUS, i18n.getMessage("header.status")).setHidable(true);

        addColumn(rolloutGroup -> DistributionBarHelper
                .getDistributionBarAsHTMLString(rolloutGroup.getTotalTargetCountStatus().getStatusTotalCountMap()),
                new HtmlRenderer()).setId(SPUILabelDefinitions.VAR_TOTAL_TARGETS_COUNT_STATUS)
                        .setCaption(i18n.getMessage("header.detail.status"))
                        .setDescriptionGenerator(
                                rolloutGroup -> DistributionBarHelper.getTooltip(
                                        rolloutGroup.getTotalTargetCountStatus().getStatusTotalCountMap(), i18n),
                                ContentMode.HTML)
                        .setExpandRatio(8).setHidable(true);

        GridComponentBuilder.addColumn(this, group -> group.getFinishedPercentage() + "%")
                .setId(SPUILabelDefinitions.ROLLOUT_GROUP_INSTALLED_PERCENTAGE)
                .setCaption(i18n.getMessage("header.rolloutgroup.installed.percentage")).setHidable(true);

        GridComponentBuilder.addColumn(this, group -> group.getErrorConditionExp() + "%")
                .setId(SPUILabelDefinitions.ROLLOUT_GROUP_ERROR_THRESHOLD)
                .setCaption(i18n.getMessage("header.rolloutgroup.threshold.error")).setHidable(true);

        GridComponentBuilder.addColumn(this, group -> group.getSuccessConditionExp() + "%")
                .setId(SPUILabelDefinitions.ROLLOUT_GROUP_THRESHOLD)
                .setCaption(i18n.getMessage("header.rolloutgroup.threshold")).setHidable(true);

        GridComponentBuilder.addCreatedByColumn(this, i18n, SPUILabelDefinitions.VAR_CREATED_USER).setHidable(true)
                .setHidden(true);
        GridComponentBuilder.addCreatedAtColumn(this, i18n, SPUILabelDefinitions.VAR_CREATED_DATE).setHidable(true)
                .setHidden(true);
        GridComponentBuilder.addModifiedByColumn(this, i18n, SPUILabelDefinitions.VAR_MODIFIED_BY).setHidable(true)
                .setHidden(true);
        GridComponentBuilder.addModifiedAtColumn(this, i18n, SPUILabelDefinitions.VAR_MODIFIED_DATE).setHidable(true)
                .setHidden(true);

        GridComponentBuilder.addDescriptionColumn(this, i18n, SPUILabelDefinitions.VAR_DESC).setHidable(true)
                .setHidden(true).setExpandRatio(1);

        GridComponentBuilder.addColumn(this, ProxyRolloutGroup::getTotalTargetsCount)
                .setId(SPUILabelDefinitions.VAR_TOTAL_TARGETS).setCaption(i18n.getMessage("header.total.targets"))
                .setHidable(true);
    }

    private Button buildRolloutGroupLink(final ProxyRolloutGroup rolloutGroup) {
        final boolean enableButton = RolloutGroupStatus.CREATING != rolloutGroup.getStatus();
        final ClickListener listener = permissionChecker.hasRolloutTargetsReadPermission()
                ? (clickEvent -> onClickOfRolloutGroupName(rolloutGroup))
                : null;
        final Button link = GridComponentBuilder.buildLink(rolloutGroup, "rolloutgroup.link.", rolloutGroup.getName(),
                enableButton, listener);
        if (!enableButton) {
            link.addStyleName("boldhide");
        }
        return link;
    }

    private void onClickOfRolloutGroupName(final ProxyRolloutGroup rolloutGroup) {
        getSelectionSupport().sendSelectionChangedEvent(SelectionChangedEventType.ENTITY_SELECTED, rolloutGroup);
        rolloutManagementUIState.setSelectedRolloutGroupName(rolloutGroup.getName());

        eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this, new LayoutVisibilityEventPayload(
                VisibilityType.SHOW, EventLayout.ROLLOUT_GROUP_TARGET_LIST, EventView.ROLLOUT));
    }

    public void updateGridItems(final Collection<Long> ids) {
        ids.stream().filter(Predicates.notNull()).map(rolloutGroupManagement::getWithDetailedStatus)
                .forEach(rolloutGroup -> rolloutGroup.ifPresent(this::updateGridItem));
    }

    private void updateGridItem(final RolloutGroup rolloutGroup) {
        final ProxyRolloutGroup proxyRolloutGroup = RolloutGroupToProxyRolloutGroupMapper.mapGroup(rolloutGroup);
        getDataProvider().refreshItem(proxyRolloutGroup);
    }

    @Override
    public void restoreState() {
        final Long masterEntityId = rolloutManagementUIState.getSelectedRolloutId();
        if (masterEntityId != null) {
            getMasterEntitySupport().masterEntityChanged(new ProxyRollout(masterEntityId));
        }
    }

    public MasterEntitySupport<ProxyRollout> getMasterEntitySupport() {
        return masterEntitySupport;
    }
}
