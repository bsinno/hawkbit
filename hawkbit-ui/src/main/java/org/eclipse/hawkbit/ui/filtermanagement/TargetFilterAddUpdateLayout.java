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

import org.eclipse.hawkbit.repository.rsql.RsqlValidationOracle;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowLayout;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.shared.Registration;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Target add/update window layout.
 */
public class TargetFilterAddUpdateLayout extends AbstractEntityWindowLayout<ProxyTargetFilterQuery> {
    private final TargetFilterAddUpdateLayoutComponentBuilder filterComponentBuilder;

    private final TextField filterName;
    private final AutoCompleteTextFieldComponent autoCompleteComponent;
    private final Link helpLink;
    private final Button searchButton;
    private final Button saveButton;

    private Registration saveListener;

    /**
     * Constructor for AbstractTagWindowLayout
     * 
     * @param i18n
     *            I18N
     */
    public TargetFilterAddUpdateLayout(final VaadinMessageSource i18n, final UiProperties uiProperties,
            final FilterManagementUIState filterManagementUIState, final UIEventBus eventBus,
            final RsqlValidationOracle rsqlValidationOracle, final Executor executor) {
        super();

        this.filterComponentBuilder = new TargetFilterAddUpdateLayoutComponentBuilder(i18n, uiProperties,
                filterManagementUIState, eventBus, rsqlValidationOracle, executor);

        this.filterName = filterComponentBuilder.createNameField(binder);
        this.autoCompleteComponent = filterComponentBuilder.createQueryField(binder);
        this.helpLink = filterComponentBuilder.createFilterHelpLink();
        this.searchButton = filterComponentBuilder.createSearchTargetsByFilterButton();
        this.saveButton = filterComponentBuilder.createSaveButton();

        addValueChangeListeners();
    }

    @Override
    public ComponentContainer getRootComponent() {
        final VerticalLayout filterAddUpdateLayout = new VerticalLayout();
        filterAddUpdateLayout.setSpacing(true);
        filterAddUpdateLayout.setMargin(false);
        filterAddUpdateLayout.setSizeUndefined();
        filterAddUpdateLayout.addStyleName(SPUIStyleDefinitions.ADD_UPDATE_FILTER_LAYOUT);

        final HorizontalLayout filterQueryLayout = new HorizontalLayout();
        filterQueryLayout.setSpacing(false);
        filterQueryLayout.setMargin(false);
        filterQueryLayout.setSizeUndefined();

        filterQueryLayout.addComponent(autoCompleteComponent);
        filterQueryLayout.addComponent(helpLink);
        filterQueryLayout.addComponent(searchButton);
        filterQueryLayout.addComponent(saveButton);

        filterAddUpdateLayout.addComponent(filterName);
        filterAddUpdateLayout.addComponent(filterQueryLayout);
        autoCompleteComponent.focus();

        return filterAddUpdateLayout;
    }

    private void addValueChangeListeners() {
        searchButton.addClickListener(event -> onSearchIconClick());
        autoCompleteComponent.addValidationListener((valid, message) -> searchButton.setEnabled(valid));
        addValidationListener(saveButton::setEnabled);
    }

    private void onSearchIconClick() {
        if (!autoCompleteComponent.isValid()) {
            return;
        }

        autoCompleteComponent.showValidationInProgress();
        // TODO: rework
        autoCompleteComponent.getExecutor().execute(autoCompleteComponent.new StatusCircledAsync(UI.getCurrent()));
    }

    public void setSaveCallback(final SaveDialogCloseListener saveCallback) {
        if (saveListener != null) {
            saveListener.remove();
        }

        saveListener = saveButton.addClickListener(event -> {
            if (!saveCallback.canWindowSaveOrUpdate()) {
                return;
            }

            saveCallback.saveOrUpdate();
        });
    }
}
