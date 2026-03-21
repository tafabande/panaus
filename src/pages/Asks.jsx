import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { db } from '../firebase';
import { collection, addDoc, query, where, orderBy, onSnapshot, doc, updateDoc } from 'firebase/firestore';
import { ArrowLeft, Send, CheckCircle, XCircle, Clock } from 'lucide-react';
import { Link } from 'react-router-dom';

const REQUEST_TYPES = [
  'Call me',
  'Send photo',
  'Remind me',
  'Help me',
  'Bring snacks',
  'Custom'
];

export default function Asks() {
  const { currentUser, userData } = useAuth();
  const [asks, setAsks] = useState([]);
  const [requestText, setRequestText] = useState('');
  const [requestType, setRequestType] = useState('Call me');
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);

  useEffect(() => {
    if (!userData?.coupleId) return;

    const q = query(
      collection(db, 'asks'),
      where('coupleId', '==', userData.coupleId),
      orderBy('createdAt', 'desc')
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const fetchedAsks = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
      setAsks(fetchedAsks);
    });

    return unsubscribe;
  }, [userData]);

  const handleSendRequest = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const askData = {
        coupleId: userData.coupleId,
        fromUserId: currentUser.uid,
        toUserId: userData.partnerId,
        requestText: requestType === 'Custom' ? requestText.trim() : requestType,
        requestType,
        status: 'pending', // pending, accepted, declined, later
        responseText: '',
        createdAt: new Date().toISOString(),
        respondedAt: null
      };
      
      await addDoc(collection(db, 'asks'), askData);
      setRequestText('');
      setRequestType('Call me');
      setShowForm(false);
    } catch (err) {
      console.error('Error sending ask', err);
    } finally {
      setLoading(false);
    }
  };

  const handleResponse = async (askId, newStatus) => {
    try {
      await updateDoc(doc(db, 'asks', askId), {
        status: newStatus,
        respondedAt: new Date().toISOString()
      });
    } catch (err) {
      console.error('Error updating ask', err);
    }
  };

  return (
    <div className="min-h-screen bg-background pb-20">
      <header className="bg-surface border-b border-rose-100 p-4 sticky top-0 z-10 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-4">
          <Link to="/" className="p-2 -ml-2 text-textLight hover:text-rose-500 rounded-full hover:bg-rose-50 transition-colors">
            <ArrowLeft size={20} />
          </Link>
          <h1 className="text-xl font-medium text-text">Requests & Asks</h1>
        </div>
        <button 
          onClick={() => setShowForm(!showForm)}
          className="text-xs font-medium text-primary hover:underline bg-primary/10 px-3 py-1.5 rounded-lg"
        >
          {showForm ? 'Cancel' : 'New Ask'}
        </button>
      </header>

      <main className="p-4 max-w-lg mx-auto mt-4 space-y-6">
        
        {showForm && (
          <section className="bg-surface border border-rose-100 p-5 rounded-3xl shadow-sm animate-in slide-in-from-top-4 fade-in duration-200">
             <form onSubmit={handleSendRequest} className="space-y-4">
               <div>
                  <label className="block text-xs font-semibold text-textLight uppercase tracking-wider mb-2 ml-1">Type of Request</label>
                  <div className="flex flex-wrap gap-2">
                    {REQUEST_TYPES.map(type => (
                      <button
                        key={type}
                        type="button"
                        onClick={() => setRequestType(type)}
                        className={`text-sm px-4 py-2 rounded-xl border transition-colors ${
                          requestType === type 
                            ? 'bg-primary text-white border-primary shadow-sm' 
                            : 'bg-background text-textLight border-rose-100 hover:border-primary/50'
                        }`}
                      >
                        {type}
                      </button>
                    ))}
                  </div>
               </div>

               {requestType === 'Custom' && (
                 <div>
                   <label className="block text-xs font-semibold text-textLight uppercase tracking-wider mb-1 ml-1 mt-4">Custom message</label>
                   <input 
                     type="text" 
                     value={requestText} 
                     onChange={e=>setRequestText(e.target.value)} 
                     required 
                     className="w-full p-3 rounded-xl bg-background border border-rose-100 focus:outline-none focus:border-primary text-text text-sm" 
                     placeholder="e.g. Please pick up the dry cleaning"
                   />
                 </div>
               )}
               
               <button type="submit" disabled={loading || (requestType === 'Custom' && !requestText.trim())} className="w-full py-3 mt-4 bg-primary text-white rounded-xl font-medium text-sm hover:bg-primary/90 transition-colors disabled:opacity-50">
                 Send Request
               </button>
             </form>
          </section>
        )}

        <section className="space-y-4 shadow-sm pb-10">
          {asks.length === 0 ? (
            <div className="text-center py-10 bg-surface rounded-3xl border border-rose-100 border-dashed">
               <Send size={32} className="mx-auto text-rose-200 mb-3" />
               <p className="text-sm text-textLight">No requests currently.</p>
            </div>
          ) : (
            asks.map(ask => {
              const amIReceiver = ask.toUserId === currentUser.uid;
              const isPending = ask.status === 'pending';
              
              let statusColor = "text-textLight bg-gray-50 border-gray-100";
              let statusIcon = null;
              if (ask.status === 'accepted') { statusColor = "text-emerald-600 bg-emerald-50 border-emerald-100"; statusIcon = <CheckCircle size={14} />; }
              if (ask.status === 'declined') { statusColor = "text-rose-600 bg-rose-50 border-rose-100"; statusIcon = <XCircle size={14} />; }
              if (ask.status === 'later') { statusColor = "text-amber-600 bg-amber-50 border-amber-100"; statusIcon = <Clock size={14} />; }

              return (
                <div key={ask.id} className="bg-surface p-4 rounded-3xl border border-rose-100 shadow-sm transition-colors">
                  <div className="flex justify-between items-start mb-2">
                    <span className="text-xs font-semibold uppercase tracking-wider text-rose-400">
                      {amIReceiver ? 'Partner asked you' : 'You asked partner'}
                    </span>
                    {!isPending && (
                      <span className={`flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider border ${statusColor}`}>
                        {statusIcon} {ask.status}
                      </span>
                    )}
                  </div>
                  
                  <p className="text-base text-text font-medium mb-1">"{ask.requestText}"</p>
                  
                  <p className="text-[10px] text-textLight uppercase tracking-wider mb-4">
                     {new Date(ask.createdAt).toLocaleDateString([], { month: 'short', day: 'numeric', hour: '2-digit', minute:'2-digit' })}
                  </p>

                  {isPending && amIReceiver && (
                    <div className="flex gap-2 pt-3 border-t border-rose-50">
                      <button onClick={() => handleResponse(ask.id, 'accepted')} className="flex-1 py-2 text-xs font-semibold bg-emerald-50 text-emerald-600 rounded-xl hover:bg-emerald-100 transition-colors border border-emerald-100">Accept</button>
                      <button onClick={() => handleResponse(ask.id, 'later')} className="flex-1 py-2 text-xs font-semibold bg-amber-50 text-amber-600 rounded-xl hover:bg-amber-100 transition-colors border border-amber-100">Later</button>
                      <button onClick={() => handleResponse(ask.id, 'declined')} className="flex-1 py-2 text-xs font-semibold bg-rose-50 text-rose-600 rounded-xl hover:bg-rose-100 transition-colors border border-rose-100">Decline</button>
                    </div>
                  )}
                  {isPending && !amIReceiver && (
                    <div className="pt-2 border-t border-rose-50">
                      <span className="inline-block px-2 py-1 bg-gray-50 text-xs font-medium text-gray-500 rounded-lg border border-gray-100">Waiting for response...</span>
                    </div>
                  )}
                </div>
              );
            })
          )}
        </section>
      </main>
    </div>
  );
}
