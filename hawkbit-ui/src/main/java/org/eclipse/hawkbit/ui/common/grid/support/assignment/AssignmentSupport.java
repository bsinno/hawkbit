/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support.assignment;

import java.util.List;

import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

/**
 * Support for assigning the items between two grids.
 * 
 * @param <S>
 *            The item-type of source items
 * @param <T>
 *            The item-type of target item
 */
public abstract class AssignmentSupport<S, T> {
    protected final UINotification notification;
    protected final VaadinMessageSource i18n;

    protected AssignmentSupport(final UINotification notification, final VaadinMessageSource i18n) {
        this.notification = notification;
        this.i18n = i18n;
    }

    // only needed for drag and drop support
    public void assignSourceItemsToTargetItem(final Object sourceItemsToAssign, final T targetItem) {
        if (sourceItemsToAssign instanceof List) {
            assignSourceItemsToTargetItem(sourceItemsToAssign, targetItem);
        } else {
            // TODO: consider providing more specific message
            showGenericErrorNotification();
        }
    }

    private void showGenericErrorNotification() {
        notification.displayValidationError(i18n.getMessage("message.action.did.not.work"));
    }

    public void assignSourceItemsToTargetItem(final List<S> sourceItemsToAssign, final T targetItem) {
        if (sourceItemsToAssign.isEmpty()) {
            showGenericErrorNotification();
            return;
        }

        // TODO: check if needed
        // selectDraggedEntities(source, sourceItemsToAssign);
        // selectDroppedEntities(targetItem);

        final List<S> filteredSourceItems = getFilteredSourceItems(sourceItemsToAssign);
        if (filteredSourceItems.isEmpty()) {
            // TODO: consider implementing stack notifications, otherwise
            // notifications coming from getFilteredSourceItems method will be
            // overwritten
            showGenericErrorNotification();
            return;
        }

        performAssignment(filteredSourceItems, targetItem);
    }

    // can be overriden in child classes in order to filter source items list
    protected List<S> getFilteredSourceItems(final List<S> sourceItemsToAssign) {
        return sourceItemsToAssign;
    }

    protected abstract void performAssignment(final List<S> sourceItemsToAssign, final T targetItem);
}
