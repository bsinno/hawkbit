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

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.model.AbstractAssignmentResult;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.management.dstag.filter.DistributionTagLayoutUiState;
import org.eclipse.hawkbit.ui.management.event.RefreshDistributionTableByFilterEvent;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Support for assigning the distribution set tags to distribution set.
 * 
 */
public class DsTagsToDistributionSetAssignmentSupport
        extends TagsAssignmentSupport<ProxyDistributionSet, DistributionSet> {
    private final DistributionSetManagement distributionSetManagement;
    private final DistributionTagLayoutUiState distributionTagLayoutUiState;
    private final UIEventBus eventBus;

    public DsTagsToDistributionSetAssignmentSupport(final UINotification notification, final VaadinMessageSource i18n,
            final DistributionSetManagement distributionSetManagement,
            final DistributionTagLayoutUiState distributionTagLayoutUiState, final UIEventBus eventBus) {
        super(notification, i18n);

        this.distributionSetManagement = distributionSetManagement;
        this.distributionTagLayoutUiState = distributionTagLayoutUiState;
        this.eventBus = eventBus;
    }

    @Override
    protected AbstractAssignmentResult<DistributionSet> toggleTagAssignment(final String tagName,
            final ProxyDistributionSet targetItem) {
        return distributionSetManagement.toggleTagAssignment(Collections.singletonList(targetItem.getId()), tagName);
    }

    @Override
    protected void publishFilterEvent(final AbstractAssignmentResult<DistributionSet> tagsAssignmentResult) {
        if (tagsAssignmentResult.getAssigned() > 0 && distributionTagLayoutUiState.isNoTagClicked()) {
            eventBus.publish(this, new RefreshDistributionTableByFilterEvent());
        }
    }
}
