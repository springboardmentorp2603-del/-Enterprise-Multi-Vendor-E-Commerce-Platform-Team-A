import { useState, useEffect, useMemo, useRef } from 'react';
import { 
  Search, 
  ShoppingBag, 
  Heart, 
  User, 
  Star, 
  ArrowRight, 
  Plus, 
  Minus, 
  Trash2, 
  X, 
  Check, 
  Activity, 
  Layers, 
  Shield, 
  Lock as LockIcon,
  DollarSign, 
  Truck, 
  Sun, 
  Moon, 
  Compass, 
  Gift, 
  BarChart2, 
  Tag, 
  Warehouse
} from 'lucide-react';
import { PRODUCTS_DATA } from '../../data/products';
import ProductImage from '../../components/ProductImage';
import './LandingPage.css';
import { api } from '../../api';

export default function LandingPage({ 
  onNavigate, 
  user, 
  onLogout, 
  appTheme = 'dark', 
  onToggleAppTheme,
  cart,
  onSaveCart,
  wishlist,
  onSaveWishlist,
  products = PRODUCTS_DATA, // Default to PRODUCTS_DATA if none provided
  onUpdateProducts,
  onCheckout,
  notifications = []
}) {
  const theme = appTheme;
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);
  const [authModalOpen, setAuthModalOpen] = useState(false);
  const [activeCategory, setActiveCategory] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [wishlistItemMap, setWishlistItemMap] = useState({});

  // --- Hero Carousel State ---
const CAROUSEL_SLIDES = [
  {
    id: 1,
    title: 'Summer Mega Sale',
    subtitle: 'Up to 60% off on Electronics & Gadgets',
    cta: 'Shop Now',
    bg: 'linear-gradient(135deg, #1e1b4b 0%, #312e81 50%, #4c1d95 100%)',
    accent: '#818cf8',
    image: 'https://images.unsplash.com/photo-1526738549149-8e07eca6c147?w=900&auto=format&fit=crop',
    badge: '🔥 Hot Deals'
  },
  {
    id: 2,
    title: 'New Arrivals',
    subtitle: 'Discover the latest trends from top vendors',
    cta: 'Explore',
    bg: 'linear-gradient(135deg, #064e3b 0%, #065f46 50%, #047857 100%)',
    accent: '#34d399',
    image: 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=900&auto=format&fit=crop',
    badge: '✨ Just In'
  },
  {
    id: 3,
    title: 'Fashion Week',
    subtitle: 'Premium styles curated by verified sellers',
    cta: 'View Collection',
    bg: 'linear-gradient(135deg, #7c2d12 0%, #9a3412 50%, #c2410c 100%)',
    accent: '#fb923c',
    image: 'https://images.unsplash.com/photo-1483985988355-763728e1935b?w=900&auto=format&fit=crop',
    badge: '👗 Fashion'
  },
  {
    id: 4,
    title: 'Home & Lifestyle',
    subtitle: 'Transform your space with handpicked products',
    cta: 'Discover',
    bg: 'linear-gradient(135deg, #0c4a6e 0%, #075985 50%, #0369a1 100%)',
    accent: '#38bdf8',
    image: 'https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=900&auto=format&fit=crop',
    badge: '🏠 Home'
  },
  {
    id: 5,
    title: 'Free Shipping',
    subtitle: 'On all orders above ₹999 — across 500+ vendors',
    cta: 'Start Shopping',
    bg: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)',
    accent: '#e94560',
    image: 'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=900&auto=format&fit=crop',
    badge: '🚚 Free Delivery'
  }
];
const [carouselIdx, setCarouselIdx] = useState(0);
const carouselTimerRef = useRef(null);

useEffect(() => {
  carouselTimerRef.current = setInterval(() => {
    setCarouselIdx(prev => (prev + 1) % CAROUSEL_SLIDES.length);
  }, 4000);
  return () => clearInterval(carouselTimerRef.current);
}, []);

const goCarousel = (idx) => {
  clearInterval(carouselTimerRef.current);
  setCarouselIdx((idx + CAROUSEL_SLIDES.length) % CAROUSEL_SLIDES.length);
  carouselTimerRef.current = setInterval(() => {
    setCarouselIdx(prev => (prev + 1) % CAROUSEL_SLIDES.length);
  }, 4000);
};
// --- End Carousel State ---
  
  // Open/Close Drawers & Modals
  const [cartOpen, setCartOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [activeQuickViewImage, setActiveQuickViewImage] = useState(null);
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  
  const unreadCount = useMemo(() => {
    if (!user) return 0;
    return notifications.filter(n => n.customerEmail === user.email && !n.read).length;
  }, [notifications, user]);

  useEffect(() => {
    if (selectedProduct) {
      setActiveQuickViewImage(selectedProduct.image);
    } else {
      setActiveQuickViewImage(null);
    }
  }, [selectedProduct]);
  
  // Toast Alerts State
  const [toasts, setToasts] = useState([]);
  
  // Toast Helper (declared at the top to prevent access-before-declaration warnings)
  const addToast = (message, type = 'success') => {
    const id = Date.now();
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 3000);
  };

  // Track previous user via ref to avoid state changes causing renders in effect
  const prevUserRef = useRef(user);

  // Monitor user state for welcome toasts
  useEffect(() => {
    if (user && !prevUserRef.current) {
      addToast(`Welcome back, ${user.name}! Logged in as ${user.role === 'CUSTOMER' ? 'Customer' : 'Vendor'}.`, 'success');
    }
    prevUserRef.current = user;
  }, [user]);

  // Vendor Registration State
  const [vendorForm, setVendorForm] = useState({
    name: '',
    email: '',
    shopName: '',
    category: 'electronics'
  });

  // Save changes to local storage
  const saveCart = (newCart) => {
    onSaveCart(newCart);
  };

  const saveWishlist = (newWishlist) => {
    onSaveWishlist(newWishlist);
  };
  useEffect(() => {
  const loadWishlist = async () => {
    if (!user?.id) {
      setWishlistItemMap({});
      return;
    }

    try {
      const response = await api.wishlist.getWishlist(user.id);

      const wishlistItems = Array.isArray(response?.data)
        ? response.data
        : Array.isArray(response)
          ? response
          : [];

      const productIds = wishlistItems
        .map((item) => item.productId)
        .filter(Boolean);

      const itemMap = {};

      wishlistItems.forEach((item) => {
        if (item.productId && item.id) {
          itemMap[item.productId] = item.id;
        }
      });

      setWishlistItemMap(itemMap);

      
    } catch (err) {
      console.error('Failed to load wishlist:', err);
    }
  };

  loadWishlist();
}, [user?.id]);

  const toggleTheme = () => {
    onToggleAppTheme();
    addToast(`Switched to ${theme === 'dark' ? 'Light' : 'Dark'} Mode`, 'info');
  };

  // Stock status helper
