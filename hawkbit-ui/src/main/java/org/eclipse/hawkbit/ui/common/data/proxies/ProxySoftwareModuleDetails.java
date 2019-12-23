/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import java.util.Map;

/**
 * Proxy to display details for Software Modules.
 */
public class ProxySoftwareModuleDetails extends ProxyIdentifiableEntity {
    private static final long serialVersionUID = 1L;

    private boolean isMandatory;
    private String typeName;
    private Map<Long, String> smIdsWithNameAndVersion;

    public ProxySoftwareModuleDetails(final boolean isMandatory, final String typeName,
            final Map<Long, String> smIdsWithNameAndVersion) {
        this.isMandatory = isMandatory;
        this.typeName = typeName;
        this.smIdsWithNameAndVersion = smIdsWithNameAndVersion;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(final boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(final String typeName) {
        this.typeName = typeName;
    }

    public Map<Long, String> getSmIdsWithNameAndVersion() {
        return smIdsWithNameAndVersion;
    }

    public void setSmIdsWithNameAndVersion(final Map<Long, String> smIdsWithNameAndVersion) {
        this.smIdsWithNameAndVersion = smIdsWithNameAndVersion;
    }
}
