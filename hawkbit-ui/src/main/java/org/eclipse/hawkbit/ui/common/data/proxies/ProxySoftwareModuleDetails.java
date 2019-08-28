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
    private Map<Long, String> softwareModules;
    private Long dsId;
    private String dsName;
    private String dsVersion;

    public ProxySoftwareModuleDetails(final Long dsId, final String dsName, final String dsVersion,
            final boolean isMandatory, final String typeName, final Map<Long, String> softwareModules) {
        this.dsId = dsId;
        this.dsName = dsName;
        this.dsVersion = dsVersion;
        this.isMandatory = isMandatory;
        this.typeName = typeName;
        this.softwareModules = softwareModules;
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

    public Map<Long, String> getSoftwareModules() {
        return softwareModules;
    }

    public void setSoftwareModules(final Map<Long, String> softwareModules) {
        this.softwareModules = softwareModules;
    }

    public Long getDsId() {
        return dsId;
    }

    public void setDsId(final Long dsId) {
        this.dsId = dsId;
    }

    public String getDsName() {
        return dsName;
    }

    public void setDsName(final String dsName) {
        this.dsName = dsName;
    }

    public String getDsVersion() {
        return dsVersion;
    }

    public void setDsVersion(final String dsVersion) {
        this.dsVersion = dsVersion;
    }
}
