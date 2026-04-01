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
            <div key={idx} className="list-card glass">
              <div>
                <div style={{ fontWeight: 700, fontSize: '1.2rem' }}>{item.productName}</div>
                <div style={{ color: 'var(--text-dim)' }}>Qty: {item.quantity} × ₩{item.price.toLocaleString()}</div>
              </div>
              <div style={{ fontWeight: 800, fontSize: '1.4rem' }}>₩{(item.price * item.quantity).toLocaleString()}</div>
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
