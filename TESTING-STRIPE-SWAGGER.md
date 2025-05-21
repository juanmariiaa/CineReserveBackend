# Probando la Integración de Stripe en Swagger

Este documento proporciona instrucciones paso a paso para probar la integración de pagos con Stripe utilizando Swagger UI.

## Requisitos Previos

1. La aplicación CineReserveBackend debe estar en ejecución.
2. Debes tener configuradas las variables de entorno necesarias:

   - `STRIPE_API_KEY`: Tu clave secreta de API de Stripe.
   - `STRIPE_WEBHOOK_SECRET`: El secreto de webhook de Stripe.
   - `STRIPE_PRODUCT_ID`: El ID del producto en Stripe.
   - `APP_DOMAIN`: El dominio de tu aplicación (por defecto: localhost:3000).

3. Debes tener una cuenta de Stripe en modo de prueba (test mode).

## Accediendo a Swagger UI

1. Abre tu navegador y navega a la URL de Swagger UI:

   ```
   http://localhost:8080/swagger-ui/index.html
   ```

2. En la interfaz de Swagger, busca y expande la sección "Payments" para ver todos los endpoints disponibles relacionados con pagos.

## Flujo de Prueba

### 1. Crear una Reserva

Antes de probar los pagos, necesitas crear una reserva:

1. Expande la sección "Reservations" en Swagger UI.
2. Encuentra y expande el endpoint `POST /api/reservations`.
3. Haz clic en "Try it out" y proporciona los datos necesarios para crear una reserva:
   ```json
   {
     "screeningId": 1,
     "seatIds": [1, 2]
   }
   ```
4. Haz clic en "Execute" y anota el ID de la reserva en la respuesta.

### 2. Crear una Sesión de Pago

1. En la sección "Payments", expande el endpoint `POST /api/payment/checkout`.
2. Haz clic en "Try it out" y proporciona los datos necesarios:

   ```json
   {
     "reservationId": 1,
     "paymentMethod": "CARD",
     "successUrlDomain": "localhost:3000"
   }
   ```

   - Reemplaza `1` con el ID de la reserva que creaste anteriormente.
   - Puedes elegir entre `CARD` o `BANK_TRANSFER` como método de pago.

3. Haz clic en "Execute" y deberías recibir una respuesta con una URL de checkout:

   ```json
   {
     "url": "https://checkout.stripe.com/c/pay/cs_test_...",
     "error": null
   }
   ```

4. Copia la URL de checkout y ábrela en una nueva pestaña del navegador.

### 3. Completar el Pago en Stripe

1. En la página de checkout de Stripe, usa los siguientes datos de prueba:

   - Número de tarjeta: `4242 4242 4242 4242`
   - Fecha de expiración: Cualquier fecha futura (ej. 12/25)
   - CVC: Cualquier número de 3 dígitos (ej. 123)
   - Nombre: Cualquier nombre
   - Dirección: Cualquier dirección

2. Completa el formulario y haz clic en "Pagar".

3. Después de un pago exitoso, serás redirigido a la URL de éxito que configuraste.

### 4. Verificar el Estado del Pago

Para verificar que el pago se ha procesado correctamente, puedes:

1. Comprobar en el dashboard de Stripe (en modo de prueba) que el pago aparece como completado.
2. Verificar en la base de datos que la reserva ha cambiado su estado a `CONFIRMED`.

### 5. Probar Otras Operaciones de Pago

#### Capturar un Pago

Si el pago está en estado pendiente (para tarjetas con método de captura manual):

1. Expande el endpoint `POST /api/payment/capture/{reservationId}`.
2. Haz clic en "Try it out" e ingresa el ID de la reserva.
3. Haz clic en "Execute" para capturar el pago.

#### Reembolsar un Pago

Para reembolsar un pago completado:

1. Expande el endpoint `POST /api/payment/refund/{reservationId}`.
2. Haz clic en "Try it out" e ingresa el ID de la reserva.
3. Haz clic en "Execute" para procesar el reembolso.

#### Cancelar un Pago

Para cancelar un pago pendiente:

1. Expande el endpoint `POST /api/payment/cancel/{reservationId}`.
2. Haz clic en "Try it out" e ingresa el ID de la reserva.
3. Haz clic en "Execute" para cancelar el pago.

## Probando Webhooks

Para probar los webhooks de Stripe localmente, necesitarás:

1. Instalar la CLI de Stripe:

   ```
   npm install -g stripe-cli
   ```

2. Iniciar sesión en Stripe:

   ```
   stripe login
   ```

3. Reenviar eventos de webhook a tu servidor local:

   ```
   stripe listen --forward-to http://localhost:8080/api/payment/webhook
   ```

4. En otra terminal, desencadenar eventos de prueba:
   ```
   stripe trigger checkout.session.completed
   ```

## Solución de Problemas

Si encuentras problemas al probar la integración de Stripe, verifica lo siguiente:

1. Asegúrate de que todas las variables de entorno estén configuradas correctamente.
2. Verifica que estés usando la cuenta de Stripe en modo de prueba.
3. Comprueba los logs del servidor para ver si hay errores específicos.
4. Verifica que la reserva exista y esté en el estado correcto antes de intentar procesar pagos.

## Recursos Adicionales

- [Documentación de Stripe](https://stripe.com/docs)
- [Tarjetas de prueba de Stripe](https://stripe.com/docs/testing#cards)
- [Webhooks de Stripe](https://stripe.com/docs/webhooks)
