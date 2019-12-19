/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import org.eclipse.hawkbit.repository.model.NamedEntity;
import org.eclipse.hawkbit.repository.model.NamedVersionedEntity;
import org.eclipse.hawkbit.ui.common.builder.TextAreaBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
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

    public TextField createNameField(final Binder<ProxyDistributionSet> binder) {
        final TextField dsName = new TextFieldBuilder(NamedEntity.NAME_MAX_SIZE).id(UIComponentIdProvider.DIST_ADD_NAME)
                .caption(i18n.getMessage(TEXTFIELD_NAME)).prompt(i18n.getMessage(TEXTFIELD_NAME)).buildTextComponent();
        dsName.setSizeUndefined();

        // TODO: use i18n
        binder.forField(dsName).asRequired("You must provide distribution set name").bind(ProxyDistributionSet::getName,
                ProxyDistributionSet::setName);

        return dsName;
    }

    public TextField createVersionField(final Binder<ProxyDistributionSet> binder) {
        final TextField dsVersion = new TextFieldBuilder(NamedVersionedEntity.VERSION_MAX_SIZE)
                .id(UIComponentIdProvider.DIST_ADD_VERSION).caption(i18n.getMessage(TEXTFIELD_VERSION))
                .prompt(i18n.getMessage(TEXTFIELD_VERSION)).buildTextComponent();
        dsVersion.setSizeUndefined();

        // TODO: use i18n
        binder.forField(dsVersion).asRequired("You must provide distribution set version")
                .bind(ProxyDistributionSet::getVersion, ProxyDistributionSet::setVersion);

        return dsVersion;
    }

    public TextArea createDescription(final Binder<ProxyDistributionSet> binder) {
        final TextArea dsDescription = new TextAreaBuilder(NamedEntity.DESCRIPTION_MAX_SIZE)
                .id(UIComponentIdProvider.DIST_ADD_DESC).caption(i18n.getMessage(TEXTFIELD_DESCRIPTION))
                .prompt(i18n.getMessage(TEXTFIELD_DESCRIPTION)).style("text-area-style").buildTextComponent();
        dsDescription.setSizeUndefined();

        binder.forField(dsDescription).bind(ProxyDistributionSet::getDescription, ProxyDistributionSet::setDescription);

        return dsDescription;
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
