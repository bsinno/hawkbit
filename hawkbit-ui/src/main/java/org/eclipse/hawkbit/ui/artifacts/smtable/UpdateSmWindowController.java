/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtable;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.builder.SoftwareModuleUpdate;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.EntityReadOnlyException;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowController;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowLayout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

public class UpdateSmWindowController extends AbstractEntityWindowController<ProxySoftwareModule, ProxySoftwareModule> {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final SoftwareModuleManagement smManagement;

    private final SmWindowLayout layout;

    public UpdateSmWindowController(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification, final SoftwareModuleManagement smManagement,
            final SmWindowLayout layout) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        this.smManagement = smManagement;

        this.layout = layout;
    }

    @Override
    public AbstractEntityWindowLayout<ProxySoftwareModule> getLayout() {
        return layout;
    }

    @Override
    protected ProxySoftwareModule buildEntityFromProxy(final ProxySoftwareModule proxyEntity) {
        final ProxySoftwareModule sm = new ProxySoftwareModule();

        sm.setId(proxyEntity.getId());
        sm.setProxyType(proxyEntity.getProxyType());
        sm.setName(proxyEntity.getName());
        sm.setVersion(proxyEntity.getVersion());
        sm.setVendor(proxyEntity.getVendor());
        sm.setDescription(proxyEntity.getDescription());

        return sm;
    }

    @Override
    protected void adaptLayout() {
        layout.disableSmTypeSelect();
        layout.disableNameField();
        layout.disableVersionField();
    }

    @Override
    protected void persistEntity(final ProxySoftwareModule entity) {
        final SoftwareModuleUpdate smUpdate = entityFactory.softwareModule().update(entity.getId())
                .vendor(entity.getVendor()).description(entity.getDescription());

        final SoftwareModule updatedSm;
        try {
            updatedSm = smManagement.update(smUpdate);
        } catch (final EntityNotFoundException | EntityReadOnlyException e) {
            // TODO: use i18n
            uiNotification.displayWarning("Software Module with name " + entity.getName()
                    + " was deleted or you are not allowed to update it");
            return;
        }

        uiNotification.displaySuccess(
                i18n.getMessage("message.update.success", updatedSm.getName() + ":" + updatedSm.getVersion()));
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                EntityModifiedEventType.ENTITY_UPDATED, ProxySoftwareModule.class, updatedSm.getId()));
    }

    @Override
    protected boolean isEntityValid(final ProxySoftwareModule entity) {
        if (!StringUtils.hasText(entity.getName()) || !StringUtils.hasText(entity.getVersion())
                || entity.getProxyType() == null) {
            // TODO: should we adapt message to include type?
            uiNotification.displayValidationError(i18n.getMessage("message.error.missing.nameorversion"));
            return false;
        }

        return true;
    }
}
