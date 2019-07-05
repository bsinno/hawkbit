/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.controllers;

import java.time.LocalDateTime;

import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditions;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowLayoutComponentBuilder;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AddRolloutWindowLayout;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Controller for populating data in Copy Rollout Window.
 */
public class CopyRolloutWindowController extends AddRolloutWindowController {

    private final TargetManagement targetManagement;
    private final TargetFilterQueryManagement targetFilterQueryManagement;

    public CopyRolloutWindowController(final RolloutWindowDependencies dependencies,
            final AddRolloutWindowLayout layout) {
        super(dependencies, layout);
        this.targetManagement = dependencies.getTargetManagement();
        this.targetFilterQueryManagement = dependencies.getTargetFilterQueryManagement();
    }

    @Override
    public void populateWithData(final ProxyRollout proxyRollout) {
        proxyRolloutWindow = new ProxyRolloutWindow(proxyRollout);

        proxyRolloutWindow.setName(i18n.getMessage("textfield.rollout.copied.name", proxyRolloutWindow.getName()));
        final Page<TargetFilterQuery> filterQueries = targetFilterQueryManagement.findByQuery(PageRequest.of(0, 1),
                proxyRolloutWindow.getTargetFilterQuery());
        if (filterQueries.getTotalElements() > 0) {
            proxyRolloutWindow.setTargetFilterId(filterQueries.getContent().get(0).getId());
        }
        proxyRolloutWindow.setTotalTargets(targetManagement.countByRsql(proxyRolloutWindow.getTargetFilterQuery()));

        if (proxyRolloutWindow.getForcedTime() == null) {
            proxyRolloutWindow.setForcedTime(LocalDateTime.now().plusWeeks(2)
                    .atZone(SPDateTimeUtil.getTimeZoneId(SPDateTimeUtil.getBrowserTimeZone())).toInstant()
                    .toEpochMilli());
        }
        final RolloutGroupConditions defaultRolloutGroupConditions = RolloutWindowLayoutComponentBuilder
                .getDefaultRolloutGroupConditions();
        proxyRolloutWindow.setTriggerThresholdPercentage(defaultRolloutGroupConditions.getSuccessConditionExp());
        proxyRolloutWindow.setErrorThresholdPercentage(defaultRolloutGroupConditions.getErrorConditionExp());

        layout.getProxyRolloutBinder().setBean(proxyRolloutWindow);
        layout.populateTotalTargetsLegend();
        layout.populateAdvancedRolloutGroups();
        layout.selectAdvancedRolloutGroupsTab();
    }
}
