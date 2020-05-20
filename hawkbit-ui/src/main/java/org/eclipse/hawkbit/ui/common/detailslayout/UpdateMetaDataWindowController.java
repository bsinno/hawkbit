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

import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.EntityReadOnlyException;
import org.eclipse.hawkbit.repository.model.MetaData;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowController;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class UpdateMetaDataWindowController extends AbstractEntityWindowController<ProxyMetaData, ProxyMetaData> {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateMetaDataWindowController.class);

    private final VaadinMessageSource i18n;
    private final UINotification uiNotification;

    private final MetaDataAddUpdateWindowLayout layout;

    private final Function<ProxyMetaData, MetaData> updateMetaDataCallback;
    private final Consumer<ProxyMetaData> saveMetaDataCallback;

    public UpdateMetaDataWindowController(final VaadinMessageSource i18n, final UINotification uiNotification,
            final MetaDataAddUpdateWindowLayout layout, final Function<ProxyMetaData, MetaData> updateMetaDataCallback,
            final Consumer<ProxyMetaData> saveMetaDataCallback) {
        this.i18n = i18n;
        this.uiNotification = uiNotification;

        this.layout = layout;

        this.updateMetaDataCallback = updateMetaDataCallback;
        this.saveMetaDataCallback = saveMetaDataCallback;
    }

    @Override
    public MetaDataAddUpdateWindowLayout getLayout() {
        return layout;
    }

    @Override
    protected ProxyMetaData buildEntityFromProxy(final ProxyMetaData proxyEntity) {
        final ProxyMetaData metaData = new ProxyMetaData();

        metaData.setKey(proxyEntity.getKey());
        metaData.setValue(proxyEntity.getValue());
        metaData.setEntityId(proxyEntity.getEntityId());
        metaData.setTargetVisible(proxyEntity.isTargetVisible());

        return metaData;
    }

    @Override
    protected void adaptLayout() {
        layout.disableMetadataKey();
    }

    @Override
    protected void persistEntity(final ProxyMetaData entity) {
        MetaData updatedMetaData;
        try {
            updatedMetaData = updateMetaDataCallback.apply(entity);
        } catch (final EntityNotFoundException | EntityReadOnlyException e) {
            LOG.trace("Update of meta data failed in UI: {}", e.getMessage());
            // TODO: use i18n
            uiNotification.displayWarning(
                    "Metadata with key " + entity.getKey() + " was deleted or you are not allowed to update it");
            return;
        }

        saveMetaDataCallback.accept(entity);

        uiNotification.displaySuccess(i18n.getMessage("message.metadata.updated", updatedMetaData.getKey()));
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

        return true;
    }

    @Override
    protected boolean closeWindowAfterSave() {
        return false;
    }
}
