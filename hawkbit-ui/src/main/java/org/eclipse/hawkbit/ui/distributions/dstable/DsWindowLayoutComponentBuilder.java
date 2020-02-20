/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import org.eclipse.hawkbit.ui.common.builder.FormComponentBuilder;
import org.eclipse.hawkbit.ui.common.builder.FormComponentBuilder.BindType;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetTypeDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

//TODO: remove duplication with other builders
public class DsWindowLayoutComponentBuilder {

    public static final String SELECT_TYPE = "label.combobox.type";
    public static final String TEXTFIELD_NAME = "textfield.name";
    public static final String TEXTFIELD_VERSION = "textfield.version";
    public static final String TEXTFIELD_DESCRIPTION = "textfield.description";
    public static final String MIGRATION_STEP = "checkbox.dist.required.migration.step";

    private final VaadinMessageSource i18n;
    private final DistributionSetTypeDataProvider dsTypeDataProvider;

    public DsWindowLayoutComponentBuilder(final VaadinMessageSource i18n,
            final DistributionSetTypeDataProvider dsTypeDataProvider) {
        this.i18n = i18n;
        this.dsTypeDataProvider = dsTypeDataProvider;
    }

    public ComboBox<ProxyType> createDistributionSetTypeCombo(final Binder<ProxyDistributionSet> binder) {
        final ComboBox<ProxyType> dsTypeSelect = new ComboBox<>(i18n.getMessage(SELECT_TYPE));

        dsTypeSelect.setId(UIComponentIdProvider.DIST_ADD_DISTSETTYPE);
        dsTypeSelect.setDescription(i18n.getMessage(SELECT_TYPE));
        dsTypeSelect.addStyleName(ValoTheme.COMBOBOX_SMALL);

        dsTypeSelect.setItemCaptionGenerator(ProxyType::getName);
        dsTypeSelect.setDataProvider(dsTypeDataProvider);

        // TODO: use i18n
        binder.forField(dsTypeSelect).asRequired("You must provide the distribution set type")
                .bind(ProxyDistributionSet::getProxyType, ProxyDistributionSet::setProxyType);

        return dsTypeSelect;
    }

    /**
     * create name field
     * 
     * @param binder
     *            binder the input will be bound to
     * @return input component
     */
    public TextField createNameField(final Binder<ProxyDistributionSet> binder) {
        return FormComponentBuilder.createNameInput(binder, i18n, UIComponentIdProvider.DIST_ADD_NAME, BindType.REQUIRED);
    }

    /**
     * create version field
     * 
     * @param binder
     *            binder the input will be bound to
     * @return input component
     */
    public TextField createVersionField(final Binder<ProxyDistributionSet> binder) {
        return FormComponentBuilder.createVersionInput(binder, i18n, UIComponentIdProvider.DIST_ADD_VERSION);
    }

    /**
     * create description field
     * 
     * @param binder
     *            binder the input will be bound to
     * @return input component
     */
    public TextArea createDescription(final Binder<ProxyDistributionSet> binder) {
        return FormComponentBuilder.createDescriptionInput(binder, i18n, UIComponentIdProvider.DIST_ADD_DESC);
    }

    public CheckBox createMigrationStepField(final Binder<ProxyDistributionSet> binder) {
        final CheckBox dsMigrationStepRequired = new CheckBox(i18n.getMessage(MIGRATION_STEP));

        dsMigrationStepRequired.setId(UIComponentIdProvider.DIST_ADD_MIGRATION_CHECK);
        dsMigrationStepRequired.setDescription(i18n.getMessage(MIGRATION_STEP));
        dsMigrationStepRequired.addStyleName("dist-checkbox-style");
        dsMigrationStepRequired.addStyleName(ValoTheme.CHECKBOX_SMALL);

        binder.forField(dsMigrationStepRequired).bind(ProxyDistributionSet::isRequiredMigrationStep,
                ProxyDistributionSet::setRequiredMigrationStep);

        return dsMigrationStepRequired;
    }
}
