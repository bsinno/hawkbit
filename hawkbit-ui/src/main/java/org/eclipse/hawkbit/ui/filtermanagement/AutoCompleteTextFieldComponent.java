/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.repository.rsql.RsqlValidationOracle;
import org.eclipse.hawkbit.repository.rsql.ValidationOracleContext;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.filtermanagement.event.CustomFilterUIEvent;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

/**
 * An textfield with the {@link TextFieldSuggestionBox} extension which shows
 * suggestions in a suggestion-pop-up window while typing.
 */
public class AutoCompleteTextFieldComponent extends CustomField<String> {
    private static final long serialVersionUID = 1L;

    private final FilterManagementUIState filterManagementUIState;

    private final transient UIEventBus eventBus;
    private final transient RsqlValidationOracle rsqlValidationOracle;
    private final transient Executor executor;

    private final transient List<ValidationListener> listeners = new LinkedList<>();

    private final Label validationIcon;
    private final TextField queryTextField;
    private final HorizontalLayout autoCompleteLayout;

    private boolean isValid;
    private String targetFilterQuery;

    public AutoCompleteTextFieldComponent(final FilterManagementUIState filterManagementUIState,
            final UIEventBus eventBus, final RsqlValidationOracle rsqlValidationOracle, final Executor executor) {
        this.filterManagementUIState = filterManagementUIState;
        this.eventBus = eventBus;
        this.rsqlValidationOracle = rsqlValidationOracle;
        this.executor = executor;

        this.validationIcon = createStatusIcon();
        this.queryTextField = createSearchField();
        this.autoCompleteLayout = new HorizontalLayout();

        this.isValid = false;
        this.targetFilterQuery = "";

        init();
    }

    private void init() {
        autoCompleteLayout.setSizeUndefined();
        autoCompleteLayout.setSpacing(false);
        autoCompleteLayout.setMargin(false);
        autoCompleteLayout.addStyleName("custom-search-layout");

        autoCompleteLayout.addComponents(validationIcon, queryTextField);
        autoCompleteLayout.setComponentAlignment(validationIcon, Alignment.TOP_CENTER);

        new TextFieldSuggestionBox(rsqlValidationOracle, this).extend(queryTextField);
    }

    @Override
    protected Component initContent() {
        return autoCompleteLayout;
    }

    @Override
    protected void doSetValue(final String value) {
        if (value == null) {
            queryTextField.setValue("");
            resetIcon();

            targetFilterQuery = "";
            isValid = false;
        } else {
            queryTextField.setValue(value);
            // TODO: remove duplication with
            // TextFieldSuggestionBox#updateValidationIcon
            final ValidationOracleContext suggest = rsqlValidationOracle.suggest(value, value.length());
            final String errorMessage = suggest.getSyntaxErrorContext() != null
                    ? suggest.getSyntaxErrorContext().getErrorMessage()
                    : value;

            updateComponents(value, !suggest.isSyntaxError(), errorMessage);
        }
    }

    @Override
    public String getValue() {
        return targetFilterQuery;
    }

    private Label createStatusIcon() {
        final Label statusIcon = new Label();

        statusIcon.setId(UIComponentIdProvider.VALIDATION_STATUS_ICON_ID);
        statusIcon.setContentMode(ContentMode.HTML);
        statusIcon.setSizeFull();
        statusIcon.setStyleName("hide-status-label");

        statusIcon.setValue(VaadinIcons.CHECK_CIRCLE.getHtml());

        return statusIcon;
    }

    private TextField createSearchField() {
        final TextField textField = new TextFieldBuilder(TargetFilterQuery.QUERY_MAX_SIZE)
                .id(UIComponentIdProvider.CUSTOM_FILTER_QUERY).buildTextComponent();

        textField.addStyleName("target-filter-textfield");
        textField.setWidth(900.0F, Unit.PIXELS);

        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.setValueChangeTimeout(100);

        return textField;
    }

    // @EventBusListenerMethod(scope = EventScope.UI)
    // void onEvent(final CustomFilterUIEvent custFUIEvent) {
    // if (custFUIEvent == CustomFilterUIEvent.UPDATE_TARGET_FILTER_SEARCH_ICON)
    // {
    // validationIcon.setValue(VaadinIcons.CHECK_CIRCLE.getHtml());
    // if (!isValidationError()) {
    // validationIcon.setStyleName(SPUIStyleDefinitions.SUCCESS_ICON);
    // } else {
    // validationIcon.setStyleName(SPUIStyleDefinitions.ERROR_ICON);
    // }
    // }
    // }

