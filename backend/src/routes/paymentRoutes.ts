import { Router, Request, Response } from 'express';
import Stripe from 'stripe';

const router = Router();

// Initialize Stripe — key loaded from environment
const stripeClient = process.env.STRIPE_SECRET_KEY
  ? new Stripe(process.env.STRIPE_SECRET_KEY)
  : null;

// Price for a living trust document (in cents: $29.99)
const TRUST_DOCUMENT_PRICE_CENTS = 2999;
const TRUST_DOCUMENT_CURRENCY = 'usd';

// ─────────────────────────────────────────────────────────────────────────────
// ROUTES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * GET /api/payments/config
 * Returns the Stripe publishable key and pricing info for the frontend.
 */
router.get('/config', (_req: Request, res: Response) => {
  res.json({
    success: true,
    publishableKey: process.env.STRIPE_PUBLISHABLE_KEY || '',
    price: {
      amount: TRUST_DOCUMENT_PRICE_CENTS,
      currency: TRUST_DOCUMENT_CURRENCY,
      displayPrice: `$${(TRUST_DOCUMENT_PRICE_CENTS / 100).toFixed(2)}`,
      description: 'Official Living Trust Document (PDF)',
    },
  });
});

/**
 * POST /api/payments/create-intent
 * Creates a Stripe PaymentIntent for the living trust document purchase.
 * Body: { trustId?, trustName, customerEmail? }
 * Returns: { clientSecret, paymentIntentId, amount, currency }
 */
router.post('/create-intent', async (req: Request, res: Response) => {
  try {
    // If Stripe is not configured, return a mock for development
    if (!stripeClient) {
      console.warn('[PAYMENTS] Stripe not configured — returning mock PaymentIntent for development.');
      return res.json({
        success: true,
        clientSecret: 'pi_mock_secret_for_development_only',
        paymentIntentId: `pi_mock_${Date.now()}`,
        amount: TRUST_DOCUMENT_PRICE_CENTS,
        currency: TRUST_DOCUMENT_CURRENCY,
        displayPrice: `$${(TRUST_DOCUMENT_PRICE_CENTS / 100).toFixed(2)}`,
        isDevelopmentMock: true,
      });
    }

    const { trustName, customerEmail, trustId } = req.body;

    const paymentIntent = await stripeClient.paymentIntents.create({
      amount: TRUST_DOCUMENT_PRICE_CENTS,
      currency: TRUST_DOCUMENT_CURRENCY,
      automatic_payment_methods: { enabled: true },
      description: `Living Trust Document: ${trustName || 'Living Trust'}`,
      receipt_email: customerEmail || undefined,
      metadata: {
        product: 'living_trust_pdf',
        trustId: trustId || '',
        trustName: trustName || '',
        generatedAt: new Date().toISOString(),
      },
    });

    res.json({
      success: true,
      clientSecret: paymentIntent.client_secret,
      paymentIntentId: paymentIntent.id,
      amount: paymentIntent.amount,
      currency: paymentIntent.currency,
      displayPrice: `$${(paymentIntent.amount / 100).toFixed(2)}`,
    });
  } catch (err: any) {
    console.error('[PAYMENTS] create-intent error:', err);
    res.status(500).json({
      error: 'Failed to create payment intent',
      message: err.message,
      code: err.code,
    });
  }
});

/**
 * POST /api/payments/confirm
 * Verifies that a PaymentIntent was successfully paid, then issues a download token.
 * Body: { paymentIntentId, trustData }
 * Returns: { success, downloadToken, expiresAt }
 */
router.post('/confirm', async (req: Request, res: Response) => {
  try {
    const { paymentIntentId, trustData } = req.body;

    if (!paymentIntentId || !trustData) {
      return res.status(400).json({ error: 'paymentIntentId and trustData are required' });
    }

    // Development mock: accept mock payment intent IDs
    if (paymentIntentId.startsWith('pi_mock_') && !stripeClient) {
      console.warn('[PAYMENTS] Accepting mock PaymentIntent for development.');
      // Issue download token via internal call
      const tokenResponse = await issueToken(trustData, paymentIntentId);
      return res.json(tokenResponse);
    }

    if (!stripeClient) {
      return res.status(503).json({ error: 'Payment system not configured. Please set STRIPE_SECRET_KEY.' });
    }

    // Retrieve the PaymentIntent from Stripe to verify it was actually paid
    const paymentIntent = await stripeClient.paymentIntents.retrieve(paymentIntentId);

    if (paymentIntent.status !== 'succeeded') {
      return res.status(402).json({
        error: 'Payment not completed',
        status: paymentIntent.status,
        message: 'Payment has not been confirmed as successful. Please complete the payment first.',
      });
    }

    // Verify the amount matches our expected price
    if (paymentIntent.amount !== TRUST_DOCUMENT_PRICE_CENTS) {
      console.warn(`[PAYMENTS] Amount mismatch: expected ${TRUST_DOCUMENT_PRICE_CENTS}, got ${paymentIntent.amount}`);
    }

    const tokenResponse = await issueToken(trustData, paymentIntentId);
    res.json(tokenResponse);
  } catch (err: any) {
    console.error('[PAYMENTS] confirm error:', err);
    res.status(500).json({
      error: 'Failed to confirm payment',
      message: err.message,
    });
  }
});

