/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support.assignment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.im.authentication.SpPermission;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetTagAssignmentResult;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.DsModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.management.dstag.filter.DistributionTagLayoutUiState;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Support for assigning the {@link ProxyDistributionSet} items to
 * {@link ProxyTag}.
 * 
 */
// TODO: remove duplication with TargetsToTagAssignmentSupport
public class DistributionSetsToTagAssignmentSupport extends AssignmentSupport<ProxyDistributionSet, ProxyTag> {
    private final DistributionSetManagement distributionSetManagement;
    private final DistributionTagLayoutUiState distributionTagLayoutUiState;
    private final UIEventBus eventBus;
    private final SpPermissionChecker permChecker;

    public DistributionSetsToTagAssignmentSupport(final UINotification notification, final VaadinMessageSource i18n,
            final DistributionSetManagement distributionSetManagement, final UIEventBus eventBus,
            final SpPermissionChecker permChecker, final DistributionTagLayoutUiState distributionTagLayoutUiState) {
        super(notification, i18n);

        this.distributionSetManagement = distributionSetManagement;
        this.distributionTagLayoutUiState = distributionTagLayoutUiState;
        this.eventBus = eventBus;
        this.permChecker = permChecker;
    }

    @Override
    public List<String> getMissingPermissionsForDrop() {
        return permChecker.hasUpdateTargetPermission() ? Collections.emptyList()
                : Collections.singletonList(SpPermission.UPDATE_REPOSITORY);
    }

    @Override
    protected void performAssignment(final List<ProxyDistributionSet> sourceItemsToAssign, final ProxyTag targetItem) {
        final String tagName = targetItem.getName();
        final Collection<Long> dsIdsToAssign = sourceItemsToAssign.stream().map(ProxyDistributionSet::getId)
                .collect(Collectors.toList());

        final DistributionSetTagAssignmentResult tagsAssignmentResult = distributionSetManagement
                .toggleTagAssignment(dsIdsToAssign, tagName);

        // TODO: check if it could be extracted from HawkbitCommonUtil
        // TODO: fix the bug of displaying messages for Targets instead of
        // Distribution Sets
        notification.displaySuccess(HawkbitCommonUtil.createAssignmentMessage(tagName, tagsAssignmentResult, i18n));

        publishTagAssignmentEvent(tagsAssignmentResult, sourceItemsToAssign, targetItem);
    }

    private void publishTagAssignmentEvent(final DistributionSetTagAssignmentResult tagsAssignmentResult,
            final List<ProxyDistributionSet> sourceItemsToAssign, final ProxyTag targetItem) {
        final List<Long> assignedDsIds = sourceItemsToAssign.stream().map(ProxyIdentifiableEntity::getId)
                .collect(Collectors.toList());
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new DsModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, assignedDsIds));

        // TODO: should we additionally send tag assignment event in order to
        // refresh the grid?
        // if ((tagsAssignmentResult.getUnassigned() > 0 &&
        // !CollectionUtils.isEmpty(distributionTagLayoutUiState.getClickedDsTagIdsWithName()
        // &&
        // distributionTagLayoutUiState.getClickedDsTagIdsWithName().keySet().contains(targetItem.getId()))
        // || (tagsAssignmentResult.getAssigned() > 0 &&
        // distributionTagLayoutUiState.isNoTagClicked())) {
        // eventBus.publish("tagAssignmentChanged", this, new
        // TagAssignmentPayload(...);}
    }
}
