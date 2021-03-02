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
     *                  the defult error handler
     */
    public DelegatingConditionalErrorHandler(final List<ConditionalErrorHandler> handlers, final ErrorHandler defaultHandler) {
        this.handlers = handlers;
        this.defaultHandler = defaultHandler;
    }

    @Override
    public void handleError(final Throwable t) {
        ErrorHandlerChain.getHandler(handlers, defaultHandler).doHandle(t);
    }

    /**
     * An error handler chain which processes a {@link List} of error handlers based on the type of {@link ConditionalErrorHandler}
     */
    static class ErrorHandlerChain implements EventHandlerChain<Throwable> {
        private final Iterator<ConditionalErrorHandler> iterator;
        private final ErrorHandler defaultHandler;

        ErrorHandlerChain(Iterator<ConditionalErrorHandler> iterator, ErrorHandler defaultHandler) {
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
        public static ErrorHandlerChain getHandler(final List<ConditionalErrorHandler> errorHandlers, ErrorHandler defaultHandler) {
            return new ErrorHandlerChain(errorHandlers.iterator(), defaultHandler);
        }

        @Override
        public void doHandle(Throwable event) {
            if (iterator.hasNext()) {
                final ConditionalErrorHandler handler = iterator.next();
                handler.handle(event, this);
            } else {
                defaultHandler.handleError(event);
            }
        }
    }
}
