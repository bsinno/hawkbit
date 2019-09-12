/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support;

import java.util.List;
import java.util.Map;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.AssignmentSupport;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;

import com.cronutils.utils.StringUtils;
import com.vaadin.shared.ui.grid.DropMode;
import com.vaadin.ui.components.grid.GridDragSource;
import com.vaadin.ui.components.grid.GridDropTarget;

/**
 * Support for dragging items between grids.
 * 
 * @param <T>
 *            The item-type used by the source grid
 */
public class DragAndDropSupport<T extends ProxyIdentifiableEntity> {
    private final AbstractGrid<T, ?> grid;
    private final VaadinMessageSource i18n;
    private final UINotification notification;
    private final Map<String, AssignmentSupport<?, T>> sourceTargetAssignmentStrategies;

    public DragAndDropSupport(final AbstractGrid<T, ?> grid, final VaadinMessageSource i18n,
            final UINotification notification,
            final Map<String, AssignmentSupport<?, T>> sourceTargetAssignmentStrategies) {
        this.grid = grid;
        this.i18n = i18n;
        this.notification = notification;
        this.sourceTargetAssignmentStrategies = sourceTargetAssignmentStrategies;
    }

    public void addDragAndDrop() {
        addDragSource();
        addDropTarget();
    }

    public void addDragSource() {
        final GridDragSource<T> dragSource = new GridDragSource<>(grid);

        dragSource.setDataTransferData("source_id", grid.getGridId());
        dragSource.addGridDragStartListener(event -> dragSource.setDragData(event.getDraggedItems()));

        dragSource.addGridDragEndListener(event -> {
            if (event.isCanceled()) {
                showActionNotAllowedNotification();
            }
        });
    }

    private void showActionNotAllowedNotification() {
        notification.displayValidationError(i18n.getMessage(UIMessageIdProvider.MESSAGE_ACTION_NOT_ALLOWED));
    }

    public void addDropTarget() {
        final GridDropTarget<T> dropTarget = new GridDropTarget<>(grid, DropMode.ON_TOP);

        dropTarget.addGridDropListener(event -> {
            final String sourceId = event.getDataTransferData("source_id").orElse("");
            final T dropTargetItem = event.getDropTargetRow().orElse(null);
            final AssignmentSupport<?, T> assignmentStrategy = sourceTargetAssignmentStrategies.get(sourceId);

            if (!isDropValid(sourceId, dropTargetItem, assignmentStrategy)) {
                return;
            }

            event.getDragSourceExtension().ifPresent(source -> {
                if (source instanceof GridDragSource) {
                    assignmentStrategy.assignSourceItemsToTargetItem(source.getDragData(), dropTargetItem);
                } else {
                    showActionNotAllowedNotification();
                }
            });
        });
    }

    private boolean isDropValid(final String sourceId, final T dropTargetItem,
            final AssignmentSupport<?, T> assignmentStrategy) {
        if (StringUtils.isEmpty(sourceId) || !sourceTargetAssignmentStrategies.keySet().contains(sourceId)
                || dropTargetItem == null || assignmentStrategy == null) {
            showActionNotAllowedNotification();
            return false;
        }

        final List<String> requiredPermissions = assignmentStrategy.getMissingPermissionsForDrop();
        if (!CollectionUtils.isEmpty(requiredPermissions)) {
            notification
                    .displayValidationError(i18n.getMessage("message.permission.insufficient", requiredPermissions));
            return false;
        }

        return true;
    }
}