const getStockStatus = (product) => {
  const stock = Number(
    product.stockQuantity ??
    product.stock ??
    product.quantity ??
    product.availableQuantity ??
    0
  );

  if (stock <= 0) {
    return {
      status: '',
      text: 'Out of Stock'
    };
  }

  if (stock <= 2) {
    return {
      status: '',
      text: `Only ${stock} Left!`
    };
  }

  return {
    status: '',
    text: 'In Stock'
  };
};

  // Cart operations
 const handleAddToCart = (product) => {
  const MAX_ITEMS = 10;

  const stock = Number(
    product.stockQuantity ??
    product.stock ??
    product.quantity ??
    product.availableQuantity ??
    0
  );

  // Out of stock
  if (stock <= 0) {
    addToast(
      `"${product.name || product.productName}" is out of stock.`,
      'error'
    );
    return;
  }

  const existing = cart.find(
    (item) => item.product.id === product.id
  );

  if (existing) {
    const currentQuantity = Number(existing.quantity) || 0;

    // Maximum 10
    if (currentQuantity >= MAX_ITEMS) {
      addToast(
        'Maximum 10 items per product allowed.',
        'error'
      );
      return;
    }

    // Stock validation
    if (currentQuantity >= stock) {
      addToast(
        `Only ${stock} unit${stock === 1 ? '' : 's'} of "${product.name || product.productName}" available.`,
        'error'
      );
      return;
    }

    const updated = cart.map((item) =>
      item.product.id === product.id
        ? {
            ...item,

            // Update product information as well
            product: product,

            quantity: currentQuantity + 1
          }
        : item
    );

    saveCart(updated);

    addToast(
      `"${product.name || product.productName}" quantity updated.`
    );

    return;
  }

  // New product
  saveCart([
    ...cart,
    {
      product: product,
      quantity: 1
    }
  ]);

  addToast(
    `Added "${product.name || product.productName}" to cart!`
  );
};
  const handleRemoveFromCart = (productId) => {
    const updated = cart.filter((item) => item.product.id !== productId);
    saveCart(updated);
    addToast('Item removed from cart.', 'info');
  };

