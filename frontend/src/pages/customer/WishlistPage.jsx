import { api } from '../../api';
import { useState, useMemo, useRef, useEffect } from 'react';
import { 
  Layers, 
  User, 
  ShoppingBag, 
  Heart, 
  Search, 
  Trash2, 
  ArrowLeft, 
  Sun, 
  Moon, 
  Share2, 
  Bell, 
  Plus, 
  Minus, 
  Star, 
  X, 
  Check, 
  Copy,
  AlertCircle
} from 'lucide-react';
import './WishlistPage.css';
import ProductImage from '../../components/ProductImage';

export default function WishlistPage({ 
  onNavigate, 
  user, 
  onLogout, 
  theme, 
  onToggleTheme, 
  cart, 
  onSaveCart, 
  wishlist, 
  onSaveWishlist,
  products = [],
  notifications = [],
  onCheckout
}) {
  const [searchQuery, setSearchQuery] = useState('');
  const [priceAlerts, setPriceAlerts] = useState(false);
  const [stockAlerts, setStockAlerts] = useState(false);
  const [shareModalOpen, setShareModalOpen] = useState(false);
  const [copied, setCopied] = useState(false);
  
  // Cart Drawer & Profiles States
  const [cartOpen, setCartOpen] = useState(false);
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);
  
  const unreadCount = useMemo(() => {
    if (!user) return 0;
    return notifications.filter(n => n.customerEmail === user.email && !n.read).length;
  }, [notifications, user]);

  const [clearConfirmOpen, setClearConfirmOpen] = useState(false);
  const [toasts, setToasts] = useState([]);
  const toastIdRef = useRef(0);
  const shareIdRef = useRef(1001);

  const [backendWishlist, setBackendWishlist] = useState([]);
  const [loading, setLoading] = useState(false);

  // Toast Helper
  const addToast = (message, type = 'success') => {
    toastIdRef.current += 1;
    const id = toastIdRef.current;
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 3000);
  };

  // ✅ LOAD WISHLIST FROM BACKEND ON MOUNT
  useEffect(() => {
    if (user) {
      loadWishlistFromBackend();
    }
  }, [user]);

  const loadWishlistFromBackend = async () => {
  try {
    setLoading(true);

    const response = await api.wishlist.getWishlist(user.id);

    const wishlistItems = Array.isArray(response?.data)
      ? response.data
      : [];

    // Keep complete backend wishlist objects
    setBackendWishlist(wishlistItems);

    // IMPORTANT:
    // Update the frontend wishlist with product IDs
    const productIds = wishlistItems
      .map(item => item.productId)
      .filter(Boolean);

    onSaveWishlist(productIds);

  } catch (err) {
    console.error('Failed to load wishlist:', err);
  } finally {
    setLoading(false);
  }
};

  // Mock Stock level generator based on product ID
  const getStockStatus = (product) => {
  const stock = Number(
    product.stock ??
    product.stockQuantity ??
    product.quantity ??
    product.availableQuantity ??
    0
  );

  if (stock <= 0) {
    return {
      status: 'out-of-stock',
      text: 'Out of Stock'
    };
  }

  if (stock <= 2) {
    return {
      status: 'low-stock',
      text: `Only ${stock} Left!`
    };
  }

  return {
    status: 'in-stock',
    text: 'In Stock'
  };
};

  // Filter products in the wishlist
  const wishlistedProducts = useMemo(() => {
    const approvedProducts = products.filter((p) => !p.approvalStatus || String(p.approvalStatus).toLowerCase() === 'approved');
    return approvedProducts.filter((product) => wishlist.includes(product.id)).filter((product) => {
      const query = searchQuery.toLowerCase();
      return String(product.name || product.productName || '').toLowerCase().includes(query) ||
             String(product.vendor || '').toLowerCase().includes(query) ||
             String(product.category || '').toLowerCase().includes(query);
    });
  }, [wishlist, searchQuery, products]);

  // Total wishlist item count
  const totalWishlistCount = wishlist.length;

  // Add individual product to Cart
