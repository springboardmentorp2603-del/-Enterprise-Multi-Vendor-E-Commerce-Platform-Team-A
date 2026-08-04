import React, { useState, useEffect } from 'react';
import { api } from '../api';

/**
 * Reusable "available coupons" list.
 *
 * Usage as a standalone dashboard page:
 *   <AvailableCoupons addToast={addToast} />
 *
 * Usage inside checkout, with eligibility checking + click-to-apply:
 *   <AvailableCoupons
 *     addToast={addToast}
 *     cartTotal={cartSubtotal}
 *     compact
 *     onApply={(code) => { setCouponCode(code); handleApplyCoupon(code); }}
 *   />
 */
function AvailableCoupons({ addToast, cartTotal, compact = false, onApply }) {
  const [coupons, setCoupons] = useState([]);
  const [loading, setLoading] = useState(false);
  const [expanded, setExpanded] = useState(!compact); // compact mode starts collapsed

  useEffect(() => {
    fetchCoupons();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cartTotal]);

  const fetchCoupons = async () => {
    setLoading(true);
    try {
      const res = await api.coupons.getAvailable(cartTotal);
      setCoupons(res?.data || []);
    } catch (err) {
      addToast?.(err.message || 'Failed to load offers', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = (code) => {
    navigator.clipboard.writeText(code);
    addToast?.(`Copied "${code}" to clipboard`, 'success');
  };

  const formatDiscount = (c) =>
    c.discountType === 'PERCENTAGE'
      ? `${c.discountValue}% off${c.maxDiscountAmount ? ` (up to ₹${c.maxDiscountAmount})` : ''}`
      : `₹${c.discountValue} off`;

  const cardStyle = {
    background: 'var(--bg-card)',
    border: '1px solid var(--border-color)',
    borderRadius: 'var(--radius-md)',
    padding: '1rem',
    display: 'flex',
    flexDirection: 'column',
    gap: '0.4rem',
    minWidth: '220px',
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: '1rem' }}>
        <div className="spinner" style={{ width: '24px', height: '24px' }} />
      </div>
    );
  }

  if (coupons.length === 0) {
    return compact ? null : (
      <p style={{ color: 'var(--text-secondary)', textAlign: 'center' }}>No offers available right now.</p>
    );
  }

  // ---- Compact mode: collapsible hint banner for checkout ----
  if (compact) {
    return (
      <div style={{ marginBottom: '0.75rem' }}>
        <button
          type="button"
          onClick={() => setExpanded((e) => !e)}
          style={{
            background: 'none',
            border: 'none',
            color: 'var(--primary, #6366f1)',
            fontSize: '0.85rem',
            cursor: 'pointer',
            padding: 0,
          }}
        >
          💡 {coupons.length} offer{coupons.length > 1 ? 's' : ''} available — {expanded ? 'hide' : 'view'}
        </button>

        {expanded && (
          <div style={{ display: 'flex', gap: '0.75rem', overflowX: 'auto', paddingTop: '0.75rem' }}>
            {coupons.map((c) => (
              <div key={c.id} style={{ ...cardStyle, minWidth: '200px' }}>
                <div style={{ fontFamily: 'var(--font-heading)', fontWeight: 700 }}>{c.code}</div>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{formatDiscount(c)}</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                  Min order ₹{c.minOrderAmount ?? 0}
                </div>

                {c.eligibleForCart === true && (
                  <span style={{ color: '#10b981', fontSize: '0.8rem' }}>✅ Eligible</span>
                )}
                {c.eligibleForCart === false && (
                  <span style={{ color: '#f59e0b', fontSize: '0.8rem' }}>
                    Add ₹{(c.minOrderAmount - (cartTotal || 0)).toFixed(2)} more
                  </span>
                )}

                <button
                  className="btn btn-primary"
                  style={{ marginTop: '0.25rem', fontSize: '0.8rem' }}
                  disabled={c.eligibleForCart === false}
                  onClick={() => onApply?.(c.code)}
                >
                  Apply
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    );
  }

  // ---- Full page mode: dashboard "Offers" tab ----
  return (
    <div>
      <h2 style={{ fontFamily: 'var(--font-heading)', fontWeight: 800, marginBottom: '1rem' }}>
        Available Offers
      </h2>
      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
        {coupons.map((c) => (
          <div key={c.id} style={cardStyle}>
            <div style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, fontSize: '1.1rem' }}>
              {c.code}
            </div>
            {c.description && (
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{c.description}</div>
            )}
            <div style={{ fontWeight: 600 }}>{formatDiscount(c)}</div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              Minimum order: ₹{c.minOrderAmount ?? 0}
            </div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              Valid till: {new Date(c.validTo).toLocaleDateString()}
            </div>
            {c.vendorId && (
              <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                Vendor-specific offer
              </div>
            )}
            <button className="btn btn-secondary" onClick={() => handleCopy(c.code)}>
              Copy Code
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default AvailableCoupons;