import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { db } from '../firebase';
import { collection, query, where, orderBy, onSnapshot } from 'firebase/firestore';
import { ArrowLeft, TrendingUp, Lock } from 'lucide-react';
import { Link } from 'react-router-dom';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';

export default function Analytics() {
  const { currentUser, userData } = useAuth();
  const [moodData, setMoodData] = useState([]);

  useEffect(() => {
    if (!userData?.coupleId) return;
    
    const q = query(
      collection(db, 'moods'),
      where('coupleId', '==', userData.coupleId),
      orderBy('createdAt', 'asc')
    );

    const unsub = onSnapshot(q, snap => {
      // Process data for charts
      // In the db, moods have userId, moodValue (1-5), createdAt
      const processed = [];
      const groupedByDay = {};

      snap.docs.forEach(d => {
        const data = d.data();
        const dateStr = new Date(data.createdAt).toLocaleDateString([], { month: 'short', day: 'numeric' });
        
        if (!groupedByDay[dateStr]) groupedByDay[dateStr] = { date: dateStr };
        
        if (data.userId === currentUser.uid) {
           groupedByDay[dateStr].me = data.moodValue;
        } else {
           groupedByDay[dateStr].partner = data.moodValue;
        }
      });

      for (const key in groupedByDay) {
         processed.push(groupedByDay[key]);
      }
      setMoodData(processed);
    });
    return unsub;
  }, [userData]);

  if (!userData?.isPremium) {
     return (
       <div className="min-h-screen bg-background flex flex-col items-center justify-center p-6 text-center">
         <div className="w-20 h-20 bg-amber-100 rounded-full flex items-center justify-center mb-6 shadow-sm border border-amber-500/20">
            <Lock size={32} className="text-amber-500" />
         </div>
         <h1 className="text-2xl font-bold text-text mb-2">Premium Feature</h1>
         <p className="text-textLight max-w-sm mb-8 text-sm leading-relaxed">Upgrade to Premium to unlock full Graph Analytics and see how your emotional trends as a couple evolve over time.</p>
         <Link to="/" className="px-8 py-4 bg-gradient-to-r from-amber-400 to-amber-500 text-white rounded-2xl font-bold shadow-lg shadow-amber-500/30 hover:shadow-xl transition-all hover:-translate-y-1">Return Home</Link>
       </div>
     );
  }

  return (
    <div className="min-h-screen bg-background pb-20">
      <header className="bg-surface border-b border-rose-100 p-4 sticky top-0 z-10 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-4">
          <Link to="/" className="p-2 -ml-2 text-textLight hover:text-rose-500 rounded-full hover:bg-rose-50 transition-colors">
            <ArrowLeft size={20} />
          </Link>
          <h1 className="text-xl font-medium text-text">Analytics</h1>
        </div>
      </header>

      <main className="p-4 max-w-lg mx-auto mt-4 space-y-6">
         <section className="bg-surface border border-rose-100 p-5 rounded-3xl shadow-sm">
           <div className="flex items-center gap-2 mb-6">
             <TrendingUp size={20} className="text-amber-500" />
             <h2 className="text-sm font-semibold text-textLight uppercase tracking-wider">Mood Timeline</h2>
           </div>
           
           <div className="w-full h-64 bg-background rounded-xl p-2 border border-rose-50 -ml-2">
             {moodData.length > 0 ? (
               <ResponsiveContainer width="100%" height="100%">
                 <LineChart data={moodData} margin={{ top: 5, right: 10, left: -20, bottom: 0 }}>
                   <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#fecdd3" opacity={0.5} />
                   <XAxis dataKey="date" tick={{fontSize: 10, fill: '#fda4af'}} axisLine={false} tickLine={false} />
                   <YAxis tick={{fontSize: 10, fill: '#fda4af'}} axisLine={false} tickLine={false} domain={[1, 5]} ticks={[1,2,3,4,5]} />
                   <Tooltip 
                     contentStyle={{ borderRadius: '12px', border: '1px solid #ffe4e6', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                     labelStyle={{ color: '#881337', fontWeight: 600, marginBottom: '4px' }}
                   />
                   <Legend iconType="circle" wrapperStyle={{ fontSize: '12px', paddingTop: '10px' }}/>
                   <Line type="monotone" name="Me" dataKey="me" stroke="#f43f5e" strokeWidth={3} dot={{r: 4, fill: '#f43f5e', strokeWidth: 2, stroke: '#fff'}} activeDot={{ r: 6 }} connectNulls />
                   <Line type="monotone" name="Partner" dataKey="partner" stroke="#6366f1" strokeWidth={3} dot={{r: 4, fill: '#6366f1', strokeWidth: 2, stroke: '#fff'}} activeDot={{ r: 6 }} connectNulls />
                 </LineChart>
               </ResponsiveContainer>
             ) : (
                <div className="w-full h-full flex flex-col items-center justify-center text-textLight">
                  <p className="text-sm">Not enough mood data yet.</p>
                </div>
             )}
           </div>
         </section>
      </main>
    </div>
  );
}
