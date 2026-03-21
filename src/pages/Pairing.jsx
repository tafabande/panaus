import React, { useState } from 'react';
import { auth, db } from '../firebase';
import { doc, getDoc, updateDoc, setDoc } from 'firebase/firestore';
import { useAuth } from '../context/AuthContext';
import { LogOut, Copy, Check } from 'lucide-react';

export default function Pairing() {
  const { currentUser, userData } = useAuth();
  const [partnerCode, setPartnerCode] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(currentUser.uid);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handlePair = async (e) => {
    e.preventDefault();
    if (!partnerCode.trim()) return;
    if (partnerCode === currentUser.uid) return setError("You can't pair with yourself.");

    setLoading(true);
    setError('');

    try {
      const partnerRef = doc(db, 'users', partnerCode);
      const partnerSnap = await getDoc(partnerRef);
      
      if (!partnerSnap.exists()) {
        throw new Error("Invalid invite code. Partner not found.");
      }
      
      const partnerData = partnerSnap.data();
      if (partnerData.partnerId) {
        throw new Error("This user is already paired with someone.");
      }

      const coupleId = [currentUser.uid, partnerCode].sort().join('_');

      await setDoc(doc(db, 'couples', coupleId), {
        coupleId,
        user1Id: currentUser.uid,
        user2Id: partnerCode,
        createdAt: new Date().toISOString()
      });

      await updateDoc(doc(db, 'users', currentUser.uid), {
        partnerId: partnerCode,
        coupleId: coupleId
      });
      
      await updateDoc(partnerRef, {
        partnerId: currentUser.uid,
        coupleId: coupleId
      });

    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => auth.signOut();

  return (
    <div className="min-h-screen flex items-center justify-center px-4 bg-background">
      <div className="w-full max-w-md bg-surface p-8 rounded-3xl shadow-sm border border-rose-100 relative overflow-hidden">
        
        <button onClick={handleLogout} className="absolute top-6 right-6 p-2 text-textLight hover:text-rose-500 transition-colors bg-rose-50 rounded-full">
          <LogOut size={18} />
        </button>

        <h1 className="text-3xl font-medium text-center text-text mb-2 mt-4">Connect</h1>
        <p className="text-center text-textLight mb-8 text-sm px-6">Pair with your partner to start your shared space.</p>
        
        {error && <div className="mb-4 text-sm text-rose-600 bg-rose-50 p-3 rounded-xl">{error}</div>}

        <div className="bg-rose-50 rounded-2xl p-6 mb-7 border border-rose-100/50 relative">
          <p className="text-xs font-semibold text-rose-400 uppercase tracking-wider mb-2">Your Invite Code</p>
          <div className="flex items-center gap-2">
            <code className="flex-1 bg-white px-3 py-2.5 rounded-xl text-rose-600 border border-rose-200 overflow-hidden text-ellipsis whitespace-nowrap text-sm font-mono shadow-sm">
              {currentUser?.uid}
            </code>
            <button onClick={handleCopy} className="p-2.5 border border-rose-200 bg-white rounded-xl hover:bg-rose-100 transition-colors text-rose-500 shadow-sm">
              {copied ? <Check size={18} /> : <Copy size={18} />}
            </button>
          </div>
          <p className="text-xs text-rose-600/60 mt-3 leading-relaxed">Have your partner paste this code on their device, or paste theirs below.</p>
        </div>

        <form onSubmit={handlePair} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-textLight uppercase tracking-wider mb-2 ml-1">Partner's Code</label>
            <input 
              type="text" 
              className="w-full p-4 rounded-2xl bg-background border border-rose-100 focus:outline-none focus:border-primary transition-colors text-text shadow-sm"
              value={partnerCode} onChange={e => setPartnerCode(e.target.value)} required 
              placeholder="Paste code here"
              disabled={loading}
            />
          </div>
          <button type="submit" disabled={loading} className="w-full py-4 mt-8 bg-primary text-white rounded-2xl font-medium hover:bg-primary/90 transition-colors shadow-lg shadow-rose-200 disabled:opacity-50">
            {loading ? "Connecting..." : "Link Accounts"}
          </button>
        </form>
      </div>
    </div>
  );
}
