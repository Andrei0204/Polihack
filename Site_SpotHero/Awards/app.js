// Import Firebase
import { initializeApp } from "https://www.gstatic.com/firebasejs/9.22.0/firebase-app.js";
import { getAuth, signInWithEmailAndPassword } from "https://www.gstatic.com/firebasejs/9.22.0/firebase-auth.js";

// Your Firebase Configuration
const firebaseConfig = {
  apiKey: "AIzaSyD6vYXiKRBMVCkqSvdOmd9R_ejfa6c9zR4",
  authDomain: "polihack-2ede7.firebaseapp.com",
  projectId: "polihack-2ede7",
  storageBucket: "polihack-2ede7.firebasestorage.app",
  messagingSenderId: "743212779280",
  appId: "1:743212779280:web:527349115db6c3b8b48795",
  measurementId: "G-3EGJPFECJT"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

// Login Functionality
document.getElementById('loginButton').addEventListener('click', () => {
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const errorMessage = document.getElementById('errorMessage');

    // Reset error message
    errorMessage.textContent = '';

    // Login with Firebase
    signInWithEmailAndPassword(auth, email, password)
        .then((userCredential) => {
            // User is signed in
            const user = userCredential.user;
            alert(`Welcome, ${user.email}`);
            // Redirect or update UI as needed
        })
        .catch((error) => {
            // Handle Errors
            errorMessage.textContent = error.message;
        });
});
