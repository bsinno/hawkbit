/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.exception;

import org.springframework.util.ErrorHandler;

/**
 * Interface declaration of {@link ConditionalErrorHandler} that validates if a typed {@link ConditionalErrorHandler}
 * can handle the incoming error or not.
 */
public interface ConditionalErrorHandler extends ErrorHandler {

    /**
     * Determines if the typed {@link ConditionalErrorHandler} can handle the specific error.
     *
     * @param e
     *            the throwable
     * @return true if the error can be handled, otherwise false
     */
    boolean canHandle(final Throwable e);
}
