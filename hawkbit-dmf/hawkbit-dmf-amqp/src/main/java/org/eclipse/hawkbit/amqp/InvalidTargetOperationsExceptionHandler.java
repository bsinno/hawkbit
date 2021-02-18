package org.eclipse.hawkbit.amqp;

import org.eclipse.hawkbit.exception.ConditionalErrorHandler;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.InvalidTargetAttributeException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;

public class InvalidTargetOperationsExceptionHandler extends ConditionalRejectingErrorHandler implements ConditionalErrorHandler {
    @Override
    public boolean canHandle(Throwable e) {
        return e.getCause() instanceof InvalidTargetAttributeException || e.getCause() instanceof EntityNotFoundException;
    }

    @Override public void handleError(Throwable t) {
        throw new AmqpRejectAndDontRequeueException(t.getCause().getMessage());
    }
}
