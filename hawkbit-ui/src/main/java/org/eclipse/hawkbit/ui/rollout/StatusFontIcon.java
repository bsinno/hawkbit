/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout;

import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.RolloutGroup;

import com.google.gwt.i18n.client.AutoDirectionHandler.Target;
import com.vaadin.server.FontAwesome;

/**
 * 
 * Helper class which holds the details of font icon to be displayed for
 * {@link Rollout}/ {@link RolloutGroup} / Rollout {@link Target}
 * 
 *
 */
public class StatusFontIcon {
    final FontAwesome fontIcon;
    final String style;

    public StatusFontIcon(final FontAwesome fontIcon, final String style) {
        super();
        this.fontIcon = fontIcon;
        this.style = style;
    }

    public FontAwesome getFontIcon() {
        return fontIcon;
    }

    public String getStyle() {
        return style;
    }

}
