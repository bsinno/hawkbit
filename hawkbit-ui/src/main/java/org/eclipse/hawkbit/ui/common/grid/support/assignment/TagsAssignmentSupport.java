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

import org.eclipse.hawkbit.repository.model.AssignmentResult;
import org.eclipse.hawkbit.repository.model.NamedEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

/**
 * Support for assigning the {@link ProxyTag} items to target item (target or
 * distribution set).
 * 
 * @param <T>
 *            The item-type of target item
 * @param <R>
 *            The item-type of assignment result
 */
public abstract class TagsAssignmentSupport<T, R extends NamedEntity> extends AssignmentSupport<ProxyTag, T> {

    protected TagsAssignmentSupport(final UINotification notification, final VaadinMessageSource i18n) {
        super(notification, i18n);
    }

    @Override
    protected List<ProxyTag> getFilteredSourceItems(final List<ProxyTag> sourceItemsToAssign) {
        if (isNoTagAssigned(sourceItemsToAssign)) {
            notification.displayValidationError(
                    i18n.getMessage("message.tag.cannot.be.assigned", i18n.getMessage("label.no.tag.assigned")));
            return Collections.emptyList();
        }

        return sourceItemsToAssign;
    }

    // TODO: check if it is a valid check
    private boolean isNoTagAssigned(final List<ProxyTag> targetTagsToAssign) {
        return targetTagsToAssign.stream()
                .anyMatch(targetTag -> targetTag.getName().equals(i18n.getMessage("label.no.tag.assigned")));
    }

    // TODO: Implement multi-tag assignment
    // (e.x. within TargetManagement(toggleTagAssignment),
    // createAssignmentMessage, etc.)
    @Override
    protected void performAssignment(final List<ProxyTag> sourceItemsToAssign, final T targetItem) {

        // TODO: fix (we are taking first tag because multi-tag assignment is
        // not supported)
        final String tagName = sourceItemsToAssign.get(0).getName();
        final AssignmentResult<R> tagsAssignmentResult = toggleTagAssignment(tagName, targetItem);

        // TODO: check if it could be extracted from HawkbitCommonUtil
        notification.displaySuccess(HawkbitCommonUtil.createAssignmentMessage(tagName, tagsAssignmentResult, i18n));

        publishFilterEvent(tagsAssignmentResult);
    }

    protected abstract AssignmentResult<R> toggleTagAssignment(final String tagName, final T targetItem);

    protected abstract void publishFilterEvent(final AssignmentResult<R> tagsAssignmentResult);
}
