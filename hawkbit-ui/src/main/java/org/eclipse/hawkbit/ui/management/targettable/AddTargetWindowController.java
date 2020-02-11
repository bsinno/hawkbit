/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowController;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowLayout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

public class AddTargetWindowController extends AbstractEntityWindowController<ProxyTarget, ProxyTarget> {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final TargetManagement targetManagement;

    private final TargetWindowLayout layout;

    public AddTargetWindowController(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification, final TargetManagement targetManagement,
            final TargetWindowLayout layout) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        this.targetManagement = targetManagement;

        this.layout = layout;
    }

    @Override
    public AbstractEntityWindowLayout<ProxyTarget> getLayout() {
        return layout;
    }

    @Override
    protected ProxyTarget buildEntityFromProxy(final ProxyTarget proxyEntity) {
        // We ignore the method parameter, because we are interested in the
        // empty object, that we can populate with defaults
        return new ProxyTarget();
    }

    @Override
    protected void persistEntity(final ProxyTarget entity) {
        final Target newTarget = targetManagement.create(entityFactory.target().create()
                .controllerId(entity.getControllerId()).name(entity.getName()).description(entity.getDescription()));

        uiNotification.displaySuccess(i18n.getMessage("message.save.success", newTarget.getName()));
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                EntityModifiedEventType.ENTITY_ADDED, ProxyTarget.class, newTarget.getId()));
    }

    @Override
    protected boolean isEntityValid(final ProxyTarget entity) {
        if (!StringUtils.hasText(entity.getControllerId())) {
            uiNotification.displayValidationError(i18n.getMessage("message.error.missing.controllerId"));
            return false;
        }

        final String trimmedControllerId = StringUtils.trimWhitespace(entity.getControllerId());
        if (targetManagement.getByControllerID(trimmedControllerId).isPresent()) {
            uiNotification
                    .displayValidationError(i18n.getMessage("message.target.duplicate.check", trimmedControllerId));
            return false;
        }

        return true;
    }
}
