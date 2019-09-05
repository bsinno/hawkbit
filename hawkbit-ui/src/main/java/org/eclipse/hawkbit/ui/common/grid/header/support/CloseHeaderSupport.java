/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.header.support;

import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;

public class CloseHeaderSupport {
    private final VaadinMessageSource i18n;

    private final String closeIconId;
    private final Runnable closeCallback;

    private final Button closeIcon;

    public CloseHeaderSupport(final VaadinMessageSource i18n, final String closeIconId, final Runnable closeCallback) {
        this.i18n = i18n;

        this.closeIconId = closeIconId;
        this.closeCallback = closeCallback;

        this.closeIcon = createCloseButton();
    }

    private Button createCloseButton() {
        final Button closeButton = SPUIComponentProvider.getButton(closeIconId, "",
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_CLOSE), null, false, VaadinIcons.CLOSE,
                SPUIButtonStyleNoBorder.class);

        closeButton.addClickListener(event -> closeCallback.run());

        return closeButton;
    }

    public Button getCloseIcon() {
        return closeIcon;
    }
}
