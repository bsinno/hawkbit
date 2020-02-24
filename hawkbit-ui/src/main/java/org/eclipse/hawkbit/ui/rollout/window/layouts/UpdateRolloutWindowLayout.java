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

import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.ui.common.builder.BoundComponent;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.management.miscs.ActionTypeOptionGroupAssignmentLayout;
import org.eclipse.hawkbit.ui.rollout.groupschart.GroupsPieChart;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowLayoutComponentBuilder;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AutoStartOptionGroupLayout.AutoStartOption;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;

/**
 * Layout builder for Update Rollout window.
 */
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2160" })
public class UpdateRolloutWindowLayout extends AbstractRolloutWindowLayout {
    private final ComboBox<ProxyDistributionSet> distributionSet;
    private final BoundComponent<ActionTypeOptionGroupAssignmentLayout> actionTypeOptionGroupLayout;
    private final BoundComponent<AutoStartOptionGroupLayout> autoStartOptionGroupLayout;
    private final GroupsPieChart groupsPieChart;
    private final GroupsLegendLayout groupsLegendLayout;

    public UpdateRolloutWindowLayout(final RolloutWindowDependencies dependencies) {
        super(dependencies);

        this.distributionSet = rolloutComponentBuilder.createDistributionSetCombo(binder);
        this.actionTypeOptionGroupLayout = rolloutComponentBuilder.createActionTypeOptionGroupLayout(binder);
        this.autoStartOptionGroupLayout = rolloutComponentBuilder.createAutoStartOptionGroupLayout(binder);
        this.groupsPieChart = rolloutComponentBuilder.createGroupsPieChart();
        this.groupsLegendLayout = rolloutComponentBuilder.createGroupsLegendLayout();

        addValueChangeListeners();
    }

    @Override
    protected void addComponents(final GridLayout rootLayout) {
        rootLayout.setRows(6);

        rootLayout.addComponent(rolloutComponentBuilder.getLabel(RolloutWindowLayoutComponentBuilder.TEXTFIELD_NAME), 0,
                0);
        final TextField rolloutName = rolloutComponentBuilder.createRolloutNameField(binder);
        rootLayout.addComponent(rolloutName, 1, 0);
        rolloutName.focus();

        rootLayout.addComponent(
                rolloutComponentBuilder.getLabel(RolloutWindowLayoutComponentBuilder.PROMPT_DISTRIBUTION_SET), 0, 1);
        rootLayout.addComponent(distributionSet, 1, 1);

        rootLayout.addComponent(
                rolloutComponentBuilder.getLabel(RolloutWindowLayoutComponentBuilder.PROMPT_TARGET_FILTER), 0, 2);
        rootLayout.addComponent(rolloutComponentBuilder.createTargetFilterQuery(binder), 1, 2);

        rootLayout.addComponent(
                rolloutComponentBuilder.getLabel(RolloutWindowLayoutComponentBuilder.TEXTFIELD_DESCRIPTION), 0, 3);
        rootLayout.addComponent(rolloutComponentBuilder.createDescription(binder), 1, 3, 1, 3);

        rootLayout.addComponent(groupsLegendLayout, 3, 0, 3, 3);

        rootLayout.addComponent(groupsPieChart, 2, 0, 2, 3);

        rootLayout.addComponent(
                rolloutComponentBuilder.getLabel(RolloutWindowLayoutComponentBuilder.CAPTION_ROLLOUT_ACTION_TYPE), 0,
                4);
        rootLayout.addComponent(actionTypeOptionGroupLayout.getComponent(), 1, 4, 3, 4);

        rootLayout.addComponent(
                rolloutComponentBuilder.getLabel(RolloutWindowLayoutComponentBuilder.CAPTION_ROLLOUT_START_TYPE), 0, 5);
        rootLayout.addComponent(autoStartOptionGroupLayout.getComponent(), 1, 5, 3, 5);
    }

    public void disableRequiredFieldsOnEdit() {
        distributionSet.setEnabled(false);
        actionTypeOptionGroupLayout.getComponent().getActionTypeOptionGroup().setEnabled(false);
        autoStartOptionGroupLayout.getComponent().getAutoStartOptionGroup().setEnabled(false);
    }

    public void updateGroupsChart(final List<RolloutGroup> savedGroups, final long totalTargetsCount) {
        final List<Long> targetsPerGroup = savedGroups.stream().map(group -> (long) group.getTotalTargets())
                .collect(Collectors.toList());

        groupsPieChart.setChartState(targetsPerGroup, totalTargetsCount);
        groupsLegendLayout.populateGroupsLegendByGroups(savedGroups);
    }

    public void populateTotalTargetsLegend() {
        groupsLegendLayout.populateTotalTargets(getEntity().getTotalTargets());
    }

    private void addValueChangeListeners() {
        actionTypeOptionGroupLayout.getComponent().getActionTypeOptionGroup().addValueChangeListener(
                event -> actionTypeOptionGroupLayout.setRequired(event.getValue() == ActionType.TIMEFORCED));
        autoStartOptionGroupLayout.getComponent().getAutoStartOptionGroup().addValueChangeListener(
                event -> autoStartOptionGroupLayout.setRequired(event.getValue() == AutoStartOption.SCHEDULED));
    }
}
