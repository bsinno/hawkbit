/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Window;

public class AutoAssignmentWindowBuilder extends AbstractEntityWindowBuilder<ProxyTargetFilterQuery> {
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final TargetManagement targetManagement;
    private final TargetFilterQueryManagement targetFilterQueryManagement;
    private final DistributionSetManagement dsManagement;

    public AutoAssignmentWindowBuilder(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final UINotification uiNotification, final TargetManagement targetManagement,
            final TargetFilterQueryManagement targetFilterQueryManagement,
            final DistributionSetManagement dsManagement) {
        super(i18n);

        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        this.targetManagement = targetManagement;
        this.targetFilterQueryManagement = targetFilterQueryManagement;
        this.dsManagement = dsManagement;
    }

    @Override
    protected String getWindowId() {
        return UIComponentIdProvider.DIST_SET_SELECT_WINDOW_ID;
    }

    public Window getWindowForAutoAssignment(final ProxyTargetFilterQuery proxyTargetFilter) {
        return getWindowForEntity(proxyTargetFilter, new AutoAssignmentWindowController(i18n, eventBus, uiNotification,
                targetManagement, targetFilterQueryManagement, new AutoAssignmentWindowLayout(i18n, dsManagement)));
    }
}