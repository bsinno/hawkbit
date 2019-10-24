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
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowController;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowLayout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SmModifiedEventPayload;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

public class AddSmWindowController extends AbstractEntityWindowController<ProxySoftwareModule, ProxySoftwareModule> {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final SoftwareModuleManagement smManagement;

    private final SmWindowLayout layout;

    public AddSmWindowController(final VaadinMessageSource i18n, final EntityFactory entityFactory,
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
        // We ignore the method parameter, because we are interested in the
        // empty object, that we can populate with defaults
        return new ProxySoftwareModule();
    }

    @Override
    protected void persistEntity(final ProxySoftwareModule entity) {
        final SoftwareModule newSm = smManagement.create(
                entityFactory.softwareModule().create().type(entity.getProxyType().getKey()).name(entity.getName())
                        .version(entity.getVersion()).vendor(entity.getVendor()).description(entity.getDescription()));

        uiNotification.displaySuccess(i18n.getMessage("message.save.success", newSm.getName()));
        // TODO: verify if sender is correct
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new SmModifiedEventPayload(EntityModifiedEventType.ENTITY_ADDED, newSm.getId()));
    }

    @Override
    protected boolean isEntityValid(final ProxySoftwareModule entity) {
        if (!StringUtils.hasText(entity.getName()) || !StringUtils.hasText(entity.getVersion())
                || entity.getProxyType() == null) {
            // TODO: should we adapt message to include type?
            uiNotification.displayValidationError(i18n.getMessage("message.error.missing.nameorversion"));
            return false;
        }

        final String trimmedName = StringUtils.trimWhitespace(entity.getName());
        final String trimmedVersion = StringUtils.trimWhitespace(entity.getVersion());
        final Long typeId = entity.getProxyType().getId();
        if (smManagement.getByNameAndVersionAndType(trimmedName, trimmedVersion, typeId).isPresent()) {
            uiNotification.displayValidationError(
                    i18n.getMessage("message.duplicate.softwaremodule", trimmedName, trimmedVersion));
            return false;
        }

        return true;
    }
}
