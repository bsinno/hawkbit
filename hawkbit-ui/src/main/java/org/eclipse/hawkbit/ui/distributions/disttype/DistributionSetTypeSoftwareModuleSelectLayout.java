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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.CollectionUtils;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Layout for the software modules select grids for managing Distribution Set
 * Types on the Distributions View.
 */
public class DistributionSetTypeSoftwareModuleSelectLayout extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    // TODO: consider using lazy loading with dataprovider
    private static final int MAX_SM_TYPE_QUERY = 1000;

    private final transient SoftwareModuleTypeManagement softwareModuleTypeManagement;

    private SmTypeSelectedGrid selectedGrid;

    private SmTypeSourceGrid sourceGrid;

    private List<ProxyType> selectedGridTypesList;

    private List<ProxyType> sourceGridTypesList;

    private HorizontalLayout distTypeSelectLayout;

    private final VaadinMessageSource i18n;

    /**
     * Constructor
     * 
     * @param i18n
     *            VaadinMessageSource
     * @param softwareModuleTypeManagement
     *            SoftwareModuleTypeManagement
     */
    public DistributionSetTypeSoftwareModuleSelectLayout(final VaadinMessageSource i18n,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement) {
        this.softwareModuleTypeManagement = softwareModuleTypeManagement;
        this.i18n = i18n;
        init();
    }

    protected void init() {
        distTypeSelectLayout = createTwinColumnLayout();
        setSizeFull();
        addComponent(distTypeSelectLayout);
    }

    private HorizontalLayout createTwinColumnLayout() {
        final HorizontalLayout twinColumnLayout = new HorizontalLayout();
        twinColumnLayout.setSizeFull();
        twinColumnLayout.setWidth("400px");

        final VerticalLayout selectButtonLayout = new VerticalLayout();
        final Button selectButton = SPUIComponentProvider.getButton(UIComponentIdProvider.SELECT_DIST_TYPE, "", "",
                "arrow-button", true, FontAwesome.FORWARD, SPUIButtonStyleNoBorder.class);
        selectButton.addClickListener(event -> addSmTypeToSelectedGrid());
        final Button unSelectButton = SPUIComponentProvider.getButton("unselect-dist-type", "", "", "arrow-button",
                true, FontAwesome.BACKWARD, SPUIButtonStyleNoBorder.class);
        unSelectButton.addClickListener(event -> removeSmTypeFromSelectedGrid());
        selectButtonLayout.addComponent(selectButton);
        selectButtonLayout.addComponent(unSelectButton);
        selectButtonLayout.setComponentAlignment(selectButton, Alignment.MIDDLE_CENTER);
        selectButtonLayout.setComponentAlignment(unSelectButton, Alignment.MIDDLE_CENTER);

        sourceGrid = buildSourceGrid();
        selectedGrid = buildSelectedGrid();

        twinColumnLayout.addComponent(sourceGrid);
        twinColumnLayout.addComponent(selectButtonLayout);
        twinColumnLayout.addComponent(selectedGrid);
        twinColumnLayout.setComponentAlignment(sourceGrid, Alignment.MIDDLE_LEFT);
        twinColumnLayout.setComponentAlignment(selectButtonLayout, Alignment.MIDDLE_CENTER);
        twinColumnLayout.setComponentAlignment(selectedGrid, Alignment.MIDDLE_RIGHT);
        twinColumnLayout.setExpandRatio(sourceGrid, 0.45F);
        twinColumnLayout.setExpandRatio(selectButtonLayout, 0.07F);
        twinColumnLayout.setExpandRatio(selectedGrid, 0.48F);

        return twinColumnLayout;
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
                .forEach(smType -> sourceGridTypesList.add(mapSmTypeToProxy(smType)));
    }

    public static ProxyType mapSmTypeToProxy(final SoftwareModuleType smType) {
        final ProxyType smTypeItem = new ProxyType();

        smTypeItem.setId(smType.getId());
        smTypeItem.setName(smType.getName());
        smTypeItem.setKey(smType.getKey());
        smTypeItem.setDescription(smType.getDescription());

        return smTypeItem;
    }

    private SmTypeSelectedGrid buildSelectedGrid() {
        final SmTypeSelectedGrid grid = new SmTypeSelectedGrid(i18n);
        selectedGridTypesList = new ArrayList<>();
        grid.setItems(selectedGridTypesList);

        return grid;
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

    public SmTypeSelectedGrid getSelectedGrid() {
        return selectedGrid;
    }

    public List<ProxyType> getSourceGridTypesList() {
        return sourceGridTypesList;
    }

    public List<ProxyType> getSelectedGridTypesList() {
        return selectedGridTypesList;
    }

    public HorizontalLayout getDistTypeSelectLayout() {
        return distTypeSelectLayout;
    }

    /**
     * Resets the tables for selecting the software modules
     */
    public void reset() {
        selectedGrid.setItems(Collections.emptyList());
        populateSmTypeSourceGrid();
        // TODO: should we call refreshAll on SourceGrid here?
    }

}
