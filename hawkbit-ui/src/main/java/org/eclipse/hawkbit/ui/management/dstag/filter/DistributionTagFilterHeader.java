/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstag.filter;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.event.DistributionSetTagFilterHeaderEvent;
import org.eclipse.hawkbit.ui.common.event.FilterHeaderEvent.FilterHeaderEnum;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.CloseHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.CrudMenuHeaderSupport;
import org.eclipse.hawkbit.ui.management.dstag.DsTagWindowBuilder;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
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
 * Table header for filtering distribution set tags
 *
 */
// TODO: remove duplication with other FilterHeader classes
public class DistributionTagFilterHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final transient DsTagWindowBuilder dsTagWindowBuilder;

    private final ManagementUIState managementUIState;

    private final DistributionTagButtons distributionTagButtons;

    private final transient CrudMenuHeaderSupport crudMenuHeaderSupport;
    private final transient CloseHeaderSupport closeHeaderSupport;

    public DistributionTagFilterHeader(final VaadinMessageSource i18n, final ManagementUIState managementUIState,
            final SpPermissionChecker permChecker, final UIEventBus eventBus,
            final DistributionTagButtons distributionTagButtons, final DsTagWindowBuilder dsTagWindowBuilder) {
        super(i18n, permChecker, eventBus);

        this.managementUIState = managementUIState;
        this.dsTagWindowBuilder = dsTagWindowBuilder;

        this.distributionTagButtons = distributionTagButtons;

        this.crudMenuHeaderSupport = new CrudMenuHeaderSupport(i18n, UIComponentIdProvider.DIST_TAG_MENU_BAR_ID,
                permChecker.hasCreateTargetPermission(), permChecker.hasUpdateTargetPermission(),
                permChecker.hasDeleteRepositoryPermission(), getAddButtonCommand(), getUpdateButtonCommand(),
                getDeleteButtonCommand(), getCloseButtonCommand());
        this.closeHeaderSupport = new CloseHeaderSupport(i18n, UIComponentIdProvider.HIDE_DS_TAGS,
                this::hideFilterButtonLayout);
        addHeaderSupports(Arrays.asList(crudMenuHeaderSupport, closeHeaderSupport));

        restoreHeaderState();
        buildHeader();
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage("header.filter.tag")).buildCaptionLabel();
    }

    private Command getAddButtonCommand() {
        return menuItem -> {
            final Window addWindow = dsTagWindowBuilder.getWindowForAddDsTag();

            addWindow.setCaption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.tag")));
            UI.getCurrent().addWindow(addWindow);
            addWindow.setVisible(Boolean.TRUE);
        };
    }

    private Command getUpdateButtonCommand() {
        return command -> {
            distributionTagButtons.showEditColumn();
            eventBus.publish(this, new DistributionSetTagFilterHeaderEvent(FilterHeaderEnum.SHOW_CANCEL_BUTTON));
        };
    }

    private Command getDeleteButtonCommand() {
        return command -> {
            distributionTagButtons.showDeleteColumn();
            eventBus.publish(this, new DistributionSetTagFilterHeaderEvent(FilterHeaderEnum.SHOW_CANCEL_BUTTON));
        };
    }

    private Command getCloseButtonCommand() {
        return command -> {
            distributionTagButtons.hideActionColumns();
            eventBus.publish(this, new DistributionSetTagFilterHeaderEvent(FilterHeaderEnum.SHOW_MENUBAR));
        };
    }

    private void hideFilterButtonLayout() {
        managementUIState.setDistTagFilterClosed(true);
        eventBus.publish(this, ManagementUIEvent.HIDE_DISTRIBUTION_TAG_LAYOUT);
    }

    // TODO: Do we really need this listener, or should we activate mode in
    // commands?
    @EventBusListenerMethod(scope = EventScope.UI)
    private void onEvent(final DistributionSetTagFilterHeaderEvent event) {
        if (FilterHeaderEnum.SHOW_MENUBAR == event.getFilterHeaderEnum()
                && crudMenuHeaderSupport.isEditModeActivated()) {
            crudMenuHeaderSupport.activateSelectMode();
        } else if (FilterHeaderEnum.SHOW_CANCEL_BUTTON == event.getFilterHeaderEnum()) {
            crudMenuHeaderSupport.activateEditMode();
        }
    }
}
