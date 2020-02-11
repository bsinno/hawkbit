/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.header.support;

import java.util.function.BooleanSupplier;

import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

public class ResizeHeaderSupport implements HeaderSupport {
    private final VaadinMessageSource i18n;

    private static final String MAXIMIZE_ID_SUFFIX = "maximize";
    private static final String MINIMIZE_ID_SUFFIX = "minimize";

    private final String maxMinIconId;
    private final Runnable maximizeCallback;
    private final Runnable minimizeCallback;
    private final BooleanSupplier isMaximizedStateSupplier;

    private final Button maxMinIcon;

    private boolean isMaximized;

    public ResizeHeaderSupport(final VaadinMessageSource i18n, final String maxMinIconId,
            final Runnable maximizeCallback, final Runnable minimizeCallback,
            final BooleanSupplier isMaximizedStateSupplier) {
        this.i18n = i18n;

        this.maxMinIconId = maxMinIconId;
        this.maximizeCallback = maximizeCallback;
        this.minimizeCallback = minimizeCallback;
        this.isMaximizedStateSupplier = isMaximizedStateSupplier;

        this.maxMinIcon = createMaxMinIcon();
        this.isMaximized = false;
    }

    private Button createMaxMinIcon() {
        final Button maxMinbutton = SPUIComponentProvider.getButton(String.join(".", maxMinIconId, MAXIMIZE_ID_SUFFIX),
                "", i18n.getMessage(UIMessageIdProvider.TOOLTIP_MAXIMIZE), null, false, VaadinIcons.EXPAND,
                SPUIButtonStyleNoBorder.class);

        maxMinbutton.addClickListener(event -> maxMinButtonClicked());

        return maxMinbutton;
    }

    private void maxMinButtonClicked() {
        if (isMaximized) {
            // Clicked on min icon
            showMaxIcon();
            minimizeCallback.run();
        } else {
            // Clicked on max Icon
            showMinIcon();
            maximizeCallback.run();
        }
    }

    /**
     * Styles min-max-button icon with minimize decoration
     */
    private void showMinIcon() {
        maxMinIcon.setId(String.join(".", maxMinIconId, MINIMIZE_ID_SUFFIX));
        maxMinIcon.setIcon(VaadinIcons.COMPRESS);
        maxMinIcon.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_MINIMIZE));

        isMaximized = true;
    }

    /**
     * Styles min-max-button icon with maximize decoration
     */
    private void showMaxIcon() {
        maxMinIcon.setId(String.join(".", maxMinIconId, MAXIMIZE_ID_SUFFIX));
        maxMinIcon.setIcon(VaadinIcons.EXPAND);
        maxMinIcon.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_MAXIMIZE));

        isMaximized = false;
    }

    @Override
    public Component getHeaderComponent() {
        return maxMinIcon;
    }

    @Override
    public void restoreState() {
        if (isMaximizedStateSupplier.getAsBoolean()) {
            showMinIcon();
        }
    }
}
