# Stripe Payment Integration for CineReserve

This document explains how to set up and use the Stripe payment integration for the CineReserve cinema seat reservation application.

## Setup

### 1. Stripe Account Setup

1. Create a Stripe account at [https://stripe.com](https://stripe.com) if you don't have one already.
2. Go to the Stripe Dashboard and navigate to the Developers section.
3. Get your API keys (both publishable and secret keys).
4. Set up a webhook endpoint that points to your application's `/api/payment/webhook` endpoint.
5. Create a product in Stripe for cinema reservations and note its ID.

### 2. Environment Variables

Add the following environment variables to your application:

```
STRIPE_API_KEY=your_stripe_secret_key
STRIPE_WEBHOOK_SECRET=your_webhook_signing_secret
STRIPE_PRODUCT_ID=your_product_id
APP_DOMAIN=your_application_domain
```

You can add these to your `.env` file or set them directly in your environment.

## Payment Flow

The payment flow for cinema seat reservations is as follows:

1. User selects seats for a movie screening.
2. User proceeds to checkout.
3. The application creates a Stripe checkout session and redirects the user to the Stripe payment page.
4. User completes the payment on the Stripe page.
5. Stripe sends a webhook event to the application's webhook endpoint.
6. The application processes the webhook event and updates the reservation status accordingly.
7. User is redirected back to the application's confirmation page.

## API Endpoints

### Create Checkout Session

```
POST /api/payment/checkout
```

Request body:

```json
{
  "reservationId": 123,
  "paymentMethod": "CARD",
  "successUrlDomain": "example.com"
}
```

Response:

```json
{
  "url": "https://checkout.stripe.com/..."
}
```

### Webhook Endpoint

```
POST /api/payment/webhook
```

This endpoint receives webhook events from Stripe and processes them accordingly.

### Refund Payment

```
POST /api/payment/refund/{reservationId}
```

Refunds a payment for a reservation.

### Capture Payment Intent

```
POST /api/payment/capture/{reservationId}
```

Captures a payment intent for a reservation.

### Cancel Payment Intent

```
POST /api/payment/cancel/{reservationId}
```

Cancels a payment intent for a reservation.

## Models

### Payment

The `Payment` entity represents a payment for a reservation and contains the following fields:

- `id`: The payment ID
- `amount`: The payment amount
- `paymentDate`: The date and time of the payment
- `status`: The payment status (PENDING, COMPLETED, FAILED, REFUNDED, CANCELLED)
- `paymentMethod`: The payment method (CARD, BANK_TRANSFER)
- `stripeSessionId`: The Stripe checkout session ID
- `stripePaymentIntentId`: The Stripe payment intent ID
- `stripeCustomerId`: The Stripe customer ID
- `reservation`: The associated reservation

## Integration with Frontend

To integrate with the frontend, you need to:

1. Add a payment button or form to your checkout page.
2. When the user clicks the button, send a request to the `/api/payment/checkout` endpoint.
3. Redirect the user to the URL returned in the response.
4. After the payment is completed, the user will be redirected to your confirmation page.

## Testing

For testing purposes, you can use Stripe's test cards:

- `4242 4242 4242 4242`: Successful payment
- `4000 0000 0000 0002`: Declined payment

Set your Stripe account to test mode when testing the integration.

## Troubleshooting

If you encounter any issues with the Stripe integration, check the following:

1. Make sure your environment variables are correctly set.
2. Check that your webhook endpoint is correctly configured in the Stripe Dashboard.
3. Look for any errors in the application logs.
4. Verify that your Stripe account is in the correct mode (test or live).

For more information, refer to the [Stripe API documentation](https://stripe.com/docs/api).
