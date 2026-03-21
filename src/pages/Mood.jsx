import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { db } from '../firebase';
import { collection, addDoc, query, where, orderBy, getDocs, limit } from 'firebase/firestore';
import { ArrowLeft } from 'lucide-react';
import { Link } from 'react-router-dom';

const MOODS = [
  { value: 1, emoji: '😢', label: 'Sad' },
  { value: 2, emoji: '😐', label: 'Okay' },
  { value: 3, emoji: '🙂', label: 'Good' },
  { value: 4, emoji: '😁', label: 'Great' },
  { value: 5, emoji: '😍', label: 'Amazing' }
];

export default function Mood() {
  const { currentUser, userData } = useAuth();
  const [selectedMood, setSelectedMood] = useState(null);
  const [note, setNote] = useState('');
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (userData?.coupleId) {
      loadHistory();
    }
  }, [userData]);

  const loadHistory = async () => {
    try {
      const q = query(
        collection(db, 'moods'),
        where('coupleId', '==', userData.coupleId),
        orderBy('createdAt', 'desc'),
        limit(10)
      );
      const snapshot = await getDocs(q);
      const moods = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
      setHistory(moods);
    } catch (err) {
      console.error("Error loading moods:", err);
    }
  };

  const handleSave = async () => {
    if (!selectedMood) return;
    setLoading(true);
    try {
      const newMood = {
        userId: currentUser.uid,
        coupleId: userData.coupleId,
        moodValue: selectedMood.value,
        emoji: selectedMood.emoji,
        note,
        createdAt: new Date().toISOString()
      };
      await addDoc(collection(db, 'moods'), newMood);
      setSelectedMood(null);
      setNote('');
      loadHistory();
    } catch (err) {
      console.error("Error saving mood", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-background pb-20">
      <header className="bg-surface border-b border-rose-100 p-4 sticky top-0 z-10 flex items-center gap-4 shadow-sm">
        <Link to="/" className="p-2 -ml-2 text-textLight hover:text-rose-500 rounded-full hover:bg-rose-50 transition-colors">
          <ArrowLeft size={20} />
        </Link>
        <h1 className="text-xl font-medium text-text">Mood Tracker</h1>
      </header>

      <main className="p-4 max-w-lg mx-auto mt-4 space-y-8">
        <section className="bg-surface border border-rose-100 p-6 rounded-3xl shadow-sm">
          <h2 className="text-center font-medium text-text mb-6">How are you feeling right now?</h2>
          
          <div className="flex justify-between items-center mb-6 px-2">
            {MOODS.map(m => (
              <button 
                key={m.value}
                onClick={() => setSelectedMood(m)}
                className={`text-4xl transition-transform hover:scale-110 ${selectedMood?.value === m.value ? 'scale-125 drop-shadow-md' : 'opacity-50 hover:opacity-100 grayscale hover:grayscale-0'}`}
              >
                {m.emoji}
              </button>
            ))}
          </div>

          <textarea 
            className="w-full bg-background border border-rose-100 rounded-2xl p-4 text-sm focus:outline-none focus:border-primary transition-colors resize-none mb-4 text-text shadow-inner"
            rows="3"
            placeholder="Add a little note... (optional)"
            value={note}
            onChange={e => setNote(e.target.value)}
          />

          <button 
            disabled={!selectedMood || loading}
            onClick={handleSave}
            className="w-full py-4 bg-primary text-white rounded-2xl font-medium shadow-lg shadow-rose-200 hover:bg-primary/90 transition-colors disabled:opacity-50 disabled:shadow-none"
          >
            {loading ? "Saving..." : "Log Mood"}
          </button>
        </section>

        <section>
          <h3 className="text-xs font-semibold text-textLight uppercase tracking-wider mb-4 px-2">Recent History</h3>
          <div className="space-y-3">
            {history.length === 0 ? (
              <p className="text-sm text-textLight italic text-center py-8">No moods logged yet.</p>
            ) : (
              history.map(item => (
                <div key={item.id} className="bg-surface border border-rose-100 p-4 rounded-2xl flex items-center gap-4 shadow-sm">
                  <span className="text-3xl drop-shadow-sm">{item.emoji}</span>
                  <div className="flex-1">
                    <div className="flex justify-between items-baseline mb-1">
                      <span className="font-medium text-text text-sm">
                        {item.userId === currentUser.uid ? 'You' : 'Your Partner'}
                      </span>
                      <span className="text-[10px] text-textLight uppercase tracking-wider font-medium">
                        {new Date(item.createdAt).toLocaleDateString([], { month: 'short', day: 'numeric', hour: '2-digit', minute:'2-digit' })}
                      </span>
                    </div>
                    {item.note && <p className="text-sm text-textLight italic bg-rose-50/50 p-2 rounded-lg mt-2 border border-rose-100/30">"{item.note}"</p>}
                  </div>
                </div>
              ))
            )}
          </div>
        </section>
      </main>
    </div>
  );
}
