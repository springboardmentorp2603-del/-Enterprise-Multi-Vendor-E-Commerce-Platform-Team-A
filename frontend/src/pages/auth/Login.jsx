import { useState } from 'react';

// Local error handling state for this component
import { 
  Layers, 
  User, 
  Warehouse, 
  Mail, 
  Lock as LockIcon, 
  Eye, 
  EyeOff, 
  ArrowLeft, 
  AlertCircle,
  Shield
} from 'lucide-react';
import './Auth.css';

export default function Login({
  theme,
  role,
  setRole,
  email,
  setEmail,
  password,
  setPassword,
  loading,
  // error prop is no longer used; internal state will manage errors
  onSubmit,
  switchToRegister
}) {
  // Internal error state to replace missing external setError
  const [localError, setLocalError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);

  const handleRoleSelect = (selectedRole) => {
    setRole(selectedRole);
    // clear error when role changes
    setLocalError('');
  };

  return (
    <div className={`auth-page-container ${theme === 'light' ? 'light-theme' : ''}`}>
      
      {/* Background blobs matching main site */}
      <div className="bg-blobs">
        <div className="bg-blob blob-1"></div>
        <div className="bg-blob blob-2"></div>
        <div className="bg-blob blob-3"></div>
      </div>

      <div className="auth-card glass-card">
        
        {/* Back Link */}
        <a 
          href="#" 
          className="back-to-home-link"
          onClick={(e) => {
            e.preventDefault();
            window.location.href = '/'; // Navigate back to the main marketplace
          }}
        >
          <ArrowLeft size={14} />
          <span>Back to Marketplace</span>
        </a>

        {/* Auth Page Header */}
        <div className="auth-header" style={{ marginTop: '16px' }}>
          <div className="auth-logo">
            <div className="auth-logo-icon">
              <Layers size={24} color="#fff" />
            </div>
            <span>Shop<span className="text-gradient">Stack</span></span>
          </div>
          <h2 className="auth-title">Welcome Back</h2>
          <p className="auth-subtitle">Sign in to manage your marketplace experience</p>
        </div>

        {/* Role Selector Cards */}
        <div className="role-selector-container">
          <span className="role-selector-label">Select Your Role</span>
          <div className="role-cards-grid">
            
            {/* Customer Role Card */}
            <div 
              className={`role-card ${role === 'customer' ? 'active' : ''}`}
              onClick={() => handleRoleSelect('customer')}
            >
              <div className="role-card-icon">
                <User size={20} />
              </div>
              <h3 className="role-card-title">Customer</h3>
              <p className="role-card-desc">Browse catalogs, buy goods, and track order histories.</p>
              <div className="role-card-badge">
                {role === 'customer' && <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#fff' }}></span>}
              </div>
            </div>

            {/* Vendor Role Card */}
            <div 
              className={`role-card ${role === 'vendor' ? 'active' : ''}`}
              onClick={() => handleRoleSelect('vendor')}
            >
              <div className="role-card-icon">
                <Warehouse size={20} />
              </div>
              <h3 className="role-card-title">Vendor</h3>
              <p className="role-card-desc">List products, fulfill orders, and monitor sales analytics.</p>
              <div className="role-card-badge">
                {role === 'vendor' && <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#fff' }}></span>}
              </div>
            </div>

            {/* Admin Role Card */}
            <div 
              className={`role-card ${role === 'admin' ? 'active' : ''}`}
              onClick={() => handleRoleSelect('admin')}
            >
              <div className="role-card-icon">
                <Shield size={20} />
              </div>
              <h3 className="role-card-title">Admin</h3>
              <p className="role-card-desc">Review listed products, verify vendors, and manage site settings.</p>
              <div className="role-card-badge">
                {role === 'admin' && <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#fff' }}></span>}
              </div>
            </div>

          </div>
        </div>

        {/* Error Alert Display */}
        {localError && (
            <div className="auth-error-banner" style={{ marginBottom: '20px' }}>
              <AlertCircle size={16} />
              <span>{localError}</span>
            </div>
          )}

        {/* Sign In Form */}
        <form className="auth-form" onSubmit={onSubmit}>
          
          <div className="auth-input-group">
            <label htmlFor="email">
              {role === 'customer' ? 'Email Address' : role === 'vendor' ? 'Business Email Address' : 'Admin Email Address'}
            </label>
            <div className="auth-input-wrapper">
              <Mail className="auth-input-icon" size={18} />
              <input 
                id="email"
                type="email"
                className="auth-input"
                placeholder={role === 'customer' ? 'you@example.com' : role === 'vendor' ? 'seller@company.com' : 'admin@shopstack.com'}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                disabled={loading}
              />
            </div>
          </div>

          <div className="auth-input-group">
            <label htmlFor="password">Password</label>
            <div className="auth-input-wrapper">
              <LockIcon className="auth-input-icon" size={18} />
              <input 
                id="password"
                type={showPassword ? 'text' : 'password'}
                className="auth-input"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={loading}
              />
              <button 
                type="button"
                className="password-toggle-btn"
                onClick={() => setShowPassword(!showPassword)}
                title={showPassword ? "Hide password" : "Show password"}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <div className="form-options">
            <label className="remember-me">
              <input 
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                disabled={loading}
              />
              <span>Remember me</span>
            </label>
            <a 
              href="#" 
              className="forgot-password-link"
              onClick={(e) => {
                e.preventDefault();
                setLocalError('Password reset instructions sent to your email.');
              }}
            >
              Forgot password?
            </a>
          </div>

          <button 
            type="submit" 
            className="auth-submit-btn" 
            disabled={loading}
          >
            {loading ? (
              <>
                <div className="spinner"></div>
                <span>Signing In...</span>
              </>
            ) : (
              <span>Sign In as {role === 'customer' ? 'Customer' : role === 'vendor' ? 'Vendor' : 'Admin'}</span>
            )}
          </button>

        </form>

        {/* Social Dividers & Mock Buttons */}
        <div className="social-divider">or continue with</div>

        <div className="social-btn-container">
          <button 
            className="social-auth-btn" 
            title="Google Sign In"
            onClick={() => {
                setEmail('simulated.user@gmail.com');
                setPassword('password123');
                setLocalError('');
              }}
          >
            {/* Simple colored dots simulating Google logo */}
            <span style={{ display: 'inline-flex', gap: '3px' }}>
              <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#ea4335' }}></span>
              <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#4285f4' }}></span>
              <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#fbbc05' }}></span>
            </span>
          </button>
          <button 
            className="social-auth-btn" 
            title="Apple Sign In"
            onClick={() => {
                setEmail('simulated.apple@icloud.com');
                setPassword('passwordApple');
                setLocalError('');
              }}
          >
            {/* Stylized placeholder for Apple */}
            <span style={{ fontSize: '14px', fontWeight: 'bold' }}>A</span>
          </button>
          <button 
            className="social-auth-btn" 
            title="GitHub Sign In"
            onClick={() => {
                setEmail('simulated.github@github.com');
                setPassword('githubSecure8');
                setLocalError('');
              }}
          >
            {/* Stylized placeholder for GitHub */}
            <span style={{ fontSize: '14px', fontWeight: 'bold' }}>G</span>
          </button>
        </div>

        {/* Switch Link */}
        <div className="auth-footer">
          <span>Don't have an account? </span>
          <a 
            href="#" 
            className="auth-switch-link"
            onClick={(e) => {
              e.preventDefault();
              switchToRegister();
            }}
          >
            Sign Up
          </a>
        </div>

      </div>
    </div>
  );
}