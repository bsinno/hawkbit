/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import org.eclipse.hawkbit.ui.common.builder.BoundComponent;
import org.eclipse.hawkbit.ui.common.builder.FormComponentBuilder;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.management.miscs.ActionTypeOptionGroupAutoAssignmentLayout;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;

//TODO: remove duplication with other builders
public class AutoAssignmentWindowLayoutComponentBuilder {

    public static final String PROMPT_DISTRIBUTION_SET = "prompt.distribution.set";

    private final VaadinMessageSource i18n;

    public AutoAssignmentWindowLayoutComponentBuilder(final VaadinMessageSource i18n) {
        this.i18n = i18n;
    }

    public Label createDescriptionLabel() {
        final Label autoAssignmentDescription = new Label(
                i18n.getMessage(UIMessageIdProvider.LABEL_AUTO_ASSIGNMENT_DESC));
        autoAssignmentDescription.setSizeFull();

        return autoAssignmentDescription;
    }

    public CheckBox createEnableCheckbox(final Binder<ProxyTargetFilterQuery> binder) {
        final String caption = i18n.getMessage(UIMessageIdProvider.LABEL_AUTO_ASSIGNMENT_ENABLE);
        return SPUIComponentProvider.getCheckBox(caption, UIComponentIdProvider.DIST_SET_SELECT_ENABLE_ID, binder,
                ProxyTargetFilterQuery::isAutoAssignmentEnabled, ProxyTargetFilterQuery::setAutoAssignmentEnabled);
    }

    public ActionTypeOptionGroupAutoAssignmentLayout createActionTypeOptionGroupLayout(
            final Binder<ProxyTargetFilterQuery> binder) {
        final ActionTypeOptionGroupAutoAssignmentLayout actionTypeOptionGroupLayout = new ActionTypeOptionGroupAutoAssignmentLayout(
                i18n, UIComponentIdProvider.AUTO_ASSIGNMENT_ACTION_TYPE_OPTIONS_ID);

        binder.forField(actionTypeOptionGroupLayout.getActionTypeOptionGroup())
                .bind(ProxyTargetFilterQuery::getAutoAssignActionType, ProxyTargetFilterQuery::setAutoAssignActionType);

        return actionTypeOptionGroupLayout;
    }

    /**
     * create optional Distribution Set ComboBox
     * 
     * @param binder
     *            binder the input will be bound to
     * @param dataProvider
     *            provides Distribution Set data
     * @return bound ComboBox
     */
    public BoundComponent<ComboBox<ProxyDistributionSet>> createDistributionSetCombo(
            final Binder<ProxyTargetFilterQuery> binder, final DistributionSetStatelessDataProvider dataProvider) {
        final BoundComponent<ComboBox<ProxyDistributionSet>> boundComboBox = FormComponentBuilder
                .createDistributionSetComboBox(binder, dataProvider, i18n,
                        UIComponentIdProvider.DIST_SET_SELECT_COMBO_ID);
        boundComboBox.getComponent().setSizeFull();

        return boundComboBox;
    }
}
