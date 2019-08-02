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

import org.eclipse.hawkbit.ui.common.ConfirmationDialog;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyNamedEntity;
import org.eclipse.hawkbit.ui.management.miscs.DeploymentAssignmentWindowController;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

/**
 * Support for assigning the {@link ProxyNamedEntity} items between two grids
 * (targets to distribution set or distribution sets to target).
 * 
 * @param <S>
 *            The item-type of source items
 * @param <T>
 *            The item-type of target item
 */
public abstract class DeploymentAssignmentSupport<S extends ProxyNamedEntity, T extends ProxyNamedEntity>
        extends AssignmentSupport<S, T> {
    protected final DeploymentAssignmentWindowController assignmentController;

    protected DeploymentAssignmentSupport(final UINotification notification, final VaadinMessageSource i18n,
            final DeploymentAssignmentWindowController assignmentController) {
        super(notification, i18n);

        this.assignmentController = assignmentController;
    }

    // similar to RolloutWindowBuilder
    protected void openConfirmationWindowForAssignments(final List<S> sourceItemsToAssign, final T targetItem,
            final Runnable assignmentExecutor) {
        assignmentController.populateWithData();

        final String confirmationMessage = getConfirmationMessageForAssignments(sourceItemsToAssign, targetItem);
        final ConfirmationDialog confirmAssignDialog = createConfirmationWindow(confirmationMessage,
                assignmentController.getLayout(), assignmentExecutor);

        assignmentController.getLayout().addValidationListener(confirmAssignDialog::setOkButtonEnabled);

        UI.getCurrent().addWindow(confirmAssignDialog.getWindow());
        confirmAssignDialog.getWindow().bringToFront();
    }

    private String getConfirmationMessageForAssignments(final List<S> sourceItemsToAssign, final T targetItem) {
        final int sourceItemsToAssignCount = sourceItemsToAssign.size();
        final String targetItemName = targetItem.getName();

        if (sourceItemsToAssignCount > 1) {
            return i18n.getMessage(UIMessageIdProvider.MESSAGE_CONFIRM_ASSIGN_MULTIPLE_ENTITIES_TO_ENTITY,
                    sourceItemsToAssignCount, sourceEntityType(), targetEntityType(), targetItemName);
        }

        return i18n.getMessage(UIMessageIdProvider.MESSAGE_CONFIRM_ASSIGN_MULTIPLE_ENTITIES_TO_ENTITY,
                sourceEntityType(), sourceItemsToAssign.get(0).getName(), targetEntityType(), targetItemName);
    }

    private ConfirmationDialog createConfirmationWindow(final String confirmationMessage, final Component content,
            final Runnable assignmentExecutor) {
        final String caption = i18n.getMessage(UIMessageIdProvider.CAPTION_ENTITY_ASSIGN_ACTION_CONFIRMBOX);
        final String okLabelCaption = i18n.getMessage(UIMessageIdProvider.BUTTON_OK);
        final String cancelLabelCaption = i18n.getMessage(UIMessageIdProvider.BUTTON_CANCEL);

        return new ConfirmationDialog(caption, confirmationMessage, okLabelCaption, cancelLabelCaption, ok -> {
            if (ok && assignmentController.isMaintenanceWindowValid()) {
                assignmentExecutor.run();
            }
        }, content, UIComponentIdProvider.DIST_SET_TO_TARGET_ASSIGNMENT_CONFIRM_ID);
    }

    protected abstract String sourceEntityType();

    protected abstract String targetEntityType();
}