const handleUpdateQuantity = (productId, delta) => {
  const MAX_ITEMS = 10;

  const cartItem = cart.find(
    (item) => item.product.id === productId
  );

  if (!cartItem) return;

  // Get the latest product information from marketplace products
  const latestProduct = products.find(
    (product) => product.id === productId
  );

  // Use latest product if available, otherwise use cart product
  const product = latestProduct || cartItem.product;

  const stock = Number(
    product.stockQuantity ??
    product.stock ??
    product.quantity ??
    product.availableQuantity ??
    0
  );

  const currentQuantity = Number(cartItem.quantity) || 0;
  const newQty = currentQuantity + delta;

  // Decrease quantity
  if (newQty < 1) {
    return;
  }

  // Maximum 10 per product
  if (newQty > MAX_ITEMS) {
    addToast(
      'Maximum 10 items per product allowed.',
      'error'
    );
    return;
  }

  // Check actual stock
  if (newQty > stock) {
    addToast(
      `Only ${stock} unit${stock === 1 ? '' : 's'} of "${product.name || product.productName}" available.`,
      'error'
    );
    return;
  }

  const updated = cart.map((item) =>
    item.product.id === productId
      ? {
          ...item,

          // Keep the latest product data
          product: product,

          quantity: newQty
        }
      : item
  );

  saveCart(updated);
};
  // Wishlist operations
  const handleToggleWishlist = async (product) => {

  const stockInfo = getStockStatus(product);

  // Don't allow a new wishlist item when out of stock
  if (stockInfo.status === 'out-of-stock' && !wishlist.includes(product.id)) {
    addToast(`"${product.name}" is out of stock and cannot be added to wishlist.`, 'error');
    return;
  }

  if (!user) {
    setAuthModalOpen(true);
    return;
  }

  try {
    const isWishlisted = wishlist.includes(product.id);

    if (isWishlisted) {
      // Get the actual WishlistItem ID
      const wishlistItemId = wishlistItemMap[product.id];

      if (!wishlistItemId) {
        throw new Error('Wishlist item ID not found. Please refresh the page.');
      }

      // Remove from backend
      await api.wishlist.removeFromWishlist(
        user.id,
        wishlistItemId
      );

      // Update frontend state
      const updated = wishlist.filter(
        (id) => id !== product.id
      );

      saveWishlist(updated);

      // Remove from item map
      setWishlistItemMap((prev) => {
        const updatedMap = { ...prev };
        delete updatedMap[product.id];
        return updatedMap;
      });

      addToast('Removed from wishlist.', 'info');

    } else {
      // Add to backend
      const response = await api.wishlist.addToWishlist(
        user.id,
        product.id
      );

      // Update frontend state
      const updated = [
        ...wishlist,
        product.id
      ];

      saveWishlist(updated);

      // Save WishlistItem ID
      const newWishlistItem = response?.data;

      if (newWishlistItem?.id) {
        setWishlistItemMap((prev) => ({
          ...prev,
          [product.id]: newWishlistItem.id,
        }));
      }

      addToast(`Added "${product.name}" to wishlist!`);
    }

  } catch (err) {
    console.error('Wishlist update failed:', err);

    addToast(
      err.message || 'Wishlist update failed',
      'error'
    );
  }
};

  // Bulk: Add all currently filtered products to wishlist
  const handleAddAllToWishlist = () => {
    if (!user) {
      setAuthModalOpen(true);
      return;
    }
    const productsToAdd = filteredProducts.filter((p) => !wishlist.includes(p.id));
    if (productsToAdd.length === 0) {
      addToast('All shown products are already in your wishlist.', 'info');
      return;
    }
    const newWishlist = [...wishlist, ...productsToAdd.map((p) => p.id)];
    saveWishlist(newWishlist);
    addToast(`Added ${productsToAdd.length} products to wishlist!`, 'success');
  };

  // Vendor Form Onboarding On-Submit
  const handleVendorSubmit = (e) => {
    e.preventDefault();
    if (!vendorForm.name || !vendorForm.shopName || !vendorForm.email) {
      addToast('Please fill out all fields.', 'info');
      return;
    }
    
    // Create vendor user session
    const mockVendorUser = {
      name: vendorForm.name,
      email: vendorForm.email,
      role: 'vendor',
      avatar: vendorForm.name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2),
      vendorDetails: {
        shopName: vendorForm.shopName,
        category: vendorForm.category,
        phone: ''
      }
    };

    addToast(`Successfully registered "${vendorForm.shopName}"! Welcome to ShopStack.`, 'success');
    setVendorForm({ name: '', email: '', shopName: '', category: 'electronics' });
    
    
    // Redirect to Vendor Dashboard
    setTimeout(() => {
      window.location.href = '/vendor-dashboard';
    }, 800);
  };

  // Filtering Logic
  // Use fallback to PRODUCTS_DATA if no products provided
  const approvedProducts = useMemo(() => {
    const catalogProducts = products.length > 0 ? products : PRODUCTS_DATA;
    return catalogProducts.filter((p) => !p.approvalStatus || String(p.approvalStatus).toLowerCase() === 'approved');
  }, [products]);

  const filteredProducts = approvedProducts.filter((product) => {
    const matchesCategory = activeCategory === 'all' || String(product.category || '').toLowerCase() === String(activeCategory || '').toLowerCase();
    const matchesSearch = 
      (product.name || product.productName || '').toLowerCase().includes((searchQuery || '').toLowerCase()) ||
      (product.description || '').toLowerCase().includes((searchQuery || '').toLowerCase()) ||
      (product.vendor || '').toLowerCase().includes((searchQuery || '').toLowerCase());
    return matchesCategory && matchesSearch;
  });

  // Cart totals
  const totalCartCount = useMemo(() => {
    return cart.reduce((acc, item) => acc + item.quantity, 0);
  }, [cart]);

  const totalCartPrice = useMemo(() => {
    return cart.reduce((acc, item) => acc + item.product.price * item.quantity, 0).toFixed(2);
  }, [cart]);

  return (
    <div className={`landing-page-container ${theme === 'light' ? 'light-theme' : ''} ${user && user.role === 'customer' ? 'has-sidebar' : ''}`}>
      
      {/* Visual background blurred gradients */}
      <div className="bg-blobs">
        <div className="bg-blob blob-1"></div>
        <div className="bg-blob blob-2"></div>
        <div className="bg-blob blob-3"></div>
      </div>

      {user && user.role === 'customer' && (
        <aside className="customer-left-sidebar glass-card">
          <div className="sidebar-header" style={{ display: 'flex', flexDirection: 'column', gap: '4px', marginBottom: '8px' }}>
            <div className="brand" style={{ display: 'flex', alignItems: 'center', gap: '10px', textDecoration: 'none' }}>
              <div className="brand-icon-wrapper" style={{ background: 'var(--button-primary)', padding: '6px', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <Layers size={18} color="#fff" />
              </div>
              <span style={{ fontSize: '18px', fontWeight: '800', color: 'var(--text-primary)' }}>Shop<span className="text-gradient">Stack</span></span>
            </div>
            <p className="sidebar-subtitle" style={{ fontSize: '12px', color: 'var(--text-muted)', margin: 0 }}>Enterprise Marketplace</p>
          </div>

          <div className="sidebar-user-section glass-card" style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px', borderRadius: '12px', background: 'rgba(255,255,255,0.02)', border: '1px solid var(--glass-border)', margin: '12px 0 24px 0' }}>
            <div className="user-avatar" style={{ width: '40px', height: '40px', borderRadius: '50%', background: 'var(--button-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', color: '#fff', fontSize: '14px' }}>
              {user.avatar || 'SJ'}
            </div>
            <div className="user-info" style={{ display: 'flex', flexDirection: 'column', gap: '2px', overflow: 'hidden' }}>
              <h4 style={{ margin: 0, fontSize: '14px', fontWeight: '600', color: 'var(--text-primary)', whiteSpace: 'nowrap', textOverflow: 'ellipsis', overflow: 'hidden' }}>{user.name}</h4>
              <p style={{ margin: 0, fontSize: '11px', color: 'var(--text-secondary)', whiteSpace: 'nowrap', textOverflow: 'ellipsis', overflow: 'hidden' }}>{user.email}</p>
              <span className="user-role-tag" style={{ alignSelf: 'flex-start', fontSize: '9px', background: 'var(--secondary-glow)', color: '#fff', padding: '1px 6px', borderRadius: '4px', fontWeight: '600', textTransform: 'uppercase', marginTop: '2px' }}>Customer</span>
            </div>
          </div>

          <nav className="sidebar-nav" style={{ display: 'flex', flexDirection: 'column', gap: '8px', flex: 1 }}>
            <button 
              className="sidebar-link active"
              onClick={(e) => { e.preventDefault(); }}
              style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 16px', borderRadius: '12px', border: 'none', background: 'var(--button-primary)', color: '#fff', fontWeight: '600', width: '100%', cursor: 'pointer', textAlign: 'left' }}
            >
              <ShoppingBag size={18} />
              <span>Marketplace Shop</span>
            </button>

            <button 
              className="sidebar-link"
              onClick={() => onNavigate('customer-dashboard', 'overview')}
              style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 16px', borderRadius: '12px', border: 'none', background: 'transparent', color: 'var(--text-secondary)', fontWeight: '500', width: '100%', cursor: 'pointer', textAlign: 'left', transition: 'var(--transition-smooth)' }}
            >
              <BarChart2 size={18} />
              <span>Dashboard Overview</span>
            </button>

            <button 
              className="sidebar-link"
              onClick={() => onNavigate('customer-dashboard', 'orders')}
              style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 16px', borderRadius: '12px', border: 'none', background: 'transparent', color: 'var(--text-secondary)', fontWeight: '500', width: '100%', cursor: 'pointer', textAlign: 'left', transition: 'var(--transition-smooth)' }}
            >
              <Truck size={18} />
              <span>Orders & Tracking</span>
            </button>

            <button 
              className="sidebar-link"
              onClick={() => onNavigate('customer-dashboard', 'wishlist')}
              style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 16px', borderRadius: '12px', border: 'none', background: 'transparent', color: 'var(--text-secondary)', fontWeight: '500', width: '100%', cursor: 'pointer', textAlign: 'left', transition: 'var(--transition-smooth)' }}
            >
              <Heart size={18} />
              <span>My Wishlist</span>
            </button>

            <button 
              className="sidebar-link"
              onClick={() => onNavigate('customer-dashboard', 'profile')}
              style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 16px', borderRadius: '12px', border: 'none', background: 'transparent', color: 'var(--text-secondary)', fontWeight: '500', width: '100%', cursor: 'pointer', textAlign: 'left', transition: 'var(--transition-smooth)' }}
            >
              <User size={18} />
              <span>My Profile Details</span>
            </button>

            <button 
              className="sidebar-link"
              onClick={() => onNavigate('customer-dashboard', 'security')}
              style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 16px', borderRadius: '12px', border: 'none', background: 'transparent', color: 'var(--text-secondary)', fontWeight: '500', width: '100%', cursor: 'pointer', textAlign: 'left', transition: 'var(--transition-smooth)' }}
            >
              <LockIcon size={18} />
              <span>Security Settings</span>
            </button>
          </nav>

          <div className="sidebar-footer-actions" style={{ display: 'flex', flexDirection: 'column', gap: '8px', borderTop: '1px solid var(--glass-border)', paddingTop: '16px', marginTop: 'auto' }}>
            <button className="theme-toggle-btn secondary-btn" onClick={onToggleAppTheme} style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', padding: '8px 12px', fontSize: '13px', borderRadius: '8px', cursor: 'pointer' }}>
              {theme === 'light' ? <Moon size={16} /> : <Sun size={16} />}
              <span>{theme === 'light' ? 'Dark Mode' : 'Light Mode'}</span>
            </button>

            <button className="logout-btn primary-btn" onClick={onLogout} style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', padding: '10px 12px', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold' }}>
              <span>Log Out</span>
            </button>
          </div>
        </aside>
      )}

      <div className="landing-content-wrapper">

      {/* Frosted Navbar */}
      <header className="glass-navbar glass-element">
        <div className="section-width navbar-content">
          <a href="#" className="brand">
            <div className="brand-icon-wrapper" style={{ background: 'var(--button-primary)' }}>
              <Layers size={22} color="#fff" />
            </div>
            <span>Shop<span className="text-gradient">Stack</span></span>
          </a>

          <nav>
            <ul className="nav-links">
              <li><a href="#" className="active">Home</a></li>
              <li><a href="#browse-section">Categories</a></li>
              <li><a href="#browse-section">All Products</a></li>
              <li>
                <a 
                  href="#" 
                  onClick={(e) => { 
                    e.preventDefault(); 
                    if (user) {
                      onNavigate(user.role === 'VENDOR' ? 'vendor-dashboard' : 'customer-dashboard');
                    } else {
                      onNavigate('login');
                    }
                  }}
                >
                  Dashboard
                </a>
              </li>
            </ul>
          </nav>

          <div className="nav-actions">
            {/* Hamburger toggle (mobile only) */}
            <button
              className="mobile-nav-toggle"
              onClick={() => setMobileNavOpen(!mobileNavOpen)}
              title="Toggle navigation menu"
              aria-label="Toggle navigation"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
                {mobileNavOpen ? (
                  <>
                    <line x1="18" y1="6" x2="6" y2="18" />
                    <line x1="6" y1="6" x2="18" y2="18" />
                  </>
                ) : (
                  <>
                    <line x1="3" y1="7" x2="21" y2="7" />
                    <line x1="3" y1="12" x2="21" y2="12" />
                    <line x1="3" y1="17" x2="21" y2="17" />
                  </>
                )}
              </svg>
            </button>

            {/* Dark Mode toggle button */}
            <button className="icon-btn" onClick={toggleTheme} title="Toggle dark/light mode">
              {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
            </button>

            {/* Wishlist button */}
            <button className="icon-btn" onClick={() => onNavigate('wishlist')} title="View wishlist">
              <Heart size={18} />
              {wishlist.length > 0 && <span className="badge">{wishlist.length}</span>}
            </button>

            {/* Cart toggle */}
            <button className="icon-btn" onClick={() => setCartOpen(true)} title="View cart">
              <ShoppingBag size={18} />
              {totalCartCount > 0 && <span className="badge">{totalCartCount}</span>}
            </button>

            {!user ? (
              <button 
                className="primary-btn" 
                onClick={() => onNavigate('login')}
              >
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
                        if (user.role === 'VENDOR') {
                          onNavigate('vendor-dashboard');
                        } else {
                          onNavigate('customer-dashboard');
                        }
                      }}
                    >
                      <Layers size={14} />
                      <span>{user.role === 'CUSTOMER' ? 'My Orders' : 'Vendor Dashboard'}</span>
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

        {/* Mobile nav slide-down menu */}
        <div className={`mobile-nav-menu ${mobileNavOpen ? 'open' : ''}`}>
          <a href="#" onClick={() => setMobileNavOpen(false)}>Home</a>
          <a href="#browse-section" onClick={() => setMobileNavOpen(false)}>Categories</a>
          <a href="#browse-section" onClick={() => setMobileNavOpen(false)}>All Products</a>
          <a 
            href="#" 
            onClick={(e) => { 
              e.preventDefault(); 
              setMobileNavOpen(false);
              if (user) {
                onNavigate(user.role === 'VENDOR' ? 'vendor-dashboard' : 'customer-dashboard');
              } else {
                onNavigate('login');
              }
            }}
          >
            Dashboard
          </a>
        </div>
      </header>


      {/* Hero Section */}
      <section className="section-width hero-section">
        <div className="hero-badge">
          <Gift size={14} />
          <span>ShopStack Multi-Vendor Architecture 1.0</span>
        </div>
        <h1 className="hero-title">
          The Centralized <br />
          <span className="text-gradient">Enterprise Marketplace</span>
        </h1>
        <p className="hero-subtitle">
          Onboard multiple vendors, deploy customized catalogs, process secure global payments, 
          and track smart automated logistics through our unified glassmorphic commerce hub.
        </p>

        {/* Hero Search Box */}
        <div className="search-bar-hero glass-card">
          <Search size={22} style={{ color: 'var(--text-muted)', margin: 'auto 8px auto 16px' }} />
          <input 
            type="text" 
            placeholder="Search products, brands, or specific vendor shops..." 
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          <button className="primary-btn" onClick={() => {
            document.getElementById('browse-section')?.scrollIntoView({ behavior: 'smooth' });
          }}>
            <span>Search</span>
          </button>
        </div>

        {/* Quick Marketplace Stats */}
        <div className="stats-container">
          <div className="stat-item glass-card">
            <div className="stat-number">10k+</div>
            <div className="stat-label">Products Active</div>
          </div>
          <div className="stat-item glass-card">
            <div className="stat-number">500+</div>
            <div className="stat-label">Verified Sellers</div>
          </div>
          <div className="stat-item glass-card">
            <div className="stat-number">99.9%</div>
            <div className="stat-label">Payment Success</div>
          </div>
          <div className="stat-item glass-card">
            <div className="stat-number">&lt; 2s</div>
            <div className="stat-label">API Response Time</div>
          </div>
        </div>
      </section>

      {/* Feature Showcase (15 Modules from specification) */}
      <section className="section-width" style={{ marginBottom: '80px' }}>
        <div className="section-header">
          <span className="section-tag">Key Workflows</span>
          <h2 className="section-title">Architected for Scalability</h2>
        </div>

        <div className="features-grid">
          <div className="feature-card glass-card">
            <div className="feature-icon-wrapper" style={{ background: '#aa3bff' }}>
              <Shield size={20} />
            </div>
            <h3 className="feature-title">Role Access (JWT)</h3>
            <p className="feature-desc">Secure microservice auth defining Customers, Vendors, Admins, and Warehouse staff access rules.</p>
            </div>
  
            <div className="feature-card glass-card">
              <div className="feature-icon-wrapper" style={{ background: '#3b82f6' }}>
                <DollarSign size={20} />
              </div>
              <h3 className="feature-title">Smart Checkouts</h3>
              <p className="feature-desc">Transactional integrity integrations linking Stripe, Razorpay, and PayPal multi-currency gateway settlements.</p>
            </div>
  
            <div className="feature-card glass-card">
              <div className="feature-icon-wrapper" style={{ background: '#ec4899' }}>
                <Warehouse size={20} />
              </div>
              <h3 className="feature-title">Warehouse Allocation</h3>
              <p className="feature-desc">Automated inventory synchronization, localized stock movement routing, and courier pick-and-pack logic.</p>
            </div>
  
            <div className="feature-card glass-card">
              <div className="feature-icon-wrapper" style={{ background: '#10b981' }}>
                <BarChart2 size={20} />
              </div>
              <h3 className="feature-title">Seller Analytics</h3>
              <p className="feature-desc">Real-time charts depicting revenue margins, system commission fees, customer conversion rates, and CSV exporting.</p>
          </div>
        </div>
      </section>

     

      {/* ===== HERO CAROUSEL ===== */}
      <section className="section-width" style={{ marginBottom: '56px' }}>
        <div className="hero-carousel">
          {/* Slides */}
          <div
            className="carousel-track"
            style={{ transform: `translateX(-${carouselIdx * 100}%)` }}
          >
            {CAROUSEL_SLIDES.map((slide) => (
              <div
                key={slide.id}
                className="carousel-slide"
                style={{ background: slide.bg }}
              >
                <img
                  src={slide.image}
                  alt={slide.title}
                  className="carousel-slide-img"
                />
                <div className="carousel-slide-overlay" />
                <div className="carousel-slide-content">
                  <span className="carousel-badge" style={{ background: 'rgba(255,255,255,0.15)', border: `1px solid ${slide.accent}`, color: slide.accent }}>
                    {slide.badge}
                  </span>
                  <h2 className="carousel-title">{slide.title}</h2>
                  <p className="carousel-subtitle">{slide.subtitle}</p>
                  <button
                    className="carousel-cta"
                    style={{ background: slide.accent, color: '#0f172a' }}
                    onClick={() => document.getElementById('browse-section')?.scrollIntoView({ behavior: 'smooth' })}
                  >
                    {slide.cta} <ArrowRight size={16} style={{ display: 'inline', verticalAlign: 'middle' }} />
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Prev / Next Arrows */}
          <button className="carousel-arrow carousel-arrow-prev" onClick={() => goCarousel(carouselIdx - 1)} aria-label="Previous slide">
            &#8249;
          </button>
          <button className="carousel-arrow carousel-arrow-next" onClick={() => goCarousel(carouselIdx + 1)} aria-label="Next slide">
            &#8250;
          </button>

          {/* Dot indicators */}
          <div className="carousel-dots">
            {CAROUSEL_SLIDES.map((_, i) => (
              <button
                key={i}
                className={`carousel-dot ${i === carouselIdx ? 'active' : ''}`}
                onClick={() => goCarousel(i)}
                aria-label={`Go to slide ${i + 1}`}
              />
            ))}
          </div>
        </div>
      </section>
      {/* ===== END CAROUSEL ===== */}

      {/* Browse Catalog Section */}
      <section className="section-width products-showcase" id="browse-section">
      </section>

      {/* Browse Catalog Section */}
      <section className="section-width products-showcase" id="browse-section">
        <div className="section-header">
          <span className="section-tag">Explore ShopStack</span>
          <h2 className="section-title">Featured Multi-Vendor Catalogs</h2>
        </div>

        {/* Category Tabs */}
        <div className="category-filter-nav">
          <button 
            className={`category-tab ${activeCategory === 'all' ? 'active' : ''}`}
            onClick={() => setActiveCategory('all')}
          >
            <Compass size={16} />
            <span>All Categories</span>
          </button>
          <button 
            className={`category-tab ${activeCategory === 'electronics' ? 'active' : ''}`}
            onClick={() => setActiveCategory('electronics')}
          >
            <Layers size={16} />
            <span>Electronics</span>
          </button>
          <button 
            className={`category-tab ${activeCategory === 'fashion' ? 'active' : ''}`}
            onClick={() => setActiveCategory('fashion')}
          >
            <Tag size={16} />
            <span>Fashion</span>
          </button>
          <button 
            className={`category-tab ${activeCategory === 'home' ? 'active' : ''}`}
            onClick={() => setActiveCategory('home')}
          >
            <Warehouse size={16} />
            <span>Home & Living</span>
          </button>
          <button 
            className={`category-tab ${activeCategory === 'beauty' ? 'active' : ''}`}
            onClick={() => setActiveCategory('beauty')}
          >
            <Heart size={16} />
            <span>Beauty & Wellness</span>
          </button>

          <button 
            className="category-tab add-all-wishlist-btn"
            style={{ 
              borderColor: 'var(--accent-border)', 
              background: 'var(--accent-bg)',
              color: 'var(--primary-glow)',
              marginLeft: 'auto',
              display: 'flex',
              alignItems: 'center',
              gap: '8px'
            }}
            onClick={handleAddAllToWishlist}
          >
            <Heart size={16} fill={filteredProducts.every((p) => wishlist.includes(p.id)) && filteredProducts.length > 0 ? 'currentColor' : 'none'} />
            <span>Add All to Wishlist</span>
          </button>
        </div>

        {/* Search Results Count info */}
        {searchQuery && (
          <div style={{ marginBottom: '24px', color: 'var(--text-secondary)', fontSize: '15px' }}>
            Found {filteredProducts.length} items matching "{searchQuery}"
          </div>
        )}

        {/* Product Cards Grid */}
        {filteredProducts.length > 0 ? (
          <div className="categories-showcase-container">
            {(() => {
              const categoryTitles = {
                electronics: 'Electronics & Spatial Tech',
                fashion: 'Apparel & Athleisure',
                home: 'Home Living & Lumens',
                beauty: 'Botanics & Beauty Care'
              };

              const categoriesToRender = activeCategory === 'all' 
                ? ['electronics', 'fashion', 'home', 'beauty'] 
                : [activeCategory];

              return categoriesToRender.map((cat) => {
                const catProducts = approvedProducts.filter((product) => {
                  const matchesCategory = String(product.category || '').toLowerCase() === String(cat || '').toLowerCase();
                  const matchesSearch = 
                    !searchQuery ||
                    (product.name || product.productName || '').toLowerCase().includes((searchQuery || '').toLowerCase()) ||
                    (product.description || '').toLowerCase().includes((searchQuery || '').toLowerCase()) ||
                    (product.vendor || '').toLowerCase().includes((searchQuery || '').toLowerCase());
                  return matchesCategory && matchesSearch;
                });

                if (catProducts.length === 0) return null;

                return (
                  <div className="category-showcase-section" key={cat} style={{ marginBottom: '40px' }}>
                    <div className="category-section-header" style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '20px', borderBottom: '1px solid var(--glass-border)', paddingBottom: '10px' }}>
                      <h3 style={{ fontSize: '20px', fontWeight: '700', margin: 0, color: 'var(--text-primary)' }}>
                        {categoryTitles[cat] || cat}
                      </h3>
                      <span className="category-count-badge" style={{ fontSize: '12px', background: 'var(--accent-bg)', color: 'var(--accent)', padding: '2px 10px', borderRadius: '12px', fontWeight: '600', border: '1px solid var(--accent-border)' }}>
                        {catProducts.length} {catProducts.length === 1 ? 'Item' : 'Items'}
                      </span>
                    </div>

                    <div className="products-grid-scroll-container">
                      <div className="products-grid">
                        {catProducts.map((product) => {
                          const isWishlisted = wishlist.includes(product.id);
                          const stockInfo = getStockStatus(product);
                          return (
                            <div className="product-card glass-card" key={product.id}>
                              <div className="product-image-container">
                                <ProductImage src={product.image} alt={product.name} className="product-image" />
                                <span className="product-badge">₹{product.price}</span>
                                <span
                                  className={`stock-status-badge ${stockInfo.status}`}
                                   style={{
                                          position: 'absolute',
                                          bottom: '10px',
                                          left: '10px',
                                          zIndex: 2,
                                          background: 'none',
                                          color: stockInfo.text === 'Out of Stock' ? '#dc2626' : 'inherit',
                                          padding: 0,
                                          border: 'none',
                                          fontWeight: 600,
                                          fontSize: '13px'
                                        }}
                                      >
                                        {stockInfo.text}
                                      </span>
                                
                                <button 
          className={`product-wishlist-toggle ${isWishlisted ? 'active' : ''}`}
          onClick={() => handleToggleWishlist(product)}
          disabled={stockInfo.status === 'out-of-stock' && !isWishlisted}
          title={
                stockInfo.status === 'out-of-stock' && !isWishlisted
                  ? 'Out of stock'
                  : isWishlisted
                    ? 'Remove from wishlist'
                    : 'Add to wishlist'
              }
            >
                             <Heart size={16} fill={isWishlisted ? "#fff" : "none"} />
                                </button>
                              </div>

                              <div className="product-meta">
                                <span className="product-vendor">{product.vendor}</span>
                                <span className="product-rating">
                                  <Star size={14} fill="#fb3" color="#fb3" />
                                  <span>{product.rating}</span>
                                </span>
                              </div>

                              <h3 className="product-name">{product.name}</h3>
                              <p className="product-desc">{product.description}</p>

                              <div className="product-footer">
                                <button 
                                  className="secondary-btn" 
                                  style={{ padding: '6px 12px', fontSize: '13px' }}
                                  onClick={() => setSelectedProduct(product)}
                                >
                                  Quick View
                                </button>
                                <button 
                                  className="add-to-cart-btn"
                                  onClick={() => handleAddToCart(product)}
                                  disabled={stockInfo.status === 'out-of-stock'}
                                  title= {
                                    stockInfo.status === 'out-of-stock'
                                      ? 'Out of stock'
                                      : 'Add to cart'
                                  }
                                >
                                  <Plus size={16} />
                                </button>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  </div>
                );
              });
            })()}
          </div>
        ) : (
          <div className="glass-card empty-state">
            <Search size={40} style={{ color: 'var(--text-muted)' }} />
            <h3>No products found</h3>
            <p>Try searching for other terms or resetting your category filter.</p>
            <button className="primary-btn" onClick={() => { setSearchQuery(''); setActiveCategory('all'); }}>
              Reset Filters
            </button>
          </div>
        )}
      </section>

      {/* Become a Vendor Call to Action */}
      <section className="section-width" id="become-vendor-section">
        <div className="vendor-cta-banner glass-card">
          <div className="vendor-cta-content text-left">
            <h2>Expand Your Enterprise. <br /><span className="text-gradient">Sell on ShopStack.</span></h2>
            <p>
              Join hundreds of high-volume suppliers. Access custom catalog modules, real-time 
              pricing models, automated inventory dashboards, and rapid settlement workflows.
            </p>
            <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: 'var(--text-secondary)' }}>
                <Check size={16} color="#10b981" />
                <span>Low 2% Commission Fee</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: 'var(--text-secondary)' }}>
                <Check size={16} color="#10b981" />
                <span>Instant Bank Settlements</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: 'var(--text-secondary)' }}>
                <Check size={16} color="#10b981" />
                <span>API ERP Integrations</span>
              </div>
            </div>
          </div>

          <form className="vendor-cta-form glass-element" onSubmit={handleVendorSubmit}>
            <h3 style={{ fontSize: '18px', fontWeight: '700', marginTop: '0', marginBottom: '16px' }}>Request Vendor Onboarding</h3>
            <div className="form-group">
              <label>Contact Name</label>
              <input 
                type="text" 
                placeholder="Jane Doe" 
                value={vendorForm.name} 
                onChange={(e) => setVendorForm({ ...vendorForm, name: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>Work Email</label>
              <input 
                type="email" 
                placeholder="jane@company.com" 
                value={vendorForm.email}
                onChange={(e) => setVendorForm({ ...vendorForm, email: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>Shop / Business Name</label>
              <input 
                type="text" 
                placeholder="TechStore Premium" 
                value={vendorForm.shopName}
                onChange={(e) => setVendorForm({ ...vendorForm, shopName: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>Primary Category</label>
              <select 
                value={vendorForm.category}
                onChange={(e) => setVendorForm({ ...vendorForm, category: e.target.value })}
              >
                <option value="electronics">Electronics & Gadgets</option>
                <option value="fashion">Fashion & Runners</option>
                <option value="home">Home & Furniture</option>
                <option value="beauty">Beauty & Wellness</option>
              </select>
            </div>
            <button className="primary-btn checkout-btn" type="submit" style={{ marginTop: '10px' }}>
              <span>Submit Registration</span>
              <ArrowRight size={16} />
            </button>
          </form>
        </div>
      </section>

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
                <button className="primary-btn" onClick={() => { setCartOpen(false); document.getElementById('browse-section')?.scrollIntoView({ behavior: 'smooth' }); }}>
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
                  <ArrowRight size={16} />
                </button>
              ) : (
                <button 
                  className="primary-btn checkout-btn" 
                  onClick={() => {
                    if (onCheckout) {
                      onCheckout(cart);
                    } else {
                      saveCart([]);
                    }
                    setCartOpen(false);
                  }}
                >
                  <span>Proceed to Checkout</span>
                  <ArrowRight size={16} />
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Guest Auth Warning Modal */}
      <div className={`glass-modal-overlay ${authModalOpen ? 'open' : ''}`} onClick={() => setAuthModalOpen(false)}>
        <div className="glass-modal" style={{ maxWidth: '400px' }} onClick={(e) => e.stopPropagation()}>
          <button className="modal-close" onClick={() => setAuthModalOpen(false)}>
            <X size={18} />
          </button>
          <div style={{ textAlign: 'center', padding: '10px' }}>
            <div style={{ 
              width: '60px', 
              height: '60px', 
              borderRadius: '50%', 
              background: 'var(--accent-bg)', 
              color: 'var(--primary-glow)', 
              display: 'inline-flex', 
              alignItems: 'center', 
              justifyContent: 'center', 
              marginBottom: '16px' 
            }}>
              <Heart size={28} fill="currentColor" />
            </div>
            <h3 style={{ fontSize: '20px', fontWeight: '600', color: 'var(--text-h)', margin: '0 0 10px 0' }}>Sign In Required</h3>
            <p style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: '1.5', margin: '0 0 20px 0' }}>
              Please sign in to save items to your wishlist and manage your favorites.
            </p>
            <div style={{ display: 'flex', gap: '12px' }}>
              <button 
                className="secondary-btn" 
                style={{ flex: 1, padding: '10px 14px' }} 
                onClick={() => setAuthModalOpen(false)}
              >
                Cancel
              </button>
              <button 
                className="primary-btn" 
                style={{ flex: 1, padding: '10px 14px', justifyContent: 'center' }} 
                onClick={() => {
                  setAuthModalOpen(false);
                  onNavigate('login');
                }}
              >
                Sign In
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Quick View Product Modal */}
      <div className={`glass-modal-overlay ${selectedProduct ? 'open' : ''}`} onClick={() => setSelectedProduct(null)}>
        {selectedProduct && (
          <div className="glass-modal" onClick={(e) => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setSelectedProduct(null)}>
              <X size={18} />
            </button>
            <div className="quickview-layout">
              <div className="quickview-image-panel" style={{ display: 'flex', flexDirection: 'column', gap: '12px', height: 'auto' }}>
                <div style={{ height: '280px', borderRadius: '12px', overflow: 'hidden', border: '1px solid var(--glass-border)', background: 'var(--input-bg)' }}>
                  <ProductImage
                    src={activeQuickViewImage || selectedProduct.image} 
                    alt={selectedProduct.name} 
                    style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                  />
                </div>
                {selectedProduct.images && selectedProduct.images.length > 1 && (
                  <div className="quickview-thumbnails" style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '4px' }}>
                    {selectedProduct.images.map((imgUrl, idx) => (
                      <button
                        key={idx}
                        style={{
                          width: '50px',
                          height: '50px',
                          borderRadius: '8px',
                          border: `2px solid ${(activeQuickViewImage || selectedProduct.image) === imgUrl ? 'var(--accent)' : 'var(--glass-border)'}`,
                          padding: 0,
                          overflow: 'hidden',
                          cursor: 'pointer',
                          background: 'none',
                          flexShrink: 0,
                          transition: 'var(--transition-smooth)'
                        }}
                        onClick={() => setActiveQuickViewImage(imgUrl)}
                      >
                        <ProductImage src={imgUrl} alt={`thumbnail-${idx}`} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                      </button>
                    ))}
                  </div>
                )}
              </div>
              <div className="quickview-details">
                <span className="product-vendor" style={{ alignSelf: 'flex-start', marginBottom: '8px' }}>
                  {selectedProduct.vendor}
                </span>
                <h3 className="quickview-title">{selectedProduct.name}</h3>
                
                <div className="quickview-meta">
                  <span className="product-rating">
                    <Star size={16} fill="#fb3" color="#fb3" />
                    <strong style={{ marginLeft: '4px' }}>{selectedProduct.rating}</strong>
                  </span>
                  <span style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
                    ({selectedProduct.reviews} validated customer reviews)
                  </span>
                </div>

                <p className="quickview-desc">{selectedProduct.description}</p>
                
                <div style={{ marginBottom: '20px', fontSize: '13px', color: 'var(--text-secondary)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '6px' }}>
                    <Truck size={14} color="var(--secondary-glow)" />
                    <span>Free shipping from warehouse in 2 business days.</span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Shield size={14} color="var(--primary-glow)" />
                    <span>Includes 1 year merchant vendor warranty.</span>
                  </div>
                </div>

                <div className="quickview-price-action">
                  <div className="quickview-price">₹{selectedProduct.price}</div>
                  <button className="primary-btn" onClick={() => {
                    handleAddToCart(selectedProduct);
                    setSelectedProduct(null);
                  }}>
                    <ShoppingBag size={16} />
                    <span>Add to Bag</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>



      {/* Footer */}
      <footer className="glass-footer">
        <div className="section-width footer-grid">
          <div className="footer-logo-side">
            <h3>Shop<span className="text-gradient">Stack</span></h3>
            <p>
              An enterprise-level multi-vendor marketplace platform built on a scalable React 19 
              frontend and Java Spring Boot microservice cloud backend.
            </p>
            <div className="social-links">
              <span className="icon-btn" style={{ padding: '6px' }}><Activity size={16} /></span>
              <span className="icon-btn" style={{ padding: '6px' }}><Shield size={16} /></span>
              <span className="icon-btn" style={{ padding: '6px' }}><Layers size={16} /></span>
            </div>
          </div>

          <div className="footer-col">
            <h4>For Customers</h4>
            <ul className="footer-links">
              <li><a href="#browse-section">Browse Stores</a></li>
              <li><a href="#" onClick={(e) => { e.preventDefault(); setCartOpen(true); }}>View Cart</a></li>
              <li><a href="#" onClick={(e) => { e.preventDefault(); onNavigate('wishlist'); }}>Your Wishlist</a></li>
              <li><a href="#" onClick={(e) => { e.preventDefault(); onNavigate('login'); }}>Customer Login</a></li>
              <li><a href="#" onClick={(e) => { e.preventDefault(); onNavigate('register'); }}>Customer Signup</a></li>
            </ul>
          </div>

          <div className="footer-col">
            <h4>For Vendors</h4>
            <ul className="footer-links">
              <li><a href="#become-vendor-section">Become a Seller</a></li>
              <li><a href="#" onClick={(e) => { e.preventDefault(); onNavigate('login'); }}>Vendor Login</a></li>
              <li><a href="#" onClick={(e) => { e.preventDefault(); onNavigate('register'); }}>Vendor Registration</a></li>
              <li><a href="#" onClick={(e) => { e.preventDefault(); addToast('Guidelines: Standard delivery within 2 days, flat 2% commission fee.', 'info'); }}>Vendor Guidelines</a></li>
            </ul>
          </div>

          <div className="footer-newsletter">
            <h4>Newsletter</h4>
            <p>Subscribe to receive system status alerts, promotional coupon campaign news, and vendor updates.</p>
            <div className="newsletter-form">
              <input type="email" placeholder="enter your email..." />
              <button 
                className="primary-btn" 
                onClick={() => addToast('Subscribed to ShopStack Newsletter!', 'success')}
                style={{ padding: '10px 14px' }}
              >
                <span>Join</span>
              </button>
            </div>
          </div>
        </div>

        <div className="section-width footer-bottom">
          <span>&copy; {new Date().getFullYear()} ShopStack Platform. All rights reserved.</span>
          <div className="footer-bottom-links">
            <a href="#" onClick={(e) => e.preventDefault()}>Privacy Policy</a>
            <a href="#" onClick={(e) => e.preventDefault()}>Terms of Service</a>
          </div>
        </div>
      </footer>

      </div> {/* End of landing-content-wrapper */}

      {/* Toast Alert Popups wrapper */}
      <div className="glass-toast-container">
        {toasts.map((t) => (
          <div className={`glass-toast ${t.type}`} key={t.id}>
            {t.type === 'success' ? <Check size={16} /> : <Compass size={16} />}
            <span>{t.message}</span>
          </div>
        ))}
      </div>

    </div>
  );
}
