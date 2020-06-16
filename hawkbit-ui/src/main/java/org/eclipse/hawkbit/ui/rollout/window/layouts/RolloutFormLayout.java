/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import java.util.function.Consumer;

import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.ui.common.builder.BoundComponent;
import org.eclipse.hawkbit.ui.common.builder.FormComponentBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextAreaBuilder;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutForm;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.management.miscs.ActionTypeOptionGroupAssignmentLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AutoStartOptionGroupLayout.AutoStartOption;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.LongRangeValidator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

public class RolloutFormLayout {

    private static final String PROMPT_TARGET_FILTER = "prompt.target.filter";
    private static final String MESSAGE_ROLLOUT_FILTER_TARGET_EXISTS = "message.rollout.filter.target.exists";
    private static final String TEXTFIELD_DESCRIPTION = "textfield.description";
    private static final String PROMPT_DISTRIBUTION_SET = "prompt.distribution.set";
    private static final String TEXTFIELD_NAME = "textfield.name";
    private static final String CAPTION_ROLLOUT_START_TYPE = "caption.rollout.start.type";
    private static final String CAPTION_ROLLOUT_ACTION_TYPE = "caption.rollout.action.type";

    private static final int CAPTION_COLUMN = 0;
    private static final int FIELD_COLUMN = 1;

    private final VaadinMessageSource i18n;

    private final DistributionSetStatelessDataProvider distributionSetDataProvider;
    private final TargetFilterQueryDataProvider targetFilterQueryDataProvider;

    private final Binder<ProxyRolloutForm> binder;

    private final TextField nameField;
    private final ComboBox<ProxyDistributionSet> dsCombo;
    private final BoundComponent<ComboBox<ProxyTargetFilterQuery>> targetFilterQueryCombo;
    private final TextArea targetFilterQueryField;
    private final TextArea descriptionField;
    private final BoundComponent<ActionTypeOptionGroupAssignmentLayout> actionTypeLayout;
    private final BoundComponent<AutoStartOptionGroupLayout> autoStartOptionGroupLayout;

    private Long totalTargets;

    private Consumer<String> filterQueryChangedListener;

    public RolloutFormLayout(final VaadinMessageSource i18n,
            final DistributionSetStatelessDataProvider distributionSetDataProvider,
            final TargetFilterQueryDataProvider targetFilterQueryDataProvider) {
        this.i18n = i18n;
        this.distributionSetDataProvider = distributionSetDataProvider;
        this.targetFilterQueryDataProvider = targetFilterQueryDataProvider;

        this.binder = new Binder<>();

        this.nameField = createRolloutNameField();
        this.dsCombo = createDistributionSetCombo();
        this.targetFilterQueryCombo = createTargetFilterQueryCombo();
        this.targetFilterQueryField = createTargetFilterQuery();
        this.descriptionField = createDescription();
        this.actionTypeLayout = createActionTypeOptionGroupLayout();
        this.autoStartOptionGroupLayout = createAutoStartOptionGroupLayout();

        addValueChangeListeners();
    }

    /**
     * Create name field.
     * 
     * @return input component
     */
    private TextField createRolloutNameField() {
        final TextField textField = FormComponentBuilder
                .createNameInput(binder, i18n, UIComponentIdProvider.ROLLOUT_NAME_FIELD_ID).getComponent();
        textField.setCaption(null);
        return textField;
    }

    /**
     * Create required Distribution Set ComboBox.
     * 
     * @return ComboBox
     */
    private ComboBox<ProxyDistributionSet> createDistributionSetCombo() {
        final ComboBox<ProxyDistributionSet> dsComboBox = FormComponentBuilder.createDistributionSetComboBox(binder,
                distributionSetDataProvider, i18n, UIComponentIdProvider.ROLLOUT_DS_ID).getComponent();
        dsComboBox.setCaption(null);

        return dsComboBox;
    }

    private BoundComponent<ComboBox<ProxyTargetFilterQuery>> createTargetFilterQueryCombo() {
        return FormComponentBuilder.createTargetFilterQueryCombo(binder, atLeastOneTargetPresentValidator(),
                targetFilterQueryDataProvider, i18n, UIComponentIdProvider.ROLLOUT_TARGET_FILTER_COMBO_ID);

    }

    private Validator<ProxyTargetFilterQuery> atLeastOneTargetPresentValidator() {
        return (filterQuery,
                context) -> new LongRangeValidator(i18n.getMessage(MESSAGE_ROLLOUT_FILTER_TARGET_EXISTS), 1L, null)
                        .apply(totalTargets, context);
    }

    private TextArea createTargetFilterQuery() {
        final TextArea targetFilterQuery = new TextAreaBuilder(TargetFilterQuery.QUERY_MAX_SIZE)
                .style("text-area-style").id(UIComponentIdProvider.ROLLOUT_TARGET_FILTER_QUERY_FIELD)
                .buildTextComponent();
        targetFilterQuery.setSizeUndefined();

        binder.forField(targetFilterQuery).bind(ProxyRolloutForm::getTargetFilterQuery,
                ProxyRolloutForm::setTargetFilterQuery);

        return targetFilterQuery;
    }

