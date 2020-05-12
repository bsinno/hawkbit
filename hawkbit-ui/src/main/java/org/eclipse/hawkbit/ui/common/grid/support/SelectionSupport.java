/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.Supplier;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.components.grid.MultiSelectionModel;
import com.vaadin.ui.components.grid.MultiSelectionModel.SelectAllCheckBoxVisibility;
import com.vaadin.ui.components.grid.NoSelectionModel;
import com.vaadin.ui.components.grid.SingleSelectionModel;

/**
 * Support for selection on the grid.
 * 
 * @param <T>
 *            The item-type used by the grid
 */
public class SelectionSupport<T extends ProxyIdentifiableEntity> {
    private final Grid<T> grid;
    private final UIEventBus eventBus;
    private final EventLayout layout;
    private final EventView view;

    private final LongFunction<Optional<T>> mapIdToProxyEntityFunction;
    private final Supplier<Optional<Long>> selectedEntityIdUiStateProvider;
    private final Consumer<Optional<Long>> setSelectedEntityIdUiStateCallback;

    // For grids without selection support
    public SelectionSupport(final Grid<T> grid) {
        this(grid, null, null, null, null, null, null);
    }

    public SelectionSupport(final Grid<T> grid, final UIEventBus eventBus, final EventLayout layout,
            final EventView view, final LongFunction<Optional<T>> mapIdToProxyEntityFunction,
            final Supplier<Optional<Long>> selectedEntityIdUiStateProvider,
            final Consumer<Optional<Long>> setSelectedEntityIdUiStateCallback) {
        this.grid = grid;
        this.eventBus = eventBus;
        this.layout = layout;
        this.view = view;

        this.mapIdToProxyEntityFunction = mapIdToProxyEntityFunction;
        this.selectedEntityIdUiStateProvider = selectedEntityIdUiStateProvider;
        this.setSelectedEntityIdUiStateCallback = setSelectedEntityIdUiStateCallback;
    }

    public final void disableSelection() {
        grid.setSelectionMode(SelectionMode.NONE);
    }

    public final void enableSingleSelection() {
        grid.setSelectionMode(SelectionMode.SINGLE);

        grid.asSingleSelect().addSingleSelectionListener(event -> {
            final SelectionChangedEventType selectionType = event.getSelectedItem().isPresent()
                    ? SelectionChangedEventType.ENTITY_SELECTED
                    : SelectionChangedEventType.ENTITY_DESELECTED;
            final T itemToSend = event.getSelectedItem().orElse(event.getOldValue());

            sendSelectionChangedEvent(selectionType, itemToSend);
        });
    }

    public void sendSelectionChangedEvent(final SelectionChangedEventType selectionType, final T itemToSend) {
        if (eventBus == null) {
            return;
        }

        if (SelectionChangedEventType.ENTITY_SELECTED == selectionType && itemToSend == null) {
            return;
        }

        eventBus.publish(EventTopics.SELECTION_CHANGED, grid,
                new SelectionChangedEventPayload<>(selectionType, itemToSend, layout, view));

        updateUiState(selectionType, itemToSend);
    }

    private void updateUiState(final SelectionChangedEventType selectionType, final T itemToSend) {
        if (setSelectedEntityIdUiStateCallback == null) {
            return;
        }

        final Optional<Long> selectedItemId = SelectionChangedEventType.ENTITY_SELECTED == selectionType
                ? Optional.of(itemToSend).map(ProxyIdentifiableEntity::getId)
                : Optional.empty();
        setSelectedEntityIdUiStateCallback.accept(selectedItemId);
    }

    public final void enableMultiSelection() {
        grid.setSelectionMode(SelectionMode.MULTI);

        grid.asMultiSelect().setSelectAllCheckBoxVisibility(SelectAllCheckBoxVisibility.VISIBLE);
        grid.asMultiSelect().addMultiSelectionListener(event -> {
            if (event.getAllSelectedItems().size() == 1) {
                sendSelectionChangedEvent(SelectionChangedEventType.ENTITY_SELECTED,
                        event.getAllSelectedItems().iterator().next());
            } else if (event.getOldSelection().size() == 1) {
                sendSelectionChangedEvent(SelectionChangedEventType.ENTITY_DESELECTED,
                        event.getOldSelection().iterator().next());
            }
        });
    }

    public boolean isNoSelectionModel() {
        return grid.getSelectionModel() instanceof NoSelectionModel;
    }

    public boolean isSingleSelectionModel() {
        return grid.getSelectionModel() instanceof SingleSelectionModel;
    }

    public boolean isMultiSelectionModel() {
        return grid.getSelectionModel() instanceof MultiSelectionModel;
    }

    public Set<T> getSelectedItems() {
        if (isNoSelectionModel()) {
            return Collections.emptySet();
        }

        return grid.getSelectedItems();
    }

    public Optional<T> getSelectedEntity() {
        final Set<T> selectedItems = getSelectedItems();

        if (selectedItems.size() == 1) {
            return Optional.of(selectedItems.iterator().next());
        }

        return Optional.empty();
    }

    public Optional<Long> getSelectedEntityId() {
        if (isNoSelectionModel() && selectedEntityIdUiStateProvider != null) {
            return selectedEntityIdUiStateProvider.get();
        }

        return getSelectedEntity().map(ProxyIdentifiableEntity::getId);
    }

    /**
     * Selects the first row if available and enabled.
     */
    public boolean selectFirstRow() {
        if (isNoSelectionModel()) {
            return false;
        }

        final int size = grid.getDataCommunicator().getDataProviderSize();
        if (size > 0) {
            final T firstItem = grid.getDataCommunicator().fetchItemsWithRange(0, 1).get(0);

            if (firstItem != null) {
                grid.select(firstItem);

                return true;
            }
        }

        grid.deselectAll();

        return false;
    }

    public void select(final T itemToSelect) {
        if (isNoSelectionModel()) {
            return;
        }

        grid.select(itemToSelect);
    }

    public void selectEntityById(final Long entityId) {
        if (isNoSelectionModel()) {
            return;
        }

        if (mapIdToProxyEntityFunction == null || entityId == null) {
            return;
        }

        mapIdToProxyEntityFunction.apply(entityId).ifPresent(this::select);
    }

    public void selectAll() {
        if (!isMultiSelectionModel()) {
            return;
        }

        grid.asMultiSelect().selectAll();
    }

    public void deselect(final T itemToDeselect) {
        if (isNoSelectionModel()) {
            return;
        }

        grid.deselect(itemToDeselect);
    }

    /**
     * Clears the selection.
     */
    public void deselectAll() {
        if (isNoSelectionModel()) {
            return;
        }

        if (!getSelectedItems().isEmpty()) {
            grid.deselectAll();
        }
    }

    public void restoreSelection() {
        if (selectedEntityIdUiStateProvider == null) {
            return;
        }

        final Long lastSelectedEntityId = selectedEntityIdUiStateProvider.get().orElse(null);

        if (lastSelectedEntityId != null) {
            selectEntityById(lastSelectedEntityId);
        } else {
            selectFirstRow();
        }
    }
}
