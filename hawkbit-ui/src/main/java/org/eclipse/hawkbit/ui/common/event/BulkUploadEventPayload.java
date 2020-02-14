/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class BulkUploadEventPayload {
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

    public static BulkUploadEventPayload buildStarted() {
        return new BulkUploadEventPayload(BulkUploadState.STARTED);
    }

    public static BulkUploadEventPayload buildProgressUpdated(final float progress) {
        return new BulkUploadEventPayload(BulkUploadState.PROGRESS_UPDATED, progress);
    }

    public static BulkUploadEventPayload buildCompleted(final int successBulkUploadCount,
            final int failBulkUploadCount) {
        return new BulkUploadEventPayload(BulkUploadState.COMPLETED, 1, successBulkUploadCount, failBulkUploadCount);
    }

    public static BulkUploadEventPayload buildFailed(final String failureReason) {
        return new BulkUploadEventPayload(BulkUploadState.FAILED, failureReason);
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
        STARTED, PROGRESS_UPDATED, COMPLETED, FAILED;
    }
}
