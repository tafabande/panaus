import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Heart, Calendar, MessageSquare, Bell, CheckCircle, Image as ImageIcon, TrendingUp, MapPin, User, Settings } from 'lucide-react';
import { auth, db } from '../firebase';
import { collection, query, where, orderBy, limit, onSnapshot, addDoc } from 'firebase/firestore';
import { Link } from 'react-router-dom';

export default function Dashboard() {
  const { userData } = useAuth();
  const [partnerStatus, setPartnerStatus] = useState(null);
  
  const handleLogout = () => auth.signOut();

  useEffect(() => {
    if (!userData?.coupleId || !userData?.partnerId) return;

    const q = query(
      collection(db, 'updates'),
      where('coupleId', '==', userData.coupleId),
      where('userId', '==', userData.partnerId),
      orderBy('createdAt', 'desc'),
      limit(1)
    );

    const unsub = onSnapshot(q, snap => {
      if (!snap.empty) {
        setPartnerStatus(snap.docs[0].data());
      }
    });

    return unsub;
  }, [userData]);

  return (
    <div className="min-h-screen bg-background pb-20">
      <header className="bg-surface border-b border-rose-100 p-4 sticky top-0 z-10 flex justify-between items-center shadow-sm">
        <div>
          <h1 className="text-xl font-medium text-text">Our Space</h1>
          <div className="flex items-center gap-2 mt-0.5">
            <p className="text-xs text-textLight">Hi, {userData?.name || 'there'}</p>
            {partnerStatus && (
               <Link to="/updates" className="text-[10px] font-medium px-2 py-0.5 bg-indigo-50 text-indigo-600 rounded-full border border-indigo-100 hover:bg-indigo-100 transition-colors">
                 Partner is {partnerStatus.statusText.toLowerCase()}
               </Link>
            )}
            {!partnerStatus && (
               <Link to="/updates" className="text-[10px] font-medium px-2 py-0.5 bg-gray-50 text-gray-400 rounded-full border border-gray-100 hover:bg-gray-100 transition-colors">
                 Set Status
               </Link>
            )}
          </div>
        </div>
        <button onClick={handleLogout} className="text-xs text-rose-500 font-medium px-4 py-2 bg-rose-50 rounded-xl shrink-0 transition-colors hover:bg-rose-100 border border-rose-100">
          Sign Out
        </button>
      </header>

      <main className="p-4 max-w-lg mx-auto mt-2 space-y-6">
        
        {/* Mood Widget */}
        <section className="bg-surface border border-rose-100 p-5 rounded-3xl shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xs font-semibold text-textLight uppercase tracking-wider flex items-center gap-2">
              <Heart size={16} className="text-primary"/> Today's Mood
            </h2>
            <Link to="/mood" className="text-xs font-medium text-primary hover:underline bg-primary/10 px-3 py-1.5 rounded-lg flex items-center">Log Mood</Link>
          </div>
          <div className="flex items-center justify-center p-6 bg-rose-50/50 rounded-2xl border border-rose-100/50">
             <p className="text-sm text-textLight">View and log moods in the tracker.</p>
          </div>
        </section>

        {/* Next Event Widget */}
        <section className="bg-surface border border-rose-100 p-5 rounded-3xl shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xs font-semibold text-textLight uppercase tracking-wider flex items-center gap-2">
              <Calendar size={16} className="text-blue-500"/> Next Event
            </h2>
            <Link to="/calendar" className="text-xs font-medium text-blue-500 hover:underline bg-blue-500/10 px-3 py-1.5 rounded-lg">View All</Link>
          </div>
          <div className="flex items-center gap-4 bg-blue-50/50 p-4 rounded-2xl border border-blue-100/50">
            <div className="bg-white px-3 py-2 rounded-xl text-center shadow-sm border border-blue-100">
              <p className="text-[10px] font-bold text-blue-500 uppercase tracking-wider">Plan</p>
              <p className="text-lg font-medium text-text leading-none mt-1">!</p>
            </div>
            <div>
              <p className="font-medium text-text text-sm">Shared Calendar</p>
              <p className="text-xs text-textLight mt-1">Tap View All to manage events</p>
            </div>
          </div>
        </section>

        {/* Recent Note Widget */}
        <section className="bg-surface border border-rose-100 p-5 rounded-3xl shadow-sm">
           <div className="flex items-center justify-between mb-4">
            <h2 className="text-xs font-semibold text-textLight uppercase tracking-wider flex items-center gap-2">
              <MessageSquare size={16} className="text-amber-500"/> Recent Note
            </h2>
            <Link to="/notes" className="text-xs font-medium text-amber-500 hover:underline bg-amber-500/10 px-3 py-1.5 rounded-lg">Inbox</Link>
          </div>
          <div className="p-4 bg-amber-50/50 rounded-2xl border border-amber-100/50 relative">
             <div className="w-1.5 h-full absolute left-0 top-0 bg-amber-300 rounded-l-2xl"></div>
             <p className="text-sm text-text italic">"Don't forget to check your pocket notes!"</p>
          </div>
        </section>

        {/* Double Widget Grid */}
        <div className="grid grid-cols-2 gap-4">
          <section className="bg-purple-500/5 border border-purple-500/20 p-5 rounded-3xl shadow-sm flex flex-col justify-between h-32 relative overflow-hidden group hover:bg-purple-500/10 transition-colors">
            <div className="absolute right-[-10px] top-[-10px] text-purple-500 opacity-20 group-hover:opacity-40 transition-opacity"><ImageIcon size={80} /></div>
            <div className="z-10 relative">
              <h2 className="text-sm font-semibold text-purple-600 mb-1">Memories</h2>
              <p className="text-[11px] text-textLight">Photos</p>
            </div>
            <Link to="/photos" className="text-xs font-medium text-purple-600 hover:underline z-10">Gallery &rarr;</Link>
          </section>

          <section className="bg-amber-500/5 border border-amber-500/20 p-5 rounded-3xl shadow-sm flex flex-col justify-between h-32 relative overflow-hidden group hover:bg-amber-500/10 transition-colors">
            <div className="absolute right-[-10px] top-[-10px] text-amber-500 opacity-20 group-hover:opacity-40 transition-opacity"><TrendingUp size={80} /></div>
            <div className="z-10 relative">
              <h2 className="text-sm font-semibold text-amber-600 mb-1">Analytics</h2>
              <p className="text-[11px] text-textLight">Premium</p>
            </div>
            <Link to="/analytics" className="text-xs font-medium text-amber-600 hover:underline z-10">Insights &rarr;</Link>
          </section>
        </div>

        {/* Engagement Grid */}
        <div className="grid grid-cols-2 gap-4">
          <Link to="/asks" className="bg-primary/5 border border-primary/20 p-5 rounded-3xl shadow-sm flex flex-col justify-between h-32 relative overflow-hidden group hover:bg-primary/10 transition-colors">
             <div className="absolute right-[-10px] top-[-10px] text-primary opacity-20 group-hover:opacity-40 transition-opacity"><Bell size={80} /></div>
             <div className="z-10 relative">
               <h2 className="text-sm font-semibold text-primary mb-1">Make a Request</h2>
               <p className="text-[11px] text-textLight">Send a nudge</p>
             </div>
          </Link>

          <Link to="/todos" className="bg-emerald-500/5 border border-emerald-500/20 p-5 rounded-3xl shadow-sm flex flex-col justify-between h-32 relative overflow-hidden group hover:bg-emerald-500/10 transition-colors">
             <div className="absolute right-[-10px] top-[-10px] text-emerald-500 opacity-20 group-hover:opacity-40 transition-opacity"><CheckCircle size={80} /></div>
             <div className="z-10 relative">
               <h2 className="text-sm font-semibold text-emerald-600 mb-1">Shared To-Dos</h2>
               <p className="text-[11px] text-textLight">Manage tasks</p>
             </div>
          </Link>

          <Link to="/location" className="bg-sky-500/5 border border-sky-500/20 p-5 rounded-3xl shadow-sm flex flex-col justify-between h-32 relative overflow-hidden group hover:bg-sky-500/10 transition-colors">
             <div className="absolute right-[-10px] top-[-10px] text-sky-500 opacity-20 group-hover:opacity-40 transition-opacity"><MapPin size={80} /></div>
             <div className="z-10 relative">
               <h2 className="text-sm font-semibold text-sky-600 mb-1">Location Map</h2>
               <p className="text-[11px] text-textLight">Check-ins</p>
             </div>
          </Link>

          <div
             onClick={async () => {
                if(!userData?.coupleId) return;
                await addDoc(collection(db, 'asks'), {
                  coupleId: userData.coupleId,
                  fromUserId: auth.currentUser.uid,
                  toUserId: userData.partnerId,
                  requestText: 'Sent you a Poke! 👉',
                  requestType: 'Poke',
                  status: 'pending',
                  responseText: '',
                  createdAt: new Date().toISOString(),
                  respondedAt: null
                });
                alert('Poke sent!');
             }}
             className="bg-indigo-500/5 border border-indigo-500/20 p-5 rounded-3xl shadow-sm flex flex-col justify-between h-32 relative overflow-hidden group hover:bg-indigo-500/10 transition-colors cursor-pointer"
          >
             <div className="absolute right-[-12px] top-[-12px] text-indigo-500 opacity-20 group-hover:opacity-40 transition-opacity"><User size={80} /></div>
             <div className="z-10 relative pointer-events-none">
               <h2 className="text-sm font-semibold text-indigo-600 mb-1">Send a Poke</h2>
               <p className="text-[11px] text-textLight">Instant tap</p>
             </div>
          </div>
        </div>

      </main>
    </div>
  );
}
