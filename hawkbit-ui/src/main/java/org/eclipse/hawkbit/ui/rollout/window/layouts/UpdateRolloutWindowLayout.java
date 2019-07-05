/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.management.miscs.ActionTypeOptionGroupAssignmentLayout;
import org.eclipse.hawkbit.ui.rollout.groupschart.GroupsPieChart;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowLayoutComponentBuilder;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;

/**
 * Layout builder for Update Rollout window.
 */
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2160" })
public class UpdateRolloutWindowLayout extends AbstractRolloutWindowLayout {
    private static final long serialVersionUID = 1L;

    private final ComboBox<ProxyDistributionSet> distributionSet;
    private final ActionTypeOptionGroupAssignmentLayout actionTypeOptionGroupLayout;
    private final AutoStartOptionGroupLayout autoStartOptionGroupLayout;
    private final GroupsPieChart groupsPieChart;
    private final GroupsLegendLayout groupsLegendLayout;

    public UpdateRolloutWindowLayout(final RolloutWindowDependencies dependencies) {
        super(dependencies);

        this.distributionSet = componentBuilder.createDistributionSetCombo(proxyRolloutBinder);
        this.actionTypeOptionGroupLayout = componentBuilder.createActionTypeOptionGroupLayout(proxyRolloutBinder);
        this.autoStartOptionGroupLayout = componentBuilder.createAutoStartOptionGroupLayout(proxyRolloutBinder);
        this.groupsPieChart = componentBuilder.createGroupsPieChart();
        this.groupsLegendLayout = componentBuilder.createGroupsLegendLayout();

        buildLayout(componentBuilder);
    }

    public void buildLayout(final RolloutWindowLayoutComponentBuilder builder) {
        setRows(6);

        addComponent(builder.getLabel(RolloutWindowLayoutComponentBuilder.TEXTFIELD_NAME), 0, 0);
        final TextField rolloutName = builder.createRolloutNameField(proxyRolloutBinder);
        addComponent(rolloutName, 1, 0);
        rolloutName.focus();

        addComponent(builder.getLabel(RolloutWindowLayoutComponentBuilder.PROMPT_DISTRIBUTION_SET), 0, 1);
        addComponent(distributionSet, 1, 1);

        addComponent(builder.getLabel(RolloutWindowLayoutComponentBuilder.PROMPT_TARGET_FILTER), 0, 2);
        addComponent(builder.createTargetFilterQuery(proxyRolloutBinder), 1, 2);

        addComponent(builder.getLabel(RolloutWindowLayoutComponentBuilder.TEXTFIELD_DESCRIPTION), 0, 3);
        addComponent(builder.createDescription(proxyRolloutBinder), 1, 3, 1, 3);

        addComponent(groupsLegendLayout, 3, 0, 3, 3);

        addComponent(groupsPieChart, 2, 0, 2, 3);

        addComponent(builder.getLabel(RolloutWindowLayoutComponentBuilder.CAPTION_ROLLOUT_ACTION_TYPE), 0, 4);
        addComponent(actionTypeOptionGroupLayout, 1, 4, 3, 4);

        addComponent(builder.getLabel(RolloutWindowLayoutComponentBuilder.CAPTION_ROLLOUT_START_TYPE), 0, 5);
        addComponent(autoStartOptionGroupLayout, 1, 5, 3, 5);
    }

    public void disableRequiredFieldsOnEdit() {
        distributionSet.setEnabled(false);
        actionTypeOptionGroupLayout.getActionTypeOptionGroup().setEnabled(false);
        actionTypeOptionGroupLayout.addStyleName(SPUIStyleDefinitions.DISABLE_ACTION_TYPE_LAYOUT);
        autoStartOptionGroupLayout.getAutoStartOptionGroup().setEnabled(false);
        autoStartOptionGroupLayout.addStyleName(SPUIStyleDefinitions.DISABLE_ACTION_TYPE_LAYOUT);
    }

    public void updateGroupsChart(final List<RolloutGroup> savedGroups, final long totalTargetsCount) {
        final List<Long> targetsPerGroup = savedGroups.stream().map(group -> (long) group.getTotalTargets())
                .collect(Collectors.toList());

        groupsPieChart.setChartState(targetsPerGroup, totalTargetsCount);
        groupsLegendLayout.populateGroupsLegendByGroups(savedGroups);
    }

    public void populateTotalTargetsLegend() {
        groupsLegendLayout.populateTotalTargets(getTotalTargets());
    }
}
