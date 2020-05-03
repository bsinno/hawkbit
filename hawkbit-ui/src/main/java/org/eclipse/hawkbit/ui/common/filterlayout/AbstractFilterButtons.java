/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.filterlayout;

import java.util.Collection;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.GridComponentBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyFilterButton;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUITagButtonStyle;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Parent class for filter button layout.
 * 
 * @param <T>
 *            The type of the Filter Button
 * @param <F>
 *            The filter-type used by the grid
 */
public abstract class AbstractFilterButtons<T extends ProxyFilterButton, F> extends AbstractGrid<T, F> {
    private static final long serialVersionUID = 1L;

    protected static final String DEFAULT_GREEN = "rgb(44,151,32)";

    protected static final String FILTER_BUTTON_COLUMN_ID = "filterButton";
    protected static final String FILTER_BUTTON_EDIT_ID = "filterButtonEdit";
    protected static final String FILTER_BUTTON_DELETE_ID = "filterButtonDelete";

    protected final transient DeleteSupport<T> filterButtonDeleteSupport;

    protected AbstractFilterButtons(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final UINotification notification, final SpPermissionChecker permChecker) {
        super(i18n, eventBus, permChecker);

        this.filterButtonDeleteSupport = new DeleteSupport<>(this, i18n, notification, getFilterButtonsType(),
                ProxyFilterButton::getName, this::deleteFilterButtons,
                UIComponentIdProvider.FILTER_BUTTON_DELETE_CONFIRMATION_DIALOG);
    }

    @Override
    protected void init() {
        super.init();

        setHeaderVisible(false);
        setStyleName("type-button-layout");
        addStyleName(ValoTheme.TABLE_NO_STRIPES);
        addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
        addStyleName(ValoTheme.TABLE_BORDERLESS);
        addStyleName(ValoTheme.TABLE_COMPACT);
    }

    protected abstract String getFilterButtonsType();

    protected abstract boolean deleteFilterButtons(Collection<T> filterButtonsToDelete);

    @Override
    public void addColumns() {
        addComponentColumn(this::buildFilterButton).setId(FILTER_BUTTON_COLUMN_ID).setMinimumWidth(120d)
                .setStyleGenerator(item -> {
                    if (getFilterButtonClickBehaviour().isFilterPreviouslyClicked(item)) {
                        return SPUIStyleDefinitions.SP_FILTER_BTN_CLICKED_STYLE;
                    } else {
                        return null;
                    }
                });
        addComponentColumn(this::buildEditFilterButton).setId(FILTER_BUTTON_EDIT_ID).setHidden(true);
        addComponentColumn(this::buildDeleteFilterButton).setId(FILTER_BUTTON_DELETE_ID).setHidden(true);
    }

    private Button buildFilterButton(final T clickedFilter) {
        final Button filterButton = SPUIComponentProvider.getButton(
                getFilterButtonIdPrefix() + "." + clickedFilter.getId(), clickedFilter.getName(),
                clickedFilter.getName(), null, false, null, SPUITagButtonStyle.class);
        filterButton.addStyleName(SPUIStyleDefinitions.FILTER_BUTTON_WRAPPER);
        filterButton.addStyleName("text-cut");

        final String colour = clickedFilter.getColour() != null ? clickedFilter.getColour() : DEFAULT_GREEN;
        filterButton.setCaption(
                new ProxyFontIcon(VaadinIcons.CIRCLE, "", "", colour).getHtml() + " " + clickedFilter.getName());
        filterButton.setCaptionAsHtml(true);

        filterButton.addClickListener(event -> getFilterButtonClickBehaviour().processFilterClick(clickedFilter));

        return filterButton;
    }

    /**
     * Get prefix Id of Button Wrapper to be used for drag and drop, delete and
     * test cases.
     *
     * @return prefix Id of Button Wrapper
     */
    protected abstract String getFilterButtonIdPrefix();

    protected abstract AbstractFilterButtonClickBehaviour<T> getFilterButtonClickBehaviour();

    private Button buildEditFilterButton(final T clickedFilter) {
        // TODO: check permissions for enable/disable
        return GridComponentBuilder.buildActionButton(i18n, clickEvent -> editButtonClickListener(clickedFilter),
                VaadinIcons.EDIT, SPUIDefinitions.EDIT, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                getFilterButtonIdPrefix() + ".edit." + clickedFilter.getId(), true);
    }

    protected abstract void editButtonClickListener(final T clickedFilter);

    private Button buildDeleteFilterButton(final T clickedFilter) {
        return GridComponentBuilder.buildActionButton(i18n,
                clickEvent -> filterButtonDeleteSupport.openConfirmationWindowDeleteAction(clickedFilter),
                VaadinIcons.TRASH, UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                getFilterButtonIdPrefix() + ".delete." + clickedFilter.getId(), isDeletionAllowed());
    }

    protected abstract boolean isDeletionAllowed();

    /**
     * Hides the edit and delete icon next to the filter tags in target,
     * distribution and software module tags/types layouts.
     */
    public void hideActionColumns() {
        getColumn(FILTER_BUTTON_EDIT_ID).setHidden(true);
        getColumn(FILTER_BUTTON_DELETE_ID).setHidden(true);
    }

    public void showDeleteColumn() {
        getColumn(FILTER_BUTTON_DELETE_ID).setHidden(false);
    }

    public void showEditColumn() {
        getColumn(FILTER_BUTTON_EDIT_ID).setHidden(false);
    }
}
