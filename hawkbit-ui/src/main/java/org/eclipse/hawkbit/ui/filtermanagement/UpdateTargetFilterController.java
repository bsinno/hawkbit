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
import org.eclipse.hawkbit.repository.builder.TargetFilterQueryUpdate;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.EntityReadOnlyException;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowController;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowLayout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.TargetFilterModifiedEventPayload;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

public class UpdateTargetFilterController
        extends AbstractEntityWindowController<ProxyTargetFilterQuery, ProxyTargetFilterQuery> {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final TargetFilterQueryManagement targetFilterManagement;

    private final TargetFilterAddUpdateLayout layout;

    private String nameBeforeEdit;

    public UpdateTargetFilterController(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification,
            final TargetFilterQueryManagement targetFilterManagement, final TargetFilterAddUpdateLayout layout) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        this.targetFilterManagement = targetFilterManagement;

        this.layout = layout;
    }

    @Override
    public AbstractEntityWindowLayout<ProxyTargetFilterQuery> getLayout() {
        return layout;
    }

    @Override
    protected ProxyTargetFilterQuery buildEntityFromProxy(final ProxyTargetFilterQuery proxyEntity) {
        final ProxyTargetFilterQuery target = new ProxyTargetFilterQuery();

        target.setId(proxyEntity.getId());
        target.setName(proxyEntity.getName());
        target.setQuery(proxyEntity.getQuery());

        nameBeforeEdit = proxyEntity.getName();

        return target;
    }

    @Override
    protected void persistEntity(final ProxyTargetFilterQuery entity) {
        final TargetFilterQueryUpdate targetFilterUpdate = entityFactory.targetFilterQuery().update(entity.getId())
                .name(entity.getName()).query(entity.getQuery());

        final TargetFilterQuery updatedTargetFilter;
        try {
            updatedTargetFilter = targetFilterManagement.update(targetFilterUpdate);
        } catch (final EntityNotFoundException | EntityReadOnlyException e) {
            // TODO: use i18n
            uiNotification.displayWarning(
                    "Target filter with name " + entity.getName() + " was deleted or you are not allowed to update it");
            return;
        }

        uiNotification.displaySuccess(i18n.getMessage("message.update.success", updatedTargetFilter.getName()));
        // TODO: verify if sender is correct
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new TargetFilterModifiedEventPayload(
                EntityModifiedEventType.ENTITY_UPDATED, updatedTargetFilter.getId()));
    }

    @Override
    protected boolean isEntityValid(final ProxyTargetFilterQuery entity) {
        if (!StringUtils.hasText(entity.getName())) {
            uiNotification.displayValidationError(i18n.getMessage("message.error.missing.filtername"));
            return false;
        }

        final String trimmedName = StringUtils.trimWhitespace(entity.getName());
        if (!nameBeforeEdit.equals(trimmedName) && targetFilterManagement.getByName(trimmedName).isPresent()) {
            // TODO: is the notification right here?
            uiNotification.displayValidationError(i18n.getMessage("message.target.filter.duplicate", trimmedName));
            return false;
        }

        return true;
    }
}
