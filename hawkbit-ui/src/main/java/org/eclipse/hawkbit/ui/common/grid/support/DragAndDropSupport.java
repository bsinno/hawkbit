/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.EntityDraggingEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityDraggingEventPayload.DraggingEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.selection.RangeSelectionGridDragSource;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.AssignmentSupport;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.cronutils.utils.StringUtils;
import com.vaadin.server.AbstractExtension;
import com.vaadin.shared.ui.grid.DropMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.components.grid.GridDragSource;
import com.vaadin.ui.components.grid.GridDragStartEvent;
import com.vaadin.ui.components.grid.GridDropTarget;

/**
 * Support for dragging items between grids.
 * 
 * @param <T>
 *            The item-type used by the source grid
 */
public class DragAndDropSupport<T extends ProxyIdentifiableEntity> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DragAndDropSupport.class);

    private static final String DRAG_SOURCE = "drag-source";
    private static final String DROP_TARGET = "drop-target";

    private final AbstractGrid<T, ?> grid;
    private final VaadinMessageSource i18n;
    private final UINotification notification;
    private final Map<String, AssignmentSupport<?, T>> sourceTargetAssignmentStrategies;
    private final UIEventBus eventBus;

    private GridDragSource<T> dragSource;
    private GridDropTarget<T> dropTarget;
    private EntityDraggingListener draggingListener;

    private boolean ignoreSelection;

    public DragAndDropSupport(final AbstractGrid<T, ?> grid, final VaadinMessageSource i18n,
            final UINotification notification,
            final Map<String, AssignmentSupport<?, T>> sourceTargetAssignmentStrategies, final UIEventBus eventBus) {
        this.grid = grid;
        this.i18n = i18n;
        this.notification = notification;
        this.sourceTargetAssignmentStrategies = sourceTargetAssignmentStrategies;
        this.eventBus = eventBus;

        this.dragSource = null;
        this.dropTarget = null;
        this.draggingListener = null;

        this.ignoreSelection = false;

        // TODO: check if needed or implement unsubscribe on pre destroy of grid
        // (does not work on grid maximize/minimize)
        // addDraggingListenerSubscription();
    }

    // TODO: workaround for target/ds tags that currently do not support
    // selection
    public void ignoreSelection(final boolean ignoreSelection) {
        this.ignoreSelection = ignoreSelection;
    }

    private void addDraggingListenerSubscription() {
        grid.addAttachListener(event -> {
            if (draggingListener != null) {
                eventBus.subscribe(draggingListener, EventTopics.ENTITY_DRAGGING);
            }
        });
        grid.addDetachListener(event -> {
            if (draggingListener != null) {
                eventBus.unsubscribe(draggingListener);
            }
        });
    }

    public void addDragAndDrop() {
        addDragSource();
        addDropTarget();
    }

    public void addDragSource() {
        if (!isGridValidForDragOrDrop(dragSource, DRAG_SOURCE)) {
            return;
        }

        dragSource = new RangeSelectionGridDragSource<>(grid);

        final String gridId = grid.getGridId();
        dragSource.setDataTransferData("source_id", gridId);
        dragSource.addGridDragStartListener(event -> {
            dragSource.setDragData(getItemsToDrag(event));
            eventBus.publish(EventTopics.ENTITY_DRAGGING, this,
                    new EntityDraggingEventPayload(gridId, DraggingEventType.STARTED));
        });
        dragSource.addGridDragEndListener(event -> {
            if (event.isCanceled()) {
                showActionNotAllowedNotification();
            }
            eventBus.publish(EventTopics.ENTITY_DRAGGING, this,
                    new EntityDraggingEventPayload(gridId, DraggingEventType.STOPPED));
        });
    }

    private boolean isGridValidForDragOrDrop(final AbstractExtension dragOrDropExtension,
            final String dragOrDropDescription) {
        if (!ignoreSelection && !grid.hasSelectionSupport()) {
            LOGGER.warn("Can not add {} for non-selectable grid '{}', specify single- or multi-selection model",
                    dragOrDropDescription, grid.getGridId());
            return false;
        }

        if (dragOrDropExtension != null) {
            LOGGER.warn(
                    "Can not add {} to the grid '{}', because it already exists. Consider removing previous before adding a new one",
                    dragOrDropDescription, grid.getGridId());
            return false;
        }

        return true;
    }

    private List<T> getItemsToDrag(final GridDragStartEvent<T> event) {
        final List<T> selectedItems = new ArrayList<>(event.getComponent().getSelectedItems());
        final List<T> draggedVisibleItems = event.getDraggedItems();

        if (draggedVisibleItems.size() == 1 && !selectedItems.contains(draggedVisibleItems.get(0))) {
            return Collections.singletonList(draggedVisibleItems.get(0));
        } else {
            return selectedItems;
        }
    }

    private void showActionNotAllowedNotification() {
        notification.displayValidationError(i18n.getMessage(UIMessageIdProvider.MESSAGE_ACTION_NOT_ALLOWED));
    }

    public void addDropTarget() {
        if (!isGridValidForDragOrDrop(dropTarget, DROP_TARGET)) {
            return;
        }

        dropTarget = new GridDropTarget<>(grid, DropMode.ON_TOP);

        addGridDropStylingListener();

        dropTarget.addGridDropListener(event -> {
            final String sourceId = event.getDataTransferData("source_id").orElse("");
            final T dropTargetItem = event.getDropTargetRow().orElse(null);
            final AssignmentSupport<?, T> assignmentStrategy = sourceTargetAssignmentStrategies.get(sourceId);

            if (!isDropValid(sourceId, dropTargetItem, assignmentStrategy)) {
                return;
            }

            grid.deselectAll();
            grid.select(dropTargetItem);

            event.getDragSourceExtension().ifPresent(source -> {
                if (source instanceof GridDragSource) {
                    assignmentStrategy.assignSourceItemsToTargetItem(source.getDragData(), dropTargetItem);
                } else {
                    showActionNotAllowedNotification();
                }
            });
        });
    }

    private void addGridDropStylingListener() {
        draggingListener = new EntityDraggingListener(sourceTargetAssignmentStrategies.keySet(), grid);
        // TODO: is it alright to subscribe here, what if already subscribed?
        // Can we use addDraggingListenerSubscription method?
        eventBus.subscribe(draggingListener, EventTopics.ENTITY_DRAGGING);
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

    public void removeDragAndDrop() {
        removeDragSource();
        removeDropTarget();
    }

    public void removeDragSource() {
        if (dragSource != null) {
            dragSource.remove();
            dragSource = null;
        }
    }

    public void removeDropTarget() {
        if (dropTarget != null) {
            dropTarget.remove();
            dropTarget = null;
        }

        if (draggingListener != null) {
            eventBus.unsubscribe(draggingListener);
            draggingListener = null;
        }
    }

    public static class EntityDraggingListener {
        private static final String DROP_HINT_STYLE = "show-drop-hint";
        private final Collection<String> draggingSourceIds;
        private final Component dropComponent;

        public EntityDraggingListener(final Collection<String> draggingSourceIds, final Component dropComponent) {
            this.draggingSourceIds = draggingSourceIds;
            this.dropComponent = dropComponent;
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onEntityDraggingEvent(final EntityDraggingEventPayload eventPayload) {
            if (CollectionUtils.isEmpty(draggingSourceIds)
                    || !draggingSourceIds.contains(eventPayload.getSourceGridId())) {
                return;
            }

            if (eventPayload.getDraggingEventType() == DraggingEventType.STARTED) {
                dropComponent.addStyleName(DROP_HINT_STYLE);
            } else {
                dropComponent.removeStyleName(DROP_HINT_STYLE);
            }
        }
    }
}
