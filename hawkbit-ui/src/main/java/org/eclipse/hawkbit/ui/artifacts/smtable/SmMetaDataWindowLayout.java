/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtable;

import java.util.Collection;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.model.MetaData;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.providers.SmMetaDataDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractMetaDataWindowLayout;
import org.eclipse.hawkbit.ui.common.detailslayout.AddMetaDataWindowController;
import org.eclipse.hawkbit.ui.common.detailslayout.MetaDataAddUpdateWindowLayout;
import org.eclipse.hawkbit.ui.common.detailslayout.MetaDataWindowGrid;
import org.eclipse.hawkbit.ui.common.detailslayout.UpdateMetaDataWindowController;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SmModifiedEventPayload;
import org.eclipse.hawkbit.ui.distributions.smtable.SmMetaDataAddUpdateWindowLayout;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Class for metadata add/update window layout.
 */
public class SmMetaDataWindowLayout extends AbstractMetaDataWindowLayout<Long> {
    private static final long serialVersionUID = 1L;

    private final UINotification uiNotification;
    private final transient SoftwareModuleManagement smManagement;
    private final transient EntityFactory entityFactory;

    private final MetaDataWindowGrid<Long> smMetaDataWindowGrid;

    private final transient SmMetaDataAddUpdateWindowLayout smMetaDataAddUpdateWindowLayout;
    private final transient AddMetaDataWindowController addSmMetaDataWindowController;
    private final transient UpdateMetaDataWindowController updateSmMetaDataWindowController;

    /**
     * Constructor for AbstractTagWindowLayout
     * 
     * @param i18n
     *            I18N
     */
    public SmMetaDataWindowLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permChecker, final UINotification uiNotification,
            final EntityFactory entityFactory, final SoftwareModuleManagement smManagement) {
        super(i18n, eventBus, permChecker);

        this.uiNotification = uiNotification;
        this.smManagement = smManagement;
        this.entityFactory = entityFactory;

        this.smMetaDataWindowGrid = new MetaDataWindowGrid<>(i18n, eventBus, permChecker, uiNotification,
                new SmMetaDataDataProvider(smManagement), this::deleteMetaData);

        this.smMetaDataAddUpdateWindowLayout = new SmMetaDataAddUpdateWindowLayout(i18n);
        this.addSmMetaDataWindowController = new AddMetaDataWindowController(i18n, uiNotification,
                smMetaDataAddUpdateWindowLayout, this::createMetaData, this::onMetaDataModified, this::isDuplicate);
        this.updateSmMetaDataWindowController = new UpdateMetaDataWindowController(i18n, uiNotification,
                smMetaDataAddUpdateWindowLayout, this::updateMetaData, this::onMetaDataModified);

        buildLayout();
        addGridSelectionListener();
    }

    private void deleteMetaData(final Collection<ProxyMetaData> metaDataToDelete) {
        if (masterEntityFilter != null && !CollectionUtils.isEmpty(metaDataToDelete)) {
            // as of now we only allow deletion of single metadata entry
            final String metaDataKey = metaDataToDelete.iterator().next().getKey();
            smManagement.deleteMetaData(masterEntityFilter, metaDataKey);

            smMetaDataWindowGrid.refreshContainer();

            publishEntityModifiedEvent();
        } else {
            // TODO: use i18n
            uiNotification.displayValidationError(
                    "No software module is currently selected or metadata to delete is missing");
        }
    }

    private MetaData createMetaData(final ProxyMetaData entity) {
        return smManagement.createMetaData(entityFactory.softwareModuleMetadata().create(masterEntityFilter)
                .key(entity.getKey()).value(entity.getValue()).targetVisible(entity.isTargetVisible()));
    }

    private boolean isDuplicate(final String metaDataKey) {
        return smManagement.getMetaDataBySoftwareModuleId(masterEntityFilter, metaDataKey).isPresent();
    }

    private MetaData updateMetaData(final ProxyMetaData entity) {
        return smManagement
                .updateMetaData(entityFactory.softwareModuleMetadata().update(masterEntityFilter, entity.getKey())
                        .value(entity.getValue()).targetVisible(entity.isTargetVisible()));
    }

    @Override
    protected MetaDataWindowGrid<Long> getMetaDataWindowGrid() {
        return smMetaDataWindowGrid;
    }

    @Override
    public AddMetaDataWindowController getAddMetaDataWindowController() {
        return addSmMetaDataWindowController;
    }

    @Override
    public UpdateMetaDataWindowController getUpdateMetaDataWindowController() {
        return updateSmMetaDataWindowController;
    }

    @Override
    public MetaDataAddUpdateWindowLayout getMetaDataAddUpdateWindowLayout() {
        return smMetaDataAddUpdateWindowLayout;
    }

    @Override
    protected void publishEntityModifiedEvent() {
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new SmModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, masterEntityFilter));
    }
}
