/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtype;

import org.eclipse.hawkbit.repository.model.Type;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType.SmTypeAssign;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextField;

//TODO: remove duplication with other builders
public class SmTypeWindowLayoutComponentBuilder {

    public static final String TEXTFIELD_KEY = "textfield.key";

    private final VaadinMessageSource i18n;

    public SmTypeWindowLayoutComponentBuilder(final VaadinMessageSource i18n) {
        this.i18n = i18n;
    }

    public TextField createKeyField(final Binder<ProxyType> binder) {
        final TextField typeKey = new TextFieldBuilder(Type.KEY_MAX_SIZE).id(UIComponentIdProvider.TYPE_POPUP_KEY)
                .caption(i18n.getMessage(TEXTFIELD_KEY)).prompt(i18n.getMessage(TEXTFIELD_KEY)).buildTextComponent();
        typeKey.setSizeUndefined();

        binder.forField(typeKey).asRequired(i18n.getMessage("message.type.key.empty")).bind(ProxyType::getKey,
                ProxyType::setKey);

        return typeKey;
    }

    public RadioButtonGroup<SmTypeAssign> createSmTypeAssignOptionGroup(final Binder<ProxyType> binder) {
        final RadioButtonGroup<SmTypeAssign> smTypeAssignOptionGroup = new RadioButtonGroup<>();
        smTypeAssignOptionGroup.setId(UIComponentIdProvider.ASSIGN_OPTION_GROUP_SOFTWARE_MODULE_TYPE_ID);

        smTypeAssignOptionGroup.setItemCaptionGenerator(item -> {
            switch (item) {
            case SINGLE:
                return i18n.getMessage("label.singleAssign.type");
            case MULTI:
                return i18n.getMessage("label.multiAssign.type");
            default:
                return null;
            }
        });
        // TODO: adapt tooltips to describe single/multi assignment
        smTypeAssignOptionGroup.setItemDescriptionGenerator(item -> {
            switch (item) {
            case SINGLE:
                return i18n.getMessage("label.singleAssign.type");
            case MULTI:
                return i18n.getMessage("label.multiAssign.type");
            default:
                return null;
            }
        });

        // TODO: should we mark it as required?
        binder.forField(smTypeAssignOptionGroup).bind(ProxyType::getSmTypeAssign, ProxyType::setSmTypeAssign);
        smTypeAssignOptionGroup.setItems(SmTypeAssign.values());

        return smTypeAssignOptionGroup;
    }
}
