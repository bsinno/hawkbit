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
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowController;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowLayout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.management.tag.TagWindowLayout;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Controller for update distribution set tag window
 */
//TODO: remove duplication with UpdateTargetTagWindowController
public class UpdateDsTagWindowController extends AbstractEntityWindowController<ProxyTag, ProxyTag> {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateDsTagWindowController.class);

    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final DistributionSetTagManagement dsTagManagement;

    private final TagWindowLayout<ProxyTag> layout;

    private String nameBeforeEdit;

    /**
     * Constructor for UpdateDsTagWindowController
     *
     * @param i18n
     *          VaadinMessageSource
     * @param entityFactory
     *          EntityFactory
     * @param eventBus
     *          UIEventBus
     * @param uiNotification
     *          UINotification
     * @param dsTagManagement
     *          DistributionSetTagManagement
     * @param layout
     *          Tag window layout
     */
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
    public AbstractEntityWindowLayout<ProxyTag> getLayout() {
        return layout;
    }

    @Override
    protected ProxyTag buildEntityFromProxy(final ProxyTag proxyEntity) {
        final ProxyTag dsTag = new ProxyTag();

        dsTag.setId(proxyEntity.getId());
        dsTag.setName(proxyEntity.getName());
        dsTag.setDescription(proxyEntity.getDescription());
        dsTag.setColour(proxyEntity.getColour());

        nameBeforeEdit = proxyEntity.getName();

        return dsTag;
    }

    @Override
    protected void adaptLayout(final ProxyTag proxyEntity) {
        layout.disableTagName();
    }

    @Override
    protected void persistEntity(final ProxyTag entity) {
        final TagUpdate tagUpdate = entityFactory.tag().update(entity.getId()).name(entity.getName())
                .description(entity.getDescription()).colour(entity.getColour());

        DistributionSetTag updatedTag;
        try {
            updatedTag = dsTagManagement.update(tagUpdate);
        } catch (final EntityNotFoundException | EntityReadOnlyException e) {
            LOG.trace("Update of DS tag failed in UI: {}", e.getMessage());
            // TODO: use i18n
            uiNotification.displayWarning(
                    "Tag with name " + entity.getName() + " was deleted or you are not allowed to update it");
            return;
        }

        uiNotification.displaySuccess(i18n.getMessage("message.update.success", updatedTag.getName()));
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new EntityModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, ProxyDistributionSet.class,
                        ProxyTag.class, updatedTag.getId()));
    }

    @Override
    protected boolean isEntityValid(final ProxyTag entity) {
        if (!StringUtils.hasText(entity.getName())) {
            uiNotification.displayValidationError(i18n.getMessage("message.error.missing.tagname"));
            return false;
        }

        final String trimmedName = StringUtils.trimWhitespace(entity.getName());
        if (!nameBeforeEdit.equals(trimmedName) && dsTagManagement.getByName(trimmedName).isPresent()) {
            // TODO: is the notification right here?
            uiNotification.displayValidationError(i18n.getMessage("message.tag.duplicate.check", trimmedName));
            return false;
        }

        return true;
    }
}
