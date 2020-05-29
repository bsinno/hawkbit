/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.builder;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.cronutils.utils.StringUtils;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
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
