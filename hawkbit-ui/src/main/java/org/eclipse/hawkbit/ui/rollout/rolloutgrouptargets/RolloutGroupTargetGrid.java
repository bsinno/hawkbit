/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rolloutgrouptargets;

import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.ui.common.builder.StatusIconBuilder.RolloutActionStatusIconSupplier;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetWithActionStatusToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.providers.RolloutGroupTargetsDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutGroup;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.FilterSupport;
import org.eclipse.hawkbit.ui.common.grid.support.MasterEntitySupport;
import org.eclipse.hawkbit.ui.rollout.RolloutManagementUIState;
import org.eclipse.hawkbit.ui.utils.SPUILabelDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Grid component with targets of rollout group.
 */
public class RolloutGroupTargetGrid extends AbstractGrid<ProxyTarget, Long> {
    private static final long serialVersionUID = 1L;

    private final RolloutManagementUIState rolloutManagementUIState;

    private final RolloutActionStatusIconSupplier<ProxyTarget> actionStatusIconSupplier;

    private final transient MasterEntitySupport<ProxyRolloutGroup> masterEntitySupport;

    /**
     * Constructor for RolloutGroupTargetsListGrid
     * 
     * @param i18n
     *            I18N
     * @param eventBus
     *            UIEventBus
     * @param rolloutGroupManagement
     *            RolloutGroupManagement
     * @param rolloutManagementUIState
     *            RolloutUIState
     */
    public RolloutGroupTargetGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final RolloutGroupManagement rolloutGroupManagement,
            final RolloutManagementUIState rolloutManagementUIState) {
        super(i18n, eventBus, null);
        this.rolloutManagementUIState = rolloutManagementUIState;

        setFilterSupport(new FilterSupport<>(new RolloutGroupTargetsDataProvider(rolloutGroupManagement,
                new TargetWithActionStatusToProxyTargetMapper())));
        initFilterMappings();

        this.masterEntitySupport = new MasterEntitySupport<>(getFilterSupport());

        actionStatusIconSupplier = new RolloutActionStatusIconSupplier<>(i18n, ProxyTarget::getStatus,
                UIComponentIdProvider.ROLLOUT_GROUP_TARGET_STATUS_LABEL_ID, rolloutGroupManagement,
                rolloutManagementUIState);
        init();
    }

    private void initFilterMappings() {
        getFilterSupport().<Long> addMapping(FilterType.MASTER,
                (filter, masterFilter) -> getFilterSupport().setFilter(masterFilter));
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.ROLLOUT_GROUP_TARGETS_LIST_GRID_ID;
    }

    @Override
    public void addColumns() {
        addColumn(ProxyTarget::getName).setId(SPUILabelDefinitions.VAR_NAME).setCaption(i18n.getMessage("header.name"))
                .setMinimumWidth(20).setMaximumWidth(280);

        addComponentColumn(actionStatusIconSupplier::getLabel).setId(SPUILabelDefinitions.VAR_STATUS)
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
