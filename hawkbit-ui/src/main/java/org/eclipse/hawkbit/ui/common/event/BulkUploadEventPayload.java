/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public final class BulkUploadEventPayload {
    private final BulkUploadState bulkUploadState;
    private final float bulkUploadProgress;
    private final int successBulkUploadCount;
    private final int failBulkUploadCount;
    private final String failureReason;

    private BulkUploadEventPayload(final BulkUploadState bulkUploadState) {
        this(bulkUploadState, "");
    }

    private BulkUploadEventPayload(final BulkUploadState bulkUploadState, final String failureReason) {
        this(bulkUploadState, 0, 0, 0, failureReason);
    }

    private BulkUploadEventPayload(final BulkUploadState bulkUploadState, final float bulkUploadProgress) {
        this(bulkUploadState, bulkUploadProgress, 0, 0);
    }

    private BulkUploadEventPayload(final BulkUploadState bulkUploadState, final float bulkUploadProgress,
            final int successBulkUploadCount, final int failBulkUploadCount) {
        this(bulkUploadState, bulkUploadProgress, successBulkUploadCount, failBulkUploadCount, "");
    }

    private BulkUploadEventPayload(final BulkUploadState bulkUploadState, final float bulkUploadProgress,
            final int successBulkUploadCount, final int failBulkUploadCount, final String failureReason) {
        this.bulkUploadState = bulkUploadState;
        this.bulkUploadProgress = bulkUploadProgress;
        this.successBulkUploadCount = successBulkUploadCount;
        this.failBulkUploadCount = failBulkUploadCount;
        this.failureReason = failureReason;
    }

    public static BulkUploadEventPayload buildUploadStarted() {
        return new BulkUploadEventPayload(BulkUploadState.UPLOAD_STARTED);
    }

    public static BulkUploadEventPayload buildUploadFailed(final String failureReason) {
        return new BulkUploadEventPayload(BulkUploadState.UPLOAD_FAILED, failureReason);
    }

    public static BulkUploadEventPayload buildTargetProvisioningStarted() {
        return new BulkUploadEventPayload(BulkUploadState.TARGET_PROVISIONING_STARTED);
    }

    public static BulkUploadEventPayload buildTargetProvisioningProgressUpdated(final float progress) {
        return new BulkUploadEventPayload(BulkUploadState.TARGET_PROVISIONING_PROGRESS_UPDATED, progress);
    }

    public static BulkUploadEventPayload buildTagsAndDsAssignmentStarted() {
        return new BulkUploadEventPayload(BulkUploadState.TAGS_AND_DS_ASSIGNMENT_STARTED);
    }

    public static BulkUploadEventPayload buildTagsAndDsAssignmentFailed(final String failureReason) {
        return new BulkUploadEventPayload(BulkUploadState.TAGS_AND_DS_ASSIGNMENT_FAILED, failureReason);
    }

    public static BulkUploadEventPayload buildBulkUploadCompleted(final int successBulkUploadCount,
            final int failBulkUploadCount) {
        return new BulkUploadEventPayload(BulkUploadState.BULK_UPLOAD_COMPLETED, 1, successBulkUploadCount,
                failBulkUploadCount);
    }

    public BulkUploadState getBulkUploadState() {
        return bulkUploadState;
    }

    public float getBulkUploadProgress() {
        return bulkUploadProgress;
    }

    public int getSuccessBulkUploadCount() {
        return successBulkUploadCount;
    }

    public int getFailBulkUploadCount() {
        return failBulkUploadCount;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public enum BulkUploadState {
        UPLOAD_STARTED, UPLOAD_FAILED, TARGET_PROVISIONING_STARTED, TARGET_PROVISIONING_PROGRESS_UPDATED, TAGS_AND_DS_ASSIGNMENT_STARTED, TAGS_AND_DS_ASSIGNMENT_FAILED, BULK_UPLOAD_COMPLETED;
    }
}
