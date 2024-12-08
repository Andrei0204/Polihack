const firebaseConfig = {
  apiKey: "YOUR_API_KEY",
  authDomain: "YOUR_AUTH_DOMAIN",
  databaseURL: "YOUR_DATABASE_URL",
  projectId: "YOUR_PROJECT_ID",
  storageBucket: "YOUR_STORAGE_BUCKET",
  messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
  appId: "YOUR_APP_ID",
  measurementId: "YOUR_MEASUREMENT_ID"
};

import { initializeApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore'; // For Firestore
import { getAuth } from 'firebase/auth'; // For Firebase Authentication

// Initialize Firebase with the config object
const app = initializeApp(firebaseConfig);

// Initialize Firestore or Auth
const db = getFirestore(app);  // For Firestore
const auth = getAuth(app); 

import {collection, addDoc } from 'firebase/firestore';

async function addData() {
  try {
    const docRef = await addDoc(collection(db, "users"), {
      name: "John Doe",
      age: 30,
    });
    console.log("Document written with ID: ", docRef.id);
  } catch (e) {
    console.error("Error adding document: ", e);
  }
}


import { collection, getDocs } from 'firebase/firestore';

async function fetchData() {
  const querySnapshot = await getDocs(collection(db, "users"));
  querySnapshot.forEach((doc) => {
    console.log(doc.id, " => ", doc.data());
  });
}

import { createUserWithEmailAndPassword } from 'firebase/auth';

async function signUp(email, password) {
  try {
    const userCredential = await createUserWithEmailAndPassword(auth, email, password);
    const user = userCredential.user;
    console.log("User created:", user);
  } catch (error) {
    console.error("Error signing up: ", error);
  }
}

import { signInWithEmailAndPassword } from 'firebase/auth';

async function login(email, password) {
  try {
    const userCredential = await signInWithEmailAndPassword(auth, email, password);
    const user = userCredential.user;
    console.log("User logged in:", user);
  } catch (error) {
    console.error("Error logging in: ", error);
  }
}