/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.header.support;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

public class SearchHeaderSupport implements HeaderSupport {
    private final VaadinMessageSource i18n;

    private final String searchFieldId;
    private final String searchResetIconId;
    private final Supplier<String> searchStateSupplier;
    private final Consumer<String> searchByCallback;

    private final TextField searchField;
    private final Button searchResetIcon;

    public SearchHeaderSupport(final VaadinMessageSource i18n, final String searchFieldId,
            final String searchResetIconId, final Supplier<String> searchStateSupplier,
            final Consumer<String> searchByCallback) {
        this.i18n = i18n;

        this.searchFieldId = searchFieldId;
        this.searchResetIconId = searchResetIconId;
        this.searchStateSupplier = searchStateSupplier;
        this.searchByCallback = searchByCallback;

        this.searchField = createSearchField();
        this.searchResetIcon = createSearchResetIcon();
    }

    private TextField createSearchField() {
        return new TextFieldBuilder(64).id(searchFieldId).createSearchField(event -> {
            // we do not want to send the event during state restore, so we
            // react only on user input
            if (event.isUserOriginated()) {
                searchByCallback.accept(event.getValue());
            }
        });
    }

    private Button createSearchResetIcon() {
        final Button searchResetButton = SPUIComponentProvider.getButton(searchResetIconId, "",
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_SEARCH), null, false, VaadinIcons.SEARCH,
                SPUIButtonStyleNoBorder.class);

        searchResetButton.addClickListener(event -> onSearchResetClick());
        // TODO: consider finding another way instead of setting data
        searchResetButton.setData(Boolean.FALSE);

        return searchResetButton;
    }

    private void onSearchResetClick() {
        final Boolean isSearchActivated = isSearchActivated();

        if (isSearchActivated == null || Boolean.FALSE.equals(isSearchActivated)) {
            // Clicked on search icon
            openSearchTextField();
        } else {
            // Clicked on reset search icon
            closeSearchTextField();
        }
    }

    public Boolean isSearchActivated() {
        return (Boolean) searchResetIcon.getData();
    }

    private void openSearchTextField() {
        searchResetIcon.addStyleName(SPUIDefinitions.FILTER_RESET_ICON);
        searchResetIcon.setIcon(VaadinIcons.CLOSE);
        searchResetIcon.setData(Boolean.TRUE);
        searchResetIcon.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_RESET));

        searchField.setVisible(true);
        searchField.focus();
    }

    private void closeSearchTextField() {
        searchResetIcon.removeStyleName(SPUIDefinitions.FILTER_RESET_ICON);
        searchResetIcon.setIcon(VaadinIcons.SEARCH);
        searchResetIcon.setData(Boolean.FALSE);
        searchResetIcon.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_SEARCH));

        searchField.setValue("");
        searchField.setVisible(false);

        // TODO: check if it does not lead to additional event being sent, e.g.
        // onSimpleFilterTab reset. What about ui state?
        searchByCallback.accept(null);
    }

    @Override
    public void restoreState() {
        final String onLoadSearchBoxValue = searchStateSupplier.get();

        if (!StringUtils.isEmpty(onLoadSearchBoxValue)) {
            openSearchTextField();
            searchField.setValue(onLoadSearchBoxValue);
        }
    }

    public TextField getSearchField() {
        return searchField;
    }

    public void disableSearch() {
        searchResetIcon.setEnabled(false);
    }

    public void enableSearch() {
        searchResetIcon.setEnabled(true);
    }

    public void resetSearch() {
        if (isSearchActivated()) {
            closeSearchTextField();
        }
    }

    @Override
    public Component getHeaderComponent() {
        final HorizontalLayout headerIconLayout = new HorizontalLayout();
        headerIconLayout.setMargin(false);
        headerIconLayout.setSpacing(false);

        headerIconLayout.addComponent(searchField);
        headerIconLayout.addComponent(searchResetIcon);

        // hidden by default
        searchField.setVisible(false);

        return headerIconLayout;
    }
}
