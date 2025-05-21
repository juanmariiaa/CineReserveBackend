package org.example.backend.controller;

import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.dto.CheckoutRequestDTO;
import org.example.backend.dto.PaymentResponseDTO;
import org.example.backend.service.PaymentService;
import org.example.backend.stripe.StripeEventManager;
import org.example.backend.stripe.definitions.StripeAction;
import org.example.backend.stripe.exceptions.StripeAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@Tag(name = "Payments", description = "Payment processing APIs with Stripe integration")
public class PaymentController {

    private final PaymentService paymentService;
    private final StripeEventManager stripeEventManager;

    @Autowired
    public PaymentController(PaymentService paymentService, StripeEventManager stripeEventManager) {
        this.paymentService = paymentService;
        this.stripeEventManager = stripeEventManager;

        // Initialize Stripe
        paymentService.initializeStripe();
    }

    /**
     * Create a checkout session for a reservation
     *
     * @param request The checkout request
     * @return The checkout session URL
     */
    @PostMapping("/checkout")
    @Operation(summary = "Create a Stripe checkout session", description = "Creates a reservation and checkout session, then returns the checkout URL", responses = {
            @ApiResponse(responseCode = "200", description = "Checkout session created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponseDTO.class), examples = {
                    @ExampleObject(name = "Success", value = "{\"url\": \"https://checkout.stripe.com/c/pay/cs_test_...\", \"error\": null}")
            })),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponseDTO.class), examples = {
                    @ExampleObject(name = "Error", value = "{\"url\": null, \"error\": \"Screening not found\"}")
            }))
    })
    public ResponseEntity<PaymentResponseDTO> createCheckoutSession(
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Checkout request details", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CheckoutRequestDTO.class), examples = {
                    @ExampleObject(name = "Checkout Request", value = "{\"screeningId\": 1, \"seatIds\": [1, 2, 3], \"successUrlDomain\": \"localhost:3000\"}")
            })) CheckoutRequestDTO request) {
        try {
            String checkoutUrl = paymentService.createCheckoutSessionWithReservation(
                    request.getReservationCreateDTO(),
                    request.getSuccessUrlDomain());

            PaymentResponseDTO response = new PaymentResponseDTO();
            response.setUrl(checkoutUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating checkout session", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PaymentResponseDTO(null, e.getMessage()));
        }
    }

    /**
     * Stripe webhook endpoint
     *
     * @param request The HTTP request
     * @return The response
     */
    @PostMapping("/webhook")
    @Operation(summary = "Handle Stripe webhook events", description = "Processes webhook events from Stripe (checkout completion, payment success, etc.)", responses = {
            @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid webhook payload or signature"),
            @ApiResponse(responseCode = "500", description = "Server error processing webhook")
    })
    public ResponseEntity<String> handleWebhook(HttpServletRequest request) {
        try {
            // Get the request body
            String payload = request.getReader().lines().collect(Collectors.joining());

            // Get the signature header
            String sigHeader = request.getHeader("Stripe-Signature");

            // Initialize the event manager
            stripeEventManager.initialize(payload, sigHeader);

            // Get the action to take
            StripeAction action = stripeEventManager.getAction();

            // Process the event based on the action
            switch (action) {
                case PROCESS_CHECKOUT:
                    Session session = (Session) stripeEventManager.getObject();
                    paymentService.processCheckoutSessionCompleted(session);
                    break;
                case PROCESS_PAYMENT_SUCCESS:
                    StripeObject paymentIntent = stripeEventManager.getObject();
                    JSONObject paymentIntentJson = new JSONObject(paymentIntent.toJson());
                    String successfulPaymentIntentId = paymentIntentJson.getString("id");
                    paymentService.processPaymentIntentSuccess(successfulPaymentIntentId);
                    break;
                case PROCESS_CANCEL:
                    StripeObject cancelledIntent = stripeEventManager.getObject();
                    JSONObject cancelledIntentJson = new JSONObject(cancelledIntent.toJson());
                    String cancelledPaymentIntentId = cancelledIntentJson.getString("id");
                    paymentService.processPaymentIntentCancellation(cancelledPaymentIntentId);
                    break;
                case NOOP:
                    // No operation needed
                    break;
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (StripeAPIException e) {
            log.error("Stripe API error", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            log.error("Error reading request body", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error reading request body");
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }

    /**
     * Cancel a payment intent
     *
     * @param reservationId The id of the reservation
     * @return The cancellation result
     */
    @PostMapping("/cancel/{reservationId}")
    @Operation(summary = "Cancel a payment intent", description = "Cancels a payment intent for a specific reservation", responses = {
            @ApiResponse(responseCode = "200", description = "Payment intent cancelled successfully", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "Success", value = "{\"id\": \"pi_1234\", \"status\": \"canceled\"}")
            })),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "Error", value = "{\"error\": \"Payment is not pending\"}")
            }))
    })
    public ResponseEntity<Map<String, String>> cancelPaymentIntent(
            @Parameter(description = "ID of the reservation to cancel payment for", required = true) @PathVariable Long reservationId) {
        try {
            Map<String, String> cancelResult = paymentService.cancelPaymentIntent(reservationId);
            return ResponseEntity.ok(cancelResult);
        } catch (Exception e) {
            log.error("Error cancelling payment intent", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}