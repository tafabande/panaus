import React, { useState } from 'react';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from '../firebase';
import { useNavigate, Link } from 'react-router-dom';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await signInWithEmailAndPassword(auth, email, password);
      navigate('/');
    } catch (err) {
      setError("Error: " + err.message);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4 bg-background">
      <div className="w-full max-w-sm bg-surface p-8 rounded-3xl shadow-sm border border-rose-100">
        <h1 className="text-3xl font-medium text-center text-text mb-8">Welcome Back</h1>
        
        {error && <div className="mb-4 text-sm text-rose-600 bg-rose-50 p-3 rounded-xl">{error}</div>}

        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-textLight mb-1 ml-1">Email</label>
            <input 
              type="email" 
              className="w-full p-4 rounded-2xl bg-background border border-rose-100 focus:outline-none focus:border-primary transition-colors text-text"
              value={email} onChange={e => setEmail(e.target.value)} required 
              placeholder="you@example.com"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-textLight mb-1 ml-1">Password</label>
            <input 
              type="password" 
              className="w-full p-4 rounded-2xl bg-background border border-rose-100 focus:outline-none focus:border-primary transition-colors text-text"
              value={password} onChange={e => setPassword(e.target.value)} required 
              placeholder="••••••••"
            />
          </div>
          <button type="submit" className="w-full py-4 mt-6 bg-primary text-white rounded-2xl font-medium hover:bg-rose-600 transition-colors shadow-lg shadow-rose-200">
            Sign In
          </button>
        </form>
        <p className="mt-8 text-center text-sm text-textLight">
          Don't have an account? <Link to="/register" className="text-primary hover:underline font-medium">Sign up</Link>
        </p>
      </div>
    </div>
  );
}
