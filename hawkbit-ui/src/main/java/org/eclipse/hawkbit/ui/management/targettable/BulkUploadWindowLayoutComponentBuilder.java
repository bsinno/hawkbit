/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.Collections;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.model.NamedEntity;
import org.eclipse.hawkbit.ui.common.builder.TextAreaBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyBulkUploadWindow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;

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

    public BulkUploadTagsComponent createTargetTagsField(final Binder<ProxyBulkUploadWindow> binder,
            final TargetBulkTokenTags targetBulkTokenTags) {
        final BulkUploadTagsComponent bulkUploadTagsComponent = new BulkUploadTagsComponent(targetBulkTokenTags);
        bulkUploadTagsComponent.setSizeFull();

        binder.forField(bulkUploadTagsComponent).withConverter(tags -> {
            if (CollectionUtils.isEmpty(tags)) {
                return Collections.<Long, String> emptyMap();
            }

            return tags.stream().collect(Collectors.toMap(ProxyTag::getId, ProxyTag::getName));
        }, tagIdsWithName -> {
            if (CollectionUtils.isEmpty(tagIdsWithName)) {
                return Collections.emptyList();
            }

            return targetBulkTokenTags
                    .getTagsById(tagIdsWithName.keySet().stream().map(Long.class::cast).collect(Collectors.toList()));
        }).bind(ProxyBulkUploadWindow::getTagIdsWithNameToAssign, ProxyBulkUploadWindow::setTagIdsWithNameToAssign);

        return bulkUploadTagsComponent;
    }

    public TextArea createDescriptionField(final Binder<ProxyBulkUploadWindow> binder) {
        final TextArea targetDescription = new TextAreaBuilder(NamedEntity.DESCRIPTION_MAX_SIZE)
                .id(UIComponentIdProvider.TARGET_ADD_DESC).caption(i18n.getMessage(TEXTFIELD_DESCRIPTION))
                .prompt(i18n.getMessage(TEXTFIELD_DESCRIPTION)).style("text-area-style").buildTextComponent();
        targetDescription.setSizeFull();

        binder.forField(targetDescription).bind(ProxyBulkUploadWindow::getDescription,
                ProxyBulkUploadWindow::setDescription);

        return targetDescription;
    }
}
