/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtype;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType.SmTypeAssign;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SmTypeModifiedEventPayload;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

public class AddSmTypeWindowController implements TypeWindowController {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final SoftwareModuleTypeManagement smTypeManagement;

    private final SmTypeWindowLayout layout;

    private ProxyType type;

    public AddSmTypeWindowController(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification,
            final SoftwareModuleTypeManagement smTypeManagement, final SmTypeWindowLayout layout) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        this.smTypeManagement = smTypeManagement;

        this.layout = layout;
    }

    @Override
    public SmTypeWindowLayout getLayout() {
        return layout;
    }

    @Override
    public void populateWithData(final ProxyType proxyType) {
        // We ignore the method parameter, because we are interested in the
        // empty object, that we can populate with defaults
        type = new ProxyType();
        // TODO: either extract the constant, or define it as a default in model
        type.setColour("#2c9720");
        type.setSmTypeAssign(SmTypeAssign.SINGLE);

        layout.getBinder().setBean(type);
    }

    @Override
    public SaveDialogCloseListener getSaveDialogCloseListener() {
        return new SaveDialogCloseListener() {
            @Override
            public void saveOrUpdate() {
                saveSmType();
            }

            @Override
            public boolean canWindowSaveOrUpdate() {
                return duplicateCheck();
            }
        };
    }

    private void saveSmType() {
        final int assignNumber = type.getSmTypeAssign() == SmTypeAssign.SINGLE ? 1 : Integer.MAX_VALUE;

        final SoftwareModuleType newSmType = smTypeManagement
                .create(entityFactory.softwareModuleType().create().key(type.getKey()).name(type.getName())
                        .description(type.getDescription()).colour(type.getColour()).maxAssignments(assignNumber));

        uiNotification.displaySuccess(i18n.getMessage("message.save.success", newSmType.getName()));
        // TODO: verify if sender is correct
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new SmTypeModifiedEventPayload(EntityModifiedEventType.ENTITY_ADDED, newSmType.getId()));
    }

    private boolean duplicateCheck() {
        if (!StringUtils.hasText(type.getName()) || !StringUtils.hasText(type.getKey())) {
            uiNotification.displayValidationError(i18n.getMessage("message.error.missing.typenameorkey"));
            return false;
        }
        // TODO: check if this is correct
        if (smTypeManagement.getByName(getTrimmedTypeName()).isPresent()) {
            uiNotification.displayValidationError(i18n.getMessage("message.tag.duplicate.check", getTrimmedTypeName()));
            return false;
        }
        if (smTypeManagement.getByKey(getTrimmedTypeKey()).isPresent()) {
            uiNotification.displayValidationError(
                    i18n.getMessage("message.type.key.swmodule.duplicate.check", getTrimmedTypeKey()));
            return false;
        }
        return true;
    }

    private String getTrimmedTypeName() {
        return StringUtils.trimWhitespace(type.getName());
    }

    private String getTrimmedTypeKey() {
        return StringUtils.trimWhitespace(type.getKey());
    }
}
