import React, { useState } from 'react';
import { createUserWithEmailAndPassword } from 'firebase/auth';
import { setDoc, doc } from 'firebase/firestore';
import { auth, db } from '../firebase';
import { useNavigate, Link } from 'react-router-dom';

export default function Register() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!name.trim()) return setError("Name is required");
    
    setLoading(true);
    setError('');
    
    try {
      const userCredential = await createUserWithEmailAndPassword(auth, email, password);
      // Create user profile in Firestore
      await setDoc(doc(db, 'users', userCredential.user.uid), {
        userId: userCredential.user.uid,
        name,
        email,
        partnerId: null,
        coupleId: null,
        createdAt: new Date().toISOString()
      });
      navigate('/');
    } catch (err) {
      setError("Error: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4 bg-background">
      <div className="w-full max-w-sm bg-surface p-8 rounded-3xl shadow-sm border border-rose-100">
        <h1 className="text-3xl font-medium text-center text-text mb-2">Create Account</h1>
        <p className="text-center text-textLight mb-8 text-sm">Start your shared space today.</p>

        {error && <div className="mb-4 text-sm text-rose-600 bg-rose-50 p-3 rounded-xl">{error}</div>}

        <form onSubmit={handleRegister} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-textLight mb-1 ml-1">Your Name</label>
            <input 
              type="text" 
              className="w-full p-4 rounded-2xl bg-background border border-rose-100 focus:outline-none focus:border-primary transition-colors text-text"
              value={name} onChange={e => setName(e.target.value)} required 
              placeholder="Alex"
              disabled={loading}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-textLight mb-1 ml-1">Email</label>
            <input 
              type="email" 
              className="w-full p-4 rounded-2xl bg-background border border-rose-100 focus:outline-none focus:border-primary transition-colors text-text"
              value={email} onChange={e => setEmail(e.target.value)} required 
              placeholder="you@example.com"
              disabled={loading}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-textLight mb-1 ml-1">Password</label>
            <input 
              type="password" 
              className="w-full p-4 rounded-2xl bg-background border border-rose-100 focus:outline-none focus:border-primary transition-colors text-text"
              value={password} onChange={e => setPassword(e.target.value)} required 
              placeholder="••••••••"
              disabled={loading}
              minLength="6"
            />
          </div>
          <button type="submit" disabled={loading} className="w-full py-4 mt-6 bg-primary text-white rounded-2xl font-medium hover:bg-rose-600 transition-colors shadow-lg shadow-rose-200 disabled:opacity-50">
            {loading ? "Creating..." : "Sign Up"}
          </button>
        </form>
        <p className="mt-8 text-center text-sm text-textLight">
          Already have an account? <Link to="/login" className="text-primary hover:underline font-medium">Log in</Link>
        </p>
      </div>
    </div>
  );
}
