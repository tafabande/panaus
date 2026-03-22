import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore, enableIndexedDbPersistence } from "firebase/firestore";
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

// Enable offline persistence so Firestore caches data locally.
// This is THE critical fix for "services not working" on native Android —
// without it, every read requires a live network connection.
enableIndexedDbPersistence(db).catch((err) => {
  if (err.code === 'failed-precondition') {
    // Multiple tabs open — persistence can only be enabled in one tab at a time
    console.warn('Firestore persistence failed: multiple tabs open');
  } else if (err.code === 'unimplemented') {
    // The current browser/environment doesn't support persistence
    console.warn('Firestore persistence not supported in this environment');
  }
});
