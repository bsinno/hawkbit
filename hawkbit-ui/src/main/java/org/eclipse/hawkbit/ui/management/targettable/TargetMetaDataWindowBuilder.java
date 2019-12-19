/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractMetaDataWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Window;

public class TargetMetaDataWindowBuilder extends AbstractMetaDataWindowBuilder<String> {
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;
    private final SpPermissionChecker permChecker;

    private final TargetManagement targetManagement;

    public TargetMetaDataWindowBuilder(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification, final SpPermissionChecker permChecker,
            final TargetManagement targetManagement) {
        super(i18n);

        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;
        this.permChecker = permChecker;

        this.targetManagement = targetManagement;
    }

    public Window getWindowForShowTargetMetaData(final String controllerId) {
        return getWindowForShowMetaData(new TargetMetaDataWindowLayout(i18n, eventBus, permChecker, uiNotification,
                entityFactory, targetManagement), controllerId, null);
    }

    public Window getWindowForShowTargetMetaData(final String controllerId, final ProxyMetaData proxyMetaData) {
        return getWindowForShowMetaData(new TargetMetaDataWindowLayout(i18n, eventBus, permChecker, uiNotification,
                entityFactory, targetManagement), controllerId, proxyMetaData);
    }
}
