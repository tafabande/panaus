import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
import { getStorage } from "firebase/storage";

const firebaseConfig = {
  apiKey: "AIzaSyAIaMjNMy1kcvViHXo0CTWO2RdEew_6hIc",
  authDomain: "two-lovers-os.firebaseapp.com",
  projectId: "two-lovers-os",
  storageBucket: "two-lovers-os.firebasestorage.app",
  messagingSenderId: "374714245828",
  appId: "1:374714245828:web:e05814b2b68e33a5e95f91",
  measurementId: "G-GP6ST8W5FW"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
export const storage = getStorage(app);
