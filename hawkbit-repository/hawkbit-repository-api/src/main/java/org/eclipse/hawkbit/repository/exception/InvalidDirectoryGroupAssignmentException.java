package org.eclipse.hawkbit.repository.exception;

import org.eclipse.hawkbit.exception.AbstractServerRtException;
import org.eclipse.hawkbit.exception.SpServerError;

/**
 * the {@link InvalidDirectoryGroupAssignmentException} is thrown when a group assignment is tried to
 * be created which is not allowed, e.g. causing a loop.
 */
public class InvalidDirectoryGroupAssignmentException extends AbstractServerRtException {

    private static final SpServerError THIS_ERROR = SpServerError.SP_DIRECTORY_GROUP_FALSE_ASSIGNMENT;

    public InvalidDirectoryGroupAssignmentException() {
        super(THIS_ERROR);
    }

    public InvalidDirectoryGroupAssignmentException(final String message) {
        super(message, THIS_ERROR);
    }

    public InvalidDirectoryGroupAssignmentException(final Long childId, final Long parentId, final String reason) {
        this("Assigning parent group " + parentId + " to " + childId + " is not allowed. " + reason);
    }

}
