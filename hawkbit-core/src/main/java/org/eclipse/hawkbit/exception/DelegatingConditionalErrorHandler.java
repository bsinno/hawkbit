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

import java.util.Iterator;
import java.util.List;

/**
 * An error handler which performs delegation based on the type of {@link ConditionalErrorHandler}
 */
public class DelegatingConditionalErrorHandler implements ErrorHandler {
    private final List<ConditionalErrorHandler> handlers;
    private final ErrorHandler defaultHandler;

    /**
     * Constructor
     *
     * @param handlers
     *                 {@link List} of error handlers
     * @param defaultHandler
     *                  the default error handler
     */
    public DelegatingConditionalErrorHandler(final List<ConditionalErrorHandler> handlers, final ErrorHandler defaultHandler) {
        this.handlers = handlers;
        this.defaultHandler = defaultHandler;
    }

    @Override
    public void handleError(final Throwable t) {
        ConditionalErrorHandlerChain.getHandler(handlers, defaultHandler).handle(t);
    }
}
