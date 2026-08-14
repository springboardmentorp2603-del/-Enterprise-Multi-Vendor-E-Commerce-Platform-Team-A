import React, { useState, useEffect, useMemo } from 'react';
import { RefreshCw, ArrowLeft, Package, CircleDollarSign, ReceiptText } from 'lucide-react';
import { api, unwrap } from '../api';
import StatusBadge from './StatusBadge';
import ReturnRefundModal from './ReturnRefundModal';
import ProductImage from './ProductImage';
import './ReturnRefund.css';

// ── Filter tabs ────────────────────────────────────────────────────────────────
const TABS = [
  { key: 'ALL',      label: 'All Returns' },
  { key: 'RETURNED', label: 'Pending'     },
  { key: 'REFUNDED', label: 'Refunded'    },
];

// ── Helper: pick readable label for stored reason key ─────────────────────────
const REASON_LABELS = {
  DEFECTIVE:        '🔧 Defective / Damaged',
  WRONG_ITEM:       '📦 Wrong item',
  NOT_AS_DESCRIBED: '📋 Not as described',
  CHANGED_MIND:     '💭 Changed mind',
  BETTER_PRICE:     '💰 Better price found',
  OTHER:            '❓ Other',
};

// ── Helper: format date ────────────────────────────────────────────────────────
const fmt = (raw) =>
  raw
    ? new Date(raw).toLocaleDateString('en-IN', {
        day: '2-digit', month: 'short', year: 'numeric',
      })
    : '—';

// ── Helper: derive normalised status from order ────────────────────────────────
const statusOf = (o) => String(o.status || 'PENDING').toUpperCase().replace(/ /g, '_');

