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
import org.eclipse.hawkbit.ui.common.CommonDialogWindow;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.builder.WindowBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

public class TargetWindowBuilder {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final TargetManagement targetManagement;

    public TargetWindowBuilder(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification, final TargetManagement targetManagement) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        this.targetManagement = targetManagement;
    }

    public Window getWindowForAddTarget() {
        return getWindowForTag(null, new AddTargetWindowController(i18n, entityFactory, eventBus, uiNotification,
                targetManagement, new TargetWindowLayout(i18n)));

    }

    public Window getWindowForUpdateTarget(final ProxyTarget proxyTarget) {
        return getWindowForTag(proxyTarget, new UpdateTargetWindowController(i18n, entityFactory, eventBus,
                uiNotification, targetManagement, new TargetWindowLayout(i18n)));
    }

    private Window getWindowForTag(final ProxyTarget proxyTarget, final TargetWindowController controller) {
        controller.populateWithData(proxyTarget);

        final CommonDialogWindow window = createWindow(controller.getLayout(), controller.getSaveDialogCloseListener());

        controller.getLayout().addValidationListener(window::setSaveButtonEnabled);

        return window;

    }

    private CommonDialogWindow createWindow(final Component content,
            final SaveDialogCloseListener saveDialogCloseListener) {
        return new WindowBuilder(SPUIDefinitions.CREATE_UPDATE_WINDOW).id(UIComponentIdProvider.TAG_POPUP_ID)
                .content(content).i18n(i18n).saveDialogCloseListener(saveDialogCloseListener).buildCommonDialogWindow();
    }
}
