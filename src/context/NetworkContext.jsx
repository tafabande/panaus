import React, { createContext, useContext, useState, useEffect } from 'react';

const NetworkContext = createContext({ isOnline: true });

export const useNetwork = () => useContext(NetworkContext);

export const NetworkProvider = ({ children }) => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);

  useEffect(() => {
    const goOnline = () => setIsOnline(true);
    const goOffline = () => setIsOnline(false);

    window.addEventListener('online', goOnline);
    window.addEventListener('offline', goOffline);

    return () => {
      window.removeEventListener('online', goOnline);
      window.removeEventListener('offline', goOffline);
    };
  }, []);

  return (
    <NetworkContext.Provider value={{ isOnline }}>
      {/* Offline banner — slides in from top when offline */}
      {!isOnline && (
        <div className="fixed top-0 left-0 right-0 z-[9999] bg-amber-500 text-white text-center py-2 text-xs font-semibold tracking-wide shadow-lg animate-pulse">
          ⚡ You're offline — changes will sync when reconnected
        </div>
      )}
      {children}
    </NetworkContext.Provider>
  );
};
