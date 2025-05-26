package org.example.backend.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class StripeConfig {

    @Value("${stripe.api.key}")
    private String apiKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${stripe.product.id}")
    private String stripeProductId;
    
    @Value("${stripe.price.id}")
    private String stripePriceId;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }
}
