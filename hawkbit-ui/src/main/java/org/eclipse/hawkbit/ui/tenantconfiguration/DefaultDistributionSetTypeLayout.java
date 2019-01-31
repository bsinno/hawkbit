/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration;

import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.repository.model.TenantMetaData;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.PageRequest;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * Default DistributionSet Panel.
 */
public class DefaultDistributionSetTypeLayout extends BaseConfigurationView {

    private static final long serialVersionUID = 1L;

    private final transient SystemManagement systemManagement;

    private Long currentDefaultDisSetType;

    private Long selectedDefaultDisSetType;

    private TenantMetaData tenantMetaData;

    private final ComboBox combobox;

    private final Label changeIcon;

    DefaultDistributionSetTypeLayout(final SystemManagement systemManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement, final VaadinMessageSource i18n,
            final SpPermissionChecker permChecker) {
        this.systemManagement = systemManagement;
        combobox = SPUIComponentProvider.getComboBox(null, "330", null, null, false, "", "label.combobox.tag");
        changeIcon = new Label();

        if (!permChecker.hasReadRepositoryPermission()) {
            return;
        }

        final Panel rootPanel = new Panel();
        rootPanel.setSizeFull();
        rootPanel.addStyleName("config-panel");
        final VerticalLayout vlayout = new VerticalLayout();
        vlayout.setMargin(true);
        vlayout.setSizeFull();

        final Label header = new Label(i18n.getMessage("configuration.defaultdistributionset.title"));
        header.addStyleName("config-panel-header");
        vlayout.addComponent(header);

        final DistributionSetType currentDistributionSetType = getCurrentDistributionSetType();
        currentDefaultDisSetType = currentDistributionSetType.getId();

        final HorizontalLayout hlayout = new HorizontalLayout();
        hlayout.setSpacing(true);
        hlayout.setImmediate(true);

        final Label configurationLabel = new LabelBuilder()
                .name(i18n.getMessage("configuration.defaultdistributionset.select.label")).buildLabel();
        hlayout.addComponent(configurationLabel);

        final Iterable<DistributionSetType> distributionSetTypeCollection = distributionSetTypeManagement
                .findAll(PageRequest.of(0, 100));

        combobox.setId(UIComponentIdProvider.SYSTEM_CONFIGURATION_DEFAULTDIS_COMBOBOX);
        combobox.setNullSelectionAllowed(false);
        for (final DistributionSetType distributionSetType : distributionSetTypeCollection) {
            combobox.addItem(distributionSetType.getId());
            combobox.setItemCaption(distributionSetType.getId(),
                    distributionSetType.getKey() + " (" + distributionSetType.getName() + ")");

            if (distributionSetType.getId().equals(currentDistributionSetType.getId())) {
                combobox.select(distributionSetType.getId());
            }
        }
        combobox.setImmediate(true);
        combobox.addValueChangeListener(event -> selectDistributionSetValue());
        hlayout.addComponent(combobox);

        changeIcon.setIcon(FontAwesome.CHECK);
        hlayout.addComponent(changeIcon);
        changeIcon.setVisible(false);

        vlayout.addComponent(hlayout);
        rootPanel.setContent(vlayout);
        setCompositionRoot(rootPanel);
    }

    private DistributionSetType getCurrentDistributionSetType() {
        tenantMetaData = systemManagement.getTenantMetadata();
        return tenantMetaData.getDefaultDsType();
    }

    @Override
    public void save() {
        if (!currentDefaultDisSetType.equals(selectedDefaultDisSetType) && selectedDefaultDisSetType != null) {
            tenantMetaData = systemManagement.updateTenantMetadata(selectedDefaultDisSetType);
            currentDefaultDisSetType = selectedDefaultDisSetType;
        }
        changeIcon.setVisible(false);
    }

    @Override
    public void undo() {
        combobox.select(currentDefaultDisSetType);
        selectedDefaultDisSetType = currentDefaultDisSetType;
        changeIcon.setVisible(false);
    }

    /**
     * Method that is called when combobox event is performed.
     */
    private void selectDistributionSetValue() {
        selectedDefaultDisSetType = (Long) combobox.getValue();
        if (!selectedDefaultDisSetType.equals(currentDefaultDisSetType)) {
            changeIcon.setVisible(true);
            notifyConfigurationChanged();
        } else {
            changeIcon.setVisible(false);
        }
    }

}
