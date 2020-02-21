/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtable;

import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.ui.common.builder.FormComponentBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.providers.SoftwareModuleTypeDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

//TODO: remove duplication with other builders
public class SmWindowLayoutComponentBuilder {

    public static final String TEXTFIELD_NAME = "textfield.name";
    public static final String TEXTFIELD_VERSION = "textfield.version";
    public static final String TEXTFIELD_VENDOR = "textfield.vendor";
    public static final String TEXTFIELD_DESCRIPTION = "textfield.description";

    private final VaadinMessageSource i18n;
    private final SoftwareModuleTypeDataProvider smTypeDataProvider;

    public SmWindowLayoutComponentBuilder(final VaadinMessageSource i18n,
            final SoftwareModuleTypeDataProvider smTypeDataProvider) {
        this.i18n = i18n;
        this.smTypeDataProvider = smTypeDataProvider;
    }

    public ComboBox<ProxyType> createSoftwareModuleTypeCombo(final Binder<ProxySoftwareModule> binder) {
        final ComboBox<ProxyType> smTypeSelect = new ComboBox<>(
                i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_SOFTWARE_MODULE_TYPE));

        smTypeSelect.setId(UIComponentIdProvider.SW_MODULE_TYPE);
        smTypeSelect.setDescription(i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_SOFTWARE_MODULE_TYPE));
        smTypeSelect.addStyleName(ValoTheme.COMBOBOX_SMALL);
        smTypeSelect.addStyleName(SPUIDefinitions.COMBO_BOX_SPECIFIC_STYLE);

        smTypeSelect.setItemCaptionGenerator(ProxyType::getName);
        smTypeSelect.setDataProvider(smTypeDataProvider);

        // TODO: use i18n
        binder.forField(smTypeSelect).asRequired("You must provide the software module type")
                .bind(ProxySoftwareModule::getProxyType, ProxySoftwareModule::setProxyType);

        return smTypeSelect;
    }

    /**
     * create name field
     * 
     * @param binder
     *            binder the input will be bound to
     * @return input component
     */
    public TextField createNameField(final Binder<ProxySoftwareModule> binder) {
        return FormComponentBuilder.createNameInput(binder, i18n, UIComponentIdProvider.SOFT_MODULE_NAME)
                .getComponent();
    }

    /**
     * create version field
     * 
     * @param binder
     *            binder the input will be bound to
     * @return input component
     */
    public TextField createVersionField(final Binder<ProxySoftwareModule> binder) {
        return FormComponentBuilder.createVersionInput(binder, i18n, UIComponentIdProvider.SOFT_MODULE_VERSION);
    }

    public TextField createVendorField(final Binder<ProxySoftwareModule> binder) {
        final TextField smVendor = new TextFieldBuilder(SoftwareModule.VENDOR_MAX_SIZE)
                .id(UIComponentIdProvider.SOFT_MODULE_VENDOR).caption(i18n.getMessage(TEXTFIELD_VENDOR))
                .prompt(i18n.getMessage(TEXTFIELD_VENDOR)).buildTextComponent();
        smVendor.setSizeUndefined();

        // TODO: use i18n
        binder.forField(smVendor).bind(ProxySoftwareModule::getVendor, ProxySoftwareModule::setVendor);

        return smVendor;
    }

    /**
     * create description field
     * 
     * @param binder
     *            binder the input will be bound to
     * @return input component
     */
    public TextArea createDescription(final Binder<ProxySoftwareModule> binder) {
        return FormComponentBuilder.createDescriptionInput(binder, i18n,
                UIComponentIdProvider.ADD_SW_MODULE_DESCRIPTION);
    }
}
