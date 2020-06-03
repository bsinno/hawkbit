/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import java.io.Serializable;

import org.eclipse.hawkbit.repository.model.Rollout.ApprovalDecision;

/**
 * Proxy for rollout approval definition.
 */
public class ProxyRolloutApproval implements Serializable {

    private static final long serialVersionUID = 1L;

    private String approvalRemark;
    private ApprovalDecision approvalDecision;

    public String getApprovalRemark() {
        return approvalRemark;
    }

    public void setApprovalRemark(final String approvalRemark) {
        this.approvalRemark = approvalRemark;
    }

    public ApprovalDecision getApprovalDecision() {
        return approvalDecision;
    }

    public void setApprovalDecision(final ApprovalDecision approvalDecision) {
        this.approvalDecision = approvalDecision;
    }
}
