/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.actionhistory;

import java.util.Map;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.model.Action.Status;
import org.eclipse.hawkbit.ui.common.builder.IconBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.ActionStatusToProxyActionStatusMapper;
import org.eclipse.hawkbit.ui.common.data.providers.ActionStatusDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyActionStatus;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.FilterSupport;
import org.eclipse.hawkbit.ui.common.grid.support.MasterEntitySupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Label;

/**
 * This grid presents the action states for a selected action.
 */
public class ActionStatusGrid extends AbstractGrid<ProxyActionStatus, Long> {
    private static final long serialVersionUID = 1L;

    private static final String STATUS_ID = "status";
    private static final String CREATED_AT_ID = "createdAt";

    private final Map<Status, ProxyFontIcon> statusIconMap;

    private final transient MasterEntitySupport<ProxyAction> masterEntitySupport;

    /**
     * Constructor.
     *
     * @param i18n
     * @param eventBus
     * @param deploymentManagement
     */
    protected ActionStatusGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final DeploymentManagement deploymentManagement) {
        super(i18n, eventBus, null);

        setSelectionSupport(new SelectionSupport<ProxyActionStatus>(this, eventBus,
                EventLayout.ACTION_HISTORY_STATUS_LIST, EventView.DEPLOYMENT, null, null, null));
        getSelectionSupport().enableSingleSelection();

        setFilterSupport(new FilterSupport<>(
                new ActionStatusDataProvider(deploymentManagement, new ActionStatusToProxyActionStatusMapper())));
        initFilterMappings();

        this.masterEntitySupport = new MasterEntitySupport<>(getFilterSupport());

        statusIconMap = IconBuilder.generateActionStatusIcons(i18n);

        init();
    }

    private void initFilterMappings() {
        getFilterSupport().<Long> addMapping(FilterType.MASTER,
                (filter, masterFilter) -> getFilterSupport().setFilter(masterFilter));
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.ACTION_HISTORY_DETAILS_GRID_ID;
    }

    @Override
    public void addColumns() {
        addComponentColumn(this::buildStatusIcon).setId(STATUS_ID).setCaption(i18n.getMessage("header.status"))
                .setMinimumWidth(53d).setMaximumWidth(53d).setHidable(false).setHidden(false)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);

        addColumn(actionStatus -> SPDateTimeUtil.getFormattedDate(actionStatus.getCreatedAt(),
                SPUIDefinitions.LAST_QUERY_DATE_FORMAT_SHORT)).setId(CREATED_AT_ID)
                        .setCaption(i18n.getMessage("header.rolloutgroup.target.date"))
                        .setDescriptionGenerator(
                                actionStatus -> SPDateTimeUtil.getFormattedDate(actionStatus.getCreatedAt()))
                        .setMinimumWidth(100d).setMaximumWidth(400d).setHidable(false).setHidden(false);
    }

    private Label buildStatusIcon(final ProxyActionStatus actionStatus) {
        return IconBuilder.buildStatusIconLabel(i18n, statusIconMap, ProxyActionStatus::getStatus,
                UIComponentIdProvider.ACTION_STATUS_GRID_STATUS_LABEL_ID, actionStatus);
    }

    public MasterEntitySupport<ProxyAction> getMasterEntitySupport() {
        return masterEntitySupport;
    }
}
