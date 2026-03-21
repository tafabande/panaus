import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { db, storage } from '../firebase';
import { collection, addDoc, query, where, orderBy, onSnapshot, doc, updateDoc, arrayUnion, arrayRemove } from 'firebase/firestore';
import { ref, uploadString, getDownloadURL } from 'firebase/storage';
import { ArrowLeft, Image as ImageIcon, Heart, UploadCloud } from 'lucide-react';
import { Link } from 'react-router-dom';
import Resizer from 'react-image-file-resizer';

export default function Photos() {
  const { currentUser, userData } = useAuth();
  const [photos, setPhotos] = useState([]);
  const [loading, setLoading] = useState(false);
  const fileInputRef = useRef(null);

  useEffect(() => {
    if (!userData?.coupleId) return;
    const q = query(
      collection(db, 'photos'),
      where('coupleId', '==', userData.coupleId),
      orderBy('createdAt', 'desc')
    );
    const unsub = onSnapshot(q, snap => {
      setPhotos(snap.docs.map(d => ({ id: d.id, ...d.data() })));
    });
    return unsub;
  }, [userData]);

  const resizeFile = (file) => new Promise(resolve => {
    Resizer.imageFileResizer(
      file,
      800, // max width
      800, // max height
      'JPEG', // compress format
      80, // quality (0-100)
      0, // rotation
      (uri) => {
        resolve(uri);
      },
      'base64' // output type
    );
  });

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    
    setLoading(true);
    try {
      const base64Image = await resizeFile(file);
      
      // Upload to Storage
      const imageRef = ref(storage, `couples/${userData.coupleId}/photos/${Date.now()}_${currentUser.uid}.jpeg`);
      await uploadString(imageRef, base64Image, 'data_url');
      const downloadURL = await getDownloadURL(imageRef);
      
      // Save to Firestore
      await addDoc(collection(db, 'photos'), {
        coupleId: userData.coupleId,
        uploadedBy: currentUser.uid,
        imageUrl: downloadURL,
        reactions: [],
        createdAt: new Date().toISOString()
      });
      
    } catch (err) {
      console.error('Upload failed:', err);
    } finally {
      setLoading(false);
      e.target.value = null;
    }
  };

  const toggleReaction = async (photoId, currentReactions) => {
    const hasReacted = currentReactions.includes(currentUser.uid);
    const photoRef = doc(db, 'photos', photoId);
    
    if (hasReacted) {
      await updateDoc(photoRef, { reactions: arrayRemove(currentUser.uid) });
    } else {
      await updateDoc(photoRef, { reactions: arrayUnion(currentUser.uid) });
    }
  };

  return (
    <div className="min-h-screen bg-background pb-20">
      <header className="bg-surface border-b border-rose-100 p-4 sticky top-0 z-10 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-4">
          <Link to="/" className="p-2 -ml-2 text-textLight hover:text-rose-500 rounded-full hover:bg-rose-50 transition-colors">
            <ArrowLeft size={20} />
          </Link>
          <h1 className="text-xl font-medium text-text">Our Gallery</h1>
        </div>
        
        <div>
           <input 
             type="file" 
             accept="image/*" 
             ref={fileInputRef} 
             className="hidden" 
             onChange={handleFileUpload}
           />
           <button 
             disabled={loading}
             onClick={() => fileInputRef.current.click()}
             className="flex items-center gap-2 text-xs font-semibold px-4 py-2 bg-primary text-white rounded-xl shadow-sm hover:bg-primary/90 transition-colors disabled:opacity-50"
           >
             <UploadCloud size={16} /> {loading ? 'Uploading...' : 'Upload'}
           </button>
        </div>
      </header>

      <main className="p-4 max-w-lg mx-auto mt-4 space-y-6">
         {photos.length === 0 ? (
           <div className="text-center py-16 bg-surface rounded-3xl border border-rose-100 border-dashed">
              <ImageIcon size={40} className="mx-auto text-rose-200 mb-4" />
              <p className="text-sm font-medium text-text">No memories yet.</p>
              <p className="text-xs text-textLight mt-1">Upload an image to start your gallery.</p>
           </div>
         ) : (
           <div className="grid grid-cols-1 gap-6">
             {photos.map(photo => {
               const reactionCount = photo.reactions?.length || 0;
               const hasReacted = photo.reactions?.includes(currentUser.uid);
               
               return (
                 <div key={photo.id} className="bg-surface rounded-3xl overflow-hidden border border-rose-100 shadow-sm transition-transform duration-300 hover:shadow-md">
                   <div className="w-full aspect-square bg-gray-100 relative group">
                     {/* Image */}
                     <img 
                       src={photo.imageUrl} 
                       alt="Memory" 
                       className="w-full h-full object-cover"
                       loading="lazy"
                     />
                   </div>
                   <div className="p-4 flex items-center justify-between">
                     <div className="flex items-center gap-3">
                       <button 
                         onClick={() => toggleReaction(photo.id, photo.reactions || [])}
                         className={`p-2 rounded-full transition-all flex items-center gap-1.5 ${hasReacted ? 'bg-rose-50 text-rose-500' : 'bg-gray-50 text-gray-400 hover:text-rose-400'}`}
                       >
                         <Heart size={20} className={hasReacted ? 'fill-rose-500' : ''} />
                         {reactionCount > 0 && <span className="text-xs font-bold">{reactionCount}</span>}
                       </button>
                     </div>
                     <span className="text-[10px] text-textLight font-medium uppercase tracking-widest">
                       {new Date(photo.createdAt).toLocaleDateString([], { month: 'short', day: 'numeric' })}
                     </span>
                   </div>
                 </div>
               )
             })}
           </div>
         )}
      </main>
    </div>
  );
}
