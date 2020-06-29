/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
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
import org.springframework.util.CollectionUtils;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationResult;
import com.vaadin.ui.TextField;

/**
 * Builder for Distribution set type window layout component
 */
public class DsTypeWindowLayoutComponentBuilder {

    public static final String TEXTFIELD_KEY = "textfield.key";

    private final VaadinMessageSource i18n;
    private final SoftwareModuleTypeManagement softwareModuleTypeManagement;

    /**
     * Constructor for DsTypeWindowLayoutComponentBuilder
     *
     * @param i18n
     *          VaadinMessageSource
     * @param softwareModuleTypeManagement
     *          SoftwareModuleTypeManagement
     */
    public DsTypeWindowLayoutComponentBuilder(final VaadinMessageSource i18n,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement) {
        this.i18n = i18n;
        this.softwareModuleTypeManagement = softwareModuleTypeManagement;
    }

    /**
     * @param binder
     *          Vadin binder
     *
     * @return Key text field
     */
    // TODO: remove duplication with SmTypeWindowLayoutComponentBuilder
    public TextField createKeyField(final Binder<ProxyType> binder) {
        final TextField typeKey = new TextFieldBuilder(Type.KEY_MAX_SIZE).id(UIComponentIdProvider.TYPE_POPUP_KEY)
                .caption(i18n.getMessage(TEXTFIELD_KEY)).prompt(i18n.getMessage(TEXTFIELD_KEY)).buildTextComponent();
        typeKey.setSizeUndefined();

        binder.forField(typeKey).asRequired(i18n.getMessage("message.key.missing")).bind(ProxyType::getKey, ProxyType::setKey);

        return typeKey;
    }

    /**
     * @param binder
     *          Vaadin binder
     *
     * @return layout of distribution set software module selection
     */
    public DsTypeSmSelectLayout createDsTypeSmSelectLayout(final Binder<ProxyType> binder) {
        final DsTypeSmSelectLayout dsTypeSmSelectLayout = new DsTypeSmSelectLayout(i18n, softwareModuleTypeManagement);
        dsTypeSmSelectLayout.setRequiredIndicatorVisible(true);

        binder.forField(dsTypeSmSelectLayout)
                .withValidator((selectedSmTypes, context) -> CollectionUtils.isEmpty(selectedSmTypes)
                        ? ValidationResult.error(i18n.getMessage("message.select.softwaremodule"))
                        : ValidationResult.ok())
                .bind(ProxyType::getSelectedSmTypes, ProxyType::setSelectedSmTypes);

        return dsTypeSmSelectLayout;
    }

}
