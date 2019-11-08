/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
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
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.providers.TargetMetaDataDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractMetaDataWindowLayout;
import org.eclipse.hawkbit.ui.common.detailslayout.AddMetaDataWindowController;
import org.eclipse.hawkbit.ui.common.detailslayout.MetaDataAddUpdateWindowLayout;
import org.eclipse.hawkbit.ui.common.detailslayout.MetaDataWindowGrid;
import org.eclipse.hawkbit.ui.common.detailslayout.UpdateMetaDataWindowController;
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
    private final TargetManagement targetManagement;
    private final EntityFactory entityFactory;

    private final MetaDataWindowGrid<String> targetMetaDataWindowGrid;

    private final MetaDataAddUpdateWindowLayout metaDataAddUpdateWindowLayout;
    private final AddMetaDataWindowController addTargetMetaDataWindowController;
    private final UpdateMetaDataWindowController updateTargetMetaDataWindowController;

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
        this.addTargetMetaDataWindowController = new AddMetaDataWindowController(i18n, eventBus, uiNotification,
                metaDataAddUpdateWindowLayout, this::createMetaData, this::onMetaDataModified, this::isDuplicate);
        this.updateTargetMetaDataWindowController = new UpdateMetaDataWindowController(i18n, eventBus, uiNotification,
                metaDataAddUpdateWindowLayout, this::updateMetaData, this::onMetaDataModified);

        buildLayout();
        addGridSelectionListener();
    }

    private void deleteMetaData(final Collection<ProxyMetaData> metaDataToDelete) {
        if (!StringUtils.isEmpty(masterEntityFilter) && !CollectionUtils.isEmpty(metaDataToDelete)) {
            // as of now we only allow deletion of single metadata entry
            final String metaDataKey = metaDataToDelete.iterator().next().getKey();
            targetManagement.deleteMetaData(masterEntityFilter, metaDataKey);

            // TODO: check if we should publish the event here
            targetMetaDataWindowGrid.refreshContainer();
        } else {
            // TODO: use i18n
            uiNotification.displayValidationError("No Target is currently selected or metadata to delete is missing");
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
}
