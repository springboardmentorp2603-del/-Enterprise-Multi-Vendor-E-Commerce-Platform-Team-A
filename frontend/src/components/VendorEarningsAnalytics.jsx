import React, { useEffect, useMemo, useState } from "react";
import { api } from "../api";
import {
  ResponsiveContainer,
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from "recharts";

/**
 * VendorEarningsAnalytics
 *
 * Drop-in tab for VendorPortal.jsx, matching the same self-contained
 * data-fetching pattern as the rest of the file (e.g. loadVendorProfile).
 *
 * Uses api.vendor.getEarnings(vendorId, range) — matching the structured
 * style of your other calls (api.vendor.getProfile(), api.vendor.uploadDocument()).
 * You'll need to add a getEarnings method to the `vendor` section of your
 * api.js file if it isn't there yet — see the note at the bottom of this file.
 *
 * Expects a response shaped like:
 * {
 *   "totalEarnings": 45000,
 *   "netPayout": 40500,
 *   "avgOrderValue": 1200,
 *   "totalOrders": 38,
 *   "trend": [{ "label": "Jul 1", "earnings": 3200 }],
 *   "topProducts": [{ "productName": "Wireless Mouse", "revenue": 8000, "unitsSold": 40 }]
 * }
 */

const RANGE_OPTIONS = [
  { label: "Last 30 days", value: "30d" },
  { label: "Last 90 days", value: "90d" },
  { label: "Last 12 months", value: "1y" },
];

function formatCurrency(value) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(value ?? 0);
}

function SummaryCard({ label, value, sub, accent }) {
  return (
    <div className="glass-card" style={{ padding: "1rem" }}>
      <p style={{ fontSize: "0.8rem", color: "var(--text-secondary)" }}>{label}</p>
      <p style={{ fontSize: "1.5rem", fontWeight: 700, marginTop: "0.25rem", color: accent }}>
        {value}
      </p>
      {sub && (
        <p style={{ fontSize: "0.75rem", color: "var(--text-muted)", marginTop: "0.25rem" }}>
          {sub}
        </p>
      )}
    </div>
  );
}

