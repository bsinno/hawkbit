/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
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
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

//TODO: remove duplication with other builders
public class MetaDataAddUpdateWindowLayoutComponentBuilder {

    public static final String TEXTFIELD_KEY = "textfield.key";
    public static final String TEXTFIELD_VALUE = "textfield.value";
    public static final String TARGET_VISIBLE = "metadata.targetvisible";

    private static final int INPUT_DEBOUNCE_TIMEOUT = 250;

    private final VaadinMessageSource i18n;

    public MetaDataAddUpdateWindowLayoutComponentBuilder(final VaadinMessageSource i18n) {
        this.i18n = i18n;
    }

    public TextField createKeyField(final Binder<ProxyMetaData> binder) {
        final TextField keyField = new TextFieldBuilder(MetaData.KEY_MAX_SIZE)
                .id(UIComponentIdProvider.METADATA_KEY_FIELD_ID).caption(i18n.getMessage(TEXTFIELD_KEY))
                .prompt(i18n.getMessage(TEXTFIELD_KEY)).buildTextComponent();
        keyField.setSizeFull();
        keyField.setValueChangeMode(ValueChangeMode.LAZY);
        keyField.setValueChangeTimeout(INPUT_DEBOUNCE_TIMEOUT);

        // TODO: use i18n for all the required fields messages
        binder.forField(keyField).asRequired("You must provide metadata key").bind(ProxyMetaData::getKey,
                ProxyMetaData::setKey);

        return keyField;
    }

    public TextArea createValueField(final Binder<ProxyMetaData> binder) {
        final TextArea valueField = new TextAreaBuilder(MetaData.VALUE_MAX_SIZE)
                .id(UIComponentIdProvider.METADATA_VALUE_ID).caption(i18n.getMessage(TEXTFIELD_VALUE))
                .prompt(i18n.getMessage(TEXTFIELD_VALUE)).buildTextComponent();
        valueField.setSizeFull();
        valueField.setValueChangeMode(ValueChangeMode.LAZY);
        valueField.setValueChangeTimeout(INPUT_DEBOUNCE_TIMEOUT);

        // TODO: use i18n for all the required fields messages
        binder.forField(valueField).asRequired("You must provide metadata value").bind(ProxyMetaData::getValue,
                ProxyMetaData::setValue);

        return valueField;
    }

    public CheckBox createVisibleForTargetsField(final Binder<ProxyMetaData> binder) {
        final CheckBox visibleForTargetsField = new CheckBox(i18n.getMessage(TARGET_VISIBLE));
        visibleForTargetsField.setId(UIComponentIdProvider.METADATA_TARGET_VISIBLE_ID);

        binder.forField(visibleForTargetsField).bind(ProxyMetaData::isTargetVisible, ProxyMetaData::setTargetVisible);

        return visibleForTargetsField;
    }
}
