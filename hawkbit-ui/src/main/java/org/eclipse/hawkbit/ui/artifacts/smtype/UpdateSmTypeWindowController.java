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
import org.eclipse.hawkbit.repository.builder.SoftwareModuleTypeUpdate;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.EntityReadOnlyException;
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

public class UpdateSmTypeWindowController implements TypeWindowController {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final SoftwareModuleTypeManagement smTypeManagement;

    private final SmTypeWindowLayout layout;

    private ProxyType type;
    private String typeNameBeforeEdit;
    private String typeKeyBeforeEdit;

    public UpdateSmTypeWindowController(final VaadinMessageSource i18n, final EntityFactory entityFactory,
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
        type = new ProxyType();

        type.setId(proxyType.getId());
        type.setName(proxyType.getName());
        type.setDescription(proxyType.getDescription());
        type.setColour(StringUtils.hasText(proxyType.getColour()) ? proxyType.getColour() : "#2c9720");
        type.setKey(proxyType.getKey());
        type.setSmTypeAssign(getSmTypeAssignById(proxyType.getId()));

        typeNameBeforeEdit = proxyType.getName();
        typeKeyBeforeEdit = proxyType.getKey();

        layout.getBinder().setBean(type);
        layout.disableTagName();
        layout.disableTypeKey();
        layout.disableTypeAssignOptionGroup();
    }

    private SmTypeAssign getSmTypeAssignById(final Long id) {
        return smTypeManagement.get(id)
                .map(smType -> smType.getMaxAssignments() == 1 ? SmTypeAssign.SINGLE : SmTypeAssign.MULTI)
                .orElse(SmTypeAssign.SINGLE);
    }

    @Override
    public SaveDialogCloseListener getSaveDialogCloseListener() {
        return new SaveDialogCloseListener() {
            @Override
            public void saveOrUpdate() {
                editType();
            }

            @Override
            public boolean canWindowSaveOrUpdate() {
                return duplicateCheckForEdit();
            }
        };
    }

    private void editType() {
        if (type == null) {
            return;
        }

        final SoftwareModuleTypeUpdate smTypeUpdate = entityFactory.softwareModuleType().update(type.getId())
                .description(type.getDescription()).colour(type.getColour());

        final SoftwareModuleType updatedSmType;
        try {
            updatedSmType = smTypeManagement.update(smTypeUpdate);
        } catch (final EntityNotFoundException | EntityReadOnlyException e) {
            // TODO: use i18n
            uiNotification.displayWarning(
                    "Type with name " + type.getName() + " was deleted or you are not allowed to update it");
            return;
        }

        uiNotification.displaySuccess(i18n.getMessage("message.update.success", updatedSmType.getName()));
        // TODO: verify if sender is correct
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new SmTypeModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, updatedSmType.getId()));
    }

    private boolean duplicateCheckForEdit() {
        if (!StringUtils.hasText(type.getName()) || !StringUtils.hasText(type.getKey())) {
            uiNotification.displayValidationError(i18n.getMessage("message.error.missing.typenameorkey"));
            return false;
        }
        if (!typeNameBeforeEdit.equals(getTrimmedTypeName())
                && smTypeManagement.getByName(getTrimmedTypeName()).isPresent()) {
            // TODO: is the notification right here?
            uiNotification.displayValidationError(i18n.getMessage("message.tag.duplicate.check", getTrimmedTypeName()));
            return false;
        }
        if (!typeKeyBeforeEdit.equals(getTrimmedTypeKey())
                && smTypeManagement.getByKey(getTrimmedTypeKey()).isPresent()) {
            // TODO: is the notification right here?
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
