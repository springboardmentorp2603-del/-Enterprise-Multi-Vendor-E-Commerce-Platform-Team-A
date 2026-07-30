import { useState, useMemo } from 'react';
import ProductImage from '../../components/ProductImage';
import { 
  Layers, 
  Shield, 
  BarChart2, 
  Check, 
  X, 
  Sun, 
  Moon, 
  Package, 
  User, 
  Warehouse, 
  Search, 
  Trash2,
  Lock,
  Mail,
  UserCheck,
  RefreshCw,
  Plus
} from 'lucide-react';
import './AdminDashboard.css';

export default function AdminDashboard({
  user,
  activeTab = 'stats',
  onSetActiveTab,
  loading = false,
  stats,
  vendors = [],
  vendorFilter = '',
  onSetVendorFilter,
  selectedVendor,
  onSetSelectedVendor,
  approvalHistory = [],
  reviewRemarks = '',
  onSetReviewRemarks,
  pendingProducts = [],
  selectedProduct,
  onSetSelectedProduct,
  categories = [],
  showCategoryForm = false,
  onSetShowCategoryForm,
  editingCategory,
  catName = '',
  catDesc = '',
  onSetCatName,
  onSetCatDesc,
  staffName = '',
  staffEmail = '',
  staffPassword = '',
  staffPhone = '',
  staffRole = 'WAREHOUSE_STAFF',
  onSetStaffName,
  onSetStaffEmail,
  onSetStaffPassword,
  onSetStaffPhone,
  onSetStaffRole,
  customers = [],
  onHandleVendorReview,
  onHandleProductReview,
  onViewVendorDetails,
  onHandleCategorySubmit,
  onStartEditCategory,
  onHandleDeleteCategory,
  onHandleStaffProvision,
  onRefresh,
  addToast
}) {
  const [searchQuery, setSearchQuery] = useState('');

  return (
    <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>

      {/* Header */}
      <div className="glass-card" style={{ padding: '1.25rem 1.5rem', marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <Shield size={24} style={{ color: 'var(--primary)' }} />
          <div>
            <h2 style={{ margin: 0, fontFamily: 'var(--font-heading)', fontWeight: 800, fontSize: '1.3rem' }}>
              Admin<span className="text-gradient">Console</span>
            </h2>
            <p style={{ margin: 0, fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              {user?.name || 'Administrator'} &nbsp;·&nbsp; {user?.email || ''}
            </p>
          </div>
        </div>
        <button className="btn btn-secondary" onClick={onRefresh} title="Refresh">
          <RefreshCw size={15} style={{ marginRight: '6px' }} /> Refresh
        </button>
      </div>

      {/* Tab Navigation */}
      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '1.5rem' }}>
        {[
          { key: 'stats', label: 'Overview', icon: <BarChart2 size={14} /> },
          { key: 'vendors', label: 'Vendors', icon: <Warehouse size={14} /> },
          { key: 'products', label: 'Pending Products', icon: <Package size={14} /> },
          { key: 'categories', label: 'Categories', icon: <Layers size={14} /> },
          { key: 'staff', label: 'Staff', icon: <UserCheck size={14} /> },
          { key: 'customers', label: 'Customers', icon: <User size={14} /> }
        ].map(tab => (
          <button
            key={tab.key}
            className={`btn ${activeTab === tab.key ? 'btn-primary' : 'btn-secondary'}`}
            style={{ display: 'flex', alignItems: 'center', gap: '6px', padding: '0.4rem 0.9rem', fontSize: '0.85rem' }}
            onClick={() => onSetActiveTab && onSetActiveTab(tab.key)}
          >
            {tab.icon} {tab.label}
            {tab.key === 'products' && pendingProducts.length > 0 && (
              <span style={{ background: '#ef4444', color: '#fff', borderRadius: '10px', fontSize: '10px', padding: '1px 6px', fontWeight: 700, marginLeft: '4px' }}>
                {pendingProducts.length}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Loading */}
      {loading && (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '3rem' }}>
          <div className="spinner"></div>
        </div>
      )}

      {!loading && (
        <>
          {/* TAB: STATS OVERVIEW */}
          {activeTab === 'stats' && stats && (
            <div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
                {[
                  { label: 'Total Vendors', value: stats.totalVendors ?? stats.vendorCount ?? '—', color: '#aa3bff' },
                  { label: 'Total Customers', value: stats.totalCustomers ?? stats.customerCount ?? '—', color: '#3b82f6' },
                  { label: 'Total Products', value: stats.totalProducts ?? stats.productCount ?? '—', color: '#10b981' },
                  { label: 'Pending Products', value: stats.pendingProducts ?? stats.pendingProductCount ?? '—', color: '#f59e0b' },
                  { label: 'Total Orders', value: stats.totalOrders ?? stats.orderCount ?? '—', color: '#ec4899' }
                ].map(card => (
                  <div key={card.label} className="glass-card" style={{ padding: '1.25rem' }}>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700, marginBottom: '4px' }}>{card.label}</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 800, color: card.color }}>{card.value}</div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === 'stats' && !stats && (
            <div className="glass-card" style={{ padding: '3rem', textAlign: 'center' }}>
              <BarChart2 size={48} style={{ marginBottom: '1rem', opacity: 0.4 }} />
              <p style={{ color: 'var(--text-secondary)' }}>Statistics loading or unavailable.</p>
            </div>
          )}

          {/* TAB: VENDORS */}
          {activeTab === 'vendors' && (
            <div>
              <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1.25rem', flexWrap: 'wrap', alignItems: 'center' }}>
                <select
                  value={vendorFilter}
                  onChange={(e) => onSetVendorFilter && onSetVendorFilter(e.target.value)}
                  style={{ padding: '0.4rem 0.75rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }}
                >
                  <option value="">All Statuses</option>
                  <option value="PENDING">Pending</option>
                  <option value="ACTIVE">Active</option>
                  <option value="REJECTED">Rejected</option>
                  <option value="SUSPENDED">Suspended</option>
                </select>
              </div>

              {vendors.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {vendors.map(vendor => (
                    <div key={vendor.vendorId} className="glass-card" style={{ padding: '1.25rem', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
                      <div style={{ flex: 1, minWidth: 0 }}>
                        <h4 style={{ margin: '0 0 4px 0', fontSize: '1rem', fontWeight: 700 }}>{vendor.businessName}</h4>
                        <p style={{ margin: '0 0 4px 0', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{vendor.businessEmail}</p>
                        <span style={{ fontSize: '0.75rem', fontWeight: 700, padding: '2px 8px', borderRadius: '8px', background: vendor.status === 'ACTIVE' ? 'rgba(16,185,129,0.15)' : vendor.status === 'PENDING' ? 'rgba(245,158,11,0.15)' : 'rgba(239,68,68,0.15)', color: vendor.status === 'ACTIVE' ? '#10b981' : vendor.status === 'PENDING' ? '#f59e0b' : '#ef4444' }}>
                          {vendor.status}
                        </span>
                      </div>
                      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', alignItems: 'center' }}>
                        <button className="btn btn-secondary" style={{ fontSize: '0.8rem', padding: '0.3rem 0.7rem' }} onClick={() => onViewVendorDetails && onViewVendorDetails(vendor)}>
                          Details
                        </button>
                        {vendor.status === 'PENDING' && (
                          <>
                            <button className="btn btn-primary" style={{ fontSize: '0.8rem', padding: '0.3rem 0.7rem', background: '#10b981', border: 'none' }} onClick={() => onHandleVendorReview && onHandleVendorReview(vendor.vendorId, 'approve')}>
                              <Check size={13} style={{ marginRight: '4px' }} /> Approve
                            </button>
                            <button className="btn btn-secondary" style={{ fontSize: '0.8rem', padding: '0.3rem 0.7rem', color: '#ef4444', background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.25)' }} onClick={() => onHandleVendorReview && onHandleVendorReview(vendor.vendorId, 'reject')}>
                              <X size={13} style={{ marginRight: '4px' }} /> Reject
                            </button>
                          </>
                        )}
                        {vendor.status === 'ACTIVE' && (
                          <button className="btn btn-secondary" style={{ fontSize: '0.8rem', padding: '0.3rem 0.7rem', color: '#f59e0b', background: 'rgba(245,158,11,0.1)', border: '1px solid rgba(245,158,11,0.25)' }} onClick={() => onHandleVendorReview && onHandleVendorReview(vendor.vendorId, 'suspend')}>
                            Suspend
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="glass-card" style={{ padding: '3rem', textAlign: 'center' }}>
                  <Warehouse size={48} style={{ marginBottom: '1rem', opacity: 0.4 }} />
                  <p style={{ color: 'var(--text-secondary)' }}>No vendors found for the selected filter.</p>
                </div>
              )}

              {/* Vendor Detail Panel */}
              {selectedVendor && (
                <div className="modal-overlay" onClick={() => onSetSelectedVendor && onSetSelectedVendor(null)}>
                  <div className="modal-content" style={{ maxWidth: '560px', textAlign: 'left' }} onClick={(e) => e.stopPropagation()}>
                    <button className="modal-close" onClick={() => onSetSelectedVendor && onSetSelectedVendor(null)}>×</button>
                    <h3 style={{ fontFamily: 'var(--font-heading)', fontWeight: 800, marginBottom: '0.5rem' }}>{selectedVendor.businessName}</h3>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '1rem' }}>{selectedVendor.businessEmail} &nbsp;|&nbsp; {selectedVendor.businessPhone}</p>
                    {selectedVendor.description && <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1rem' }}>{selectedVendor.description}</p>}
                    
                    <div style={{ marginBottom: '1rem' }}>
                      <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '4px' }}>Review Remarks</label>
                      <textarea
                        value={reviewRemarks}
                        onChange={(e) => onSetReviewRemarks && onSetReviewRemarks(e.target.value)}
                        placeholder="Optional: add remarks for this action..."
                        rows={2}
                        style={{ width: '100%', padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)', boxSizing: 'border-box' }}
                      />
                    </div>

                    <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
                      {selectedVendor.status === 'PENDING' && (
                        <>
                          <button className="btn btn-primary" style={{ background: '#10b981', border: 'none' }} onClick={() => onHandleVendorReview && onHandleVendorReview(selectedVendor.vendorId, 'approve')}>
                            <Check size={14} style={{ marginRight: '6px' }} /> Approve Vendor
                          </button>
                          <button className="btn btn-secondary" style={{ color: '#ef4444', background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.25)' }} onClick={() => onHandleVendorReview && onHandleVendorReview(selectedVendor.vendorId, 'reject')}>
                            <X size={14} style={{ marginRight: '6px' }} /> Reject Vendor
                          </button>
                        </>
                      )}
                      {selectedVendor.status === 'ACTIVE' && (
                        <button className="btn btn-secondary" style={{ color: '#f59e0b', background: 'rgba(245,158,11,0.1)', border: '1px solid rgba(245,158,11,0.25)' }} onClick={() => onHandleVendorReview && onHandleVendorReview(selectedVendor.vendorId, 'suspend')}>
                          Suspend Vendor
                        </button>
                      )}
                    </div>

                    {approvalHistory.length > 0 && (
                      <div style={{ marginTop: '1.5rem' }}>
                        <h4 style={{ marginBottom: '0.75rem' }}>Approval History</h4>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', maxHeight: '200px', overflowY: 'auto' }}>
                          {approvalHistory.map((h, idx) => (
                            <div key={idx} style={{ padding: '0.6rem', background: 'var(--input-bg)', borderRadius: '8px', border: '1px solid var(--border-color)', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                              <strong style={{ color: 'var(--text-primary)' }}>{h.action}</strong> — {h.remarks || '—'} <span style={{ color: 'var(--text-muted)' }}>({h.createdAt || h.date || ''})</span>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* TAB: PENDING PRODUCTS */}
          {activeTab === 'products' && (
            <div>
              {pendingProducts.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {pendingProducts.map(p => (
                    <div key={p.id} className="glass-card" style={{ padding: '1.25rem', display: 'flex', gap: '1.25rem', alignItems: 'center', flexWrap: 'wrap' }}>
                      {p.image && (
                        <ProductImage src={p.image} alt={p.productName || p.name} style={{ width: '72px', height: '72px', borderRadius: '10px', objectFit: 'cover', border: '1px solid var(--border-color)', flexShrink: 0 }} />
                      )}
                      <div style={{ flex: 1, minWidth: 0 }}>
                        <h4 style={{ margin: '0 0 4px 0', fontSize: '1rem', fontWeight: 700, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{p.productName}</h4>
                        <p style={{ margin: '0 0 6px 0', fontSize: '0.82rem', color: 'var(--text-secondary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{p.description}</p>
                        <div style={{ display: 'flex', gap: '1rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                          <span>Price: <strong style={{ color: 'var(--text-primary)' }}>${p.price}</strong></span>
                          <span>Stock: <strong style={{ color: 'var(--text-primary)' }}>{p.stockQuantity}</strong></span>
                          {p.brand && <span>Brand: <strong style={{ color: 'var(--text-primary)' }}>{p.brand}</strong></span>}
                        </div>
                      </div>
                      <div style={{ display: 'flex', gap: '0.5rem', flexShrink: 0 }}>
                        <button
                          className="btn btn-primary"
                          style={{ background: '#10b981', border: 'none', padding: '0.4rem 0.9rem', fontSize: '0.85rem' }}
                          onClick={() => onHandleProductReview && onHandleProductReview(p.id, 'approve')}
                        >
                          <Check size={14} style={{ marginRight: '4px' }} /> Approve
                        </button>
                        <button
                          className="btn btn-secondary"
                          style={{ color: '#ef4444', background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.25)', padding: '0.4rem 0.9rem', fontSize: '0.85rem' }}
                          onClick={() => onHandleProductReview && onHandleProductReview(p.id, 'reject')}
                        >
                          <X size={14} style={{ marginRight: '4px' }} /> Reject
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="glass-card" style={{ padding: '3rem', textAlign: 'center' }}>
                  <Check size={48} style={{ marginBottom: '1rem', color: '#10b981', opacity: 0.6 }} />
                  <h3>All products reviewed</h3>
                  <p style={{ color: 'var(--text-secondary)' }}>There are no pending product submissions.</p>
                </div>
              )}
            </div>
          )}

          {/* TAB: CATEGORIES */}
          {activeTab === 'categories' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '1.25rem' }}>
                <button className="btn btn-primary" onClick={() => { onSetCatName && onSetCatName(''); onSetCatDesc && onSetCatDesc(''); onSetShowCategoryForm && onSetShowCategoryForm(true); }}>
                  <Plus size={15} style={{ marginRight: '6px' }} /> New Category
                </button>
              </div>

              {showCategoryForm && (
                <div className="glass-card" style={{ padding: '1.5rem', marginBottom: '1.5rem', maxWidth: '480px' }}>
                  <h3 style={{ margin: '0 0 1rem 0', fontFamily: 'var(--font-heading)', fontWeight: 800 }}>
                    {editingCategory ? 'Edit Category' : 'New Category'}
                  </h3>
                  <form onSubmit={onHandleCategorySubmit} style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    <input
                      type="text"
                      placeholder="Category Name *"
                      value={catName}
                      onChange={(e) => onSetCatName && onSetCatName(e.target.value)}
                      required
                      style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }}
                    />
                    <textarea
                      placeholder="Description (optional)"
                      value={catDesc}
                      onChange={(e) => onSetCatDesc && onSetCatDesc(e.target.value)}
                      rows={2}
                      style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)', resize: 'vertical' }}
                    />
                    <div style={{ display: 'flex', gap: '0.75rem' }}>
                      <button type="button" className="btn btn-secondary" onClick={() => onSetShowCategoryForm && onSetShowCategoryForm(false)}>Cancel</button>
                      <button type="submit" className="btn btn-primary">{editingCategory ? 'Update' : 'Create'} Category</button>
                    </div>
                  </form>
                </div>
              )}

              {categories.length > 0 ? (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: '1rem' }}>
                  {categories.map(cat => (
                    <div key={cat.id} className="glass-card" style={{ padding: '1.25rem', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem' }}>
                      <div style={{ flex: 1, minWidth: 0 }}>
                        <h4 style={{ margin: '0 0 4px 0', fontSize: '0.95rem', fontWeight: 700 }}>{cat.categoryName}</h4>
                        {cat.description && <p style={{ margin: 0, fontSize: '0.82rem', color: 'var(--text-secondary)' }}>{cat.description}</p>}
                      </div>
                      <div style={{ display: 'flex', gap: '0.4rem', flexShrink: 0 }}>
                        <button className="btn btn-secondary" style={{ padding: '0.3rem 0.5rem' }} onClick={() => onStartEditCategory && onStartEditCategory(cat)}>
                          Edit
                        </button>
                        <button className="btn btn-secondary" style={{ padding: '0.3rem 0.5rem', color: '#ef4444', background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.25)' }} onClick={() => onHandleDeleteCategory && onHandleDeleteCategory(cat.id)}>
                          <Trash2 size={13} />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="glass-card" style={{ padding: '3rem', textAlign: 'center' }}>
                  <Layers size={48} style={{ marginBottom: '1rem', opacity: 0.4 }} />
                  <p style={{ color: 'var(--text-secondary)' }}>No categories found. Create one to get started.</p>
                </div>
              )}
            </div>
          )}

          {/* TAB: STAFF PROVISIONING */}
          {activeTab === 'staff' && (
            <div>
              <div className="glass-card" style={{ padding: '1.5rem', maxWidth: '560px' }}>
                <h3 style={{ margin: '0 0 1rem 0', fontFamily: 'var(--font-heading)', fontWeight: 800 }}>Provision Staff Account</h3>
                <form onSubmit={onHandleStaffProvision} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <label style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>Full Name *</label>
                    <input type="text" value={staffName} onChange={(e) => onSetStaffName && onSetStaffName(e.target.value)} required style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }} />
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <label style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>Email *</label>
                    <input type="email" value={staffEmail} onChange={(e) => onSetStaffEmail && onSetStaffEmail(e.target.value)} required style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }} />
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <label style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>Password *</label>
                    <input type="password" value={staffPassword} onChange={(e) => onSetStaffPassword && onSetStaffPassword(e.target.value)} required style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }} />
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <label style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>Phone</label>
                    <input type="text" value={staffPhone} onChange={(e) => onSetStaffPhone && onSetStaffPhone(e.target.value)} style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }} />
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', gridColumn: '1 / -1' }}>
                    <label style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>Role</label>
                    <select value={staffRole} onChange={(e) => onSetStaffRole && onSetStaffRole(e.target.value)} style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }}>
                      <option value="WAREHOUSE_STAFF">Warehouse Staff</option>
                      <option value="SUPPORT_STAFF">Support Staff</option>
                      <option value="FINANCE_STAFF">Finance Staff</option>
                    </select>
                  </div>
                  <div style={{ gridColumn: '1 / -1' }}>
                    <button type="submit" className="btn btn-primary">Create Staff Account</button>
                  </div>
                </form>
              </div>
            </div>
          )}

          {/* TAB: CUSTOMERS */}
          {activeTab === 'customers' && (
            <div>
              {customers.length > 0 ? (
                <div className="glass-card" style={{ padding: '1rem', overflowX: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' }}>
                    <thead>
                      <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)', fontSize: '0.78rem', textTransform: 'uppercase' }}>
                        <th style={{ padding: '10px 12px', textAlign: 'left' }}>Customer</th>
                        <th style={{ padding: '10px 12px', textAlign: 'left' }}>Email</th>
                        <th style={{ padding: '10px 12px', textAlign: 'left' }}>Phone</th>
                        <th style={{ padding: '10px 12px', textAlign: 'left' }}>Joined</th>
                      </tr>
                    </thead>
                    <tbody>
                      {customers.map((c) => (
                        <tr key={c.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                          <td style={{ padding: '10px 12px', fontWeight: 600, color: 'var(--text-primary)' }}>
                            {c.firstName} {c.lastName}
                          </td>
                          <td style={{ padding: '10px 12px', color: 'var(--text-secondary)' }}>{c.email}</td>
                          <td style={{ padding: '10px 12px', color: 'var(--text-secondary)' }}>{c.phone || '—'}</td>
                          <td style={{ padding: '10px 12px', color: 'var(--text-muted)', fontSize: '0.82rem' }}>{c.createdAt ? new Date(c.createdAt).toLocaleDateString() : '—'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div className="glass-card" style={{ padding: '3rem', textAlign: 'center' }}>
                  <User size={48} style={{ marginBottom: '1rem', opacity: 0.4 }} />
                  <p style={{ color: 'var(--text-secondary)' }}>No customers found.</p>
                </div>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
}
