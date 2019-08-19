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
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;

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

    // TODO: can ProxyType be used here?
    private SoftwareModuleType type;

    private boolean assigned;

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

    public SoftwareModuleType getType() {
        return type;
    }

    public void setType(final SoftwareModuleType type) {
        this.type = type;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }
}
