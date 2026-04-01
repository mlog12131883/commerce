import React from 'react';
import { ShoppingBag } from 'lucide-react';

export default function CartView({ cart, goToCheckout }) {
  return (
    <div className="view">
      <h2 style={{ fontSize: '2.5rem', marginBottom: '2rem' }}>Your Cart</h2>
      {cart.items.length === 0 ? (
        <div style={{ textAlign: 'center', color: 'var(--text-dim)', padding: '4rem 0' }}>
          <ShoppingBag size={64} style={{ marginBottom: '1rem', opacity: 0.2 }} />
          <p>Your cart is empty. Start shopping!</p>
        </div>
      ) : (
        <div>
          {cart.items.map((item, idx) => (
            <div key={idx} className="list-card glass" style={{ 
              marginBottom: '1rem', 
              padding: '1.5rem', 
              borderRadius: 'var(--radius-md)', 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'space-between',
              gap: '1.5rem'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', flex: 1 }}>
                <div style={{ 
                  width: '64px', 
                  height: '64px', 
                  background: 'var(--surface)', 
                  borderRadius: '12px', 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'center', 
                  fontSize: '2rem' 
                }}>
                  {item.productName.toLowerCase().includes('hoodie') ? '👕' : '⌚'}
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ fontWeight: 800, fontSize: '1.25rem', marginBottom: '0.3rem', letterSpacing: '-0.02em' }}>
                    {item.productName}
                  </div>
                  <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', fontSize: '0.9rem' }}>
                    <span style={{ color: 'var(--text-dim)' }}>수량: <strong style={{ color: 'var(--text)' }}>{item.quantity}</strong></span>
                    <span style={{ height: '4px', width: '4px', borderRadius: '50%', background: 'var(--border)' }}></span>
                    <span style={{ color: 'var(--text-dim)' }}>옵션: <strong style={{ color: 'var(--accent)', textTransform: 'uppercase' }}>{item.selectedOption || 'Default'}</strong></span>
                  </div>
                </div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-dim)', marginBottom: '0.2rem' }}>Subtotal</div>
                <div style={{ fontWeight: 900, fontSize: '1.4rem', color: '#fff' }}>
                  ₩{(item.price * item.quantity).toLocaleString()}
                </div>
              </div>
            </div>
          ))}
          <div className="cart-footer">
            <div className="total-price">₩{cart.totalAmount.toLocaleString()}</div>
            <button className="btn btn-primary" onClick={goToCheckout}>결제하기</button>
          </div>
        </div>
      )}
    </div>
  );
}