// Add individual product to Cart
const handleAddToCart = async (product) => {
  const stockInfo = getStockStatus(product);

  if (stockInfo.status === 'out-of-stock') {
    addToast('Cannot add out-of-stock items to cart.', 'info');
    return;
  }

  try {
    // 1. Find wishlist item from backend
    const wishlistItem = backendWishlist.find(
      item => (item.product?.id || item.productId) === product.id
    );

    // 2. Add product to cart
    const existing = cart.find(
      item => item.product?.id === product.id
    );

    if (existing) {
      const updatedCart = cart.map(item =>
        item.product?.id === product.id
          ? { ...item, quantity: item.quantity + 1 }
          : item
      );

      onSaveCart(updatedCart);
    } else {
      onSaveCart([
        ...cart,
        {
          product,
          quantity: 1
        }
      ]);
    }

    // 3. Remove from backend wishlist
    if (user && wishlistItem?.id) {
      await api.wishlist.removeFromWishlist(
        user.id,
        wishlistItem.id
      );
    }

    // 4. Remove from frontend wishlist immediately
    const updatedWishlist = wishlist.filter(
      id => id !== product.id
    );

    onSaveWishlist(updatedWishlist);

    // 5. Update local backend wishlist state
    if (wishlistItem?.id) {
      setBackendWishlist(prev =>
        prev.filter(item => item.id !== wishlistItem.id)
      );
    }

    addToast(
      `"${product.name}" moved to cart and removed from wishlist.`,
      'success'
    );

  } catch (err) {
    console.error('Move to cart failed:', err);

    addToast(
      err.message || 'Failed to move product to cart',
      'error'
    );
  }
};

  // Remove individual product from Wishlist
  const handleRemoveFromWishlist = async (productId) => {
     try {
      if (user) {
        const wishlistItem = backendWishlist.find(
  item => (item.product?.id || item.productId) === productId
);
        if (wishlistItem) {
          await api.wishlist.removeFromWishlist(user.id, wishlistItem.id);
          // Reload wishlist from backend
          await loadWishlistFromBackend();
        }
      }
    const updated = wishlist.filter((id) => id !== productId);
    onSaveWishlist(updated);
    addToast('Item removed from wishlist.', 'info');
  } catch (err) {
    addToast(err.message || 'Failed to remove from wishlist', 'error');
  }
};

  // Bulk: Move All In-Stock items to Cart
  const handleMoveAllToCart = () => {
   const inStockWishlistItems = products.filter(
  (p) =>
    wishlist.includes(p.id) &&
    getStockStatus(p).status !== 'out-of-stock'
);
    
    if (inStockWishlistItems.length === 0) {
      addToast('No in-stock wishlist items to move.', 'info');
      return;
    }

    let updatedCart = [...cart];
    inStockWishlistItems.forEach((product) => {
      const existing = updatedCart.find((item) => item.product.id === product.id);
      if (existing) {
        updatedCart = updatedCart.map((item) => 
          item.product.id === product.id 
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      } else {
        updatedCart.push({ product, quantity: 1 });
      }
    });

    onSaveCart(updatedCart);
    // Remove moved items from wishlist
    const movedIds = inStockWishlistItems.map((p) => p.id);
    const updatedWishlist = wishlist.filter((id) => !movedIds.includes(id));
    onSaveWishlist(updatedWishlist);
    addToast(`Moved ${inStockWishlistItems.length} items to shopping cart!`, 'success');
  };

  // Bulk: Clear entire Wishlist
  const handleClearWishlist = () => {
    if (wishlist.length === 0) return;
    setClearConfirmOpen(true);
  };

  // Share Wishlist link mock copy
  const handleCopyShareLink = () => {
    shareIdRef.current += 1;
    const mockUrl = `https://shopstack.platform/wishlist/share/usr_${user?.avatar ? user.avatar.toLowerCase() : 'guest'}_${shareIdRef.current}`;
    navigator.clipboard.writeText(mockUrl).then(() => {
      setCopied(true);
      addToast('Share link copied to clipboard!');
      setTimeout(() => setCopied(false), 2000);
    });
  };

  // Toggle Price Drop alert mock
  const handleTogglePriceAlerts = () => {
    const nextVal = !priceAlerts;
    setPriceAlerts(nextVal);
    addToast(nextVal ? 'Price drop email notifications enabled!' : 'Price drop notifications disabled.', 'info');
  };

  // Toggle Stock alert mock
  const handleToggleStockAlerts = () => {
    const nextVal = !stockAlerts;
    setStockAlerts(nextVal);
    addToast(nextVal ? 'Back-in-stock email alerts enabled!' : 'Back-in-stock alerts disabled.', 'info');
  };

  // Cart operations inside Drawer
  const handleRemoveFromCart = (productId) => {
    const updated = cart.filter((item) => item.product.id !== productId);
    onSaveCart(updated);
    addToast('Item removed from cart.', 'info');
  };

  const handleUpdateQuantity = (productId, delta) => {
    const MAX_ITEMS = 10;
    const updated = cart.map((item) => {
      if (item.product.id === productId) {
        const newQty = item.quantity + delta;

        if (newQty > MAX_ITEMS) {
          addToast('Maximum 10 items per product aloowed','error');
          return item;
        }

        if (newQty < 1){
          return { ...item, quantity: 1 };
        }
        return { ...item, quantity: newQty};
      }
      return item;
    });
    onSaveCart(updated);
  };

  const totalCartCount = cart.reduce((acc, item) => acc + item.quantity, 0);
  const totalCartPrice = cart.reduce((acc, item) => acc + item.product.price * item.quantity, 0).toFixed(2);

  return (
    <div className={`wishlist-page-container ${theme === 'light' ? 'light-theme' : ''}`}>
      
      {/* Background blobs */}
      <div className="bg-blobs">
        <div className="bg-blob blob-1"></div>
        <div className="bg-blob blob-2"></div>
        <div className="bg-blob blob-3"></div>
      </div>

      {/* Navbar */}
      <header className="glass-navbar glass-element">
        <div className="section-width navbar-content">
          <a href="#" className="brand" onClick={(e) => { e.preventDefault(); onNavigate('landing'); }}>
            <div className="brand-icon-wrapper" style={{ background: 'var(--button-primary)' }}>
              <Layers size={22} color="#fff" />
            </div>
            <span>Shop<span className="text-gradient">Stack</span></span>
          </a>

          <nav>
            <ul className="nav-links">
              <li><a href="#" onClick={(e) => { e.preventDefault(); onNavigate('landing'); }}>Home</a></li>
              <li><a href="#" onClick={(e) => { e.preventDefault(); onNavigate('landing'); setTimeout(() => document.getElementById('browse-section')?.scrollIntoView({ behavior: 'smooth' }), 100); }}>Browse Shop</a></li>
              <li><a href="#" className="active" onClick={(e) => e.preventDefault()}>Wishlist</a></li>
            </ul>
          </nav>

          <div className="nav-actions">
            {/* Dark Mode toggle button */}
            <button className="icon-btn" onClick={onToggleTheme} title="Toggle dark/light mode">
              {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
            </button>

            {/* Cart toggle */}
            <button className="icon-btn" onClick={() => setCartOpen(true)} title="View cart">
              <ShoppingBag size={18} />
              {totalCartCount > 0 && <span className="badge">{totalCartCount}</span>}
            </button>

            {!user ? (
              <button className="primary-btn" onClick={() => onNavigate('login')}>
                <User size={16} />
                <span>Sign In</span>
              </button>
            ) : (
              <div className="profile-menu-container">
                 <button 
                  className="profile-trigger"
                  onClick={() => setProfileMenuOpen(!profileMenuOpen)}
                  style={{ position: 'relative' }}
                >
                  <div className="avatar-circle">{user.avatar}</div>
                  <span style={{ maxWidth: '120px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {user.name.split(' ')[0]}
                  </span>
                  {unreadCount > 0 && (
                    <span className="nav-notification-dot" style={{ position: 'absolute', top: '2px', left: '26px', width: '8px', height: '8px', borderRadius: '50%', background: '#ef4444', border: '1px solid var(--bg-dark)', boxShadow: '0 0 8px #ef4444' }}></span>
                  )}
                </button>
                {profileMenuOpen && (
                  <div className="profile-dropdown glass-card">
                    <div className="dropdown-user-info">
                      <span className="dropdown-user-name">{user.name}</span>
                      <span className="dropdown-user-email">{user.email}</span>
                      <span className={`role-tag ${user.role}`}>
                        {user.role}
                      </span>
                    </div>
                    <button 
                      className="dropdown-item"
                      onClick={() => {
                        setProfileMenuOpen(false);
                        if (user.role === 'vendor') {
                          onNavigate('vendor-dashboard');
                        } else {
                          onNavigate('customer-dashboard');
                        }
                      }}
                    >
                      <Layers size={14} />
                      <span>{user.role === 'customer' ? 'My Orders' : 'Vendor Dashboard'}</span>
                      {unreadCount > 0 && (
                        <span className="badge" style={{ marginLeft: 'auto', background: '#ef4444', color: '#fff', fontSize: '10px', padding: '2px 6px', borderRadius: '10px', position: 'static' }}>
                          {unreadCount}
                        </span>
                      )}
                    </button>
                    <button 
                      className="dropdown-item logout"
                      onClick={() => {
                        setProfileMenuOpen(false);
                        onLogout();
                        addToast('Successfully signed out.', 'info');
                      }}
                    >
                      <X size={14} />
                      <span>Sign Out</span>
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="section-width wishlist-header-section">
        
        {/* Back Link */}
        <a 
          href="#" 
          className="back-to-home-link"
          style={{ marginBottom: '24px' }}
          onClick={(e) => {
            e.preventDefault();
            onNavigate('landing');
          }}
        >
          <ArrowLeft size={14} />
          <span>Back to Marketplace</span>
        </a>

        {/* Title row */}
        <div className="wishlist-title-row">
          <div>
            <h1>My Wishlist</h1>
            <p style={{ color: 'var(--text-secondary)', fontSize: '15px', marginTop: '6px' }}>
              Review, share, and purchase items you have saved to your catalog profile
            </p>
          </div>
          <div className="wishlist-count-badge">
            {totalWishlistCount} {totalWishlistCount === 1 ? 'Product' : 'Products'} Saved
          </div>
        </div>

        {wishlist.length > 0 ? (
          <>
            {/* Interactive Alerts Panel */}
            <div className="wishlist-alerts-panel glass-card" style={{ marginTop: '24px' }}>
              <div className="alerts-panel-info">
                <div className="alerts-panel-icon">
                  <Bell size={20} />
                </div>
                <div>
                  <div className="alerts-panel-title">Smart Catalog Notifications</div>
                  <div className="alerts-panel-desc">We will email you when your wishlisted items drop in price or come back in stock.</div>
                </div>
              </div>
              <div className="alerts-toggles-container">
                <div 
                  className={`toggle-switch-group ${priceAlerts ? 'active' : ''}`}
                  onClick={handleTogglePriceAlerts}
                >
                  <span>Price Drop Alerts</span>
                  <div className="toggle-switch"></div>
                </div>
                <div 
                  className={`toggle-switch-group ${stockAlerts ? 'active' : ''}`}
                  onClick={handleToggleStockAlerts}
                >
                  <span>Stock Availability</span>
                  <div className="toggle-switch"></div>
                </div>
              </div>
            </div>

            {/* Toolbar search & bulk controls */}
            <div className="wishlist-toolbar">
              <div className="wishlist-search-wrapper">
                <Search size={18} className="wishlist-search-icon" />
                <input 
                  type="text" 
                  className="wishlist-search-input" 
                  placeholder="Search inside wishlist..." 
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>

              <div className="wishlist-bulk-actions">
                <button 
                  className="icon-btn" 
                  style={{ gap: '8px', padding: '10px 16px', fontSize: '14px' }}
                  onClick={() => setShareModalOpen(true)}
                  title="Share wishlist"
                >
                  <Share2 size={16} />
                  <span>Share Wishlist</span>
                </button>
                <button 
                  className="icon-btn" 
                  style={{ gap: '8px', padding: '10px 16px', fontSize: '14px', color: '#ef4444' }}
                  onClick={handleClearWishlist}
                  title="Clear wishlist"
                >
                  <Trash2 size={16} />
                  <span>Clear All</span>
                </button>
                <button 
                  className="primary-btn" 
                  style={{ padding: '10px 20px', fontSize: '14px' }}
                  onClick={handleMoveAllToCart}
                >
                  <ShoppingBag size={16} />
                  <span>Move All to Bag</span>
                </button>
              </div>
            </div>

            {/* Filtered wishlisted products grid */}
            {wishlistedProducts.length > 0 ? (
              <div className="wishlist-products-grid">
                {wishlistedProducts.map((product) => {
                  const stockInfo = getStockStatus(product);
                  return (
                    <div className="wishlist-card glass-card" key={product.id}>
                      <div className="wishlist-card-image-panel">
                        <ProductImage src={product.image} alt={product.name} className="wishlist-card-image" />
                        <span className={`stock-status-badge ${stockInfo.status}`}>
                          {stockInfo.text}
                        </span>
                        <button 
                          className="wishlist-delete-btn"
                          onClick={() => handleRemoveFromWishlist(product.id)}
                          title="Remove item"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>

                      <div className="wishlist-card-details">
                        <span className="wishlist-card-vendor">{product.vendor}</span>
                        <h3 className="wishlist-card-name" title={product.name}>{product.name}</h3>
                        
                        <div className="wishlist-card-rating">
                          <Star size={14} fill="#fb3" color="#fb3" />
                          <span>{product.rating}</span>
                          <span style={{ color: 'var(--text-muted)' }}>({product.reviews} reviews)</span>
                        </div>

                        <div className="wishlist-card-price-row">
                          <div className="wishlist-card-price">₹{product.price.toFixed(2)}</div>
                          <button 
                            className="wishlist-add-bag-btn"
                            onClick={() => handleAddToCart(product)}
                            disabled={stockInfo.status === 'out-of-stock'}
                          >
                            <Plus size={14} />
                            <span>Add to Bag</span>
                          </button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="glass-card empty-state" style={{ margin: '40px auto' }}>
                <Search size={40} style={{ color: 'var(--text-muted)' }} />
                <h3>No matching products</h3>
                <p>No products in your wishlist match "{searchQuery}".</p>
                <button className="primary-btn" onClick={() => setSearchQuery('')}>
                  Clear Search
                </button>
              </div>
            )}
          </>
        ) : (
          <div className="wishlist-empty-state glass-card">
            <div className="wishlist-empty-icon-wrapper">
              <Heart size={44} fill="currentColor" />
            </div>
            <h3>Your Wishlist is Empty</h3>
            <p>
              Tap the heart outline icon on premium marketplace items to track them, 
              compare specs, and review price drop analytics.
            </p>
            <button 
              className="primary-btn"
              onClick={() => {
                onNavigate('landing');
                setTimeout(() => {
                  document.getElementById('browse-section')?.scrollIntoView({ behavior: 'smooth' });
                }, 100);
              }}
            >
              <span>Explore Marketplace</span>
            </button>
          </div>
        )}
      </main>

      {/* Sliding Shopping Cart Drawer */}
      <div className={`glass-drawer-overlay ${cartOpen ? 'open' : ''}`} onClick={() => setCartOpen(false)}>
        <div className="glass-drawer" onClick={(e) => e.stopPropagation()}>
          <div className="drawer-header">
            <h3 className="drawer-title">
              <ShoppingBag size={20} />
              <span>Shopping Cart ({totalCartCount})</span>
            </h3>
            <button className="drawer-close" onClick={() => setCartOpen(false)}>
              <X size={20} />
            </button>
          </div>

          <div className="drawer-body">
            {cart.length > 0 ? (
              cart.map((item) => (
                <div className="drawer-item" key={item.product.id}>
                  <ProductImage src={item.product.image} alt={item.product.name} className="drawer-item-img" />
                  <div className="drawer-item-details">
                    <h4 className="drawer-item-name">{item.product.name}</h4>
                    <span className="drawer-item-vendor">Sold by: {item.product.vendor}</span>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span className="drawer-item-price">₹{(item.product.price * item.quantity).toFixed(2)}</span>
                      <div className="drawer-item-actions">
                        <div className="quantity-controls">
                          <button className="qty-btn" onClick={() => handleUpdateQuantity(item.product.id, -1)}>
                            <Minus size={12} />
                          </button>
                          <span className="qty-val">{item.quantity}</span>
                          <button className="qty-btn" onClick={() => handleUpdateQuantity(item.product.id, 1)}>
                            <Plus size={12} />
                          </button>
                        </div>
                        <button className="item-delete-btn" onClick={() => handleRemoveFromCart(item.product.id)} title="Remove item">
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="empty-state">
                <ShoppingBag size={48} style={{ color: 'var(--text-muted)', marginBottom: '12px' }} />
                <h4>Your cart is empty</h4>
                <p>Browse our catalog and add items you want to purchase.</p>
                <button className="primary-btn" onClick={() => { setCartOpen(false); onNavigate('landing'); setTimeout(() => document.getElementById('browse-section')?.scrollIntoView({ behavior: 'smooth' }), 100); }}>
                  Start Shopping
                </button>
              </div>
            )}
          </div>

          {cart.length > 0 && (
            <div className="drawer-footer">
              <div className="cart-summary">
                <span>Subtotal</span>
                <span>₹{totalCartPrice}</span>
              </div>
              
              {!user ? (
                <button 
                  className="primary-btn checkout-btn" 
                  onClick={() => {
                    setCartOpen(false);
                    onNavigate('login');
                  }}
                >
                  <User size={16} />
                  <span>Login to Checkout</span>
                  <X size={14} style={{ display: 'none' }} />
                </button>
              ) : (
                <button 
                  className="primary-btn checkout-btn" 
                  onClick={() => {
                    if (onCheckout) {
                      onCheckout(cart);
                    } else {
                      onSaveCart([]);
                    }
                    setCartOpen(false);
                  }}
                >
                  <span>Proceed to Checkout</span>
                  <Plus size={14} style={{ display: 'none' }} />
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Share Dialog Modal */}
      <div className={`share-modal-overlay ${shareModalOpen ? 'open' : ''}`} onClick={() => setShareModalOpen(false)}>
        <div className="share-dialog-card glass-card" onClick={(e) => e.stopPropagation()}>
          <div className="share-dialog-header">
            <h3>Share Your Wishlist</h3>
            <button className="share-dialog-close" onClick={() => setShareModalOpen(false)}>
              <X size={18} />
            </button>
          </div>
          <p className="share-dialog-desc">
            Copy the private catalog link below to share this wishlist with friends, 
            co-workers, or family members.
          </p>
          <div className="share-link-wrapper">
            <input 
              type="text" 
              className="share-link-input" 
              value={`https://shopstack.platform/wishlist/share/usr_${user?.avatar ? user.avatar.toLowerCase() : 'guest'}`}
              readOnly 
            />
            <button 
              className="primary-btn share-copy-btn"
              onClick={handleCopyShareLink}
            >
              {copied ? <Check size={16} /> : <Copy size={16} />}
              <span>{copied ? 'Copied!' : 'Copy'}</span>
            </button>
          </div>
        </div>
      </div>

      {/* Custom Confirmation Modal for Clearing Wishlist */}
      {clearConfirmOpen && (
        <div 
          className="glass-modal-overlay open"
          style={{ zIndex: 4000 }}
          onClick={() => setClearConfirmOpen(false)}
        >
          <div className="glass-modal" style={{ maxWidth: '400px' }} onClick={(e) => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setClearConfirmOpen(false)}>
              <X size={18} />
            </button>
            <div style={{ padding: '28px', textAlign: 'center' }}>
              <div style={{ 
                width: '60px', 
                height: '60px', 
                borderRadius: '50%', 
                background: 'rgba(239, 68, 68, 0.15)', 
                color: '#ef4444', 
                display: 'inline-flex', 
                alignItems: 'center', 
                justifyContent: 'center', 
                margin: '0 auto 16px auto'
              }}>
                <Trash2 size={28} />
              </div>
              <h3 style={{ fontSize: '20px', fontWeight: '800', color: 'var(--text-primary)', margin: '0 0 10px 0', textAlign: 'center' }}>Clear Wishlist?</h3>
              <p style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: '1.5', margin: '0 0 24px 0', textAlign: 'center' }}>
                Are you sure you want to remove all items from your saved wishlist? This cannot be undone.
              </p>
              <div style={{ display: 'flex', gap: '12px' }}>
                <button 
                  className="secondary-btn" 
                  style={{ flex: 1, padding: '10px 14px', justifyContent: 'center' }} 
                  onClick={() => setClearConfirmOpen(false)}
                >
                  Cancel
                </button>
                <button 
                  className="primary-btn" 
                  style={{ flex: 1, padding: '10px 14px', justifyContent: 'center', background: '#ef4444', color: '#fff' }} 
                  onClick={() => {
                    onSaveWishlist([]);
                    addToast('Wishlist cleared.', 'info');
                    setClearConfirmOpen(false);
                  }}
                >
                  Clear All
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Toast Alert Popups wrapper */}
      <div className="glass-toast-container">
        {toasts.map((t) => (
          <div className={`glass-toast ${t.type}`} key={t.id}>
            {t.type === 'success' ? <Check size={16} /> : <AlertCircle size={16} />}
            <span>{t.message}</span>
          </div>
        ))}
      </div>

    </div>
  );
}
