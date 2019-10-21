/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.disttype;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.common.data.mappers.TypeToProxyTypeMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.CollectionUtils;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Layout for the software modules select grids for managing Distribution Set
 * Types on the Distributions View.
 */
public class DsTypeSmSelectLayout extends CustomField<List<ProxyType>> {
    private static final long serialVersionUID = 1L;

    // TODO: consider using lazy loading with dataprovider
    private static final int MAX_SM_TYPE_QUERY = 500;

    private final VaadinMessageSource i18n;

    private final transient SoftwareModuleTypeManagement softwareModuleTypeManagement;
    private final transient TypeToProxyTypeMapper<SoftwareModuleType> smTypeToProxyTypeMapper;

    private SmTypeSelectedGrid selectedGrid;
    private SmTypeSourceGrid sourceGrid;

    private List<ProxyType> selectedGridTypesList;
    private List<ProxyType> sourceGridTypesList;

    private final HorizontalLayout dsTypeSmSelectLayout;

    /**
     * Constructor
     * 
     * @param i18n
     *            VaadinMessageSource
     * @param softwareModuleTypeManagement
     *            SoftwareModuleTypeManagement
     */
    public DsTypeSmSelectLayout(final VaadinMessageSource i18n,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement) {
        this.i18n = i18n;
        this.softwareModuleTypeManagement = softwareModuleTypeManagement;
        this.smTypeToProxyTypeMapper = new TypeToProxyTypeMapper<>();

        this.dsTypeSmSelectLayout = new HorizontalLayout();
        this.dsTypeSmSelectLayout.setSpacing(false);
        this.dsTypeSmSelectLayout.setMargin(false);
        this.dsTypeSmSelectLayout.setSizeFull();
        this.dsTypeSmSelectLayout.setWidth("400px");

        buildLayout();
    }

    private void buildLayout() {
        final VerticalLayout selectButtonLayout = new VerticalLayout();
        selectButtonLayout.setSpacing(false);
        selectButtonLayout.setMargin(false);

        final Button selectButton = SPUIComponentProvider.getButton(UIComponentIdProvider.SELECT_DIST_TYPE, "", "",
                "arrow-button", true, VaadinIcons.FORWARD, SPUIButtonStyleNoBorder.class);
        selectButton.addClickListener(event -> addSmTypeToSelectedGrid());

        final Button unSelectButton = SPUIComponentProvider.getButton("unselect-dist-type", "", "", "arrow-button",
                true, VaadinIcons.BACKWARDS, SPUIButtonStyleNoBorder.class);
        unSelectButton.addClickListener(event -> removeSmTypeFromSelectedGrid());

        selectButtonLayout.addComponent(selectButton);
        selectButtonLayout.addComponent(unSelectButton);
        selectButtonLayout.setComponentAlignment(selectButton, Alignment.MIDDLE_CENTER);
        selectButtonLayout.setComponentAlignment(unSelectButton, Alignment.MIDDLE_CENTER);

        sourceGrid = buildSourceGrid();
        selectedGrid = buildSelectedGrid();

        dsTypeSmSelectLayout.addComponent(sourceGrid);
        dsTypeSmSelectLayout.addComponent(selectButtonLayout);
        dsTypeSmSelectLayout.addComponent(selectedGrid);
        dsTypeSmSelectLayout.setComponentAlignment(sourceGrid, Alignment.MIDDLE_LEFT);
        dsTypeSmSelectLayout.setComponentAlignment(selectButtonLayout, Alignment.MIDDLE_CENTER);
        dsTypeSmSelectLayout.setComponentAlignment(selectedGrid, Alignment.MIDDLE_RIGHT);
        dsTypeSmSelectLayout.setExpandRatio(sourceGrid, 0.45F);
        dsTypeSmSelectLayout.setExpandRatio(selectButtonLayout, 0.07F);
        dsTypeSmSelectLayout.setExpandRatio(selectedGrid, 0.48F);
    }

    private void addSmTypeToSelectedGrid() {
        final Set<ProxyType> selectedSourceSmTypes = sourceGrid.getSelectedItems();
        if (CollectionUtils.isEmpty(selectedSourceSmTypes)) {
            return;
        }

        for (final ProxyType selectedSourceSmType : selectedSourceSmTypes) {
            selectedGridTypesList.add(selectedSourceSmType);
            sourceGridTypesList.remove(selectedSourceSmType);
            // TODO: should we call refreshAll on both grids here?
        }
    }

    private void removeSmTypeFromSelectedGrid() {
        final Set<ProxyType> selectedSelectedSmTypes = selectedGrid.getSelectedItems();
        if (CollectionUtils.isEmpty(selectedSelectedSmTypes)) {
            return;
        }

        for (final ProxyType selectedSelectedSmType : selectedSelectedSmTypes) {
            selectedGridTypesList.remove(selectedSelectedSmType);
            sourceGridTypesList.add(selectedSelectedSmType);
            // TODO: should we call refreshAll on both grids here?
        }
    }

    private SmTypeSourceGrid buildSourceGrid() {
        final SmTypeSourceGrid grid = new SmTypeSourceGrid(i18n);
        populateSmTypeSourceGrid();
        grid.setItems(sourceGridTypesList);

        if (!CollectionUtils.isEmpty(sourceGridTypesList)) {
            grid.select(sourceGridTypesList.get(0));
        }

        return grid;
    }

    private void populateSmTypeSourceGrid() {
        if (sourceGridTypesList == null) {
            sourceGridTypesList = new ArrayList<>();
        } else {
            sourceGridTypesList.clear();
        }

        softwareModuleTypeManagement.findAll(PageRequest.of(0, MAX_SM_TYPE_QUERY))
                .forEach(smType -> sourceGridTypesList.add(smTypeToProxyTypeMapper.map(smType)));
    }

    private SmTypeSelectedGrid buildSelectedGrid() {
        final SmTypeSelectedGrid grid = new SmTypeSelectedGrid(i18n);
        selectedGridTypesList = new ArrayList<>();
        grid.setItems(selectedGridTypesList);

        return grid;
    }

    @Override
    public List<ProxyType> getValue() {
        return selectedGridTypesList;
    }

    @Override
    protected Component initContent() {
        return dsTypeSmSelectLayout;
    }

    @Override
    protected void doSetValue(final List<ProxyType> value) {
        if (value == null) {
            return;
        }

        selectedGridTypesList = value;
        value.forEach(sourceGridTypesList::remove);
        // TODO: should we call refreshAll on both grids here?
    }
}
