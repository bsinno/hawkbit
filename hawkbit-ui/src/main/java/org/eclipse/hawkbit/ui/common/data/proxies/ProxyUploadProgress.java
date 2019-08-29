/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import java.security.SecureRandom;

import org.eclipse.hawkbit.ui.artifacts.upload.FileUploadId;

/**
 * Proxy for UploadProgressGrid.
 */
public class ProxyUploadProgress extends ProxyIdentifiableEntity {
    private static final long serialVersionUID = 1L;

    private FileUploadId fileUploadId;
    private ProgressSatus status;
    private double progress;
    private String reason;

    public ProxyUploadProgress() {
        setId(new SecureRandom().nextLong());
    }

    public ProgressSatus getStatus() {
        return status;
    }

    public void setStatus(final ProgressSatus status) {
        this.status = status;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(final double progress) {
        this.progress = progress;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public FileUploadId getFileUploadId() {
        return fileUploadId;
    }

    public void setFileUploadId(final FileUploadId fileUploadId) {
        this.fileUploadId = fileUploadId;
    }

    public enum ProgressSatus {
        INPROGRESS, FINISHED, FAILED;
    }
}
