/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.builder.RolloutGroupCreate;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupErrorAction;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupErrorCondition;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupSuccessAction;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupSuccessCondition;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditionBuilder;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditions;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.ui.common.builder.BoundComponent;
import org.eclipse.hawkbit.ui.common.builder.FormComponentBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAdvancedRolloutGroupRow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

import com.vaadin.data.Binder;
import com.vaadin.data.StatusChangeListener;
import com.vaadin.data.converter.StringToFloatConverter;
import com.vaadin.data.validator.FloatRangeValidator;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.UserError;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

public class AdvancedGroupRow {

    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final TargetFilterQueryManagement targetFilterQueryManagement;
    private final TargetFilterQueryDataProvider targetFilterQueryDataProvider;

    private final int groupCount;

    private final Binder<ProxyAdvancedRolloutGroupRow> binder;

    private final TextField groupName;
    private final ComboBox<ProxyTargetFilterQuery> targetFilterQueryCombo;
    private final TextField targetPercentage;
    private final TextField triggerThreshold;
    private final TextField errorThreshold;

    public AdvancedGroupRow(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final TargetFilterQueryManagement targetFilterQueryManagement,
            final TargetFilterQueryDataProvider targetFilterQueryDataProvider, final int groupCount) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.targetFilterQueryManagement = targetFilterQueryManagement;
        this.targetFilterQueryDataProvider = targetFilterQueryDataProvider;

        this.groupCount = groupCount;

        this.binder = new Binder<>();

