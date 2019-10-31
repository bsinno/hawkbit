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

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.providers.TargetMetaDataDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractMetaDataWindowGrid;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;

/**
 * Grid for Target MetaData pop up layout.
 */
public class TargetMetaDataWindowGrid extends AbstractMetaDataWindowGrid<String> {
    private static final long serialVersionUID = 1L;

    private final TargetManagement targetManagement;

    private final ConfigurableFilterDataProvider<ProxyMetaData, Void, String> metaDataDataProvider;

    public TargetMetaDataWindowGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final TargetManagement targetManagement) {
        super(i18n, eventBus, permissionChecker, notification);

        this.targetManagement = targetManagement;

        this.metaDataDataProvider = new TargetMetaDataDataProvider(targetManagement).withConfigurableFilter();
    }

    @Override
    protected void deleteMetaData(final Collection<ProxyMetaData> metaDataToDelete) {
        if (!StringUtils.isEmpty(masterEntityFilter) && !CollectionUtils.isEmpty(metaDataToDelete)) {
            // as of now we only allow deletion of single metadata entry
            final String metaDataKey = metaDataToDelete.iterator().next().getKey();
            targetManagement.deleteMetaData(masterEntityFilter, metaDataKey);
        } else {
            // TODO: use i18n
            notification.displayValidationError("No target is currently selected or metadata to delete is missing");
        }
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyMetaData, Void, String> getFilterDataProvider() {
        return metaDataDataProvider;
    }
}
