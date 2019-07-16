/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;

/**
 * Master-details support for the identifiable master-data entities.
 * 
 * @param <T>
 *            The masterItem-type
 */
public class MasterDetailsSupportIdentifiable<T extends ProxyIdentifiableEntity> extends MasterDetailsSupport<T, Long> {

    public MasterDetailsSupportIdentifiable(final AbstractGrid<?, Long> grid) {
        super(grid);
    }

    @Override
    protected Long mapMasterItemToDetailsFilter(final T masterItem) {
        return masterItem.getId();
    }
}
