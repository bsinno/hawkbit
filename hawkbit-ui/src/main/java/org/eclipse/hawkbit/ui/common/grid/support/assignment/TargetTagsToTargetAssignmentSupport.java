/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support.assignment;

import java.util.Collections;
import java.util.List;

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.AssignmentResult;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.management.event.TargetFilterEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Support for assigning target tags to target.
 * 
 */
public class TargetTagsToTargetAssignmentSupport extends TagsAssignmentSupport<ProxyTarget, Target> {
    private final TargetManagement targetManagement;
    private final ManagementUIState managementUIState;
    private final UIEventBus eventBus;

    public TargetTagsToTargetAssignmentSupport(final UINotification notification, final VaadinMessageSource i18n,
            final TargetManagement targetManagement, final ManagementUIState managementUIState,
            final UIEventBus eventBus) {
        super(notification, i18n);

        this.targetManagement = targetManagement;
        this.managementUIState = managementUIState;
        this.eventBus = eventBus;
    }

    @Override
    protected AssignmentResult<Target> toggleTagAssignment(final String tagName, final ProxyTarget targetItem) {
        return targetManagement.toggleTagAssignment(Collections.singletonList(targetItem.getControllerId()), tagName);
    }

    @Override
    protected void publishFilterEvent(final AssignmentResult<Target> tagsAssignmentResult) {
        final List<String> tagsClickedList = managementUIState.getTargetTableFilters().getClickedTargetTags();
        if (tagsAssignmentResult.getUnassigned() > 0 && !tagsClickedList.isEmpty()) {
            eventBus.publish(this, TargetFilterEvent.FILTER_BY_TAG);
        }
    }
}
