/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.layout.listener;

import java.util.Collection;
import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener.EntityModifiedAwareSupport;

public class EntityModifiedGridRefreshAwareSupport implements EntityModifiedAwareSupport {
    private final Runnable refreshGridCallback;
    private final Consumer<Collection<Long>> refreshGridItemsCallback;

    public EntityModifiedGridRefreshAwareSupport(final Runnable refreshGridCallback,
            final Consumer<Collection<Long>> refreshGridItemsCallback) {
        this.refreshGridCallback = refreshGridCallback;
        this.refreshGridItemsCallback = refreshGridItemsCallback;
    }

    public static EntityModifiedGridRefreshAwareSupport of(final Runnable refreshGridCallback) {
        return of(refreshGridCallback, null);
    }

    public static EntityModifiedGridRefreshAwareSupport of(final Runnable refreshGridCallback,
            final Consumer<Collection<Long>> refreshGridItemsCallback) {
        return new EntityModifiedGridRefreshAwareSupport(refreshGridCallback, refreshGridItemsCallback);
    }

    @Override
    public void onEntitiesAdded(final Collection<Long> entityIds) {
        refreshGridCallback.run();
    }

    @Override
    public void onEntitiesUpdated(final Collection<Long> entityIds) {
        if (refreshGridItemsCallback == null) {
            refreshGridCallback.run();
        } else {
            refreshGridItemsCallback.accept(entityIds);
        }
    }

    @Override
    public void onEntitiesDeleted(final Collection<Long> entityIds) {
        refreshGridCallback.run();
    }
}