    /**
     * Clears the textfield and resets the validation icon.
     */
    @Override
    public void clear() {
        queryTextField.clear();
        validationIcon.setValue(VaadinIcons.CHECK_CIRCLE.getHtml());
        validationIcon.setStyleName("hide-status-label");
    }

    @Override
    public void focus() {
        queryTextField.focus();
    }

    /**
     * Adds the given listener
     * 
     * @param validationListener
     *            the listener to be called in case of validation status change
     */
    public void addValidationListener(final ValidationListener validationListener) {
        listeners.add(validationListener);
    }

    /**
     * Called when the filter-query has been changed in the textfield, e.g. from
     * client-side.
     * 
     * @param currentText
     *            the current text of the textfield which has been changed
     * @param valid
     *            {@code boolean} if the current text is RSQL syntax valid
     *            otherwise {@code false}
     * @param validationMessage
     *            a message shown in case of syntax errors as tooltip
     */
    public void onQueryFilterChange(final String currentText, final boolean valid, final String validationMessage) {
        final String message = valid ? currentText : validationMessage;
        updateComponents(currentText, valid, message);

        fireEvent(createValueChange(currentText, false));
        listeners.forEach(listener -> listener.validationChanged(valid, message));
    }

    private void updateComponents(final String currentText, final boolean valid, final String message) {
        targetFilterQuery = currentText;
        isValid = valid;

        if (valid) {
            showValidationSuccesIcon(message);
        } else {
            showValidationFailureIcon(message);
        }
    }

    private void resetIcon() {
        validationIcon.setValue(null);
        validationIcon.setDescription(null);
        validationIcon.removeStyleName(validationIcon.getStyleName());
    }

    /**
     * Shows the validation success icon in the textfield
     * 
     * @param text
     *            the text to store in the UI state object
     */
    public void showValidationSuccesIcon(final String text) {
        validationIcon.setValue(VaadinIcons.CHECK_CIRCLE.getHtml());
        validationIcon.setStyleName(SPUIStyleDefinitions.SUCCESS_ICON);
        // TODO: do we need to update state here?
        filterManagementUIState.setFilterQueryValue(text);
        filterManagementUIState.setIsFilterByInvalidFilterQuery(Boolean.FALSE);
    }

    /**
     * Shows the validation error icon in the textfield
     * 
     * @param validationMessage
     *            the validation message which should be added to the error-icon
     *            tooltip
     */
    public void showValidationFailureIcon(final String validationMessage) {
        validationIcon.setValue(VaadinIcons.CLOSE_CIRCLE.getHtml());
        validationIcon.setStyleName(SPUIStyleDefinitions.ERROR_ICON);
        validationIcon.setDescription(validationMessage);
        // TODO: do we need to update state here?
        filterManagementUIState.setFilterQueryValue(null);
        filterManagementUIState.setIsFilterByInvalidFilterQuery(Boolean.TRUE);
    }

    public boolean isValid() {
        return isValid;
    }

    class StatusCircledAsync implements Runnable {
        private final UI current;

        StatusCircledAsync(final UI current) {
            this.current = current;
        }

        @Override
        public void run() {
            UI.setCurrent(current);
            eventBus.publish(this, CustomFilterUIEvent.FILTER_TARGET_BY_QUERY);
        }
    }

    /**
     * Sets the spinner as progress indicator.
     */
    public void showValidationInProgress() {
        validationIcon.setValue(null);
        validationIcon.addStyleName("show-status-label");
        validationIcon.setStyleName(SPUIStyleDefinitions.TARGET_FILTER_SEARCH_PROGRESS_INDICATOR_STYLE);
    }

    public Executor getExecutor() {
        return executor;
    }

    /**
     * Change listener on the textfield.
     */
    @FunctionalInterface
    public interface ValidationListener {
        /**
         * Called when the text has been changed and validated.
         * 
         * @param valid
         *            indicates if the entered query text is valid
         * @param query
         *            the entered query text
         */
        void validationChanged(final boolean valid, final String validationMessage);
    }
}
