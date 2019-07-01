/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import org.eclipse.hawkbit.repository.model.DistributionSet;

/**
 * Proxy for {@link DistributionSet}.
 */
public class ProxyDistributionSet extends ProxyNamedEntity {

    private static final long serialVersionUID = 1L;

    private Long distId;

    private Boolean isComplete;

    private String nameVersion;

    public String getNameVersion() {
        return nameVersion;
    }

    public void setNameVersion(final String nameVersion) {
        this.nameVersion = nameVersion;
    }

    public Boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(final Boolean isComplete) {
        this.isComplete = isComplete;
    }

    public Long getDistId() {
        return distId;
    }

    public void setDistId(final Long distId) {
        this.distId = distId;
    }
}