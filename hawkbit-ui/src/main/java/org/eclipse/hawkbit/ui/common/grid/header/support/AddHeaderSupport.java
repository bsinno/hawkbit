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

public class AddHeaderSupport {
    private final VaadinMessageSource i18n;

    private final String addIconId;
    private final Runnable addItemCallback;
    private final BooleanSupplier maximizedStateSupplier;

    private final Button addIcon;

    public AddHeaderSupport(final VaadinMessageSource i18n, final String addIconId, final Runnable addItemCallback,
            final BooleanSupplier maximizedStateSupplier) {
        this.i18n = i18n;

        this.addIconId = addIconId;
        this.addItemCallback = addItemCallback;
        this.maximizedStateSupplier = maximizedStateSupplier;

        this.addIcon = createAddButton();
    }

    private Button createAddButton() {
        final Button addButton = SPUIComponentProvider.getButton(addIconId, "",
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_ADD), null, false, VaadinIcons.PLUS,
                SPUIButtonStyleNoBorder.class);

        addButton.addClickListener(event -> addItemCallback.run());

        return addButton;
    }

    public Button getAddIcon() {
        return addIcon;
    }

    public void hideAddIcon() {
        addIcon.setVisible(false);
    }

    public void showAddIcon() {
        addIcon.setVisible(true);
    }

    public void restoreAddState() {
        if (maximizedStateSupplier.getAsBoolean()) {
            hideAddIcon();
        }
    }
}
