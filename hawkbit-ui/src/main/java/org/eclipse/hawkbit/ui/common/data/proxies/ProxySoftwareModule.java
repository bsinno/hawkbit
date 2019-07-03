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

import org.eclipse.hawkbit.repository.model.SoftwareModule;

/**
 * Proxy for {@link SoftwareModule} to display details in Software modules
 * table.
 */
public class ProxySoftwareModule extends ProxyNamedEntity {

    private static final long serialVersionUID = 1L;

    private static final SecureRandom RANDOM_OBJ = new SecureRandom();

    private String nameAndVersion;

    private Long swId;

    private String vendor;

    /**
     * Default constructor.
     */
    public ProxySoftwareModule() {
        swId = RANDOM_OBJ.nextLong();
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(final String vendor) {
        this.vendor = vendor;
    }

    public Long getSwId() {
        return swId;
    }

    public void setSwId(final Long swId) {
        this.swId = swId;
    }

    public String getNameAndVersion() {
        return nameAndVersion;
    }

    public void setNameAndVersion(final String nameAndVersion) {
        this.nameAndVersion = nameAndVersion;
    }
}