// ═════════════════════════════════════════════════════════════════════════════
// ReturnRefundPage
// ═════════════════════════════════════════════════════════════════════════════
export default function ReturnRefundPage({ user, addToast, onBack }) {
  const [orders,   setOrders]   = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [tab,      setTab]      = useState('ALL');
  const [selected, setSelected] = useState(null);  // order to open in modal (re-return view)

  // ── Load orders ────────────────────────────────────────────────────────────
  const loadOrders = async () => {
    setLoading(true);
    try {
      const raw    = await api.orders.getReturns();
      const all    = unwrap(raw);
      const arr    = Array.isArray(all) ? all : [];
      // Keep only returned / refunded orders
      const returnOrders = arr.filter((o) =>
        ['RETURNED', 'REFUNDED', 'RETURN_REQUESTED', 'RETURN_APPROVED', 'RETURN_PICKED', 'RETURN_RECEIVED', 'REFUND_INITIATED', 'REFUND_COMPLETED'].includes(statusOf(o))
      );
      setOrders(returnOrders);
    } catch {
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadOrders(); }, [user]);

  // ── Stats ──────────────────────────────────────────────────────────────────
  const stats = useMemo(() => {
    const total     = orders.length;
    const pending   = orders.filter((o) => !['REFUNDED', 'REFUND_COMPLETED'].includes(statusOf(o))).length;
    const refunded  = orders.filter((o) => ['REFUNDED', 'REFUND_COMPLETED'].includes(statusOf(o))).length;
    const totalAmt  = orders.reduce(
      (sum, o) => sum + Number(o.totalAmount || o.total || o.amount || 0), 0
    );
    return { total, pending, refunded, totalAmt };
  }, [orders]);

  // ── Filtered list ──────────────────────────────────────────────────────────
  const visible = useMemo(() => {
    if (tab === 'ALL') return orders;
    if (tab === 'RETURNED') {
      return orders.filter((o) => !['REFUNDED', 'REFUND_COMPLETED'].includes(statusOf(o)));
    }
    if (tab === 'REFUNDED') {
      return orders.filter((o) => ['REFUNDED', 'REFUND_COMPLETED'].includes(statusOf(o)));
    }
    return orders;
  }, [orders, tab]);

  // ─────────────────────────────────────────────────────────────────────────
  return (
    <div className="main-content" style={{ paddingTop: '2rem', maxWidth: '1200px', margin: '0 auto' }}>

      {/* ── Page header ── */}
      <div className="refund-page-header" style={{ background: 'linear-gradient(135deg, rgba(249,115,22,0.16), rgba(59,130,246,0.14))', border: '1px solid rgba(255,255,255,0.08)', boxShadow: '0 16px 40px rgba(0,0,0,0.16)' }}>
        <div>
          {onBack && (
            <button
              className="btn btn-secondary"
              style={{ padding: '0.4rem 0.85rem', fontSize: '0.82rem', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}
              onClick={onBack}
            >
              <ArrowLeft size={14} /> Back to Orders
            </button>
          )}
          <h1 className="refund-page-title">Returns &amp; Refunds</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem', marginTop: '0.3rem' }}>
            Track and manage your return requests and refund status.
          </p>
        </div>

        <button
          className="btn btn-secondary"
          style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', alignSelf: 'flex-start' }}
          onClick={loadOrders}
          disabled={loading}
        >
          <RefreshCw size={15} className={loading ? 'spin' : ''} />
          Refresh
        </button>
      </div>

      {/* ── Stats cards ── */}
      <div className="refund-stats-grid">
        <div className="refund-stat-card orange">
          <div className="refund-stat-icon"><Package size={18} /></div>
          <div className="refund-stat-num">{stats.total}</div>
          <div className="refund-stat-label">Total Returns</div>
        </div>
        <div className="refund-stat-card blue">
          <div className="refund-stat-icon"><ReceiptText size={18} /></div>
          <div className="refund-stat-num">{stats.pending}</div>
          <div className="refund-stat-label">Pending Review</div>
        </div>
        <div className="refund-stat-card green">
          <div className="refund-stat-icon"><CircleDollarSign size={18} /></div>
          <div className="refund-stat-num">{stats.refunded}</div>
          <div className="refund-stat-label">Refunds Completed</div>
        </div>
        <div className="refund-stat-card purple">
          <div className="refund-stat-icon">₹</div>
          <div className="refund-stat-num">₹{stats.totalAmt.toFixed(0)}</div>
          <div className="refund-stat-label">Total Refund Value</div>
        </div>
      </div>

      {/* ── Filter tabs ── */}
      <div className="refund-filter-tabs">
        {TABS.map((t) => (
          <button
            key={t.key}
            className={`refund-tab ${tab === t.key ? 'active' : ''}`}
            onClick={() => setTab(t.key)}
          >
            {t.label}
            {t.key !== 'ALL' && (
              <span style={{ marginLeft: '0.4rem', opacity: 0.6, fontSize: '0.72rem' }}>
                ({t.key === 'RETURNED' ? stats.pending : stats.refunded})
              </span>
            )}
          </button>
        ))}
      </div>

      {/* ── Loading ── */}
      {loading && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: 'var(--text-muted)', padding: '3rem 0' }}>
          <div className="spinner" style={{ width: 24, height: 24, borderWidth: 2 }} />
          Loading your returns…
        </div>
      )}

      {/* ── Empty state ── */}
      {!loading && visible.length === 0 && (
        <div className="refund-empty">
          <div className="refund-empty-icon">📬</div>
          <div className="refund-empty-title">No returns found</div>
          <div className="refund-empty-sub">
            {tab === 'ALL'
              ? "You haven't initiated any returns yet. You can return delivered orders from the My Orders page."
              : `No orders in the "${TABS.find((t) => t.key === tab)?.label}" category.`}
          </div>
        </div>
      )}

      {/* ── Return history cards ── */}
      {!loading && visible.length > 0 && (
        <div className="return-cards-grid">
          {visible.map((order) => {
            const id     = order.orderId || order.id;
            const status = statusOf(order);
            const total  = Number(order.totalAmount || order.total || order.amount || 0);
            const items  = order.items || [];
            const reason = order.returnReason || order.reason;
            const date   = order.updatedAt || order.createdAt || order.orderDate;

            return (
              <div
                key={id}
                className={`return-history-card ${['REFUNDED', 'REFUND_COMPLETED'].includes(status) ? 'refunded' : ''}`}
              >
                <div className="return-history-card-inner">

                  {/* Top row */}
                  <div className="return-card-top">
                    <div>
                      <div className="return-card-order-id">Order #{id}</div>
                      <div className="return-card-date">{fmt(date)}</div>
                    </div>
                    <StatusBadge status={status} />
                  </div>

                  {/* Items */}
                  <div className="return-card-items">
                    {items.slice(0, 2).map((item, i) => {
                      const img  = item.imageUrl || item.image || item.productImage || item.product?.imageUrl;
                      const name = item.productName || item.name || item.product?.productName || 'Product';
                      return (
                        <div key={i} className="return-card-item">
                          <ProductImage
                            src={img}
                            alt={name}
                            className="return-card-item-img"
                          />
                          <span className="return-card-item-name">{name}</span>
                          <span className="return-card-item-qty">×{item.quantity || 1}</span>
                        </div>
                      );
                    })}
                    {items.length > 2 && (
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                        +{items.length - 2} more item{items.length - 2 !== 1 ? 's' : ''}
                      </div>
                    )}
                  </div>

                  {/* Reason chip */}
                  {reason && (
                    <div className="return-card-reason-chip">
                      {REASON_LABELS[reason] || reason}
                    </div>
                  )}

                  {/* Footer */}
                  <div className="return-card-footer">
                    <div>
                      <div className="return-card-amount-label">Refund Amount</div>
                      <div className="return-card-amount">₹{total.toFixed(2)}</div>
                    </div>

                    {/* Status-based action / badge */}
                    <div style={{
                      background: status === 'REFUNDED' || status === 'REFUND_COMPLETED'
                        ? 'rgba(16,185,129,0.12)'
                        : 'rgba(59,130,246,0.12)',
                      border: status === 'REFUNDED' || status === 'REFUND_COMPLETED'
                        ? '1px solid rgba(16,185,129,0.3)'
                        : '1px solid rgba(59,130,246,0.3)',
                      color: status === 'REFUNDED' || status === 'REFUND_COMPLETED'
                        ? 'var(--success)'
                        : '#3b82f6',
                      borderRadius: '999px',
                      padding: '0.3rem 0.85rem',
                      fontSize: '0.75rem',
                      fontWeight: 700,
                    }}>
                      {status === 'REFUNDED' || status === 'REFUND_COMPLETED'
                        ? '✅ Refunded'
                        : `🔄 ${status.replace(/_/g, ' ')}`}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* ── Re-view modal (read-only context for already-returned orders) ── */}
      {selected && (
        <ReturnRefundModal
          order={selected}
          onClose={() => setSelected(null)}
          addToast={addToast}
        />
      )}
    </div>
  );
}
