/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.disttype;

import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.model.Type;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.TextField;

public class DsTypeWindowLayoutComponentBuilder {

    public static final String TEXTFIELD_KEY = "textfield.key";

    private final VaadinMessageSource i18n;
    private final SoftwareModuleTypeManagement softwareModuleTypeManagement;

    public DsTypeWindowLayoutComponentBuilder(final VaadinMessageSource i18n,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement) {
        this.i18n = i18n;
        this.softwareModuleTypeManagement = softwareModuleTypeManagement;
    }

    // TODO: remove duplication with SmTypeWindowLayoutComponentBuilder
    public TextField createKeyField(final Binder<ProxyType> binder) {
        final TextField typeKey = new TextFieldBuilder(Type.KEY_MAX_SIZE).id(UIComponentIdProvider.TYPE_POPUP_KEY)
                .caption(i18n.getMessage(TEXTFIELD_KEY)).prompt(i18n.getMessage(TEXTFIELD_KEY)).buildTextComponent();
        typeKey.setSizeUndefined();

        // TODO: use i18n for all the required fields messages
        binder.forField(typeKey).asRequired("You must provide type key").bind(ProxyType::getKey, ProxyType::setKey);

        return typeKey;
    }

    public DsTypeSmSelectLayout createDsTypeSmSelectLayout(final Binder<ProxyType> binder) {
        final DsTypeSmSelectLayout dsTypeSmSelectLayout = new DsTypeSmSelectLayout(i18n, softwareModuleTypeManagement);

        // TODO: use i18n for all the required fields messages
        // TODO: check validation and adapt as needed
        binder.forField(dsTypeSmSelectLayout).asRequired("You must select at least one software module")
                .bind(ProxyType::getSelectedSmTypes, ProxyType::setSelectedSmTypes);

        return dsTypeSmSelectLayout;
    }

}
