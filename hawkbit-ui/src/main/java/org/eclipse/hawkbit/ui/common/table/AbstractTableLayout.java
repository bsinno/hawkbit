/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.table;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyNamedEntity;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractTableDetailsLayout;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.components.RefreshableContainer;
import org.eclipse.hawkbit.ui.utils.ShortCutModifierUtils;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Parent class for table layout.
 * 
 *
 * @param <T>
 *            type of the concrete table
 */
public abstract class AbstractTableLayout<T extends ProxyNamedEntity> extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private AbstractTableHeader tableHeader;

    private AbstractGrid<T, ?> grid;

    private AbstractTableDetailsLayout<T> detailsLayout;

    private VaadinMessageSource i18n;

    protected void init(final VaadinMessageSource i18n, final AbstractTableHeader tableHeader,
            final AbstractGrid<T, ?> grid, final AbstractTableDetailsLayout<T> detailsLayout) {
        this.i18n = i18n;
        this.tableHeader = tableHeader;
        this.grid = grid;
        this.detailsLayout = detailsLayout;
        buildLayout();

        // TODO: check if it is correct
        if (grid.hasSelectionSupport()) {
            grid.getSelectionSupport().selectFirstRow();
        }
    }

    private void buildLayout() {
        setSizeFull();
        setSpacing(true);
        setMargin(false);
        setStyleName("group");
        final VerticalLayout tableHeaderLayout = new VerticalLayout();
        tableHeaderLayout.setSizeFull();
        tableHeaderLayout.setSpacing(false);
        tableHeaderLayout.setMargin(false);

        tableHeaderLayout.setStyleName("table-layout");
        tableHeaderLayout.addComponent(tableHeader);

        tableHeaderLayout.setComponentAlignment(tableHeader, Alignment.TOP_CENTER);
        if (isShortCutKeysRequired()) {
            final Panel tablePanel = new Panel();
            tablePanel.setStyleName("table-panel");
            tablePanel.setHeight(100.0F, Unit.PERCENTAGE);
            tablePanel.setContent(grid);
            tablePanel.addActionHandler(getShortCutKeysHandler(i18n));
            tablePanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
            tableHeaderLayout.addComponent(tablePanel);
            tableHeaderLayout.setComponentAlignment(tablePanel, Alignment.TOP_CENTER);
            tableHeaderLayout.setExpandRatio(tablePanel, 1.0F);
        } else {
            tableHeaderLayout.addComponent(grid);
            tableHeaderLayout.setComponentAlignment(grid, Alignment.TOP_CENTER);
            tableHeaderLayout.setExpandRatio(grid, 1.0F);
        }

        addComponent(tableHeaderLayout);
        addComponent(detailsLayout);
        setComponentAlignment(tableHeaderLayout, Alignment.TOP_CENTER);
        setComponentAlignment(detailsLayout, Alignment.TOP_CENTER);
        setExpandRatio(tableHeaderLayout, 1.0F);
    }

    /**
     * If any short cut keys required on the table.
     * 
     * @return true if required else false. Default is 'true'.
     */
    protected boolean isShortCutKeysRequired() {
        return true;
    }

    /**
     * Get the action handler for the short cut keys.
     * 
     * @return reference of {@link Handler} to handler the short cut keys.
     *         Default is null.
     */
    protected Handler getShortCutKeysHandler(final VaadinMessageSource i18n) {
        return new TableShortCutHandler(i18n);
    }

    protected void publishEvent() {
        // can be override by subclasses
    }

    public void setShowFilterButtonVisible(final boolean visible) {
        tableHeader.setFilterButtonsIconVisible(visible);
    }

    public RefreshableContainer getTable() {
        return grid;
    }

    private final class TableShortCutHandler implements Handler {

        private static final long serialVersionUID = 1L;

        private final String selectAllText;

        private final ShortcutAction selectAllAction;

        private TableShortCutHandler(final VaadinMessageSource i18n) {
            selectAllText = i18n.getMessage("action.target.table.selectall");
            selectAllAction = new ShortcutAction(selectAllText, ShortcutAction.KeyCode.A,
                    new int[] { ShortCutModifierUtils.getCtrlOrMetaModifier() });
        }

        @Override
        public void handleAction(final Action action, final Object sender, final Object target) {
            if (!selectAllAction.equals(action)) {
                return;
            }
            grid.getSelectionSupport().selectAll();
            publishEvent();
        }

        @Override
        public Action[] getActions(final Object target, final Object sender) {
            return new Action[] { selectAllAction };
        }
    }

}
