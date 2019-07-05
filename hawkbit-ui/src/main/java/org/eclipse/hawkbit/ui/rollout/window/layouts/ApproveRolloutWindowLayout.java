/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;

/**
 * Layout builder for Approve Rollout window.
 */
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2160" })
public class ApproveRolloutWindowLayout extends UpdateRolloutWindowLayout {

    private static final long serialVersionUID = 1L;

    public ApproveRolloutWindowLayout(final RolloutWindowDependencies dependencies) {
        super(dependencies);

        insertRow(getRows());
        addComponent(componentBuilder.getLabel("label.approval.decision"), 0, 6);
        addComponent(componentBuilder.createApprovalLayout(getProxyRolloutBinder()), 1, 6, 3, 6);
    }
}
