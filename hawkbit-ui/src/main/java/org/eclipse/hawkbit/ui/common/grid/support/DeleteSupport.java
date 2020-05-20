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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.ui.common.ConfirmationDialog;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.vaadin.ui.Grid;
import com.vaadin.ui.UI;

/**
 * Support for deleting the items in grid.
 * 
 * @param <T>
 *            The item-type used by the grid
 */
public class DeleteSupport<T extends ProxyIdentifiableEntity> {
    private static final Logger LOG = LoggerFactory.getLogger(DeleteSupport.class);

    private final Grid<T> grid;
    private final VaadinMessageSource i18n;
    private final String entityType;
    private final UINotification notification;
    private final Predicate<Collection<T>> itemsDeletionCallback;
    private final String deletionWindowId;
    private final Function<T, String> entityNameGenerator;

    private Function<T, String> confirmationQuestionDetailsGenerator;

    public DeleteSupport(final Grid<T> grid, final VaadinMessageSource i18n, final UINotification notification,
            final String entityType, final Function<T, String> entityNameGenerator,
            final Predicate<Collection<T>> itemsDeletionCallback, final String deletionWindowId) {
        this.grid = grid;
        this.i18n = i18n;
        this.entityType = entityType;
        this.entityNameGenerator = entityNameGenerator;
        this.notification = notification;
        this.itemsDeletionCallback = itemsDeletionCallback;
        this.deletionWindowId = deletionWindowId;
    }

    public void openConfirmationWindowDeleteAction(final T clickedItem) {
        final Set<T> itemsToBeDeleted = getItemsForDeletion(clickedItem);
        final int itemsToBeDeletedSize = itemsToBeDeleted.size();

        final String clickedItemName = entityNameGenerator.apply(clickedItem);
        final String confirmationCaption = i18n.getMessage("caption.entity.delete.action.confirmbox", entityType);

        final StringBuilder confirmationQuestionBuilder = new StringBuilder();
        confirmationQuestionBuilder.append(createDeletionText(UIMessageIdProvider.MESSAGE_CONFIRM_DELETE_ENTITY,
                itemsToBeDeletedSize, clickedItemName));
        if (confirmationQuestionDetailsGenerator != null) {
            final String confirmationQuestionDetails = confirmationQuestionDetailsGenerator.apply(clickedItem);
            if (!StringUtils.isEmpty(confirmationQuestionDetails)) {
                confirmationQuestionBuilder.append("\n");
                confirmationQuestionBuilder.append(confirmationQuestionDetails);
            }
        }

        final String successNotificationText = createDeletionText("message.delete.success", itemsToBeDeletedSize,
                clickedItemName);
        final String failureNotificationText = createDeletionText("message.delete.fail", itemsToBeDeletedSize,
                clickedItemName);

        final ConfirmationDialog confirmDeleteDialog = createConfirmationWindowForDeletion(itemsToBeDeleted,
                confirmationCaption, confirmationQuestionBuilder.toString(), successNotificationText,
                failureNotificationText);

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
            final String confirmationCaption, final String confirmationQuestion, final String successNotificationText,
            final String failureNotificationText) {
        return new ConfirmationDialog(confirmationCaption, confirmationQuestion,
                i18n.getMessage(UIMessageIdProvider.BUTTON_OK), i18n.getMessage(UIMessageIdProvider.BUTTON_CANCEL),
                ok -> {
                    if (ok) {
                        handleOkDelete(itemsToBeDeleted, successNotificationText, failureNotificationText);
                    }
                }, deletionWindowId);
    }

    private void handleOkDelete(final Set<T> itemsToBeDeleted, final String successNotificationText,
            final String failureNotificationText) {
        grid.deselectAll();

        boolean isDeletionSuccessfull = false;
        try {
            isDeletionSuccessfull = itemsDeletionCallback.test(itemsToBeDeleted);
        } catch (final RuntimeException ex) {
            final String itemsToBeDeletedIds = itemsToBeDeleted.stream().map(ProxyIdentifiableEntity::getId)
                    .map(String::valueOf).collect(Collectors.joining(","));
            LOG.warn("Deletion of {} with ids '{}' failed: {}", entityType, itemsToBeDeletedIds, ex.getMessage());
        }

        if (isDeletionSuccessfull) {
            notification.displaySuccess(successNotificationText);
        } else {
            notification.displayWarning(failureNotificationText);
        }
    }

    public void setConfirmationQuestionDetailsGenerator(
            final Function<T, String> confirmationQuestionDetailsGenerator) {
        this.confirmationQuestionDetailsGenerator = confirmationQuestionDetailsGenerator;
    }
}
