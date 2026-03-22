import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { db } from '../firebase';
import { collection, addDoc, query, where, orderBy, onSnapshot, limit, deleteDoc, doc } from 'firebase/firestore';
import { ArrowLeft, MapPin, Navigation2, ShieldAlert } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Geolocation } from '@capacitor/geolocation';

export default function LocationShare() {
  const { currentUser, userData } = useAuth();
  const [partnerLocation, setPartnerLocation] = useState(null);
  const [myLocation, setMyLocation] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!userData?.coupleId) return;
    
    // Listen for partner's last location
    const qPartner = query(
      collection(db, 'locationShares'),
      where('coupleId', '==', userData.coupleId),
      where('userId', '==', userData.partnerId),
      orderBy('createdAt', 'desc'),
      limit(1)
    );
    const unsubPartner = onSnapshot(qPartner, snap => {
      if (!snap.empty) setPartnerLocation(snap.docs[0].data());
    });

    // Listen for my last location
    const qMe = query(
      collection(db, 'locationShares'),
      where('coupleId', '==', userData.coupleId),
      where('userId', '==', currentUser.uid),
      orderBy('createdAt', 'desc'),
      limit(1)
    );
    const unsubMe = onSnapshot(qMe, snap => {
      if (!snap.empty) setMyLocation({ id: snap.docs[0].id, ...snap.docs[0].data() });
      else setMyLocation(null);
    });

    return () => { unsubPartner(); unsubMe(); };
  }, [userData]);

  if (!userData?.isPremium) {
     return (
       <div className="min-h-screen bg-background flex flex-col items-center justify-center p-6 text-center">
         <div className="w-20 h-20 bg-sky-100 rounded-full flex items-center justify-center mb-6 shadow-sm border border-sky-500/20">
            <ShieldAlert size={32} className="text-sky-500" />
         </div>
         <h1 className="text-2xl font-bold text-text mb-2">Secure Feature</h1>
         <p className="text-textLight max-w-sm mb-8 text-sm leading-relaxed">Upgrade to Premium to unlock Location Check-ins. Share coordinates safely with your partner over encrypted, highly-permissioned channels.</p>
         <Link to="/" className="px-8 py-4 bg-gradient-to-r from-sky-400 to-sky-500 text-white rounded-2xl font-bold shadow-lg shadow-sky-500/30 hover:shadow-xl transition-all hover:-translate-y-1">Return Home</Link>
       </div>
     );
  }

  const shareLocation = async () => {
    setLoading(true);
    try {
      // 1. Check Native Plugin Permissions
      let permStatus = { location: 'prompt' };
      try {
        permStatus = await Geolocation.checkPermissions();
      } catch (e) {
        // Ignored, means we are not on native or plugin not fully loaded
      }

      if (permStatus.location === 'prompt' || permStatus.location === 'prompt-with-rationale') {
        try {
          permStatus = await Geolocation.requestPermissions();
        } catch (e) {
          throw new Error('Could not request location permissions natively.');
        }
      }

      if (permStatus.location === 'denied') {
        throw new Error('Location permission denied. Please enable in device settings.');
      }

      // 2. Get coords
      const coordinates = await Geolocation.getCurrentPosition({ enableHighAccuracy: true, timeout: 10000 });
      
      await addDoc(collection(db, 'locationShares'), {
        coupleId: userData.coupleId,
        userId: currentUser.uid,
        latitude: coordinates.coords.latitude,
        longitude: coordinates.coords.longitude,
        accuracy: coordinates.coords.accuracy,
        createdAt: new Date().toISOString()
      });
      
    } catch (err) {
      console.warn('Native geolocation failed, trying web fallback:', err);
      
      // Fallback if Capacitor fails (e.g., in standard web browser without HTTPS or missing plugin bridge)
      if (navigator.geolocation) {
         navigator.geolocation.getCurrentPosition(async (pos) => {
            try {
              await addDoc(collection(db, 'locationShares'), {
                coupleId: userData.coupleId,
                userId: currentUser.uid,
                latitude: pos.coords.latitude,
                longitude: pos.coords.longitude,
                accuracy: pos.coords.accuracy,
                createdAt: new Date().toISOString()
              });
            } catch (dbErr) {
              alert('Location found, but could not save (offline?). ' + dbErr.message);
            }
         }, (err) => alert('Could not get location: ' + err.message));
      } else {
         alert(err.message || 'Geolocation is not supported by your device.');
      }
    } finally {
      setLoading(false);
    }
  };

  const stopSharing = async () => {
    if (myLocation?.id) {
       await deleteDoc(doc(db, 'locationShares', myLocation.id));
    }
  };

  return (
    <div className="min-h-screen bg-background pb-20">
      <header className="bg-surface border-b border-sky-100 p-4 sticky top-0 z-10 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-4">
          <Link to="/" className="p-2 -ml-2 text-textLight hover:text-sky-500 rounded-full hover:bg-sky-50 transition-colors">
            <ArrowLeft size={20} />
          </Link>
          <h1 className="text-xl font-medium text-text">Location</h1>
        </div>
      </header>

      <main className="p-4 max-w-lg mx-auto mt-4 space-y-6">
         <section className="bg-sky-50 border border-sky-100 p-5 rounded-3xl shadow-sm">
           <div className="flex items-start gap-3 mb-4">
             <ShieldAlert size={24} className="text-sky-500 shrink-0 mt-0.5" />
             <div>
               <h2 className="text-sm font-semibold text-sky-800 mb-1">Privacy First</h2>
               <p className="text-xs text-sky-600/80 leading-relaxed">Your location is never shared automatically. Tap 'Share Check-in' to explicitly send your current coordinates to your partner.</p>
             </div>
           </div>
           
           <div className="flex gap-2">
             <button 
               onClick={shareLocation} 
               disabled={loading}
               className="flex-1 py-3 bg-sky-500 text-white rounded-xl font-medium text-sm flex items-center justify-center gap-2 hover:bg-sky-600 transition-colors shadow-sm disabled:opacity-50"
             >
               <Navigation2 size={18} /> {loading ? 'Locating...' : 'Share Check-in'}
             </button>
             {myLocation && (
               <button onClick={stopSharing} className="px-4 py-3 bg-white text-sky-500 rounded-xl font-medium text-sm border border-sky-200 hover:bg-sky-50 transition-colors">
                 Clear
               </button>
             )}
           </div>
         </section>

         <section className="space-y-4">
           <h3 className="text-xs font-semibold text-textLight uppercase tracking-wider px-2">Partner's Location</h3>
           {partnerLocation ? (
             <div className="bg-surface p-5 rounded-3xl border border-rose-100 shadow-sm flex items-center gap-4">
               <div className="w-12 h-12 bg-rose-50 rounded-full flex items-center justify-center text-rose-500 shrink-0">
                 <MapPin size={24} />
               </div>
               <div>
                 <p className="font-semibold text-text text-sm">Last known check-in</p>
                 <p className="text-[10px] text-textLight my-1 font-mono">
                   {partnerLocation.latitude.toFixed(4)}, {partnerLocation.longitude.toFixed(4)}
                 </p>
                 <p className="text-[10px] uppercase font-bold text-rose-400 tracking-wider">
                    {new Date(partnerLocation.createdAt).toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute:'2-digit' })}
                 </p>
               </div>
               <a 
                 href={`https://maps.google.com/?q=${partnerLocation.latitude},${partnerLocation.longitude}`}
                 target="_blank"
                 rel="noreferrer"
                 className="ml-auto p-3 bg-rose-500 text-white rounded-full shadow-md hover:scale-105 transition-transform"
               >
                 <Navigation2 size={16} />
               </a>
             </div>
           ) : (
             <div className="bg-surface p-5 rounded-3xl border border-rose-100 border-dashed text-center">
                <p className="text-sm text-textLight">Your partner hasn't checked in recently.</p>
             </div>
           )}
         </section>
      </main>
    </div>
  );
}
