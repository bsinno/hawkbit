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
import java.util.Set;

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.AbstractAssignmentResult;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.management.event.TargetFilterEvent;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Support for assigning target tags to target.
 * 
 */
public class TargetTagsToTargetAssignmentSupport extends TagsAssignmentSupport<ProxyTarget, Target> {
    private final TargetManagement targetManagement;
    private final UIEventBus eventBus;
    private final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState;

    public TargetTagsToTargetAssignmentSupport(final UINotification notification, final VaadinMessageSource i18n,
            final TargetManagement targetManagement, final UIEventBus eventBus,
            final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState) {
        super(notification, i18n);

        this.targetManagement = targetManagement;
        this.eventBus = eventBus;
        this.targetTagFilterLayoutUiState = targetTagFilterLayoutUiState;
    }

    @Override
    protected AbstractAssignmentResult<Target> toggleTagAssignment(final String tagName, final ProxyTarget targetItem) {
        return targetManagement.toggleTagAssignment(Collections.singletonList(targetItem.getControllerId()), tagName);
    }

    @Override
    protected void publishFilterEvent(final AbstractAssignmentResult<Target> tagsAssignmentResult) {
        final Set<Long> tagsClickedList = targetTagFilterLayoutUiState.getClickedTargetTagIds();
        if (tagsAssignmentResult.getUnassigned() > 0 && !CollectionUtils.isEmpty(tagsClickedList)) {
            eventBus.publish(this, TargetFilterEvent.FILTER_BY_TAG);
        }
    }
}
