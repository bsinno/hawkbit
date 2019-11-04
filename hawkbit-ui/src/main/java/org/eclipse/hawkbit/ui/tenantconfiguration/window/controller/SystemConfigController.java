package org.eclipse.hawkbit.ui.tenantconfiguration.window.controller;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigWindow;
import org.eclipse.hawkbit.ui.tenantconfiguration.DefaultDistributionSetTypeLayout;
import org.eclipse.hawkbit.ui.tenantconfiguration.window.layouts.AbstractSystemConfigWindowLayout;
import org.eclipse.hawkbit.ui.tenantconfiguration.window.layouts.SystemConfigWindowLayout;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus;

public class SystemConfigController implements SystemController {
    private final SystemManagement systemManagement;
    private final UINotification uiNotification;
    private final EntityFactory entityFactory;
    private final EventBus.UIEventBus eventBus;
    protected final VaadinMessageSource i18n;
    protected final SystemConfigWindowLayout systemConfigLayout;
    protected ProxySystemConfigWindow proxySystemConfigWindow;

    final DefaultDistributionSetTypeLayout defaultDistributionSetTypeLayout;

    public SystemConfigController(SystemManagement systemManagement, UINotification uiNotification,
            EntityFactory entityFactory, EventBus.UIEventBus eventBus, VaadinMessageSource i18n,
            SystemConfigWindowLayout systemConfigLayout,
            DefaultDistributionSetTypeLayout defaultDistributionSetTypeLayout) {
        this.systemManagement = systemManagement;
        this.uiNotification = uiNotification;
        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.i18n = i18n;
        this.systemConfigLayout = systemConfigLayout;
        this.defaultDistributionSetTypeLayout = defaultDistributionSetTypeLayout;
    }

    @Override
    public AbstractSystemConfigWindowLayout getLayout() {
        return systemConfigLayout;
    }

    @Override
    public void populateWithData(ProxySystemConfigWindow proxySystemConfig) {
        ProxySystemConfigWindow proxySystemConfigWindow = new ProxySystemConfigWindow();
//        defaultDistributionSetTypeLayout.getProxySystemConfigBinder().setBean(proxySystemConfigWindow);

    }

    @Override
    public CommonDialogWindow.SaveDialogCloseListener getSaveDialogCloseListener() {
        return null;
    }
}
