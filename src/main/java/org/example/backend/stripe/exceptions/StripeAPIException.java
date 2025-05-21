package org.example.backend.stripe.exceptions;

public class StripeAPIException extends Exception {
    /**
     * Error from Stripe API
     */
    private final String errorCode;

    public StripeAPIException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}