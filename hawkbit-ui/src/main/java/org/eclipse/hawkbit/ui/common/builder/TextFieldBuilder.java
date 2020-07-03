/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.builder;

import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Textfield builder.
 *
 */
public class TextFieldBuilder extends AbstractTextFieldBuilder<TextFieldBuilder, TextField> {

    /**
     * Constructor.
     * 
     * @param maxLengthAllowed
     *            as mandatory field
     */
    public TextFieldBuilder(final int maxLengthAllowed) {
        super(maxLengthAllowed);
        styleName(ValoTheme.TEXTAREA_TINY);
    }

    @Override
    protected TextField createTextComponent() {
        final TextField textField = new TextField();
        // TODO: should we use ValueChangeMode.LAZY here, see
        // SearchHeaderSupport#createSearchField?
        textField.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        return textField;
    }

}
