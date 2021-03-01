/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.autoconfigure.dmf.amqp;

import org.eclipse.hawkbit.amqp.DmfApiConfiguration;
import org.eclipse.hawkbit.amqp.InvalidTargetOperationsExceptionHandler;
import org.eclipse.hawkbit.amqp.MessageConversionExceptionHandler;
import org.eclipse.hawkbit.exception.ConditionalErrorHandler;
import org.eclipse.hawkbit.exception.DelegatingConditionalErrorHandler;

import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.util.ErrorHandler;

import java.util.List;

/**
 * The AMQP 0.9 based device Management Federation API (DMF) auto configuration.
 */
@Configuration
@ConditionalOnClass(DmfApiConfiguration.class)
@Import(DmfApiConfiguration.class)
public class DmfApiAutoConfiguration {

    /**
     * Create default error handler bean.
     *
     *  @param handlers
     *                  list of conditional error handlers

     * @return the delegating error handler bean
     */
    @Bean
    @Primary
    public ErrorHandler errorHandler(final List<ConditionalErrorHandler> handlers) {
        return new DelegatingConditionalErrorHandler(handlers, new ConditionalRejectingErrorHandler());
    }

    /**
     * Error handler bean for all target related fatal errors
     *
     * @return the invalid target operations exception handler bean
     */
    @Bean
    public ConditionalErrorHandler invalidTargetOperationsConditionalExceptionHandler() {
        return new InvalidTargetOperationsExceptionHandler();
    }

    /**
     * Error handler bean for amqp message conversion errors
     *
     * @return the amqp message conversion exception handler bean
     */
    @Bean
    public ConditionalErrorHandler messageConversionExceptionHandler() {
        return new MessageConversionExceptionHandler();
    }
}
