

const admin = require("firebase-admin");
const functions = require("firebase-functions")
const stripe = require('stripe')('sk_test_51QT6bmKYkxJWE8AuAeLlmIxkWUN3ixhRHxfYHamA8d7DCHMCmhxy2ErXHDjWbkT4pEviG06wObMfuBPgMjDjDLAz00JYPrzL8M');

exports.helloWorld = functions.https.onRequest(async (request, response) => {
    const amount =request.query.amt;
    const email =request.query.email;
    const customer = await stripe.customers.create({
        name:email,
        email:email,
    });
    const ephemeralKey = await stripe.ephemeralKeys.create(
        {customer: customer.id},
        {apiVersion: '2024-11-20.acacia'}
    );
    const paymentIntent = await stripe.paymentIntents.create({
        amount: amount,
        currency: 'ron',
        customer: customer.id,
        // In the latest version of the API, specifying the `automatic_payment_methods` parameter
        // is optional because Stripe enables its functionality by default.
        automatic_payment_methods: {
            enabled: true,
        },
    });

    response.json({
        paymentIntent: paymentIntent.client_secret,
        ephemeralKey: ephemeralKey.secret,
        customer: customer.id,
        publishableKey: 'pk_test_51QT6bmKYkxJWE8AuoRSYoBy8ZecgFXF7by7DcrB4DoAd90Gj0i4bnvnDmzBYrGAn7Oj8HTM4QZpJs82WJWaVK2p700OnLI0AoX'
    });
});
