/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModuleDetails;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.distributions.event.DistributionsUIEvent;
import org.eclipse.hawkbit.ui.distributions.state.ManageDistUIState;
import org.eclipse.hawkbit.ui.management.event.DistributionTableEvent;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Software module details table.
 * 
 */
public class SoftwareModuleDetailsGrid extends Grid<ProxySoftwareModuleDetails> {

    private static final long serialVersionUID = 1L;

    private static final String SOFT_TYPE_NAME_ID = "typeName";
    private static final String SOFT_MODULES_ID = "softwareModules";
    private static final String SOFT_TYPE_MANDATORY_ID = "mandatory";

    private final boolean isUnassignSoftModAllowed;
    private final SpPermissionChecker permissionChecker;
    private final transient DistributionSetManagement distributionSetManagement;
    private final VaadinMessageSource i18n;
    private final transient EventBus.UIEventBus eventBus;
    private final ManageDistUIState manageDistUIState;
    private final UINotification uiNotification;

    /**
     * Initialize software module table- to be displayed in details layout.
     * 
     * @param i18n
     *            I18N
     * @param isUnassignSoftModAllowed
     *            boolean flag to check for unassign functionality allowed for
     *            the view.
     * @param distributionSetManagement
     *            DistributionSetManagement
     * @param permissionChecker
     *            SpPermissionChecker
     * @param eventBus
     *            SessionEventBus
     * @param manageDistUIState
     *            ManageDistUIState
     * @param uiNotification
     *            UINotification for displaying error and success notifications
     */
    public SoftwareModuleDetailsGrid(final VaadinMessageSource i18n, final boolean isUnassignSoftModAllowed,
            final SpPermissionChecker permissionChecker, final DistributionSetManagement distributionSetManagement,
            final EventBus.UIEventBus eventBus, final ManageDistUIState manageDistUIState,
            final UINotification uiNotification) {
        this.i18n = i18n;
        this.isUnassignSoftModAllowed = isUnassignSoftModAllowed;
        this.permissionChecker = permissionChecker;
        this.distributionSetManagement = distributionSetManagement;
        this.manageDistUIState = manageDistUIState;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        init();
    }

    private void init() {
        addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        addStyleName(SPUIStyleDefinitions.SW_MODULE_TABLE);
        addStyleName("details-layout");

        setSelectionMode(SelectionMode.NONE);
        setSizeFull();
        // same as height of other tabs in details tabsheet
        setHeight(116, Unit.PIXELS);

        addComponentColumn(this::buildIsMandatoryLabel).setId(SOFT_TYPE_MANDATORY_ID);
        addColumn(ProxySoftwareModuleDetails::getTypeName).setId(SOFT_TYPE_NAME_ID);

        getDefaultHeaderRow().join(SOFT_TYPE_MANDATORY_ID, SOFT_TYPE_NAME_ID)
                .setText(i18n.getMessage("header.caption.typename"));

        addComponentColumn(this::buildSoftwareModulesLayout).setId(SOFT_MODULES_ID)
                .setCaption(i18n.getMessage("header.caption.softwaremodule"));
    }

    private Label buildIsMandatoryLabel(final ProxySoftwareModuleDetails softwareModuleDetails) {
        final Label isMandatoryLabel = new Label("");

        isMandatoryLabel.setSizeFull();
        isMandatoryLabel.addStyleName(SPUIDefinitions.TEXT_STYLE);
        isMandatoryLabel.addStyleName("label-style");

        if (softwareModuleDetails.isMandatory()) {
            isMandatoryLabel.setValue("*");
            isMandatoryLabel.setStyleName(SPUIStyleDefinitions.SP_TEXTFIELD_ERROR);
        }

        return isMandatoryLabel;
    }

