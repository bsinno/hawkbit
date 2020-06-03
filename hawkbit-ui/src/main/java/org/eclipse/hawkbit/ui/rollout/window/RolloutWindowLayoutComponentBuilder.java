/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window;

import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetFilterQueryToProxyTargetFilterMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.rollout.groupschart.GroupsPieChart;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AdvancedGroupsLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.ApprovalLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.GroupsLegendLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.RolloutFormLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.SimpleGroupsLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.VisualGroupDefinitionLayout;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;

/**
 * Builder for Rollout window components.
 */
public final class RolloutWindowLayoutComponentBuilder {

    private final RolloutWindowDependencies dependencies;

    private final DistributionSetStatelessDataProvider distributionSetDataProvider;
    private final TargetFilterQueryDataProvider targetFilterQueryDataProvider;

    public RolloutWindowLayoutComponentBuilder(final RolloutWindowDependencies rolloutWindowDependecies) {
        this.dependencies = rolloutWindowDependecies;

        this.distributionSetDataProvider = new DistributionSetStatelessDataProvider(
                dependencies.getDistributionSetManagement(), new DistributionSetToProxyDistributionMapper());
        this.targetFilterQueryDataProvider = new TargetFilterQueryDataProvider(
                dependencies.getTargetFilterQueryManagement(), new TargetFilterQueryToProxyTargetFilterMapper());
    }

    public Label getLabel(final String key) {
        return new LabelBuilder().name(dependencies.getI18n().getMessage(key)).buildLabel();
    }

    public RolloutFormLayout createRolloutFormLayout() {
        return new RolloutFormLayout(dependencies.getI18n(), distributionSetDataProvider,
                targetFilterQueryDataProvider);
    }

    public SimpleGroupsLayout createSimpleGroupsLayout() {
        return new SimpleGroupsLayout(dependencies.getI18n(), dependencies.getQuotaManagement());
    }

    public AdvancedGroupsLayout createAdvancedGroupsLayout() {
        return new AdvancedGroupsLayout(dependencies.getI18n(), dependencies.getEntityFactory(),
                dependencies.getRolloutManagement(), dependencies.getTargetFilterQueryManagement(),
                dependencies.getQuotaManagement(), targetFilterQueryDataProvider);
    }

    public TabSheet createGroupDefinitionTabs(final Component simpleGroupDefinitionTab,
            final Component advancedGroupDefinitionTab) {
        final TabSheet groupsDefinitionTabs = new TabSheet();
        groupsDefinitionTabs.setId(UIComponentIdProvider.ROLLOUT_GROUPS);
        groupsDefinitionTabs.setWidth(850, Unit.PIXELS);
        groupsDefinitionTabs.setHeight(300, Unit.PIXELS);
        groupsDefinitionTabs.setStyleName(SPUIStyleDefinitions.ROLLOUT_GROUPS);

        groupsDefinitionTabs
                .addTab(simpleGroupDefinitionTab, dependencies.getI18n().getMessage("caption.rollout.tabs.simple"))
                .setId(UIComponentIdProvider.ROLLOUT_SIMPLE_TAB);

        groupsDefinitionTabs
                .addTab(advancedGroupDefinitionTab, dependencies.getI18n().getMessage("caption.rollout.tabs.advanced"))
                .setId(UIComponentIdProvider.ROLLOUT_ADVANCED_TAB);

        return groupsDefinitionTabs;
    }

    public VisualGroupDefinitionLayout createVisualGroupDefinitionLayout() {
        return new VisualGroupDefinitionLayout(createGroupsPieChart(), createGroupsLegendLayout());
    }

    private GroupsPieChart createGroupsPieChart() {
        final GroupsPieChart groupsPieChart = new GroupsPieChart();
        groupsPieChart.setWidth(260, Unit.PIXELS);
        groupsPieChart.setHeight(220, Unit.PIXELS);
        groupsPieChart.setStyleName(SPUIStyleDefinitions.ROLLOUT_GROUPS_CHART);

        return groupsPieChart;
    }

    private GroupsLegendLayout createGroupsLegendLayout() {
        return new GroupsLegendLayout(dependencies.getI18n());
    }

    public ApprovalLayout createApprovalLayout() {
        return new ApprovalLayout(dependencies.getI18n());
    }
}
