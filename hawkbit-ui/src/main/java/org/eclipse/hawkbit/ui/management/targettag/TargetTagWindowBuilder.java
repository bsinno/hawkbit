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
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.management.tag.TagWindowLayout;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Window;

public class TargetTagWindowBuilder extends AbstractEntityWindowBuilder<ProxyTag> {
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final TargetTagManagement targetTagManagement;

    public TargetTagWindowBuilder(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification,
            final TargetTagManagement targetTagManagement) {
        super(i18n);

        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        this.targetTagManagement = targetTagManagement;
    }

    @Override
    protected String getWindowId() {
        return UIComponentIdProvider.TAG_POPUP_ID;
    }

    public Window getWindowForAddTargetTag() {
        return getWindowForNewEntity(new AddTargetTagWindowController(i18n, entityFactory, eventBus, uiNotification,
                targetTagManagement, new TagWindowLayout<ProxyTag>(i18n, uiNotification)));

    }

    public Window getWindowForUpdateTargetTag(final ProxyTag proxyTag) {
        return getWindowForEntity(proxyTag, new UpdateTargetTagWindowController(i18n, entityFactory, eventBus,
                uiNotification, targetTagManagement, new TagWindowLayout<ProxyTag>(i18n, uiNotification)));
    }
}
