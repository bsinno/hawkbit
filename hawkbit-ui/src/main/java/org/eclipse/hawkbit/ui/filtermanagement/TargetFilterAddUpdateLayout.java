/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import org.eclipse.hawkbit.repository.rsql.RsqlValidationOracle;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowLayout;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.filtermanagement.state.TargetFilterDetailsLayoutUiState;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Target add/update window layout.
 */
public class TargetFilterAddUpdateLayout extends AbstractEntityWindowLayout<ProxyTargetFilterQuery> {
    private final TargetFilterAddUpdateLayoutComponentBuilder filterComponentBuilder;

    private final TextField filterNameInput;
    private final AutoCompleteTextFieldComponent autoCompleteComponent;
    private final Link helpLink;
    private final Button searchButton;
    private final Button saveButton;
    private final TargetFilterDetailsLayoutUiState uiState;
    private final UIEventBus eventBus;

    private Registration saveListener;

    /**
     * Constructor for AbstractTagWindowLayout
     * 
     * @param i18n
     *            I18N
     */
    public TargetFilterAddUpdateLayout(final VaadinMessageSource i18n, final UiProperties uiProperties,
            final TargetFilterDetailsLayoutUiState uiState, final UIEventBus eventBus,
            final RsqlValidationOracle rsqlValidationOracle) {
        super();
        this.uiState = uiState;
        this.eventBus = eventBus;
        this.filterComponentBuilder = new TargetFilterAddUpdateLayoutComponentBuilder(i18n, uiProperties,
                rsqlValidationOracle);

        this.filterNameInput = filterComponentBuilder.createNameField(binder);
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

        searchButton.setEnabled(false);
        saveButton.setEnabled(false);
        filterQueryLayout.addComponent(searchButton);
        filterQueryLayout.addComponent(saveButton);

        filterAddUpdateLayout.addComponent(filterNameInput);
        filterAddUpdateLayout.addComponent(filterQueryLayout);
        autoCompleteComponent.focus();

        return filterAddUpdateLayout;
    }

    private void addValueChangeListeners() {
        searchButton.addClickListener(event -> onSearchIconClick());
        autoCompleteComponent.addValidationListener((valid, message) -> searchButton.setEnabled(valid));
        autoCompleteComponent.addTextfieldChangedListener(this::onFilterQueryTextfieldChanged);
        autoCompleteComponent
                .addShortcutListener(new ShortcutListener("List Filtered Targets", ShortcutAction.KeyCode.ENTER, null) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void handleAction(final Object sender, final Object target) {
                        onSearchIconClick();
                    }
                });
        filterNameInput.addValueChangeListener(this::onFilterNameChanged);
        addValidationListener(saveButton::setEnabled);
    }

    private void onFilterQueryTextfieldChanged(final ValueChangeEvent<String> event) {
        uiState.setFilterQueryValueInput(event.getValue());
    }

    private void onFilterNameChanged(final ValueChangeEvent<String> event) {
        uiState.setNameInput(event.getValue());
    }

    private void onSearchIconClick() {
        if (!autoCompleteComponent.isValid()) {
            return;
        }
        eventBus.publish(EventTopics.SEARCH_FILTER_CHANGED, this, autoCompleteComponent.getValue());
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

    public void disableSearchButton() {
        searchButton.setEnabled(false);
    }
}
