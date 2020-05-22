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
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.selection.RangeSelectionModel;
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
    private final transient Runnable mandatoryPropertyChangedCallback;

    public SmTypeSelectedGrid(final VaadinMessageSource i18n, final Runnable mandatoryPropertyChangedCallback) {
        this.i18n = i18n;
        this.mandatoryPropertyChangedCallback = mandatoryPropertyChangedCallback;

        init();
    }

    private void init() {
        setSizeFull();
        setHeightUndefined();
        addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        addStyleName(ValoTheme.TABLE_NO_STRIPES);
        addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
        addStyleName(ValoTheme.TABLE_SMALL);
        // used to deactivate cell text selection by user
        addStyleName(AbstractGrid.MULTI_SELECT_STYLE);

        setId(SPUIDefinitions.TWIN_TABLE_SELECTED_ID);
        setSelectionModel(new RangeSelectionModel<>());

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

        final Binder<ProxyType> binder = new Binder<>();
        binder.forField(mandatoryTypeCheckBox).bind(ProxyType::isMandatory, ProxyType::setMandatory);
        binder.setBean(smType);
        binder.addValueChangeListener(event -> mandatoryPropertyChangedCallback.run());

        return mandatoryTypeCheckBox;
    }
}
