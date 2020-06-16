/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.rollout.window.layouts.ValidatableLayout.ValidationStatus;

import com.vaadin.ui.GridLayout;

/**
 * Layout builder for Approve Rollout window.
 */
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2160" })
public class ApproveRolloutWindowLayout extends UpdateRolloutWindowLayout {

    private final ApprovalLayout approvalLayout;

    public ApproveRolloutWindowLayout(final RolloutWindowDependencies dependencies) {
        super(dependencies);

        this.approvalLayout = rolloutComponentBuilder.createApprovalLayout();

        addValidationStatusListeners();
    }

    private void addValidationStatusListeners() {
        // TODO: rethink the concept to remove duplication between listeners
        approvalLayout.setValidationListener(this::onApprovalValidationChanged);
    }

    private void onApprovalValidationChanged(final ValidationStatus status) {
        if (validationCallback == null) {
            return;
        }

        if (ValidationStatus.VALID != status) {
            validationCallback.accept(false);
            return;
        }

        validationCallback.accept(rolloutFormLayout.isValid());
    }

    @Override
    protected void addComponents(final GridLayout rootLayout) {
        super.addComponents(rootLayout);

        rootLayout.insertRow(rootLayout.getRows());

        final int lastRowIdx = rootLayout.getRows() - 1;
        final int lastColumnIdx = rootLayout.getColumns() - 1;

        approvalLayout.addApprovalToLayout(rootLayout, lastColumnIdx, lastRowIdx);
    }

    @Override
    protected void onRolloutFormValidationChanged(final ValidationStatus status) {
        // TODO: change, I do not like this solution, because we are accepting
        // true from parent rollout form and then true/false from approval
        // layout
        super.onRolloutFormValidationChanged(status);

        if (ValidationStatus.VALID == status) {
            validationCallback.accept(approvalLayout.isValid());
        }
    }

    @Override
    public void setEntity(final ProxyRolloutWindow proxyEntity) {
        super.setEntity(proxyEntity);

        approvalLayout.setBean(proxyEntity.getRolloutApproval());
    }

    @Override
    public ProxyRolloutWindow getEntity() {
        final ProxyRolloutWindow proxyEntity = super.getEntity();
        proxyEntity.setRolloutApproval(approvalLayout.getBean());

        return proxyEntity;
    }
}
