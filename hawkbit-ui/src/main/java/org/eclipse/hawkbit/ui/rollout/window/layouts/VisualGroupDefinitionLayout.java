/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAdvancedRolloutGroup;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow.GroupDefinitionMode;
import org.eclipse.hawkbit.ui.rollout.groupschart.GroupsPieChart;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.springframework.util.CollectionUtils;

import com.vaadin.ui.GridLayout;

public class VisualGroupDefinitionLayout {

    private final GroupsPieChart groupsPieChart;
    private final GroupsLegendLayout groupsLegendLayout;

    private Long totalTargets;
    private int noOfGroups;
    private List<ProxyAdvancedRolloutGroup> advancedRolloutGroupDefinitions;
    private GroupDefinitionMode groupDefinitionMode;

    public VisualGroupDefinitionLayout(final GroupsPieChart groupsPieChart,
            final GroupsLegendLayout groupsLegendLayout) {
        this.groupsPieChart = groupsPieChart;
        this.groupsLegendLayout = groupsLegendLayout;
    }

    public void setTotalTargets(final Long totalTargets) {
        this.totalTargets = totalTargets;

        groupsLegendLayout.setTotalTargets(totalTargets);

        if (groupDefinitionMode == GroupDefinitionMode.SIMPLE) {
            updateBySimpleGroupsDefinition();
        } else {
            updateByAdvancedGroupsDefinition();
        }
    }

    private void updateBySimpleGroupsDefinition() {
        if (!HawkbitCommonUtil.atLeastOnePresent(totalTargets) || noOfGroups <= 0) {
            clearGroupChartAndLegend();
            return;
        }

        final List<Long> targetsPerGroup = new ArrayList<>(noOfGroups);
        long leftTargets = totalTargets;
        for (int i = 0; i < noOfGroups; i++) {
            final double percentage = 1.0 / (noOfGroups - i);
            final long targetsInGroup = Math.round(percentage * leftTargets);
            leftTargets -= targetsInGroup;
            targetsPerGroup.add(targetsInGroup);
        }

        groupsPieChart.setChartState(totalTargets, targetsPerGroup);
        groupsLegendLayout.populateGroupsLegend(targetsPerGroup);
    }

    private void clearGroupChartAndLegend() {
        groupsPieChart.setChartState(0L, Collections.emptyList());
        groupsLegendLayout.populateGroupsLegend(Collections.emptyList());
    }

    private void updateByAdvancedGroupsDefinition() {
        if (!HawkbitCommonUtil.atLeastOnePresent(totalTargets)
                || CollectionUtils.isEmpty(advancedRolloutGroupDefinitions)) {
            clearGroupChartAndLegend();
            return;
        }

        final List<Long> targetsPerGroup = getTargetsPerGroupByDefinitions();
        final List<String> groupNames = getGroupNamesByDefinitions();

        groupsPieChart.setChartState(totalTargets, targetsPerGroup);
        groupsLegendLayout.populateGroupsLegend(totalTargets, targetsPerGroup, groupNames);
    }

    private List<Long> getTargetsPerGroupByDefinitions() {
        return advancedRolloutGroupDefinitions.stream().map(ProxyAdvancedRolloutGroup::getTargetsCount)
                .collect(Collectors.toList());
    }

    private List<String> getGroupNamesByDefinitions() {
        return advancedRolloutGroupDefinitions.stream().map(ProxyAdvancedRolloutGroup::getGroupName)
                .collect(Collectors.toList());
    }

    public void setNoOfGroups(final int noOfGroups) {
        this.noOfGroups = noOfGroups;

        if (groupDefinitionMode == GroupDefinitionMode.SIMPLE) {
            updateBySimpleGroupsDefinition();
        }
    }

    public void setAdvancedRolloutGroupDefinitions(
            final List<ProxyAdvancedRolloutGroup> advancedRolloutGroupDefinitions) {
        this.advancedRolloutGroupDefinitions = advancedRolloutGroupDefinitions;

        if (groupDefinitionMode == GroupDefinitionMode.ADVANCED) {
            updateByAdvancedGroupsDefinition();
        }
    }

    public void displayLoading() {
        groupsLegendLayout.displayLoading();
    }

    public void setGroupDefinitionMode(final GroupDefinitionMode groupDefinitionMode) {
        this.groupDefinitionMode = groupDefinitionMode;
    }

    public void addChartWithLegendToLayout(final GridLayout layout, final int lastColumnIdx, final int heightInRows) {
        layout.addComponent(groupsPieChart, lastColumnIdx - 1, 0, lastColumnIdx - 1, heightInRows);
        layout.addComponent(groupsLegendLayout, lastColumnIdx, 0, lastColumnIdx, heightInRows);
    }
}
