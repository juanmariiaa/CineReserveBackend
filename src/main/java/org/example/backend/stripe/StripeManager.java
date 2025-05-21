package org.example.backend.stripe;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.PaymentIntentCaptureParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.model.Payment;
import org.example.backend.model.Reservation;
import org.example.backend.model.User;
import org.example.backend.model.enums.PaymentMethod;
import org.example.backend.model.enums.PaymentStatus;
import org.example.backend.model.enums.ReservationStatus;
import org.example.backend.stripe.exceptions.StripeAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class StripeManager {

    @Value("${stripe.api.key}")
    private String apiKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${app.domain}")
    private String appDomain;

    /**
     * Configures the API Key for Stripe.
     */
    public void configureCredentials() {
        Stripe.apiKey = apiKey;
    }

    /**
     * Check that the received data is genuine
     *
     * @param payload   the event payload (usually the request body)
     * @param sigHeader the security header (usually the `Stripe-Signature` header)
     * @return a event from Stripe with the data
     * @throws StripeAPIException if the signature verification fails
     */
    public Event validateEvent(String payload, String sigHeader) throws StripeAPIException {
        try {
            return Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new StripeAPIException(e.getMessage(), "API.Payment.StripeBadSignature");
        } catch (Exception e) {
            throw new StripeAPIException("Invalid payload", "API.Payment.StripeInvalidPayload");
        }
    }

    /**
     * Create a Stripe checkout session
     *
     * @param productId        The id of the product
     * @param reservation      The reservation to create the session for
     * @param stripeCustomerId The id of the Stripe customer
     * @param successUrlDomain The domain of the success URL
     * @return A map containing the id and the url of the session
     * @throws StripeAPIException if the checkout session creation fails
     */
    public Map<String, String> generateCheckoutSession(
            String productId,
            Reservation reservation,
            String stripeCustomerId,
            String successUrlDomain) throws StripeAPIException {
        try {
            // Get the success URL
            String successUrl;

            // If a specific domain is provided, use it
            if (successUrlDomain != null && !successUrlDomain.isEmpty()) {
                // Ensure the domain has a protocol
                if (!successUrlDomain.startsWith("http")) {
                    successUrlDomain = "http://" + successUrlDomain;
                }

                successUrl = successUrlDomain + "/confirm-reservation?reservation_id=" + reservation.getId();
            } else {
                // Use the default domain from configuration
                successUrl = appDomain + "/confirm-reservation?reservation_id=" + reservation.getId();
            }

            // Calculate the total price of the reservation
            BigDecimal totalPrice = BigDecimal.ZERO;
            for (var seatReservation : reservation.getSeatReservations()) {
                // Get the price from the seat
                totalPrice = totalPrice.add(seatReservation.getSeat().getPrice());
            }

            // Convert the price to cents and create the line items with the inline price
            long priceInCents = totalPrice.multiply(BigDecimal.valueOf(100)).longValue();

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("EUR")
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName("Cinema Reservation")
                                                    .build())
                                    .setUnitAmount(priceInCents)
                                    .build())
                    .setQuantity(1L)
                    .build();

            // Store some info as metadata for later use
            Map<String, String> metadata = new HashMap<>();
            metadata.put("reservation_id", reservation.getId().toString());
            metadata.put("user_id", reservation.getUser().getId().toString());

            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(successUrl) // Also set cancel URL to avoid URL validation issues
                    .addLineItem(lineItem)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .putAllMetadata(metadata);

            // Set automatic payment capture
            paramsBuilder.setPaymentIntentData(
                    SessionCreateParams.PaymentIntentData.builder()
                            .setCaptureMethod(SessionCreateParams.PaymentIntentData.CaptureMethod.AUTOMATIC)
                            .build());

            Session session = Session.create(paramsBuilder.build());

            Map<String, String> result = new HashMap<>();
            result.put("id", session.getId());
            result.put("url", session.getUrl());

            return result;
        } catch (StripeException e) {
            log.error("Error creating checkout session", e);
            throw new StripeAPIException("Failed to create checkout session", "API.Payment.StripeCheckoutError");
        }
    }

    /**
     * Create a Stripe product
     *
     * @param name        The name of the product
     * @param description The description of the product
     * @param price       The price of the product
     * @return The created product
     * @throws StripeAPIException if the product creation fails
     */
    public Product createProduct(String name, String description, BigDecimal price) throws StripeAPIException {
        try {
            ProductCreateParams params = ProductCreateParams.builder()
                    .setName(name)
                    .setDescription(description)
                    .setDefaultPriceData(
                            ProductCreateParams.DefaultPriceData.builder()
                                    .setCurrency("EUR")
                                    .setUnitAmountDecimal(price.multiply(BigDecimal.valueOf(100)))
                                    .build())
                    .build();

            return Product.create(params);
        } catch (StripeException e) {
            log.error("Error creating product", e);
            throw new StripeAPIException("Failed to create product", "API.Payment.StripeProductError");
        }
    }

    /**
     * Get or create a Stripe customer
     *
     * @param email The email of the customer
     * @return The customer
     * @throws StripeAPIException if the customer creation fails
     */
    public Customer getOrCreateCustomer(String email) throws StripeAPIException {
        try {
            CustomerListParams params = CustomerListParams.builder()
                    .setEmail(email)
                    .build();

            CustomerCollection customers = Customer.list(params);

            if (!customers.getData().isEmpty()) {
                return customers.getData().get(0);
            } else {
                CustomerCreateParams createParams = CustomerCreateParams.builder()
                        .setEmail(email)
                        .build();

                return Customer.create(createParams);
            }
        } catch (StripeException e) {
            log.error("Error getting or creating customer", e);
            throw new StripeAPIException("Failed to get or create customer", "API.Payment.StripeCustomerError");
        }
    }

    /**
     * Get a Stripe product
     *
     * @param productId The id of the product
     * @return The product
     * @throws StripeAPIException if the product retrieval fails
     */
    public Product getProduct(String productId) throws StripeAPIException {
        try {
            return Product.retrieve(productId);
        } catch (StripeException e) {
            log.error("Error retrieving product", e);
            throw new StripeAPIException("Failed to retrieve product", "API.Payment.StripeProductError");
        }
    }

    /**
     * Cancel a payment intent
     *
     * @param payment The payment to cancel
     * @return The payment intent
     * @throws StripeAPIException if the cancellation fails
     */
    public Map<String, String> cancelPaymentIntent(Payment payment) throws StripeAPIException {
        try {
            PaymentIntentCancelParams params = PaymentIntentCancelParams.builder().build();

            PaymentIntent intent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
            PaymentIntent cancelledIntent = intent.cancel(params);

            Map<String, String> result = new HashMap<>();
            result.put("id", cancelledIntent.getId());
            result.put("status", cancelledIntent.getStatus());

            return result;
        } catch (StripeException e) {
            log.error("Error cancelling payment intent", e);
            throw new StripeAPIException("Failed to cancel payment intent", "API.Payment.StripeCancelError");
        }
    }

    /**
     * Create a customer
     *
     * @param user The user to create a customer for
     * @return The customer id
     * @throws StripeAPIException if the customer creation fails
     */
    public String createCustomer(User user) throws StripeAPIException {
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(user.getEmail())
                    .setName(user.getFirstName() + " " + user.getLastName())
                    .build();

            Customer customer = Customer.create(params);
            return customer.getId();
        } catch (StripeException e) {
            log.error("Error creating customer", e);
            throw new StripeAPIException("Failed to create customer", "API.Payment.StripeCustomerError");
        }
    }
}