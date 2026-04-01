import React from 'react';

export default function HistoryView({ orderHistory, claimedOrders, cancelClaimRequest, prepareClaim }) {
  return (
    <div className="view">
      <h2 style={{ fontSize: '2.5rem', marginBottom: '2rem' }}>My Order History</h2>
      {orderHistory.length === 0 ? (
        <p style={{ textAlign: 'center', color: 'var(--text-dim)' }}>No orders yet.</p>
      ) : (
        orderHistory.map((ord, idx) => (
          <div key={idx} className="list-card glass" style={{ flexDirection: 'column', alignItems: 'stretch', gap: '1rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <div>
                <div style={{ fontWeight: 800, color: 'var(--text-dim)', fontSize: '0.85rem' }}>ORDER ID: {ord.orderId}</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>{ord.createdAt}</div>
              </div>
              {claimedOrders[ord.orderId] ? (
                <button className="btn btn-outline btn-sm" onClick={() => cancelClaimRequest(ord.orderId)} style={{ borderColor: 'var(--error)', color: 'var(--error)' }}>
                  반품 취소
                </button>
              ) : (
                <button className="btn btn-outline btn-sm" onClick={() => prepareClaim(ord)}>반품 신청</button>
              )}
            </div>
            <div style={{ borderTop: '1px solid var(--border)', paddingTop: '1rem' }}>
              {ord.items.map((it, i) => (
                <div key={i} style={{ fontSize: '0.95rem' }}>{it.productId} × {it.quantity}</div>
              ))}
            </div>
            <div style={{ textAlign: 'right', fontWeight: 800, fontSize: '1.2rem', color: 'var(--accent)' }}>
              ₩{ord.totalAmount.toLocaleString()}
            </div>
          </div>
        ))
      )}
    </div>
  );
}
