/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowController;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowLayout;
import org.eclipse.hawkbit.ui.common.ConfirmationDialog;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.TargetFilterModifiedEventPayload;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.UI;

public class AutoAssignmentWindowController
        extends AbstractEntityWindowController<ProxyTargetFilterQuery, ProxyTargetFilterQuery> {
    private final VaadinMessageSource i18n;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;
    private final EntityFactory entityFactory;

    private final TargetManagement targetManagement;
    private final TargetFilterQueryManagement targetFilterQueryManagement;

    private final AutoAssignmentWindowLayout layout;

    public AutoAssignmentWindowController(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final UINotification uiNotification, final EntityFactory entityFactory,
            final TargetManagement targetManagement, final TargetFilterQueryManagement targetFilterQueryManagement,
            final AutoAssignmentWindowLayout layout) {
        this.i18n = i18n;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;
        this.entityFactory = entityFactory;

        this.targetManagement = targetManagement;
        this.targetFilterQueryManagement = targetFilterQueryManagement;

        this.layout = layout;
    }

    @Override
    public AbstractEntityWindowLayout<ProxyTargetFilterQuery> getLayout() {
        return layout;
    }

    @Override
    protected ProxyTargetFilterQuery buildEntityFromProxy(final ProxyTargetFilterQuery proxyEntity) {
        final ProxyTargetFilterQuery autoAssignmentFilter = new ProxyTargetFilterQuery();

        autoAssignmentFilter.setId(proxyEntity.getId());
        autoAssignmentFilter.setQuery(proxyEntity.getQuery());

        if (proxyEntity.getAutoAssignDistributionSet() != null) {
            autoAssignmentFilter.setAutoAssignmentEnabled(true);
            autoAssignmentFilter.setAutoAssignActionType(proxyEntity.getAutoAssignActionType());
            autoAssignmentFilter.setAutoAssignDistributionSet(proxyEntity.getAutoAssignDistributionSet());
        } else {
            autoAssignmentFilter.setAutoAssignmentEnabled(false);
            autoAssignmentFilter.setAutoAssignActionType(ActionType.FORCED);
            autoAssignmentFilter.setAutoAssignDistributionSet(null);
        }

        return autoAssignmentFilter;
    }

    @Override
    protected void adaptLayout() {
        layout.switchAutoAssignmentInputsVisibility(layout.getEntity().isAutoAssignmentEnabled());
    }

    @Override
    protected void persistEntity(final ProxyTargetFilterQuery entity) {
        if (entity.isAutoAssignmentEnabled()) {
            final Long autoAssignDsId = entity.getAutoAssignDistributionSet().getId();
            final Long targetsForAutoAssignmentCount = targetManagement.countByRsqlAndNonDS(autoAssignDsId,
                    entity.getQuery());

            final String confirmationCaption = i18n
                    .getMessage(UIMessageIdProvider.CAPTION_CONFIRM_AUTO_ASSIGN_CONSEQUENCES);
            final String confirmationQuestion = targetsForAutoAssignmentCount == 0
                    ? i18n.getMessage(UIMessageIdProvider.MESSAGE_CONFIRM_AUTO_ASSIGN_CONSEQUENCES_NONE)
                    : i18n.getMessage(UIMessageIdProvider.MESSAGE_CONFIRM_AUTO_ASSIGN_CONSEQUENCES_TEXT,
                            targetsForAutoAssignmentCount);

            showConsequencesDialog(confirmationCaption, confirmationQuestion, entity.getId(), autoAssignDsId,
                    entity.getAutoAssignActionType(), entity);
        } else {
            targetFilterQueryManagement
                    .updateAutoAssignDS(entityFactory.targetFilterQuery().updateAutoAssign(entity.getId()).ds(null));
            publishModifiedEvent(entity.getId());
        }
    }

    private void showConsequencesDialog(final String confirmationCaption, final String confirmationQuestion,
            final Long targetFilterId, final Long autoAssignDsId, final ActionType autoAssignActionType,
            final ProxyTargetFilterQuery entity) {
        final ConfirmationDialog confirmDialog = new ConfirmationDialog(confirmationCaption, confirmationQuestion,
                i18n.getMessage(UIMessageIdProvider.BUTTON_OK), i18n.getMessage(UIMessageIdProvider.BUTTON_CANCEL),
                ok -> {
                    if (ok) {
                        targetFilterQueryManagement.updateAutoAssignDS(entityFactory.targetFilterQuery()
                                .updateAutoAssign(targetFilterId).ds(autoAssignDsId).actionType(autoAssignActionType));
                        publishModifiedEvent(entity.getId());
                    }
                }, UIComponentIdProvider.DIST_SET_SELECT_CONS_WINDOW_ID);

        confirmDialog.getWindow().setWidth(40.0F, Sizeable.Unit.PERCENTAGE);

        UI.getCurrent().addWindow(confirmDialog.getWindow());
        confirmDialog.getWindow().bringToFront();
    }

    private void publishModifiedEvent(final Long entityId) {
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new TargetFilterModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, entityId));
    }

    @Override
    protected boolean isEntityValid(final ProxyTargetFilterQuery entity) {
        if (entity.isAutoAssignmentEnabled()
                && (entity.getAutoAssignActionType() == null || entity.getAutoAssignDistributionSet() == null)) {
            uiNotification.displayValidationError(
                    i18n.getMessage(UIMessageIdProvider.MESSAGE_AUTOASSIGN_CREATE_ERROR_MISSINGELEMENTS));
            return false;
        }

        return true;
    }
}