    /**
     * Create description field.
     * 
     * @return input component
     */
    private TextArea createDescription() {
        final TextArea description = FormComponentBuilder
                .createDescriptionInput(binder, i18n, UIComponentIdProvider.ROLLOUT_DESCRIPTION_ID).getComponent();
        description.setCaption(null);

        return description;
    }

    /**
     * Create bound {@link ActionTypeOptionGroupAssignmentLayout}.
     * 
     * @return input component
     */
    private BoundComponent<ActionTypeOptionGroupAssignmentLayout> createActionTypeOptionGroupLayout() {
        final BoundComponent<ActionTypeOptionGroupAssignmentLayout> actionTypeGroupBounded = FormComponentBuilder
                .createActionTypeOptionGroupLayout(binder, i18n, UIComponentIdProvider.ROLLOUT_ACTION_TYPE_OPTIONS_ID);
        actionTypeGroupBounded.setRequired(false);

        return actionTypeGroupBounded;
    }

    private BoundComponent<AutoStartOptionGroupLayout> createAutoStartOptionGroupLayout() {
        final BoundComponent<AutoStartOptionGroupLayout> autoStartOptionGroup = FormComponentBuilder
                .createAutoStartOptionGroupLayout(binder, i18n, UIComponentIdProvider.ROLLOUT_START_OPTIONS_ID);
        autoStartOptionGroup.setRequired(false);

        return autoStartOptionGroup;
    }

    private void addValueChangeListeners() {
        targetFilterQueryCombo.getComponent().addValueChangeListener(event -> {
            if (filterQueryChangedListener != null) {
                filterQueryChangedListener.accept(event.getValue() != null ? event.getValue().getQuery() : null);
            }
        });

        actionTypeLayout.getComponent().getActionTypeOptionGroup().addValueChangeListener(
                event -> actionTypeLayout.setRequired(event.getValue() == ActionType.TIMEFORCED));
        autoStartOptionGroupLayout.getComponent().getAutoStartOptionGroup().addValueChangeListener(
                event -> autoStartOptionGroupLayout.setRequired(event.getValue() == AutoStartOption.SCHEDULED));
    }

    public void addFormToLayout(final GridLayout layout, final boolean isEditMode) {
        layout.addComponent(SPUIComponentProvider.generateLabel(i18n, TEXTFIELD_NAME), CAPTION_COLUMN, 0);
        layout.addComponent(nameField, FIELD_COLUMN, 0);
        nameField.focus();

        layout.addComponent(SPUIComponentProvider.generateLabel(i18n, PROMPT_DISTRIBUTION_SET), CAPTION_COLUMN, 1);
        layout.addComponent(dsCombo, FIELD_COLUMN, 1);

        layout.addComponent(SPUIComponentProvider.generateLabel(i18n, PROMPT_TARGET_FILTER), CAPTION_COLUMN, 2);
        layout.addComponent(isEditMode ? targetFilterQueryField : targetFilterQueryCombo.getComponent(), FIELD_COLUMN,
                2);

        layout.addComponent(SPUIComponentProvider.generateLabel(i18n, TEXTFIELD_DESCRIPTION), CAPTION_COLUMN, 3);
        layout.addComponent(descriptionField, FIELD_COLUMN, 3);

        final int lastColumn = layout.getColumns() - 1;
        layout.addComponent(SPUIComponentProvider.generateLabel(i18n, CAPTION_ROLLOUT_ACTION_TYPE), CAPTION_COLUMN, 4);
        layout.addComponent(actionTypeLayout.getComponent(), FIELD_COLUMN, 4, lastColumn, 4);

        layout.addComponent(SPUIComponentProvider.generateLabel(i18n, CAPTION_ROLLOUT_START_TYPE), CAPTION_COLUMN, 5);
        layout.addComponent(autoStartOptionGroupLayout.getComponent(), FIELD_COLUMN, 5, lastColumn, 5);
    }

    public void disableFieldsOnEditForInActive() {
        targetFilterQueryField.setEnabled(false);
    }

    public void disableFieldsOnEditForActive() {
        dsCombo.setEnabled(false);
        targetFilterQueryField.setEnabled(false);
        actionTypeLayout.getComponent().setEnabled(false);
        autoStartOptionGroupLayout.getComponent().setEnabled(false);
    }

    public void setFilterQueryChangedListener(final Consumer<String> filterQueryChangedListener) {
        this.filterQueryChangedListener = filterQueryChangedListener;
    }

    public void setTotalTargets(final Long totalTargets) {
        this.totalTargets = totalTargets;

        targetFilterQueryCombo.validate();
    }

    public void setBean(final ProxyRolloutForm bean) {
        binder.setBean(bean);
    }

    public ProxyRolloutForm getBean() {
        return binder.getBean();
    }
}