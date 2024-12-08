/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {onRequest} = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const stripe = require('stripe')(functions.config().stripe.secret);  // Secret key for Stripe

admin.initializeApp();

// Firebase Function to create a PaymentIntent
exports.createPaymentIntent = functions.https.onRequest( async(req, res) => {
  const token = req.body.token;  // The token passed from frontend

  try {
    // Create a PaymentIntent using Stripe's API
    const paymentIntent = await stripe.paymentIntents.create({
      amount: 5000,  // Amount in cents (e.g., $50.00)
      currency: 'usd',
      payment_method: token,
      confirm: true,  // Confirm the payment immediately
    });

    // Send the client secret to the frontend
    res.status(200).send({ success: true });
  } catch (error) {
    console.error('Payment failed:', error);
    res.status(500).send({ success: false });
  }
});


