/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SpPermissionChecker;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.event.ManagementViewAcceptCriteria;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.I18N;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

/**
 * Target Tag filter layout.
 */
public class TargetTagFilterLayout extends AbstractTargetTagFilterLayout {

    private static final long serialVersionUID = 2153612878428575009L;

    public TargetTagFilterLayout(final I18N i18n, final CreateUpdateTargetTagLayoutWindow createUpdateTargetTagLayout,
            final ManagementUIState managementUIState, final ManagementViewAcceptCriteria managementViewAcceptCriteria,
            final SpPermissionChecker permChecker, final UIEventBus eventBus, final UINotification notification,
            final EntityFactory entityFactory, final TargetManagement targetManagement,
            final TargetFilterQueryManagement targetFilterQueryManagement) {
        super(new TargetTagFilterHeader(i18n, createUpdateTargetTagLayout, managementUIState, permChecker, eventBus),
                new MultipleTargetFilter(createUpdateTargetTagLayout, permChecker, managementUIState, i18n, eventBus,
                        managementViewAcceptCriteria, notification, entityFactory, targetManagement,
                        targetFilterQueryManagement),
                managementUIState);
        eventBus.subscribe(this);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ManagementUIEvent event) {
        if (event == ManagementUIEvent.HIDE_TARGET_TAG_LAYOUT) {
            setVisible(false);
        }
        if (event == ManagementUIEvent.SHOW_TARGET_TAG_LAYOUT) {
            setVisible(true);
        }
    }

    @Override
    public Boolean onLoadIsTypeFilterIsClosed() {
        return managementUIState.isTargetTagFilterClosed();
    }
}
