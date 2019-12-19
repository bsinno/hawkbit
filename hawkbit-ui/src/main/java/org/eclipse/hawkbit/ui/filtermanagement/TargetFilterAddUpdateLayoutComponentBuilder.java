/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import java.util.concurrent.Executor;

import org.eclipse.hawkbit.repository.model.NamedEntity;
import org.eclipse.hawkbit.repository.rsql.RsqlValidationOracle;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationResult;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Button;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextField;

public class TargetFilterAddUpdateLayoutComponentBuilder {

    public static final String TEXTFIELD_FILTER_NAME = "textfield.customfiltername";

    private final VaadinMessageSource i18n;
    private final UiProperties uiProperties;
    private final FilterManagementUIState filterManagementUIState;
    private final UIEventBus eventBus;
    private final RsqlValidationOracle rsqlValidationOracle;
    private final Executor executor;

    public TargetFilterAddUpdateLayoutComponentBuilder(final VaadinMessageSource i18n, final UiProperties uiProperties,
            final FilterManagementUIState filterManagementUIState, final UIEventBus eventBus,
            final RsqlValidationOracle rsqlValidationOracle, final Executor executor) {
        this.i18n = i18n;
        this.uiProperties = uiProperties;
        this.filterManagementUIState = filterManagementUIState;
        this.eventBus = eventBus;
        this.rsqlValidationOracle = rsqlValidationOracle;
        this.executor = executor;
    }

    public TextField createNameField(final Binder<ProxyTargetFilterQuery> binder) {
        final TextField filterName = new TextFieldBuilder(NamedEntity.NAME_MAX_SIZE)
                .id(UIComponentIdProvider.CUSTOM_FILTER_ADD_NAME).caption(i18n.getMessage(TEXTFIELD_FILTER_NAME))
                .prompt(i18n.getMessage(TEXTFIELD_FILTER_NAME)).buildTextComponent();
        filterName.setWidth(380, Unit.PIXELS);

        // TODO: i18n
        binder.forField(filterName).asRequired("You must provide target filter name")
                .bind(ProxyTargetFilterQuery::getName, ProxyTargetFilterQuery::setName);

        return filterName;
    }

    public AutoCompleteTextFieldComponent createQueryField(final Binder<ProxyTargetFilterQuery> binder) {
        final AutoCompleteTextFieldComponent autoCompleteComponent = new AutoCompleteTextFieldComponent(
                filterManagementUIState, eventBus, rsqlValidationOracle, executor);

        binder.forField(autoCompleteComponent)
                .withValidator((query, context) -> autoCompleteComponent.isValid() ? ValidationResult.ok()
                        // TODO: use i18n or validation message
                        : ValidationResult.error("Target filter query is not valid"))
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
