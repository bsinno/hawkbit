/**
l * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import org.eclipse.hawkbit.ui.common.builder.FormComponentBuilder;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetTypeDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.TypeInfo;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Builder for Distribution set window layout component
 */
// TODO: remove duplication with other builders
public class DsWindowLayoutComponentBuilder {

    public static final String SELECT_TYPE = "label.combobox.type";
    public static final String TEXTFIELD_NAME = "textfield.name";
    public static final String TEXTFIELD_VERSION = "textfield.version";
    public static final String TEXTFIELD_DESCRIPTION = "textfield.description";
    public static final String MIGRATION_STEP = "label.dist.required.migration.step";

    private final VaadinMessageSource i18n;
    private final DistributionSetTypeDataProvider<TypeInfo> dsTypeDataProvider;

    /**
     * Constructor for DsWindowLayoutComponentBuilder
     *
     * @param i18n
     *            VaadinMessageSource
     * @param dsTypeDataProvider
     *            DistributionSetTypeDataProvider
     */
    public DsWindowLayoutComponentBuilder(final VaadinMessageSource i18n,
            final DistributionSetTypeDataProvider<TypeInfo> dsTypeDataProvider) {
        this.i18n = i18n;
        this.dsTypeDataProvider = dsTypeDataProvider;
    }

    /**
     * @param binder
     *            Vaddin binder
     *
     * @return Distribution set type combobox
     */
    public ComboBox<TypeInfo> createDistributionSetTypeCombo(final Binder<ProxyDistributionSet> binder) {
        final ComboBox<TypeInfo> dsTypeSelect = new ComboBox<>(i18n.getMessage(SELECT_TYPE));

        dsTypeSelect.setId(UIComponentIdProvider.DIST_ADD_DISTSETTYPE);
        dsTypeSelect.setDescription(i18n.getMessage(SELECT_TYPE));
        dsTypeSelect.addStyleName(ValoTheme.COMBOBOX_SMALL);

        dsTypeSelect.setItemCaptionGenerator(TypeInfo::getName);
        dsTypeSelect.setDataProvider(dsTypeDataProvider);

        binder.forField(dsTypeSelect).asRequired(i18n.getMessage("message.error.distributionSetRequired"))
                .bind(ProxyDistributionSet::getTypeInfo, ProxyDistributionSet::setTypeInfo);

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
        return FormComponentBuilder.createNameInput(binder, i18n, UIComponentIdProvider.DIST_ADD_NAME).getComponent();
    }

    /**
     * create version field
     * 
     * @param binder
     *            binder the input will be bound to
     * @return input component
     */
    public TextField createVersionField(final Binder<ProxyDistributionSet> binder) {
        return FormComponentBuilder.createVersionInput(binder, i18n, UIComponentIdProvider.DIST_ADD_VERSION)
                .getComponent();
    }

    /**
     * create description field
     * 
     * @param binder
     *            binder the input will be bound to
     * @return input component
     */
    public TextArea createDescription(final Binder<ProxyDistributionSet> binder) {
        return FormComponentBuilder.createDescriptionInput(binder, i18n, UIComponentIdProvider.DIST_ADD_DESC)
                .getComponent();
    }

    /**
     * @param binder
     *            Vaadin binder
     *
     * @return Migration step required checkbox
     */
    public CheckBox createMigrationStepField(final Binder<ProxyDistributionSet> binder) {
        final CheckBox migrationRequired = FormComponentBuilder.getCheckBox(i18n.getMessage(MIGRATION_STEP),
                UIComponentIdProvider.DIST_ADD_MIGRATION_CHECK, binder, ProxyDistributionSet::isRequiredMigrationStep,
                ProxyDistributionSet::setRequiredMigrationStep);

        migrationRequired.setDescription(i18n.getMessage(MIGRATION_STEP));
        migrationRequired.addStyleName("dist-checkbox-style");
        migrationRequired.addStyleName(ValoTheme.CHECKBOX_SMALL);

        return migrationRequired;
    }
}
