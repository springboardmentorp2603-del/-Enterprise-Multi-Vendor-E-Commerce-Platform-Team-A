import React, { useState } from 'react';
import { api, tokenStorage } from '../api';

export default function AuthPortal({ onAuthSuccess, addToast }) {
  const [isLogin, setIsLogin] = useState(true);
  const [role, setRole] = useState('CUSTOMER'); // For registration: CUSTOMER or VENDOR
  const [loading, setLoading] = useState(false);

  // Form states
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');

  // Vendor extra fields for initial account creation
  const [businessName, setBusinessName] = useState('');
  const [gstNumber, setGstNumber] = useState('');
  const [panNumber, setPanNumber] = useState('');
  const [businessType, setBusinessType] = useState('PROPRIETORSHIP');

  const handleLogin = async (e) => {
    e.preventDefault();
    if (!email || !password) {
      addToast('Please enter both email and password', 'error');
      return;
    }
    setLoading(true);
    try {
      const response = await api.auth.login({ email, password });
      tokenStorage.setToken(response.token);
      
      // Fetch details of the logged in user
      const user = await api.auth.me();
      onAuthSuccess(user);
      addToast(`Welcome back, ${user.name}!`, 'success');
    } catch (err) {
      addToast(err.message || 'Login failed. Please check credentials.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!email || !password) {
      addToast('Email and password are required', 'error');
      return;
    }

    setLoading(true);
    try {
      if (role === 'CUSTOMER') {
        if (!fullName || !phone) {
          addToast('Full Name and Phone Number are required', 'error');
          setLoading(false);
          return;
        }
        // Register Customer (calls POST /api/v1/customers which creates User & Customer profile)
        await api.customer.register({
          fullName,
          email,
          password,
          phoneNumber: phone
        });
        addToast('Registration successful! Please login.', 'success');
        setIsLogin(true);
      } else {
        // Register VENDOR or ADMIN (via general auth/register)
        // This creates user account. Profile is completed upon first login.
        if (!fullName) {
          addToast('Name is required', 'error');
          setLoading(false);
          return;
        }
        await api.auth.register({
          name: fullName,
          email,
          password,
          role: 'VENDOR',
          phone: phone ? parseInt(phone.replace(/\D/g, '')) : null
        });
        addToast('Vendor account created! Please login to complete business profile.', 'success');
        setIsLogin(true);
      }
    } catch (err) {
      addToast(err.message || 'Registration failed.', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh', padding: '2rem' }}>
      <div className="glass-card" style={{ maxWidth: '480px', width: '100%', padding: '2.5rem', textAlign: 'center' }}>
        
        {/* Logo/Icon */}
        <div style={{ marginBottom: '1.5rem' }}>
          <span style={{ fontSize: '3rem' }}>🛍️</span>
          <h2 style={{ fontFamily: 'var(--font-heading)', fontWeight: 800, marginTop: '0.5rem' }}>ShopStack</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Enterprise Multi-Vendor Platform</p>
        </div>

        {/* Tab Selection */}
        <div style={{ display: 'flex', borderBottom: '1px solid var(--border-color)', marginBottom: '2rem' }}>
          <button 
            className={`nav-link ${isLogin ? 'active' : ''}`}
            style={{ flex: 1, paddingBottom: '0.75rem', background: 'transparent', border: 'none', fontWeight: 600 }}
            onClick={() => setIsLogin(true)}
          >
            Sign In
          </button>
          <button 
            className={`nav-link ${!isLogin ? 'active' : ''}`}
            style={{ flex: 1, paddingBottom: '0.75rem', background: 'transparent', border: 'none', fontWeight: 600 }}
            onClick={() => setIsLogin(false)}
          >
            Register
          </button>
        </div>

        {isLogin ? (
          /* Login Form */
          <form onSubmit={handleLogin}>
            <div className="form-group">
              <label className="form-label">Email Address</label>
              <input 
                type="email" 
                className="form-input" 
                placeholder="you@example.com" 
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            
            <div className="form-group">
              <label className="form-label">Password</label>
              <input 
                type="password" 
                className="form-input" 
                placeholder="••••••••" 
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }} disabled={loading}>
              {loading ? <div className="spinner" style={{ width: '20px', height: '20px' }}></div> : 'Access Account'}
            </button>
          </form>
        ) : (
          /* Registration Form */
          <form onSubmit={handleRegister}>
            {/* Account Type Toggle */}
            <div className="form-group" style={{ marginBottom: '1.5rem' }}>
              <label className="form-label">I want to join as a</label>
              <div style={{ display: 'flex', gap: '1rem', marginTop: '0.25rem' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', color: 'var(--text-primary)' }}>
                  <input 
                    type="radio" 
                    name="role" 
                    value="CUSTOMER" 
                    checked={role === 'CUSTOMER'}
                    onChange={() => setRole('CUSTOMER')}
                  />
                  Customer
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', color: 'var(--text-primary)' }}>
                  <input 
                    type="radio" 
                    name="role" 
                    value="VENDOR" 
                    checked={role === 'VENDOR'}
                    onChange={() => setRole('VENDOR')}
                  />
                  Seller / Vendor
                </label>
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">{role === 'CUSTOMER' ? 'Full Name' : 'Contact Person Name'}</label>
              <input 
                type="text" 
                className="form-input" 
                placeholder="John Doe" 
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Email Address</label>
              <input 
                type="email" 
                className="form-input" 
                placeholder="name@company.com" 
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Phone Number</label>
              <input 
                type="tel" 
                className="form-input" 
                placeholder="10 digit mobile number" 
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <input 
                type="password" 
                className="form-input" 
                placeholder="Min 8 characters" 
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                minLength={role === 'CUSTOMER' ? 6 : 8}
              />
            </div>

            <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1.5rem' }} disabled={loading}>
              {loading ? <div className="spinner" style={{ width: '20px', height: '20px' }}></div> : 'Create Free Account'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
