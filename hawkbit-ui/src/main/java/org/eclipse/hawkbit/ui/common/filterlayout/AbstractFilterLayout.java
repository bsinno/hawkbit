/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.filterlayout;

import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Parent class for filter button layout.
 */
public abstract class AbstractFilterLayout extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    protected void buildLayout() {
        setWidth(SPUIDefinitions.FILTER_BY_TYPE_WIDTH, Unit.PIXELS);
        setStyleName("filter-btns-main-layout");
        setHeight(100.0F, Unit.PERCENTAGE);
        setSpacing(false);
        setMargin(false);

        final Component filterHeader = getFilterHeader();
        final Component filterButtons = getFilterButtons();

        addComponents(filterHeader, filterButtons);

        setComponentAlignment(filterHeader, Alignment.TOP_CENTER);
        setComponentAlignment(filterButtons, Alignment.TOP_CENTER);

        setExpandRatio(filterButtons, 1.0F);
    }

    protected void restoreState() {
        if (isFilterLayoutClosedOnLoad()) {
            setVisible(false);
        }
    }

    protected abstract AbstractGridHeader getFilterHeader();

    // we use Component here due to NO TAG button
    protected abstract Component getFilterButtons();

    /**
     * Check if filter layout should be closed on load.
     * 
     * @return true if filter should be initially closed.
     */
    public abstract Boolean isFilterLayoutClosedOnLoad();
}
