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
import java.util.Map.Entry;

import org.eclipse.hawkbit.repository.builder.RolloutGroupCreate;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.RolloutGroupsValidation;
import org.eclipse.hawkbit.ui.common.builder.BoundComponent;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.management.miscs.ActionTypeOptionGroupAssignmentLayout;
import org.eclipse.hawkbit.ui.rollout.groupschart.GroupsPieChart;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowLayoutComponentBuilder;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowLayoutComponentBuilder.ERROR_THRESHOLD_OPTIONS;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AutoStartOptionGroupLayout.AutoStartOption;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;

import com.vaadin.data.Binder.Binding;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;

/**
 * Layout builder for Add Rollout window together with component value change
 * listeners.
 */
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2160" })
public class AddRolloutWindowLayout extends AbstractRolloutWindowLayout {
    private final VaadinMessageSource i18n;

    private final BoundComponent<ActionTypeOptionGroupAssignmentLayout> actionTypeLayout;
    private final BoundComponent<AutoStartOptionGroupLayout> autoStartOptionGroupLayout;
    private final ComboBox<ProxyTargetFilterQuery> targetFilterQueryCombo;
    private final TextField noOfGroups;
    private final Binding<ProxyRolloutWindow, Integer> noOfGroupsFieldBinding;
    private final Label groupSizeLabel;
    private final TextField errorThreshold;
    private final RadioButtonGroup<ERROR_THRESHOLD_OPTIONS> errorThresholdOptionGroup;
    private final DefineGroupsLayout defineGroupsLayout;
    private final TabSheet groupsDefinitionTabs;
    private final GroupsPieChart groupsPieChart;
    private final GroupsLegendLayout groupsLegendLayout;

    public AddRolloutWindowLayout(final RolloutWindowDependencies dependencies) {
        super(dependencies);

        this.i18n = dependencies.getI18n();

        this.actionTypeLayout = rolloutComponentBuilder.createActionTypeOptionGroupLayout(binder);
        this.autoStartOptionGroupLayout = rolloutComponentBuilder.createAutoStartOptionGroupLayout(binder);
        this.targetFilterQueryCombo = rolloutComponentBuilder.createTargetFilterQueryCombo(binder,
                this::updateTotalTargetsByQuery);
        final Entry<TextField, Binding<ProxyRolloutWindow, Integer>> noOfGroupsWithBinding = rolloutComponentBuilder
                .createNoOfGroupsField(binder, this::getGroupSizeByGroupNumber);
        this.noOfGroups = noOfGroupsWithBinding.getKey();
        this.noOfGroupsFieldBinding = noOfGroupsWithBinding.getValue();
        this.groupSizeLabel = rolloutComponentBuilder.createCountLabel();
        this.errorThresholdOptionGroup = rolloutComponentBuilder.createErrorThresholdOptionGroup();
        this.errorThreshold = rolloutComponentBuilder.createErrorThreshold(binder, errorThresholdOptionGroup::getValue,
                this::getGroupSize);
        this.defineGroupsLayout = rolloutComponentBuilder.createAdvancedGroupDefinitionTab();
        this.groupsDefinitionTabs = rolloutComponentBuilder
                .createGroupDefinitionTabs(rolloutComponentBuilder.createSimpleGroupDefinitionTab(noOfGroups,
                        groupSizeLabel, errorThreshold, errorThresholdOptionGroup, binder), defineGroupsLayout);
        this.groupsPieChart = rolloutComponentBuilder.createGroupsPieChart();
        this.groupsLegendLayout = rolloutComponentBuilder.createGroupsLegendLayout();

        addValueChangeListeners();
    }

    private int getGroupSize() {
        if (StringUtils.isEmpty(noOfGroups.getValue())) {
            return 0;
        }

        return getGroupSizeByGroupNumber(Integer.parseInt(noOfGroups.getValue()));
    }

