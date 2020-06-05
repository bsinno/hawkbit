/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import java.util.function.IntConsumer;

import org.eclipse.hawkbit.repository.QuotaManagement;
import org.eclipse.hawkbit.ui.common.builder.BoundComponent;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySimpleRolloutGroupsDefinition;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

public class SimpleGroupsLayout extends GridLayout {
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_ENTER_NUMBER = "message.enter.number";
    private static final String MESSAGE_ROLLOUT_MAX_GROUP_SIZE_EXCEEDED = "message.rollout.max.group.size.exceeded";
    private static final String MESSAGE_ROLLOUT_FIELD_VALUE_RANGE = "message.rollout.field.value.range";

    private final VaadinMessageSource i18n;
    private final transient QuotaManagement quotaManagement;

    private final Binder<ProxySimpleRolloutGroupsDefinition> binder;

    private final BoundComponent<TextField> noOfGroupsWithBinding;
    private final Label groupSizeLabel;
    private final TextField triggerThreshold;
    private final Label percentHintLabel;
    private final BoundComponent<TextField> errorThreshold;
    private final RadioButtonGroup<ERROR_THRESHOLD_OPTIONS> errorThresholdOptionGroup;

    private Long totalTargets;

    private transient IntConsumer noOfGroupsChangedListener;

    public SimpleGroupsLayout(final VaadinMessageSource i18n, final QuotaManagement quotaManagement) {
        this.i18n = i18n;
        this.quotaManagement = quotaManagement;

        this.binder = new Binder<>();

        this.noOfGroupsWithBinding = createNoOfGroupsField();
        this.groupSizeLabel = createCountLabel();
        this.triggerThreshold = createTriggerThreshold();
        this.percentHintLabel = getPercentHintLabel();
        this.errorThreshold = createErrorThreshold();
        this.errorThresholdOptionGroup = createErrorThresholdOptionGroup();

        buildLayout();
        addValueChangeListeners();
    }

    private BoundComponent<TextField> createNoOfGroupsField() {
        final TextField noOfGroups = new TextFieldBuilder(3).id(UIComponentIdProvider.ROLLOUT_NO_OF_GROUPS_ID)
                .prompt(i18n.getMessage("prompt.number.of.groups")).buildTextComponent();
        noOfGroups.setSizeUndefined();

        final Binding<ProxySimpleRolloutGroupsDefinition, Integer> noOfGroupsFieldBinding = binder.forField(noOfGroups)
                // TODO: use i18n
                .asRequired("You must specify at least one group").withNullRepresentation("")
                .withConverter(new StringToIntegerConverter(i18n.getMessage(MESSAGE_ENTER_NUMBER)))
                .withValidator((number, context) -> {
                    final int maxGroups = quotaManagement.getMaxRolloutGroupsPerRollout();
                    return new IntegerRangeValidator(i18n.getMessage(MESSAGE_ROLLOUT_FIELD_VALUE_RANGE, 1, maxGroups),
                            1, maxGroups).apply(number, context);
                }).withValidator((number, context) -> {
                    final int maxTargetsPerGroupSize = quotaManagement.getMaxTargetsPerRolloutGroup();
                    if (getTargetPerGroupByGroupCount(number) > maxTargetsPerGroupSize) {
                        final String msg = i18n.getMessage(MESSAGE_ROLLOUT_MAX_GROUP_SIZE_EXCEEDED,
                                maxTargetsPerGroupSize);
                        return ValidationResult.error(msg);
                    } else {
                        return ValidationResult.ok();
                    }
                }).bind(ProxySimpleRolloutGroupsDefinition::getNumberOfGroups,
                        ProxySimpleRolloutGroupsDefinition::setNumberOfGroups);

        return new BoundComponent<>(noOfGroups, noOfGroupsFieldBinding);
    }

    private int getTargetPerGroupByGroupCount(final Integer numberOfGroups) {
        if (totalTargets == null || numberOfGroups == null || totalTargets <= 0L || numberOfGroups <= 0L) {
            return 0;
        }

        return (int) Math.ceil((double) totalTargets / (double) numberOfGroups);
    }

