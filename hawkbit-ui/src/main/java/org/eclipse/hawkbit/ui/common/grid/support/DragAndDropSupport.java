/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.hawkbit.im.authentication.SpPermission;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.AssignmentSupport;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

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
public class DragAndDropSupport<T> {
    private final AbstractGrid<T, ?> grid;
    private final VaadinMessageSource i18n;
    private final UINotification notification;
    private final SpPermissionChecker permissionChecker;
    // TODO: would it be better to use chain of responsibility pattern?
    private final Map<String, AssignmentSupport<?, T>> sourceTargetAssignmentStrategies;

    public DragAndDropSupport(final AbstractGrid<T, ?> grid, final VaadinMessageSource i18n,
            final UINotification notification, final SpPermissionChecker permissionChecker,
            final Map<String, AssignmentSupport<?, T>> sourceTargetAssignmentStrategies) {
        this.grid = grid;
        this.i18n = i18n;
        this.notification = notification;
        this.permissionChecker = permissionChecker;
        this.sourceTargetAssignmentStrategies = sourceTargetAssignmentStrategies;
    }

    public void addDragAndDrop() {
        addDragSource();
        addDropTarget();
    }

    public void addDragSource() {
        final GridDragSource<T> dragSource = new GridDragSource<>(grid);

        dragSource.setDataTransferData("source_id", grid.getId());
        dragSource.addGridDragStartListener(event -> dragSource.setDragData(event.getDraggedItems()));

        dragSource.addGridDragEndListener(event -> {
            if (event.isCanceled()) {
                // TODO: extract to method
                notification.displayValidationError(i18n.getMessage(UIMessageIdProvider.MESSAGE_ACTION_NOT_ALLOWED));
            }
        });
    }

    public void addDropTarget() {
        final GridDropTarget<T> dropTarget = new GridDropTarget<>(grid, DropMode.ON_TOP);

        dropTarget.addGridDropListener(event -> {
            final String sourceId = event.getDataTransferData("source_id").orElse("");
            final T dropTargetItem = event.getDropTargetRow().orElse(null);

            if (!isDropValid(sourceId, dropTargetItem)) {
                return;
            }

            event.getDragSourceExtension().ifPresent(source -> {
                if (source instanceof GridDragSource) {
                    final AssignmentSupport<?, T> assignmentStrategy = sourceTargetAssignmentStrategies.get(sourceId);
                    if (assignmentStrategy != null) {
                        assignmentStrategy.assignSourceItemsToTargetItem(source.getDragData(), dropTargetItem);
                    } else {
                        notification.displayValidationError(
                                i18n.getMessage(UIMessageIdProvider.MESSAGE_ACTION_NOT_ALLOWED));
                    }
                }
            });
        });
    }

    private boolean isDropValid(final String sourceId, final T dropTargetItem) {
        if (StringUtils.isEmpty(sourceId) || !sourceTargetAssignmentStrategies.keySet().contains(sourceId)
                || dropTargetItem == null) {
            notification.displayValidationError(i18n.getMessage(UIMessageIdProvider.MESSAGE_ACTION_NOT_ALLOWED));
            return false;
        }

        // TODO: check if it influences tags assignment
        if (!permissionChecker.hasUpdateTargetPermission()) {
            notification.displayValidationError(
                    i18n.getMessage("message.permission.insufficient", Arrays.asList(SpPermission.UPDATE_TARGET)));
            return false;
        }

        return true;
    }
}
