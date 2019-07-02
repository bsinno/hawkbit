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

import org.eclipse.hawkbit.ui.management.tag.TagIdName;

/**
 *
 *
 */
public class ProxyTag extends ProxyIdentifiableEntity {

    private static final long serialVersionUID = 1L;

    private TagIdName tagIdName;

    private String name;

    private String colour;

    private String description;

    /**
     * Proxy tag constructor.
     */
    public ProxyTag() {
        final Integer generatedIntId = new SecureRandom().nextInt(Integer.MAX_VALUE) - Integer.MAX_VALUE;
        tagIdName = new TagIdName(generatedIntId.toString(), null);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public TagIdName getTagIdName() {
        return tagIdName;
    }

    public void setTagIdName(final TagIdName tagIdName) {
        this.tagIdName = tagIdName;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(final String colour) {
        this.colour = colour;
    }

}
