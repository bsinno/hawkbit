/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.smtype.filter;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.smtype.SmTypeWindowBuilder;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.FilterButtonsActionsChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityChangedEventPayload;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.CloseHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.CrudMenuHeaderSupport;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Software Module Type filter buttons header.
 */
// TODO: remove duplication with other FilterHeader classes
public class DistSMTypeFilterHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final DistSMTypeFilterLayoutUiState distSMTypeFilterLayoutUiState;

    private final transient SmTypeWindowBuilder smTypeWindowBuilder;

    private final transient CrudMenuHeaderSupport crudMenuHeaderSupport;
    private final transient CloseHeaderSupport closeHeaderSupport;

    public DistSMTypeFilterHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final DistSMTypeFilterLayoutUiState distSMTypeFilterLayoutUiState,
            final SmTypeWindowBuilder smTypeWindowBuilder) {
        super(i18n, permChecker, eventBus);

        this.distSMTypeFilterLayoutUiState = distSMTypeFilterLayoutUiState;
        this.smTypeWindowBuilder = smTypeWindowBuilder;

        this.crudMenuHeaderSupport = new CrudMenuHeaderSupport(i18n, UIComponentIdProvider.SOFT_MODULE_TYPE_MENU_BAR_ID,
                permChecker.hasCreateTargetPermission(), permChecker.hasUpdateTargetPermission(),
                permChecker.hasDeleteRepositoryPermission(), getAddButtonCommand(), getUpdateButtonCommand(),
                getDeleteButtonCommand(), getCloseButtonCommand());
        this.closeHeaderSupport = new CloseHeaderSupport(i18n, UIComponentIdProvider.SM_SHOW_FILTER_BUTTON_ID,
                this::hideFilterButtonLayout);
        addHeaderSupports(Arrays.asList(crudMenuHeaderSupport, closeHeaderSupport));

        restoreHeaderState();
        buildHeader();
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage(UIMessageIdProvider.CAPTION_FILTER_BY_TYPE)).buildCaptionLabel();
    }

    private Command getAddButtonCommand() {
        return menuItem -> {
            final Window addWindow = smTypeWindowBuilder.getWindowForAddSmType();

            addWindow.setCaption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.type")));
            UI.getCurrent().addWindow(addWindow);
            addWindow.setVisible(Boolean.TRUE);
        };
    }

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

    private void hideFilterButtonLayout() {
        eventBus.publish(EventTopics.LAYOUT_VISIBILITY_CHANGED, this,
                LayoutVisibilityChangedEventPayload.LAYOUT_HIDDEN);

        distSMTypeFilterLayoutUiState.setHidden(true);
    }
}
