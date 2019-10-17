/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstag;

import org.eclipse.hawkbit.ui.components.CustomColorPicker;
import org.eclipse.hawkbit.ui.management.tag.AbstractTagWindowLayout;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

public class AddDsTagWindowLayout extends AbstractTagWindowLayout {
    private static final long serialVersionUID = 1L;

    protected final TextField tagName;

    public AddDsTagWindowLayout(final VaadinMessageSource i18n) {
        super(i18n);

        this.tagName = componentBuilder.createNameField(UIComponentIdProvider.NEW_DISTRIBUTION_TAG_NAME,
                proxyTagBinder);

        buildLayout();
    }

    public void buildLayout() {
        final FormLayout formLayout = new FormLayout();

        formLayout.addComponent(tagName);
        tagName.focus();

        formLayout.addComponent(
                componentBuilder.createDescription(UIComponentIdProvider.NEW_DISTRIBUTION_TAG_DESC, proxyTagBinder));

        final CustomColorPicker colorPickerBtn = componentBuilder
                .createColorPickerButton(UIComponentIdProvider.TAG_COLOR_PREVIEW_ID, proxyTagBinder);
        formLayout.addComponent(colorPickerBtn);

        addComponent(formLayout);
        addComponent(colorPickerBtn.getColorPickerLayout());
    }

}
