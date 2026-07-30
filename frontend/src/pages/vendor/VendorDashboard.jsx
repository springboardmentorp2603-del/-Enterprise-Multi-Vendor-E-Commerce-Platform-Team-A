import { useState, useMemo } from 'react';
import { 
  Layers, 
  Search, 
  Plus, 
  Minus, 
  Trash2, 
  ArrowLeft, 
  Sun, 
  Moon, 
  Shield, 
  BarChart2, 
  Star, 
  PlusCircle, 
  Check, 
  X, 
  Camera, 
  Lock as LockIcon, 
  RefreshCw, 
  Key, 
  Info,  
  Eye, 
  ShoppingBag,
  TrendingUp,
  Package,
  AlertTriangle,
  DollarSign,
  User
} from 'lucide-react';
import './VendorDashboard.css';
import ProductImage from '../../components/ProductImage';

export default function VendorDashboard({
  user,
  vendorProfile,
  myProducts = [],
  categories = [],
  loading = false,
  showProductForm = false,
  editingProduct = null,
  prodName = '',
  prodBrand = '',
  prodDesc = '',
  prodPrice = '',
  prodStock = '',
  prodCategory = '',
  prodFeatured = false,
  prodActive = true,
  docType = 'GST_CERTIFICATE',
  docUrl = '',
  docLoading = false,
  onSetShowProductForm,
  onSetEditingProduct,
  onSetProdName,
  onSetProdBrand,
  onSetProdDesc,
  onSetProdPrice,
  onSetProdStock,
  onSetProdCategory,
  onSetProdFeatured,
  onSetProdActive,
  onSetDocType,
  onSetDocUrl,
  onHandleProductSubmit,
  onStartEditProduct,
  onHandleDeleteProduct,
  onHandleDocumentSubmit,
  onResetProductForm,
  imagePreview = '',
  onHandleImageSelect,
  addToast
}) {
  const [activeTab, setActiveTab] = useState('products');
  const [productSearch, setProductSearch] = useState('');
  const [selectedCategoryTab, setSelectedCategoryTab] = useState('all');
  const [deleteConfirmProduct, setDeleteConfirmProduct] = useState(null);
  const [detailProduct, setDetailProduct] = useState(null);
  const [detailActiveImgIdx, setDetailActiveImgIdx] = useState(0);
  const [toasts, setToasts] = useState([]);

  const addLocalToast = (message, type = 'success') => {
    const id = Date.now();
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 3000);
  };

  // Stats computed from myProducts (backend data)
  const stats = useMemo(() => {
    const safeProducts = (myProducts || []).filter(Boolean);
    const totalCount = safeProducts.length;
    const stockValue = safeProducts.reduce((acc, p) => acc + (Number(p.price) || 0) * (Number(p.stockQuantity) || 0), 0);
    const lowStockCount = safeProducts.filter((p) => (p.stockQuantity !== undefined ? p.stockQuantity : 0) < 5).length;
    const pendingCount = safeProducts.filter((p) => p.approvalStatus === 'PENDING').length;
    const approvedCount = safeProducts.filter((p) => p.approvalStatus === 'APPROVED' || p.active).length;
    const totalReviews = safeProducts.reduce((acc, p) => acc + (p.reviewCount || 0), 0);
    const avgRating = totalCount > 0
      ? (safeProducts.reduce((acc, p) => acc + (p.averageRating || 0), 0) / totalCount).toFixed(1)
      : '0.0';

    return {
      totalCount,
      stockValue: stockValue.toLocaleString('en-US', { style: 'currency', currency: 'USD' }),
      lowStockCount,
      pendingCount,
      approvedCount,
      avgRating,
      totalReviews
    };
  }, [myProducts]);

  // Filter products for list view
  const filteredProducts = useMemo(() => {
    return (myProducts || []).filter((p) => {
      if (!p) return false;
      const catName = (p.categoryName || '').toLowerCase();
      const matchesCategory = selectedCategoryTab === 'all' || catName === selectedCategoryTab;
      const searchLower = productSearch.toLowerCase();
      const matchesSearch = !productSearch ||
        (p.productName || '').toLowerCase().includes(searchLower) ||
        (p.description || '').toLowerCase().includes(searchLower);
      return matchesCategory && matchesSearch;
    });
  }, [myProducts, selectedCategoryTab, productSearch]);

  const handleCancelProductForm = () => {
    onSetShowProductForm && onSetShowProductForm(false);
    onSetEditingProduct && onSetEditingProduct(null);
    onResetProductForm && onResetProductForm();
  };

  return (
    <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>

      {/* Toast Notifications */}
      <div style={{ position: 'fixed', top: '80px', right: '24px', zIndex: 9999, display: 'flex', flexDirection: 'column', gap: '8px' }}>
        {toasts.map((t) => (
          <div key={t.id} style={{ background: t.type === 'success' ? 'rgba(16,185,129,0.9)' : t.type === 'error' ? 'rgba(239,68,68,0.9)' : 'rgba(59,130,246,0.9)', color: '#fff', padding: '10px 18px', borderRadius: '10px', fontSize: '13px', fontWeight: 600 }}>
            {t.message}
          </div>
        ))}
      </div>

      {/* Vendor Profile Header */}
      {vendorProfile && (
        <div className="glass-card" style={{ padding: '1.5rem', marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <h2 style={{ margin: 0, fontFamily: 'var(--font-heading)', fontWeight: 800 }}>{vendorProfile.businessName}</h2>
            <p style={{ margin: '4px 0 0 0', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
              {vendorProfile.businessEmail} &nbsp;|&nbsp; Status:&nbsp;
              <span style={{ color: vendorProfile.status === 'ACTIVE' ? '#10b981' : vendorProfile.status === 'PENDING' ? '#f59e0b' : '#ef4444', fontWeight: 700 }}>
                {vendorProfile.status}
              </span>
            </p>
          </div>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button
              className={`btn ${activeTab === 'products' ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setActiveTab('products')}
            >
              <Package size={15} style={{ marginRight: '6px' }} /> Products
            </button>
            <button
              className={`btn ${activeTab === 'documents' ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setActiveTab('documents')}
            >
              <Shield size={15} style={{ marginRight: '6px' }} /> Documents
            </button>
          </div>
        </div>
      )}

      {/* Stats Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '1rem', marginBottom: '2rem' }}>
        <div className="glass-card" style={{ padding: '1.25rem' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700, marginBottom: '4px' }}>Total Products</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--text-primary)' }}>{stats.totalCount}</div>
        </div>
        <div className="glass-card" style={{ padding: '1.25rem' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700, marginBottom: '4px' }}>Est. Stock Value</div>
          <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--text-primary)' }}>{stats.stockValue}</div>
        </div>
        <div className="glass-card" style={{ padding: '1.25rem' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700, marginBottom: '4px' }}>Low Stock</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: stats.lowStockCount > 0 ? '#ef4444' : 'var(--text-primary)' }}>{stats.lowStockCount}</div>
        </div>
        <div className="glass-card" style={{ padding: '1.25rem' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700, marginBottom: '4px' }}>Avg Rating</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--text-primary)' }}>{stats.avgRating}</div>
        </div>
        <div className="glass-card" style={{ padding: '1.25rem' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700, marginBottom: '4px' }}>Pending</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: stats.pendingCount > 0 ? '#f59e0b' : 'var(--text-primary)' }}>{stats.pendingCount}</div>
        </div>
      </div>

      {/* TAB: PRODUCTS */}
      {activeTab === 'products' && (
        <div>
          {/* Controls */}
          <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem', flexWrap: 'wrap', alignItems: 'center' }}>
            <div style={{ flex: 1, minWidth: '200px', display: 'flex', alignItems: 'center', gap: '8px', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', padding: '0.4rem 0.75rem' }}>
              <Search size={15} style={{ color: 'var(--text-muted)' }} />
              <input
                type="text"
                placeholder="Search products..."
                value={productSearch}
                onChange={(e) => setProductSearch(e.target.value)}
                style={{ background: 'transparent', border: 'none', outline: 'none', color: 'var(--text-primary)', width: '100%' }}
              />
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
              {['all', 'electronics', 'fashion', 'home', 'beauty'].map(cat => (
                <button
                  key={cat}
                  className={`btn ${selectedCategoryTab === cat ? 'btn-primary' : 'btn-secondary'}`}
                  style={{ padding: '0.3rem 0.7rem', fontSize: '0.8rem', textTransform: 'capitalize' }}
                  onClick={() => setSelectedCategoryTab(cat)}
                >
                  {cat === 'all' ? 'All' : cat}
                </button>
              ))}
            </div>
            <button
              className="btn btn-primary"
              onClick={() => { onResetProductForm && onResetProductForm(); onSetShowProductForm && onSetShowProductForm(true); }}
              style={{ padding: '0.4rem 1rem' }}
            >
              <Plus size={15} style={{ marginRight: '5px' }} /> Add Product
            </button>
          </div>

          {/* Product Form */}
          {showProductForm && (
            <div className="glass-card" style={{ padding: '1.5rem', marginBottom: '1.5rem' }}>
              <h3 style={{ margin: '0 0 1rem 0', fontFamily: 'var(--font-heading)', fontWeight: 800 }}>
                {editingProduct ? 'Edit Product' : 'Add New Product'}
              </h3>
              <form onSubmit={onHandleProductSubmit} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Product Name *</label>
                  <input type="text" value={prodName} onChange={(e) => onSetProdName && onSetProdName(e.target.value)} required style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Brand</label>
                  <input type="text" value={prodBrand} onChange={(e) => onSetProdBrand && onSetProdBrand(e.target.value)} style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Price (USD) *</label>
                  <input type="number" step="0.01" value={prodPrice} onChange={(e) => onSetProdPrice && onSetProdPrice(e.target.value)} required style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Stock Quantity *</label>
                  <input type="number" value={prodStock} onChange={(e) => onSetProdStock && onSetProdStock(e.target.value)} required style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Category *</label>
                  <select value={prodCategory} onChange={(e) => onSetProdCategory && onSetProdCategory(e.target.value)} required style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }}>
                    {(categories || []).map(cat => (
                      <option key={cat.id} value={cat.id}>{cat.categoryName || cat.name}</option>
                    ))}
                  </select>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <input type="checkbox" id="prodFeatured" checked={prodFeatured} onChange={(e) => onSetProdFeatured && onSetProdFeatured(e.target.checked)} />
                  <label htmlFor="prodFeatured" style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Featured Product</label>
                  {editingProduct && (
                    <>
                      <input type="checkbox" id="prodActive" checked={prodActive} onChange={(e) => onSetProdActive && onSetProdActive(e.target.checked)} style={{ marginLeft: '1rem' }} />
                      <label htmlFor="prodActive" style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Active</label>
                    </>
                  )}
                </div>
                <div style={{ gridColumn: '1 / -1', display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Description</label>
                  <textarea value={prodDesc} onChange={(e) => onSetProdDesc && onSetProdDesc(e.target.value)} rows={3} style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)', resize: 'vertical' }} />
                </div>
                <div style={{ gridColumn: '1 / -1', display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Product Image</label>
                  <div style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start' }}>
                    <div style={{ flex: 1 }}>
                      <input
                        type="file"
                        accept="image/*"
                        onChange={onHandleImageSelect}
                        style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)', width: '100%', cursor: 'pointer' }}
                      />
                      <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', margin: '4px 0 0 0' }}>Supported: JPG, PNG, WebP (Max 5MB)</p>
                    </div>
                    {imagePreview && (
                      <div style={{ width: '100px', height: '100px', borderRadius: '8px', overflow: 'hidden', border: '1px solid var(--border-color)', flexShrink: 0 }}>
                        <ProductImage src={imagePreview} alt="Preview" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                      </div>
                    )}
                  </div>
                  {imagePreview && (
                    <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      ℹ️ Image preview shown. Backend integration pending.
                    </p>
                  )}
                </div>
                <div style={{ gridColumn: '1 / -1', display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
                  <button type="button" className="btn btn-secondary" onClick={handleCancelProductForm}>Cancel</button>
                  <button type="submit" className="btn btn-primary">{editingProduct ? 'Update Product' : 'Submit for Approval'}</button>
                </div>
              </form>
            </div>
          )}

          {/* Products List */}
          {filteredProducts.length > 0 ? (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '1.25rem' }}>
              {filteredProducts.map(p => (
                <div key={p.id} className="glass-card" style={{ padding: '1.25rem' }}>
                  <ProductImage src={p.image} alt={p.productName || p.name || 'Product image'} style={{ width: '100%', height: '160px', objectFit: 'cover', borderRadius: '8px', marginBottom: '0.75rem' }} />
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '4px' }}>
                    <h4 style={{ margin: 0, fontSize: '0.95rem', fontWeight: 700, color: 'var(--text-primary)' }}>{p.productName || p.name || 'Untitled product'}</h4>
                    <span style={{ fontWeight: 700, color: 'var(--secondary)', whiteSpace: 'nowrap', marginLeft: '8px' }}>₹{p.price}</span>
                  </div>
                  {p.brand && <p style={{ margin: '0 0 4px 0', fontSize: '0.78rem', color: 'var(--text-muted)' }}>Brand: {p.brand}</p>}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                    <span style={{ fontSize: '0.78rem', color: (p.stockQuantity || 0) < 5 ? '#ef4444' : '#10b981' }}>
                      Stock: {p.stockQuantity || 0}
                    </span>
                    <span style={{ fontSize: '0.75rem', fontWeight: 700, padding: '2px 8px', borderRadius: '8px', background: p.approvalStatus === 'PENDING' ? 'rgba(245,158,11,0.15)' : p.approvalStatus === 'REJECTED' ? 'rgba(239,68,68,0.15)' : 'rgba(16,185,129,0.15)', color: p.approvalStatus === 'PENDING' ? '#f59e0b' : p.approvalStatus === 'REJECTED' ? '#ef4444' : '#10b981' }}>
                      {p.approvalStatus || 'ACTIVE'}
                    </span>
                  </div>
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button className="btn btn-secondary" style={{ flex: 1, padding: '0.35rem', fontSize: '0.8rem', justifyContent: 'center' }} onClick={() => onStartEditProduct && onStartEditProduct(p)}>
                      <Eye size={13} style={{ marginRight: '4px' }} /> Edit
                    </button>
                    <button className="btn btn-secondary" style={{ padding: '0.35rem 0.6rem', background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.25)', color: '#ef4444' }} onClick={() => setDeleteConfirmProduct(p)}>
                      <Trash2 size={13} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="glass-card" style={{ padding: '3rem', textAlign: 'center' }}>
              <Package size={48} style={{ marginBottom: '1rem', opacity: 0.4 }} />
              <p style={{ color: 'var(--text-secondary)' }}>No products found. Add your first product to get started.</p>
            </div>
          )}
        </div>
      )}

      {/* TAB: DOCUMENTS */}
      {activeTab === 'documents' && (
        <div>
          <div className="glass-card" style={{ padding: '1.5rem', maxWidth: '560px' }}>
            <h3 style={{ margin: '0 0 1rem 0', fontFamily: 'var(--font-heading)', fontWeight: 800 }}>Upload Verification Document</h3>
            <form onSubmit={onHandleDocumentSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Document Type</label>
                <select value={docType} onChange={(e) => onSetDocType && onSetDocType(e.target.value)} style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }}>
                  <option value="GST_CERTIFICATE">GST Certificate</option>
                  <option value="PAN_CARD">PAN Card</option>
                  <option value="BANK_STATEMENT">Bank Statement</option>
                  <option value="BUSINESS_REGISTRATION">Business Registration</option>
                </select>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Document URL *</label>
                <input type="text" placeholder="https://..." value={docUrl} onChange={(e) => onSetDocUrl && onSetDocUrl(e.target.value)} required style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }} />
              </div>
              <button type="submit" className="btn btn-primary" disabled={docLoading} style={{ alignSelf: 'flex-start' }}>
                {docLoading ? 'Uploading...' : 'Upload Document'}
              </button>
            </form>
          </div>

          {/* Existing Documents */}
          {vendorProfile && vendorProfile.documents && vendorProfile.documents.length > 0 && (
            <div style={{ marginTop: '2rem' }}>
              <h4 style={{ marginBottom: '1rem' }}>Uploaded Documents</h4>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {vendorProfile.documents.map((doc, idx) => (
                  <div key={idx} className="glass-card" style={{ padding: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <strong style={{ fontSize: '0.9rem' }}>{doc.documentType}</strong>
                      <p style={{ margin: '2px 0 0 0', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{doc.documentUrl}</p>
                    </div>
                    <span style={{ fontSize: '0.75rem', padding: '2px 8px', borderRadius: '8px', background: doc.verified ? 'rgba(16,185,129,0.15)' : 'rgba(245,158,11,0.15)', color: doc.verified ? '#10b981' : '#f59e0b', fontWeight: 700 }}>
                      {doc.verified ? 'Verified' : 'Pending'}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Delete Confirm Modal */}
      {deleteConfirmProduct && (
        <div className="modal-overlay" onClick={() => setDeleteConfirmProduct(null)}>
          <div className="modal-content" style={{ maxWidth: '380px', textAlign: 'center' }} onClick={(e) => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setDeleteConfirmProduct(null)}>×</button>
            <div style={{ padding: '1rem 0' }}>
              <Trash2 size={36} style={{ color: '#ef4444', marginBottom: '1rem' }} />
              <h3 style={{ margin: '0 0 0.5rem 0' }}>Delete Product?</h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
                Are you sure you want to permanently delete "{deleteConfirmProduct.productName}"?
              </p>
              <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center' }}>
                <button className="btn btn-secondary" onClick={() => setDeleteConfirmProduct(null)}>Cancel</button>
                <button
                  className="btn btn-danger"
                  style={{ background: '#ef4444', color: '#fff', border: 'none' }}
                  onClick={() => {
                    onHandleDeleteProduct && onHandleDeleteProduct(deleteConfirmProduct.id);
                    setDeleteConfirmProduct(null);
                  }}
                >
                  Confirm Delete
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
