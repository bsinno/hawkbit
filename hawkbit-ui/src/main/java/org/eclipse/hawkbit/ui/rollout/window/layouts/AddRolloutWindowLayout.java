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

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.builder.RolloutGroupCreate;
import org.eclipse.hawkbit.repository.model.RolloutGroupsValidation;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.rollout.groupschart.GroupsPieChart;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowLayoutComponentBuilder;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowLayoutComponentBuilder.ERROR_THRESHOLD_OPTIONS;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;

import com.google.common.base.Strings;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.ui.ComboBox;
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
    private static final long serialVersionUID = 1L;

    private final VaadinMessageSource i18n;
    private final transient TargetManagement targetManagement;

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
        this.targetManagement = dependencies.getTargetManagement();

        this.targetFilterQueryCombo = componentBuilder.createTargetFilterQueryCombo(proxyRolloutBinder);
        final Entry<TextField, Binding<ProxyRolloutWindow, Integer>> noOfGroupsWithBinding = componentBuilder
                .createNoOfGroupsField(proxyRolloutBinder);
        this.noOfGroups = noOfGroupsWithBinding.getKey();
        this.noOfGroupsFieldBinding = noOfGroupsWithBinding.getValue();
        this.groupSizeLabel = componentBuilder.createCountLabel();
        this.errorThreshold = componentBuilder.createErrorThreshold(proxyRolloutBinder);
        this.errorThresholdOptionGroup = componentBuilder.createErrorThresholdOptionGroup(proxyRolloutBinder);
        this.defineGroupsLayout = componentBuilder.createAdvancedGroupDefinitionTab();
        this.groupsDefinitionTabs = componentBuilder
                .createGroupDefinitionTabs(componentBuilder.createSimpleGroupDefinitionTab(noOfGroups, groupSizeLabel,
                        errorThreshold, errorThresholdOptionGroup, proxyRolloutBinder), defineGroupsLayout);
        this.groupsPieChart = componentBuilder.createGroupsPieChart();
        this.groupsLegendLayout = componentBuilder.createGroupsLegendLayout();

        buildLayout(componentBuilder);
        addValueChangeListeners();
    }

    public void buildLayout(final RolloutWindowLayoutComponentBuilder builder) {
        setRows(7);

        addComponent(builder.getLabel(RolloutWindowLayoutComponentBuilder.TEXTFIELD_NAME), 0, 0);
        final TextField rolloutName = builder.createRolloutNameField(proxyRolloutBinder);
        addComponent(rolloutName, 1, 0);
        rolloutName.focus();

        addComponent(builder.getLabel(RolloutWindowLayoutComponentBuilder.PROMPT_DISTRIBUTION_SET), 0, 1);
        addComponent(builder.createDistributionSetCombo(proxyRolloutBinder), 1, 1);

        addComponent(builder.getLabel(RolloutWindowLayoutComponentBuilder.PROMPT_TARGET_FILTER), 0, 2);
        addComponent(targetFilterQueryCombo, 1, 2);

        addComponent(builder.getLabel(RolloutWindowLayoutComponentBuilder.TEXTFIELD_DESCRIPTION), 0, 3);
        addComponent(builder.createDescription(proxyRolloutBinder), 1, 3, 1, 3);

        addComponent(groupsLegendLayout, 3, 0, 3, 3);

        addComponent(groupsPieChart, 2, 0, 2, 3);

        addComponent(builder.getLabel(RolloutWindowLayoutComponentBuilder.CAPTION_ROLLOUT_ACTION_TYPE), 0, 4);
        addComponent(builder.createActionTypeOptionGroupLayout(proxyRolloutBinder), 1, 4, 3, 4);

        addComponent(builder.getLabel(RolloutWindowLayoutComponentBuilder.CAPTION_ROLLOUT_START_TYPE), 0, 5);
        addComponent(builder.createAutoStartOptionGroupLayout(proxyRolloutBinder), 1, 5, 3, 5);

        addComponent(groupsDefinitionTabs, 0, 6, 3, 6);
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
        defineGroupsLayout.setTargetFilter(proxyRolloutBinder.getBean().getTargetFilterQuery());
        defineGroupsLayout.populateByRolloutId(proxyRolloutBinder.getBean().getId());
    }

    public void selectAdvancedRolloutGroupsTab() {
        groupsDefinitionTabs.setSelectedTab(1);
    }

    public void populateTotalTargetsLegend() {
        groupsLegendLayout.populateTotalTargets(getTotalTargets());
    }

    public void resetGroupsLegendLayout() {
        groupsLegendLayout.reset();
    }

    private int getPositionOfSelectedTab() {
        return groupsDefinitionTabs.getTabPosition(groupsDefinitionTabs.getTab(groupsDefinitionTabs.getSelectedTab()));
    }

    private void addValueChangeListeners() {
        errorThresholdOptionGroup.addValueChangeListener(event -> errorThreshold.clear());
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
            proxyRolloutBinder.getBean().setTotalTargets(0L);
            proxyRolloutBinder.getBean().setTargetFilterQuery(null);
            groupsLegendLayout.populateTotalTargets(null);
            defineGroupsLayout.setTargetFilter(null);
        } else {
            proxyRolloutBinder.getBean().setTotalTargets(targetManagement.countByRsql(filterQueryString));
            proxyRolloutBinder.getBean().setTargetFilterQuery(filterQueryString);
            groupsLegendLayout.populateTotalTargets(getTotalTargets());
            defineGroupsLayout.setTargetFilter(filterQueryString);
        }
        updateTargetsPerGroup(noOfGroups.getValue());
    }

    private void updateTargetsPerGroup(final String numberOfGroups) {
        if (!Strings.isNullOrEmpty(numberOfGroups) && isNoOfGroupsValid() && getTotalTargets() != null
                && getTotalTargets() > 0L && isNumberOfGroups()) {
            groupSizeLabel
                    .setValue(getTargetPerGroupMessage(String.valueOf(getGroupSize(Integer.parseInt(numberOfGroups)))));
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

    private String getTargetPerGroupMessage(final String value) {
        return new StringBuilder(i18n.getMessage("label.target.per.group")).append(value).toString();
    }

    private int getGroupSize(final Integer numberOfGroups) {
        return (int) Math.ceil((double) getTotalTargets() / (double) numberOfGroups);
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

        proxyRolloutBinder.getBean().setTotalTargets(validation.getTotalTargets());
        groupsLegendLayout.populateTotalTargets(validation.getTotalTargets());
        groupsLegendLayout.populateGroupsLegendByValidation(validation, defineGroupsLayout.getSavedRolloutGroups());

    }

    private void updateGroupsChart(final int amountOfGroups) {
        if (getTotalTargets() == null || getTotalTargets() == 0L || amountOfGroups == 0) {
            groupsPieChart.setChartState(Collections.emptyList(), 0L);
            groupsLegendLayout.populateGroupsLegendByTargetCounts(Collections.emptyList());
        } else {
            final List<Long> targetsPerGroup = new ArrayList<>(amountOfGroups);
            long leftTargets = getTotalTargets();
            for (int i = 0; i < amountOfGroups; i++) {
                final float percentage = 1.0F / (amountOfGroups - i);
                final long targetsInGroup = Math.round(percentage * (double) leftTargets);
                leftTargets -= targetsInGroup;
                targetsPerGroup.add(targetsInGroup);
            }

            groupsPieChart.setChartState(targetsPerGroup, getTotalTargets());
            groupsLegendLayout.populateGroupsLegendByTargetCounts(targetsPerGroup);
        }
    }
}
