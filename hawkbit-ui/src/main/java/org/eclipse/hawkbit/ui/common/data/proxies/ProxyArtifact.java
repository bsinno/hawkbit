/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import org.eclipse.hawkbit.repository.model.Artifact;

/**
 * Proxy for {@link Artifact}
 */
public class ProxyArtifact extends ProxyIdentifiableEntity {
    private static final long serialVersionUID = 1L;

    private String filename;
    private String md5Hash;
    private String sha1Hash;
    private String sha256Hash;
    private long size;
    private String modifiedDate;

    public String getFilename() {
        return filename;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(final String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public String getSha1Hash() {
        return sha1Hash;
    }

    public void setSha1Hash(final String sha1Hash) {
        this.sha1Hash = sha1Hash;
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public void setSha256Hash(final String sha256Hash) {
        this.sha256Hash = sha256Hash;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(final String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

}
