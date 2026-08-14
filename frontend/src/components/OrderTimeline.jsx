import React from 'react';
import { CheckCircle2, Circle, XCircle } from 'lucide-react';

const formatStatus = (status) => status.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());

export default function OrderTimeline({ status, timeline = [] }) {
  const normalizedStatus = String(status || 'PENDING').toUpperCase().replace(/ /g, '_');

  let steps = ['PENDING', 'CONFIRMED', 'PACKING', 'READY_FOR_PICKUP', 'SHIPPED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED'];
  
  if (normalizedStatus === 'CANCELLED' || normalizedStatus === 'FAILED' || normalizedStatus === 'PAYMENT_FAILED') {
    steps = ['PENDING', 'CANCELLED'];
  } else if (['RETURN_REQUESTED', 'RETURN_APPROVED', 'RETURN_PICKED', 'RETURN_RECEIVED', 'REFUND_INITIATED', 'REFUND_COMPLETED', 'RETURNED', 'REFUNDED'].includes(normalizedStatus)) {
    steps = ['DELIVERED', 'RETURN_REQUESTED', 'RETURN_APPROVED', 'RETURN_PICKED', 'RETURN_RECEIVED', 'REFUND_COMPLETED'];
  }

  const getIndex = (curr) => {
    if (steps.includes(curr)) return steps.indexOf(curr);
    
    // Synonym mapping
    if (curr === 'PAYMENT_PENDING') return 0;
    if (curr === 'PAYMENT_SUCCESS') return 1;
    if (curr === 'PROCESSING') return 2;
    if (curr === 'RETURNED') return 1;
    if (curr === 'REFUNDED') return 5;
    return 0;
  };

  const currentIndex = getIndex(normalizedStatus);

  return (
    <div style={{ display: 'grid', gap: '10px' }}>
      {steps.map((step, index) => {
        const event = timeline.find((item) => String(item.status || '').toUpperCase().replace(/ /g, '_') === step);
        const completed = currentIndex >= index;
        const current = currentIndex === index;
        
        let icon = <Circle size={18} color="var(--text-muted)" />;
        if (completed) {
          if (step === 'CANCELLED' || step === 'FAILED') {
            icon = <XCircle size={18} color="var(--danger)" />;
          } else {
            icon = <CheckCircle2 size={18} color={current ? 'var(--secondary)' : '#10b981'} />;
          }
        }

        return (
          <div key={step} style={{ display: 'flex', gap: '10px', alignItems: 'center', opacity: completed ? 1 : 0.45, transition: 'opacity 180ms ease' }}>
            {icon}
            <div>
              <strong style={{ color: current ? 'var(--secondary)' : 'var(--text-primary)' }}>{formatStatus(step)}</strong>
              {event?.timestamp && !isNaN(new Date(event.timestamp)) && (
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{new Date(event.timestamp).toLocaleString()}</div>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}
