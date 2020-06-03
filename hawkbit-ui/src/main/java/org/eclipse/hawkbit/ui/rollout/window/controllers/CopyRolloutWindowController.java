/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.controllers;

import java.util.List;

import org.eclipse.hawkbit.repository.QuotaManagement;
import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.model.RepositoryModelConstants;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow.GroupDefinitionMode;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AddRolloutWindowLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AutoStartOptionGroupLayout.AutoStartOption;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.CollectionUtils;

/**
 * Controller for populating data in Copy Rollout Window.
 */
public class CopyRolloutWindowController extends AddRolloutWindowController {

    private final TargetFilterQueryManagement targetFilterQueryManagement;
    private final RolloutGroupManagement rolloutGroupManagement;
    private final QuotaManagement quotaManagement;

    public CopyRolloutWindowController(final RolloutWindowDependencies dependencies,
            final AddRolloutWindowLayout layout) {
        super(dependencies, layout);

        this.targetFilterQueryManagement = dependencies.getTargetFilterQueryManagement();
        this.rolloutGroupManagement = dependencies.getRolloutGroupManagement();
        this.quotaManagement = dependencies.getQuotaManagement();
    }

    @Override
    protected ProxyRolloutWindow buildEntityFromProxy(final ProxyRollout proxyEntity) {
        final ProxyRolloutWindow proxyRolloutWindow = new ProxyRolloutWindow(proxyEntity);

        proxyRolloutWindow.setName(i18n.getMessage("textfield.rollout.copied.name", proxyRolloutWindow.getName()));

        setTargetFilterId(proxyRolloutWindow);

        if (proxyRolloutWindow.getForcedTime() == null
                || RepositoryModelConstants.NO_FORCE_TIME.equals(proxyRolloutWindow.getForcedTime())) {
            proxyRolloutWindow.setForcedTime(SPDateTimeUtil.twoWeeksFromNowEpochMilli());
        }

        proxyRolloutWindow.setAutoStartOption(proxyRolloutWindow.getOptionByStartAt());
        if (AutoStartOption.SCHEDULED != proxyRolloutWindow.getAutoStartOption()) {
            proxyRolloutWindow.setStartAt(SPDateTimeUtil.halfAnHourFromNowEpochMilli());
        }

        proxyRolloutWindow.setGroupDefinitionMode(GroupDefinitionMode.ADVANCED);
        setRolloutGroups(proxyRolloutWindow);

        if (CollectionUtils.isEmpty(proxyRolloutWindow.getAdvancedRolloutGroups())) {
            setDefaultThresholds(proxyRolloutWindow);
        } else {
            setThresholdsOfFirstGroup(proxyRolloutWindow);
        }

        return proxyRolloutWindow;
    }

    private void setTargetFilterId(final ProxyRolloutWindow proxyRolloutWindow) {
        final Page<TargetFilterQuery> filterQueries = targetFilterQueryManagement.findByQuery(PageRequest.of(0, 1),
                proxyRolloutWindow.getTargetFilterQuery());
        if (filterQueries.getTotalElements() > 0) {
            proxyRolloutWindow.setTargetFilterId(filterQueries.getContent().get(0).getId());
        }
    }

    private void setRolloutGroups(final ProxyRolloutWindow proxyRolloutWindow) {
        final List<RolloutGroup> advancedRolloutGroups = rolloutGroupManagement
                .findByRollout(PageRequest.of(0, quotaManagement.getMaxRolloutGroupsPerRollout()),
                        proxyRolloutWindow.getId())
                .getContent();
        proxyRolloutWindow.setAdvancedRolloutGroups(advancedRolloutGroups);
    }

    private void setThresholdsOfFirstGroup(final ProxyRolloutWindow proxyRolloutWindow) {
        final RolloutGroup firstAdvancedRolloutGroup = proxyRolloutWindow.getAdvancedRolloutGroups().get(0);
        proxyRolloutWindow.setTriggerThresholdPercentage(firstAdvancedRolloutGroup.getSuccessConditionExp());
        proxyRolloutWindow.setErrorThresholdPercentage(firstAdvancedRolloutGroup.getErrorConditionExp());
    }

    @Override
    protected void adaptLayout() {
        layout.selectAdvancedGroupsTab();
    }
}