    private VerticalLayout buildSoftwareModulesLayout(final ProxySoftwareModuleDetails softwareModuleDetails) {
        final VerticalLayout softwareModulesLayout = new VerticalLayout();

        for (final Entry<Long, String> softwareModuleEntry : softwareModuleDetails.getSoftwareModules().entrySet()) {
            final Label softwareModuleNameWithVersionLabel = buildSmLabel(softwareModuleEntry.getValue(),
                    softwareModuleEntry.getKey());

            if (isUnassignSoftModAllowed && permissionChecker.hasUpdateRepositoryPermission()) {
                final HorizontalLayout smLabelWithUnassignButtonLayout = new HorizontalLayout();
                smLabelWithUnassignButtonLayout.setSizeFull();

                smLabelWithUnassignButtonLayout.addComponent(softwareModuleNameWithVersionLabel);
                smLabelWithUnassignButtonLayout.addComponent(buildSmUnassignButton(softwareModuleDetails.getDsId(),
                        softwareModuleDetails.getDsName(), softwareModuleDetails.getDsVersion(),
                        softwareModuleEntry.getKey(), softwareModuleEntry.getValue()));

                softwareModulesLayout.addComponent(smLabelWithUnassignButtonLayout);
            } else {
                softwareModulesLayout.addComponent(softwareModuleNameWithVersionLabel);
            }
        }

        return softwareModulesLayout;
    }

    private Label buildSmLabel(final String smNameWithVersion, final Long smId) {
        final Label smLabel = new Label(smNameWithVersion);

        smLabel.setId("sm-label-" + smId);
        smLabel.setSizeFull();
        smLabel.addStyleName(SPUIDefinitions.TEXT_STYLE);
        smLabel.addStyleName("label-style");

        return smLabel;
    }

    private Button buildSmUnassignButton(final Long dsId, final String dsName, final String dsVersion, final Long smId,
            final String smNameAndVersion) {
        final Button unassignSoftwareModuleButton = new Button(VaadinIcons.CLOSE_SMALL);

        unassignSoftwareModuleButton.setId("sm-unassign-button-" + smId);
        unassignSoftwareModuleButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        unassignSoftwareModuleButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        unassignSoftwareModuleButton.addStyleName("button-no-border");

        unassignSoftwareModuleButton
                .addClickListener(event -> unassignSoftwareModule(dsId, dsName, dsVersion, smId, smNameAndVersion));

        return unassignSoftwareModuleButton;
    }

    private void unassignSoftwareModule(final Long dsId, final String dsName, final String dsVersion, final Long smId,
            final String smNameAndVersion) {
        if (distributionSetManagement.isInUse(dsId)) {
            uiNotification.displayValidationError(
                    i18n.getMessage("message.error.notification.ds.target.assigned", dsName, dsVersion));
        } else {
            final DistributionSet newDistributionSet = distributionSetManagement.unassignSoftwareModule(dsId, smId);
            manageDistUIState.setLastSelectedEntityId(newDistributionSet.getId());
            eventBus.publish(this, new DistributionTableEvent(BaseEntityEventType.SELECTED_ENTITY,
                    new DistributionSetToProxyDistributionMapper().map(newDistributionSet)));
            eventBus.publish(this, DistributionsUIEvent.ORDER_BY_DISTRIBUTION);
            uiNotification.displaySuccess(i18n.getMessage("message.sw.unassigned", smNameAndVersion));
        }
    }

    /**
     * Populate software module grid.
     * 
     * @param distributionSet
     */
    public void populateModule(final ProxyDistributionSet distributionSet) {
        if (distributionSet == null) {
            setItems(Collections.emptyList());
            return;
        }

        final List<ProxySoftwareModuleDetails> items = new ArrayList<>();

        for (final SoftwareModuleType mandatoryType : distributionSet.getType().getMandatoryModuleTypes()) {
            final ProxySoftwareModuleDetails smDetails = getDetailsByDsAndType(distributionSet, true, mandatoryType);
            items.add(smDetails);
        }

        for (final SoftwareModuleType optionalType : distributionSet.getType().getOptionalModuleTypes()) {
            final ProxySoftwareModuleDetails smDetails = getDetailsByDsAndType(distributionSet, false, optionalType);
            items.add(smDetails);
        }

        setItems(items);
    }

    private ProxySoftwareModuleDetails getDetailsByDsAndType(final ProxyDistributionSet distributionSet,
            final boolean isMandatory, final SoftwareModuleType type) {
        return new ProxySoftwareModuleDetails(distributionSet.getId(), distributionSet.getName(),
                distributionSet.getVersion(), isMandatory, type.getName(),
                distributionSet.getModules().stream().filter(sm -> sm.getType().getKey().equals(type.getKey()))
                        .collect(Collectors.toMap(ProxySoftwareModule::getId, ProxySoftwareModule::getNameAndVersion)));
    }
}
