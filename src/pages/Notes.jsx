import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { db } from '../firebase';
import { collection, addDoc, query, where, orderBy, onSnapshot, limit } from 'firebase/firestore';
import { ArrowLeft, Send } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function Notes() {
  const { currentUser, userData } = useAuth();
  const [notes, setNotes] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [sending, setSending] = useState(false);
  const bottomRef = useRef(null);

  useEffect(() => {
    if (!userData?.coupleId) return;

    const q = query(
      collection(db, 'notes'),
      where('coupleId', '==', userData.coupleId),
      orderBy('createdAt', 'asc'),
      limit(50)
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const fetchedNotes = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
      setNotes(fetchedNotes);
      setTimeout(() => bottomRef.current?.scrollIntoView({ behavior: 'smooth' }), 100);
    });

    return unsubscribe;
  }, [userData]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!newMessage.trim() || sending) return;

    setSending(true);
    try {
      const noteData = {
        coupleId: userData.coupleId,
        senderId: currentUser.uid,
        receiverId: userData.partnerId,
        content: newMessage.trim(),
        createdAt: new Date().toISOString(),
      };
      await addDoc(collection(db, 'notes'), noteData);
      setNewMessage('');
    } catch (err) {
      console.error('Error sending note', err);
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <header className="bg-surface border-b border-rose-100 p-4 sticky top-0 z-10 flex items-center gap-4 shadow-sm shrink-0">
        <Link to="/" className="p-2 -ml-2 text-textLight hover:text-rose-500 rounded-full hover:bg-rose-50 transition-colors">
          <ArrowLeft size={20} />
        </Link>
        <h1 className="text-xl font-medium text-text">Pocket Notes</h1>
      </header>

      <main className="flex-1 p-4 max-w-lg w-full mx-auto overflow-y-auto space-y-4 pb-24">
        {notes.length === 0 ? (
          <p className="text-center text-textLight text-sm italic mt-10">No notes yet. Leave a little message!</p>
        ) : (
          notes.map(note => {
            const isMe = note.senderId === currentUser.uid;
            return (
              <div key={note.id} className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}>
                <div className={`max-w-[75%] p-3 rounded-2xl shadow-sm border ${
                  isMe ? 'bg-primary text-white border-primary rounded-tr-sm' 
                       : 'bg-surface text-text border-rose-100 rounded-tl-sm'
                }`}>
                  <p className="text-sm">{note.content}</p>
                  <p className={`text-[10px] mt-1 text-right ${isMe ? 'text-rose-200' : 'text-textLight'}`}>
                    {new Date(note.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </p>
                </div>
              </div>
            );
          })
        )}
        <div ref={bottomRef} />
      </main>

      <footer className="bg-surface border-t border-rose-100 p-4 fixed bottom-0 w-full max-w-lg left-1/2 -translate-x-1/2">
        <form onSubmit={handleSend} className="flex items-center gap-2 relative">
          <input
            type="text"
            className="flex-1 bg-background border border-rose-200 rounded-full py-3 pl-4 pr-12 text-sm focus:outline-none focus:border-primary transition-colors text-text shadow-inner"
            placeholder="Write a note..."
            value={newMessage}
            onChange={e => setNewMessage(e.target.value)}
          />
          <button 
            type="submit" 
            disabled={!newMessage.trim() || sending}
            className="absolute right-2 p-2 bg-primary text-white rounded-full hover:bg-primary/90 transition-colors disabled:opacity-50 disabled:hover:bg-primary shadow-sm"
          >
            <Send size={16} />
          </button>
        </form>
      </footer>
    </div>
  );
}
