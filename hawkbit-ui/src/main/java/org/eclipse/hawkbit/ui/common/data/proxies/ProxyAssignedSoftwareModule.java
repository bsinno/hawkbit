/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

/**
 * Proxy for assignable software module to display details in Software modules
 * table.
 */
public class ProxyAssignedSoftwareModule extends ProxySoftwareModule {

    private static final long serialVersionUID = 1L;

    private String colour;

    private Long typeId;

    private boolean assigned;

    public String getColour() {
        return colour;
    }

    public void setColour(final String colour) {
        this.colour = colour;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(final Long typeId) {
        this.typeId = typeId;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(final boolean assigned) {
        this.assigned = assigned;
    }

}
