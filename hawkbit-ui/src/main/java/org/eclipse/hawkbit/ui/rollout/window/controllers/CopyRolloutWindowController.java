/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.controllers;

import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.RepositoryModelConstants;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditions;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowLayoutComponentBuilder;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AddRolloutWindowLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AutoStartOptionGroupLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AutoStartOptionGroupLayout.AutoStartOption;
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
    protected ProxyRolloutWindow buildEntityFromProxy(final ProxyRollout proxyEntity) {
        final ProxyRolloutWindow proxyRolloutWindow = new ProxyRolloutWindow(proxyEntity);

        proxyRolloutWindow.setName(i18n.getMessage("textfield.rollout.copied.name", proxyRolloutWindow.getName()));

        final Page<TargetFilterQuery> filterQueries = targetFilterQueryManagement.findByQuery(PageRequest.of(0, 1),
                proxyRolloutWindow.getTargetFilterQuery());
        if (filterQueries.getTotalElements() > 0) {
            proxyRolloutWindow.setTargetFilterId(filterQueries.getContent().get(0).getId());
        }

        proxyRolloutWindow.setTotalTargets(targetManagement.countByRsql(proxyRolloutWindow.getTargetFilterQuery()));

        if (proxyRolloutWindow.getForcedTime() == null
                || RepositoryModelConstants.NO_FORCE_TIME.equals(proxyRolloutWindow.getForcedTime())) {
            proxyRolloutWindow.setForcedTime(SPDateTimeUtil.twoWeeksFromNowEpochMilli());
        }

        proxyRolloutWindow.setAutoStartOption(layout.getStartAtOption(proxyRolloutWindow.getStartAt()));
        if (AutoStartOption.SCHEDULED != proxyRolloutWindow.getAutoStartOption()) {
            proxyRolloutWindow.setStartAt(SPDateTimeUtil.halfAnHourFromNowEpochMilli());
        }

        final RolloutGroupConditions defaultRolloutGroupConditions = RolloutWindowLayoutComponentBuilder
                .getDefaultRolloutGroupConditions();
        proxyRolloutWindow.setTriggerThresholdPercentage(defaultRolloutGroupConditions.getSuccessConditionExp());
        proxyRolloutWindow.setErrorThresholdPercentage(defaultRolloutGroupConditions.getErrorConditionExp());

        return proxyRolloutWindow;
    }

    @Override
    protected void adaptLayout() {
        layout.populateTotalTargetsLegend();
        layout.populateAdvancedRolloutGroups();
        layout.selectAdvancedRolloutGroupsTab();
    }
}
