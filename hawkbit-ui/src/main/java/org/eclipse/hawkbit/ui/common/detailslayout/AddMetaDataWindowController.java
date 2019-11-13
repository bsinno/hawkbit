/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.hawkbit.repository.model.MetaData;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowController;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.MetaDataModifiedEventPayload;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

public class AddMetaDataWindowController extends AbstractEntityWindowController<ProxyMetaData, ProxyMetaData> {
    private final VaadinMessageSource i18n;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final MetaDataAddUpdateWindowLayout layout;

    private final Function<ProxyMetaData, MetaData> createMetaDataCallback;
    private final Consumer<ProxyMetaData> saveMetaDataCallback;
    private final Predicate<String> duplicateCheckCallback;

    public AddMetaDataWindowController(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final UINotification uiNotification, final MetaDataAddUpdateWindowLayout layout,
            final Function<ProxyMetaData, MetaData> createMetaDataCallback,
            final Consumer<ProxyMetaData> saveMetaDataCallback, final Predicate<String> duplicateCheckCallback) {
        this.i18n = i18n;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        this.layout = layout;

        this.createMetaDataCallback = createMetaDataCallback;
        this.saveMetaDataCallback = saveMetaDataCallback;
        this.duplicateCheckCallback = duplicateCheckCallback;
    }

    @Override
    public MetaDataAddUpdateWindowLayout getLayout() {
        return layout;
    }

    @Override
    protected ProxyMetaData buildEntityFromProxy(final ProxyMetaData proxyEntity) {
        // We ignore the method parameter, because we are interested in the
        // empty object, that we can populate with defaults
        return new ProxyMetaData();
    }

    @Override
    protected void adaptLayout() {
        layout.enableMetadataKey();
    }

    @Override
    protected void persistEntity(final ProxyMetaData entity) {
        final MetaData newMetaData = createMetaDataCallback.apply(entity);

        // TODO: check if could be substituted by the event
        saveMetaDataCallback.accept(entity);

        uiNotification.displaySuccess(i18n.getMessage("message.metadata.saved", newMetaData.getKey()));
        // TODO: verify if sender and payload is correct
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new MetaDataModifiedEventPayload(EntityModifiedEventType.ENTITY_ADDED, newMetaData.getEntityId()));
    }

    @Override
    protected boolean isEntityValid(final ProxyMetaData entity) {
        if (!StringUtils.hasText(entity.getKey())) {
            uiNotification.displayValidationError(i18n.getMessage("message.key.missing"));
            return false;
        }

        if (!StringUtils.hasText(entity.getValue())) {
            uiNotification.displayValidationError(i18n.getMessage("message.value.missing"));
            return false;
        }

        final String trimmedKey = StringUtils.trimWhitespace(entity.getKey());
        if (duplicateCheckCallback.test(trimmedKey)) {
            uiNotification.displayValidationError(i18n.getMessage("message.metadata.duplicate.check", trimmedKey));
            return false;
        }

        return true;
    }

    @Override
    protected boolean closeWindowAfterSave() {
        return false;
    }
}