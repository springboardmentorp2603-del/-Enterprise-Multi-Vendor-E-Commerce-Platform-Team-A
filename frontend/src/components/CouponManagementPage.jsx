import React, { useState, useEffect } from 'react';
import { api } from '../api';

export default function CouponManagementPage({ addToast }) {
  const [coupons, setCoupons] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // Analytics modal state
  const [analyticsData, setAnalyticsData] = useState(null);
  const [selectedCouponForAnalytics, setSelectedCouponForAnalytics] = useState(null);
  const [analyticsLoading, setAnalyticsLoading] = useState(false);

  // Form Fields
  const [formData, setFormData] = useState({
    code: '',
    description: '',
    discountType: 'PERCENTAGE',
    discountValue: '',
    minOrderAmount: '',
    maxDiscountAmount: '',
    usageLimitTotal: '',
    usageLimitPerUser: '',
    validFrom: '',
    validTo: '',
  });

  useEffect(() => {
    loadCoupons();
  }, []);

  const loadCoupons = async () => {
    setLoading(true);
    try {
      const res = await api.coupons.list();
      const list = res.data || res || [];
      setCoupons(Array.isArray(list) ? list : []);
    } catch (err) {
      addToast(err.message || 'Failed to load coupons', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    if (!formData.code.trim()) {
      addToast('Coupon code is required', 'warning');
      return;
    }
    if (!formData.discountValue || Number(formData.discountValue) <= 0) {
      addToast('Discount value must be greater than zero', 'warning');
      return;
    }
    if (!formData.validFrom || !formData.validTo) {
      addToast('Valid From and Valid To dates are required', 'warning');
      return;
    }

    // Format ISO LocalDateTime (ensure :00 seconds suffix if missing)
    const formatLocalDateTime = (dtStr) => {
      if (!dtStr) return null;
      if (dtStr.length === 16) return dtStr + ':00';
      return dtStr;
    };

    const payload = {
      code: formData.code.trim().toUpperCase(),
      description: formData.description.trim() || null,
      discountType: formData.discountType,
      discountValue: parseFloat(formData.discountValue),
      minOrderAmount: formData.minOrderAmount ? parseFloat(formData.minOrderAmount) : null,
      maxDiscountAmount: formData.maxDiscountAmount ? parseFloat(formData.maxDiscountAmount) : null,
      usageLimitTotal: formData.usageLimitTotal ? parseInt(formData.usageLimitTotal, 10) : null,
      usageLimitPerUser: formData.usageLimitPerUser ? parseInt(formData.usageLimitPerUser, 10) : null,
      validFrom: formatLocalDateTime(formData.validFrom),
      validTo: formatLocalDateTime(formData.validTo),
    };

    setSubmitting(true);
    try {
      await api.coupons.create(payload);
      addToast(`Coupon '${payload.code}' created successfully!`, 'success');
      setShowCreateModal(false);
      resetForm();
      loadCoupons();
    } catch (err) {
      addToast(err.message || 'Failed to create coupon', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  const resetForm = () => {
    setFormData({
      code: '',
      description: '',
      discountType: 'PERCENTAGE',
      discountValue: '',
      minOrderAmount: '',
      maxDiscountAmount: '',
      usageLimitTotal: '',
      usageLimitPerUser: '',
      validFrom: '',
      validTo: '',
    });
  };

  const handleDeactivate = async (coupon) => {
    if (!window.confirm(`Are you sure you want to deactivate coupon '${coupon.code}'?`)) {
      return;
    }
    try {
      await api.coupons.deactivate(coupon.id);
      addToast(`Coupon '${coupon.code}' deactivated successfully!`, 'success');
      loadCoupons();
    } catch (err) {
      addToast(err.message || 'Failed to deactivate coupon', 'error');
    }
  };

  const handleViewAnalytics = async (coupon) => {
    setSelectedCouponForAnalytics(coupon);
    setAnalyticsLoading(true);
    setAnalyticsData(null);
    try {
      const res = await api.coupons.getAnalytics(coupon.id);
      const data = res.data || res;
      setAnalyticsData(data);
    } catch (err) {
      addToast(err.message || 'Failed to fetch coupon analytics', 'error');
    } finally {
      setAnalyticsLoading(false);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    try {
      return new Date(dateStr).toLocaleString();
    } catch {
      return dateStr;
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', padding: '1rem 0' }}>
      {/* Header Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2 style={{ fontFamily: 'var(--font-heading)', fontSize: '1.75rem', margin: 0 }}>Coupon & Promotion Engine</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '0.25rem', fontSize: '0.9rem' }}>
            Manage discount codes, set usage limits, track promotion analytics, and deactivate campaigns.
          </p>
        </div>
        <button
          className="btn btn-primary"
          onClick={() => setShowCreateModal(true)}
          style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.6rem 1.25rem' }}
        >
          <span>✨</span> Create New Coupon
        </button>
      </div>

      {/* Coupons Table / Cards */}
      <div className="glass-card">
        {loading ? (
          <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '4rem' }}>
            <div className="spinner"></div>
          </div>
        ) : coupons.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--text-secondary)' }}>
            <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🎟️</div>
            <h3>No Coupons Found</h3>
            <p style={{ marginTop: '0.5rem' }}>Create your first discount campaign to boost sales!</p>
            <button className="btn btn-primary" style={{ marginTop: '1.25rem' }} onClick={() => setShowCreateModal(true)}>
              + Create Coupon
            </button>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)' }}>
                  <th style={{ padding: '0.75rem 1rem' }}>Code</th>
                  <th style={{ padding: '0.75rem 1rem' }}>Discount</th>
                  <th style={{ padding: '0.75rem 1rem' }}>Min Order / Max Cap</th>
                  <th style={{ padding: '0.75rem 1rem' }}>Usage Limit</th>
                  <th style={{ padding: '0.75rem 1rem' }}>Validity</th>
                  <th style={{ padding: '0.75rem 1rem' }}>Status</th>
                  <th style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {coupons.map((c) => {
                  const isActive = c.active !== false;
                  return (
                    <tr key={c.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                      <td style={{ padding: '1rem' }}>
                        <div style={{ fontWeight: 700, fontFamily: 'monospace', fontSize: '1rem', color: 'var(--secondary)' }}>
                          {c.code}
                        </div>
                        {c.description && (
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '2px' }}>
                            {c.description}
                          </div>
                        )}
                      </td>
                      <td style={{ padding: '1rem' }}>
                        <span style={{ fontWeight: 600 }}>
                          {c.discountType === 'PERCENTAGE' ? `${c.discountValue}% OFF` : `₹${c.discountValue} FLAT`}
                        </span>
                      </td>
                      <td style={{ padding: '1rem', color: 'var(--text-secondary)' }}>
                        <div>Min: {c.minOrderAmount ? `₹${c.minOrderAmount}` : 'None'}</div>
                        <div>Max Cap: {c.maxDiscountAmount ? `₹${c.maxDiscountAmount}` : 'No cap'}</div>
                      </td>
                      <td style={{ padding: '1rem', color: 'var(--text-secondary)' }}>
                        <div>Used: {c.totalTimesUsed || 0} times</div>
                        <div>Total Limit: {c.usageLimitTotal ?? '∞'}</div>
                        <div>Per User: {c.usageLimitPerUser ?? '∞'}</div>
                      </td>
                      <td style={{ padding: '1rem', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                        <div>From: {formatDate(c.validFrom)}</div>
                        <div>To: {formatDate(c.validTo)}</div>
                      </td>
                      <td style={{ padding: '1rem' }}>
                        <span
                          style={{
                            padding: '0.25rem 0.6rem',
                            borderRadius: '12px',
                            fontSize: '0.75rem',
                            fontWeight: 700,
                            background: isActive ? 'var(--success-glow)' : 'var(--danger-glow)',
                            color: isActive ? 'var(--success)' : 'var(--danger)',
                            border: `1px solid ${isActive ? 'var(--success)' : 'var(--danger)'}`,
                          }}
                        >
                          {isActive ? 'ACTIVE' : 'INACTIVE'}
                        </span>
                      </td>
                      <td style={{ padding: '1rem', textAlign: 'right' }}>
                        <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                          <button
                            className="btn btn-secondary"
                            style={{ padding: '0.3rem 0.65rem', fontSize: '0.8rem' }}
                            onClick={() => handleViewAnalytics(c)}
                            title="View Analytics"
                          >
                            📊 Analytics
                          </button>
                          {isActive && (
                            <button
                              className="btn btn-danger"
                              style={{ padding: '0.3rem 0.65rem', fontSize: '0.8rem' }}
                              onClick={() => handleDeactivate(c)}
                              title="Deactivate Coupon"
                            >
                              🚫 Deactivate
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* CREATE COUPON MODAL */}
      {showCreateModal && (
        <div className="modal-overlay" onClick={() => setShowCreateModal(false)}>
          <div
            className="modal-content"
            onClick={(e) => e.stopPropagation()}
            style={{ maxWidth: '650px', width: '100%', maxHeight: '90vh', overflowY: 'auto' }}
          >
            <button className="modal-close" onClick={() => setShowCreateModal(false)}>×</button>

            <h3 style={{ fontFamily: 'var(--font-heading)', fontSize: '1.4rem', marginBottom: '1.25rem' }}>
              Create New Coupon
            </h3>

            <form onSubmit={handleCreateSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label">Coupon Code *</label>
                  <input
                    type="text"
                    name="code"
                    className="form-input"
                    placeholder="e.g. SUMMER20"
                    value={formData.code}
                    onChange={handleInputChange}
                    required
                    style={{ textTransform: 'uppercase' }}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Discount Type *</label>
                  <select
                    name="discountType"
                    className="form-input"
                    value={formData.discountType}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="PERCENTAGE">PERCENTAGE (%)</option>
                    <option value="FLAT">FLAT AMOUNT (₹)</option>
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Description</label>
                <input
                  type="text"
                  name="description"
                  className="form-input"
                  placeholder="e.g. Get 20% off on all summer items"
                  value={formData.description}
                  onChange={handleInputChange}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label">
                    Discount Value * {formData.discountType === 'PERCENTAGE' ? '(%)' : '(₹)'}
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    name="discountValue"
                    className="form-input"
                    placeholder={formData.discountType === 'PERCENTAGE' ? 'e.g. 20' : 'e.g. 150'}
                    value={formData.discountValue}
                    onChange={handleInputChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Min Order Amount (₹)</label>
                  <input
                    type="number"
                    step="0.01"
                    name="minOrderAmount"
                    className="form-input"
                    placeholder="e.g. 500"
                    value={formData.minOrderAmount}
                    onChange={handleInputChange}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label">Max Discount (₹)</label>
                  <input
                    type="number"
                    step="0.01"
                    name="maxDiscountAmount"
                    className="form-input"
                    placeholder="e.g. 200"
                    value={formData.maxDiscountAmount}
                    onChange={handleInputChange}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Usage Limit (Total)</label>
                  <input
                    type="number"
                    name="usageLimitTotal"
                    className="form-input"
                    placeholder="e.g. 100"
                    value={formData.usageLimitTotal}
                    onChange={handleInputChange}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Usage Limit (Per User)</label>
                  <input
                    type="number"
                    name="usageLimitPerUser"
                    className="form-input"
                    placeholder="e.g. 1"
                    value={formData.usageLimitPerUser}
                    onChange={handleInputChange}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label">Valid From *</label>
                  <input
                    type="datetime-local"
                    name="validFrom"
                    className="form-input"
                    value={formData.validFrom}
                    onChange={handleInputChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Valid To *</label>
                  <input
                    type="datetime-local"
                    name="validTo"
                    className="form-input"
                    value={formData.validTo}
                    onChange={handleInputChange}
                    required
                  />
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowCreateModal(false)}
                  disabled={submitting}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Creating...' : 'Create Coupon'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ANALYTICS MODAL */}
      {selectedCouponForAnalytics && (
        <div className="modal-overlay" onClick={() => setSelectedCouponForAnalytics(null)}>
          <div
            className="modal-content"
            onClick={(e) => e.stopPropagation()}
            style={{ maxWidth: '500px', width: '100%' }}
          >
            <button className="modal-close" onClick={() => setSelectedCouponForAnalytics(null)}>×</button>

            <h3 style={{ fontFamily: 'var(--font-heading)', fontSize: '1.4rem', marginBottom: '0.5rem' }}>
              Coupon Analytics
            </h3>
            <p style={{ color: 'var(--secondary)', fontWeight: 700, fontSize: '1.1rem', marginBottom: '1.5rem' }}>
              {selectedCouponForAnalytics.code}
            </p>

            {analyticsLoading ? (
              <div style={{ display: 'flex', justifyContent: 'center', padding: '3rem' }}>
                <div className="spinner"></div>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div style={{ background: 'var(--bg-input)', padding: '1.25rem', borderRadius: 'var(--radius-sm)', textAlign: 'center' }}>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', textTransform: 'uppercase' }}>Total Usage</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 800, marginTop: '0.25rem', color: 'var(--text-primary)' }}>
                      {analyticsData?.totalTimesUsed ?? selectedCouponForAnalytics.totalTimesUsed ?? 0}
                    </div>
                  </div>

                  <div style={{ background: 'var(--bg-input)', padding: '1.25rem', borderRadius: 'var(--radius-sm)', textAlign: 'center' }}>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', textTransform: 'uppercase' }}>Total Discount Given</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 800, marginTop: '0.25rem', color: 'var(--success)' }}>
                      ₹{analyticsData?.totalDiscountGiven !== undefined && analyticsData?.totalDiscountGiven !== null ? Number(analyticsData.totalDiscountGiven).toFixed(2) : '0.00'}
                    </div>
                  </div>
                </div>

                <div style={{ background: 'var(--bg-input)', padding: '1.25rem', borderRadius: 'var(--radius-sm)', display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.9rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-secondary)' }}>Status:</span>
                    <span style={{ fontWeight: 700, color: selectedCouponForAnalytics.active !== false ? 'var(--success)' : 'var(--danger)' }}>
                      {selectedCouponForAnalytics.active !== false ? 'ACTIVE' : 'INACTIVE'}
                    </span>
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-secondary)' }}>Usage Limit Total:</span>
                    <span style={{ fontWeight: 600 }}>
                      {analyticsData?.usageLimitTotal ?? selectedCouponForAnalytics.usageLimitTotal ?? 'Unlimited'}
                    </span>
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-secondary)' }}>Discount Config:</span>
                    <span style={{ fontWeight: 600 }}>
                      {selectedCouponForAnalytics.discountType === 'PERCENTAGE' ? `${selectedCouponForAnalytics.discountValue}% OFF` : `₹${selectedCouponForAnalytics.discountValue} FLAT`}
                    </span>
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-secondary)' }}>Valid Until:</span>
                    <span style={{ fontWeight: 600 }}>
                      {formatDate(selectedCouponForAnalytics.validTo)}
                    </span>
                  </div>
                </div>

                <div style={{ marginTop: '1rem', textAlign: 'right' }}>
                  <button className="btn btn-secondary" onClick={() => setSelectedCouponForAnalytics(null)}>
                    Close
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