    @Override
    protected void addComponents(final GridLayout rootLayout) {
        rootLayout.setRows(7);

        rootLayout.addComponent(rolloutComponentBuilder.getLabel(RolloutWindowLayoutComponentBuilder.TEXTFIELD_NAME), 0,
                0);
        final TextField rolloutName = rolloutComponentBuilder.createRolloutNameField(binder);
        rootLayout.addComponent(rolloutName, 1, 0);
        rolloutName.focus();

        rootLayout.addComponent(
                rolloutComponentBuilder.getLabel(RolloutWindowLayoutComponentBuilder.PROMPT_DISTRIBUTION_SET), 0, 1);
        rootLayout.addComponent(rolloutComponentBuilder.createDistributionSetCombo(binder), 1, 1);

        rootLayout.addComponent(
                rolloutComponentBuilder.getLabel(RolloutWindowLayoutComponentBuilder.PROMPT_TARGET_FILTER), 0, 2);
        rootLayout.addComponent(targetFilterQueryCombo, 1, 2);

        rootLayout.addComponent(
                rolloutComponentBuilder.getLabel(RolloutWindowLayoutComponentBuilder.TEXTFIELD_DESCRIPTION), 0, 3);
        rootLayout.addComponent(rolloutComponentBuilder.createDescription(binder), 1, 3, 1, 3);

        rootLayout.addComponent(groupsLegendLayout, 3, 0, 3, 3);

        rootLayout.addComponent(groupsPieChart, 2, 0, 2, 3);

        rootLayout.addComponent(
                rolloutComponentBuilder.getLabel(RolloutWindowLayoutComponentBuilder.CAPTION_ROLLOUT_ACTION_TYPE), 0,
                4);
        rootLayout.addComponent(actionTypeLayout.getComponent(), 1, 4, 3, 4);

        rootLayout.addComponent(
                rolloutComponentBuilder.getLabel(RolloutWindowLayoutComponentBuilder.CAPTION_ROLLOUT_START_TYPE), 0, 5);
        rootLayout.addComponent(autoStartOptionGroupLayout.getComponent(), 1, 5, 3, 5);

        rootLayout.addComponent(groupsDefinitionTabs, 0, 6, 3, 6);
    }

    public void addAdvancedGroupRowAndValidate() {
        defineGroupsLayout.addGroupRowAndValidate();
    }

    public boolean isNumberOfGroups() {
        return getPositionOfSelectedTab() == 0;
    }

    public boolean isGroupsDefinition() {
        return getPositionOfSelectedTab() == 1;
    }

    public List<RolloutGroupCreate> getAdvancedRolloutGroups() {
        return defineGroupsLayout.getSavedRolloutGroups();
    }

    public void populateAdvancedRolloutGroups() {
        defineGroupsLayout.setTargetFilter(getEntity().getTargetFilterQuery());
        defineGroupsLayout.populateByRolloutId(getEntity().getId());
    }

    public void selectAdvancedRolloutGroupsTab() {
        groupsDefinitionTabs.setSelectedTab(1);
    }

    public void populateTotalTargetsLegend() {
        groupsLegendLayout.populateTotalTargets(getEntity().getTotalTargets());
    }

    public void resetGroupsLegendLayout() {
        groupsLegendLayout.reset();
    }

    private int getPositionOfSelectedTab() {
        return groupsDefinitionTabs.getTabPosition(groupsDefinitionTabs.getTab(groupsDefinitionTabs.getSelectedTab()));
    }

    private void addValueChangeListeners() {
        actionTypeLayout.getComponent().getActionTypeOptionGroup().addValueChangeListener(
                event -> actionTypeLayout.setRequired(event.getValue() == ActionType.TIMEFORCED));
        autoStartOptionGroupLayout.getComponent().getAutoStartOptionGroup().addValueChangeListener(
                event -> autoStartOptionGroupLayout.setRequired(event.getValue() == AutoStartOption.SCHEDULED));

        errorThresholdOptionGroup.addValueChangeListener(event -> {
            if (event.isUserOriginated()) {
                errorThreshold.clear();
            }
            errorThreshold.setMaxLength(ERROR_THRESHOLD_OPTIONS.PERCENT == event.getValue() ? 3 : 7);
        });

        targetFilterQueryCombo.addValueChangeListener(this::onTargetFilterChange);
        groupsDefinitionTabs.addSelectedTabChangeListener(event -> validateGroups());
        noOfGroups.addValueChangeListener(event -> updateTargetsPerGroup(event.getValue()));
        defineGroupsLayout.setValidationListener(this::displayValidationStatus);
    }