    private Label createCountLabel() {
        final Label countLabel = new LabelBuilder().visible(false).name("").buildLabel();
        countLabel.addStyleName(ValoTheme.LABEL_TINY + " " + "rollout-target-count-message");
        countLabel.setSizeUndefined();

        return countLabel;
    }

    private TextField createTriggerThreshold() {
        final TextField triggerThresholdField = new TextFieldBuilder(3)
                .id(UIComponentIdProvider.ROLLOUT_TRIGGER_THRESOLD_ID)
                .prompt(i18n.getMessage("prompt.tigger.threshold")).buildTextComponent();
        triggerThresholdField.setSizeUndefined();

        // TODO: use i18n
        binder.forField(triggerThresholdField).asRequired("Trigger threshold can not be empty")
                .withValidator((triggerThresholdText,
                        context) -> new IntegerRangeValidator(
                                i18n.getMessage(MESSAGE_ROLLOUT_FIELD_VALUE_RANGE, 0, 100), 0, 100)
                                        .apply(Integer.valueOf(triggerThresholdText), context))
                .bind(ProxySimpleRolloutGroupsDefinition::getTriggerThresholdPercentage,
                        ProxySimpleRolloutGroupsDefinition::setTriggerThresholdPercentage);

        return triggerThresholdField;
    }

    private Label getPercentHintLabel() {
        final Label percentSymbol = new Label("%");
        percentSymbol.addStyleName(ValoTheme.LABEL_TINY + " " + ValoTheme.LABEL_BOLD);
        percentSymbol.setSizeUndefined();

        return percentSymbol;
    }

    private BoundComponent<TextField> createErrorThreshold() {
        final TextField errorThresholdField = new TextFieldBuilder(3)
                .id(UIComponentIdProvider.ROLLOUT_ERROR_THRESOLD_ID).prompt(i18n.getMessage("prompt.error.threshold"))
                .buildTextComponent();
        errorThresholdField.setSizeUndefined();

        // TODO: use i18n
        final Binding<ProxySimpleRolloutGroupsDefinition, String> binding = binder.forField(errorThresholdField)
                .asRequired("Error threshold can not be empty").withValidator((errorThresholdText, context) -> {
                    if (ERROR_THRESHOLD_OPTIONS.PERCENT == errorThresholdOptionGroup.getValue()) {
                        return new IntegerRangeValidator(i18n.getMessage(MESSAGE_ROLLOUT_FIELD_VALUE_RANGE, 0, 100), 0,
                                100).apply(Integer.valueOf(errorThresholdText), context);
                    }

                    final int groupSize = getTargetsPerGroup();

                    if (groupSize == 0) {
                        final String msg = i18n.getMessage("message.rollout.noofgroups.or.targetfilter.missing");

                        return ValidationResult.error(msg);
                    } else {
                        return new IntegerRangeValidator(
                                i18n.getMessage(MESSAGE_ROLLOUT_FIELD_VALUE_RANGE, 0, groupSize), 0, groupSize)
                                        .apply(Integer.valueOf(errorThresholdText), context);
                    }
                }).withConverter(errorThresholdPresentation -> {
                    if (errorThresholdPresentation == null) {
                        return null;
                    }

                    if (ERROR_THRESHOLD_OPTIONS.COUNT == errorThresholdOptionGroup.getValue()) {
                        final int errorThresholdCount = Integer.parseInt(errorThresholdPresentation);
                        final int groupSize = getTargetsPerGroup();

                        return String
                                .valueOf((int) Math.ceil(((double) errorThresholdCount / (double) groupSize) * 100));
                    }

                    return errorThresholdPresentation;
                }, errorThresholdModel -> {
                    if (errorThresholdModel == null) {
                        return null;
                    }

                    return errorThresholdModel;
                }).bind(ProxySimpleRolloutGroupsDefinition::getErrorThresholdPercentage,
                        ProxySimpleRolloutGroupsDefinition::setErrorThresholdPercentage);

        return new BoundComponent<>(errorThresholdField, binding);
    }

