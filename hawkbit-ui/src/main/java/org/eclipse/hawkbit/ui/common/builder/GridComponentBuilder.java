/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.builder;

import java.util.function.Predicate;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyNamedEntity;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.cronutils.utils.StringUtils;
import com.vaadin.data.ValueProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Builder class for grid components
 */
public final class GridComponentBuilder {
    private GridComponentBuilder() {
    }

    /**
     * Create a {@link Button} with link optic
     * 
     * @param idSuffix
     *            suffix to build the button ID
     * @param idPrefix
     *            prefix to build the button ID
     * @param caption
     *            button caption
     * @param enabled
     *            is button enabled
     * @param clickListener
     *            execute on button click (null for none)
     * @return the button
     */
    public static Button buildLink(final String idSuffix, final String idPrefix, final String caption,
            final boolean enabled, final ClickListener clickListener) {
        final Button link = new Button();
        final String id = new StringBuilder(idPrefix).append('.').append(idSuffix).toString();

        link.setCaption(caption);
        link.setEnabled(enabled);
        link.setId(id);
        link.addStyleName("borderless");
        link.addStyleName("small");
        link.addStyleName("on-focus-no-border");
        link.addStyleName("link");
        if (clickListener != null) {
            link.addClickListener(clickListener);
        }
        link.setVisible(!StringUtils.isEmpty(caption));
        return link;
    }

    /**
     * Create a {@link Button} with link optic
     * 
     * @param entity
     *            to build the button ID
     * @param idPrefix
     *            prefix to build the button ID
     * @param caption
     *            button caption
     * @param enabled
     *            is button enabled
     * @param clickListener
     *            execute on button click (null for none)
     * @return the button
     */
    public static <E extends ProxyIdentifiableEntity> Button buildLink(final E entity, final String idPrefix,
            final String caption, final boolean enabled, final ClickListener clickListener) {
        return buildLink(entity.getId().toString(), idPrefix, caption, enabled, clickListener);
    }

    /**
     * Add name column to grid
     * 
     * @param <E>
     *            entity type of the grid
     * @param grid
     *            to add the column to
     * @param i18n
     *            message source for internationalization
     * @param columnId
     *            column ID
     * @return the created column
     */
    public static <E extends ProxyNamedEntity> Column<E, String> addNameColumn(final Grid<E> grid,
            final VaadinMessageSource i18n, final String columnId) {
        return addColumn(i18n, grid, E::getName, "header.name", columnId, 100D);
    }

    /**
     * Add description column to grid
     * 
     * @param <E>
     *            entity type of the grid
     * @param grid
     *            to add the column to
     * @param i18n
     *            message source for internationalization
     * @param columnId
     *            column ID
     * @return the created column
     */
    public static <E extends ProxyNamedEntity> Column<E, String> addDescriptionColumn(final Grid<E> grid,
            final VaadinMessageSource i18n, final String columnId) {
        return addColumn(i18n, grid, E::getDescription, "header.description", columnId, 100D);
    }

    /**
     * Add CreatedBy column to grid
     * 
     * @param <E>
     *            entity type of the grid
     * @param grid
     *            to add the column to
     * @param i18n
     *            message source for internationalization
     * @param columnId
     *            column ID
     * @return the created column
     */
    public static <E extends ProxyNamedEntity> Column<E, String> addCreatedByColumn(final Grid<E> grid,
            final VaadinMessageSource i18n, final String columnId) {
        return addColumn(i18n, grid, E::getCreatedBy, "header.createdBy", columnId, 100D);
    }

    /**
     * Add CreatedAt column to grid
     * 
     * @param <E>
     *            entity type of the grid
     * @param grid
     *            to add the column to
     * @param i18n
     *            message source for internationalization
     * @param columnId
     *            column ID
     * @return the created column
     */
    public static <E extends ProxyNamedEntity> Column<E, String> addCreatedAtColumn(final Grid<E> grid,
            final VaadinMessageSource i18n, final String columnId) {
        return addColumn(i18n, grid, E::getCreatedDate, "header.createdDate", columnId, 100D);
    }

