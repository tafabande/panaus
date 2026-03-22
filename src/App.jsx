import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { NetworkProvider } from './context/NetworkContext';
import { App as NativeApp } from '@capacitor/app';
import { Capacitor } from '@capacitor/core';
import { StatusBar, Style } from '@capacitor/status-bar';
import { SplashScreen } from '@capacitor/splash-screen';
import Login from './pages/Login';
import Register from './pages/Register';
import Pairing from './pages/Pairing';
import Dashboard from './pages/Dashboard';
import Mood from './pages/Mood';
import Notes from './pages/Notes';
import Calendar from './pages/Calendar';
import Asks from './pages/Asks';
import Updates from './pages/Updates';
import Todos from './pages/Todos';
import Photos from './pages/Photos';
import Analytics from './pages/Analytics';
import LocationShare from './pages/LocationShare';

const PrivateRoute = ({ children, requirePairing = true }) => {
  const { currentUser, userData } = useAuth();
  
  // Not logged in
  if (!currentUser) return <Navigate to="/login" />;
  
  if (requirePairing) {
    // Logged in, but document hasn't loaded yet
    if (!userData) return <div className="min-h-screen bg-background flex justify-center items-center"><div className="animate-pulse text-primary font-medium">Checking Profile...</div></div>;
    
    // Logged in, profile loaded, but no partner
    if (!userData.partnerId) {
      return <Navigate to="/pairing" />;
    }
  } else {
    // If we're ON the pairing page, check if we actually have a partner now
    if (userData && userData.partnerId) {
      return <Navigate to="/" />;
    }
  }
  
  return children;
};

// Native hardware back-button handler (needs Router context so it must be inside Router)
const AppRoutes = () => {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // Only register on native platforms (Android/iOS WebView)
    if (Capacitor.isNativePlatform()) {
      const backListener = NativeApp.addListener('backButton', ({ canGoBack }) => {
        // If we are at the app root, back button means "close app"
        if (location.pathname === '/' || location.pathname === '/login') {
          NativeApp.exitApp();
        } else {
          // Otherwise navigate back in React Router history
          navigate(-1);
        }
      });
      return () => {
        backListener.then(l => l.remove());
      };
    }
  }, [navigate, location]);

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/pairing" element={<PrivateRoute requirePairing={false}><Pairing /></PrivateRoute>} />
      <Route path="/mood" element={<PrivateRoute requirePairing={true}><Mood /></PrivateRoute>} />
      <Route path="/notes" element={<PrivateRoute requirePairing={true}><Notes /></PrivateRoute>} />
      <Route path="/calendar" element={<PrivateRoute requirePairing={true}><Calendar /></PrivateRoute>} />
      <Route path="/asks" element={<PrivateRoute requirePairing={true}><Asks /></PrivateRoute>} />
      <Route path="/updates" element={<PrivateRoute requirePairing={true}><Updates /></PrivateRoute>} />
      <Route path="/todos" element={<PrivateRoute requirePairing={true}><Todos /></PrivateRoute>} />
      <Route path="/photos" element={<PrivateRoute requirePairing={true}><Photos /></PrivateRoute>} />
      <Route path="/analytics" element={<PrivateRoute requirePairing={true}><Analytics /></PrivateRoute>} />
      <Route path="/location" element={<PrivateRoute requirePairing={true}><LocationShare /></PrivateRoute>} />
      <Route path="/*" element={<PrivateRoute requirePairing={true}><Dashboard /></PrivateRoute>} />
    </Routes>
  );
};

function App() {
  useEffect(() => {
    const initNative = async () => {
      if (Capacitor.isNativePlatform()) {
        try {
          // Set status bar to match app theme (light background)
          await StatusBar.setStyle({ style: Style.Light });
          await StatusBar.setBackgroundColor({ color: '#ffffff' });
          // Hide splash screen explicitly once React has painted everything
          await SplashScreen.hide();
        } catch (e) {
          console.warn('Native APIs not available:', e);
        }
      }
    };
    initNative();
  }, []);

  return (
    <NetworkProvider>
      <AuthProvider>
        <Router>
          <AppRoutes />
        </Router>
      </AuthProvider>
    </NetworkProvider>
  );
}

export default App;
