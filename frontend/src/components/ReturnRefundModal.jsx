import React, { useState } from 'react';
import { createPortal } from 'react-dom';
import { X, Package, AlertTriangle, Tag, RefreshCw, MessageSquare, CreditCard, Gift } from 'lucide-react';
import { api } from '../api';
import ProductImage from './ProductImage';
import './ReturnRefund.css';

// ── Return reasons with icons ────────────────────────────────────────────────
const RETURN_REASONS = [
  { id: 'DEFECTIVE',        label: 'Defective / Damaged',     icon: '🔧' },
  { id: 'WRONG_ITEM',       label: 'Wrong item received',     icon: '📦' },
  { id: 'NOT_AS_DESCRIBED', label: 'Not as described',        icon: '📋' },
  { id: 'CHANGED_MIND',     label: 'Changed my mind',         icon: '💭' },
  { id: 'BETTER_PRICE',     label: 'Found better price',      icon: '💰' },
  { id: 'OTHER',            label: 'Other reason',            icon: '❓' },
];

// ── Refund type options ───────────────────────────────────────────────────────
const REFUND_TYPES = [
  {
    id: 'ORIGINAL_PAYMENT',
    icon: '💳',
    name: 'Original Payment',
    desc: 'Refund to your original payment method (3–7 business days)',
  },
  {
    id: 'STORE_CREDIT',
    icon: '🎁',
    name: 'Store Credit',
    desc: 'Instant credit added to your ShopStack wallet',
  },
];

// ── Step definitions ──────────────────────────────────────────────────────────
const STEPS = ['Select Reason', 'Refund Method', 'Confirm'];

// ── Post-submission timeline ──────────────────────────────────────────────────
const REFUND_TIMELINE = [
  { label: 'Return Requested',     desc: 'Your return has been initiated',               color: '#f97316', done: true  },
  { label: 'Return Under Review',  desc: 'Our team will verify the reason (1–2 days)',   color: '#3b82f6', done: false },
  { label: 'Return Approved',      desc: 'Item pickup or drop-off scheduled',            color: '#8b5cf6', done: false },
  { label: 'Refund Processed',     desc: 'Refund sent to your selected method',          color: '#10b981', done: false },
];

// ── Helpers ───────────────────────────────────────────────────────────────────
const formatDate = (raw) =>
  raw
    ? new Date(raw).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
    : 'N/A';

const getOrderTotal = (order) =>
  Number(order.totalAmount || order.total || order.amount || 0);

