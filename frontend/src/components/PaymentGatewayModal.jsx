import { useState } from 'react';
import { CreditCard, Smartphone, ShieldCheck, X, AlertCircle } from 'lucide-react';
import ProductImage from './ProductImage';

export default function PaymentGatewayModal({ 
  cartItems = [], 
  user = {}, 
  onPaymentSuccess, 
  onCancel, 
  theme = 'dark' 
}) {
  const [activeTab, setActiveTab] = useState('card'); // card, upi, netbanking
  const [paymentState, setPaymentState] = useState('idle'); // idle, processing, success
  const [errors, setErrors] = useState({});

  // Input states
  const [cardForm, setCardForm] = useState({
    number: '',
    name: user ? user.name : 'Sarah Jenkins',
    expiry: '',
    cvv: ''
  });
  const [upiId, setUpiId] = useState('');
  const [selectedBank, setSelectedBank] = useState('HDFC Bank');

  // Calculations
  const subtotal = cartItems.reduce((acc, item) => acc + (item.product.price * item.quantity), 0);
  const tax = subtotal * 0.08;
  const shipping = subtotal > 300 ? 0 : 15.00;
  const total = subtotal + tax + shipping;

  // Format Card Number (adds spaces every 4 digits)
  const handleCardNumberChange = (e) => {
    const raw = e.target.value.replace(/\D/g, '');
    const formatted = raw.match(/.{1,4}/g)?.join(' ') || '';
    setCardForm({ ...cardForm, number: formatted.slice(0, 19) }); // max 16 digits + 3 spaces
    setErrors({ ...errors, number: '' });
  };

  // Format Expiry Date (MM/YY)
  const handleExpiryChange = (e) => {
    const raw = e.target.value.replace(/\D/g, '');
    let formatted = raw;
    if (raw.length > 2) {
      formatted = `${raw.slice(0, 2)}/${raw.slice(2, 4)}`;
    }
    setCardForm({ ...cardForm, expiry: formatted.slice(0, 5) });
    setErrors({ ...errors, expiry: '' });
  };

  const handleCvvChange = (e) => {
    const raw = e.target.value.replace(/\D/g, '');
    setCardForm({ ...cardForm, cvv: raw.slice(0, 3) });
    setErrors({ ...errors, cvv: '' });
  };

  const handleNameChange = (e) => {
    setCardForm({ ...cardForm, name: e.target.value });
    setErrors({ ...errors, name: '' });
  };

  // Validation
  const validateForm = () => {
    const errs = {};
    if (activeTab === 'card') {
      const cleanCard = cardForm.number.replace(/\s/g, '');
      if (cleanCard.length < 16) {
        errs.number = 'Please enter a valid 16-digit card number';
      }
      if (!cardForm.name.trim()) {
        errs.name = 'Cardholder name is required';
      }
      const expiryRegex = /^(0[1-9]|1[0-2])\/\d{2}$/;
      if (!expiryRegex.test(cardForm.expiry)) {
        errs.expiry = 'Use format MM/YY';
      }
      if (cardForm.cvv.length < 3) {
        errs.cvv = '3-digit CVV required';
      }
    } else if (activeTab === 'upi') {
      if (!upiId.trim() || !upiId.includes('@')) {
        errs.upiId = 'Please enter a valid UPI ID (e.g., username@bank)';
      }
    }
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handlePay = (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setPaymentState('processing');

    // Simulate Payment Gateway Response
    setTimeout(() => {
      setPaymentState('success');
      
      // Complete checkout after success animation finishes
      setTimeout(() => {
        onPaymentSuccess();
      }, 1200);
    }, 2000);
  };

  return (
    <div className="custom-modal-overlay">
      <div className={`payment-modal-card glass-card ${theme === 'light' ? 'light-theme' : ''}`}>
        
        {/* Left Panel: Summary */}
        <div className="payment-left-panel">
          <div className="payment-summary-header">
            <span className="payment-summary-title">Amount to Pay</span>
            <div className="payment-summary-total">₹{total.toFixed(2)}</div>
          </div>
          
          <h4 style={{ margin: '0 0 12px 0', fontSize: '14px', color: 'var(--text-primary)' }}>Order Summary</h4>
          <div className="payment-items-list">
            {cartItems.map((item, idx) => (
              <div className="payment-item-row" key={idx}>
                <ProductImage src={item.product.image} alt={item.product.name} />
                <div style={{ flexGrow: 1 }}>
                  <h5 style={{ margin: 0, fontWeight: '700', color: 'var(--text-primary)' }}>{item.product.name}</h5>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Qty: {item.quantity}</span>
                </div>
                <div style={{ fontWeight: '600', color: 'var(--text-secondary)' }}>
                  ₹{(item.product.price * item.quantity).toFixed(2)}
                </div>
              </div>
            ))}
          </div>
          
          <div style={{ borderTop: '1px solid rgba(255,255,255,0.05)', marginTop: '16px', paddingTop: '16px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: 'var(--text-muted)' }}>
              <span>Subtotal</span>
              <span>₹{subtotal.toFixed(2)}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: 'var(--text-muted)' }}>
              <span>Boutique Tax (8%)</span>
              <span>₹{tax.toFixed(2)}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: 'var(--text-muted)' }}>
              <span>Delivery Fee</span>
              <span>{shipping === 0 ? 'FREE' : `₹{shipping.toFixed(2)}`}</span>
            </div>
          </div>
        </div>

        {/* Right Panel: Gateway Input Forms */}
        <div className="payment-right-panel">
          
          {/* Close Button */}
          {paymentState === 'idle' && (
            <button 
              type="button"
              onClick={onCancel}
              style={{ position: 'absolute', top: '16px', right: '16px', background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
            >
              <X size={20} />
            </button>
          )}

          {/* Tab Selection */}
          <div className="payment-tab-bar">
            <button 
              type="button"
              className={`payment-tab-btn ${activeTab === 'card' ? 'active' : ''}`}
              onClick={() => { setActiveTab('card'); setErrors({}); }}
              disabled={paymentState !== 'idle'}
            >
              <CreditCard size={16} />
              <span>Card</span>
            </button>
            <button 
              type="button"
              className={`payment-tab-btn ${activeTab === 'upi' ? 'active' : ''}`}
              onClick={() => { setActiveTab('upi'); setErrors({}); }}
              disabled={paymentState !== 'idle'}
            >
              <Smartphone size={16} />
              <span>UPI</span>
            </button>
            <button 
              type="button"
              className={`payment-tab-btn ${activeTab === 'netbanking' ? 'active' : ''}`}
              onClick={() => { setActiveTab('netbanking'); setErrors({}); }}
              disabled={paymentState !== 'idle'}
            >
              <ShieldCheck size={16} />
              <span>Net Banking</span>
            </button>
          </div>

          {/* Form Handling */}
          <form onSubmit={handlePay} style={{ display: 'flex', flexDirection: 'column', flexGrow: 1, justifyContent: 'space-between' }}>
            
            {/* Card Payment Section */}
            {activeTab === 'card' && (
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <div className="payment-input-group">
                  <label>Card Number</label>
                  <input 
                    type="text" 
                    placeholder="4111 2222 3333 4444"
                    value={cardForm.number}
                    onChange={handleCardNumberChange}
                    style={{ border: errors.number ? '1px solid #ef4444' : '' }}
                  />
                  {errors.number && <span style={{ fontSize: '11px', color: '#ef4444', display: 'flex', alignItems: 'center', gap: '4px' }}><AlertCircle size={10} /> {errors.number}</span>}
                </div>

                <div className="payment-input-group">
                  <label>Cardholder Name</label>
                  <input 
                    type="text" 
                    placeholder="Sarah Jenkins"
                    value={cardForm.name}
                    onChange={handleNameChange}
                    style={{ border: errors.name ? '1px solid #ef4444' : '' }}
                  />
                  {errors.name && <span style={{ fontSize: '11px', color: '#ef4444', display: 'flex', alignItems: 'center', gap: '4px' }}><AlertCircle size={10} /> {errors.name}</span>}
                </div>

                <div className="payment-row-grid">
                  <div className="payment-input-group">
                    <label>Expiration Date</label>
                    <input 
                      type="text" 
                      placeholder="MM/YY"
                      value={cardForm.expiry}
                      onChange={handleExpiryChange}
                      style={{ border: errors.expiry ? '1px solid #ef4444' : '' }}
                    />
                    {errors.expiry && <span style={{ fontSize: '11px', color: '#ef4444', display: 'flex', alignItems: 'center', gap: '4px' }}><AlertCircle size={10} /> {errors.expiry}</span>}
                  </div>
                  
                  <div className="payment-input-group">
                    <label>CVV</label>
                    <input 
                      type="password" 
                      placeholder="•••"
                      value={cardForm.cvv}
                      onChange={handleCvvChange}
                      style={{ border: errors.cvv ? '1px solid #ef4444' : '' }}
                    />
                    {errors.cvv && <span style={{ fontSize: '11px', color: '#ef4444', display: 'flex', alignItems: 'center', gap: '4px' }}><AlertCircle size={10} /> {errors.cvv}</span>}
                  </div>
                </div>
              </div>
            )}

            {/* UPI Payment Section */}
            {activeTab === 'upi' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <div className="payment-input-group">
                  <label>UPI ID</label>
                  <input 
                    type="text" 
                    placeholder="sarah@okaxis"
                    value={upiId}
                    onChange={(e) => { setUpiId(e.target.value); setErrors({}); }}
                    style={{ border: errors.upiId ? '1px solid #ef4444' : '' }}
                  />
                  {errors.upiId && <span style={{ fontSize: '11px', color: '#ef4444', display: 'flex', alignItems: 'center', gap: '4px' }}><AlertCircle size={10} /> {errors.upiId}</span>}
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '16px', background: 'rgba(255,255,255,0.02)', padding: '16px', borderRadius: '10px', border: '1px solid rgba(255,255,255,0.04)' }}>
                  {/* QR Code Scanner Visual Simulation */}
                  <div style={{ width: '80px', height: '80px', background: '#fff', padding: '6px', borderRadius: '6px', display: 'flex', alignItems: 'center', justifyItems: 'center' }}>
                    <svg viewBox="0 0 100 100" style={{ width: '100%', height: '100%', fill: '#000' }}>
                      <path d="M5,5 h30 v30 h-30 z M15,15 h10 v10 h-10 z M65,5 h30 v30 h-30 z M75,15 h10 v10 h-10 z M5,65 h30 v30 h-30 z M15,75 h10 v10 h-10 z M45,15 h10 v10 h-10 z M45,45 h10 v10 h-10 z M15,45 h10 v10 h-10 z M75,45 h10 v10 h-10 z M45,75 h10 v10 h-10 z M65,65 h10 v10 h-10 z M85,75 h10 v10 h-10 z" />
                    </svg>
                  </div>
                  <div style={{ textAlign: 'left' }}>
                    <h5 style={{ margin: '0 0 4px 0', fontSize: '13px', color: 'var(--text-primary)', fontWeight: '700' }}>Scan & Pay Instant QR</h5>
                    <p style={{ margin: 0, fontSize: '11px', color: 'var(--text-muted)' }}>Scan using BHIM, Google Pay, PhonePe, or PayTM apps to complete fast payment.</p>
                  </div>
                </div>
              </div>
            )}

            {/* Net Banking Section */}
            {activeTab === 'netbanking' && (
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <div className="payment-input-group">
                  <label>Select Bank</label>
                  <select 
                    value={selectedBank} 
                    onChange={(e) => setSelectedBank(e.target.value)}
                  >
                    <option value="HDFC Bank">HDFC Bank</option>
                    <option value="State Bank of India">State Bank of India (SBI)</option>
                    <option value="ICICI Bank">ICICI Bank</option>
                    <option value="Axis Bank">Axis Bank</option>
                    <option value="Kotak Mahindra Bank">Kotak Mahindra Bank</option>
                  </select>
                </div>
              </div>
            )}

            <button type="submit" className="payment-submit-btn" disabled={paymentState !== 'idle'}>
              <span>Pay ₹{total.toFixed(2)} Securely</span>
            </button>
          </form>

          {/* Loader Processing Overlay */}
          {paymentState === 'processing' && (
            <div className="payment-processing-overlay">
              <div className="payment-spinner"></div>
              <h4 style={{ margin: 0, fontSize: '15px', color: '#fff', fontWeight: '700' }}>Authorizing Gateway Transaction...</h4>
              <p style={{ margin: 0, fontSize: '12px', color: 'var(--text-muted)' }}>Connecting to bank secure servers. Do not close this window.</p>
            </div>
          )}

          {/* Success Overlay */}
          {paymentState === 'success' && (
            <div className="payment-processing-overlay">
              <div className="payment-success-badge">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              </div>
              <h4 style={{ margin: 0, fontSize: '16px', color: '#22c55e', fontWeight: '800' }}>Payment Complete!</h4>
              <p style={{ margin: 0, fontSize: '12px', color: 'var(--text-muted)' }}>Transaction ID: txn_{Math.floor(Math.random() * 90000000)}</p>
            </div>
          )}

        </div>

      </div>
    </div>
  );
}
