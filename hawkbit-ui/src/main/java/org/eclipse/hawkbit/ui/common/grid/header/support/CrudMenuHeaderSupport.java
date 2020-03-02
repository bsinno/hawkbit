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
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.themes.ValoTheme;

public class CrudMenuHeaderSupport implements HeaderSupport {
    private final VaadinMessageSource i18n;

    private final String crudMenuBarId;
    private final boolean hasCreatePermission;
    private final boolean hasUpdatePermission;
    private final boolean hasDeletePermission;
    private final Command addCommand;
    private final Command updateCommand;
    private final Command deleteCommand;
    private final Command closeCommand;

    private final MenuBar crudMenuBar;
    private final MenuItem crudMenuItem;

    private boolean isEditModeActivated;

    public CrudMenuHeaderSupport(final VaadinMessageSource i18n, final String crudMenuBarId,
            final boolean hasCreatePermission, final boolean hasUpdatePermission, final boolean hasDeletePermission,
            final Command addCommand, final Command updateCommand, final Command deleteCommand,
            final Command closeCommand) {
        this.i18n = i18n;

        this.crudMenuBarId = crudMenuBarId;
        this.hasCreatePermission = hasCreatePermission;
        this.hasUpdatePermission = hasUpdatePermission;
        this.hasDeletePermission = hasDeletePermission;
        this.addCommand = addCommand;
        this.updateCommand = updateCommand;
        this.deleteCommand = deleteCommand;
        this.closeCommand = closeCommand;

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
                    addCommand);
        }
        if (hasUpdatePermission) {
            crudMenuItem.addItem(i18n.getMessage(UIMessageIdProvider.CAPTION_CONFIG_EDIT), VaadinIcons.EDIT,
                    updateCommand);
        }
        if (hasDeletePermission) {
            crudMenuItem.addItem(i18n.getMessage(UIMessageIdProvider.CAPTION_CONFIG_DELETE), VaadinIcons.TRASH,
                    deleteCommand);
        }
    }

    public void activateSelectMode() {
        crudMenuItem.setIcon(VaadinIcons.COG);
        crudMenuItem.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_CONFIGURE));
        crudMenuItem.setCommand(null);

        isEditModeActivated = false;

        crudMenuBar.removeStyleName("edit-mode");
    }

    public void activateEditMode() {
        crudMenuItem.setIcon(VaadinIcons.CLOSE_CIRCLE);
        crudMenuItem.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_CONFIGURE_CLOSE));
        crudMenuItem.setCommand(closeCommand);

        isEditModeActivated = true;

        crudMenuBar.addStyleName("edit-mode");
    }

    @Override
    public Component getHeaderComponent() {
        return crudMenuBar;
    }

    public boolean isEditModeActivated() {
        return isEditModeActivated;
    }

    public void enableCrudMenu() {
        crudMenuBar.setEnabled(true);
    }

    public void disableCrudMenu() {
        crudMenuBar.setEnabled(false);
    }
}
