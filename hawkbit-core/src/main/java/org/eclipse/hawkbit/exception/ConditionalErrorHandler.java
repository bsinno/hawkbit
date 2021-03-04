/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.exception;

/**
 * Interface declaration of {@link ConditionalErrorHandler} that handles errors based on the
 * types of {@link ConditionalErrorHandler}
 */
public interface ConditionalErrorHandler<T> {

    /**
     * Handles the error based on the type of {@link ConditionalErrorHandler}
     *
     * @param error
     *            the error
     * @param chain
     *            an {@link ErrorHandlerChain}
     */
void doHandle(final T error, final ErrorHandlerChain<T> chain);

}
