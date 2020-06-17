/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import java.util.Arrays;

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow.GroupDefinitionMode;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.rollout.window.layouts.ValidatableLayout.ValidationStatus;
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
    private final VisualGroupDefinitionLayout visualGroupDefinitionLayout;

    private String filterQuery;
    private Long totalTargets;
    private int noOfGroups;

    public AddRolloutWindowLayout(final RolloutWindowDependencies dependencies) {
        super(dependencies);

        this.targetManagement = dependencies.getTargetManagement();

        this.rolloutFormLayout = rolloutComponentBuilder.createRolloutFormLayout();
        this.simpleGroupsLayout = rolloutComponentBuilder.createSimpleGroupsLayout();
        this.advancedGroupsLayout = rolloutComponentBuilder.createAdvancedGroupsLayout();
        this.groupsDefinitionTabs = rolloutComponentBuilder.createGroupDefinitionTabs(simpleGroupsLayout.getLayout(),
                advancedGroupsLayout.getLayout());
        this.visualGroupDefinitionLayout = rolloutComponentBuilder.createVisualGroupDefinitionLayout();

        addValueChangeListeners();

        addValidatableLayouts(Arrays.asList(rolloutFormLayout, simpleGroupsLayout));
    }

    @Override
    protected void addComponents(final GridLayout rootLayout) {
        rootLayout.setRows(7);

        final int lastRowIdx = rootLayout.getRows() - 1;
        final int lastColumnIdx = rootLayout.getColumns() - 1;

        rolloutFormLayout.addFormToAddLayout(rootLayout);
        visualGroupDefinitionLayout.addChartWithLegendToLayout(rootLayout, lastColumnIdx, 3);
        rootLayout.addComponent(groupsDefinitionTabs, 0, lastRowIdx, lastColumnIdx, lastRowIdx);
    }

    private void addValueChangeListeners() {
        rolloutFormLayout.setFilterQueryChangedListener(this::onTargetFilterQueryChange);
        groupsDefinitionTabs.addSelectedTabChangeListener(event -> onGroupDefinitionTabChanged());
        simpleGroupsLayout.setNoOfGroupsChangedListener(this::onNoOfSimpleGroupsChanged);
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
        visualGroupDefinitionLayout.setTotalTargets(totalTargets);
        if (isSimpleGroupsTabSelected()) {
            simpleGroupsLayout.setTotalTargets(totalTargets);
        }
    }

    public boolean isSimpleGroupsTabSelected() {
        return groupsDefinitionTabs.getSelectedTab().equals(simpleGroupsLayout.getLayout());
    }

    public boolean isAdvancedGroupsTabSelected() {
        return groupsDefinitionTabs.getSelectedTab().equals(advancedGroupsLayout.getLayout());
    }

    private void onGroupDefinitionTabChanged() {
        if (isSimpleGroupsTabSelected()) {
            removeValidatableLayout(advancedGroupsLayout);
            // TODO: save button is not disabled after tab switch if simple
            // group definition is INVALID and was INVALID before. We need some
            // flag to reset the Validation Status (e.g. UNKNOWN or INACTIVE),
            // etc.
            addValidatableLayout(simpleGroupsLayout);

            simpleGroupsLayout.setTotalTargets(totalTargets);

            visualGroupDefinitionLayout.setGroupDefinitionMode(GroupDefinitionMode.SIMPLE);
            visualGroupDefinitionLayout.setNoOfGroups(noOfGroups);
        }

        if (isAdvancedGroupsTabSelected()) {
            removeValidatableLayout(simpleGroupsLayout);
            // TODO: extract onAdvancedGroupsChanged to value change listener
            addValidatableLayout(advancedGroupsLayout, this::onAdvancedGroupsChanged);

            advancedGroupsLayout.setTargetFilter(filterQuery);

            visualGroupDefinitionLayout.setGroupDefinitionMode(GroupDefinitionMode.ADVANCED);
            visualGroupDefinitionLayout.setAdvancedRolloutGroupsValidation(advancedGroupsLayout.getGroupsValidation(),
                    advancedGroupsLayout.getSavedRolloutGroupDefinitions());
        }
    }

    private void onNoOfSimpleGroupsChanged(final int noOfGroups) {
        if (!isSimpleGroupsTabSelected()) {
            return;
        }

        this.noOfGroups = noOfGroups;

        visualGroupDefinitionLayout.setNoOfGroups(noOfGroups);
    }

    private void onAdvancedGroupsChanged(final ValidationStatus status) {
        if (!isAdvancedGroupsTabSelected()) {
            return;
        }

        if (status == AdvancedGroupsLayout.ValidationStatus.LOADING) {
            visualGroupDefinitionLayout.displayLoading();
        } else {
            visualGroupDefinitionLayout.setAdvancedRolloutGroupsValidation(advancedGroupsLayout.getGroupsValidation(),
                    advancedGroupsLayout.getSavedRolloutGroupDefinitions());
        }
    }

    public void addAdvancedGroupRowAndValidate() {
        advancedGroupsLayout.addGroupRowAndValidate();
    }

    public void selectAdvancedGroupsTab() {
        groupsDefinitionTabs.setSelectedTab(advancedGroupsLayout.getLayout());
    }

    @Override
    public void setEntity(final ProxyRolloutWindow proxyEntity) {
        rolloutFormLayout.setBean(proxyEntity.getRolloutForm());
        simpleGroupsLayout.setBean(proxyEntity.getSimpleGroupsDefinition());
        advancedGroupsLayout.populateByRolloutGroups(proxyEntity.getAdvancedRolloutGroups());
        visualGroupDefinitionLayout.setGroupDefinitionMode(proxyEntity.getGroupDefinitionMode());
    }

    @Override
    public ProxyRolloutWindow getEntity() {
        final ProxyRolloutWindow proxyEntity = new ProxyRolloutWindow();
        proxyEntity.setRolloutForm(rolloutFormLayout.getBean());
        proxyEntity.setSimpleGroupsDefinition(simpleGroupsLayout.getBean());
        proxyEntity.setAdvancedRolloutGroupDefinitions(advancedGroupsLayout.getSavedRolloutGroupDefinitions());
        proxyEntity.setGroupDefinitionMode(
                isSimpleGroupsTabSelected() ? GroupDefinitionMode.SIMPLE : GroupDefinitionMode.ADVANCED);

        return proxyEntity;
    }
}
