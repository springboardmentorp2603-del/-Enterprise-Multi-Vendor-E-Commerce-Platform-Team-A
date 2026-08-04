/**
 * Central API Client for ShopStack Backend
 * Handles auth tokens, error normalization, and standard HTTP requests.
 */

const BASE_URL = ''; // Proxied via Vite config to avoid CORS & hardcoded URL issues

export const tokenStorage = {
  getToken: () => localStorage.getItem('shopstack_token'),
  setToken: (token) => localStorage.setItem('shopstack_token', token),
  clearToken: () => localStorage.removeItem('shopstack_token'),
};

export const unwrap = (data) => {
  if (Array.isArray(data)) return data;
  if (data && Array.isArray(data.content)) return data.content;
  if (data && Array.isArray(data.data)) return data.data;
  return data;
};

async function apiRequest(endpoint, options = {}) {
  const token = tokenStorage.getToken();
  const isFormData = options.body instanceof FormData;

  const headers = {
    ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
    ...(options.headers || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    ...options,
    headers,
  };

  if (config.body && typeof config.body === 'object' && !isFormData) {
    config.body = JSON.stringify(config.body);
  }

  try {
    const response = await fetch(`${BASE_URL}${endpoint}`, config);
    
    // Check for 204 No Content
    if (response.status === 204) {
      return null;
    }

    let data;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      data = await response.text();
    }

    if (!response.ok) {
      // Normalise backend errors
      const errorMessage = data?.message || data?.error || response.statusText || 'An unexpected error occurred';
      throw new Error(errorMessage);
    }

    return data;
  } catch (error) {
    console.error(`API Error on ${endpoint}:`, error);
    throw error;
  }
}

