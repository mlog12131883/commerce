import React from 'react';
import { Loader } from './Shared';

export default function ClaimView({
  claimOrder, claimItems, setClaimItems,
  claimReasonType, setClaimReasonType,
  claimReason, setClaimReason,
  submitClaim, setView, loading
}) {
  if (!claimOrder) return null;
  return (
    <div className="view">
      <h2 style={{ fontSize: '2.5rem', marginBottom: '1rem' }}>반품 신청</h2>
      <p style={{ color: 'var(--text-dim)', marginBottom: '3rem' }}>반품할 상품과 사유를 선택해주세요.</p>
      
      <div style={{ marginBottom: '2rem', background: 'var(--card)', padding: '1rem', borderRadius: '12px' }}>
        Order #{claimOrder.orderId} - Total ₩{claimOrder.totalAmount.toLocaleString()}
      </div>

      {claimItems.map((it, idx) => (
        <div key={idx} className="list-card glass">
          <div>
            <div style={{ fontWeight: 700 }}>{it.productId}</div>
            <div style={{ color: 'var(--text-dim)' }}>Price: ₩{it.price?.toLocaleString()}</div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <input type="number" defaultValue="1" min="1" max={it.quantity} 
              onChange={(e) => {
                const newItems = [...claimItems];
                newItems[idx].claimQty = parseInt(e.target.value);
                setClaimItems(newItems);
              }}
              style={{ width: '100px', background: 'transparent', border: '1px solid var(--border)', color: '#fff', padding: '0.4rem 2.5rem 0.4rem 0.4rem', borderRadius: '6px', textAlign: 'center' }} 
            />
            <span style={{ color: 'var(--text-dim)', fontSize: '0.8rem' }}> / {it.quantity}</span>
          </div>
        </div>
      ))}

      <div style={{ marginTop: '2rem' }}>
        <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-dim)' }}>반품 사유 선택</label>
        <select value={claimReasonType} onChange={(e) => setClaimReasonType(e.target.value)}
          style={{ width: '100%', background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: '12px', padding: '1rem', color: '#fff', fontSize: '1rem', outline: 'none', marginBottom: '1rem' }}>
          <option value="변심 (고객 부담, 반품비 3,000원 제외)">단순 변심 (고객 부담 / 반품 배송비 3,000원 제외 후 환불)</option>
          <option value="상품 파손 (당사 부담)">상품 파손 (당사 부담 / 전액 환불)</option>
          <option value="오배송 (당사 부담)">오배송 (당사 부담 / 전액 환불)</option>
        </select>

        <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-dim)' }}>상세 사유</label>
        <textarea value={claimReason} onChange={(e) => setClaimReason(e.target.value)}
          style={{ width: '100%', background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: '12px', padding: '1rem', color: '#fff', height: '120px', fontFamily: 'inherit', fontSize: '1rem', outline: 'none' }}
          placeholder="상세 사유를 입력해주세요..."
        />
      </div>

      <div style={{ marginTop: '3rem', textAlign: 'right' }}>
        <button className="btn btn-outline" onClick={() => setView('historyView')} style={{ marginRight: '1rem' }}>취소</button>
        <button className="btn btn-primary" style={{ background: 'var(--error)' }} onClick={submitClaim}>
          <span>반품 신청 확인</span>
          {loading.claiming && <Loader />}
        </button>
      </div>
    </div>
  );
}
