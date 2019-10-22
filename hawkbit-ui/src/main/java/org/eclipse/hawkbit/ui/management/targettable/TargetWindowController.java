/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;

public interface TargetWindowController {

    TargetWindowLayout getLayout();

    void populateWithData(final ProxyTarget proxyTarget);

    SaveDialogCloseListener getSaveDialogCloseListener();
}