export default function VendorEarningsAnalytics({ vendorId }) {
  const [range, setRange] = useState("30d");
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Ledger States
  const [ledger, setLedger] = useState([]);
  const [ledgerPage, setLedgerPage] = useState(0);
  const [ledgerTotalPages, setLedgerTotalPages] = useState(1);
  const [ledgerLoading, setLedgerLoading] = useState(false);
  const [filterType, setFilterType] = useState("");
  const [filterOrderId, setFilterOrderId] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function fetchEarnings() {
      setLoading(true);
      setError(null);
      try {
        const response = await api.vendor.getEarnings(vendorId, range);
        if (!cancelled) setData(response);
      } catch (err) {
        if (!cancelled) setError(err.message || "Failed to load earnings");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    if (vendorId) fetchEarnings();
    return () => {
      cancelled = true;
    };
  }, [vendorId, range]);

  useEffect(() => {
    let cancelled = false;

    async function fetchLedger() {
      setLedgerLoading(true);
      try {
       const params = {
  page: 0,
  size: 100,
};

const response = await api.vendor.getCommissionLedger(params);

if (!cancelled) {
  const allLedger = response.content || response.data?.content || [];

  const filteredLedger = allLedger.filter((row) => {
    const matchesType =
      !filterType ||
      row.transactionType?.toUpperCase() === filterType.toUpperCase();

    const matchesOrder =
      !filterOrderId.trim() ||
      row.orderId?.toLowerCase().includes(filterOrderId.trim().toLowerCase());

    return matchesType && matchesOrder;
  });

  setLedger(filteredLedger);
  setLedgerTotalPages(1);
  setLedgerPage(0);
}
      } catch (err) {
        console.error("Failed to fetch ledger", err);
      } finally {
        if (!cancelled) setLedgerLoading(false);
      }
    }

    fetchLedger();
    return () => {
      cancelled = true;
    };
  }, [ledgerPage, filterType, filterOrderId]);

  const trendData = useMemo(() => data?.trend ?? [], [data]);
  const topProducts = useMemo(() => data?.topProducts ?? [], [data]);

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "1.5rem" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: "0.75rem" }}>
        <h3 style={{ fontFamily: "var(--font-heading)", fontWeight: 700 }}>Earnings Analytics</h3>
        <div style={{ display: "flex", gap: "0.5rem" }}>
          {RANGE_OPTIONS.map((opt) => (
            <button
              key={opt.value}
              className={`btn ${range === opt.value ? "btn-primary" : "btn-secondary"}`}
              onClick={() => setRange(opt.value)}
              style={{ fontSize: "0.8rem", padding: "0.4rem 0.75rem" }}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </div>

      {error && (
        <div className="glass-card" style={{ borderLeft: "4px solid #ef4444", background: "rgba(239,68,68,0.05)" }}>
          <p style={{ fontSize: "0.9rem", color: "#ef4444" }}>
            Couldn't load earnings data: {error}
          </p>
        </div>
      )}

      {loading ? (
        <div className="stats-grid">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="glass-card stat-card" style={{ opacity: 0.5 }}>
              <span style={{ color: "var(--text-secondary)" }}>Loading…</span>
            </div>
          ))}
        </div>
      ) : (
        data && (
          <>
            <div className="stats-grid" style={{ gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))" }}>
              <SummaryCard
                label="Gross Sales"
                value={formatCurrency(data.grossSales || data.totalEarnings)}
                sub={`${data.totalOrders} orders`}
              />
              <SummaryCard
                label="Platform Commission"
                value={formatCurrency(data.platformCommission)}
                accent="#f59e0b"
              />
              <SummaryCard
                label="Refund Reversals"
                value={formatCurrency(data.refundReversalAmount)}
                accent="#ef4444"
              />
              <SummaryCard
                label="Net Vendor Earnings"
                value={formatCurrency(data.netVendorEarnings || data.netPayout)}
                sub="after commission & reversals"
                accent="#10b981"
              />
            </div>

            <div className="glass-card">
              <h4 style={{ fontSize: "0.9rem", fontWeight: 600, marginBottom: "1rem" }}>
                Earnings over time
              </h4>
              <ResponsiveContainer width="100%" height={280}>
                <LineChart data={trendData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }} tickFormatter={(v) => `₹${v / 1000}k`} />
                  <Tooltip formatter={(v) => formatCurrency(v)} />
                  <Legend />
                  <Line
                    type="monotone"
                    dataKey="earnings"
                    name="Earnings"
                    stroke="#10b981"
                    strokeWidth={2}
                    dot={false}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>

            <div className="glass-card">
              <h4 style={{ fontSize: "0.9rem", fontWeight: 600, marginBottom: "1rem" }}>
                Top products by revenue
              </h4>
              <ResponsiveContainer width="100%" height={280}>
                <BarChart data={topProducts} layout="vertical">
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis type="number" tick={{ fontSize: 12 }} tickFormatter={(v) => `₹${v / 1000}k`} />
                  <YAxis type="category" dataKey="productName" tick={{ fontSize: 12 }} width={140} />
                  <Tooltip formatter={(v) => formatCurrency(v)} />
                  <Bar dataKey="revenue" fill="#111827" radius={[0, 4, 4, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>

            <div className="glass-card">
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.25rem", flexWrap: "wrap", gap: "1rem" }}>
                <h4 style={{ fontSize: "1.1rem", fontWeight: 700 }}>Commission &amp; Earnings Ledger</h4>
                
                <div style={{ display: "flex", gap: "0.5rem", alignItems: "center" }}>
                  <select
                    className="form-input"
                    style={{ padding: "0.4rem 0.75rem", borderRadius: "6px", border: "1px solid var(--border-color)", background: "var(--bg-card)", color: "var(--text-primary)", fontSize: "0.85rem" }}
                    value={filterType}
                    onChange={(e) => { setFilterType(e.target.value); setLedgerPage(0); }}
                  >
                    <option value="">All Transactions</option>
                    <option value="COMMISSION">Commission</option>
                    <option value="REFUND_REVERSAL">Refund Reversal</option>
                  </select>
                  
                  <input
                    type="text"
                    placeholder="Filter Order ID"
                    className="form-input"
                    style={{ padding: "0.4rem 0.75rem", borderRadius: "6px", border: "1px solid var(--border-color)", background: "var(--bg-card)", color: "var(--text-primary)", fontSize: "0.85rem", width: "160px" }}
                    value={filterOrderId}
                    onChange={(e) => { setFilterOrderId(e.target.value); setLedgerPage(0); }}
                  />
                </div>
              </div>

              {ledgerLoading ? (
                <div style={{ display: "flex", justifyContent: "center", padding: "2rem" }}>
                  <div className="spinner"></div>
                </div>
              ) : (
                <div className="table-container" style={{ width: "100%", overflowX: "auto",}}>
                  <table className="custom-table" style={{ fontSize: "0.85rem" , width: "100%", minWidth: "1100px",}}>
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>Order Ref</th>
                        <th>Product ID</th>
                        <th>Gross Amount</th>
                        <th>Rate</th>
                        <th>Commission</th>
                        <th>Vendor Earnings</th>
                        <th>Type</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {ledger.map((row) => (
                        <tr key={row.id}>
                          <td>{new Date(row.createdAt).toLocaleDateString()}</td>
                          <td><span title={row.orderId}>#{row.orderId.substring(0, 8)}...</span></td>
                          <td><span title={row.productId}>#{row.productId.substring(0, 8)}...</span></td>
                          <td style={{ fontWeight: 600 }}>{formatCurrency(row.grossAmount)}</td>
                          <td>{row.commissionRate}%</td>
                          <td style={{ color: row.commissionAmount < 0 ? "#ef4444" : "#f59e0b", fontWeight: 600 }}>
                            {formatCurrency(row.commissionAmount)}
                          </td>
                          <td style={{ color: row.vendorAmount < 0 ? "#ef4444" : "#10b981", fontWeight: 600 }}>
                            {formatCurrency(row.vendorAmount)}
                          </td>
                          <td style={{ whiteSpace: "nowrap"}}>
                            <span className="badge" style={{
                              display: "inline-block",
                              position: "static",
                              background: row.transactionType === "COMMISSION" ? "rgba(59, 130, 246, 0.15)" : "rgba(239, 68, 68, 0.15)",
                              color: row.transactionType === "COMMISSION" ? "#3b82f6" : "#ef4444",
                              fontSize: "0.75rem",
                              whiteSpace: "nowrap"
                            }}>
                              {row.transactionType}
                            </span>
                          </td>
                          <td style={{ whiteSpace: "nowrap"}}>
                            <span className="badge" style={{
                              display: "inline-block",
                              position: "static",
                              background: row.status === "CONFIRMED" ? "rgba(16, 185, 129, 0.15)" : "rgba(245, 158, 11, 0.15)",
                              color: row.status === "CONFIRMED" ? "#10b981" : "#f59e0b",
                              fontSize: "0.75rem",
                              whiteSpace: "nowrap",
                            }}>
                              {row.status}
                            </span>
                          </td>
                        </tr>
                      ))}
                      {ledger.length === 0 && (
                        <tr>
                          <td colSpan="9" style={{ textAlign: "center", color: "var(--text-muted)", padding: "2rem" }}>
                            No ledger transactions recorded.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              )}

              {/* Pagination */}
              {ledgerTotalPages > 1 && (
                <div style={{ display: "flex", justifyContent: "center", gap: "0.5rem", marginTop: "1.25rem" }}>
                  <button
                    className="btn btn-secondary"
                    style={{ padding: "0.25rem 0.5rem", fontSize: "0.8rem" }}
                    disabled={ledgerPage === 0}
                    onClick={() => setLedgerPage(prev => prev - 1)}
                  >
                    Previous
                  </button>
                  <span style={{ display: "flex", alignItems: "center", fontSize: "0.85rem", color: "var(--text-secondary)" }}>
                    Page {ledgerPage + 1} of {ledgerTotalPages}
                  </span>
                  <button
                    className="btn btn-secondary"
                    style={{ padding: "0.25rem 0.5rem", fontSize: "0.8rem" }}
                    disabled={ledgerPage >= ledgerTotalPages - 1}
                    onClick={() => setLedgerPage(prev => prev + 1)}
                  >
                    Next
                  </button>
                </div>
              )}
            </div>
          </>
        )
      )}
    </div>
  );
}

/*
 * NOTE: add this to the `vendor` section of your api.js, alongside
 * getProfile / register / uploadDocument:
 *
 *   getEarnings: (vendorId, range) =>
 *     request(`/api/vendor/${vendorId}/earnings?range=${range}`, { method: 'GET' }),
 *
 * Adjust the request helper call to match however getProfile() is
 * implemented in your actual api.js (fetch vs axios wrapper).
 */