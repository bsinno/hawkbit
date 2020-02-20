/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.bulkupload;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.ui.common.builder.FormComponentBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyBulkUploadWindow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Builder for Rollout window components.
 */
public final class BulkUploadWindowLayoutComponentBuilder {

    private final VaadinMessageSource i18n;

    public static final String TEXTFIELD_DESCRIPTION = "textfield.description";
    public static final String PROMPT_DISTRIBUTION_SET = "prompt.distribution.set";
    public static final String TEXTFIELD_NAME = "textfield.name";

    private final DistributionSetStatelessDataProvider distributionSetDataProvider;

    public BulkUploadWindowLayoutComponentBuilder(final VaadinMessageSource i18n,
            final DistributionSetManagement distributionSetManagement) {
        this.i18n = i18n;

        this.distributionSetDataProvider = new DistributionSetStatelessDataProvider(distributionSetManagement,
                new DistributionSetToProxyDistributionMapper());
    }

    public ComboBox<ProxyDistributionSet> createDistributionSetCombo(final Binder<ProxyBulkUploadWindow> binder) {
        final ComboBox<ProxyDistributionSet> distributionSet = new ComboBox<>(
                i18n.getMessage(UIMessageIdProvider.HEADER_DISTRIBUTION_SET));

        distributionSet.setId(UIComponentIdProvider.DIST_SET_SELECT_COMBO_ID);
        distributionSet.setPlaceholder(i18n.getMessage(PROMPT_DISTRIBUTION_SET));
        distributionSet.addStyleName(ValoTheme.COMBOBOX_SMALL);
        distributionSet.setSizeFull();

        distributionSet.setItemCaptionGenerator(ProxyDistributionSet::getNameVersion);
        distributionSet.setDataProvider(distributionSetDataProvider);

        binder.forField(distributionSet).withConverter(ds -> {
            if (ds == null) {
                return null;
            }

            return ds.getId();
        }, dsId -> {
            if (dsId == null) {
                return null;
            }

            final ProxyDistributionSet ds = new ProxyDistributionSet();
            ds.setId(dsId);

            return ds;
        }).bind(ProxyBulkUploadWindow::getDistributionSetId, ProxyBulkUploadWindow::setDistributionSetId);

        return distributionSet;
    }

    /**
     * create description field
     * 
     * @param binder
     *            binder the input will be bound to
     * @return input component
     */
    public TextArea createDescriptionField(final Binder<ProxyBulkUploadWindow> binder) {
        return FormComponentBuilder.createDescriptionInput(binder, i18n, UIComponentIdProvider.TARGET_ADD_DESC);
    }
}
