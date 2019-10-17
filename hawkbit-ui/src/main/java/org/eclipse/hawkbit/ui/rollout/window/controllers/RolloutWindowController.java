/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.controllers;

import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AbstractRolloutWindowLayout;

public interface RolloutWindowController {

    AbstractRolloutWindowLayout getLayout();

    void populateWithData(final ProxyRollout proxyRollout);

    SaveDialogCloseListener getSaveDialogCloseListener();
}
