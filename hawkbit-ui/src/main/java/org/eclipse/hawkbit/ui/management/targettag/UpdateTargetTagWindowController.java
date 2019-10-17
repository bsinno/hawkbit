/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.repository.builder.TagUpdate;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.EntityReadOnlyException;
import org.eclipse.hawkbit.repository.model.TargetTag;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.TargetTagModifiedEventPayload;
import org.eclipse.hawkbit.ui.management.tag.AbstractTagWindowLayout;
import org.eclipse.hawkbit.ui.management.tag.TagWindowController;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

public class UpdateTargetTagWindowController implements TagWindowController {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final TargetTagManagement targetTagManagement;

    private final UpdateTargetTagWindowLayout layout;

    private ProxyTag tag;
    private String tagNameBeforeEdit;

    public UpdateTargetTagWindowController(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification,
            final TargetTagManagement targetTagManagement, final UpdateTargetTagWindowLayout layout) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        this.targetTagManagement = targetTagManagement;

        this.layout = layout;
    }

    @Override
    public AbstractTagWindowLayout getLayout() {
        return layout;
    }

    @Override
    public void populateWithData(final ProxyTag proxyTag) {
        tag = new ProxyTag();

        tag.setId(proxyTag.getId());
        tag.setName(proxyTag.getName());
        tag.setDescription(proxyTag.getDescription());
        tag.setColour(StringUtils.hasText(proxyTag.getColour()) ? proxyTag.getColour() : "#2c9720");

        layout.getProxyTagBinder().setBean(tag);
    }

    @Override
    public SaveDialogCloseListener getSaveDialogCloseListener() {
        return new SaveDialogCloseListener() {
            @Override
            public void saveOrUpdate() {
                editTag();
            }

            @Override
            public boolean canWindowSaveOrUpdate() {
                return duplicateCheckForEdit();
            }
        };
    }

    private void editTag() {
        if (tag == null) {
            return;
        }

        final TagUpdate tagUpdate = entityFactory.tag().update(tag.getId()).name(tag.getName())
                .description(tag.getDescription()).colour(tag.getColour());

        TargetTag updatedTag;
        try {
            updatedTag = targetTagManagement.update(tagUpdate);
        } catch (final EntityNotFoundException | EntityReadOnlyException e) {
            // TODO: use i18n
            uiNotification.displayWarning(
                    "Tag with name " + tag.getName() + " was deleted or you are not allowed to update it");
            return;
        }

        uiNotification.displaySuccess(i18n.getMessage("message.update.success", updatedTag.getName()));
        // TODO: verify if sender is correct
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new TargetTagModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, updatedTag.getId()));
    }

    private boolean duplicateCheckForEdit() {
        if (!StringUtils.hasText(tag.getName())) {
            uiNotification.displayValidationError(i18n.getMessage("message.error.missing.tagname"));
            return false;
        }
        if (!tagNameBeforeEdit.equals(getTrimmedTagName())
                && targetTagManagement.getByName(getTrimmedTagName()).isPresent()) {
            // TODO: is the notification right here?
            uiNotification.displayValidationError(i18n.getMessage("message.tag.duplicate.check", getTrimmedTagName()));
            return false;
        }
        return true;
    }

    private String getTrimmedTagName() {
        return StringUtils.trimWhitespace(tag.getName());
    }
}
