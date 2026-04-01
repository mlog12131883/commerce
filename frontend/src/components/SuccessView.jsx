import React from 'react';
import { CheckCircle2 } from 'lucide-react';
import { PAYMENT_METHODS } from './Shared';

export default function SuccessView({ lastOrderId, successPayments, loadHistory }) {
  return (
    <div className="view status-screen">
      <CheckCircle2 size={80} color="var(--success)" style={{ margin: '0 auto 2rem' }} />
      <h1>Payment Successful!</h1>
      <p style={{ color: 'var(--text-dim)', marginTop: '1rem', marginBottom: '3rem' }}>
        Your order <strong style={{ color: 'var(--text)' }}>#{lastOrderId}</strong> has been placed.
      </p>
      
      <div className="glass" style={{ textAlign: 'left', maxWidth: '400px', margin: '0 auto 3rem', padding: '1.5rem', borderRadius: '12px' }}>
        <h4 style={{ marginBottom: '1rem' }}>Payment Breakdown</h4>
        {successPayments.map((p, idx) => {
          const info = PAYMENT_METHODS.find(m => m.id === p.method);
          return (
            <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', fontSize: '0.9rem' }}>
              <span>{info.icon} {info.name}</span>
              <span style={{ fontWeight: 700 }}>₩{p.amount.toLocaleString()}</span>
            </div>
          );
        })}
      </div>

      <button className="btn btn-primary" onClick={loadHistory}>주문 내역 보기</button>
    </div>
  );
}
