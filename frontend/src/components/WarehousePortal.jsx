import React, { useState, useEffect } from 'react';
import { api } from '../api';
import { Package, Truck, Clipboard, Activity, AlertTriangle, Layers } from 'lucide-react';

export default function WarehousePortal({ user, addToast }) {
  const [warehouses, setWarehouses] = useState([]);
  const [selectedWhId, setSelectedWhId] = useState('');
  const [activeTab, setActiveTab] = useState('fulfillments'); // 'fulfillments', 'inventory', 'movements', 'analytics'
  const [loading, setLoading] = useState(false);

  // Data states
  const [fulfillments, setFulfillments] = useState([]);
  const [selectedFulfillment, setSelectedFulfillment] = useState(null);
  const [fulfillmentOrder, setFulfillmentOrder] = useState(null);
  const [inventoryList, setInventoryList] = useState([]);
  const [movements, setMovements] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [warehouseReturns, setWarehouseReturns] = useState([]);

  // Form modals
  const [showSyncModal, setShowSyncModal] = useState(false);
  const [selectedProductSync, setSelectedProductSync] = useState(null);
  const [syncQty, setSyncQty] = useState('');
  const [syncNote, setSyncNote] = useState('');

  const [showShipModal, setShowShipModal] = useState(false);
  const [shipFulfillmentId, setShipFulfillmentId] = useState('');
  const [carrier, setCarrier] = useState('BlueDart Express');
  const [trackingNo, setTrackingNo] = useState('');

  // Search filter
  const [searchFilter, setSearchFilter] = useState('');

  // Delivery Update Modal States
  const [showDeliveryModal, setShowDeliveryModal] = useState(false);
  const [deliveryFulfillment, setDeliveryFulfillment] = useState(null);
  const [deliveryEvent, setDeliveryEvent] = useState('PICKED_UP');
  const [deliveryLocation, setDeliveryLocation] = useState('Sorting Facility');
  const [deliveryDesc, setDeliveryDesc] = useState('Package picked up by courier service');

  // Load all warehouses on mount
  useEffect(() => {
    loadWarehouses();
  }, []);

  // Load tab content when warehouse or tab selection changes
  useEffect(() => {
    if (selectedWhId) {
      loadTabContent();
    }
  }, [selectedWhId, activeTab]);

  const loadWarehouses = async () => {
    setLoading(true);
    try {
      const response = await api.warehouse.list();
      const list = response?.data || response || [];
      setWarehouses(list);
      if (list.length > 0) {
        setSelectedWhId(list[0].id);
      }
    } catch (err) {
      addToast(err.message || 'Failed to load warehouses list', 'error');
    } finally {
      setLoading(false);
    }
  };

  const loadTabContent = async () => {
    setLoading(true);
    try {
      if (activeTab === 'fulfillments') {
        const response = await api.warehouseFulfillments.getByWarehouse(selectedWhId);
        setFulfillments(response?.data || response || []);
      } else if (activeTab === 'inventory') {
        const response = await api.warehouse.getById(selectedWhId);
        // We will fetch inventories from /api/inventory/vendor/{vendorId} or just filter by warehouseId from all low-stock
        // To be accurate, we can load the warehouse response, which has occupancy, and we can query lists.
        try {
          const response = await api.warehouse.getInventory(selectedWhId);
          const list = response?.data || response || [];
          const mappedList = list.map(inv => ({
            id: inv.productId,
            productName: inv.productName || 'Unnamed Product',
            brand: inv.brand || 'General',
            categoryName: inv.categoryName || 'General',
            availableStock: inv.availableStock || 0,
            reservedStock: inv.reservedStock || 0,
            reorderThreshold: inv.reorderThreshold || 10,
            lowStock: inv.lowStock || false
          }));
          setInventoryList(mappedList);
        } catch (err) {
          setInventoryList([]);
        }
      } else if (activeTab === 'movements') {
        const response = await api.warehouse.getMovements(selectedWhId);
        setMovements(response?.data || response || []);
      } else if (activeTab === 'analytics') {
        const response = await api.warehouse.getAnalytics(selectedWhId);
        setAnalytics(response?.data || response || null);
      } else if (activeTab === 'returns') {
        const response = await api.orders.adminGetReturns();
        setWarehouseReturns(response?.data || response || []);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleWarehouseReturnStatus = async (returnId, nextStatus, successMsg) => {
    try {
      await api.orders.adminUpdateReturnStatus(returnId, nextStatus);
      addToast(successMsg, 'success');
      loadTabContent();
    } catch (err) {
      addToast(err.message || 'Failed to update return status', 'error');
    }
  };

  const handleClaimFulfillment = async (id) => {
    try {
      await api.warehouseFulfillments.assignStaff(id);
      addToast('Fulfillment task claimed successfully. Status changed to PICKING.', 'success');
      loadTabContent();
    } catch (err) {
      addToast(err.message || 'Failed to claim task', 'error');
    }
  };

  const handleUpdateStatus = async (id, status) => {
    try {
      await api.warehouseFulfillments.updateStatus(id, status);
      addToast(`Fulfillment status updated to ${status}!`, 'success');
      loadTabContent();
    } catch (err) {
      addToast(err.message || 'Failed to update status', 'error');
    }
  };

  const openShipModal = (id) => {
    setShipFulfillmentId(id);
    setTrackingNo('TRK' + Math.floor(10000000 + Math.random() * 90000000));
    setShowShipModal(true);
  };

  const handlePrepareShipmentSubmit = async (e) => {
    e.preventDefault();
    if (!carrier || !trackingNo) {
      addToast('Carrier and tracking number are required', 'error');
      return;
    }
    try {
      await api.warehouseFulfillments.prepareShipment(shipFulfillmentId, carrier, trackingNo);
      addToast('Shipment prepared successfully! Order is now marked as SHIPPED.', 'success');
      setShowShipModal(false);
      loadTabContent();
    } catch (err) {
      addToast(err.message || 'Shipment preparation failed', 'error');
    }
  };

  const openSyncModal = (item) => {
    setSelectedProductSync(item);
    setSyncQty(item.availableStock !== undefined ? item.availableStock : '');
    setSyncNote('Physical stock count audit');
    setShowSyncModal(true);
  };

  const handleSyncSubmit = async (e) => {
    e.preventDefault();
    if (syncQty === '') return;
    try {
      // Call syncStock
      await api.warehouse.syncStock(selectedWhId, selectedProductSync.id, {
        newQuantity: parseInt(syncQty),
        note: syncNote
      });
      addToast('Stock synchronized successfully and logged in movement history.', 'success');
      setShowSyncModal(false);
      loadTabContent();
    } catch (err) {
      addToast(err.message || 'Sync failed', 'error');
    }
  };

  const openDeliveryUpdateModal = (ful) => {
    setDeliveryFulfillment(ful);
    setDeliveryEvent('PICKED_UP');
    setDeliveryLocation('Seattle Storage Facility');
    setDeliveryDesc('Package picked up by courier service');
    setShowDeliveryModal(true);
  };

  const handleDeliveryEventChange = (val) => {
    setDeliveryEvent(val);
    if (val === 'PICKED_UP') {
      setDeliveryDesc('Package picked up by courier service');
    } else if (val === 'IN_TRANSIT') {
      setDeliveryDesc('In transit to next destination hub');
    } else if (val === 'ARRIVAL_SCAN') {
      setDeliveryDesc('Arrival scan at local delivery hub');
    } else if (val === 'DEPARTURE_SCAN') {
      setDeliveryDesc('Departed local distribution facility');
    } else if (val === 'OUT_FOR_DELIVERY') {
      setDeliveryDesc('Out for delivery with courier assistant');
    } else if (val === 'DELIVERED') {
      setDeliveryDesc('Delivered successfully to recipient residence');
    } else if (val === 'EXCEPTION') {
      setDeliveryDesc('Exception: delay due to weather conditions or logistics transit backlog');
    }
  };

  const handleDeliverySubmit = async (e) => {
    e.preventDefault();
    try {
      await api.warehouseFulfillments.updateDeliveryStatus(
        deliveryFulfillment.id,
        deliveryEvent,
        deliveryLocation,
        deliveryDesc
      );
      addToast(`Real-time delivery event "${deliveryEvent}" updated successfully!`, 'success');
      setShowDeliveryModal(false);
      loadTabContent();
    } catch (err) {
      addToast(err.message || 'Failed to update delivery status', 'error');
    }
  };

  const viewOrderDetails = async (fulfillment) => {
    setSelectedFulfillment(fulfillment);
    setFulfillmentOrder(null);
    try {
      const order = await api.orders.getById(fulfillment.orderId);
      setFulfillmentOrder(order);
    } catch (err) {
      addToast('Failed to load order items', 'error');
    }
  };

  const renderStatusBadge = (status) => {
    switch (status) {
      case 'ALLOCATED': return <span className="badge badge-pending">Allocated</span>;
      case 'PICKING': return <span className="badge badge-warning" style={{ background: 'rgba(245, 158, 11, 0.15)', color: '#f59e0b' }}>Picking</span>;
      case 'PACKED': return <span className="badge badge-info" style={{ background: 'rgba(59, 130, 246, 0.15)', color: '#3b82f6' }}>Packed</span>;
      case 'READY_FOR_SHIPMENT': return <span className="badge badge-active" style={{ background: 'rgba(16, 185, 129, 0.15)', color: '#10b981' }}>Ready</span>;
      case 'SHIPPED': return <span className="badge badge-active">Shipped</span>;
      case 'DELIVERED': return <span className="badge badge-active" style={{ background: 'rgba(16, 185, 129, 0.15)', color: '#10b981' }}>Delivered</span>;
      case 'CANCELLED': return <span className="badge badge-rejected">Cancelled</span>;
      default: return <span className="badge badge-pending">{status}</span>;
    }
  };

  const filteredInventory = inventoryList.filter(item => 
    item.productName?.toLowerCase().includes(searchFilter.toLowerCase()) || 
    item.brand?.toLowerCase().includes(searchFilter.toLowerCase())
  );

  return (
    <div className="dashboard-container">
      {/* Sidebar navigation */}
      <div className="sidebar">
        <div className="sidebar-title" style={{ paddingBottom: '0.5rem' }}>ShopStack Storage</div>
        
        {/* Warehouse Selector */}
        <div style={{ padding: '0.5rem 1rem 1.5rem 1rem' }}>
          <label className="form-label" style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--text-muted)' }}>Working Facility</label>
          <select 
            className="form-input" 
            style={{ width: '100%', padding: '0.5rem', background: 'var(--bg-card)', border: '1px solid var(--border-color)', color: 'var(--text-primary)', borderRadius: '6px' }}
            value={selectedWhId}
            onChange={(e) => setSelectedWhId(e.target.value)}
          >
            {warehouses.map(wh => (
              <option key={wh.id} value={wh.id}>{wh.code} - {wh.name}</option>
            ))}
          </select>
        </div>

        <button className={`sidebar-link ${activeTab === 'fulfillments' ? 'active' : ''}`} onClick={() => setActiveTab('fulfillments')}>
          📋 Pick & Pack Fulfillments
        </button>
        <button className={`sidebar-link ${activeTab === 'inventory' ? 'active' : ''}`} onClick={() => setActiveTab('inventory')}>
          📦 Inventory Sync
        </button>
        <button className={`sidebar-link ${activeTab === 'movements' ? 'active' : ''}`} onClick={() => setActiveTab('movements')}>
          🔄 Stock Movements
        </button>
        <button className={`sidebar-link ${activeTab === 'analytics' ? 'active' : ''}`} onClick={() => setActiveTab('analytics')}>
          📊 Facility Analytics
        </button>
        <button className={`sidebar-link ${activeTab === 'returns' ? 'active' : ''}`} onClick={() => setActiveTab('returns')}>
          🔄 Return Inspections
        </button>
      </div>

      {/* Main content workspace */}
      <div className="main-content" style={{ textAlign: 'left' }}>
        {loading && !selectedFulfillment && !showSyncModal && !showShipModal && (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '2rem' }}>
            <div className="spinner" style={{ width: '30px', height: '30px' }}></div>
          </div>
        )}

        {/* Tab 1: Fulfillments (Picks & Packs) */}
        {activeTab === 'fulfillments' && (
          <div>
            <h3 style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, marginBottom: '1.5rem' }}>Pick & Pack Queue</h3>
            
            <div className="glass-card" style={{ padding: '1rem 1.25rem', marginBottom: '1rem', border: '1px solid rgba(59, 130, 246, 0.2)' }}>
              <strong style={{ display: 'block', marginBottom: '0.25rem' }}>Warehouse workflow</strong>
              <div style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                Claim a task → pick and pack items → mark ready for shipment → prepare dispatch → update delivery status.
              </div>
            </div>

            {fulfillments.length === 0 ? (
              <div className="glass-card" style={{ padding: '4rem', color: 'var(--text-secondary)', textAlign: 'center' }}>
                No active orders allocated to this warehouse.
              </div>
            ) : (
              <div className="table-container">
                <table className="custom-table">
                  <thead>
                    <tr>
                      <th>Order Reference</th>
                      <th>Allocated Date</th>
                      <th>Status</th>
                      <th>Assignee</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {fulfillments.map(ful => (
                      <tr key={ful.id}>
                        <td>
                          <strong>Order #{ful.orderId.substring(0, 8)}</strong>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{ful.orderId}</div>
                        </td>
                        <td>{new Date(ful.createdAt).toLocaleDateString()}</td>
                        <td>{renderStatusBadge(ful.status)}</td>
                        <td>{ful.assignedStaffId ? 'Assigned' : 'Unassigned'}</td>
                        <td style={{ display: 'flex', gap: '0.5rem' }}>
                          <button className="btn btn-secondary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem' }} onClick={() => viewOrderDetails(ful)}>
                            View Items
                          </button>
                          {ful.status === 'ALLOCATED' && (
                            <button className="btn btn-primary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem' }} onClick={() => handleClaimFulfillment(ful.id)}>
                              Claim Task
                            </button>
                          )}
                          {ful.status === 'PICKING' && (
                            <button className="btn btn-primary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem', background: '#3b82f6' }} onClick={() => handleUpdateStatus(ful.id, 'PACKED')}>
                              Pack Items
                            </button>
                          )}
                          {ful.status === 'PACKED' && (
                            <button className="btn btn-primary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem', background: '#10b981' }} onClick={() => handleUpdateStatus(ful.id, 'READY_FOR_SHIPMENT')}>
                              Ready for Ship
                            </button>
                          )}
                          {ful.status === 'READY_FOR_SHIPMENT' && (
                            <button className="btn btn-primary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem', background: 'var(--accent)' }} onClick={() => openShipModal(ful.id)}>
                              Ship Order
                            </button>
                          )}
                          {ful.status === 'SHIPPED' && (
                            <button className="btn btn-primary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem', background: '#8b5cf6' }} onClick={() => openDeliveryUpdateModal(ful)}>
                              Update Delivery
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* Tab 2: Inventory Sync */}
        {activeTab === 'inventory' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h3 style={{ fontFamily: 'var(--font-heading)', fontWeight: 700 }}>Warehouse Stored Catalog</h3>
              <input 
                type="text" 
                className="form-input" 
                placeholder="Search products..." 
                style={{ width: '240px', padding: '0.4rem 0.8rem' }} 
                value={searchFilter} 
                onChange={(e) => setSearchFilter(e.target.value)} 
              />
            </div>

            {filteredInventory.length === 0 ? (
              <div className="glass-card" style={{ padding: '4rem', color: 'var(--text-secondary)', textAlign: 'center' }}>
                No products found matching your search.
              </div>
            ) : (
              <div className="table-container">
                <table className="custom-table">
                  <thead>
                    <tr>
                      <th>Product Name</th>
                      <th>Category</th>
                      <th>Available Stock</th>
                      <th>Reserved Stock</th>
                      <th>Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredInventory.map(item => (
                      <tr key={item.id}>
                        <td>
                          <strong>{item.productName}</strong>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{item.brand}</div>
                        </td>
                        <td>{item.categoryName}</td>
                        <td style={{ fontWeight: 600 }}>{item.availableStock}</td>
                        <td style={{ color: 'var(--text-secondary)' }}>{item.reservedStock}</td>
                        <td>
                          {item.lowStock ? (
                            <span className="badge badge-rejected" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}>
                              <AlertTriangle size={12} /> Low Stock
                            </span>
                          ) : (
                            <span className="badge badge-active">In Stock</span>
                          )}
                        </td>
                        <td>
                          <button className="btn btn-secondary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem' }} onClick={() => openSyncModal(item)}>
                            Sync/Audit Stock
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* Tab 3: Movements Log */}
        {activeTab === 'movements' && (
          <div>
            <h3 style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, marginBottom: '1.5rem' }}>Warehouse Stock Audit Movements Log</h3>
            
            {movements.length === 0 ? (
              <div className="glass-card" style={{ padding: '4rem', color: 'var(--text-secondary)', textAlign: 'center' }}>
                No stock movement events logged for this warehouse.
              </div>
            ) : (
              <div className="table-container">
                <table className="custom-table">
                  <thead>
                    <tr>
                      <th>Timestamp</th>
                      <th>Event</th>
                      <th>Quantity Change</th>
                      <th>Previous Stock</th>
                      <th>New Stock</th>
                      <th>Note</th>
                    </tr>
                  </thead>
                  <tbody>
                    {movements.map(m => (
                      <tr key={m.id}>
                        <td>{new Date(m.createdAt).toLocaleString()}</td>
                        <td>
                          <span style={{ fontWeight: 600, fontSize: '0.85rem', textTransform: 'uppercase' }}>{m.movementType}</span>
                        </td>
                        <td style={{ color: m.changeQty > 0 ? '#10b981' : '#ef4444', fontWeight: 700 }}>
                          {m.changeQty > 0 ? `+${m.changeQty}` : m.changeQty}
                        </td>
                        <td>{m.previousStock}</td>
                        <td style={{ fontWeight: 600 }}>{m.newStock}</td>
                        <td style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{m.note || 'No notes'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* Tab 4: Analytics */}
        {activeTab === 'analytics' && analytics && (
          <div>
            <h3 style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, marginBottom: '2rem' }}>Facility Logistics & Occupancy Metrics</h3>
            
            <div className="stats-grid">
              <div className="glass-card stat-card">
                <Layers className="stat-icon" size={24} style={{ color: 'var(--secondary)' }} />
                <span className="stat-num">{analytics.currentOccupancy} / {analytics.totalCapacity}</span>
                <span style={{ color: 'var(--text-secondary)' }}>Total Storage Occupancy</span>
              </div>
              <div className="glass-card stat-card">
                <Activity className="stat-icon" size={24} style={{ color: '#10b981' }} />
                <span className="stat-num">{analytics.occupancyRate.toFixed(1)}%</span>
                <span style={{ color: 'var(--text-secondary)' }}>Capacity Utilization Rate</span>
              </div>
              <div className="glass-card stat-card">
                <Clipboard className="stat-icon" size={24} style={{ color: '#3b82f6' }} />
                <span className="stat-num">{analytics.activeFulfillmentsCount}</span>
                <span style={{ color: 'var(--text-secondary)' }}>Active Fulfillments Pending</span>
              </div>
              <div className="glass-card stat-card">
                <AlertTriangle className="stat-icon" size={24} style={{ color: '#ef4444' }} />
                <span className="stat-num">{analytics.lowStockItemsCount}</span>
                <span style={{ color: 'var(--text-secondary)' }}>Low Stock Skus Warning</span>
              </div>
            </div>

            <div style={{ marginTop: '2rem' }} className="glass-card">
              <h4 style={{ fontFamily: 'var(--font-heading)', fontWeight: 600, marginBottom: '1.25rem' }}>Fulfillment Queue Breakdown</h4>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1.5rem' }}>
                {Object.entries(analytics.fulfillmentsByStatus || {}).map(([status, count]) => (
                  <div key={status} style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', background: 'var(--bg-main)', padding: '0.75rem 1.25rem', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                    {renderStatusBadge(status)}
                    <span style={{ fontSize: '1.25rem', fontWeight: 700 }}>{count}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
        {activeTab === 'returns' && (
          <div>
            <h3 style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, marginBottom: '1.5rem' }}>Warehouse Return Inspections Queue</h3>
            <div className="glass-card" style={{ padding: '1rem 1.25rem', marginBottom: '1rem', border: '1px solid rgba(245, 158, 11, 0.2)' }}>
              <strong style={{ display: 'block', marginBottom: '0.25rem' }}>Fulfillment Return Inspection Workflow</strong>
              <div style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                Verify physical package contents → inspect for damage or eligibility → mark status as `RETURN_RECEIVED` to commit stock restoration back to inventory.
              </div>
            </div>

            {warehouseReturns.length === 0 ? (
              <div className="glass-card" style={{ padding: '4rem', color: 'var(--text-secondary)', textAlign: 'center' }}>
                No active return requests found in pipeline.
              </div>
            ) : (
              <div className="table-container">
                <table className="custom-table">
                  <thead>
                    <tr>
                      <th>Return Request ID</th>
                      <th>Reason</th>
                      <th>Refund Type</th>
                      <th>Notes</th>
                      <th>Status</th>
                      <th>Inspection Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {warehouseReturns.map(ret => (
                      <tr key={ret.id}>
                        <td><strong>{ret.id.substring(0, 8)}...</strong></td>
                        <td>{ret.reason}</td>
                        <td>{ret.refundType}</td>
                        <td>{ret.notes || 'No remarks'}</td>
                        <td>
                          <span className="badge" style={{
                            background: ret.status === 'REFUND_COMPLETED' ? 'rgba(16, 185, 129, 0.15)' : 'rgba(245, 158, 11, 0.15)',
                            color: ret.status === 'REFUND_COMPLETED' ? '#10b981' : '#f59e0b'
                          }}>
                            {ret.status}
                          </span>
                        </td>
                        <td>
                          <div style={{ display: 'flex', gap: '0.5rem' }}>
                            {ret.status === 'RETURN_PICKED' || ret.status === 'RETURN_APPROVED' ? (
                              <button
                                className="btn btn-primary"
                                style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', background: '#10b981', borderColor: '#10b981' }}
                                onClick={() => handleWarehouseReturnStatus(ret.id, 'RETURN_RECEIVED', 'Return received at warehouse & restocked.')}
                              >
                                Receive &amp; Restock
                              </button>
                            ) : (
                              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                                {ret.status === 'RETURN_RECEIVED' || ret.status === 'REFUND_INITIATED' || ret.status === 'REFUND_COMPLETED'
                                  ? 'Restocked'
                                  : 'Awaiting Pickup'}
                              </span>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>

      {/* MODAL 1: VIEW ITEMS / ORDER DETAILS */}
      {selectedFulfillment && (
        <div className="modal-overlay" onClick={() => setSelectedFulfillment(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '640px' }}>
            <button className="modal-close" onClick={() => setSelectedFulfillment(null)}>×</button>
            <h3 style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, marginBottom: '1rem' }}>Fulfillment Details</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
              Order Reference ID: {selectedFulfillment.orderId}
            </p>

            {fulfillmentOrder ? (
              <div>
                <h4 style={{ fontFamily: 'var(--font-heading)', fontWeight: 600, marginBottom: '0.75rem' }}>Items to Pick</h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '2rem' }}>
                  {fulfillmentOrder.items.map((item) => (
                    <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'var(--bg-main)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                      <div>
                        <strong>{item.productName}</strong>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>SKU Copy Ref: {item.productId}</div>
                      </div>
                      <div style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--secondary)' }}>
                        Qty: {item.quantity}
                      </div>
                    </div>
                  ))}
                </div>

                <div style={{ display: 'flex', gap: '1rem' }}>
                  {selectedFulfillment.status === 'ALLOCATED' && (
                    <button className="btn btn-primary" style={{ flex: 1 }} onClick={() => { handleClaimFulfillment(selectedFulfillment.id); setSelectedFulfillment(null); }}>
                      Claim Task & Start Picking
                    </button>
                  )}
                  {selectedFulfillment.status === 'PICKING' && (
                    <button className="btn btn-primary" style={{ flex: 1, background: '#3b82f6' }} onClick={() => { handleUpdateStatus(selectedFulfillment.id, 'PACKED'); setSelectedFulfillment(null); }}>
                      Confirm All Items Packed
                    </button>
                  )}
                  {selectedFulfillment.status === 'PACKED' && (
                    <button className="btn btn-primary" style={{ flex: 1, background: '#10b981' }} onClick={() => { handleUpdateStatus(selectedFulfillment.id, 'READY_FOR_SHIPMENT'); setSelectedFulfillment(null); }}>
                      Mark Ready for Dispatch
                    </button>
                  )}
                  {selectedFulfillment.status === 'READY_FOR_SHIPMENT' && (
                    <button className="btn btn-primary" style={{ flex: 1, background: 'var(--accent)' }} onClick={() => { openShipModal(selectedFulfillment.id); setSelectedFulfillment(null); }}>
                      Enter Dispatch Logistics
                    </button>
                  )}
                </div>
              </div>
            ) : (
              <div style={{ display: 'flex', justifyContent: 'center', padding: '2rem' }}>
                <div className="spinner" style={{ width: '30px', height: '30px' }}></div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* MODAL 2: SHIP/PREPARE SHIPMENT */}
      {showShipModal && (
        <div className="modal-overlay" onClick={() => setShowShipModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '440px' }}>
            <button className="modal-close" onClick={() => setShowShipModal(false)}>×</button>
            <h3 style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, marginBottom: '1.5rem' }}>Prepare Courier Dispatch</h3>
            <form onSubmit={handlePrepareShipmentSubmit}>
              <div className="form-group">
                <label className="form-label">Carrier Company *</label>
                <select className="form-input" value={carrier} onChange={(e) => setCarrier(e.target.value)} required>
                  <option value="BlueDart Express">BlueDart Express</option>
                  <option value="DHL Logistics">DHL Logistics</option>
                  <option value="Delhivery Courier">Delhivery Courier</option>
                  <option value="FedEx India">FedEx India</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Waybill Tracking Number *</label>
                <input type="text" className="form-input" value={trackingNo} onChange={(e) => setTrackingNo(e.target.value)} required />
              </div>
              <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }}>
                Dispatched / Print Waybill Label
              </button>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 3: STOCK SYNC / AUDIT */}
      {showSyncModal && selectedProductSync && (
        <div className="modal-overlay" onClick={() => setShowSyncModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '440px' }}>
            <button className="modal-close" onClick={() => setShowSyncModal(false)}>×</button>
            <h3 style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, marginBottom: '1rem' }}>Audit / Adjust Stock</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
              Product: <strong>{selectedProductSync.productName}</strong>
            </p>
            <form onSubmit={handleSyncSubmit}>
              <div className="form-group">
                <label className="form-label">Actual Count on Shelf *</label>
                <input type="number" className="form-input" min="0" placeholder="e.g. 150" value={syncQty} onChange={(e) => setSyncQty(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Audit Log Remarks/Reason</label>
                <input type="text" className="form-input" placeholder="e.g. Monthly stock take count correction" value={syncNote} onChange={(e) => setSyncNote(e.target.value)} />
              </div>
              <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }}>
                Update Count & Log Event
              </button>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 4: REAL TIME DELIVERY STATUS UPDATE */}
      {showDeliveryModal && deliveryFulfillment && (
        <div className="modal-overlay" onClick={() => setShowDeliveryModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '460px' }}>
            <button className="modal-close" onClick={() => setShowDeliveryModal(false)}>×</button>
            <h3 style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, marginBottom: '1rem' }}>Update Courier Delivery Status</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
              Order ID: <strong>#{deliveryFulfillment.orderId.substring(0, 8)}</strong> ({deliveryFulfillment.carrier || 'No carrier'} - {deliveryFulfillment.trackingNumber || 'No track ID'})
            </p>
            <form onSubmit={handleDeliverySubmit}>
              <div className="form-group">
                <label className="form-label">Delivery Event Type *</label>
                <select className="form-input" value={deliveryEvent} onChange={(e) => handleDeliveryEventChange(e.target.value)} required>
                  <option value="PICKED_UP">Picked Up</option>
                  <option value="IN_TRANSIT">In Transit</option>
                  <option value="ARRIVAL_SCAN">Arrival Scan</option>
                  <option value="DEPARTURE_SCAN">Departure Scan</option>
                  <option value="OUT_FOR_DELIVERY">Out For Delivery</option>
                  <option value="DELIVERED">Delivered</option>
                  <option value="EXCEPTION">Logistics Exception</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Current Transit Location *</label>
                <input type="text" className="form-input" value={deliveryLocation} onChange={(e) => setDeliveryLocation(e.target.value)} placeholder="e.g. Chicago Hub Facility" required />
              </div>
              <div className="form-group">
                <label className="form-label">Tracking Description Remarks *</label>
                <textarea className="form-input" value={deliveryDesc} onChange={(e) => setDeliveryDesc(e.target.value)} placeholder="e.g. Package arrived at local distribution center." rows="3" required></textarea>
              </div>
              <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem', background: '#8b5cf6' }}>
                Post Delivery Status Update
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
