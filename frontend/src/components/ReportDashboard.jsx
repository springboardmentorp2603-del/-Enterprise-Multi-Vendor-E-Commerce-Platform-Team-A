import React, { useState, useEffect } from 'react';
import { api } from '../api';
import { Bar, Line, Pie } from 'react-chartjs-2';
import Chart from 'chart.js/auto';

function ReportDashboard({ addToast }) {
  const today = new Date().toISOString().split('T')[0];
  const monthAgo = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

  const [from, setFrom] = useState(monthAgo);
  const [to, setTo] = useState(today);

  const [sales, setSales] = useState([]);
  const [vendors, setVendors] = useState([]);
  const [orders, setOrders] = useState([]);
  const [financial, setFinancial] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchAllReports();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchAllReports = async () => {
    setLoading(true);
    try {
      const [salesRes, vendorsRes, ordersRes, financialRes] = await Promise.all([
        api.admin.getSalesReport(from, to),
        api.admin.getVendorReport(from, to),
        api.admin.getOrderReport(from, to),
        api.admin.getFinancialReport(from, to),
      ]);
      setSales(salesRes?.data || []);
      setVendors(vendorsRes?.data || []);
      setOrders(ordersRes?.data || []);
      setFinancial(financialRes?.data || null);
    } catch (err) {
      addToast(err.message || 'Failed to load reports', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleExport = async (type, format) => {
    try {
      const result = type === 'sales'
        ? await api.admin.exportSalesReport(from, to, format)
        : await api.admin.exportVendorReport(from, to);
      const path = result?.data?.path;
      if (path) {
        window.open(`/${path}`, '_blank');
      } else {
        addToast('Could not generate the report file', 'error');
      }
    } catch (err) {
      addToast(err.message || 'Export failed', 'error');
    }
  };

  // ---- Chart data prep ----
  const salesChartData = {
    labels: sales.map((s) => s.date),
    datasets: [
      {
        label: 'Daily Revenue (₹)',
        data: sales.map((s) => s.revenue),
        borderColor: 'var(--primary, #6366f1)',
        backgroundColor: 'rgba(99, 102, 241, 0.2)',
        tension: 0.3,
        fill: true,
      },
    ],
  };

  const vendorChartData = {
    labels: vendors.map((v) => v.businessName),
    datasets: [
      {
        label: 'Revenue by Vendor (₹)',
        data: vendors.map((v) => v.totalRevenue),
        backgroundColor: 'rgba(6, 182, 212, 0.5)',
        borderColor: 'rgba(6, 182, 212, 1)',
        borderWidth: 1,
      },
    ],
  };

  const statusCounts = orders.reduce((acc, o) => {
    acc[o.status] = (acc[o.status] || 0) + 1;
    return acc;
  }, {});
  const statusChartData = {
    labels: Object.keys(statusCounts),
    datasets: [
      {
        data: Object.values(statusCounts),
        backgroundColor: ['#6366f1', '#06b6d4', '#10b981', '#f59e0b', '#ef4444', '#d946ef'],
      },
    ],
  };

  const chartOptions = { responsive: true, maintainAspectRatio: false };

  const statCard = (label, value, prefix = '') => (
    <div
      className="dashboard-card"
      style={{
        background: 'var(--bg-card)',
        border: '1px solid var(--border-color)',
        borderRadius: 'var(--radius-md)',
        padding: '1rem 1.25rem',
        flex: '1 1 160px',
      }}
    >
      <div style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', marginBottom: '0.25rem' }}>{label}</div>
      <div style={{ fontFamily: 'var(--font-heading)', fontWeight: 800, fontSize: '1.4rem', color: 'var(--text-primary)' }}>
        {prefix}{typeof value === 'number' ? value.toLocaleString('en-IN', { maximumFractionDigits: 2 }) : value ?? '—'}
      </div>
    </div>
  );

  return (
    <div className="report-dashboard">
      <h2 style={{ fontFamily: 'var(--font-heading)', fontWeight: 800, marginBottom: '1rem' }}>Reports</h2>

      {/* Date range controls */}
      <div style={{ display: 'flex', gap: '1rem', alignItems: 'flex-end', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
        <div>
          <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '0.25rem' }}>From</label>
          <input type="date" className="form-input" value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div>
          <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '0.25rem' }}>To</label>
          <input type="date" className="form-input" value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
        <button className="btn btn-primary" onClick={fetchAllReports} disabled={loading}>
          {loading ? 'Loading...' : 'Apply Range'}
        </button>
        <button className="btn btn-secondary" onClick={() => handleExport('sales', 'pdf')}>Export Sales PDF</button>
        <button className="btn btn-secondary" onClick={() => handleExport('sales', 'excel')}>Export Sales Excel</button>
        <button className="btn btn-secondary" onClick={() => handleExport('vendors')}>Export Vendor Excel</button>
      </div>

      {loading && (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '2rem' }}>
          <div className="spinner" style={{ width: '30px', height: '30px' }} />
        </div>
      )}

      {!loading && (
        <>
          {/* Financial summary cards — each stat on its own card, no scale-mixing */}
          {financial && (
            <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', marginBottom: '2rem' }}>
              {statCard('Total Orders', financial.totalOrders)}
              {statCard('Total Revenue', financial.totalRevenue, '₹')}
              {statCard('GST Collected', financial.totalGstCollected, '₹')}
              {statCard('Commission Earned', financial.totalCommissionEarned, '₹')}
              {statCard('Net Revenue', financial.netRevenue, '₹')}
            </div>
          )}

          {/* Sales trend */}
          <div style={{ marginBottom: '2rem' }}>
            <h3 style={{ fontFamily: 'var(--font-heading)', marginBottom: '0.75rem' }}>Sales Trend</h3>
            {sales.length === 0 ? (
              <p style={{ color: 'var(--text-secondary)' }}>No sales in this date range.</p>
            ) : (
              <div style={{ position: 'relative', height: '300px' }}>
                <Line data={salesChartData} options={chartOptions} />
              </div>
            )}
          </div>

          {/* Vendor performance */}
          <div style={{ marginBottom: '2rem' }}>
            <h3 style={{ fontFamily: 'var(--font-heading)', marginBottom: '0.75rem' }}>Vendor Performance</h3>
            {vendors.length === 0 ? (
              <p style={{ color: 'var(--text-secondary)' }}>No vendor sales in this date range.</p>
            ) : (
              <>
                <div style={{ position: 'relative', height: '300px', marginBottom: '1rem' }}>
                  <Bar data={vendorChartData} options={chartOptions} />
                </div>
                <div className="table-container">
                  <table className="custom-table">
                    <thead>
                      <tr>
                        <th>Vendor</th>
                        <th>Orders</th>
                        <th>Revenue</th>
                        <th>Commission Earned</th>
                      </tr>
                    </thead>
                    <tbody>
                      {vendors.map((v) => (
                        <tr key={v.vendorId}>
                          <td>{v.businessName}</td>
                          <td>{v.totalOrders}</td>
                          <td>₹{v.totalRevenue?.toLocaleString('en-IN')}</td>
                          <td>₹{v.commissionEarned?.toLocaleString('en-IN')}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </>
            )}
          </div>

          {/* Order status breakdown */}
          <div style={{ marginBottom: '2rem' }}>
            <h3 style={{ fontFamily: 'var(--font-heading)', marginBottom: '0.75rem' }}>Order Status Breakdown</h3>
            {orders.length === 0 ? (
              <p style={{ color: 'var(--text-secondary)' }}>No orders in this date range.</p>
            ) : (
              <div style={{ position: 'relative', height: '300px', maxWidth: '400px' }}>
                <Pie data={statusChartData} options={chartOptions} />
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}

export default ReportDashboard;