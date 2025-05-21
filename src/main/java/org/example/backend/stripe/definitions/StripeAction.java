package org.example.backend.stripe.definitions;

public enum StripeAction {
    /**
     * Stripe Actions
     */
    PROCESS_CHECKOUT,
    PROCESS_PAYMENT_SUCCESS,
    PROCESS_CANCEL,
    NOOP
}