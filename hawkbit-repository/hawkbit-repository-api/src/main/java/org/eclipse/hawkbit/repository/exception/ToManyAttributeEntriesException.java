/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.exception;

import org.eclipse.hawkbit.exception.AbstractServerRtException;
import org.eclipse.hawkbit.exception.SpServerError;

/**
 * Thrown if too many status entries have been inserted.
 *
 *
 *
 *
 */
public final class ToManyAttributeEntriesException extends AbstractServerRtException {
    /**
    *
    */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new FileUploadFailedException with
     * {@link SpServerError#SP_ATTRIBUTES_TO_MANY_ENTRIES} error.
     */
    public ToManyAttributeEntriesException() {
        super(SpServerError.SP_ATTRIBUTES_TO_MANY_ENTRIES);
    }

    /**
     * @param cause
     *            for the exception
     */
    public ToManyAttributeEntriesException(final Throwable cause) {
        super(SpServerError.SP_ATTRIBUTES_TO_MANY_ENTRIES, cause);
    }

    /**
     * @param message
     *            of the error
     */
    public ToManyAttributeEntriesException(final String message) {
        super(message, SpServerError.SP_ATTRIBUTES_TO_MANY_ENTRIES);
    }
}
