/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.amqp;

import org.springframework.util.ErrorHandler;

import java.util.Iterator;
import java.util.List;

/**
 * An error handler chain which processes a {@link List} of error handlers based on the type of {@link AmqpErrorHandler}
 */
public class AmqpErrorHandlerChain {
    private final Iterator<AmqpErrorHandler> iterator;
    private final ErrorHandler defaultHandler;

    AmqpErrorHandlerChain(Iterator<AmqpErrorHandler> iterator, ErrorHandler defaultHandler) {
        this.iterator = iterator;
        this.defaultHandler = defaultHandler;
    }

    /**
     * Returns an {@link AmqpErrorHandlerChain}
     *
     * @param errorHandlers
     *                      {@link List} of error handlers
     * @param defaultHandler
     *                      the default error handler
     * @return an {@link AmqpErrorHandlerChain}
     */
    public static AmqpErrorHandlerChain getHandler(final List<AmqpErrorHandler> errorHandlers, final ErrorHandler defaultHandler) {
        return new AmqpErrorHandlerChain(errorHandlers.iterator(), defaultHandler);
    }

    public void handle(final Throwable error) {
        // ListenerExecutionFailedException is always the parent exception
        // which contains the required details of the error
        final Throwable rootError = error.getCause();

        if (rootError == null) {
            throw new IllegalStateException("Throwable must contain a cause");
        }

        if (iterator.hasNext()) {
            final AmqpErrorHandler handler = iterator.next();
            handler.doHandle(rootError, this);
        } else {
            defaultHandler.handleError(error);
        }
    }
}

