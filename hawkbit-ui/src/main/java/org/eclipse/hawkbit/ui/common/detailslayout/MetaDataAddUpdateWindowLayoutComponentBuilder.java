/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import org.eclipse.hawkbit.repository.model.MetaData;
import org.eclipse.hawkbit.ui.common.builder.TextAreaBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * Builder for add/update window layout component
 */
//TODO: remove duplication with other builders
public class MetaDataAddUpdateWindowLayoutComponentBuilder {

    public static final String TEXTFIELD_KEY = "textfield.key";
    public static final String TEXTFIELD_VALUE = "textfield.value";
    public static final String TARGET_VISIBLE = "metadata.targetvisible";

    private static final int INPUT_DEBOUNCE_TIMEOUT = 250;

    private final VaadinMessageSource i18n;

    /**
     * Constructor for MetaDataAddUpdateWindowLayoutComponentBuilder
     *
     * @param i18n
     *          VaadinMessageSource
     */
    public MetaDataAddUpdateWindowLayoutComponentBuilder(final VaadinMessageSource i18n) {
        this.i18n = i18n;
    }

    /**
     * @param binder
     *          Vaadin binder
     *
     * @return Key textfield
     */
    public TextField createKeyField(final Binder<ProxyMetaData> binder) {
        final TextField keyField = new TextFieldBuilder(MetaData.KEY_MAX_SIZE)
                .id(UIComponentIdProvider.METADATA_KEY_FIELD_ID).caption(i18n.getMessage(TEXTFIELD_KEY))
                .prompt(i18n.getMessage(TEXTFIELD_KEY)).buildTextComponent();
        keyField.setSizeFull();
        keyField.setValueChangeMode(ValueChangeMode.LAZY);
        keyField.setValueChangeTimeout(INPUT_DEBOUNCE_TIMEOUT);

        binder.forField(keyField).asRequired("message.entity.key").bind(ProxyMetaData::getKey,
                ProxyMetaData::setKey);

        return keyField;
    }

    /**
     * @param binder
     *          Vaadin binder
     *
     * @return Value textarea
     */
    public TextArea createValueField(final Binder<ProxyMetaData> binder) {
        final TextArea valueField = new TextAreaBuilder(MetaData.VALUE_MAX_SIZE)
                .id(UIComponentIdProvider.METADATA_VALUE_ID).caption(i18n.getMessage(TEXTFIELD_VALUE))
                .prompt(i18n.getMessage(TEXTFIELD_VALUE)).buildTextComponent();
        valueField.setSizeFull();
        valueField.setValueChangeMode(ValueChangeMode.LAZY);
        valueField.setValueChangeTimeout(INPUT_DEBOUNCE_TIMEOUT);

        binder.forField(valueField).asRequired(i18n.getMessage("message.entity.value")).bind(ProxyMetaData::getValue,
                ProxyMetaData::setValue);

        return valueField;
    }

    /**
     * @param binder
     *          Vaadin binder
     *
     * @return Target field CheckBox
     */
    public CheckBox createVisibleForTargetsField(final Binder<ProxyMetaData> binder) {
        return SPUIComponentProvider.getCheckBox(i18n.getMessage(TARGET_VISIBLE),
                UIComponentIdProvider.METADATA_TARGET_VISIBLE_ID, binder, ProxyMetaData::isTargetVisible,
                ProxyMetaData::setTargetVisible);
    }
}
