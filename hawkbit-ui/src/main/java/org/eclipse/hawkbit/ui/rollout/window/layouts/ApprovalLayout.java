/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.Rollout.ApprovalDecision;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutApproval;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

public class ApprovalLayout extends ValidatableLayout {
    private static final String APPROVAL_CAPTION = "label.approval.decision";
    private static final String APPROVAL_BUTTON_LABEL = "button.approve";
    private static final String DENY_BUTTON_LABEL = "button.deny";

    private final VaadinMessageSource i18n;

    private final Binder<ProxyRolloutApproval> binder;

    private final RadioButtonGroup<Rollout.ApprovalDecision> approveButtonsGroup;
    private final TextField approvalRemark;

    public ApprovalLayout(final VaadinMessageSource i18n) {
        this.i18n = i18n;

        this.binder = new Binder<>();

        this.approveButtonsGroup = createApproveButtonsGroupField();
        this.approvalRemark = createApprovalRemarkField();

        setValidationStatusByBinder(binder);
    }

    private RadioButtonGroup<ApprovalDecision> createApproveButtonsGroupField() {
        final RadioButtonGroup<Rollout.ApprovalDecision> approveButtonsGroupField = new RadioButtonGroup<>();
        approveButtonsGroupField.setId(UIComponentIdProvider.ROLLOUT_APPROVAL_OPTIONGROUP_ID);
        approveButtonsGroupField.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
        approveButtonsGroupField.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        approveButtonsGroupField.addStyleName("custom-option-group");
        approveButtonsGroupField.setItems(Rollout.ApprovalDecision.values());

        approveButtonsGroupField.setItemCaptionGenerator(item -> {
            if (Rollout.ApprovalDecision.APPROVED == item) {
                return i18n.getMessage(APPROVAL_BUTTON_LABEL);
            } else {
                return i18n.getMessage(DENY_BUTTON_LABEL);
            }
        });
        approveButtonsGroupField.setItemIconGenerator(item -> {
            if (Rollout.ApprovalDecision.APPROVED == item) {
                return VaadinIcons.CHECK;
            } else {
                return VaadinIcons.CLOSE;
            }
        });

        binder.forField(approveButtonsGroupField).bind(ProxyRolloutApproval::getApprovalDecision,
                ProxyRolloutApproval::setApprovalDecision);

        return approveButtonsGroupField;
    }

    private TextField createApprovalRemarkField() {
        final TextField approvalRemarkField = new TextFieldBuilder(Rollout.APPROVAL_REMARK_MAX_SIZE)
                .id(UIComponentIdProvider.ROLLOUT_APPROVAL_REMARK_FIELD_ID)
                .prompt(i18n.getMessage("label.approval.remark")).buildTextComponent();
        approvalRemarkField.setWidthFull();

        binder.forField(approvalRemarkField).bind(ProxyRolloutApproval::getApprovalRemark,
                ProxyRolloutApproval::setApprovalRemark);

        return approvalRemarkField;
    }

    public void addApprovalToLayout(final GridLayout layout, final int lastColumnIdx, final int lastRowIdx) {
        layout.addComponent(SPUIComponentProvider.generateLabel(i18n, APPROVAL_CAPTION), 0, lastRowIdx);

        final HorizontalLayout approvalButtonsLayout = new HorizontalLayout(approveButtonsGroup, approvalRemark);
        approvalButtonsLayout.setSpacing(false);
        approvalButtonsLayout.setMargin(false);
        approvalButtonsLayout.setWidthFull();
        approvalButtonsLayout.setExpandRatio(approvalRemark, 1.0F);

        layout.addComponent(approvalButtonsLayout, 1, lastRowIdx, lastColumnIdx, lastRowIdx);
    }

    public void setBean(final ProxyRolloutApproval bean) {
        binder.readBean(bean);
    }

    public ProxyRolloutApproval getBean() throws ValidationException {
        final ProxyRolloutApproval bean = new ProxyRolloutApproval();
        binder.writeBean(bean);

        return bean;
    }
}
