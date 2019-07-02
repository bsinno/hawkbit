/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.mappers;

import org.eclipse.hawkbit.repository.model.TargetTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.management.tag.TagIdName;

/**
 * Maps {@link TargetTag} entities, fetched from backend, to the
 * {@link ProxyTag} entities.
 */
public class TargetTagToProxyTagMapper
        implements IdentifiableEntityToProxyIdentifiableEntityMapper<ProxyTag, TargetTag> {

    @Override
    public ProxyTag map(final TargetTag tag) {
        final ProxyTag proxyTargetTag = new ProxyTag();

        proxyTargetTag.setColour(tag.getColour());
        proxyTargetTag.setDescription(tag.getDescription());
        proxyTargetTag.setName(tag.getName());
        proxyTargetTag.setId(tag.getId());
        final TagIdName targetTagIdName = new TagIdName(tag.getName(), tag.getId());
        proxyTargetTag.setTagIdName(targetTagIdName);

        return proxyTargetTag;
    }

}
