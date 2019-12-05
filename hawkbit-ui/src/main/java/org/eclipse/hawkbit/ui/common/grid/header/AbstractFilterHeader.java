/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.header;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.FilterButtonsActionsChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityChangedEventPayload;
import org.eclipse.hawkbit.ui.common.grid.header.support.CloseHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.CrudMenuHeaderSupport;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Abstract filter header.
 */
public abstract class AbstractFilterHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final transient CrudMenuHeaderSupport crudMenuHeaderSupport;
    private final transient CloseHeaderSupport closeHeaderSupport;

    public AbstractFilterHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus) {
        super(i18n, permChecker, eventBus);

        this.crudMenuHeaderSupport = new CrudMenuHeaderSupport(i18n, getCrudMenuBarId(),
                permChecker.hasCreateRepositoryPermission(), permChecker.hasUpdateRepositoryPermission(),
                permChecker.hasDeleteRepositoryPermission(), getAddButtonCommand(), getUpdateButtonCommand(),
                getDeleteButtonCommand(), getCloseButtonCommand());
        this.closeHeaderSupport = new CloseHeaderSupport(i18n, getCloseIconId(), this::hideFilterLayout);
        addHeaderSupports(Arrays.asList(crudMenuHeaderSupport, closeHeaderSupport));
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage(getHeaderCaptionMsgKey())).buildCaptionLabel();
    }

    protected abstract String getHeaderCaptionMsgKey();

    protected abstract String getCrudMenuBarId();

    private Command getAddButtonCommand() {
        return menuItem -> {
            final Window addWindow = getWindowForAdd();

            addWindow.setCaption(
                    i18n.getMessage("caption.create.new", i18n.getMessage(getAddEntityWindowCaptionMsgKey())));
            UI.getCurrent().addWindow(addWindow);
            addWindow.setVisible(Boolean.TRUE);
        };
    }

    protected abstract Window getWindowForAdd();

    protected abstract String getAddEntityWindowCaptionMsgKey();

    private Command getUpdateButtonCommand() {
        return command -> {
            eventBus.publish(EventTopics.FILTER_BUTTONS_ACTIONS_CHANGED, this,
                    FilterButtonsActionsChangedEventPayload.SHOW_EDIT);
            crudMenuHeaderSupport.activateEditMode();
        };
    }

    private Command getDeleteButtonCommand() {
        return command -> {
            eventBus.publish(EventTopics.FILTER_BUTTONS_ACTIONS_CHANGED, this,
                    FilterButtonsActionsChangedEventPayload.SHOW_DELETE);
            crudMenuHeaderSupport.activateEditMode();
        };
    }

    private Command getCloseButtonCommand() {
        return command -> {
            eventBus.publish(EventTopics.FILTER_BUTTONS_ACTIONS_CHANGED, this,
                    FilterButtonsActionsChangedEventPayload.HIDE_ALL);
            crudMenuHeaderSupport.activateSelectMode();
        };
    }

    protected abstract String getCloseIconId();

    private void hideFilterLayout() {
        eventBus.publish(EventTopics.LAYOUT_VISIBILITY_CHANGED, this,
                LayoutVisibilityChangedEventPayload.LAYOUT_HIDDEN);

        updateHiddenUiState();
    }

    protected abstract void updateHiddenUiState();

    protected void enableCrudMenu() {
        crudMenuHeaderSupport.enableCrudMenu();
    }

    protected void disableCrudMenu() {
        crudMenuHeaderSupport.disableCrudMenu();
    }
}
