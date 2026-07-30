import React, { useState } from 'react';
import { Download } from 'lucide-react';
import { api } from '../api';

export default function InvoiceButton({ order }) {
  const [loading, setLoading] = useState(false);

  const downloadInvoice = async () => {
    const orderId = order.orderId || order.id;
    setLoading(true);
    try {
      const invoice = await api.invoices.getByOrder(orderId);
      if (invoice && invoice.pdfPath) {
        window.open(`/${invoice.pdfPath}`, '_blank');
      } else {
        alert('Invoice not available yet for this order.');
      }
    } catch (err) {
      alert('Could not load invoice: ' + (err.message || 'unknown error'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <button className="btn btn-secondary" onClick={downloadInvoice} disabled={loading} style={{ padding: '0.35rem 0.65rem', fontSize: '0.8rem' }}>
      <Download size={14} /> {loading ? 'Loading...' : 'Invoice'}
    </button>
  );
}