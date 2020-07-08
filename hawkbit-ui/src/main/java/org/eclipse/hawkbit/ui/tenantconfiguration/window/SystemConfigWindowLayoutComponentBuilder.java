/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration.window;

import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigWindow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;

import com.vaadin.data.Binder;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Builder for system config window layout component
 */
public class SystemConfigWindowLayoutComponentBuilder {
    private final SystemConfigWindowDependencies dependencies;

    /**
     * Constructor for SystemConfigWindowLayoutComponentBuilder
     *
     * @param dependencies
     *          SystemConfigWindowDependencies
     */
    public SystemConfigWindowLayoutComponentBuilder(final SystemConfigWindowDependencies dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Gets the label
     *
     * @param key
     *          Message key
     *
     * @return Label
     */
    public Label getLabel(final String key) {
        return new LabelBuilder().name(dependencies.getI18n().getMessage(key)).buildLabel();
    }

    /**
     * Create the distribution set combo
     *
     * @param binder
     *          System config window binder
     *
     * @return Distribution set combo box
     */
    public ComboBox<ProxyType> createDistributionSetCombo(final Binder<ProxySystemConfigWindow> binder) {
        final ComboBox<ProxyType> distributionSetType = new ComboBox<>();
        distributionSetType.setDescription(
                dependencies.getI18n().getMessage(UIMessageIdProvider.CAPTION_DISTRIBUTION_TAG));
        distributionSetType.setId(UIComponentIdProvider.SYSTEM_CONFIGURATION_DEFAULTDIS_COMBOBOX);
        distributionSetType.addStyleName(ValoTheme.COMBOBOX_TINY);
        distributionSetType.setWidth(330f, Sizeable.Unit.PIXELS);
        distributionSetType.setEmptySelectionAllowed(false);
        distributionSetType.setItemCaptionGenerator(ProxyType::getKeyAndName);
        distributionSetType.setDataProvider(dependencies.getDistributionSetTypeDataProvider());
        binder.forField(distributionSetType).withConverter(dst -> {
            if (dst == null) {
                return null;
            }

            return dst.getId();
        }, dstId -> {
            if (dstId == null) {
                return null;
            }

            final ProxyType dst = new ProxyType();
            dst.setId(dstId);

            return dst;
        }).bind(ProxySystemConfigWindow::getDistributionSetTypeId, ProxySystemConfigWindow::setDistributionSetTypeId);

        return distributionSetType;
    }

    /**
     * Gets the system config dependencies
     *
     * @return System config window dependencies
     */
    public SystemConfigWindowDependencies getDependencies() {
        return dependencies;
    }
}