    private int getTargetsPerGroup() {
        try {
            return getTargetPerGroupByGroupCount(parseNoOfGroups(noOfGroupsWithBinding.getComponent().getValue()));
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    private static int parseNoOfGroups(final String noOfGroups) {
        try {
            return Integer.parseInt(noOfGroups);
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    private RadioButtonGroup<ERROR_THRESHOLD_OPTIONS> createErrorThresholdOptionGroup() {
        final RadioButtonGroup<ERROR_THRESHOLD_OPTIONS> errorThresholdOptions = new RadioButtonGroup<>();
        errorThresholdOptions.setId(UIComponentIdProvider.ROLLOUT_ERROR_THRESOLD_OPTION_ID);
        errorThresholdOptions.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        errorThresholdOptions.addStyleName(SPUIStyleDefinitions.ROLLOUT_OPTION_GROUP);
        errorThresholdOptions.setSizeUndefined();

        errorThresholdOptions.setItems(ERROR_THRESHOLD_OPTIONS.values());
        errorThresholdOptions.setValue(ERROR_THRESHOLD_OPTIONS.PERCENT);

        errorThresholdOptions.setItemCaptionGenerator(errorThresholdOption -> errorThresholdOption.getValue(i18n));

        return errorThresholdOptions;
    }

    private void buildLayout() {
        setMargin(false);
        setSpacing(true);
        setSizeUndefined();
        setRows(4);
        setColumns(3);
        setStyleName("marginTop");

        addComponent(SPUIComponentProvider.getLabelByMsgKey(i18n, "caption.rollout.generate.groups"), 0, 0, 2, 0);

        addComponent(SPUIComponentProvider.getLabelByMsgKey(i18n, "prompt.number.of.groups"), 0, 1);
        addComponent(noOfGroupsWithBinding.getComponent(), 1, 1);
        addComponent(groupSizeLabel, 2, 1);

        addComponent(SPUIComponentProvider.getLabelByMsgKey(i18n, "prompt.tigger.threshold"), 0, 2);
        addComponent(triggerThreshold, 1, 2);
        addComponent(percentHintLabel, 2, 2);

        addComponent(SPUIComponentProvider.getLabelByMsgKey(i18n, "prompt.error.threshold"), 0, 3);
        addComponent(errorThreshold.getComponent(), 1, 3);
        addComponent(errorThresholdOptionGroup, 2, 3);
    }

    private void addValueChangeListeners() {
        noOfGroupsWithBinding.getComponent().addValueChangeListener(event -> {
            errorThreshold.validate();

            updateGroupSizeLabel();

            if (noOfGroupsChangedListener != null) {
                noOfGroupsChangedListener.accept(parseNoOfGroups(event.getValue()));
            }
        });

        errorThresholdOptionGroup.addValueChangeListener(event -> {
            if (event.isUserOriginated()) {
                errorThreshold.getComponent().clear();
            }

            errorThreshold.getComponent().setMaxLength(ERROR_THRESHOLD_OPTIONS.PERCENT == event.getValue() ? 3 : 7);
        });
    }

    public void setNoOfGroupsChangedListener(final IntConsumer noOfGroupsChangedListener) {
        this.noOfGroupsChangedListener = noOfGroupsChangedListener;
    }

    public void setTotalTargets(final Long totalTargets) {
        this.totalTargets = totalTargets;

        noOfGroupsWithBinding.validate();
        errorThreshold.validate();

        updateGroupSizeLabel();
    }

    private void updateGroupSizeLabel() {
        if (HawkbitCommonUtil.atLeastOnePresent(totalTargets)) {
            groupSizeLabel.setValue(getTargetPerGroupMessage(getTargetsPerGroup()));
            groupSizeLabel.setVisible(true);
        } else {
            groupSizeLabel.setVisible(false);
        }
    }

    private String getTargetPerGroupMessage(final int targetsPerGroup) {
        return new StringBuilder(i18n.getMessage("label.target.per.group")).append(targetsPerGroup).toString();
    }

    public void setBean(final ProxySimpleRolloutGroupsDefinition bean) {
        binder.setBean(bean);
    }

    public ProxySimpleRolloutGroupsDefinition getBean() {
        return binder.getBean();
    }

    public enum ERROR_THRESHOLD_OPTIONS {
        PERCENT("label.errorthreshold.option.percent"), COUNT("label.errorthreshold.option.count");

        private final String value;

        ERROR_THRESHOLD_OPTIONS(final String value) {
            this.value = value;
        }

        private String getValue(final VaadinMessageSource i18n) {
            return i18n.getMessage(value);
        }
    }
}
