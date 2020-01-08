/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

/**
 * Proxy to display details for Software Modules.
 */
public class ProxySoftwareModuleDetails extends ProxyIdentifiableEntity {
    private static final long serialVersionUID = 1L;

    private final boolean isMandatory;
    private final Long typeId;
    private final String typeName;
    private final Long smId;
    private final String smNameAndVersion;

    public ProxySoftwareModuleDetails(final boolean isMandatory, final Long typeId, final String typeName,
            final Long smId, final String smNameAndVersion) {
        this.isMandatory = isMandatory;
        this.typeId = typeId;
        this.typeName = typeName;
        this.smId = smId;
        this.smNameAndVersion = smNameAndVersion;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public Long getTypeId() {
        return typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public Long getSmId() {
        return smId;
    }

    public String getSmNameAndVersion() {
        return smNameAndVersion;
    }
}
