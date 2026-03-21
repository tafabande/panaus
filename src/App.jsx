import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
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

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/pairing" element={
            <PrivateRoute requirePairing={false}>
              <Pairing />
            </PrivateRoute>
          } />
          <Route path="/mood" element={
            <PrivateRoute requirePairing={true}>
              <Mood />
            </PrivateRoute>
          } />
          <Route path="/notes" element={
            <PrivateRoute requirePairing={true}>
              <Notes />
            </PrivateRoute>
          } />
          <Route path="/calendar" element={
            <PrivateRoute requirePairing={true}>
              <Calendar />
            </PrivateRoute>
          } />
          <Route path="/asks" element={
            <PrivateRoute requirePairing={true}>
              <Asks />
            </PrivateRoute>
          } />
          <Route path="/updates" element={
            <PrivateRoute requirePairing={true}>
              <Updates />
            </PrivateRoute>
          } />
          <Route path="/todos" element={
            <PrivateRoute requirePairing={true}>
              <Todos />
            </PrivateRoute>
          } />
          <Route path="/photos" element={
            <PrivateRoute requirePairing={true}>
              <Photos />
            </PrivateRoute>
          } />
          <Route path="/analytics" element={
            <PrivateRoute requirePairing={true}>
              <Analytics />
            </PrivateRoute>
          } />
          <Route path="/location" element={
            <PrivateRoute requirePairing={true}>
              <LocationShare />
            </PrivateRoute>
          } />
          <Route path="/*" element={
            <PrivateRoute requirePairing={true}>
              <Dashboard />
            </PrivateRoute>
          } />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
