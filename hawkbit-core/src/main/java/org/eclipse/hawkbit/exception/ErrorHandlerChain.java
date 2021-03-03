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
 * A chain where an error is passed through all handlers until one is found that
 * can process the error.
 *
 * @param <T>
 *            generic type of event.
 */
public interface ErrorHandlerChain<T> {

    /**
     * Pass the error and search for a suitable handler
     *
     * @param error
     *            the error
     */
    void handle(final T error);
}
