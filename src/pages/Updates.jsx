import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { db } from '../firebase';
import { collection, addDoc, query, where, orderBy, onSnapshot, limit } from 'firebase/firestore';
import { ArrowLeft, Activity } from 'lucide-react';
import { Link } from 'react-router-dom';

const STATUS_PRESETS = [
  'Busy 🛑',
  'In class 📚',
  'At work 💼',
  'Free soon ⌛',
  'Feeling low 🌧️',
  'Miss you ❤️',
  'Will reply later ⏳'
];

export default function Updates() {
  const { currentUser, userData } = useAuth();
  const [updates, setUpdates] = useState([]);
  const [statusText, setStatusText] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!userData?.coupleId) return;

    const q = query(
      collection(db, 'updates'),
      where('coupleId', '==', userData.coupleId),
      orderBy('createdAt', 'desc'),
      limit(20)
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const fetchedStats = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
      setUpdates(fetchedStats);
    });

    return unsubscribe;
  }, [userData]);

  const handlePost = async (text) => {
    if (!text.trim()) return;
    setLoading(true);
    
    try {
      const updateData = {
        coupleId: userData.coupleId,
        userId: currentUser.uid,
        statusText: text.trim(),
        createdAt: new Date().toISOString(),
      };
      
      await addDoc(collection(db, 'updates'), updateData);
      setStatusText('');
    } catch (err) {
      console.error('Error posting update', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-background pb-20">
      <header className="bg-surface border-b border-rose-100 p-4 sticky top-0 z-10 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-4">
          <Link to="/" className="p-2 -ml-2 text-textLight hover:text-rose-500 rounded-full hover:bg-rose-50 transition-colors">
            <ArrowLeft size={20} />
          </Link>
          <h1 className="text-xl font-medium text-text">Status Updates</h1>
        </div>
      </header>

      <main className="p-4 max-w-lg mx-auto mt-4 space-y-6">
        
        <section className="bg-surface border border-rose-100 p-5 rounded-3xl shadow-sm">
           <h2 className="text-sm font-semibold text-textLight mb-4 uppercase tracking-wider">Set a Quick Status</h2>
           <div className="flex flex-wrap gap-2 mb-4">
             {STATUS_PRESETS.map(preset => (
               <button 
                 key={preset}
                 onClick={() => handlePost(preset)}
                 className="text-sm px-4 py-2 bg-indigo-50 text-indigo-700 border border-indigo-100 hover:bg-indigo-100 rounded-full transition-colors font-medium shadow-sm"
                 disabled={loading}
               >
                 {preset}
               </button>
             ))}
           </div>
           <div className="flex gap-2 relative">
              <input 
                type="text" 
                value={statusText}
                onChange={e => setStatusText(e.target.value)}
                placeholder="Or type a custom status..."
                className="flex-1 p-3 rounded-2xl bg-background border border-rose-100 focus:outline-none focus:border-indigo-400 text-sm"
              />
              <button 
                onClick={() => handlePost(statusText)} 
                disabled={!statusText.trim() || loading}
                className="px-6 py-3 bg-indigo-500 text-white rounded-2xl font-medium text-sm hover:bg-indigo-600 disabled:opacity-50 shadow-md shadow-indigo-200"
              >
                Post
              </button>
           </div>
        </section>

        <section className="space-y-4">
          <h3 className="text-xs font-semibold text-textLight uppercase tracking-wider mb-4 px-2">Recent Timeline</h3>
          {updates.length === 0 ? (
            <div className="text-center py-10 bg-surface rounded-3xl border border-rose-100 border-dashed">
               <Activity size={32} className="mx-auto text-rose-200 mb-3" />
               <p className="text-sm text-textLight">No recent updates.</p>
            </div>
          ) : (
            <div className="relative pl-4 space-y-4">
               {/* Vertical line connecting updates */}
               <div className="absolute left-9 top-6 bottom-6 w-0.5 bg-rose-100"></div>

              {updates.map(update => {
                const isMe = update.userId === currentUser.uid;
                return (
                  <div key={update.id} className="relative flex items-center justify-between gap-4">
                    <div className="flex items-center justify-center w-10 h-10 rounded-full border-[3px] border-white bg-indigo-100 text-indigo-500 z-10 shrink-0">
                      <Activity size={16} />
                    </div>
                    <div className="flex-1 bg-surface p-4 rounded-3xl border border-rose-100 shadow-sm relative">
                      <div className="flex justify-between items-baseline mb-1">
                        <span className="font-semibold text-text text-sm">
                          {isMe ? 'You' : 'Partner'}
                        </span>
                        <span className="text-[10px] text-textLight font-medium uppercase tracking-widest">
                          {new Date(update.createdAt).toLocaleTimeString([], { hour: '2-digit', minute:'2-digit' })}
                        </span>
                      </div>
                      <p className="text-sm text-textLight">{update.statusText}</p>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </section>
      </main>
    </div>
  );
}
