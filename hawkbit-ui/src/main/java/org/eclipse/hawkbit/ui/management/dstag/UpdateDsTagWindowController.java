/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstag;

import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.builder.TagUpdate;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.EntityReadOnlyException;
import org.eclipse.hawkbit.repository.model.DistributionSetTag;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.DsTagModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.management.tag.TagWindowController;
import org.eclipse.hawkbit.ui.management.tag.TagWindowLayout;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

//TODO: remove duplication with target tag
public class UpdateDsTagWindowController implements TagWindowController {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final DistributionSetTagManagement dsTagManagement;

    private final TagWindowLayout<ProxyTag> layout;

    private ProxyTag tag;
    private String tagNameBeforeEdit;

    public UpdateDsTagWindowController(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification,
            final DistributionSetTagManagement dsTagManagement, final TagWindowLayout<ProxyTag> layout) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        this.dsTagManagement = dsTagManagement;

        this.layout = layout;
    }

    @Override
    public TagWindowLayout<ProxyTag> getLayout() {
        return layout;
    }

    @Override
    public void populateWithData(final ProxyTag proxyTag) {
        tag = new ProxyTag();

        tag.setId(proxyTag.getId());
        tag.setName(proxyTag.getName());
        tag.setDescription(proxyTag.getDescription());
        tag.setColour(StringUtils.hasText(proxyTag.getColour()) ? proxyTag.getColour() : "#2c9720");

        tagNameBeforeEdit = proxyTag.getName();

        layout.getBinder().setBean(tag);
        layout.disableTagName();
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

        DistributionSetTag updatedTag;
        try {
            updatedTag = dsTagManagement.update(tagUpdate);
        } catch (final EntityNotFoundException | EntityReadOnlyException e) {
            // TODO: use i18n
            uiNotification.displayWarning(
                    "Tag with name " + tag.getName() + " was deleted or you are not allowed to update it");
            return;
        }

        uiNotification.displaySuccess(i18n.getMessage("message.update.success", updatedTag.getName()));
        // TODO: verify if sender is correct
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new DsTagModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, updatedTag.getId()));
    }

    private boolean duplicateCheckForEdit() {
        if (!StringUtils.hasText(tag.getName())) {
            uiNotification.displayValidationError(i18n.getMessage("message.error.missing.tagname"));
            return false;
        }
        if (!tagNameBeforeEdit.equals(getTrimmedTagName())
                && dsTagManagement.getByName(getTrimmedTagName()).isPresent()) {
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
