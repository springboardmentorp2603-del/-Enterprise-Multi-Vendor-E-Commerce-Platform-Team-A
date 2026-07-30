import React from 'react';
import { CheckCircle2, Circle } from 'lucide-react';

const steps = ['PENDING', 'CONFIRMED', 'PACKED', 'SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED'];
const formatStatus = (status) => status.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());

export default function OrderTimeline({ status, timeline = [] }) {
  const currentIndex = steps.indexOf(String(status || 'PENDING').toUpperCase().replace(/ /g, '_'));
  return <div style={{ display: 'grid', gap: '10px' }}>{steps.map((step, index) => {
    const event = timeline.find((item) => String(item.status || '').toUpperCase().replace(/ /g, '_') === step);
    const completed = currentIndex >= index;
    const current = currentIndex === index;
    return <div key={step} style={{ display: 'flex', gap: '10px', alignItems: 'center', opacity: completed ? 1 : 0.45, transition: 'opacity 180ms ease' }}>
      {completed ? <CheckCircle2 size={18} color={current ? 'var(--secondary)' : '#10b981'} /> : <Circle size={18} color="var(--text-muted)" />}
      <div><strong style={{ color: current ? 'var(--secondary)' : 'var(--text-primary)' }}>{formatStatus(step)}</strong>{event?.timestamp && !isNaN(new Date(event.timestamp)) && <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{new Date(event.timestamp).toLocaleString()}</div>}</div>
    </div>;
  })}</div>;
}
