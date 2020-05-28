/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
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

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.builder.RolloutGroupCreate;
import org.eclipse.hawkbit.repository.model.RolloutGroupsValidation;
import org.eclipse.hawkbit.ui.rollout.groupschart.GroupsPieChart;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TabSheet;

/**
 * Layout builder for Add Rollout window together with component value change
 * listeners.
 */
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2160" })
public class AddRolloutWindowLayout extends AbstractRolloutWindowLayout {

    private final TargetManagement targetManagement;

    private final RolloutFormLayout rolloutFormLayout;
    private final SimpleGroupsLayout simpleGroupsLayout;
    private final AdvancedGroupsLayout advancedGroupsLayout;
    private final TabSheet groupsDefinitionTabs;
    private final GroupsPieChart groupsPieChart;
    private final GroupsLegendLayout groupsLegendLayout;

    private String filterQuery;
    private Long totalTargets;
    private int noOfGroups;

    public AddRolloutWindowLayout(final RolloutWindowDependencies dependencies) {
        super(dependencies);

        this.targetManagement = dependencies.getTargetManagement();

        this.rolloutFormLayout = rolloutComponentBuilder.createRolloutFormLayout();
        this.simpleGroupsLayout = rolloutComponentBuilder.createSimpleGroupsLayout();
        this.advancedGroupsLayout = rolloutComponentBuilder.createAdvancedGroupDefinitionTab();
        this.groupsDefinitionTabs = rolloutComponentBuilder.createGroupDefinitionTabs(simpleGroupsLayout,
                advancedGroupsLayout);
        this.groupsPieChart = rolloutComponentBuilder.createGroupsPieChart();
        this.groupsLegendLayout = rolloutComponentBuilder.createGroupsLegendLayout();

        addValueChangeListeners();
    }

    @Override
    protected void addComponents(final GridLayout rootLayout) {
        rootLayout.setRows(7);

        rolloutFormLayout.addRowToLayout(rootLayout, false);
        rootLayout.addComponent(groupsPieChart, 2, 0, 2, 3);
        rootLayout.addComponent(groupsLegendLayout, 3, 0, 3, 3);
        rootLayout.addComponent(groupsDefinitionTabs, 0, 6, 3, 6);
    }

    private void addValueChangeListeners() {
        rolloutFormLayout.setFilterQueryChangedListener(this::onTargetFilterQueryChange);
        groupsDefinitionTabs.addSelectedTabChangeListener(event -> onGroupDefinitionTabChanged());
        simpleGroupsLayout.setNoOfGroupsChangedListener(this::onNoOfSimpleGroupsChanged);
        advancedGroupsLayout.setValidationListener(this::onAdvancedGroupsChanged);
    }

    private void onTargetFilterQueryChange(final String filterQuery) {
        this.filterQuery = filterQuery;

        totalTargets = !StringUtils.isEmpty(filterQuery) ? targetManagement.countByRsql(filterQuery) : null;
        updateTotalTargetsAwareComponents();

        if (isAdvancedGroupsTabSelected()) {
            advancedGroupsLayout.setTargetFilter(filterQuery);
        }
    }

    private void updateTotalTargetsAwareComponents() {
        rolloutFormLayout.setTotalTargets(totalTargets);
        groupsLegendLayout.setTotalTargets(totalTargets);
        if (isSimpleGroupsTabSelected()) {
            simpleGroupsLayout.setTotalTargets(totalTargets);
        }

        updateGroupsChart(totalTargets, noOfGroups);
    }

    public boolean isSimpleGroupsTabSelected() {
        return groupsDefinitionTabs.getSelectedTab().equals(simpleGroupsLayout);
    }

    private void updateGroupsChart(final Long totalTargets, final int noOfGroups) {
        if (!HawkbitCommonUtil.atLeastOnePresent(totalTargets) || noOfGroups <= 0) {
            groupsPieChart.setChartState(Collections.emptyList(), 0L);
            groupsLegendLayout.populateGroupsLegendByTargetCounts(Collections.emptyList());
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

        groupsPieChart.setChartState(targetsPerGroup, totalTargets);
        groupsLegendLayout.populateGroupsLegendByTargetCounts(targetsPerGroup);
    }

    public boolean isAdvancedGroupsTabSelected() {
        return groupsDefinitionTabs.getSelectedTab().equals(advancedGroupsLayout);
    }

    private void onGroupDefinitionTabChanged() {
        if (isSimpleGroupsTabSelected()) {
            simpleGroupsLayout.setTotalTargets(totalTargets);
            updateGroupsChart(totalTargets, noOfGroups);
        }

        if (isAdvancedGroupsTabSelected()) {
            advancedGroupsLayout.setTargetFilter(filterQuery);
            updateGroupsChart(totalTargets, advancedGroupsLayout.getGroupsValidation());
        }
    }

    private void updateGroupsChart(final Long totalTargets, final RolloutGroupsValidation validation) {
        if (validation == null) {
            groupsPieChart.setChartState(Collections.emptyList(), 0L);
            return;
        }

        final List<Long> targetsPerGroup = validation.getTargetsPerGroup();
        if (!HawkbitCommonUtil.atLeastOnePresent(totalTargets) || CollectionUtils.isEmpty(targetsPerGroup)) {
            groupsPieChart.setChartState(Collections.emptyList(), 0L);
        } else {
            groupsPieChart.setChartState(targetsPerGroup, totalTargets);
        }

        groupsLegendLayout.populateGroupsLegendByValidation(validation, getAdvancedRolloutGroups());
    }

    public List<RolloutGroupCreate> getAdvancedRolloutGroups() {
        return advancedGroupsLayout.getSavedRolloutGroups();
    }

    private void onNoOfSimpleGroupsChanged(final int noOfGroups) {
        if (!isSimpleGroupsTabSelected()) {
            return;
        }

        this.noOfGroups = noOfGroups;

        updateGroupsChart(totalTargets, noOfGroups);
    }

    private void onAdvancedGroupsChanged(final AdvancedGroupsLayout.ValidationStatus status) {
        if (!isAdvancedGroupsTabSelected()) {
            return;
        }

        if (status == AdvancedGroupsLayout.ValidationStatus.LOADING) {
            groupsLegendLayout.displayLoading();
        } else {
            updateGroupsChart(totalTargets, advancedGroupsLayout.getGroupsValidation());
        }
    }

    public void addAdvancedGroupRowAndValidate() {
        advancedGroupsLayout.addGroupRowAndValidate();
    }

    public void selectAdvancedGroupsTab() {
        groupsDefinitionTabs.setSelectedTab(advancedGroupsLayout);
    }

    // TODO: !!!
    public void populateAdvancedRolloutGroups() {
        advancedGroupsLayout.setTargetFilter(getEntity().getTargetFilterQuery());
        advancedGroupsLayout.populateByRolloutId(getEntity().getId());
    }

    public void populateTotalTargetsLegend() {
        groupsLegendLayout.setTotalTargets(getEntity().getTotalTargets());
    }
}
