import React, { useEffect, useState } from 'react';
import { X, MapPin, Truck } from 'lucide-react';
import { api } from '../api';
import ProductImage from './ProductImage';
import OrderTimeline from './OrderTimeline';
import StatusBadge from './StatusBadge';

export default function OrderDetailsModal({ order, onClose }) {
  const [details, setDetails] = useState(order);
  const [timeline, setTimeline] = useState([]);
  const [tracking, setTracking] = useState(null);
  const orderId = order?.orderId || order?.id;
  useEffect(() => {
    if (!orderId) return;
    Promise.all([api.orders.getById(orderId), api.orders.getTimeline(orderId), api.orders.getTracking(orderId)])
      .then(([orderResult, timelineResult, trackingResult]) => { setDetails(orderResult?.data || orderResult || order); setTimeline(timelineResult?.data || timelineResult || []); setTracking(trackingResult?.data || trackingResult || null); })
      .catch(() => { setDetails(order); setTimeline([]); setTracking({ trackingNumber: order.trackingNumber, status: order.status }); });
  }, [order, orderId]);
  if (!order) return null;
  const items = details?.items || [];
 const address = details?.shippingAddress;
  return <div className="modal-overlay" onClick={onClose}><div className="modal-content" onClick={(event) => event.stopPropagation()} style={{ maxWidth: '760px', maxHeight: '85vh', overflowY: 'auto', textAlign: 'left' }}>
    <button className="modal-close" onClick={onClose}><X size={18} /></button>
    <h2 style={{ marginTop: 0 }}>Order #{details?.orderId || details?.id}</h2><StatusBadge status={details?.status} />
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '1rem', margin: '1rem 0' }}>
      <div><strong>Payment</strong><p>{details?.status === 'PENDING' ? 'Pending' : 'Paid'}</p><strong>Estimated delivery</strong><p>{details?.estimatedDelivery ? new Date(details.estimatedDelivery).toLocaleDateString() : '—'}</p></div>
      <div><strong><Truck size={15} style={{ verticalAlign: 'middle' }} /> Tracking</strong><p>{tracking?.trackingNumber || details?.trackingNumber || 'Not available'}</p><p>{tracking?.courierStatus || tracking?.status || 'Awaiting courier update'}</p></div>
      <div><strong><MapPin size={15} style={{ verticalAlign: 'middle' }} /> Shipping address</strong><p>{typeof address === 'string' ? address : [address?.street, address?.city, address?.state, address?.zipCode, address?.country].filter(Boolean).join(', ') || 'Address not available'}</p></div>
    </div>
    <h3>Products</h3>{items.length ? items.map((item, index) => <div key={item.id || item.productId || index} style={{ display: 'flex', gap: '10px', padding: '10px 0', borderTop: '1px solid var(--border-color)' }}><ProductImage src={item.productImage || item.image || item.product?.imageUrl || item.product?.image} alt={item.productName || item.name || item.product?.productName} style={{ width: 54, height: 54, objectFit: 'cover', borderRadius: 6 }} /><div style={{ flex: 1 }}><strong>{item.productName || item.name || item.product?.name || 'Product'}</strong><div style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>Qty {item.quantity || 1} · ${Number(item.price || item.unitPrice || 0).toFixed(2)}</div></div></div>) : <p style={{ color: 'var(--text-muted)' }}>No item details available.</p>}
    <h3 style={{ marginTop: '1.25rem' }}>Delivery timeline</h3><OrderTimeline status={details?.status} timeline={timeline} />
  </div></div>;
}
