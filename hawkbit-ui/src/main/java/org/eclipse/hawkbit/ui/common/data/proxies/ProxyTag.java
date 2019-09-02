/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import java.security.SecureRandom;

import org.eclipse.hawkbit.repository.model.Tag;
import org.eclipse.hawkbit.ui.management.tag.TagIdName;

/**
 * Proxy for {@link Tag}.
 */
public class ProxyTag extends ProxyFilterButton {

    private static final long serialVersionUID = 1L;

    private TagIdName tagIdName;

    /**
     * Proxy tag constructor.
     */
    public ProxyTag() {
        final Integer generatedIntId = new SecureRandom().nextInt(Integer.MAX_VALUE) - Integer.MAX_VALUE;
        tagIdName = new TagIdName(generatedIntId.toString(), null);
    }

    public TagIdName getTagIdName() {
        return tagIdName;
    }

    public void setTagIdName(final TagIdName tagIdName) {
        this.tagIdName = tagIdName;
    }
}
