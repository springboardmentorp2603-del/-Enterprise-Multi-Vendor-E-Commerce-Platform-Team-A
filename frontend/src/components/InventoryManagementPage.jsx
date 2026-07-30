import React, { useState, useEffect } from 'react';
import { api } from '../api';
import AvailabilityBadge from './AvailabilityBadge';

export default function InventoryManagementPage({ addToast }) {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [stockDrafts, setStockDrafts] = useState({});
  const [discountDrafts, setDiscountDrafts] = useState({});

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const prods = await api.products.getMyProducts();
      setProducts(prods || []);
    } catch (err) {
      setError(err.message || 'Failed to load inventory');
    } finally {
      setLoading(false);
    }
  };

  const buildUpdatePayload = (prod, overrides) => ({
    productName: prod.productName,
    brand: prod.brand || '',
    description: prod.description || '',
    price: prod.price,
    stockQuantity: prod.stockQuantity,
    categoryId: prod.categoryId,
    featured: prod.featured || false,
    imageUrl: prod.imageUrl || '',
    active: prod.active !== undefined ? prod.active : true,
    ...overrides,
  });

  const handleStockChange = (prod, delta) => {
    const current = stockDrafts[prod.id] ?? prod.stockQuantity;
    const next = Math.max(0, current + delta);
    setStockDrafts({ ...stockDrafts, [prod.id]: next });
  };

  const handleStockInput = (prod, value) => {
    const next = Math.max(0, parseInt(value) || 0);
    setStockDrafts({ ...stockDrafts, [prod.id]: next });
  };

  const handleStockSave = async (prod) => {
    const newQty = stockDrafts[prod.id];
    try {
      const payload = buildUpdatePayload(prod, { stockQuantity: newQty });
      await api.products.update(prod.id, payload);
      addToast('Stock updated!', 'success');
      const { [prod.id]: _discard, ...rest } = stockDrafts;
      setStockDrafts(rest);
      loadProducts();
    } catch (err) {
      addToast(err.message || 'Failed to update stock', 'error');
    }
  };

  const handleDiscountInput = (prod, value) => {
    const next = Math.min(100, Math.max(0, parseFloat(value) || 0));
    setDiscountDrafts({ ...discountDrafts, [prod.id]: next });
  };

  const handleDiscountSave = async (prod) => {
    const newDiscount = discountDrafts[prod.id];
    try {
      const payload = buildUpdatePayload(prod, { discountPercent: newDiscount });
      await api.products.update(prod.id, payload);
      addToast('Discount updated!', 'success');
      const { [prod.id]: _discard, ...rest } = discountDrafts;
      setDiscountDrafts(rest);
      loadProducts();
    } catch (err) {
      addToast(err.message || 'Failed to update discount (backend may not support this field yet)', 'error');
    }
  };

  const getFinalPrice = (prod) => {
    const discount = prod.discountPercent ?? 0;
    return (prod.price - (prod.price * discount) / 100).toFixed(2);
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '6rem' }}>
        <div className="spinner"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="glass-card" style={{ margin: '2rem', padding: '2rem', color: 'var(--text-secondary)' }}>
        Couldn't load inventory: {error}
      </div>
    );
  }

  return (
    <div style={{ textAlign: 'left', padding: '2rem', maxWidth: '1100px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h2 style={{ fontFamily: 'var(--font-heading)', fontWeight: 800 }}>Inventory Management</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.25rem' }}>
            Quick controls for stock levels, discounts, and availability. For adding, editing details, or removing products, use Merchant Panel → Manage Products.
          </p>
        </div>
      </div>

      {products.length === 0 ? (
        <div className="glass-card" style={{ padding: '4rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
          No products found. Add products from Merchant Panel → Manage Products first.
        </div>
      ) : (
        <div className="table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>Product</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Discount</th>
                <th>Final Price</th>
                <th>Availability</th>
              </tr>
            </thead>
            <tbody>
              {products.map((prod) => (
                <tr key={prod.id}>
                  <td>
                    <strong>{prod.productName}</strong>
                    {prod.brand && <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{prod.brand}</div>}
                  </td>
                  <td>${prod.price}</td>
                  <td>{prod.stockQuantity}</td>
                  <td>{prod.discountPercent ?? 0}%</td>
                  <td>
                    {prod.discountPercent > 0 ? (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.1rem' }}>
                        <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.4rem' }}>
                          <span style={{ color: '#22c55e', fontWeight: 700, fontSize: '0.85rem' }}>
                            -{prod.discountPercent}%
                          </span>
                          <span style={{ fontSize: '1rem', fontWeight: 700 }}>
                            ${getFinalPrice(prod)}
                          </span>
                        </div>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textDecoration: 'line-through' }}>
                          Actual Price: ${prod.price}
                        </span>
                      </div>
                    ) : (
                      <span>${prod.price}</span>
                    )}
                  </td>
                  <td>
                    <AvailabilityBadge stockQty={prod.stockQuantity} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}