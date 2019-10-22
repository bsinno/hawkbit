/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import java.util.Set;

import org.eclipse.hawkbit.repository.model.Type;

/**
 * Proxy for {@link Type}.
 */
public class ProxyType extends ProxyFilterButton {

    private static final long serialVersionUID = 1L;

    private String key;

    private boolean deleted;

    private boolean mandatory;

    private SmTypeAssign smTypeAssign;

    private Set<ProxyType> selectedSmTypes;

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(final boolean mandatory) {
        this.mandatory = mandatory;
    }

    public SmTypeAssign getSmTypeAssign() {
        return smTypeAssign;
    }

    public void setSmTypeAssign(final SmTypeAssign smTypeAssign) {
        this.smTypeAssign = smTypeAssign;
    }

    public Set<ProxyType> getSelectedSmTypes() {
        return selectedSmTypes;
    }

    public void setSelectedSmTypes(final Set<ProxyType> selectedSmTypes) {
        this.selectedSmTypes = selectedSmTypes;
    }

    public enum SmTypeAssign {
        SINGLE, MULTI;
    }

}
