/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.ConfirmationDialog;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyNamedEntity;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.ui.UI;

/**
 * Support for deleting the items in grid.
 * 
 * @param <T>
 *            The item-type used by the grid
 */
public class DeleteSupport<T extends ProxyNamedEntity> {
    private final AbstractGrid<T, ?> grid;
    private final VaadinMessageSource i18n;
    private final String entityType;
    private final SpPermissionChecker permissionChecker;
    private final UINotification notification;
    private final Consumer<Collection<Long>> itemIdsDeletionCallback;

    public DeleteSupport(final AbstractGrid<T, ?> grid, final VaadinMessageSource i18n, final String entityType,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final Consumer<Collection<Long>> itemIdsDeletionCallback) {
        this.grid = grid;
        this.i18n = i18n;
        this.entityType = entityType;
        this.permissionChecker = permissionChecker;
        this.notification = notification;
        this.itemIdsDeletionCallback = itemIdsDeletionCallback;
    }

    public void openConfirmationWindowDeleteAction(final T clickedItem) {
        final Set<T> itemsToBeDeleted = getItemsForDeletion(clickedItem);

        final String confirmationCaption = i18n.getMessage("caption.entity.delete.action.confirmbox", entityType);
        final String confirmationQuestion = createConfirmationQuestionForDeletion(itemsToBeDeleted);
        final ConfirmationDialog confirmDeleteDialog = createConfirmationWindowForDeletion(itemsToBeDeleted,
                confirmationCaption, confirmationQuestion);

        UI.getCurrent().addWindow(confirmDeleteDialog.getWindow());
        confirmDeleteDialog.getWindow().bringToFront();
    }

    private Set<T> getItemsForDeletion(final T clickedItem) {
        final Set<T> selectedItems = grid.getSelectedItems();

        // only clicked item should be deleted if it is not part of the
        // selection
        if (selectedItems.contains(clickedItem)) {
            return selectedItems;
        } else {
            grid.getSelectionSupport().clearSelection();
            grid.select(clickedItem);

            return Collections.singleton(clickedItem);
        }
    }

    private String createConfirmationQuestionForDeletion(final Set<T> itemsToBeDeleted) {
        if (itemsToBeDeleted.size() == 1) {
            return i18n.getMessage(UIMessageIdProvider.MESSAGE_CONFIRM_DELETE_ENTITY, entityType.toLowerCase(),
                    itemsToBeDeleted.iterator().next().getName(), "");
        } else {
            return i18n.getMessage(UIMessageIdProvider.MESSAGE_CONFIRM_DELETE_ENTITY, itemsToBeDeleted.size(),
                    entityType, "s");
        }
    }

    private ConfirmationDialog createConfirmationWindowForDeletion(final Set<T> itemsToBeDeleted,
            final String confirmationCaption, final String confirmationQuestion) {
        return new ConfirmationDialog(confirmationCaption, confirmationQuestion,
                i18n.getMessage(UIMessageIdProvider.BUTTON_OK), i18n.getMessage(UIMessageIdProvider.BUTTON_CANCEL),
                ok -> {
                    if (ok) {
                        handleOkDelete(itemsToBeDeleted);
                    }
                });
    }

    private void handleOkDelete(final Set<T> itemsToBeDeleted) {
        final Collection<Long> itemsToBeDeletedIds = itemsToBeDeleted.stream().map(ProxyIdentifiableEntity::getId)
                .collect(Collectors.toList());

        itemIdsDeletionCallback.accept(itemsToBeDeletedIds);

        notification.displaySuccess(
                i18n.getMessage("message.delete.success", itemsToBeDeletedIds.size() + " " + entityType + "(s)"));
    }

    public boolean hasDeletePermission() {
        return permissionChecker.hasDeleteRepositoryPermission() || permissionChecker.hasDeleteTargetPermission();
    }
}