    /**
     * Add ModifiedBy column to grid
     * 
     * @param <E>
     *            entity type of the grid
     * @param grid
     *            to add the column to
     * @param i18n
     *            message source for internationalization
     * @param columnId
     *            column ID
     * @return the created column
     */
    public static <E extends ProxyNamedEntity> Column<E, String> addModifiedByColumn(final Grid<E> grid,
            final VaadinMessageSource i18n, final String columnId) {
        return addColumn(i18n, grid, E::getLastModifiedBy, "header.modifiedBy", columnId, 100D);
    }

    /**
     * Add ModifiedAt column to grid
     * 
     * @param <E>
     *            entity type of the grid
     * @param grid
     *            to add the column to
     * @param i18n
     *            message source for internationalization
     * @param columnId
     *            column ID
     * @return the created column
     */
    public static <E extends ProxyNamedEntity> Column<E, String> addModifiedAtColumn(final Grid<E> grid,
            final VaadinMessageSource i18n, final String columnId) {
        return addColumn(i18n, grid, E::getModifiedDate, "header.modifiedDate", columnId, 100D);
    }

    /**
     * Add version column to grid
     * 
     * @param <E>
     *            entity type of the grid
     * @param grid
     *            to add the column to
     * @param i18n
     *            message source for internationalization
     * @param valueProvider
     *            to get the version of the entity
     * @param columnId
     *            column ID
     * @return the created column
     */
    public static <E> Column<E, String> addVersionColumn(final Grid<E> grid, final VaadinMessageSource i18n,
            final ValueProvider<E, String> valueProvider, final String columnId) {
        return addColumn(i18n, grid, valueProvider, "header.version", columnId, 100D);
    }

    private static <E> Column<E, String> addColumn(final VaadinMessageSource i18n, final Grid<E> grid,
            final ValueProvider<E, String> valueProvider, final String caption, final String columnID,
            final double minWidth) {
        return grid.addColumn(valueProvider).setId(columnID).setCaption(i18n.getMessage(caption))
                .setMinimumWidth(minWidth);
    }

    /**
     * Add delete button column to grid
     * 
     * @param <E>
     *            entity type of the grid
     * @param grid
     *            to add the column to
     * @param i18n
     *            message source for internationalization
     * @param columnId
     *            column ID
     * @param deleteSupport
     *            that executes the deletion
     * @param buttonIdPrefix
     *            prefix to create the button IDs
     * @param buttonEnabled
     *            is the button enabled
     * @return the created column
     */
    public static <E extends ProxyIdentifiableEntity> Column<E, Button> addDeleteColumn(final Grid<E> grid,
            final VaadinMessageSource i18n, final String columnId, final DeleteSupport<E> deleteSupport,
            final String buttonIdPrefix, final Predicate<E> buttonEnabled) {
        final ValueProvider<E, Button> getDelButton = entity -> buildActionButton(i18n,
                clickEvent -> deleteSupport.openConfirmationWindowDeleteAction(entity), VaadinIcons.TRASH,
                UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                buttonIdPrefix + "." + entity.getId(), buttonEnabled.test(entity));
        return grid.addComponentColumn(getDelButton).setId(columnId).setCaption(i18n.getMessage("header.action.delete"))
                .setWidth(50D);
    }

    /**
     * Create an action button (e.g. a delete button)
     * 
     * @param i18n
     *            message source for internationalization
     * @param clickListener
     *            clickListener
     * @param icon
     *            icon of the button
     * @param descriptionMsgProperty
     *            displayed as tool tip
     * @param style
     *            additional style
     * @param buttonId
     *            ID of the button
     * @param enabled
     *            is the button enabled
     * @return the button
     */
    public static Button buildActionButton(final VaadinMessageSource i18n, final ClickListener clickListener,
            final Resource icon, final String descriptionMsgProperty, final String style, final String buttonId,
            final boolean enabled) {
        final Button actionButton = new Button();

        actionButton.addClickListener(clickListener);
        actionButton.setIcon(icon, i18n.getMessage(descriptionMsgProperty));
        actionButton.setDescription(i18n.getMessage(descriptionMsgProperty));
        actionButton.setEnabled(enabled);
        actionButton.setId(buttonId);
        actionButton.addStyleName(ValoTheme.LABEL_TINY);
        actionButton.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        actionButton.addStyleName("button-no-border");
        actionButton.addStyleName("action-type-padding");
        actionButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        actionButton.addStyleName(style);

        return actionButton;
    }
}
