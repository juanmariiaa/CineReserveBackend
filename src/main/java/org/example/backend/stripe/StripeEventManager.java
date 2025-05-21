package org.example.backend.stripe;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import org.example.backend.stripe.definitions.StripeAction;
import org.example.backend.stripe.definitions.StripeEvent;
import org.example.backend.stripe.exceptions.StripeAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class that handles the Stripe events
 *
 * Each event that happens in Stripe is sent to the webhook endpoint. For the
 * checkout
 * session, the endpoint is `/api/payment/webhook`. The event is then validated
 * and the action to take
 * is returned to the service so that the StripeManager can perform the
 * appropriate action.
 */
@Component
public class StripeEventManager {

    private final StripeManager stripeManager;
    private Event event;
    private StripeAction action;

    @Autowired
    public StripeEventManager(StripeManager stripeManager) {
        this.stripeManager = stripeManager;
    }

    /**
     * Initialize the event manager with the event data
     *
     * @param payload   the event payload (usually the request body)
     * @param sigHeader the security header (usually the `Stripe-Signature` header)
     * @throws StripeAPIException if the event validation fails
     */
    public void initialize(String payload, String sigHeader) throws StripeAPIException {
        this.event = stripeManager.validateEvent(payload, sigHeader);
        this.action = determineActionToTake();
    }

    /**
     * Get the event object
     *
     * @return The object of the event
     */
    public StripeObject getObject() {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        return dataObjectDeserializer.getObject().orElse(null);
    }

    /**
     * Get the action to take
     *
     * @return The action to take
     */
    public StripeAction getAction() {
        return this.action;
    }

    /**
     * Get the event
     *
     * @return The event
     */
    public Event getEvent() {
        return this.event;
    }

    /**
     * Determine the action to take based on the event type
     *
     * @return The action to take
     */
    private StripeAction determineActionToTake() {
        String eventType = this.event.getType();

        if (eventType.equals(StripeEvent.CHECKOUT_SESSION_COMPLETED.getValue())) {
            return StripeAction.PROCESS_CHECKOUT;
        } else if (eventType.equals(StripeEvent.PAYMENT_INTENT_SUCCEEDED.getValue())) {
            return StripeAction.PROCESS_PAYMENT_SUCCESS;
        } else if (eventType.equals(StripeEvent.PAYMENT_INTENT_CANCELLED.getValue())) {
            return StripeAction.PROCESS_CANCEL;
        } else {
            return StripeAction.NOOP;
        }
    }
}