/**
 * POST /api/payments/webhook
 * Stripe webhook endpoint for async payment events.
 * Must be registered in Stripe Dashboard and uses raw body for signature verification.
 */
router.post('/webhook', (req: Request, res: Response) => {
  const sig = req.headers['stripe-signature'] as string;
  const webhookSecret = process.env.STRIPE_WEBHOOK_SECRET;

  if (!stripeClient || !webhookSecret) {
    console.warn('[PAYMENTS] Webhook received but Stripe not configured.');
    return res.json({ received: true });
  }

  let event: Stripe.Event;

  try {
    event = stripeClient.webhooks.constructEvent(req.body, sig, webhookSecret);
  } catch (err: any) {
    console.error(`[PAYMENTS] Webhook signature verification failed: ${err.message}`);
    return res.status(400).json({ error: `Webhook Error: ${err.message}` });
  }

  // Handle the event
  switch (event.type) {
    case 'payment_intent.succeeded': {
      const pi = event.data.object as Stripe.PaymentIntent;
      console.log(`[PAYMENTS] PaymentIntent succeeded: ${pi.id}, amount: $${pi.amount / 100}`);
      // TODO: Store payment record in database
      break;
    }
    case 'payment_intent.payment_failed': {
      const pi = event.data.object as Stripe.PaymentIntent;
      console.error(`[PAYMENTS] PaymentIntent failed: ${pi.id}`);
      break;
    }
    case 'charge.refunded': {
      const charge = event.data.object as Stripe.Charge;
      console.log(`[PAYMENTS] Charge refunded: ${charge.id}`);
      // TODO: Revoke download token if applicable
      break;
    }
    default:
      console.log(`[PAYMENTS] Unhandled event type: ${event.type}`);
  }

  res.json({ received: true });
});

/**
 * GET /api/payments/status/:paymentIntentId
 * Check the status of a payment intent.
 */
router.get('/status/:paymentIntentId', async (req: Request, res: Response) => {
  try {
    const { paymentIntentId } = req.params;

    if (paymentIntentId.startsWith('pi_mock_')) {
      return res.json({ success: true, status: 'succeeded', isDevelopmentMock: true });
    }

    if (!stripeClient) {
      return res.status(503).json({ error: 'Payment system not configured.' });
    }

    const paymentIntent = await stripeClient.paymentIntents.retrieve(paymentIntentId);
    res.json({
      success: true,
      status: paymentIntent.status,
      amount: paymentIntent.amount,
      currency: paymentIntent.currency,
    });
  } catch (err: any) {
    res.status(500).json({ error: 'Failed to retrieve payment status', message: err.message });
  }
});

// ─────────────────────────────────────────────────────────────────────────────
// INTERNAL HELPER
// ─────────────────────────────────────────────────────────────────────────────

async function issueToken(trustData: any, paymentIntentId: string) {
  // Internal HTTP call to PDF route to issue a download token
  const crypto = await import('crypto');
  const token = crypto.randomBytes(32).toString('hex');
  const expiresAt = Date.now() + 24 * 60 * 60 * 1000; // 24 hours

  // Re-use the downloadTokens map from pdfRoutes
  // In production, store in database. Here we use a module-level approach.
  // We call the internal API endpoint instead.
  const BASE_URL = `http://localhost:${process.env.PORT || 3001}`;

  const response = await fetch(`${BASE_URL}/api/pdf/issue-download-token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ trustData, paymentIntentId }),
  });

  if (!response.ok) {
    throw new Error('Failed to issue download token');
  }

  const data = await response.json() as any;
  return {
    success: true,
    downloadToken: data.downloadToken,
    expiresAt: data.expiresAt,
    downloadUrl: `/api/pdf/download/${data.downloadToken}`,
    downloadUrlBase64: `/api/pdf/download-base64/${data.downloadToken}`,
    message: 'Payment confirmed! Your document is ready to download.',
    paymentIntentId,
  };
}

export default router;
