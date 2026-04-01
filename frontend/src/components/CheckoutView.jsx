import React from 'react';
import { PAYMENT_METHODS, Loader } from './Shared';

export default function CheckoutView({
  orderSheet, allocations, updateAllocation,
  fillRemaining, useMaxPoints, currentAllocated,
  isPayReady, processPayment, loading
}) {
  if (!orderSheet) return null;
  return (
    <div className="view">
      <h2 style={{ fontSize: '2.5rem', marginBottom: '1rem' }}>Secure Checkout</h2>
      <p style={{ color: 'var(--text-dim)', marginBottom: '3rem' }}>Split your payment across multiple methods if you wish.</p>
      
      <div className="order-summary">
        {orderSheet.items.map((it, idx) => (
          <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border)', padding: '0.8rem 0' }}>
            <span style={{ color: 'var(--text-dim)' }}>{it.productName} × {it.quantity}</span>
            <span style={{ fontWeight: 600 }}>₩{(it.price * it.quantity).toLocaleString()}</span>
          </div>
        ))}
        <div style={{ display: 'flex', justifyContent: 'space-between', paddingTop: '1rem', color: 'var(--text-dim)' }}>
          <span>Products Total</span><span>₩{orderSheet.totalAmount.toLocaleString()}</span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '1rem' }}>
          <span>Delivery Fee</span><span>₩{orderSheet.deliveryFee.toLocaleString()}</span>
        </div>
      </div>

      <div className="glass" style={{ marginTop: '3rem', padding: '2rem', borderRadius: 'var(--radius-lg)' }}>
        <h3 style={{ marginBottom: '1.5rem' }}>Multi-Payment Selection</h3>
        
        {PAYMENT_METHODS.map(m => (
          <div key={m.id} style={{ display: 'grid', gridTemplateColumns: '1fr 140px 100px', gap: '1rem', alignItems: 'center', marginBottom: '1rem', background: 'rgba(255,255,255,0.03)', padding: '1rem', borderRadius: '12px', border: '1px solid var(--border)' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span style={{ display: 'flex' }}>{m.icon}</span>
                <span style={{ fontWeight: 700 }}>{m.name}</span>
                <span className="badge" style={{ background: m.partial ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239, 64, 64, 0.1)', color: m.partial ? 'var(--success)' : 'var(--error)', fontSize: '0.65rem', padding: '2px 6px' }}>
                  {m.partial ? 'Partial OK' : 'No Partial'}
                </span>
              </div>
              {m.id === 'POINT' && <div style={{ fontSize: '0.8rem', color: 'var(--accent)' }}>Bal: ₩{m.balance.toLocaleString()}</div>}
            </div>
            <input type="number" value={allocations[m.id]} onChange={(e) => updateAllocation(m.id, e.target.value)}
              step="1000" min="0"
              style={{ background: 'var(--surface)', border: '2px solid var(--border)', color: '#fff', padding: '0.6rem 2.5rem 0.6rem 0.6rem', borderRadius: '8px', width: '100%', fontFamily: 'inherit', fontWeight: 700, textAlign: 'right', outline: 'none' }} 
            />
            {m.id === 'POINT' ? (
              <button className="btn btn-outline btn-sm" onClick={() => useMaxPoints(m.id, m.balance)}>전액 사용</button>
            ) : (
              <button className="btn btn-outline btn-sm" onClick={() => fillRemaining(m.id)}>남은 금액</button>
            )}
          </div>
        ))}

        <div style={{ marginTop: '2rem', paddingTop: '1.5rem', borderTop: '1px dashed var(--border)', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span style={{ color: 'var(--text-dim)', fontWeight: 600 }}>Total to Pay:</span>
            <span style={{ fontWeight: 700 }}>₩{orderSheet.finalAmount.toLocaleString()}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span style={{ color: 'var(--text-dim)', fontWeight: 600 }}>Allocated:</span>
            <span style={{ fontWeight: 700, color: 'var(--accent)' }}>₩{currentAllocated.toLocaleString()}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span style={{ color: 'var(--text-dim)', fontWeight: 600 }}>Remaining:</span>
            <span style={{ fontWeight: 800, fontSize: '1.2rem', color: isPayReady ? 'var(--success)' : 'var(--error)' }}>
              ₩{Math.max(0, orderSheet.finalAmount - currentAllocated).toLocaleString()}
            </span>
          </div>
        </div>
      </div>

      <div className="checkout-footer">
        <button className="btn btn-primary" onClick={processPayment} disabled={!isPayReady} style={{ width: '100%', justifyContent: 'center' }}>
          <span>결제하기</span>
          {loading.paying && <Loader />}
        </button>
      </div>
    </div>
  );
}
