package org.example.backend.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.config.StripeConfig;
import org.example.backend.service.PaymentService;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final PaymentService paymentService;
    private final StripeConfig stripeConfig;

    @PostMapping("/webhooks/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        log.info("Received Stripe webhook");
        
        Event event;
        
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.error("Invalid signature: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            log.error("Error parsing webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook parsing error");
        }

        // Parse the raw JSON payload directly
        JSONObject jsonPayload = new JSONObject(payload);
        JSONObject eventData = jsonPayload.getJSONObject("data");
        String eventType = event.getType();
        
        try {
            switch (eventType) {
                case "checkout.session.completed":
                    log.info("Checkout session completed event received");
                    JSONObject session = eventData.getJSONObject("object");
                    String sessionId = session.getString("id");
                    log.info("Checkout session completed: {}", sessionId);
                    
                    // Update payment with payment intent ID for future reference
                    if (session.has("payment_intent") && !session.isNull("payment_intent")) {
                        String paymentIntentId = session.getString("payment_intent");
                        paymentService.updatePaymentIntentId(sessionId, paymentIntentId);
                    }
                    
                    paymentService.handleSessionCompleted(sessionId);
                    break;
                    
                case "payment_intent.succeeded":
                    log.info("Payment intent succeeded event received");
                    JSONObject paymentIntent = eventData.getJSONObject("object");
                    String paymentIntentId = paymentIntent.getString("id");
                    log.info("Payment succeeded: {}", paymentIntentId);
                    paymentService.handlePaymentSucceeded(paymentIntentId);
                    break;
                    
                case "payment_intent.payment_failed":
                    log.info("Payment intent failed event received");
                    JSONObject failedPaymentIntent = eventData.getJSONObject("object");
                    String failedPaymentIntentId = failedPaymentIntent.getString("id");
                    log.info("Payment failed: {}", failedPaymentIntentId);
                    paymentService.handlePaymentFailed(failedPaymentIntentId);
                    break;
                    
                default:
                    log.info("Unhandled event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error processing webhook: " + e.getMessage());
        }

        return ResponseEntity.ok("Webhook processed successfully");
    }
}
