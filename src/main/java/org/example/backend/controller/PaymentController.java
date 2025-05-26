package org.example.backend.controller;

import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.CheckoutSessionResponseDTO;
import org.example.backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-checkout-session/{reservationId}")
    public ResponseEntity<?> createCheckoutSession(
            @PathVariable Long reservationId,
            @RequestParam(defaultValue = "http://localhost:4200/payment/success") String successUrl,
            @RequestParam(defaultValue = "http://localhost:4200/payment/cancel") String cancelUrl) {
        
        try {
            String checkoutUrl = paymentService.createCheckoutSession(reservationId, successUrl, cancelUrl);
            
            CheckoutSessionResponseDTO response = new CheckoutSessionResponseDTO();
            response.setCheckoutUrl(checkoutUrl);
            
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
