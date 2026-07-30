import React, { useEffect, useState } from 'react';
import { Bell, RefreshCw } from 'lucide-react';
import { api } from '../api';
import { MOCK_NOTIFICATIONS } from '../data/mockOrders';

export default function NotificationPanel() {
  const [notifications, setNotifications] = useState([]);
  const [error, setError] = useState('');
  const loadNotifications = async () => {
    try { const result = await api.notifications.getAll(); const items = result?.data || result || []; setNotifications(items.length ? items : MOCK_NOTIFICATIONS); setError(''); }
    catch { setNotifications(MOCK_NOTIFICATIONS); setError(''); }
  };
  useEffect(() => { loadNotifications(); }, []);
  const markRead = async (notification) => {
    const id = notification.id || notification.notificationId;
    setNotifications((previous) => previous.map((item) => (item.id || item.notificationId) === id ? { ...item, read: true, isRead: true } : item));
    try { await api.notifications.markRead(id); } catch { /* Local read state is enough for the demo. */ }
  };
  return <section className="glass-card" style={{ padding: '1rem', marginBottom: '1.5rem' }}>
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}><h3 style={{ margin: 0, fontSize: '1rem' }}><Bell size={16} style={{ verticalAlign: 'middle', marginRight: '6px' }} />Order Notifications</h3><button className="btn btn-secondary" onClick={loadNotifications} style={{ padding: '0.25rem 0.45rem' }}><RefreshCw size={14} /></button></div>
    {error ? <p style={{ color: '#ef4444', fontSize: '0.82rem' }}>{error}</p> : notifications.slice(0, 4).map((notification) => <button key={notification.id || notification.notificationId} onClick={() => markRead(notification)} style={{ display: 'block', width: '100%', textAlign: 'left', marginTop: '8px', padding: '8px', background: notification.read || notification.isRead ? 'transparent' : 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '6px', color: 'var(--text-primary)', cursor: 'pointer' }}>{notification.message || notification.title || 'Order update'}</button>)}
  </section>;
}
