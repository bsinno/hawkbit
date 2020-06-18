package org.eclipse.hawkbit.ui.rollout.window.layouts;

import com.vaadin.data.Binder;

public abstract class ValidatableLayout {

    protected ValidationStatus validationStatus;
    protected ValidationListener validationListener;

    protected ValidatableLayout() {
        this.validationStatus = ValidationStatus.UNKNOWN;
    }

    public void setValidationListener(final ValidationListener validationListener) {
        this.validationListener = validationListener;
    }

    protected void setValidationStatusByBinder(final Binder<?> binder) {
        binder.addStatusChangeListener(
                event -> setValidationStatus(mapBinderStatusToValidationStatus(event.getBinder())));
    }

    private ValidationStatus mapBinderStatusToValidationStatus(final Binder<?> binder) {
        // will only work if the beans are set by binder read/write bean
        // (setBean will always return no changes except of invalid inputs)!
        if (!binder.hasChanges()) {
            return ValidationStatus.UNKNOWN;
        }

        return binder.isValid() ? ValidationStatus.VALID : ValidationStatus.INVALID;
    }

    protected void setValidationStatus(final ValidationStatus status) {
        if (status == validationStatus) {
            return;
        }

        validationStatus = status;

        if (validationListener != null) {
            validationListener.validationStatusChanged(status);
        }
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public boolean isValid() {
        return ValidationStatus.VALID == validationStatus;
    }

    public void resetValidationStatus() {
        validationStatus = ValidationStatus.UNKNOWN;
    }

    /**
     * Implement the interface and set the instance with setValidationListener
     * to receive updates for any validation status changes of the layout.
     */
    @FunctionalInterface
    public interface ValidationListener {
        /**
         * Is called after user input
         * 
         * @param isValid
         *            whether the input of the group rows is valid
         */
        void validationStatusChanged(ValidationStatus validationStatus);
    }

    /**
     * Status of the validation
     */
    public enum ValidationStatus {
        UNKNOWN, LOADING, VALID, INVALID
    }
}
