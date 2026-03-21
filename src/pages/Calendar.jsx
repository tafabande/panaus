import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { db } from '../firebase';
import { collection, addDoc, query, where, orderBy, onSnapshot } from 'firebase/firestore';
import { ArrowLeft, Plus, Calendar as CalendarIcon, Clock, Tag } from 'lucide-react';
import { Link } from 'react-router-dom';
import { format, parseISO } from 'date-fns';

export default function Calendar() {
  const { currentUser, userData } = useAuth();
  const [events, setEvents] = useState([]);
  const [showForm, setShowForm] = useState(false);
  
  // Form state
  const [title, setTitle] = useState('');
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [category, setCategory] = useState('Date Night');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!userData?.coupleId) return;

    const q = query(
      collection(db, 'events'),
      where('coupleId', '==', userData.coupleId),
      orderBy('date', 'asc')
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const fetchedEvents = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
      setEvents(fetchedEvents);
    });

    return unsubscribe;
  }, [userData]);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!title.trim() || !date) return;

    setLoading(true);
    try {
      const eventData = {
        coupleId: userData.coupleId,
        title: title.trim(),
        date, // YYYY-MM-DD
        time, // HH:MM
        category,
        createdBy: currentUser.uid,
        createdAt: new Date().toISOString(),
      };
      await addDoc(collection(db, 'events'), eventData);
      setTitle('');
      setDate('');
      setTime('');
      setShowForm(false);
    } catch (err) {
      console.error('Error creating event', err);
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
          <h1 className="text-xl font-medium text-text">Shared Calendar</h1>
        </div>
        <button 
          onClick={() => setShowForm(!showForm)}
          className="p-2 bg-rose-50 text-rose-500 rounded-full hover:bg-rose-100 transition-colors"
        >
          <Plus size={20} />
        </button>
      </header>

      <main className="p-4 max-w-lg mx-auto mt-4 space-y-6">
        
        {showForm && (
          <section className="bg-surface border border-rose-100 p-5 rounded-3xl shadow-sm mb-6 animate-in slide-in-from-top-4 fade-in duration-200">
             <h2 className="text-sm font-semibold text-text mb-4">Add New Event</h2>
             <form onSubmit={handleCreate} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-textLight uppercase tracking-wider mb-1 ml-1">Event Title</label>
                  <input type="text" value={title} onChange={e=>setTitle(e.target.value)} required className="w-full p-3 rounded-xl bg-background border border-rose-100 focus:outline-none focus:border-primary transition-colors text-sm" placeholder="Dinner at Luigi's"/>
                </div>
                <div className="flex gap-4">
                  <div className="flex-1">
                    <label className="block text-xs font-semibold text-textLight uppercase tracking-wider mb-1 ml-1">Date</label>
                    <input type="date" value={date} onChange={e=>setDate(e.target.value)} required className="w-full p-3 rounded-xl bg-background border border-rose-100 focus:outline-none focus:border-primary transition-colors text-sm"/>
                  </div>
                  <div className="flex-1">
                    <label className="block text-xs font-semibold text-textLight uppercase tracking-wider mb-1 ml-1">Time <span className="text-[10px] opacity-70">(opt)</span></label>
                    <input type="time" value={time} onChange={e=>setTime(e.target.value)} className="w-full p-3 rounded-xl bg-background border border-rose-100 focus:outline-none focus:border-primary transition-colors text-sm"/>
                  </div>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-textLight uppercase tracking-wider mb-1 ml-1">Category</label>
                  <select value={category} onChange={e=>setCategory(e.target.value)} className="w-full p-3 rounded-xl bg-background border border-rose-100 focus:outline-none focus:border-primary transition-colors text-sm text-text">
                    <option>Date Night</option>
                    <option>Anniversary</option>
                    <option>Birthday</option>
                    <option>Appointment</option>
                    <option>Trip</option>
                    <option>Other</option>
                  </select>
                </div>
                <div className="pt-2 flex gap-3">
                  <button type="button" onClick={() => setShowForm(false)} className="flex-1 py-3 bg-rose-50 text-rose-600 rounded-xl font-medium text-sm hover:bg-rose-100 transition-colors">Cancel</button>
                  <button type="submit" disabled={loading} className="flex-1 py-3 bg-primary text-white rounded-xl font-medium text-sm hover:bg-primary/90 transition-colors disabled:opacity-50">Save Event</button>
                </div>
             </form>
          </section>
        )}

        <section className="space-y-4">
          <h3 className="text-xs font-semibold text-textLight uppercase tracking-wider mb-4 px-2">Upcoming</h3>
          
          {events.length === 0 ? (
            <div className="text-center py-10 bg-surface rounded-3xl border border-rose-100 border-dashed">
               <CalendarIcon size={32} className="mx-auto text-rose-200 mb-3" />
               <p className="text-sm text-textLight">Your calendar is completely empty.</p>
               <button onClick={() => setShowForm(true)} className="mt-4 text-xs font-medium text-primary hover:underline">Add the first event</button>
            </div>
          ) : (
            events.map(event => {
              const eventDate = parseISO(event.date);
              return (
                <div key={event.id} className="flex items-center gap-4 bg-surface p-4 rounded-3xl border border-rose-100 shadow-sm relative overflow-hidden group hover:border-blue-200 transition-colors">
                  <div className="absolute left-0 top-0 bottom-0 w-1.5 bg-blue-400 rounded-l-3xl"></div>
                  
                  <div className="bg-blue-50/50 px-3 py-2 rounded-xl text-center border border-blue-100/50 min-w-[60px]">
                    <p className="text-[10px] font-bold text-blue-500 uppercase tracking-wider">{format(eventDate, 'MMM')}</p>
                    <p className="text-lg font-medium text-text leading-none mt-1">{format(eventDate, 'dd')}</p>
                  </div>
                  
                  <div className="flex-1">
                    <h4 className="font-medium text-text text-sm">{event.title}</h4>
                    <div className="flex items-center gap-3 mt-1.5">
                      {event.time && (
                         <span className="flex items-center gap-1 text-[11px] text-textLight font-medium">
                           <Clock size={12} /> {event.time}
                         </span>
                      )}
                      <span className="flex items-center gap-1 text-[11px] text-textLight font-medium bg-rose-50 px-1.5 py-0.5 rounded text-rose-500">
                        <Tag size={10} /> {event.category}
                      </span>
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </section>
      </main>
    </div>
  );
}
