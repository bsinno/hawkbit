/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import org.eclipse.hawkbit.repository.model.NamedEntity;
import org.eclipse.hawkbit.repository.rsql.RsqlValidationOracle;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationResult;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Button;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextField;

public class TargetFilterAddUpdateLayoutComponentBuilder {

    public static final String TEXTFIELD_FILTER_NAME = "textfield.name";

    private final VaadinMessageSource i18n;
    private final UiProperties uiProperties;
    private final RsqlValidationOracle rsqlValidationOracle;

    public TargetFilterAddUpdateLayoutComponentBuilder(final VaadinMessageSource i18n, final UiProperties uiProperties,
            final RsqlValidationOracle rsqlValidationOracle) {
        this.i18n = i18n;
        this.uiProperties = uiProperties;
        this.rsqlValidationOracle = rsqlValidationOracle;
    }

    public TextField createNameField(final Binder<ProxyTargetFilterQuery> binder) {
        final TextField filterName = new TextFieldBuilder(NamedEntity.NAME_MAX_SIZE)
                .id(UIComponentIdProvider.CUSTOM_FILTER_ADD_NAME).caption(i18n.getMessage(TEXTFIELD_FILTER_NAME))
                .prompt(i18n.getMessage(TEXTFIELD_FILTER_NAME)).buildTextComponent();
        filterName.setWidth(380, Unit.PIXELS);

        binder.forField(filterName).asRequired(i18n.getMessage(UIMessageIdProvider.MESSAGE_ERROR_NAMEREQUIRED))
                .bind(ProxyTargetFilterQuery::getName, ProxyTargetFilterQuery::setName);

        return filterName;
    }

    public AutoCompleteTextFieldComponent createQueryField(final Binder<ProxyTargetFilterQuery> binder) {
        final AutoCompleteTextFieldComponent autoCompleteComponent = new AutoCompleteTextFieldComponent(
                rsqlValidationOracle);

        binder.forField(autoCompleteComponent)
                .withValidator((query, context) -> autoCompleteComponent.isValid() ? ValidationResult.ok()
                        : ValidationResult
                                .error(i18n.getMessage(UIMessageIdProvider.MESSAGE_FILTER_QUERY_ERROR_NOTVALIDE)))
                .bind(ProxyTargetFilterQuery::getQuery, ProxyTargetFilterQuery::setQuery);

        return autoCompleteComponent;
    }

    public Link createFilterHelpLink() {
        return SPUIComponentProvider.getHelpLink(i18n,
                uiProperties.getLinks().getDocumentation().getTargetfilterView());
    }

    public Button createSearchTargetsByFilterButton() {
        return SPUIComponentProvider.getButton(UIComponentIdProvider.FILTER_SEARCH_ICON_ID, "",
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_SEARCH), null, false, VaadinIcons.SEARCH,
                SPUIButtonStyleNoBorder.class);
    }

    public Button createSaveButton() {
        return SPUIComponentProvider.getButton(UIComponentIdProvider.CUSTOM_FILTER_SAVE_ICON,
                UIComponentIdProvider.CUSTOM_FILTER_SAVE_ICON, i18n.getMessage(UIMessageIdProvider.TOOLTIP_SAVE), null,
                false, VaadinIcons.SAFE, SPUIButtonStyleNoBorder.class);
    }
}
