package org.eclipse.hawkbit.ui.rollout.window.controllers;

import org.eclipse.hawkbit.ui.common.CommonDialogWindowNew.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AbstractRolloutWindowLayout;

public interface RolloutWindowController {

    AbstractRolloutWindowLayout getLayout();

    void populateWithData(final ProxyRollout proxyRollout);

    SaveDialogCloseListener getSaveDialogCloseListener();
}
