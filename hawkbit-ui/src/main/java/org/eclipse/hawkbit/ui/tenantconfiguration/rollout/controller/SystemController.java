package org.eclipse.hawkbit.ui.tenantconfiguration.rollout.controller;

import org.eclipse.hawkbit.ui.common.CommonDialogWindow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigWindow;
import org.eclipse.hawkbit.ui.tenantconfiguration.window.layouts.AbstractSystemConfigWindowLayout;

public interface SystemController {
    AbstractSystemConfigWindowLayout getLayout();
    void populateWithData(final ProxySystemConfigWindow proxySystemConfig);
    CommonDialogWindow.SaveDialogCloseListener getSaveDialogCloseListener();
}
