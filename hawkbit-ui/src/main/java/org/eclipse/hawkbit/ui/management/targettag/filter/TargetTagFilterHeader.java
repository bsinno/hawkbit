/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag.filter;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.event.FilterHeaderEvent.FilterHeaderEnum;
import org.eclipse.hawkbit.ui.common.event.TargetTagFilterHeaderEvent;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.CloseHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.CrudMenuHeaderSupport;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.management.targettag.TargetTagWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Target Tag filter by Tag Header.
 */
// TODO: remove duplication with other FilterHeader classes
public class TargetTagFilterHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final transient TargetTagWindowBuilder targetTagWindowBuilder;

    private final ManagementUIState managementUIState;

    private final TargetTagFilterButtons targetTagButtons;

    private final transient CrudMenuHeaderSupport crudMenuHeaderSupport;
    private final transient CloseHeaderSupport closeHeaderSupport;

    public TargetTagFilterHeader(final VaadinMessageSource i18n, final ManagementUIState managementUIState,
            final SpPermissionChecker permChecker, final UIEventBus eventBus,
            final TargetTagFilterButtons targetTagButtons, final TargetTagWindowBuilder targetTagWindowBuilder) {
        super(i18n, permChecker, eventBus);

        this.managementUIState = managementUIState;
        this.targetTagWindowBuilder = targetTagWindowBuilder;

        this.targetTagButtons = targetTagButtons;

        this.crudMenuHeaderSupport = new CrudMenuHeaderSupport(i18n, UIComponentIdProvider.TARGET_MENU_BAR_ID,
                permChecker.hasCreateTargetPermission(), permChecker.hasUpdateTargetPermission(),
                permChecker.hasDeleteRepositoryPermission(), getAddButtonCommand(), getUpdateButtonCommand(),
                getDeleteButtonCommand(), getCloseButtonCommand());
        this.closeHeaderSupport = new CloseHeaderSupport(i18n, UIComponentIdProvider.HIDE_TARGET_TAGS,
                this::hideFilterButtonLayout);
        addHeaderSupports(Arrays.asList(crudMenuHeaderSupport, closeHeaderSupport));

        restoreHeaderState();
        buildHeader();
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage("header.target.filter.tag")).buildCaptionLabel();
    }

    protected Command getAddButtonCommand() {
        return menuItem -> {
            final Window addWindow = targetTagWindowBuilder.getWindowForAddTargetTag();

            addWindow.setCaption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.tag")));
            UI.getCurrent().addWindow(addWindow);
            addWindow.setVisible(Boolean.TRUE);
        };
    }

    protected Command getUpdateButtonCommand() {
        return menuItem -> {
            targetTagButtons.showEditColumn();
            eventBus.publish(this, new TargetTagFilterHeaderEvent(FilterHeaderEnum.SHOW_CANCEL_BUTTON));
        };
    }

    protected Command getDeleteButtonCommand() {
        return menuItem -> {
            targetTagButtons.showDeleteColumn();
            eventBus.publish(this, new TargetTagFilterHeaderEvent(FilterHeaderEnum.SHOW_CANCEL_BUTTON));
        };
    }

    private Command getCloseButtonCommand() {
        return command -> {
            targetTagButtons.hideActionColumns();
            eventBus.publish(this, new TargetTagFilterHeaderEvent(FilterHeaderEnum.SHOW_MENUBAR));
        };
    }

    private void hideFilterButtonLayout() {
        managementUIState.setTargetTagFilterClosed(true);
        eventBus.publish(this, ManagementUIEvent.HIDE_TARGET_TAG_LAYOUT);
    }

    // TODO: Do we really need this listener, or should we activate mode in
    // commands?
    @EventBusListenerMethod(scope = EventScope.UI)
    private void onEvent(final TargetTagFilterHeaderEvent event) {
        if (FilterHeaderEnum.SHOW_MENUBAR == event.getFilterHeaderEnum()
                && crudMenuHeaderSupport.isEditModeActivated()) {
            crudMenuHeaderSupport.activateSelectMode();
        } else if (FilterHeaderEnum.SHOW_CANCEL_BUTTON == event.getFilterHeaderEnum()) {
            crudMenuHeaderSupport.activateEditMode();
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    private void onEvent(final ManagementUIEvent event) {
        if (event == ManagementUIEvent.RESET_TARGET_FILTER_QUERY) {
            crudMenuHeaderSupport.enableCrudMenu();
        } else if (event == ManagementUIEvent.RESET_SIMPLE_FILTERS) {
            crudMenuHeaderSupport.disableCrudMenu();
        }
    }
}
