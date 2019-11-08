/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common;

import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.builder.WindowBuilder;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.ui.Component;

public abstract class AbstractEntityWindowBuilder<T> {
    protected final VaadinMessageSource i18n;

    protected AbstractEntityWindowBuilder(final VaadinMessageSource i18n) {
        this.i18n = i18n;
    }

    protected CommonDialogWindow getWindowForNewEntity(final AbstractEntityWindowController<T, ?> controller) {
        return getWindowForEntity(null, controller);
    }

    protected CommonDialogWindow getWindowForNewEntity(final AbstractEntityWindowController<T, ?> controller,
            final Component windowContent) {
        return getWindowForEntity(null, controller, windowContent);
    }

    protected CommonDialogWindow getWindowForEntity(final T proxyEntity,
            final AbstractEntityWindowController<T, ?> controller) {
        return getWindowForEntity(proxyEntity, controller, controller.getLayout().getRootComponent());
    }

    protected CommonDialogWindow getWindowForEntity(final T proxyEntity,
            final AbstractEntityWindowController<T, ?> controller, final Component windowContent) {
        controller.populateWithData(proxyEntity);

        final CommonDialogWindow window = createWindow(windowContent, controller.getSaveDialogCloseListener());

        controller.getLayout().addValidationListener(window::setSaveButtonEnabled);

        return window;
    }

    protected CommonDialogWindow createWindow(final Component content,
            final SaveDialogCloseListener saveDialogCloseListener) {
        return new WindowBuilder(SPUIDefinitions.CREATE_UPDATE_WINDOW).id(getWindowId()).content(content).i18n(i18n)
                .helpLink(getHelpLink()).saveDialogCloseListener(saveDialogCloseListener).buildCommonDialogWindow();
    }

    protected abstract String getWindowId();

    protected String getHelpLink() {
        // can be overriden to provide help link to documentation
        return null;
    }
}
