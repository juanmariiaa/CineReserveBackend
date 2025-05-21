package org.example.backend.stripe.definitions;

public enum StripeEvent {
    /**
     * Stripe Event Types
     */
    CHECKOUT_SESSION_COMPLETED("checkout.session.completed"),
    CHECKOUT_SESSION_EXPIRED("checkout.session.expired"),
    PAYMENT_INTENT_SUCCEEDED("payment_intent.succeeded"),
    PAYMENT_INTENT_CANCELLED("payment_intent.canceled");

    private final String value;

    StripeEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}