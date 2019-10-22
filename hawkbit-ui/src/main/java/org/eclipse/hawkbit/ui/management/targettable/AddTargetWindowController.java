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
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.TargetModifiedEventPayload;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

public class AddTargetWindowController implements TargetWindowController {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final TargetManagement targetManagement;

    private final TargetWindowLayout layout;

    private ProxyTarget target;

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
    public TargetWindowLayout getLayout() {
        return layout;
    }

    @Override
    public void populateWithData(final ProxyTarget proxyTarget) {
        // We ignore the method parameter, because we are interested in the
        // empty object, that we can populate with defaults
        target = new ProxyTarget();

        layout.getBinder().setBean(target);
    }

    @Override
    public SaveDialogCloseListener getSaveDialogCloseListener() {
        return new SaveDialogCloseListener() {
            @Override
            public void saveOrUpdate() {
                saveTarget();
            }

            @Override
            public boolean canWindowSaveOrUpdate() {
                return duplicateCheck();
            }
        };
    }

    private void saveTarget() {
        final Target newTarget = targetManagement.create(entityFactory.target().create()
                .controllerId(target.getControllerId()).name(target.getName()).description(target.getDescription()));

        uiNotification.displaySuccess(i18n.getMessage("message.save.success", newTarget.getName()));
        // TODO: verify if sender is correct
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new TargetModifiedEventPayload(EntityModifiedEventType.ENTITY_ADDED, newTarget.getId()));
    }

    private boolean duplicateCheck() {
        if (targetManagement.getByControllerID(getTrimmedTargetControllerId()).isPresent()) {
            uiNotification.displayValidationError(
                    i18n.getMessage("message.target.duplicate.check", getTrimmedTargetControllerId()));
            return false;
        }
        return true;
    }

    private String getTrimmedTargetControllerId() {
        return StringUtils.trimWhitespace(target.getControllerId());
    }
}
