package org.example.backend.service;

import com.stripe.model.checkout.Session;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.dto.ReservationCreateDTO;
import org.example.backend.model.Payment;
import org.example.backend.model.Reservation;
import org.example.backend.model.User;
import org.example.backend.model.enums.PaymentMethod;
import org.example.backend.model.enums.PaymentStatus;
import org.example.backend.model.enums.ReservationStatus;
import org.example.backend.repository.PaymentRepository;
import org.example.backend.repository.ReservationRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.stripe.StripeManager;
import org.example.backend.stripe.exceptions.StripeAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class PaymentService {

    @Value("${stripe.product.id}")
    private String productId;

    private final StripeManager stripeManager;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ReservationService reservationService;

    @Autowired
    public PaymentService(
            StripeManager stripeManager,
            PaymentRepository paymentRepository,
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            ReservationService reservationService) {
        this.stripeManager = stripeManager;
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.reservationService = reservationService;
    }

    /**
     * Initialize Stripe configuration
     */
    public void initializeStripe() {
        stripeManager.configureCredentials();
    }

    /**
     * Create a checkout session for a new reservation
     *
     * @param reservationCreateDTO The reservation creation data
     * @param successUrlDomain     The domain of the success URL
     * @return The checkout session URL
     * @throws Exception if the checkout session creation fails
     */
    @Transactional
    public String createCheckoutSessionWithReservation(ReservationCreateDTO reservationCreateDTO,
            String successUrlDomain)
            throws Exception {
        // First create the reservation
        Reservation reservation = reservationService.createReservation(reservationCreateDTO);

        // Get the user
        User user = reservation.getUser();

        // Calculate the total price of the reservation
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (var seatReservation : reservation.getSeatReservations()) {
            totalPrice = totalPrice.add(seatReservation.getSeat().getPrice());
        }

        // Get or create the Stripe customer
        String stripeCustomerId = user.getStripeCustomerId();
        if (stripeCustomerId == null || stripeCustomerId.isEmpty()) {
            stripeCustomerId = stripeManager.createCustomer(user);
            user.setStripeCustomerId(stripeCustomerId);
            userRepository.save(user);
        }

        // Create the checkout session
        Map<String, String> sessionData = stripeManager.generateCheckoutSession(
                productId,
                reservation,
                stripeCustomerId,
                successUrlDomain);

        // Create and save the payment
        Payment payment = new Payment();
        payment.setAmount(totalPrice);
        payment.setPaymentMethod(PaymentMethod.CARD); // Default to card payment
        payment.setStatus(PaymentStatus.PENDING);
        payment.setStripeSessionId(sessionData.get("id"));
        payment.setStripeCustomerId(stripeCustomerId);
        payment.setReservation(reservation);
        paymentRepository.save(payment);

        // The reservation status is already set to PENDING by ReservationService

        return sessionData.get("url");
    }

    /**
     * Process a checkout session completion
     *
     * @param session The checkout session
     * @throws Exception if the checkout session processing fails
     */
    @Transactional
    public void processCheckoutSessionCompleted(Session session) throws Exception {
        // Find the payment by session ID
        Payment payment = paymentRepository.findByStripeSessionId(session.getId())
                .orElseThrow(() -> new Exception("Payment not found for session: " + session.getId()));

        // Update the payment with the payment intent ID
        payment.setStripePaymentIntentId(session.getPaymentIntent());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update the reservation status
        Reservation reservation = payment.getReservation();
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);
    }

    /**
     * Process a payment intent success
     *
     * @param paymentIntentId The payment intent ID
     * @throws Exception if the payment intent processing fails
     */
    @Transactional
    public void processPaymentIntentSuccess(String paymentIntentId) throws Exception {
        // Find the payment by payment intent ID
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new Exception("Payment not found for payment intent: " + paymentIntentId));

        // Update the payment status if it's not already completed
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);

            // Update the reservation status
            Reservation reservation = payment.getReservation();
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);

            log.info("Payment {} for reservation {} successfully processed",
                    paymentIntentId, reservation.getId());
        }
    }

    /**
     * Process a payment intent cancellation
     *
     * @param paymentIntentId The payment intent ID
     * @throws Exception if the payment intent cancellation fails
     */
    @Transactional
    public void processPaymentIntentCancellation(String paymentIntentId) throws Exception {
        // Find the payment by payment intent ID
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new Exception("Payment not found for payment intent: " + paymentIntentId));

        // Update the payment status
        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        // Update the reservation status
        Reservation reservation = payment.getReservation();
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    /**
     * Cancel a payment intent
     *
     * @param reservationId The id of the reservation
     * @return The cancellation result
     * @throws Exception if the payment intent cancellation fails
     */
    @Transactional
    public Map<String, String> cancelPaymentIntent(Long reservationId) throws Exception {
        // Find the payment by reservation ID
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new Exception("Payment not found for reservation: " + reservationId));

        // Check if the payment is pending
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new Exception("Payment is not pending");
        }

        // Cancel the payment intent
        Map<String, String> cancelResult = stripeManager.cancelPaymentIntent(payment);

        // Update the payment status
        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        // Update the reservation status
        Reservation reservation = payment.getReservation();
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        return cancelResult;
    }
}