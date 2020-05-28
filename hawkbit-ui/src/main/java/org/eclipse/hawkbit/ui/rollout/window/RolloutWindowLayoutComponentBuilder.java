/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window;

import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditionBuilder;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditions;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetFilterQueryToProxyTargetFilterMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.rollout.groupschart.GroupsPieChart;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AdvancedGroupsLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.GroupsLegendLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.RolloutFormLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.SimpleGroupsLayout;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;

import com.vaadin.data.Binder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Builder for Rollout window components.
 */
public final class RolloutWindowLayoutComponentBuilder {

    private static final String APPROVAL_BUTTON_LABEL = "button.approve";
    private static final String DENY_BUTTON_LABEL = "button.deny";

    private final RolloutWindowDependencies dependencies;

    private static final RolloutGroupConditions defaultRolloutGroupConditions;

    private final DistributionSetStatelessDataProvider distributionSetDataProvider;
    private final TargetFilterQueryDataProvider targetFilterQueryDataProvider;

    static {
        defaultRolloutGroupConditions = new RolloutGroupConditionBuilder().withDefaults().build();
    }

    public RolloutWindowLayoutComponentBuilder(final RolloutWindowDependencies rolloutWindowDependecies) {
        this.dependencies = rolloutWindowDependecies;

        this.distributionSetDataProvider = new DistributionSetStatelessDataProvider(
                dependencies.getDistributionSetManagement(), new DistributionSetToProxyDistributionMapper());
        this.targetFilterQueryDataProvider = new TargetFilterQueryDataProvider(
                dependencies.getTargetFilterQueryManagement(), new TargetFilterQueryToProxyTargetFilterMapper());
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

    public AdvancedGroupsLayout createAdvancedGroupDefinitionTab() {
        final AdvancedGroupsLayout defineGroupsLayout = new AdvancedGroupsLayout(dependencies.getI18n(),
                dependencies.getEntityFactory(), dependencies.getRolloutManagement(),
                dependencies.getTargetFilterQueryManagement(), dependencies.getRolloutGroupManagement(),
                dependencies.getQuotaManagement(), targetFilterQueryDataProvider);
        defineGroupsLayout.setDefaultErrorThreshold(defaultRolloutGroupConditions.getErrorConditionExp());
        defineGroupsLayout.setDefaultTriggerThreshold(defaultRolloutGroupConditions.getSuccessConditionExp());

        return defineGroupsLayout;
    }

    public HorizontalLayout createApprovalLayout(final Binder<ProxyRolloutWindow> binder) {
        final RadioButtonGroup<Rollout.ApprovalDecision> approveButtonsGroup = new RadioButtonGroup<>();
        approveButtonsGroup.setId(UIComponentIdProvider.ROLLOUT_APPROVAL_OPTIONGROUP_ID);
        approveButtonsGroup.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
        approveButtonsGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        approveButtonsGroup.addStyleName("custom-option-group");
        approveButtonsGroup.setItems(Rollout.ApprovalDecision.values());

        approveButtonsGroup.setItemCaptionGenerator(item -> {
            if (Rollout.ApprovalDecision.APPROVED == item) {
                return dependencies.getI18n().getMessage(APPROVAL_BUTTON_LABEL);
            } else {
                return dependencies.getI18n().getMessage(DENY_BUTTON_LABEL);
            }
        });
        approveButtonsGroup.setItemIconGenerator(item -> {
            if (Rollout.ApprovalDecision.APPROVED == item) {
                return VaadinIcons.CHECK;
            } else {
                return VaadinIcons.CLOSE;
            }
        });

        binder.forField(approveButtonsGroup).bind(ProxyRolloutWindow::getApprovalDecision,
                ProxyRolloutWindow::setApprovalDecision);

        final TextField approvalRemarkField = new TextFieldBuilder(Rollout.APPROVAL_REMARK_MAX_SIZE)
                .id(UIComponentIdProvider.ROLLOUT_APPROVAL_REMARK_FIELD_ID)
                .prompt(dependencies.getI18n().getMessage("label.approval.remark")).buildTextComponent();
        approvalRemarkField.setWidthFull();

        binder.forField(approvalRemarkField).bind(ProxyRolloutWindow::getApprovalRemark,
                ProxyRolloutWindow::setApprovalRemark);

        final HorizontalLayout approvalButtonsLayout = new HorizontalLayout(approveButtonsGroup, approvalRemarkField);
        approvalButtonsLayout.setWidthFull();
        approvalButtonsLayout.setExpandRatio(approvalRemarkField, 1.0F);

        return approvalButtonsLayout;
    }

    public GroupsPieChart createGroupsPieChart() {
        final GroupsPieChart groupsPieChart = new GroupsPieChart();
        groupsPieChart.setWidth(260, Unit.PIXELS);
        groupsPieChart.setHeight(220, Unit.PIXELS);
        groupsPieChart.setStyleName(SPUIStyleDefinitions.ROLLOUT_GROUPS_CHART);

        return groupsPieChart;
    }

    public GroupsLegendLayout createGroupsLegendLayout() {
        return new GroupsLegendLayout(dependencies.getI18n());
    }

    public static RolloutGroupConditions getDefaultRolloutGroupConditions() {
        return defaultRolloutGroupConditions;
    }
}
