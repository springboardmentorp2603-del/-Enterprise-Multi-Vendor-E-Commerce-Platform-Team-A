import useFilteredProducts from '../../hooks/useFilteredProducts';
import ProductImage from '../../components/ProductImage';
import EmptyState from '../../components/EmptyState';
import NotificationPanel from '../../components/NotificationPanel';
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
  Check, 
  X, 
  Lock as LockIcon, 
  RefreshCw, 
  Key, 
  Info, 
  Eye, 
  ShoppingBag,
  Heart,
  Bell,
  Truck,
  TrendingUp,
  Package,
  CheckCircle2,
  AlertTriangle,
  User
} from 'lucide-react';
import './CustomerDashboard.css';

export default function CustomerDashboard({
  user,
  products = [],
  categories = [],
  selectedCategory,
  searchKeyword = '',
  onSearchKeywordChange,
  priceMin = '',
  priceMax = '',
  onPriceMinChange,
  onPriceMaxChange,
  loading = false,
  cart = [],
  setCart,
  addToast,
  customerProfile,
  addresses = [],
  showAddressForm,
  onShowAddressForm,
  addressLine1 = '',
  addressLine2 = '',
  city = '',
  stateName = '',
  postalCode = '',
  country = '',
  onAddressLine1Change,
  onAddressLine2Change,
  onCityChange,
  onStateNameChange,
  onPostalCodeChange,
  onCountryChange,
  selectedProduct,
  reviews = [],
  newRating = 5,
  newComment = '',
  reviewLoading = false,
  onNewRatingChange,
  onNewCommentChange,
  onHandleSearch,
  onHandleCategoryClick,
  onHandleAddToCart,
  onViewProductDetails,
  onSubmitReview,
  onDeleteReview,
  onHandleAddAddress,
  onSetSelectedProduct,
  wishlist = [],
  setWishlist,
}) {
  const filteredProducts = useFilteredProducts(products, { priceMin, priceMax, selectedCategory, searchKeyword });

  
  return (
    <div style={{ padding: '2rem' }}>
      <NotificationPanel />
              
      {/* Customer Profile Section */}
      {customerProfile && (
        <div style={{ marginTop: '3rem', borderTop: '1px solid var(--border-color)', paddingTop: '2rem' }}>
          <h2 style={{ fontFamily: 'var(--font-heading)', fontWeight: 800, marginBottom: '1rem' }}>My Profile</h2>
          <div className="glass-card" style={{ padding: '1.5rem', marginBottom: '1.5rem' }}>
            <p><strong>Name:</strong> {customerProfile.firstName} {customerProfile.lastName}</p>
            <p><strong>Email:</strong> {customerProfile.email}</p>
            {customerProfile.phone && <p><strong>Phone:</strong> {customerProfile.phone}</p>}
          </div>

          {/* Addresses */}
          <h3 style={{ marginBottom: '1rem' }}>Saved Addresses</h3>
          {addresses.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginBottom: '1rem' }}>
              {addresses.map((addr) => (
                <div key={addr.id} className="glass-card" style={{ padding: '1rem' }}>
                  <p style={{ margin: 0 }}>{addr.addressLine1}{addr.addressLine2 ? `, ${addr.addressLine2}` : ''}</p>
                  <p style={{ margin: 0, color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
                    {addr.city}, {addr.state} {addr.postalCode}, {addr.country}
                  </p>
                  {addr.isDefault && <span style={{ fontSize: '0.75rem', color: '#10b981', fontWeight: 600 }}>Default</span>}
                  <div style={{ marginTop: '0.75rem',textAlign: 'right' }}>
  <button
    className="btn btn-danger"
    onClick={() => onDeleteAddress(addr.id)}
  >
    Delete Address
  </button>
</div>
                </div>
                
              ))}
              
            </div>
          ) : (
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1rem' }}>No addresses saved yet.</p>
          )}

          {/* Add Address Form */}
          <button
            className="btn btn-secondary"
            style={{ marginBottom: '1rem' }}
            onClick={() => onShowAddressForm && onShowAddressForm(!showAddressForm)}
          >
            {showAddressForm ? 'Cancel' : '+ Add New Address'}
          </button>

          {showAddressForm && (
            <form onSubmit={onHandleAddAddress} className="glass-card" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <h4 style={{ margin: '0 0 0.5rem 0' }}>New Address</h4>
              <input type="text" placeholder="Address Line 1 *" value={addressLine1} onChange={(e) => onAddressLine1Change && onAddressLine1Change(e.target.value)} required style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }} />
              <input type="text" placeholder="Address Line 2" value={addressLine2} onChange={(e) => onAddressLine2Change && onAddressLine2Change(e.target.value)} style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }} />
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                <input type="text" placeholder="City *" value={city} onChange={(e) => onCityChange && onCityChange(e.target.value)} required style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }} />
                <input type="text" placeholder="State *" value={stateName} onChange={(e) => onStateNameChange && onStateNameChange(e.target.value)} required style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }} />
                <input type="text" placeholder="Postal Code *" value={postalCode} onChange={(e) => onPostalCodeChange && onPostalCodeChange(e.target.value)} required style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }} />
                <input type="text" placeholder="Country *" value={country} onChange={(e) => onCountryChange && onCountryChange(e.target.value)} required style={{ padding: '0.5rem', background: 'var(--input-bg)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)' }} />
              </div>
              <button type="submit" className="btn btn-primary" style={{ alignSelf: 'flex-start' }}>Save Address</button>
            </form>
          )}
        </div>
      )}
    </div>
  );
}
