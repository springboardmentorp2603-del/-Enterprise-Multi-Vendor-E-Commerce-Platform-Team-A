import React, { useState, useEffect } from 'react';
import { api } from '../api';
import { Bar, Line, Pie } from 'react-chartjs-2';
import Chart from 'chart.js/auto';

/**
 * ReportDashboard – renders the 'Reports' admin tab.
 * Fetches report data from the backend using api.admin.getReports().
 * Displays a loading spinner while fetching and visualizes data using Chart.js.
 * Errors are surfaced via the provided addToast callback.
 */
function ReportDashboard({ addToast }) {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    setLoading(true);
    try {
      const response = await api.admin.getReports();
      setReports(response?.data || response || []);
    } catch (err) {
      addToast(err.message || 'Failed to load reports', 'error');
    } finally {
      setLoading(false);
    }
  };

  // Prepare chart data – assume each report item may contain `label` and `value` arrays.
  const chartData = {
    labels: reports.map((r, i) => r.title ?? `Report ${i + 1}`),
    datasets: [
      {
        label: 'Report Values',
        data: reports.map((r) => r.value ?? Math.random() * 100),
        backgroundColor: 'rgba(54, 162, 235, 0.4)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1,
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        beginAtZero: true,
      },
    },
  };

  return (
    <div className="report-dashboard" style={{ height: '500px' }}>
      <h2 style={{ fontFamily: 'var(--font-heading)', fontWeight: 800, marginBottom: '1rem' }}>Reports</h2>
      {loading && (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '2rem' }}>
          <div className="spinner" style={{ width: '30px', height: '30px' }} />
        </div>
      )}
      {!loading && reports.length === 0 && (
        <p style={{ color: 'var(--text-secondary)', textAlign: 'center' }}>No reports available.</p>
      )}
      {!loading && reports.length > 0 && (
        <div style={{ position: 'relative', height: '400px' }}>
          <Bar data={chartData} options={chartOptions} />
        </div>
      )}
    </div>
  );
}

export default ReportDashboard;
