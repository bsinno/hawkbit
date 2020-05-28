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
import org.eclipse.hawkbit.ui.rollout.groupschart.GroupsPieChart;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;

import com.vaadin.ui.GridLayout;

/**
 * Layout builder for Update Rollout window.
 */
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2160" })
public class UpdateRolloutWindowLayout extends AbstractRolloutWindowLayout {
    private final RolloutFormLayout rolloutFormLayout;
    private final GroupsPieChart groupsPieChart;
    private final GroupsLegendLayout groupsLegendLayout;

    public UpdateRolloutWindowLayout(final RolloutWindowDependencies dependencies) {
        super(dependencies);

        this.rolloutFormLayout = rolloutComponentBuilder.createRolloutFormLayout();
        this.groupsPieChart = rolloutComponentBuilder.createGroupsPieChart();
        this.groupsLegendLayout = rolloutComponentBuilder.createGroupsLegendLayout();
    }

    @Override
    protected void addComponents(final GridLayout rootLayout) {
        rootLayout.setRows(6);

        rolloutFormLayout.addRowToLayout(rootLayout, true);
        rootLayout.addComponent(groupsPieChart, 2, 0, 2, 3);
        rootLayout.addComponent(groupsLegendLayout, 3, 0, 3, 3);
    }

    public void disableRequiredFieldsOnEdit() {
        rolloutFormLayout.disableFieldsOnEdit();
    }

    public void updateGroupsChart(final List<RolloutGroup> savedGroups, final long totalTargetsCount) {
        final List<Long> targetsPerGroup = savedGroups.stream().map(group -> (long) group.getTotalTargets())
                .collect(Collectors.toList());

        groupsPieChart.setChartState(targetsPerGroup, totalTargetsCount);
        groupsLegendLayout.populateGroupsLegendByGroups(savedGroups);
    }

    public void populateTotalTargetsLegend() {
        groupsLegendLayout.setTotalTargets(getEntity().getTotalTargets());
    }
}
