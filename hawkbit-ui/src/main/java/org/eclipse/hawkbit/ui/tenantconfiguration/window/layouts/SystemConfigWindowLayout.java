package org.eclipse.hawkbit.ui.tenantconfiguration.window.layouts;

import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.ui.tenantconfiguration.window.SystemConfigWindowDependencies;
import org.eclipse.hawkbit.ui.tenantconfiguration.window.SystemConfigWindowLayoutComponentBuilder;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

public class SystemConfigWindowLayout extends AbstractSystemConfigWindowLayout {
    private static final long serialVersionUID = 1L;
    private final VaadinMessageSource i18n;
    private final SystemManagement systemManagement;

    protected SystemConfigWindowLayout(SystemConfigWindowDependencies dependencies) {
        super(dependencies);
        this.i18n = dependencies.getI18n();
        this.systemManagement = dependencies.getSystemManagement();
        buildLayout(componentBuilder);
    }

    private void buildLayout(final SystemConfigWindowLayoutComponentBuilder componentBuilder) {
        addComponent(componentBuilder.getLabel("Distribution Configuration"),0,0);
        addComponent(componentBuilder.createDistributionSetCombo(proxySystemConfigBinder));
    }

}
