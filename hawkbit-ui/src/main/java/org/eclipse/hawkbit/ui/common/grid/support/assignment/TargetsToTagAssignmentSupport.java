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
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.im.authentication.SpPermission;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.TargetTagAssignmentResult;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Support for assigning the {@link ProxyTarget} items to {@link ProxyTag}.
 * 
 */
public class TargetsToTagAssignmentSupport extends AssignmentSupport<ProxyTarget, ProxyTag> {
    private final TargetManagement targetManagement;
    private final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState;
    private final UIEventBus eventBus;
    private final SpPermissionChecker permChecker;

    public TargetsToTagAssignmentSupport(final UINotification notification, final VaadinMessageSource i18n,
            final UIEventBus eventBus, final SpPermissionChecker permChecker, final TargetManagement targetManagement,
            final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState) {
        super(notification, i18n);

        this.eventBus = eventBus;
        this.permChecker = permChecker;
        this.targetManagement = targetManagement;
        this.targetTagFilterLayoutUiState = targetTagFilterLayoutUiState;
    }

    @Override
    public List<String> getMissingPermissionsForDrop() {
        return permChecker.hasUpdateTargetPermission() ? Collections.emptyList()
                : Collections.singletonList(SpPermission.UPDATE_TARGET);
    }

    @Override
    protected void performAssignment(final List<ProxyTarget> sourceItemsToAssign, final ProxyTag targetItem) {
        final String tagName = targetItem.getName();
        final Collection<String> controllerIdsToAssign = sourceItemsToAssign.stream().map(ProxyTarget::getControllerId)
                .collect(Collectors.toList());

        final TargetTagAssignmentResult tagsAssignmentResult = targetManagement
                .toggleTagAssignment(controllerIdsToAssign, tagName);

        // TODO: check if it could be extracted from HawkbitCommonUtil
        notification.displaySuccess(HawkbitCommonUtil.createAssignmentMessage(tagName, tagsAssignmentResult, i18n));

        publishAssignTargetTagEvent(tagsAssignmentResult);
        publishUnAssignTargetTagEvent(targetItem.getId(), tagsAssignmentResult);
    }

    private void publishAssignTargetTagEvent(final TargetTagAssignmentResult result) {
        final boolean isNewTargetTagAssigned = result.getAssigned() >= 1
                && targetTagFilterLayoutUiState.isNoTagClicked();
        if (isNewTargetTagAssigned) {
            eventBus.publish(this, ManagementUIEvent.ASSIGN_TARGET_TAG);
        }

    }

    private void publishUnAssignTargetTagEvent(final Long targetTagId, final TargetTagAssignmentResult result) {
        final Set<Long> tagsClickedList = targetTagFilterLayoutUiState.getClickedTargetTagIdsWithName().keySet();
        final boolean isTargetTagUnAssigned = result.getUnassigned() >= 1 && !CollectionUtils.isEmpty(tagsClickedList)
                && tagsClickedList.contains(targetTagId);

        if (isTargetTagUnAssigned) {
            eventBus.publish(this, ManagementUIEvent.UNASSIGN_TARGET_TAG);
        }
    }
}
