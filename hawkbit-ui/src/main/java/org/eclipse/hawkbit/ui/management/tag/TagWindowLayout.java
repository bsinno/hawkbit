/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.tag;

import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyFilterButton;
import org.eclipse.hawkbit.ui.components.ColorPickerComponent;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * Abstract class for tag add/update window layout.
 */
public class TagWindowLayout<T extends ProxyFilterButton> extends HorizontalLayout {
    private static final long serialVersionUID = 1L;

    protected final Binder<T> binder;

    protected final TagWindowLayoutComponentBuilder componentBuilder;

    protected final VaadinMessageSource i18n;

    protected final TextField tagName;
    protected final TextArea tagDescription;
    protected final FormLayout formLayout;
    protected final ColorPickerComponent colorPickerComponent;

    /**
     * Constructor for AbstractTagWindowLayout
     * 
     * @param i18n
     *            I18N
     */
    public TagWindowLayout(final VaadinMessageSource i18n) {
        this.i18n = i18n;
        this.binder = new Binder<>();
        this.componentBuilder = new TagWindowLayoutComponentBuilder(i18n);

        this.tagName = componentBuilder.createNameField(binder);
        this.tagDescription = componentBuilder.createDescription(binder);
        this.formLayout = new FormLayout();

        this.colorPickerComponent = componentBuilder.createColorPickerComponent(binder);

        initLayout();
        buildLayout();
    }

    private void initLayout() {
        setSpacing(true);
        setMargin(false);
        setSizeUndefined();
    }

    private void buildLayout() {
        formLayout.addComponent(tagName);
        tagName.focus();

        formLayout.addComponent(tagDescription);

        formLayout.addComponent(colorPickerComponent.getColorPickerBtn());

        addComponent(formLayout);
        addComponent(colorPickerComponent);
    }

    public Binder<T> getBinder() {
        return binder;
    }

    public void addValidationListener(final Consumer<Boolean> validationCallback) {
        binder.addStatusChangeListener(event -> validationCallback.accept(event.getBinder().isValid()));
    }

    public void disableTagName() {
        tagName.setEnabled(false);
    }
}
