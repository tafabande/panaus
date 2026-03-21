import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { db } from '../firebase';
import { collection, addDoc, query, where, orderBy, onSnapshot, doc, updateDoc, deleteDoc } from 'firebase/firestore';
import { ArrowLeft, CheckCircle2, Circle, Trash2, Plus, User } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function Todos() {
  const { currentUser, userData } = useAuth();
  const [todos, setTodos] = useState([]);
  const [title, setTitle] = useState('');
  const [assignedTo, setAssignedTo] = useState('unassigned');
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);

  useEffect(() => {
    if (!userData?.coupleId) return;

    const q = query(
      collection(db, 'todos'),
      where('coupleId', '==', userData.coupleId),
      orderBy('createdAt', 'desc')
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const fetchedTodos = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
      setTodos(fetchedTodos);
    });

    return unsubscribe;
  }, [userData]);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!title.trim()) return;
    setLoading(true);
    
    try {
      const todoData = {
        coupleId: userData.coupleId,
        title: title.trim(),
        assignedTo, // 'unassigned', currentUser.uid, or userData.partnerId
        isCompleted: false,
        createdBy: currentUser.uid,
        createdAt: new Date().toISOString(),
        completedAt: null
      };
      
      await addDoc(collection(db, 'todos'), todoData);
      setTitle('');
      setAssignedTo('unassigned');
      setShowForm(false);
    } catch (err) {
      console.error('Error creating todo', err);
    } finally {
      setLoading(false);
    }
  };

  const toggleComplete = async (todo) => {
    try {
      await updateDoc(doc(db, 'todos', todo.id), {
        isCompleted: !todo.isCompleted,
        completedAt: !todo.isCompleted ? new Date().toISOString() : null
      });
    } catch (err) {
      console.error('Error toggling todo', err);
    }
  };

  const deleteTodo = async (id) => {
    try {
      await deleteDoc(doc(db, 'todos', id));
    } catch (err) {
      console.error('Error deleting todo', err);
    }
  };

  const activeTodos = todos.filter(t => !t.isCompleted);
  const completedTodos = todos.filter(t => t.isCompleted);

  return (
    <div className="min-h-screen bg-background pb-20">
      <header className="bg-surface border-b border-rose-100 p-4 sticky top-0 z-10 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-4">
          <Link to="/" className="p-2 -ml-2 text-textLight hover:text-rose-500 rounded-full hover:bg-rose-50 transition-colors">
            <ArrowLeft size={20} />
          </Link>
          <h1 className="text-xl font-medium text-text">Shared To-Dos</h1>
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
          <section className="bg-surface border border-rose-100 p-5 rounded-3xl shadow-sm animate-in slide-in-from-top-4 fade-in duration-200">
             <form onSubmit={handleCreate} className="space-y-4">
               <div>
                 <label className="block text-xs font-semibold text-textLight uppercase tracking-wider mb-1 ml-1">Task</label>
                 <input 
                   type="text" 
                   value={title} 
                   onChange={e=>setTitle(e.target.value)} 
                   required 
                   className="w-full p-3 rounded-xl bg-background border border-rose-100 focus:outline-none focus:border-primary text-text text-sm" 
                   placeholder="e.g. Buy groceries"
                 />
               </div>
               <div>
                  <label className="block text-xs font-semibold text-textLight uppercase tracking-wider mb-1 ml-1">Assign to</label>
                  <select 
                    value={assignedTo} 
                    onChange={e=>setAssignedTo(e.target.value)} 
                    className="w-full p-3 rounded-xl bg-background border border-rose-100 focus:outline-none focus:border-primary text-sm text-text"
                  >
                    <option value="unassigned">Anyone</option>
                    <option value={currentUser.uid}>Me</option>
                    <option value={userData.partnerId}>Partner</option>
                  </select>
               </div>
               
               <button type="submit" disabled={loading || !title.trim()} className="w-full py-3 mt-4 bg-primary text-white rounded-xl font-medium text-sm hover:bg-primary/90 transition-colors disabled:opacity-50 shadow-md">
                 Add Task
               </button>
             </form>
          </section>
        )}

        <section className="space-y-3">
          {activeTodos.length === 0 && completedTodos.length === 0 && !showForm && (
             <div className="text-center py-10 bg-surface rounded-3xl border border-rose-100 border-dashed">
                <CheckCircle2 size={32} className="mx-auto text-rose-200 mb-3" />
                <p className="text-sm text-textLight">You're all caught up!</p>
             </div>
          )}

          {activeTodos.map(todo => (
            <div key={todo.id} className="bg-surface p-4 rounded-2xl border border-rose-100 shadow-sm flex items-start gap-3 transition-colors hover:border-primary/30">
               <button onClick={() => toggleComplete(todo)} className="mt-0.5 text-rose-300 hover:text-primary transition-colors shrink-0">
                 <Circle size={22} />
               </button>
               <div className="flex-1">
                 <p className="text-sm font-medium text-text">{todo.title}</p>
                 {todo.assignedTo !== 'unassigned' && (
                   <span className="inline-flex items-center gap-1 mt-1.5 px-2 py-0.5 bg-rose-50 text-[10px] font-bold uppercase tracking-wider text-rose-500 rounded border border-rose-100">
                     <User size={10} /> {todo.assignedTo === currentUser.uid ? 'Me' : 'Partner'}
                   </span>
                 )}
               </div>
               <button onClick={() => deleteTodo(todo.id)} className="text-rose-200 hover:text-rose-500 transition-colors p-1 shrink-0">
                 <Trash2 size={16} />
               </button>
            </div>
          ))}

          {completedTodos.length > 0 && (
            <div className="pt-4 mt-6 border-t border-rose-100/50 space-y-3">
               <h3 className="text-xs font-semibold text-textLight uppercase tracking-wider mb-2 px-2">Completed</h3>
               {completedTodos.map(todo => (
                 <div key={todo.id} className="bg-surface/50 p-4 rounded-2xl border border-rose-50 flex items-start gap-3 opacity-60">
                    <button onClick={() => toggleComplete(todo)} className="mt-0.5 text-emerald-500 hover:text-emerald-600 transition-colors shrink-0">
                      <CheckCircle2 size={22} />
                    </button>
                    <div className="flex-1">
                      <p className="text-sm font-medium text-textLight line-through">{todo.title}</p>
                    </div>
                    <button onClick={() => deleteTodo(todo.id)} className="text-rose-200 hover:text-rose-500 transition-colors p-1 shrink-0">
                      <Trash2 size={16} />
                    </button>
                 </div>
               ))}
            </div>
          )}
        </section>
      </main>
    </div>
  );
}