        this.groupName = createGroupName();
        this.targetFilterQueryCombo = createTargetFilterQueryCombo();
        this.targetPercentage = createTargetPercentage();
        this.triggerThreshold = createTriggerThreshold();
        this.errorThreshold = createErrorThreshold();
    }

    private TextField createGroupName() {
        final TextField nameField = new TextFieldBuilder(RolloutGroup.NAME_MAX_SIZE)
                .prompt(i18n.getMessage("textfield.name")).id(UIComponentIdProvider.ROLLOUT_GROUP_LIST_GRID_ID)
                .buildTextComponent();
        nameField.setSizeUndefined();
        nameField.setStyleName("rollout-group-name");
        nameField.addStyleName(ValoTheme.TEXTAREA_SMALL);
        nameField.setWidth(12, Unit.EM);

        binder.forField(nameField).asRequired(i18n.getMessage(UIMessageIdProvider.MESSAGE_ERROR_NAMEREQUIRED))
                .bind(ProxyAdvancedRolloutGroupRow::getGroupName, ProxyAdvancedRolloutGroupRow::setGroupName);

        return nameField;
    }

    private ComboBox<ProxyTargetFilterQuery> createTargetFilterQueryCombo() {
        final BoundComponent<ComboBox<ProxyTargetFilterQuery>> boundTfqCombo = FormComponentBuilder
                .createTargetFilterQueryCombo(binder, null, targetFilterQueryDataProvider, i18n,
                        UIComponentIdProvider.ROLLOUT_TARGET_FILTER_COMBO_ID + "." + groupCount);
        boundTfqCombo.setRequired(false);

        return boundTfqCombo.getComponent();
    }

    private TextField createTargetPercentage() {
        final TextField targetPercentageField = new TextFieldBuilder(32)
                .prompt(i18n.getMessage("textfield.target.percentage"))
                .id(UIComponentIdProvider.ROLLOUT_GROUP_TARGET_PERC_ID).buildTextComponent();
        targetPercentageField.setWidth(5, Unit.EM);

        binder.forField(targetPercentageField).asRequired()
                // TODO: use i18n
                .withConverter(new StringToFloatConverter("only float values are allowed"))
                .withValidator((value, context) -> {
                    final FloatRangeValidator validator = new FloatRangeValidator(
                            i18n.getMessage("message.rollout.field.value.range", 0, 100), 0F, 100F);
                    validator.setMinValueIncluded(false);
                    return validator.apply(value, context);
                }).bind(ProxyAdvancedRolloutGroupRow::getTargetPercentage,
                        ProxyAdvancedRolloutGroupRow::setTargetPercentage);

        return targetPercentageField;
    }

    private TextField createTriggerThreshold() {
        final TextField triggerThresholdField = new TextFieldBuilder(32)
                .prompt(i18n.getMessage("prompt.tigger.threshold"))
                .id(UIComponentIdProvider.ROLLOUT_TRIGGER_THRESOLD_ID).buildTextComponent();
        triggerThresholdField.setWidth(5, Unit.EM);

        // TODO: add as required description
        binder.forField(triggerThresholdField).asRequired().bind(
                ProxyAdvancedRolloutGroupRow::getTriggerThresholdPercentage,
                ProxyAdvancedRolloutGroupRow::setTriggerThresholdPercentage);

        return triggerThresholdField;
    }

    private TextField createErrorThreshold() {
        final TextField errorThresholdField = new TextFieldBuilder(32).prompt(i18n.getMessage("prompt.error.threshold"))
                .id(UIComponentIdProvider.ROLLOUT_ERROR_THRESOLD_ID).buildTextComponent();
        errorThresholdField.setWidth(5, Unit.EM);

        // TODO: add as required description
        binder.forField(errorThresholdField).asRequired().bind(
                ProxyAdvancedRolloutGroupRow::getErrorThresholdPercentage,
                ProxyAdvancedRolloutGroupRow::setErrorThresholdPercentage);

        return errorThresholdField;
    }

    public void addRowToLayout(final GridLayout layout, final int index) {
        layout.addComponent(groupName, 0, index);
        layout.addComponent(targetFilterQueryCombo, 1, index);
        layout.addComponent(targetPercentage, 2, index);
        layout.addComponent(triggerThreshold, 3, index);
        layout.addComponent(errorThreshold, 4, index);
    }

    /**
     * Populates the row with the default data.
     * 
     */
    public void populateWithDefaults() {
        final ProxyAdvancedRolloutGroupRow advancedGroupRowBean = new ProxyAdvancedRolloutGroupRow();
        advancedGroupRowBean.setGroupName(i18n.getMessage("textfield.rollout.group.default.name", groupCount));
        advancedGroupRowBean.setTargetPercentage(100f);
        setDefaultThresholds(advancedGroupRowBean);

        binder.setBean(advancedGroupRowBean);
    }

    private void setDefaultThresholds(final ProxyAdvancedRolloutGroupRow advancedGroupRow) {
        final RolloutGroupConditions defaultRolloutGroupConditions = new RolloutGroupConditionBuilder().withDefaults()
                .build();
        advancedGroupRow.setTriggerThresholdPercentage(defaultRolloutGroupConditions.getSuccessConditionExp());
        advancedGroupRow.setErrorThresholdPercentage(defaultRolloutGroupConditions.getErrorConditionExp());
    }

    /**
     * Populates the row with the data from the provided groups.
     * 
     * @param group
     *            the data source
     */
    public void populateByGroup(final RolloutGroup group) {
        final ProxyAdvancedRolloutGroupRow advancedGroupRowBean = new ProxyAdvancedRolloutGroupRow();
        advancedGroupRowBean.setGroupName(group.getName());

        final String groupTargetFilterQuery = group.getTargetFilterQuery();
        if (!StringUtils.isEmpty(groupTargetFilterQuery)) {
            advancedGroupRowBean.setTargetFilterQuery(groupTargetFilterQuery);
            final Page<TargetFilterQuery> filterQueries = targetFilterQueryManagement.findByQuery(PageRequest.of(0, 1),
                    groupTargetFilterQuery);
            if (filterQueries.getTotalElements() == 1) {
                advancedGroupRowBean.setTargetFilterId(filterQueries.getContent().get(0).getId());
            }
        }

        advancedGroupRowBean.setTargetPercentage(group.getTargetPercentage());
        advancedGroupRowBean.setTriggerThresholdPercentage(group.getSuccessConditionExp());
        advancedGroupRowBean.setErrorThresholdPercentage(group.getErrorConditionExp());

        binder.setBean(advancedGroupRowBean);
    }

    /**
     * Builds a group definition from this group row
     * 
     * @return the RolloutGroupCreate definition
     */
    public RolloutGroupCreate getGroupEntity() {
        final ProxyAdvancedRolloutGroupRow advancedGroupRowBean = binder.getBean();

        final RolloutGroupConditions conditions = new RolloutGroupConditionBuilder()
                .successAction(RolloutGroupSuccessAction.NEXTGROUP, null)
                .successCondition(RolloutGroupSuccessCondition.THRESHOLD,
                        advancedGroupRowBean.getTriggerThresholdPercentage())
                .errorCondition(RolloutGroupErrorCondition.THRESHOLD,
                        advancedGroupRowBean.getErrorThresholdPercentage())
                .errorAction(RolloutGroupErrorAction.PAUSE, null).build();

        return entityFactory.rolloutGroup().create().name(advancedGroupRowBean.getGroupName())
                .description(advancedGroupRowBean.getGroupName())
                .targetFilterQuery(advancedGroupRowBean.getTargetFilterQuery())
                .targetPercentage(advancedGroupRowBean.getTargetPercentage()).conditions(conditions);
    }

    public void addStatusChangeListener(final StatusChangeListener listener) {
        binder.addStatusChangeListener(listener);
    }

    /**
     * @return whether the data entered in this row is valid
     */
    public boolean isValid() {
        return binder.isValid();
    }

    public void setError(final String error) {
        targetPercentage.setComponentError(new UserError(error));
    }

    public void resetError() {
        targetPercentage.setComponentError(null);
    }
}
