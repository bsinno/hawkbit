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
import java.util.function.Function;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.ConfirmationDialog;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.ui.Grid;
import com.vaadin.ui.UI;

/**
 * Support for deleting the items in grid.
 * 
 * @param <T>
 *            The item-type used by the grid
 */
public class DeleteSupport<T> {
    private final Grid<T> grid;
    private final VaadinMessageSource i18n;
    private final String entityType;
    private final SpPermissionChecker permissionChecker;
    private final UINotification notification;
    private final Consumer<Collection<T>> itemsDeletionCallback;
    private final String deletionWindowId;
    private final Function<T, String> entityNameGenerator;

    public DeleteSupport(final Grid<T> grid, final VaadinMessageSource i18n, final String entityType,
            final Function<T, String> entityNameGenerator, final SpPermissionChecker permissionChecker,
            final UINotification notification, final Consumer<Collection<T>> itemsDeletionCallback,
            final String deletionWindowId) {
        this.grid = grid;
        this.i18n = i18n;
        this.entityType = entityType;
        this.entityNameGenerator = entityNameGenerator;
        this.permissionChecker = permissionChecker;
        this.notification = notification;
        this.itemsDeletionCallback = itemsDeletionCallback;
        this.deletionWindowId = deletionWindowId;
    }

    public void openConfirmationWindowDeleteAction(final T clickedItem) {
        final Set<T> itemsToBeDeleted = getItemsForDeletion(clickedItem);
        final int itemsToBeDeletedSize = itemsToBeDeleted.size();

        final String clickedItemName = entityNameGenerator.apply(clickedItem);
        final String confirmationCaption = i18n.getMessage("caption.entity.delete.action.confirmbox", entityType);

        final String confirmationQuestion = createDeletionText(UIMessageIdProvider.MESSAGE_CONFIRM_DELETE_ENTITY,
                itemsToBeDeletedSize, clickedItemName);
        final String successNotificationText = createDeletionText("message.delete.success", itemsToBeDeletedSize,
                clickedItemName);

        final ConfirmationDialog confirmDeleteDialog = createConfirmationWindowForDeletion(itemsToBeDeleted,
                confirmationCaption, confirmationQuestion, successNotificationText);

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
            grid.deselectAll();
            grid.select(clickedItem);

            return Collections.singleton(clickedItem);
        }
    }

    private String createDeletionText(final String messageId, final int itemsToBeDeletedSize,
            final String clickedItemName) {
        if (itemsToBeDeletedSize == 1) {
            return i18n.getMessage(messageId, entityType, clickedItemName, "");
        } else {
            return i18n.getMessage(messageId, itemsToBeDeletedSize, entityType, "s");
        }
    }

    private ConfirmationDialog createConfirmationWindowForDeletion(final Set<T> itemsToBeDeleted,
            final String confirmationCaption, final String confirmationQuestion, final String successNotificationText) {
        return new ConfirmationDialog(confirmationCaption, confirmationQuestion,
                i18n.getMessage(UIMessageIdProvider.BUTTON_OK), i18n.getMessage(UIMessageIdProvider.BUTTON_CANCEL),
                ok -> {
                    if (ok) {
                        handleOkDelete(itemsToBeDeleted, successNotificationText);
                    }
                }, deletionWindowId);
    }

    private void handleOkDelete(final Set<T> itemsToBeDeleted, final String successNotificationText) {
        grid.deselectAll();
        // TODO: should we catch the exception here?
        itemsDeletionCallback.accept(itemsToBeDeleted);

        notification.displaySuccess(successNotificationText);
    }

    // TODO: check if it should be passed as a parameter
    public boolean hasDeletePermission() {
        return permissionChecker.hasDeleteRepositoryPermission() || permissionChecker.hasDeleteTargetPermission();
    }
}
