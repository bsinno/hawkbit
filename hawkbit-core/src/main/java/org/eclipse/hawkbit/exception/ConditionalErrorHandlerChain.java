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
 * An error handler chain which processes a {@link List} of error handlers based on the type of {@link ConditionalErrorHandler}
 */
public class ConditionalErrorHandlerChain implements ErrorHandlerChain<Throwable> {
    private final Iterator<ConditionalErrorHandler> iterator;
    private final ErrorHandler defaultHandler;

    ConditionalErrorHandlerChain(Iterator<ConditionalErrorHandler> iterator, ErrorHandler defaultHandler) {
        this.iterator = iterator;
        this.defaultHandler = defaultHandler;
    }

    /**
     * Returns an {@link ErrorHandlerChain}
     *
     * @param errorHandlers
     *                      {@link List} of error handlers
     * @param defaultHandler
     *                      the default error handler
     * @return an {@link ErrorHandlerChain}
     */
    public static ErrorHandlerChain getHandler(final List<ConditionalErrorHandler> errorHandlers, final ErrorHandler defaultHandler) {
        return new ConditionalErrorHandlerChain(errorHandlers.iterator(), defaultHandler);
    }

    @Override
    public void handle(final Throwable error) {
        if (iterator.hasNext()) {
            final ConditionalErrorHandler handler = iterator.next();
            handler.doHandle(error, this);
        } else {
            defaultHandler.handleError(error);
        }
    }
}

