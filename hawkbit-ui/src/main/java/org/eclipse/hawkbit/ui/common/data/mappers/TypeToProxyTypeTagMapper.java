/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.mappers;

import org.eclipse.hawkbit.repository.model.Tag;
import org.eclipse.hawkbit.repository.model.Type;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;

/**
 * Maps {@link Tag} entities, fetched from backend, to the {@link ProxyTag}
 * entities.
 */
public class TypeToProxyTypeTagMapper<T extends Type>
        extends AbstractNamedEntityToProxyNamedEntityMapper<ProxyType, T> {

    @Override
    public ProxyType map(final T type) {
        final ProxyType proxyType = new ProxyType();

        mapNamedEntityAttributes(type, proxyType);

        proxyType.setKey(type.getKey());
        proxyType.setColour(type.getColour());
        proxyType.setDeleted(type.isDeleted());

        return proxyType;
    }

}
