/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.header.support;

import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.themes.ValoTheme;

public class CrudMenuHeaderSupport implements HeaderSupport {
    private static final String MODE_EDIT = "mode-edit";
    private static final String MODE_DELETE = "mode-delete";

    private final VaadinMessageSource i18n;

    private final String crudMenuBarId;
    private final boolean hasCreatePermission;
    private final boolean hasUpdatePermission;
    private final boolean hasDeletePermission;
    private final Runnable addCallback;
    private final Runnable editCallback;
    private final Runnable deleteCallback;
    private final Runnable closeCallback;

    private final MenuBar crudMenuBar;
    private final MenuItem crudMenuItem;

    public CrudMenuHeaderSupport(final VaadinMessageSource i18n, final String crudMenuBarId,
            final boolean hasCreatePermission, final boolean hasUpdatePermission, final boolean hasDeletePermission,
            final Runnable addCallback, final Runnable editCallback, final Runnable deleteCallback,
            final Runnable closeCallback) {
        this.i18n = i18n;

        this.crudMenuBarId = crudMenuBarId;
        this.hasCreatePermission = hasCreatePermission;
        this.hasUpdatePermission = hasUpdatePermission;
        this.hasDeletePermission = hasDeletePermission;
        this.addCallback = addCallback;
        this.editCallback = editCallback;
        this.deleteCallback = deleteCallback;
        this.closeCallback = closeCallback;

        this.crudMenuBar = createCrudMenuBar();
        this.crudMenuItem = crudMenuBar.addItem("");

        addCrudMenuItemCommands();
        activateSelectMode();
    }

    private MenuBar createCrudMenuBar() {
        final MenuBar menuBar = new MenuBar();

        menuBar.setId(crudMenuBarId);
        menuBar.setStyleName(ValoTheme.MENUBAR_BORDERLESS);
        menuBar.addStyleName("crud-menubar");

        return menuBar;
    }

    private void addCrudMenuItemCommands() {
        if (hasCreatePermission) {
            crudMenuItem.addItem(i18n.getMessage(UIMessageIdProvider.CAPTION_CONFIG_CREATE), VaadinIcons.PLUS,
                    menuItem -> addCallback.run());
        }
        if (hasUpdatePermission) {
            crudMenuItem.addItem(i18n.getMessage(UIMessageIdProvider.CAPTION_CONFIG_EDIT), VaadinIcons.EDIT,
                    menuItem -> {
                        activateEditMode(MODE_EDIT);
                        editCallback.run();
                    });
        }
        if (hasDeletePermission) {
            crudMenuItem.addItem(i18n.getMessage(UIMessageIdProvider.CAPTION_CONFIG_DELETE), VaadinIcons.TRASH,
                    menuItem -> {
                        activateEditMode(MODE_DELETE);
                        deleteCallback.run();
                    });
        }
    }

    private void activateSelectMode() {
        crudMenuItem.setIcon(VaadinIcons.COG);
        crudMenuItem.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_CONFIGURE));
        crudMenuItem.setCommand(null);

        crudMenuBar.removeStyleNames(MODE_EDIT, MODE_DELETE);
    }

    private void activateEditMode(final String mode) {
        crudMenuItem.setIcon(VaadinIcons.CLOSE_CIRCLE);
        crudMenuItem.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_CONFIGURE_CLOSE));
        crudMenuItem.setCommand(menuItem -> {
            activateSelectMode();
            closeCallback.run();
        });

        crudMenuBar.addStyleName(mode);
    }

    @Override
    public Component getHeaderComponent() {
        return crudMenuBar;
    }

    public void enableCrudMenu() {
        crudMenuBar.setEnabled(true);
    }

    public void disableCrudMenu() {
        crudMenuBar.setEnabled(false);
    }
}
