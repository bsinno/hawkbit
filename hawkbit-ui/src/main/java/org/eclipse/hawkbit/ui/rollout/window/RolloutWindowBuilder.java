/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window;

import org.eclipse.hawkbit.ui.common.CommonDialogWindowNew;
import org.eclipse.hawkbit.ui.common.CommonDialogWindowNew.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.builder.WindowBuilderNew;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.rollout.window.controllers.AddRolloutWindowController;
import org.eclipse.hawkbit.ui.rollout.window.controllers.CopyRolloutWindowController;
import org.eclipse.hawkbit.ui.rollout.window.controllers.RolloutWindowController;
import org.eclipse.hawkbit.ui.rollout.window.controllers.UpdateRolloutWindowController;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AddRolloutWindowLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.ApproveRolloutWindowLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.UpdateRolloutWindowLayout;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

/**
 * Builder for Add/Approve/Update/Copy Rollout windows.
 */
public final class RolloutWindowBuilder {

    private final RolloutWindowDependencies dependencies;

    public RolloutWindowBuilder(final RolloutWindowDependencies rolloutWindowDependencies) {
        this.dependencies = rolloutWindowDependencies;
    }

    public Window getWindowForAddRollout() {
        return getWindowForRollout(null,
                new AddRolloutWindowController(dependencies, new AddRolloutWindowLayout(dependencies)));
    }

    public Window getWindowForCopyRollout(final ProxyRollout proxyRollout) {

        return getWindowForRollout(proxyRollout,
                new CopyRolloutWindowController(dependencies, new AddRolloutWindowLayout(dependencies)));
    }

    public Window getWindowForUpdateRollout(final ProxyRollout proxyRollout) {

        return getWindowForRollout(proxyRollout,
                new UpdateRolloutWindowController(dependencies, new UpdateRolloutWindowLayout(dependencies)));
    }

    public Window getWindowForApproveRollout(final ProxyRollout proxyRollout) {

        return getWindowForRollout(proxyRollout,
                new UpdateRolloutWindowController(dependencies, new ApproveRolloutWindowLayout(dependencies)));
    }

    private Window getWindowForRollout(final ProxyRollout proxyRollout, final RolloutWindowController controller) {
        controller.populateWithData(proxyRollout);

        final CommonDialogWindowNew window = createWindow(controller.getLayout(),
                controller.getSaveDialogCloseListener());

        controller.getLayout().addValidationListener(window::setSaveButtonEnabled);

        return window;

    }

    private CommonDialogWindowNew createWindow(final Component content,
            final SaveDialogCloseListener saveDialogCloseListener) {
        return new WindowBuilderNew(SPUIDefinitions.CREATE_UPDATE_WINDOW).id(UIComponentIdProvider.ROLLOUT_POPUP_ID)
                .caption(dependencies.getI18n().getMessage("caption.create.new",
                        dependencies.getI18n().getMessage("caption.rollout")))
                .content(content).i18n(dependencies.getI18n())
                .helpLink(dependencies.getUiProperties().getLinks().getDocumentation().getRolloutView())
                .saveDialogCloseListener(saveDialogCloseListener).buildCommonDialogWindow();
    }

}
