package org.example.backend.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.config.StripeConfig;
import org.example.backend.model.Payment;
import org.example.backend.model.Reservation;
import org.example.backend.model.enums.PaymentStatus;
import org.example.backend.model.enums.ReservationStatus;
import org.example.backend.repository.PaymentRepository;
import org.example.backend.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final StripeConfig stripeConfig;
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Value("${stripe.seat.price}")
    private BigDecimal seatPrice;

    public String createCheckoutSession(Long reservationId, String successUrl, String cancelUrl) throws StripeException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found with ID: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Reservation is not in PENDING state");
        }

        // Calculate total amount based on number of seats
        int seatCount = reservation.getSeatReservations().size();
        BigDecimal amount = seatPrice.multiply(BigDecimal.valueOf(seatCount));

        // Create metadata for the session
        Map<String, String> metadata = new HashMap<>();
        metadata.put("reservationId", reservation.getId().toString());

        // Create Stripe Checkout Session
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder();
        paramsBuilder.setMode(SessionCreateParams.Mode.PAYMENT);
        paramsBuilder.setSuccessUrl(successUrl);
        paramsBuilder.setCancelUrl(cancelUrl);
        
        // Add line item
        SessionCreateParams.LineItem.Builder lineItemBuilder = SessionCreateParams.LineItem.builder();
        lineItemBuilder.setPrice(stripeConfig.getStripePriceId());
        lineItemBuilder.setQuantity(Long.valueOf(seatCount));
        paramsBuilder.addLineItem(lineItemBuilder.build());
        
        // Add metadata - add each metadata entry individually
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            paramsBuilder.putMetadata(entry.getKey(), entry.getValue());
        }
        
        Session session = Session.create(paramsBuilder.build());

        // Create or update payment record
        Payment payment = paymentRepository.findByReservation(reservation)
                .orElse(new Payment());

        payment.setReservation(reservation);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setStripeCheckoutId(session.getId());
        payment.setPaymentDate(LocalDateTime.now());
        
        paymentRepository.save(payment);

        return session.getUrl();
    }

    public void handleSessionCompleted(String sessionId) {
        Payment payment = paymentRepository.findByStripeCheckoutId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for session: " + sessionId));

        payment.setStatus(PaymentStatus.SUCCEEDED);
        paymentRepository.save(payment);

        Reservation reservation = payment.getReservation();
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);
    }

    public void handlePaymentSucceeded(String paymentIntentId) {
        try {
            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElse(null);
            
            if (payment == null) {
                // Log the event but don't throw an exception
                log.warn("Payment not found for payment intent: {}. This may be a duplicate event or out-of-order event.", paymentIntentId);
                return;
            }

            payment.setStatus(PaymentStatus.SUCCEEDED);
            paymentRepository.save(payment);

            Reservation reservation = payment.getReservation();
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);
        } catch (Exception e) {
            log.error("Error processing payment success for intent {}: {}", paymentIntentId, e.getMessage());
        }
    }

    public void handlePaymentFailed(String paymentIntentId) {
        try {
            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElse(null);
                    
            if (payment == null) {
                // Log the event but don't throw an exception
                log.warn("Payment not found for payment intent: {}. This may be a duplicate event or out-of-order event.", paymentIntentId);
                return;
            }

            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            Reservation reservation = payment.getReservation();
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
        } catch (Exception e) {
            log.error("Error processing payment failure for intent {}: {}", paymentIntentId, e.getMessage());
        }
    }

    public void updatePaymentIntentId(String sessionId, String paymentIntentId) {
        Payment payment = paymentRepository.findByStripeCheckoutId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for session: " + sessionId));

        payment.setStripePaymentIntentId(paymentIntentId);
        paymentRepository.save(payment);
    }
}
