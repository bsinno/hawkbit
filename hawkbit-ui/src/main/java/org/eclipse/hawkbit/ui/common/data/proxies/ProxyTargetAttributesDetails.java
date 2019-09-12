/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import java.util.List;

/**
 * Proxy for target attributes details.
 */
// TODO: check if we need to implement Serializable and override equals and
// hashcode
public class ProxyTargetAttributesDetails {

    private final String controllerId;
    private final boolean isRequestAttributes;
    private final List<ProxyKeyValueDetails> targetAttributes;

    // dummy constructor needed for TargetAttributesDetailsComponent getValue
    public ProxyTargetAttributesDetails() {
        this.controllerId = null;
        this.isRequestAttributes = false;
        this.targetAttributes = null;
    }

    public ProxyTargetAttributesDetails(final String controllerId, final boolean isRequestAttributes,
            final List<ProxyKeyValueDetails> targetAttributes) {
        this.controllerId = controllerId;
        this.isRequestAttributes = isRequestAttributes;
        this.targetAttributes = targetAttributes;
    }

    public String getControllerId() {
        return controllerId;
    }

    public boolean isRequestAttributes() {
        return isRequestAttributes;
    }

    public List<ProxyKeyValueDetails> getTargetAttributes() {
        return targetAttributes;
    }
}
