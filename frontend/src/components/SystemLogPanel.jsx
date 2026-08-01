import React, { useState, useEffect } from 'react';
import { api } from '../api';

/**
 * SystemLogPanel – renders the 'System Logs' admin tab.
 * Fetches system log entries from the backend using api.admin.getSystemLogs().
 * Shows a spinner while loading and displays logs in a table.
 * Errors are reported via the addToast callback.
 */
function SystemLogPanel({ addToast }) {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchLogs();
  }, []);

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const response = await api.admin.getSystemLogs();
      setLogs(response?.data || response || []);
    } catch (err) {
      addToast(err.message || 'Failed to load system logs', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="system-log-panel">
      <h2 style={{ fontFamily: 'var(--font-heading)', fontWeight: 800, marginBottom: '1rem' }}>System Logs</h2>
      {loading && (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '2rem' }}>
          <div className="spinner" style={{ width: '30px', height: '30px' }}></div>
        </div>
      )}
      {!loading && logs.length === 0 && (
        <p style={{ color: 'var(--text-secondary)', textAlign: 'center' }}>No log entries found.</p>
      )}
      {logs.length > 0 && (
        <div className="table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Level</th>
                <th>Message</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((log, idx) => (
                <tr key={idx}>
                  <td>{log.timestamp ?? ''}</td>
                  <td>{log.level ?? ''}</td>
                  <td>{log.message ?? ''}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default SystemLogPanel;