// ═════════════════════════════════════════════════════════════════════════════
// ReturnRefundModal
// ═════════════════════════════════════════════════════════════════════════════
export default function ReturnRefundModal({ order, onClose, addToast }) {
  const [step, setStep]           = useState(0);          // 0 | 1 | 2
  const [reason, setReason]       = useState('');
  const [refundType, setRefundType] = useState('ORIGINAL_PAYMENT');
  const [notes, setNotes]         = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted]   = useState(false);

  const orderId = order.orderId || order.id;
  const items   = order.items || [];
  const total   = getOrderTotal(order);

  // ── Step validation ────────────────────────────────────────────────────────
  const canProceed = () => {
    if (step === 0) return !!reason;
    if (step === 1) return !!refundType;
    return true;
  };

  // ── Submit ─────────────────────────────────────────────────────────────────
  const handleSubmit = async () => {
    setSubmitting(true);
    try {
      await api.orders.initiateReturn(orderId, { reason, refundType, notes });
      setSubmitted(true);
      addToast?.('Return request submitted successfully!', 'success');
    } catch (err) {
      addToast?.(err.message || 'Failed to submit return request.', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  // ── Step bubble state helper ───────────────────────────────────────────────
  const stepState = (idx) => {
    if (submitted) return 'done';
    if (idx < step)  return 'done';
    if (idx === step) return 'active';
    return '';
  };

  // ── Backdrop click ─────────────────────────────────────────────────────────
  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget) onClose();
  };

  return createPortal(
    <div className="return-modal-overlay" onClick={handleOverlayClick}>
      <div className="return-modal" onClick={(e) => e.stopPropagation()}>

        {/* ── Header ── */}
        <div className="return-modal-header" style={{ padding: '1.5rem 1.5rem 1rem', margin: 0 }}>
          <div>
            <div className="return-modal-title">🔄 Return &amp; Refund</div>
            <div className="return-modal-subtitle">Order #{orderId}</div>
          </div>
          <button
            onClick={onClose}
            className="modal-close"
            style={{ position: 'relative', top: 'auto', right: 'auto', fontSize: '1.4rem' }}
            aria-label="Close"
          >
            <X size={20} />
          </button>
        </div>

        <div className="return-modal-content-scroll">
        {/* ── SUCCESS STATE ── */}
        {submitted ? (
          <div className="return-success-state">
            <div className="return-success-icon">✅</div>
            <div className="return-success-title">Return Request Submitted!</div>
            <div className="return-success-sub">
              Your return for Order <strong>#{orderId}</strong> has been received. We'll review it
              within 1–2 business days and notify you at each stage.
            </div>

            {/* Refund amount chip */}
            <div style={{
              background: 'rgba(16,185,129,0.1)',
              border: '1px solid rgba(16,185,129,0.3)',
              borderRadius: '999px',
              padding: '0.4rem 1.2rem',
              color: 'var(--success)',
              fontWeight: 700,
              fontSize: '1rem',
            }}>
              ₹{total.toFixed(2)} — Refund Initiated
            </div>

            {/* Timeline */}
            <div className="return-timeline">
              {REFUND_TIMELINE.map((item, i) => (
                <div key={i} className="return-timeline-item">
                  <div
                    className="return-timeline-dot"
                    style={{
                      background: item.done ? item.color : 'rgba(255,255,255,0.08)',
                      border: `2px solid ${item.done ? item.color : 'var(--border-color)'}`,
                      boxShadow: item.done ? `0 0 8px ${item.color}55` : 'none',
                    }}
                  />
                  <div className="return-timeline-content">
                    <div
                      className="return-timeline-label"
                      style={{ color: item.done ? item.color : 'var(--text-secondary)' }}
                    >
                      {item.label}
                    </div>
                    <div className="return-timeline-desc">{item.desc}</div>
                  </div>
                </div>
              ))}
            </div>

            <button className="btn btn-primary" style={{ marginTop: '0.5rem' }} onClick={onClose}>
              Close
            </button>
          </div>
        ) : (
          <>
            {/* ── Step Indicator ── */}
            <div className="return-steps">
              {STEPS.map((label, idx) => (
                <React.Fragment key={label}>
                  <div className={`return-step ${stepState(idx)}`}>
                    <div className="return-step-bubble">
                      {stepState(idx) === 'done' ? '✓' : idx + 1}
                    </div>
                    <div className="return-step-label">{label}</div>
                  </div>
                  {idx < STEPS.length - 1 && (
                    <div className={`return-step-line ${idx < step ? 'done' : ''}`} />
                  )}
                </React.Fragment>
              ))}
            </div>

            {/* ── Order Summary Strip ── */}
            <div className="return-order-summary">
              <div className="return-summary-item">
                <span className="return-summary-label">Order Total</span>
                <span className="return-summary-value" style={{ color: '#10b981' }}>
                  ₹{total.toFixed(2)}
                </span>
              </div>
              <div style={{ width: 1, background: 'var(--border-color)', alignSelf: 'stretch' }} />
              <div className="return-summary-item">
                <span className="return-summary-label">Order Date</span>
                <span className="return-summary-value">
                  {formatDate(order.createdAt || order.orderDate || order.date)}
                </span>
              </div>
              <div style={{ width: 1, background: 'var(--border-color)', alignSelf: 'stretch' }} />
              <div className="return-summary-item">
                <span className="return-summary-label">Items</span>
                <span className="return-summary-value">{items.length} product{items.length !== 1 ? 's' : ''}</span>
              </div>
            </div>

            {/* ── Item Thumbnails (collapsed) ── */}
            {items.length > 0 && (
              <div style={{ display: 'flex', gap: '0.6rem', flexWrap: 'wrap', marginBottom: '1rem' }}>
                {items.slice(0, 4).map((item, i) => {
                  const img  = item.imageUrl || item.image || item.productImage || item.product?.imageUrl;
                  const name = item.productName || item.name || item.product?.productName || 'Item';
                  return (
                    <div key={i} title={name} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '4px' }}>
                      <ProductImage
                        src={img}
                        alt={name}
                        style={{ width: 48, height: 48, borderRadius: 8, objectFit: 'cover', border: '1px solid var(--border-color)' }}
                      />
                      <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)', maxWidth: 52, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {name}
                      </span>
                    </div>
                  );
                })}
                {items.length > 4 && (
                  <div style={{ width: 48, height: 48, borderRadius: 8, background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    +{items.length - 4}
                  </div>
                )}
              </div>
            )}

            {/* ─────────────────────────────────────────────────────────
                STEP 0 — Select Return Reason
            ───────────────────────────────────────────────────────── */}
            {step === 0 && (
              <>
                <div className="return-section-title">Why are you returning this order?</div>
                <div className="return-reason-grid">
                  {RETURN_REASONS.map((r) => (
                    <button
                      key={r.id}
                      className={`return-reason-pill ${reason === r.id ? 'selected' : ''}`}
                      onClick={() => setReason(r.id)}
                    >
                      <span className="return-reason-icon">{r.icon}</span>
                      {r.label}
                    </button>
                  ))}
                </div>

                {reason && (
                  <>
                    <div className="return-section-title">Additional notes (optional)</div>
                    <textarea
                      className="return-notes"
                      placeholder="Describe the issue in more detail…"
                      value={notes}
                      onChange={(e) => setNotes(e.target.value)}
                    />
                  </>
                )}
              </>
            )}

            {/* ─────────────────────────────────────────────────────────
                STEP 1 — Choose Refund Method
            ───────────────────────────────────────────────────────── */}
            {step === 1 && (
              <>
                <div className="return-section-title">How would you like your refund?</div>
                <div className="refund-type-grid">
                  {REFUND_TYPES.map((rt) => (
                    <div
                      key={rt.id}
                      className={`refund-type-card ${refundType === rt.id ? 'selected' : ''}`}
                      onClick={() => setRefundType(rt.id)}
                    >
                      <div className="refund-type-icon">{rt.icon}</div>
                      <div className="refund-type-name">{rt.name}</div>
                      <div className="refund-type-desc">{rt.desc}</div>
                    </div>
                  ))}
                </div>

                {/* Refund amount summary */}
                <div style={{
                  background: 'rgba(16,185,129,0.06)',
                  border: '1px solid rgba(16,185,129,0.2)',
                  borderRadius: 'var(--radius-sm)',
                  padding: '0.85rem 1.1rem',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  marginTop: '0.5rem',
                }}>
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                    Estimated Refund Amount
                  </span>
                  <span style={{ fontSize: '1.1rem', fontWeight: 800, color: '#10b981' }}>
                    ₹{total.toFixed(2)}
                  </span>
                </div>
              </>
            )}

            {/* ─────────────────────────────────────────────────────────
                STEP 2 — Confirm
            ───────────────────────────────────────────────────────── */}
            {step === 2 && (
              <>
                <div className="return-section-title">Review your return request</div>

                {[
                  { label: 'Return Reason', value: RETURN_REASONS.find((r) => r.id === reason)?.label || reason },
                  { label: 'Refund Method', value: REFUND_TYPES.find((r) => r.id === refundType)?.name || refundType },
                  { label: 'Refund Amount', value: `₹${total.toFixed(2)}` },
                  ...(notes ? [{ label: 'Notes', value: notes }] : []),
                ].map(({ label, value }) => (
                  <div key={label} style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    padding: '0.7rem 0',
                    borderBottom: '1px solid var(--border-color)',
                    gap: '1rem',
                    flexWrap: 'wrap',
                  }}>
                    <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                      {label}
                    </span>
                    <span style={{ fontSize: '0.9rem', color: 'var(--text-primary)', fontWeight: 600, textAlign: 'right', maxWidth: '60%' }}>
                      {value}
                    </span>
                  </div>
                ))}

                <div style={{
                  marginTop: '1rem',
                  background: 'rgba(245,158,11,0.07)',
                  border: '1px solid rgba(245,158,11,0.2)',
                  borderRadius: 'var(--radius-sm)',
                  padding: '0.8rem 1rem',
                  fontSize: '0.8rem',
                  color: 'var(--warning)',
                  lineHeight: 1.6,
                }}>
                  ⚠️ By submitting, you confirm that the item is unused and in its original packaging. Refund processing begins after our team verifies the return.
                </div>
              </>
            )}
          </>
        )}
        </div>
        
        {!submitted && (
            <div style={{ 
              display: 'flex', gap: '0.75rem', padding: '1rem 1.5rem', 
              justifyContent: 'flex-end', borderTop: '1px solid var(--border-color)',
              background: 'rgba(0,0,0,0.2)' 
            }}>
              {step > 0 && (
                <button
                  className="btn btn-secondary"
                  style={{ padding: '0.6rem 1.2rem' }}
                  onClick={() => setStep((s) => s - 1)}
                  disabled={submitting}
                >
                  ← Back
                </button>
              )}

              {step < STEPS.length - 1 ? (
                <button
                  className="btn btn-primary"
                  style={{ padding: '0.6rem 1.4rem' }}
                  onClick={() => setStep((s) => s + 1)}
                  disabled={!canProceed()}
                >
                  Continue →
                </button>
              ) : (
                <button
                  className="btn btn-primary"
                  style={{
                    padding: '0.6rem 1.6rem',
                    background: submitting ? undefined : 'linear-gradient(135deg, #f97316, #ef4444)',
                    boxShadow: '0 4px 14px rgba(249,115,22,0.35)',
                  }}
                  onClick={handleSubmit}
                  disabled={submitting}
                >
                  {submitting ? (
                    <>
                      <span className="spinner" style={{ width: 16, height: 16, borderWidth: 2 }} />
                      Submitting…
                    </>
                  ) : (
                    '✅ Submit Return Request'
                  )}
                </button>
              )}
            </div>
        )}
      </div>
    </div>,
    document.body
  );
}
