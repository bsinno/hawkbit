/**
 * Copyright (c) 2018 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rollout;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.TotalTargetCountStatus;
import org.eclipse.hawkbit.ui.common.UserDetailsFormatter;
import org.eclipse.hawkbit.ui.customrenderers.client.renderers.RolloutRendererData;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SpringContextHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public class ProxyRolloutService {

    private final String searchText;

    private transient RolloutManagement rolloutManagement;

    private transient RolloutUIState rolloutUIState;

    public ProxyRolloutService() {
        this.searchText = getSearchText();
    }

    public List<ProxyRollout> findAll() {
        final Slice<Rollout> rolloutBeans;
        final PageRequest pageRequest = new PageRequest(0, SPUIDefinitions.PAGE_SIZE, Sort.Direction.ASC);
        if (StringUtils.isEmpty(searchText)) {
            rolloutBeans = getRolloutManagement().findAllWithDetailedStatus(pageRequest, false);
        } else {
            rolloutBeans = getRolloutManagement().findByFiltersWithDetailedStatus(pageRequest, searchText, false);
        }
        return getProxyRolloutList(rolloutBeans);
    }

    public int size() {
        long size = getRolloutManagement().count();
        if (!StringUtils.isEmpty(searchText)) {
            size = getRolloutManagement().countByFilters(searchText);
        }
        return (int) size;
    }

    protected ProxyRollout constructBean() {
        return new ProxyRollout();
    }

    // protected List<ProxyRollout> loadBeans(final int startIndex) {
    // final Slice<Rollout> rolloutBeans;
    // final PageRequest pageRequest = new PageRequest(startIndex /
    // SPUIDefinitions.PAGE_SIZE,
    // SPUIDefinitions.PAGE_SIZE, sort);
    // if (StringUtils.isEmpty(searchText)) {
    // rolloutBeans =
    // getRolloutManagement().findAllWithDetailedStatus(pageRequest, false);
    // } else {
    // rolloutBeans =
    // getRolloutManagement().findByFiltersWithDetailedStatus(pageRequest,
    // searchText, false);
    // }
    // return getProxyRolloutList(rolloutBeans);
    // }

    private static List<ProxyRollout> getProxyRolloutList(final Slice<Rollout> rolloutBeans) {
        return rolloutBeans.getContent().stream().map(ProxyRolloutService::createProxy).collect(Collectors.toList());
    }

    private static ProxyRollout createProxy(final Rollout rollout) {
        final ProxyRollout proxyRollout = new ProxyRollout();
        proxyRollout.setName(rollout.getName());
        proxyRollout.setDescription(rollout.getDescription());
        final DistributionSet distributionSet = rollout.getDistributionSet();
        proxyRollout.setDistributionSetNameVersion(
                HawkbitCommonUtil.getFormattedNameVersion(distributionSet.getName(), distributionSet.getVersion()));
        proxyRollout.setNumberOfGroups(rollout.getRolloutGroupsCreated());
        proxyRollout.setCreatedDate(SPDateTimeUtil.getFormattedDate(rollout.getCreatedAt()));
        proxyRollout.setModifiedDate(SPDateTimeUtil.getFormattedDate(rollout.getLastModifiedAt()));
        proxyRollout.setCreatedBy(UserDetailsFormatter.loadAndFormatCreatedBy(rollout));
        proxyRollout.setLastModifiedBy(UserDetailsFormatter.loadAndFormatLastModifiedBy(rollout));
        proxyRollout.setForcedTime(rollout.getForcedTime());
        proxyRollout.setId(rollout.getId());
        proxyRollout.setStatus(rollout.getStatus());
        proxyRollout.setRolloutRendererData(new RolloutRendererData(rollout.getName(), rollout.getStatus().toString()));

        final TotalTargetCountStatus totalTargetCountActionStatus = rollout.getTotalTargetCountStatus();
        proxyRollout.setTotalTargetCountStatus(totalTargetCountActionStatus);
        proxyRollout.setTotalTargetsCount(String.valueOf(rollout.getTotalTargets()));
        proxyRollout.setApprovalDecidedBy(rollout.getApprovalDecidedBy());
        proxyRollout.setApprovalRemark(rollout.getApprovalRemark());
        return proxyRollout;
    }

    private String getSearchText() {
        return getRolloutUIState().getSearchText().map(

                value -> String.format("%%%s%%", value)).orElse(null);

    }

    private RolloutManagement getRolloutManagement() {
        if (rolloutManagement == null) {
            rolloutManagement = SpringContextHelper.getBean(RolloutManagement.class);
        }
        return rolloutManagement;
    }

    private RolloutUIState getRolloutUIState() {
        if (rolloutUIState == null) {
            rolloutUIState = SpringContextHelper.getBean(RolloutUIState.class);
        }
        return rolloutUIState;
    }
}
