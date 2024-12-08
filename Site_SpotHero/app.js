// Initialize Stripe.js with your public key (replace with your own key)
const stripe = Stripe('pk_test_51QT6bmKYkxJWE8AuoRSYoBy8ZecgFXF7by7DcrB4DoAd90Gj0i4bnvnDmzBYrGAn7Oj8HTM4QZpJs82WJWaVK2p700OnLI0AoX'); 
const elements = stripe.elements();

// Create an instance of the card Element
const card = elements.create('card');
card.mount('#card-element');

// Handle form submission
const form = document.getElementById('payment-form');
form.addEventListener('submit', async (event) => {
  event.preventDefault();

  // Create a payment method using the card Element
  const {token, error} = await stripe.createToken(card);

  if (error) {
    // Display error in the card errors div
    const errorElement = document.getElementById('card-errors');
    errorElement.textContent = error.message;
  } else {
    // Send the token to your backend for processing
    createPaymentIntent(token.id);
  }
});

// Function to handle the payment intent creation
async function createPaymentIntent(token) {
  const response = await fetch('/create-payment-intent', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      token: token,
    }),
  });

  const data = await response.json();

  if (data.success) {
    alert('Payment Successful!');
  } else {
    alert('Payment Failed!');
  }
}
