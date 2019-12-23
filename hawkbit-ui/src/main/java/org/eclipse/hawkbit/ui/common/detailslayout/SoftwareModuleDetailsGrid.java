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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModuleDetails;
import org.eclipse.hawkbit.ui.common.event.DsModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.grid.HeightMode;
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

    private final VaadinMessageSource i18n;
    private final transient UIEventBus eventBus;
    private final UINotification uiNotification;
    private final SpPermissionChecker permissionChecker;

    private final transient DistributionSetManagement distributionSetManagement;
    private final transient SoftwareModuleManagement smManagement;
    private final transient DistributionSetTypeManagement dsTypeManagement;

    private final boolean isUnassignSmAllowed;

    private ProxyDistributionSet masterEntity;

    /**
     * Initialize software module table- to be displayed in details layout.
     * 
     * @param i18n
     *            I18N
     * @param eventBus
     *            SessionEventBus
     * @param uiNotification
     *            UINotification for displaying error and success notifications
     * @param permissionChecker
     *            SpPermissionChecker
     * @param distributionSetManagement
     *            DistributionSetManagement
     * @param smManagement
     *            SoftwareModuleManagement
     * @param dsTypeManagement
     *            DistributionSetTypeManagement
     * @param isUnassignSmAllowed
     *            boolean flag to check for unassign functionality allowed for
     *            the view.
     */
    public SoftwareModuleDetailsGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final UINotification uiNotification, final SpPermissionChecker permissionChecker,
            final DistributionSetManagement distributionSetManagement, final SoftwareModuleManagement smManagement,
            final DistributionSetTypeManagement dsTypeManagement, final boolean isUnassignSmAllowed) {
        this.i18n = i18n;
        this.uiNotification = uiNotification;
        this.eventBus = eventBus;
        this.permissionChecker = permissionChecker;

        this.distributionSetManagement = distributionSetManagement;
        this.smManagement = smManagement;
        this.dsTypeManagement = dsTypeManagement;

        this.isUnassignSmAllowed = isUnassignSmAllowed;

        init();
    }

    private void init() {
        setSizeFull();
        setHeightMode(HeightMode.UNDEFINED);

        addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        addStyleName(SPUIStyleDefinitions.SW_MODULE_TABLE);
        addStyleName("details-layout");

        setSelectionMode(SelectionMode.NONE);

        // same as height of other tabs in details tabsheet
        // setHeight(116, Unit.PIXELS);

        addColumns();
    }

    private void addColumns() {
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
        softwareModulesLayout.setSpacing(false);
        softwareModulesLayout.setMargin(false);

        for (final Entry<Long, String> softwareModuleEntry : softwareModuleDetails.getSmIdsWithNameAndVersion()
                .entrySet()) {
            final Label softwareModuleNameWithVersionLabel = buildSmLabel(softwareModuleEntry.getValue(),
                    softwareModuleEntry.getKey());

            if (isUnassignSmAllowed && permissionChecker.hasUpdateRepositoryPermission()) {
                final HorizontalLayout smLabelWithUnassignButtonLayout = new HorizontalLayout();
                smLabelWithUnassignButtonLayout.setSpacing(false);
                smLabelWithUnassignButtonLayout.setMargin(false);
                smLabelWithUnassignButtonLayout.setSizeFull();

                smLabelWithUnassignButtonLayout.addComponent(softwareModuleNameWithVersionLabel);
                smLabelWithUnassignButtonLayout.addComponent(
                        buildSmUnassignButton(softwareModuleEntry.getKey(), softwareModuleEntry.getValue()));

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

    private Button buildSmUnassignButton(final Long smId, final String smNameAndVersion) {
        final Button unassignSoftwareModuleButton = new Button(VaadinIcons.CLOSE_SMALL);

        unassignSoftwareModuleButton.setId("sm-unassign-button-" + smId);
        unassignSoftwareModuleButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        unassignSoftwareModuleButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        unassignSoftwareModuleButton.addStyleName("button-no-border");

        unassignSoftwareModuleButton.addClickListener(event -> unassignSoftwareModule(smId, smNameAndVersion));

        return unassignSoftwareModuleButton;
    }

    private void unassignSoftwareModule(final Long smId, final String smNameAndVersion) {
        if (masterEntity == null) {
            // TODO: use i18n
            uiNotification.displayValidationError("no distribution set selected");
            return;
        }

        final Long dsId = masterEntity.getId();

        if (distributionSetManagement.isInUse(dsId)) {
            uiNotification.displayValidationError(i18n.getMessage("message.error.notification.ds.target.assigned",
                    masterEntity.getName(), masterEntity.getVersion()));
        } else {
            distributionSetManagement.unassignSoftwareModule(dsId, smId);

            eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                    new DsModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, dsId));
            uiNotification.displaySuccess(i18n.getMessage("message.sw.unassigned", smNameAndVersion));
        }
    }

    public void updateMasterEntityFilter(final ProxyDistributionSet masterEntityFilter) {
        if (masterEntityFilter == null) {
            masterEntity = null;
            setItems(Collections.emptyList());
            return;
        }

        final Optional<DistributionSetType> dsType = dsTypeManagement.get(masterEntityFilter.getTypeId());

        final List<ProxySoftwareModuleDetails> items = new ArrayList<>();

        // TODO: try to optimize
        dsType.ifPresent(type -> {
            final Collection<SoftwareModule> softwareModules = getSoftwareModulesByDsId(masterEntityFilter.getId());

            for (final SoftwareModuleType mandatoryType : type.getMandatoryModuleTypes()) {
                final ProxySoftwareModuleDetails smDetails = getSmDetailsByType(softwareModules, mandatoryType, true);
                items.add(smDetails);
            }

            for (final SoftwareModuleType optionalType : type.getOptionalModuleTypes()) {
                final ProxySoftwareModuleDetails smDetails = getSmDetailsByType(softwareModules, optionalType, false);
                items.add(smDetails);
            }
        });

        masterEntity = masterEntityFilter;
        setItems(items);
    }

    private Collection<SoftwareModule> getSoftwareModulesByDsId(final Long dsId) {
        Pageable query = PageRequest.of(0, SPUIDefinitions.PAGE_SIZE);
        Page<SoftwareModule> smPage;
        final Collection<SoftwareModule> softwareModules = new ArrayList<>();

        do {
            smPage = smManagement.findByAssignedTo(query, dsId);
            softwareModules.addAll(smPage.getContent());
        } while ((query = smPage.nextPageable()) != Pageable.unpaged());

        return softwareModules;
    }

    private ProxySoftwareModuleDetails getSmDetailsByType(final Collection<SoftwareModule> softwareModules,
            final SoftwareModuleType type, final boolean isMandatory) {
        final Map<Long, String> smIdsWithNameAndVersion = softwareModules.stream()
                .filter(sm -> sm.getType().getId().equals(type.getId())).collect(Collectors.toMap(SoftwareModule::getId,
                        sm -> HawkbitCommonUtil.concatStrings(":", sm.getName(), sm.getVersion())));

        return new ProxySoftwareModuleDetails(isMandatory, type.getName(), smIdsWithNameAndVersion);
    }
}
