import { useState } from 'react';
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
  Briefcase,
  Phone
} from 'lucide-react';
import './Auth.css';

export default function Register({
  theme,
  role,
  setRole,
  fullName,
  setFullName,
  email,
  setEmail,
  phone,
  setPhone,
  password,
  setPassword,
  loading,
  error,
  onSubmit,
  switchToLogin
}) {
  const [confirmPassword, setConfirmPassword] = useState('');
const [agreeTerms, setAgreeTerms] = useState(false);
const [showPassword, setShowPassword] = useState(false);
const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const handleRoleSelect = (selectedRole) => {
    setRole(selectedRole);
    setError(''); // clear error when role changes
  };

  return (
    <div className={`auth-page-container ${theme === 'light' ? 'light-theme' : ''}`}>
      
      {/* Background blobs matching main site */}
      <div className="bg-blobs">
        <div className="bg-blob blob-1"></div>
        <div className="bg-blob blob-2"></div>
        <div className="bg-blob blob-3"></div>
      </div>

      <div className="auth-card glass-card" style={{ maxWidth: '600px' }}>
        
        {/* Back Link */}
        <a 
          href="#" 
          className="back-to-home-link"
          onClick={(e) => {
            e.preventDefault();
            onNavigate('landing');
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
          <h2 className="auth-title">Create Account</h2>
          <p className="auth-subtitle">Register and begin your journey on our commerce hub</p>
        </div>

        {/* Role Selector Cards */}
        <div className="role-selector-container">
          <span className="role-selector-label">Choose Your Account Type</span>
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

          </div>
        </div>

        {/* Error Alert Display */}
        {error && (
          <div className="auth-error-banner" style={{ marginBottom: '20px' }}>
            <AlertCircle size={16} />
            <span>{error}</span>
          </div>
        )}

        {/* Sign Up Form */}
        <form className="auth-form" onSubmit={onSubmit}>
          
          {/* Row 1: Full Name & Email */}
          <div className="form-row-2">
            <div className="auth-input-group">
              <label htmlFor="name">Full Name</label>
              <div className="auth-input-wrapper">
                <User className="auth-input-icon" size={18} />
                <input 
                  id="name"
                  type="text"
                  className="auth-input"
                  placeholder="John Doe"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  disabled={loading}
                />
              </div>
            </div>

            <div className="auth-input-group">
              <label htmlFor="email">
                {role === 'vendor' ? 'Business Email' : 'Email Address'}
              </label>
              <div className="auth-input-wrapper">
                <Mail className="auth-input-icon" size={18} />
                <input 
                  id="email"
                  type="email"
                  className="auth-input"
                  placeholder={role === 'vendor' ? 'sales@shop.com' : 'you@example.com'}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={loading}
                />
              </div>
            </div>
          </div>

          {/* Row 2 (Vendor specific details): Shop Name & Category */}
          {role === 'vendor' && (
            <div className="form-row-2">
              <div className="auth-input-group">
                <label htmlFor="shopName">Shop / Brand Name</label>
                <div className="auth-input-wrapper">
                  <Briefcase className="auth-input-icon" size={18} />
                  <input 
                    id="shopName"
                    type="text"
                    className="auth-input"
                    placeholder="MegaTech Traders"
                    value={shopName}
                    onChange={(e) => setShopName(e.target.value)}
                    disabled={loading}
                  />
                </div>
              </div>

              <div className="auth-input-group">
                <label htmlFor="category">Primary Catalog Category</label>
                <select 
                  id="category"
                  className="auth-input auth-select"
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  disabled={loading}
                >
                  <option value="electronics">Electronics & Gadgets</option>
                  <option value="fashion">Fashion & Runners</option>
                  <option value="home">Home & Living</option>
                  <option value="beauty">Beauty & Wellness</option>
                </select>
              </div>
            </div>
          )}

          {/* Row 3 (Vendor phone details or spacing spacer) */}
          {role === 'vendor' && (
            <div className="auth-input-group">
              <label htmlFor="phone">Contact Number / Support Line</label>
              <div className="auth-input-wrapper">
                <Phone className="auth-input-icon" size={18} />
                <input 
                  id="phone"
                  type="text"
                  className="auth-input"
                  placeholder="+1 (555) 019-2834"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  disabled={isLoading}
                />
              </div>
            </div>
          )}

          {/* Row 4: Password & Confirm Password */}
          <div className="form-row-2">
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
                  disabled={isLoading}
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

            <div className="auth-input-group">
              <label htmlFor="confirmPassword">Confirm Password</label>
              <div className="auth-input-wrapper">
                <LockIcon className="auth-input-icon" size={18} />
                <input 
                  id="confirmPassword"
                  type={showConfirmPassword ? 'text' : 'password'}
                  className="auth-input"
                  placeholder="••••••••"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  disabled={isLoading}
                />
                <button 
                  type="button"
                  className="password-toggle-btn"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  title={showConfirmPassword ? "Hide password" : "Show password"}
                >
                  {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>
          </div>

          {/* Terms Agreement Checkbox */}
          <div className="form-options" style={{ justifyContent: 'flex-start' }}>
            <label className="remember-me">
              <input 
                type="checkbox"
                checked={agreeTerms}
                onChange={(e) => setAgreeTerms(e.target.checked)}
                disabled={isLoading}
              />
              <span style={{ fontSize: '13px', lineHeight: '1.4' }}>
                I agree to the <a href="#" style={{ color: 'var(--primary-glow)', textDecoration: 'none' }} onClick={(e)=>e.preventDefault()}>Terms of Service</a> and <a href="#" style={{ color: 'var(--primary-glow)', textDecoration: 'none' }} onClick={(e)=>e.preventDefault()}>Privacy Policy</a>.
              </span>
            </label>
          </div>

          <button 
            type="submit" 
            className="auth-submit-btn" 
            disabled={loading || !agreeTerms}
          >
            {loading ? (
              <>
                <div className="spinner"></div>
                <span>Creating Account...</span>
              </>
            ) : (
              <span>Register as {role === 'customer' ? 'Customer' : 'Vendor'}</span>
            )}
          </button>

        </form>

        {/* Switch Link */}
        <div className="auth-footer" style={{ marginTop: '24px' }}>
          <span>Already have an account? </span>
          <a 
            href="#" 
            className="auth-switch-link"
            onClick={(e) => {
              e.preventDefault();
              switchToLogin();
            }}
          >
            Sign In
          </a>
        </div>

      </div>
    </div>
  );
}
