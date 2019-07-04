/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout;

import java.io.Serializable;

import com.vaadin.icons.VaadinIcons;

/**
 * Helper class which holds the details of font icon to be displayed as
 * label/button in grid:
 * <p>
 * <code>RolloutListGrid</code> / <code>RolloutGroupListGrid</code> /
 * <code>RolloutGroupTargetsListGrid</code> / <code>ActionHistoryGrid</code>
 */
public class FontIcon implements Serializable {
    private static final long serialVersionUID = 1L;

    private VaadinIcons icon;
    private String style;
    private String description;

    /**
     * NOTE: This constructor is used for (de-)serialization only!!!
     */
    public FontIcon() {
        // empty
    }

    /**
     * Constructor to create icon metadata object.
     *
     * @param icon
     *            the font representing the icon
     */
    public FontIcon(final VaadinIcons icon) {
        this(icon, "");
    }

    /**
     * Constructor to create icon metadata object.
     *
     * @param icon
     *            the font representing the icon
     * @param style
     *            the style
     */
    public FontIcon(final VaadinIcons icon, final String style) {
        this(icon, style, "");
    }

    /**
     * Constructor to create icon metadata object.
     *
     * @param icon
     *            the font representing the icon
     * @param style
     *            the style
     * @param description
     *            the description shown as tooltip
     */
    public FontIcon(final VaadinIcons icon, final String style, final String description) {
        this.icon = icon;
        this.style = style;
        this.description = description;
    }

    /**
     * Gets the font representing the icon.
     *
     * @return the font representing the icon
     */
    public VaadinIcons getIcon() {
        return icon;
    }

    /**
     * Gets the style.
     *
     * @return the style
     */
    public String getStyle() {
        return style;
    }

    /**
     * Gets the description shown as tooltip.
     *
     * @return the description shown as tooltip.
     */
    public String getDescription() {
        return description;
    }
}
