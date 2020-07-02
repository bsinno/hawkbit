/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.MetaData;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.providers.TargetMetaDataDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractMetaDataWindowLayout;
import org.eclipse.hawkbit.ui.common.detailslayout.AddMetaDataWindowController;
import org.eclipse.hawkbit.ui.common.detailslayout.MetaDataAddUpdateWindowLayout;
import org.eclipse.hawkbit.ui.common.detailslayout.MetaDataWindowGrid;
import org.eclipse.hawkbit.ui.common.detailslayout.UpdateMetaDataWindowController;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Class for metadata add/update window layout.
 */
public class TargetMetaDataWindowLayout extends AbstractMetaDataWindowLayout<String> {
    private static final long serialVersionUID = 1L;

    private final UINotification uiNotification;
    private final transient TargetManagement targetManagement;
    private final transient EntityFactory entityFactory;

    private final MetaDataWindowGrid<String> targetMetaDataWindowGrid;

    private final transient MetaDataAddUpdateWindowLayout metaDataAddUpdateWindowLayout;
    private final transient AddMetaDataWindowController addTargetMetaDataWindowController;
    private final transient UpdateMetaDataWindowController updateTargetMetaDataWindowController;

    /**
     * Constructor for AbstractTagWindowLayout
     * 
     * @param i18n
     *            I18N
     */
    public TargetMetaDataWindowLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permChecker, final UINotification uiNotification,
            final EntityFactory entityFactory, final TargetManagement targetManagement) {
        super(i18n, eventBus, permChecker);

        this.uiNotification = uiNotification;
        this.targetManagement = targetManagement;
        this.entityFactory = entityFactory;

        this.targetMetaDataWindowGrid = new MetaDataWindowGrid<>(i18n, eventBus, permChecker, uiNotification,
                new TargetMetaDataDataProvider(targetManagement), this::deleteMetaData);

        this.metaDataAddUpdateWindowLayout = new MetaDataAddUpdateWindowLayout(i18n);
        this.addTargetMetaDataWindowController = new AddMetaDataWindowController(i18n, uiNotification,
                metaDataAddUpdateWindowLayout, this::createMetaData, this::onMetaDataModified, this::isDuplicate);
        this.updateTargetMetaDataWindowController = new UpdateMetaDataWindowController(i18n, uiNotification,
                metaDataAddUpdateWindowLayout, this::updateMetaData, this::onMetaDataModified);

        buildLayout();
        addGridSelectionListener();
    }

    private boolean deleteMetaData(final Collection<ProxyMetaData> metaDataToDelete) {
        if (!StringUtils.isEmpty(masterEntityFilter) && !CollectionUtils.isEmpty(metaDataToDelete)) {
            // as of now we only allow deletion of single metadata entry
            final String metaDataKey = metaDataToDelete.iterator().next().getKey();
            targetManagement.deleteMetaData(masterEntityFilter, metaDataKey);

            targetMetaDataWindowGrid.refreshAll();

            publishEntityModifiedEvent();

            return true;
        } else {
            final String entityType = i18n.getMessage("caption.target");
            uiNotification.displayValidationError(i18n.getMessage("message.error.deleteMetaData", entityType));

            return false;
        }
    }

    private MetaData createMetaData(final ProxyMetaData entity) {
        return targetManagement
                .createMetaData(masterEntityFilter, Collections
                        .singletonList(entityFactory.generateTargetMetadata(entity.getKey(), entity.getValue())))
                .get(0);
    }

    private boolean isDuplicate(final String metaDataKey) {
        return targetManagement.getMetaDataByControllerId(masterEntityFilter, metaDataKey).isPresent();
    }

    private MetaData updateMetaData(final ProxyMetaData entity) {
        return targetManagement.updateMetadata(masterEntityFilter,
                entityFactory.generateTargetMetadata(entity.getKey(), entity.getValue()));
    }

    @Override
    protected MetaDataWindowGrid<String> getMetaDataWindowGrid() {
        return targetMetaDataWindowGrid;
    }

    @Override
    public AddMetaDataWindowController getAddMetaDataWindowController() {
        return addTargetMetaDataWindowController;
    }

    @Override
    public UpdateMetaDataWindowController getUpdateMetaDataWindowController() {
        return updateTargetMetaDataWindowController;
    }

    @Override
    public MetaDataAddUpdateWindowLayout getMetaDataAddUpdateWindowLayout() {
        return metaDataAddUpdateWindowLayout;
    }

    @Override
    protected void publishEntityModifiedEvent() {
        targetManagement.getByControllerID(masterEntityFilter).map(Target::getId).ifPresent(
                targetId -> eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                        EntityModifiedEventType.ENTITY_UPDATED, ProxyTarget.class, targetId)));
    }
}