export const api = {
  // --- AUTH ENDPOINTS ---
  auth: {
    login: (credentials) => apiRequest('/auth/login', {
      method: 'POST',
      body: credentials,
    }),
    register: (userData) => apiRequest('/auth/register', {
      method: 'POST',
      body: userData,
    }),
    me: () => apiRequest('/auth/me', {
      method: 'GET',
    }),
  },

  // --- CUSTOMER PORTAL ---
  customer: {
    register: (customerData) => apiRequest('/api/v1/customers', {
      method: 'POST',
      body: customerData,
    }),
    getProfile: () => apiRequest('/api/v1/customers/me', {
      method: 'GET',
    }),
    getAddresses: (customerId) => apiRequest(`/api/v1/customers/${customerId}/addresses`, {
      method: 'GET',
    }),
    addAddress: (customerId, addressData) => apiRequest(`/api/v1/customers/${customerId}/addresses`, {
      method: 'POST',
      body: addressData,
    }),
  },

  // --- VENDOR PORTAL ---
  vendor: {
    register: (vendorData) => apiRequest('/api/v1/vendors', {
      method: 'POST',
      body: vendorData,
    }),
    getProfile: () => apiRequest('/api/v1/vendors/me', {
      method: 'GET',
    }),
    updateProfile: (vendorId, updateData) => apiRequest(`/api/v1/vendors/${vendorId}`, {
      method: 'PATCH',
      body: updateData,
    }),
    addAddress: (vendorId, addressData) => apiRequest(`/api/v1/vendors/${vendorId}/addresses`, {
      method: 'POST',
      body: addressData,
    }),
    addBankDetails: (vendorId, bankData) => apiRequest(`/api/v1/vendors/${vendorId}/bank-details`, {
      method: 'POST',
      body: bankData,
    }),
    uploadDocument: (vendorId, docData) => apiRequest(`/api/v1/vendors/${vendorId}/documents`, {
      method: 'POST',
      body: docData,
    }),
  },

  // --- PRODUCT & CATEGORY ENDPOINTS ---
  products: {
    browseActive: () => apiRequest('/api/products/browse', {
      method: 'GET',
    }),
    getById: (id) => apiRequest(`/api/products/${id}`, {
      method: 'GET',
    }),
    search: (keyword) => apiRequest(`/api/products/search?keyword=${encodeURIComponent(keyword)}`, {
      method: 'GET',
    }),
    getByCategory: (categoryId) => apiRequest(`/api/products/category/${categoryId}`, {
      method: 'GET',
    }),
    getMyProducts: () => apiRequest('/api/products/my-products', {
      method: 'GET',
    }),
    create: (productData) => apiRequest('/api/products', {
      method: 'POST',
      body: productData,
    }),
    update: (id, productData) => apiRequest(`/api/products/${id}`, {
      method: 'PUT',
      body: productData,
    }),
    delete: (id) => apiRequest(`/api/products/${id}`, {
      method: 'DELETE',
    }),
  },

  categories: {
    listAll: () => apiRequest('/api/categories', {
      method: 'GET',
    }),
  },

  reviews: {
    getByProduct: (productId) => apiRequest(`/api/reviews/${productId}`, {
      method: 'GET',
    }),
    create: (productId, reviewData) => apiRequest(`/api/reviews/${productId}`, {
      method: 'POST',
      body: reviewData,
    }),
    delete: (reviewId) => apiRequest(`/api/reviews/${reviewId}`, {
      method: 'DELETE',
    }),
  },

  // --- ADMIN PORTAL ---
  admin: {
    getStats: () => apiRequest('/admin/dashboard/stats', {
      method: 'GET',
    }),
    listVendors: (status = '') => apiRequest(`/admin/vendors${status ? `?status=${status}` : ''}`, {
      method: 'GET',
    }),
    getVendorById: (id) => apiRequest(`/admin/vendors/${id}`, {
      method: 'GET',
    }),
    approveVendor: (id, remarks) => apiRequest(`/admin/vendors/${id}/approve`, {
      method: 'POST',
      body: { remarks },
    }),
    rejectVendor: (id, remarks) => apiRequest(`/admin/vendors/${id}/reject`, {
      method: 'POST',
      body: { remarks },
    }),
    suspendVendor: (id, remarks) => apiRequest(`/admin/vendors/${id}/suspend`, {
      method: 'POST',
      body: { remarks },
    }),
    getVendorApprovalHistory: (id) => apiRequest(`/admin/vendors/${id}/approval-history`, {
      method: 'GET',
    }),
    listProducts: () => apiRequest('/admin/products', {
      method: 'GET',
    }),
    listPendingProducts: () => apiRequest('/admin/products/pending', {
      method: 'GET',
    }),
    approveProduct: (id, remarks) => apiRequest(`/admin/products/${id}/approve`, {
      method: 'POST',
      body: { remarks },
    }),
    rejectProduct: (id, remarks) => apiRequest(`/admin/products/${id}/reject`, {
      method: 'POST',
      body: { remarks },
    }),
    listCategories: () => apiRequest('/admin/categories', {
      method: 'GET',
    }),
    createCategory: (catData) => apiRequest('/admin/categories', {
      method: 'POST',
      body: catData,
    }),
    updateCategory: (id, catData) => apiRequest(`/admin/categories/${id}`, {
      method: 'PUT',
      body: catData,
    }),
    deleteCategory: (id) => apiRequest(`/admin/categories/${id}`, {
      method: 'DELETE',
    }),
    createStaffAccount: (staffData) => apiRequest('/admin/staff', {
      method: 'POST',
      body: staffData,
    }),
    listCustomers: () => apiRequest('/admin/customers', {
      method: 'GET',
    }),
    // --- NEW: Reports
    getReports: () => apiRequest('/admin/reports', {
      method: 'GET',
    }),
    // --- NEW: System Logs
    getSystemLogs: () => apiRequest('/admin/system-logs', {
      method: 'GET',
    }),
    // --- NEW: Reports ---
    getSalesReport: (from, to) => apiRequest(`/admin/reports/sales?from=${from}&to=${to}`, {
      method: 'GET',
    }),
    getVendorReport: (from, to) => apiRequest(`/admin/reports/vendors?from=${from}&to=${to}`, {
      method: 'GET',
    }),
    getOrderReport: (from, to, status) => {
      const query = `?from=${from}&to=${to}` + (status ? `&status=${status}` : '');
      return apiRequest(`/admin/reports/orders${query}`, {
        method: 'GET',
      });
    },
    getFinancialReport: (from, to) => apiRequest(`/admin/reports/financial?from=${from}&to=${to}`, {
      method: 'GET',
    }),
    exportSalesReport: (from, to, format) => apiRequest(`/admin/reports/sales/export?from=${from}&to=${to}&format=${format}`, {
      method: 'GET',
    }),
    exportVendorReport: (from, to) => apiRequest(`/admin/reports/vendors/export?from=${from}&to=${to}`, {
      method: 'GET',
    }),
    // --- NEW: System Logs ---
    getSystemLogs: () => apiRequest('/admin/system-logs', {
      method: 'GET',
    }),
  },

  // --- MODULE 2: CART, CHECKOUT, & PAYMENT ---
 cart: {
  get: (userId) => apiRequest(`/api/v1/cart?userId=${userId}`, { method: 'GET' }),

  addItem: (itemData) => apiRequest(`/api/v1/cart/items?userId=${itemData.userId}`, {
    method: 'POST',
    body: {
      productId: itemData.productId,
      quantity: itemData.quantity,
    },
  }),

  updateItem: (id, updateData) => apiRequest(`/api/v1/cart/items/${id}?userId=${updateData.userId}`, {
    method: 'PATCH',
    body: {
      quantity: updateData.quantity,
    },
  }),

  removeItem: (id, userId) => apiRequest(`/api/v1/cart/items/${id}?userId=${userId}`, {
    method: 'DELETE',
  }),
},

  wishlist: {
  getWishlist: (userId) => apiRequest(`/api/v1/wishlist?userId=${userId}`, { method: 'GET' }),
  addToWishlist: (userId, productId) => apiRequest('/api/v1/wishlist/add', {
    method: 'POST',
    body: { userId, productId }
  }),
  removeFromWishlist: (userId, itemId) => apiRequest(`/api/v1/wishlist/${itemId}?userId=${userId}`, { 
    method: 'DELETE' 
  }),
  addToCartAndRemove: (userId, itemId) => apiRequest(`/api/v1/wishlist/${itemId}/to-cart?userId=${userId}`, { 
    method: 'POST' 
  }),
  checkInWishlist: (userId, productId) => apiRequest(`/api/v1/wishlist/check/${productId}?userId=${userId}`, { 
    method: 'GET' 
  }),
  getWishlistCount: (userId) => apiRequest(`/api/v1/wishlist/count?userId=${userId}`, { 
    method: 'GET' 
  }),
  clearWishlist: (userId) => apiRequest(`/api/v1/wishlist/clear?userId=${userId}`, { 
    method: 'DELETE' 
  }),
},

  checkout: {
    process: (checkoutData) => apiRequest('/api/v1/checkout', { method: 'POST', body: checkoutData }),
  },
  orders: {
    create: (orderData) => apiRequest('/api/v1/orders', { method: 'POST', body: orderData }),
    list: (userId) => apiRequest(`/api/v1/orders?userId=${userId}`, { method: 'GET' }),
    getAll: (userId) => apiRequest(`/api/v1/orders?userId=${userId}`, { method: 'GET' }),
    getVendorAll: () => apiRequest('/api/v1/orders/vendor', { method: 'GET' }),
    getById: (id) => apiRequest(`/api/v1/orders/${id}`, { method: 'GET' }),
    getTimeline: (id) => apiRequest(`/api/v1/orders/${id}/timeline`, { method: 'GET' }),
    getTracking: (id) => apiRequest(`/api/v1/orders/${id}/tracking`, { method: 'GET' }),
    updateStatus: (id, status) => apiRequest(`/api/v1/orders/${id}/status?status=${status}`, { method: 'PATCH' }),
    cancel: (id) => apiRequest(`/api/v1/orders/${id}/cancel`, { method: 'POST' }),
    returnOrder: (id) => apiRequest(`/api/v1/orders/${id}/return`, { method: 'POST' }),
  },
  get order() { return this.orders; },
  payment: {
    create: (paymentData) => apiRequest('/api/v1/payments/create', { method: 'POST', body: paymentData }),
    verify: (verifyData) => apiRequest('/api/v1/payments/verify', { method: 'POST', body: verifyData }),
  },
  invoices: {
    getByOrder: (orderId) => apiRequest(`/api/v1/invoices/order/${orderId}`, { method: 'GET' }),
  },

  coupons: {
    getAvailable: (cartTotal) => apiRequest(
      `/api/v1/coupons/available${cartTotal ? `?cartTotal=${cartTotal}` : ''}`,
      { method: 'GET' }
    ),
    validate: (payload) => apiRequest('/api/v1/coupons/validate', {
      method: 'POST',
      body: payload,
    }),

    create: (couponData) => apiRequest('/api/v1/coupons', {
      method: 'POST',
      body: couponData,
    }),
    list: () => apiRequest('/api/v1/coupons', {
      method: 'GET',
    }),
    getById: (id) => apiRequest(`/api/v1/coupons/${id}`, {
      method: 'GET',
    }),
    update: (id, couponData) => apiRequest(`/api/v1/coupons/${id}`, {
      method: 'PUT',
      body: couponData,
    }),
    deactivate: (id) => apiRequest(`/api/v1/coupons/${id}/deactivate`, {
      method: 'POST',
    }),
    getAnalytics: (id) => apiRequest(`/api/v1/coupons/${id}/analytics`, {
      method: 'GET',
    }),
  },
};
