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
import org.eclipse.hawkbit.ui.common.builder.LabelBuilderV7;
import org.eclipse.hawkbit.ui.common.data.mappers.TypeToProxyTypeMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetTypeDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.Label;

/**
 * Default DistributionSet Panel.
 */
public class DefaultDistributionSetTypeLayout extends BaseConfigurationView {

    private static final long serialVersionUID = 1L;

    private final transient SystemManagement systemManagement;

    private final VaadinMessageSource i18n;

    private final SpPermissionChecker permissionChecker;

    private final transient DistributionSetTypeManagement distributionSetTypeManagement;

    private Long currentDefaultDisSetType;

    private Long selectedDefaultDisSetType;

    private TenantMetaData tenantMetaData;

    private ComboBox<ProxyType> dsSetComboBox;

    private Label changeIcon;

    private TypeToProxyTypeMapper<DistributionSetType> mapper = new TypeToProxyTypeMapper<>();

    DefaultDistributionSetTypeLayout(final SystemManagement systemManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement, final VaadinMessageSource i18n,
            final SpPermissionChecker permChecker) {
        this.systemManagement = systemManagement;
        this.i18n = i18n;
        this.permissionChecker = permChecker;
        this.distributionSetTypeManagement = distributionSetTypeManagement;
        initDsSetTypeComponent();
    }

    private void initDsSetTypeComponent() {
        if (!permissionChecker.hasReadRepositoryPermission()) {
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

        final Label configurationLabel = new LabelBuilderV7().name(
                i18n.getMessage("configuration.defaultdistributionset.select.label")).buildLabel();
        hlayout.addComponent(configurationLabel);

        initDsSetComboBox();
        hlayout.addComponent(dsSetComboBox);

        changeIcon = new Label();
        changeIcon.setIcon(VaadinIcons.CHECK);
        hlayout.addComponent(changeIcon);
        changeIcon.setVisible(false);

        vlayout.addComponent(hlayout);
        rootPanel.setContent(vlayout);
        setCompositionRoot(rootPanel);
    }

    private void initDsSetComboBox() {
        dsSetComboBox = new ComboBox<>();
        dsSetComboBox.setDescription(i18n.getMessage(UIMessageIdProvider.CAPTION_DISTRIBUTION_TAG));
        dsSetComboBox.setId(UIComponentIdProvider.SYSTEM_CONFIGURATION_DEFAULTDIS_COMBOBOX);
        dsSetComboBox.addStyleName(SPUIDefinitions.COMBO_BOX_SPECIFIC_STYLE);
        dsSetComboBox.addStyleName(ValoTheme.COMBOBOX_TINY);
        dsSetComboBox.setEmptySelectionAllowed(false);
        dsSetComboBox.setItemCaptionGenerator(ProxyType::getName);
        dsSetComboBox.setDataProvider(
                new DistributionSetTypeDataProvider(distributionSetTypeManagement, new TypeToProxyTypeMapper<>()));
        dsSetComboBox.setValue(mapper.map(getCurrentDistributionSetType()));
        dsSetComboBox.addValueChangeListener(event -> selectDistributionSetValue());
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
        dsSetComboBox.setValue(mapper.map(getCurrentDistributionSetType()));
        selectedDefaultDisSetType = currentDefaultDisSetType;
        changeIcon.setVisible(false);
    }

    /**
     * Method that is called when combobox event is performed.
     */
    private void selectDistributionSetValue() {
        selectedDefaultDisSetType = dsSetComboBox.getSelectedItem().map(ProxyType::getId).orElse(null);
        if (!selectedDefaultDisSetType.equals(currentDefaultDisSetType)) {
            changeIcon.setVisible(true);
            notifyConfigurationChanged();
        } else {
            changeIcon.setVisible(false);
        }
    }

}
