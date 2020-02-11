/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TargetBulkUploadUiState implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long dsId;

    private String description;

    private Map<Long, String> tagIdsWithNameToAssign;

    private int sucessfulUploadCount;

    private int failedUploadCount;

    private float progressBarCurrentValue;

    private final List<String> assignedTagNames = new ArrayList<>();

    private final List<String> targetsCreated = new ArrayList<>();

    public List<String> getTargetsCreated() {
        return targetsCreated;
    }

    public List<String> getAssignedTagNames() {
        return assignedTagNames;
    }

    public float getProgressBarCurrentValue() {
        return progressBarCurrentValue;
    }

    public void setProgressBarCurrentValue(final float progressBarCurrentValue) {
        this.progressBarCurrentValue = progressBarCurrentValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public int getSucessfulUploadCount() {
        return sucessfulUploadCount;
    }

    public void setSucessfulUploadCount(final int sucessfulUploadCount) {
        this.sucessfulUploadCount = sucessfulUploadCount;
    }

    public int getFailedUploadCount() {
        return failedUploadCount;
    }

    public void setFailedUploadCount(final int failedUploadCount) {
        this.failedUploadCount = failedUploadCount;
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

    public void setTagIdsWithNameToAssign(Map<Long, String> tagIdsWithNameToAssign) {
        this.tagIdsWithNameToAssign = tagIdsWithNameToAssign;
    }
}
