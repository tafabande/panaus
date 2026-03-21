import React, { createContext, useContext, useEffect, useState } from 'react';
import { auth, db } from '../firebase';
import { onAuthStateChanged } from 'firebase/auth';
import { doc, onSnapshot } from 'firebase/firestore';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [currentUser, setCurrentUser] = useState(null);
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribeAuth = onAuthStateChanged(auth, user => {
      setCurrentUser(user);
      if (!user) {
        setUserData(null);
        setLoading(false);
      }
    });
    return unsubscribeAuth;
  }, []);

  useEffect(() => {
    if (currentUser) {
      const unsubscribeDoc = onSnapshot(doc(db, 'users', currentUser.uid), (docSnap) => {
        if (docSnap.exists()) {
          setUserData(docSnap.data());
        }
        setLoading(false);
      }, (error) => {
         console.error(error);
         setLoading(false);
      });
      return unsubscribeDoc;
    }
  }, [currentUser]);

  if (loading) return <div className="min-h-screen flex items-center justify-center bg-background"><div className="animate-pulse text-primary font-medium text-lg">Loading...</div></div>;

  return (
    <AuthContext.Provider value={{ currentUser, userData }}>
      {children}
    </AuthContext.Provider>
  );
};
