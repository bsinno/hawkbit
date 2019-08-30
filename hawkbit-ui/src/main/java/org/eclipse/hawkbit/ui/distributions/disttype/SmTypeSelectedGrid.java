/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.disttype;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Software Module Selected Type grid which is shown on the Distribution Type
 * Create/Update popup layout.
 */
public class SmTypeSelectedGrid extends Grid<ProxyType> {
    private static final long serialVersionUID = 1L;

    // TODO: consider changing to i18n 'Mandatory'
    private static final String STAR = " * ";
    private static final String SM_TYPE_SELECTED_NAME_ID = "smTypeSelectedName";
    private static final String SM_TYPE_SELECTED_MANDATORY = "smTypeSelectedMandatory";

    private final VaadinMessageSource i18n;

    public SmTypeSelectedGrid(final VaadinMessageSource i18n) {
        this.i18n = i18n;

        init();
    }

    private void init() {
        addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        addStyleName(ValoTheme.TABLE_NO_STRIPES);
        addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
        addStyleName(ValoTheme.TABLE_SMALL);
        addStyleName("dist_type_twin-table");

        setId(SPUIDefinitions.TWIN_TABLE_SELECTED_ID);
        setSelectionMode(SelectionMode.MULTI);
        setSizeFull();
        setRequiredIndicatorVisible(true);

        addColumns();
    }

    private void addColumns() {
        addColumn(ProxyType::getName).setId(SM_TYPE_SELECTED_NAME_ID)
                .setCaption(i18n.getMessage("header.dist.twintable.selected"))
                .setDescriptionGenerator(ProxyType::getDescription).setExpandRatio(3);

        addComponentColumn(this::buildMandatoryTypeCheckBox).setId(SM_TYPE_SELECTED_MANDATORY).setCaption(STAR)
                .setDescriptionGenerator(smType -> i18n.getMessage(UIMessageIdProvider.TOOLTIP_CHECK_FOR_MANDATORY))
                .setExpandRatio(1);
    }

    private CheckBox buildMandatoryTypeCheckBox(final ProxyType smType) {
        final CheckBox mandatoryTypeCheckBox = new CheckBox();
        mandatoryTypeCheckBox.setId("selected.sm.type." + smType.getId());

        // TODO: check if it works
        final Binder<ProxyType> binder = new Binder<>();
        binder.forField(mandatoryTypeCheckBox).bind(ProxyType::isMandatory, ProxyType::setMandatory);
        binder.setBean(smType);

        return mandatoryTypeCheckBox;
    }
}
