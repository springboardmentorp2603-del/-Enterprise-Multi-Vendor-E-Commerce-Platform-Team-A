import React from 'react';

export default function EmptyState({ message = 'No items found.' }) {
  return (
    <div style={{ textAlign: 'center', padding: '2rem', color: '#666' }}>
      <p>{message}</p>
    </div>
  );
}