    private void onTargetFilterChange(final ValueChangeEvent<ProxyTargetFilterQuery> event) {
        // we do not want to call the value change listener while setting the
        // bean via binder.setBean()
        if (!event.isUserOriginated()) {
            return;
        }

        final String filterQueryString = event.getValue() != null ? event.getValue().getQuery() : null;
        if (StringUtils.isEmpty(filterQueryString)) {
            groupsLegendLayout.populateTotalTargets(null);
            defineGroupsLayout.setTargetFilter(null);
        } else {
            groupsLegendLayout.populateTotalTargets(getEntity().getTotalTargets());
            defineGroupsLayout.setTargetFilter(filterQueryString);
        }
        updateTargetsPerGroup(noOfGroups.getValue());
    }

    private void updateTargetsPerGroup(final String numberOfGroups) {
        if (isNumberOfGroups() && !StringUtils.isEmpty(numberOfGroups) && isNoOfGroupsValid()
                && atLeastOneTargetPresent()) {
            groupSizeLabel.setValue(getTargetPerGroupMessage(
                    String.valueOf(getGroupSizeByGroupNumber(Integer.parseInt(numberOfGroups)))));
            groupSizeLabel.setVisible(true);
            updateGroupsChart(Integer.parseInt(numberOfGroups));
        } else {
            groupSizeLabel.setVisible(false);
            if (isNumberOfGroups()) {
                updateGroupsChart(0);
            }
        }
    }

    private boolean isNoOfGroupsValid() {
        return !noOfGroupsFieldBinding.validate(false).isError();
    }

    private boolean atLeastOneTargetPresent() {
        return getEntity().getTotalTargets() != null && getEntity().getTotalTargets() > 0L;
    }

    private String getTargetPerGroupMessage(final String value) {
        return new StringBuilder(i18n.getMessage("label.target.per.group")).append(value).toString();
    }

    private void validateGroups() {
        if (isGroupsDefinition()) {
            final List<RolloutGroupCreate> savedRolloutGroups = defineGroupsLayout.getSavedRolloutGroups();
            if (!defineGroupsLayout.isValid() || savedRolloutGroups == null || savedRolloutGroups.isEmpty()) {
                noOfGroups.clear();
            } else {
                noOfGroups.setValue(String.valueOf(savedRolloutGroups.size()));
            }
            updateGroupsChart(defineGroupsLayout.getGroupsValidation());
        }
        if (isNumberOfGroups()) {
            if (isNoOfGroupsValid()) {
                updateGroupsChart(Integer.parseInt(noOfGroups.getValue()));
            } else {
                updateGroupsChart(0);
            }
        }
    }

    private void displayValidationStatus(final DefineGroupsLayout.ValidationStatus status) {
        if (status == DefineGroupsLayout.ValidationStatus.LOADING) {
            groupsLegendLayout.displayLoading();
        } else {
            validateGroups();
        }
    }

    private void updateGroupsChart(final RolloutGroupsValidation validation) {
        if (validation == null) {
            groupsPieChart.setChartState(Collections.emptyList(), 0L);
            return;
        }
        final List<Long> targetsPerGroup = validation.getTargetsPerGroup();
        if (validation.getTotalTargets() == 0L || targetsPerGroup.isEmpty()) {
            groupsPieChart.setChartState(Collections.emptyList(), 0L);
        } else {
            groupsPieChart.setChartState(targetsPerGroup, validation.getTotalTargets());
        }

        getEntity().setTotalTargets(validation.getTotalTargets());
        groupsLegendLayout.populateTotalTargets(validation.getTotalTargets());
        groupsLegendLayout.populateGroupsLegendByValidation(validation, defineGroupsLayout.getSavedRolloutGroups());

    }

    private void updateGroupsChart(final int amountOfGroups) {
        if (getEntity().getTotalTargets() == null || getEntity().getTotalTargets() == 0L || amountOfGroups == 0) {
            groupsPieChart.setChartState(Collections.emptyList(), 0L);
            groupsLegendLayout.populateGroupsLegendByTargetCounts(Collections.emptyList());
        } else {
            final List<Long> targetsPerGroup = new ArrayList<>(amountOfGroups);
            long leftTargets = getEntity().getTotalTargets();
            for (int i = 0; i < amountOfGroups; i++) {
                final double percentage = 1.0 / (amountOfGroups - i);
                final long targetsInGroup = Math.round(percentage * leftTargets);
                leftTargets -= targetsInGroup;
                targetsPerGroup.add(targetsInGroup);
            }

            groupsPieChart.setChartState(targetsPerGroup, getEntity().getTotalTargets());
            groupsLegendLayout.populateGroupsLegendByTargetCounts(targetsPerGroup);
        }
    }
}
