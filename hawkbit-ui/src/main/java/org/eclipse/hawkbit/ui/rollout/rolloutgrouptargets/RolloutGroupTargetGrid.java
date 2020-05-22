/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rolloutgrouptargets;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.Action.Status;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupStatus;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetWithActionStatusToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.providers.RolloutGroupTargetsDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutGroup;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.FilterSupport;
import org.eclipse.hawkbit.ui.common.grid.support.MasterEntitySupport;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
import org.eclipse.hawkbit.ui.rollout.RolloutManagementUIState;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUILabelDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Label;

/**
 * Grid component with targets of rollout group.
 */
public class RolloutGroupTargetGrid extends AbstractGrid<ProxyTarget, Long> {
    private static final long serialVersionUID = 1L;

    private final RolloutManagementUIState rolloutManagementUIState;

    private final transient RolloutGroupManagement rolloutGroupManagement;

    private final Map<Status, ProxyFontIcon> statusIconMap = new EnumMap<>(Status.class);

    private final transient MasterEntitySupport<ProxyRolloutGroup> masterEntitySupport;

    /**
     * Constructor for RolloutGroupTargetsListGrid
     * 
     * @param i18n
     *            I18N
     * @param eventBus
     *            UIEventBus
     * @param rolloutManagementUIState
     *            RolloutUIState
     */
    public RolloutGroupTargetGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final RolloutGroupManagement rolloutGroupManagement,
            final RolloutManagementUIState rolloutManagementUIState) {
        super(i18n, eventBus, null);
        this.rolloutManagementUIState = rolloutManagementUIState;
        this.rolloutGroupManagement = rolloutGroupManagement;

        setFilterSupport(new FilterSupport<>(new RolloutGroupTargetsDataProvider(rolloutGroupManagement,
                new TargetWithActionStatusToProxyTargetMapper())));
        initFilterMappings();

        this.masterEntitySupport = new MasterEntitySupport<>(getFilterSupport());

        initStatusIconMap();
        init();
    }

    private void initFilterMappings() {
        getFilterSupport().<Long> addMapping(FilterType.MASTER,
                (filter, masterFilter) -> getFilterSupport().setFilter(masterFilter));
    }

    private void initStatusIconMap() {
        statusIconMap.put(Status.FINISHED, new ProxyFontIcon(VaadinIcons.CHECK_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getStatusDescription(Status.FINISHED)));
        statusIconMap.put(Status.SCHEDULED, new ProxyFontIcon(VaadinIcons.HOURGLASS_EMPTY,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getStatusDescription(Status.SCHEDULED)));
        statusIconMap.put(Status.RUNNING, new ProxyFontIcon(VaadinIcons.ADJUST, SPUIStyleDefinitions.STATUS_ICON_YELLOW,
                getStatusDescription(Status.RUNNING)));
        statusIconMap.put(Status.RETRIEVED, new ProxyFontIcon(VaadinIcons.CHECK_CIRCLE_O,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getStatusDescription(Status.RETRIEVED)));
        statusIconMap.put(Status.WARNING, new ProxyFontIcon(VaadinIcons.EXCLAMATION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_ORANGE, getStatusDescription(Status.WARNING)));
        statusIconMap.put(Status.DOWNLOAD, new ProxyFontIcon(VaadinIcons.CLOUD_DOWNLOAD,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getStatusDescription(Status.DOWNLOAD)));
        statusIconMap.put(Status.DOWNLOADED, new ProxyFontIcon(VaadinIcons.CLOUD_DOWNLOAD,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getStatusDescription(Status.DOWNLOADED)));
        statusIconMap.put(Status.CANCELING, new ProxyFontIcon(VaadinIcons.CLOSE_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getStatusDescription(Status.CANCELING)));
        statusIconMap.put(Status.CANCELED, new ProxyFontIcon(VaadinIcons.CLOSE_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getStatusDescription(Status.CANCELED)));
        statusIconMap.put(Status.ERROR, new ProxyFontIcon(VaadinIcons.EXCLAMATION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_RED, getStatusDescription(Status.ERROR)));
    }

    private String getStatusDescription(final Status actionStatus) {
        return i18n
                .getMessage(UIMessageIdProvider.TOOLTIP_ACTION_STATUS_PREFIX + actionStatus.toString().toLowerCase());
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.ROLLOUT_GROUP_TARGETS_LIST_GRID_ID;
    }

    @Override
    public void addColumns() {
        addColumn(ProxyTarget::getName).setId(SPUILabelDefinitions.VAR_NAME).setCaption(i18n.getMessage("header.name"))
                .setMinimumWidth(20).setMaximumWidth(280);

        addComponentColumn(this::buildStatusIcon).setId(SPUILabelDefinitions.VAR_STATUS)
                .setCaption(i18n.getMessage("header.status")).setMinimumWidth(50).setMaximumWidth(80)
                .setStyleGenerator(item -> "v-align-center");

        addColumn(ProxyTarget::getCreatedDate).setId(SPUILabelDefinitions.VAR_CREATED_DATE)
                .setCaption(i18n.getMessage("header.createdDate")).setMaximumWidth(180).setMinimumWidth(30);

        addColumn(ProxyTarget::getCreatedBy).setId(SPUILabelDefinitions.VAR_CREATED_BY)
                .setCaption(i18n.getMessage("header.createdBy")).setMaximumWidth(180).setMinimumWidth(50);

        addColumn(ProxyTarget::getModifiedDate).setId(SPUILabelDefinitions.VAR_LAST_MODIFIED_DATE)
                .setCaption(i18n.getMessage("header.modifiedDate")).setMaximumWidth(180).setMinimumWidth(30);

        addColumn(ProxyTarget::getLastModifiedBy).setId(SPUILabelDefinitions.VAR_LAST_MODIFIED_BY)
                .setCaption(i18n.getMessage("header.modifiedBy")).setMaximumWidth(180).setMinimumWidth(50);

        addColumn(ProxyTarget::getDescription).setId(SPUILabelDefinitions.VAR_DESC)
                .setCaption(i18n.getMessage("header.description"));
    }

    private Label buildStatusIcon(final ProxyTarget target) {
        final Optional<RolloutGroup> group = rolloutGroupManagement
                .get(rolloutManagementUIState.getSelectedRolloutGroupId());

        final ProxyFontIcon statusFontIcon = target.getStatus() == null || statusIconMap.get(target.getStatus()) == null
                ? buildDefaultStatusIcon(group.orElse(null))
                : getFontIconFromStatusMap(target.getStatus(), group.orElse(null));

        final String statusId = new StringBuilder(UIComponentIdProvider.ROLLOUT_GROUP_TARGET_STATUS_LABEL_ID)
                .append(".").append(target.getId()).toString();

        return SPUIComponentProvider.getLabelIcon(statusFontIcon, statusId);
    }

    private ProxyFontIcon getFontIconFromStatusMap(final Status status, final RolloutGroup group) {
        if (Status.DOWNLOADED == status && isDownloadOnly(group)) {
            return statusIconMap.get(Status.FINISHED);
        }

        return statusIconMap.get(status);
    }

    private boolean isDownloadOnly(final RolloutGroup group) {
        if (group == null) {
            return false;
        }

        return ActionType.DOWNLOAD_ONLY == group.getRollout().getActionType();
    }

    // Actions are not created for targets when rollout's status is
    // READY and when duplicate assignment is done. In these cases
    // display a appropriate status with description
    private ProxyFontIcon buildDefaultStatusIcon(final RolloutGroup rolloutGroup) {
        if (rolloutGroup != null && rolloutGroup.getStatus() == RolloutGroupStatus.READY) {
            return new ProxyFontIcon(VaadinIcons.BULLSEYE, SPUIStyleDefinitions.STATUS_ICON_LIGHT_BLUE,
                    i18n.getMessage(UIMessageIdProvider.TOOLTIP_ROLLOUT_GROUP_STATUS_PREFIX
                            + RolloutGroupStatus.READY.toString().toLowerCase()));
        } else if (rolloutGroup != null && rolloutGroup.getStatus() == RolloutGroupStatus.FINISHED) {
            final DistributionSet dist = rolloutGroup.getRollout().getDistributionSet();
            final String ds = HawkbitCommonUtil.getFormattedNameVersion(dist.getName(), dist.getVersion());

            return new ProxyFontIcon(VaadinIcons.MINUS_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                    i18n.getMessage("message.dist.already.assigned", ds));
        } else {
            return new ProxyFontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                    i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN));
        }
    }

    @Override
    public void restoreState() {
        final Long masterEntityId = rolloutManagementUIState.getSelectedRolloutGroupId();
        if (masterEntityId != null) {
            getMasterEntitySupport().masterEntityChanged(new ProxyRolloutGroup(masterEntityId));
        }
    }

    public MasterEntitySupport<ProxyRolloutGroup> getMasterEntitySupport() {
        return masterEntitySupport;
    }
}
