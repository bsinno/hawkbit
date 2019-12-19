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
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetTypeToProxyDistributionSetTypeMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetProxyTypeDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSetType;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigWindow;
import org.eclipse.hawkbit.ui.tenantconfiguration.window.SystemConfigWindowDependencies;
import org.eclipse.hawkbit.ui.tenantconfiguration.window.SystemConfigWindowLayoutComponentBuilder;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.icons.VaadinIcons;
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

    private final VaadinMessageSource i18n;

    private final SpPermissionChecker permissionChecker;
    private Long currentDefaultDisSetType;
    private Long selectedDefaultDisSetType;
    private TenantMetaData tenantMetaData;
    private ComboBox<ProxyDistributionSetType> dsSetComboBox = new ComboBox<>();
    private Binder<ProxySystemConfigWindow> binder;
    private final SystemConfigWindowLayoutComponentBuilder builder;
    final SystemConfigWindowDependencies dependencies;
    private Label changeIcon;

    DefaultDistributionSetTypeLayout(final SystemManagement systemManagement, final VaadinMessageSource i18n,
            final SpPermissionChecker permChecker,final Binder<ProxySystemConfigWindow> binder,
            final DistributionSetTypeManagement typeManagement) {
        this.systemManagement = systemManagement;
        this.i18n = i18n;
        this.permissionChecker = permChecker;
        this.binder = binder;
        final DistributionSetProxyTypeDataProvider dataProvider = new DistributionSetProxyTypeDataProvider(typeManagement, new DistributionSetTypeToProxyDistributionSetTypeMapper());
        this.dependencies = new SystemConfigWindowDependencies(systemManagement, i18n, permChecker, typeManagement, dataProvider, tenantMetaData);
        this.builder = new SystemConfigWindowLayoutComponentBuilder(this.dependencies);
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

        final Label configurationLabel = new LabelBuilder().name(
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
        dsSetComboBox = builder.createDistributionSetCombo(binder);
    }

    private DistributionSetType getCurrentDistributionSetType() {
        tenantMetaData = this.systemManagement.getTenantMetadata();
        return tenantMetaData.getDefaultDsType();
    }

    @Override
    public void save() {
        selectedDefaultDisSetType = binder.getBean().getDistributionSetTypeId();
        if (!currentDefaultDisSetType.equals(selectedDefaultDisSetType) && selectedDefaultDisSetType != null) {
            tenantMetaData = this.systemManagement.updateTenantMetadata(binder.getBean().getDistributionSetTypeId());
            currentDefaultDisSetType = selectedDefaultDisSetType;
        }
        changeIcon.setVisible(false);
    }

    @Override
    public void undo() {
        selectedDefaultDisSetType = currentDefaultDisSetType;
        changeIcon.setVisible(false);
    }

}
