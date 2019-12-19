/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import org.eclipse.hawkbit.repository.model.Tag;

/**
 * Proxy for {@link Tag}.
 */
public class ProxyTag extends ProxyFilterButton {

    private static final long serialVersionUID = 1L;

    // TODO: check if dummy 'ProxyNoTag extends ProxyTag' class is better
    private boolean isNoTag;

    public boolean isNoTag() {
        return isNoTag;
    }

    public void setNoTag(final boolean isNoTag) {
        this.isNoTag = isNoTag;
    }
}
