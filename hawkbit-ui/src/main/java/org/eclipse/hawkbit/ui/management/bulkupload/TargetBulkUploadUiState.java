/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.bulkupload;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TargetBulkUploadUiState implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean isInProgress;

    private Long dsId;
    private final Map<Long, String> tagIdsWithNameToAssign = new HashMap<>();
    private String description;

    public boolean isInProgress() {
        return isInProgress;
    }

    public void setInProgress(final boolean isInProgress) {
        this.isInProgress = isInProgress;
    }

    public Long getDsId() {
        return dsId;
    }

    public void setDsId(final Long dsId) {
        this.dsId = dsId;
    }

    public Map<Long, String> getTagIdsWithNameToAssign() {
        return tagIdsWithNameToAssign;
    }

    public void setTagIdsWithNameToAssign(final Map<Long, String> tagIdsWithNameToAssign) {
        tagIdsWithNameToAssign.clear();
        tagIdsWithNameToAssign.putAll(tagIdsWithNameToAssign);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
