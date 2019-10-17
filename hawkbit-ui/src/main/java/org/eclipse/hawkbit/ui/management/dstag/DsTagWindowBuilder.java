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
import org.eclipse.hawkbit.ui.common.CommonDialogWindow;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.builder.WindowBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.management.tag.TagWindowController;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

public class DsTagWindowBuilder {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final DistributionSetTagManagement dsTagManagement;

    public DsTagWindowBuilder(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification,
            final DistributionSetTagManagement dsTagManagement) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        this.dsTagManagement = dsTagManagement;
    }

    public Window getWindowForAddDsTag() {
        return getWindowForTag(null, new AddDsTagWindowController(i18n, entityFactory, eventBus, uiNotification,
                dsTagManagement, new AddDsTagWindowLayout(i18n)));

    }

    public Window getWindowForUpdateDsTag(final ProxyTag proxyTag) {
        return getWindowForTag(proxyTag, new UpdateDsTagWindowController(i18n, entityFactory, eventBus, uiNotification,
                dsTagManagement, new UpdateDsTagWindowLayout(i18n)));
    }

    private Window getWindowForTag(final ProxyTag proxyTag, final TagWindowController controller) {
        controller.populateWithData(proxyTag);

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
