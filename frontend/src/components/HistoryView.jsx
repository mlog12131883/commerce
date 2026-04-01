import React, { useState } from 'react';
import { Filter, ChevronLeft, ChevronRight } from 'lucide-react';

export default function HistoryView({ orderHistory, claimedOrders, cancelClaimRequest, prepareClaim }) {
  const [filter, setFilter] = useState('ALL'); // ALL, COMPLETED, CLAIMED
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;

  // Enhance orders with claim status
  const enhancedOrders = orderHistory.map(ord => ({
    ...ord,
    isClaimed: !!claimedOrders[ord.orderId]
  }));

  // Filtering
  const filteredOrders = enhancedOrders.filter(ord => {
    if (filter === 'COMPLETED') return !ord.isClaimed;
    if (filter === 'CLAIMED') return ord.isClaimed;
    return true;
  });

  // Pagination
  const totalPages = Math.ceil(filteredOrders.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const currentOrders = filteredOrders.slice(startIndex, startIndex + itemsPerPage);

  const handleFilterChange = (newFilter) => {
    setFilter(newFilter);
    setCurrentPage(1); // Reset to first page
  };

  // Helper for Short Order ID
  const getShortId = (id) => id.length > 8 ? `...${id.slice(-8)}` : id;

  return (
    <div className="view">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '2.5rem', margin: 0 }}>주문 내역</h2>
        
        {/* Filter UI */}
        <div style={{ display: 'flex', gap: '0.5rem', background: 'var(--surface)', padding: '0.4rem', borderRadius: '12px', border: '1px solid var(--border)' }}>
          {['ALL', 'COMPLETED', 'CLAIMED'].map((f) => (
            <button 
              key={f}
              onClick={() => handleFilterChange(f)}
              style={{
                padding: '0.5rem 1rem',
                borderRadius: '8px',
                border: 'none',
                fontSize: '0.85rem',
                fontWeight: 600,
                cursor: 'pointer',
                background: filter === f ? 'var(--accent)' : 'transparent',
                color: filter === f ? '#fff' : 'var(--text-dim)',
                transition: 'all 0.2s'
              }}
            >
              {f === 'ALL' ? '전체' : f === 'COMPLETED' ? '구매 확정' : '반품/취소'}
            </button>
          ))}
        </div>
      </div>

      {currentOrders.length === 0 ? (
        <p style={{ textAlign: 'center', color: 'var(--text-dim)', padding: '4rem 0' }}>조건에 맞는 주문 내역이 없습니다.</p>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {currentOrders.map((ord) => (
            <div key={ord.orderId} className="list-card glass" style={{ flexDirection: 'column', alignItems: 'stretch', gap: '1rem', borderLeft: ord.isClaimed ? '4px solid var(--error)' : '1px solid var(--border)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.2rem' }}>
                    <span style={{ fontWeight: 800, color: 'var(--text)', fontSize: '1rem' }}>주문번호 {getShortId(ord.orderId)}</span>
                    <span style={{ fontSize: '0.75rem', padding: '2px 6px', borderRadius: '4px', background: ord.isClaimed ? 'rgba(239, 64, 64, 0.1)' : 'rgba(16, 185, 129, 0.1)', color: ord.isClaimed ? 'var(--error)' : 'var(--success)' }}>
                      {ord.isClaimed ? '반품 처리중' : '배송 완료'}
                    </span>
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>결제일시: {ord.createdAt}</div>
                </div>
                
                {ord.isClaimed ? (
                  <button className="btn btn-outline btn-sm" onClick={() => cancelClaimRequest(ord.orderId)} style={{ borderColor: 'var(--error)', color: 'var(--error)' }}>
                    반품 취소
                  </button>
                ) : (
                  <button className="btn btn-outline btn-sm" onClick={() => prepareClaim(ord)}>반품 신청</button>
                )}
              </div>
              
              <div style={{ borderTop: '1px solid var(--border)', paddingTop: '1rem' }}>
                {ord.items.map((it, i) => (
                  <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.95rem' }}>
                    <span>{it.productId} × {it.quantity}</span>
                    <span style={{ color: 'var(--text-dim)' }}>₩{it.price.toLocaleString()}</span>
                  </div>
                ))}
              </div>
              
              <div style={{ textAlign: 'right', fontWeight: 800, fontSize: '1.2rem', color: 'var(--accent)', paddingTop: '0.5rem' }}>
                Total: ₩{ord.totalAmount.toLocaleString()}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '1rem', marginTop: '3rem' }}>
          <button 
            disabled={currentPage === 1}
            onClick={() => setCurrentPage(prev => prev - 1)}
            style={{ display: 'flex', alignItems: 'center', background: 'transparent', border: '1px solid var(--border)', color: currentPage === 1 ? 'var(--border)' : 'var(--text)', cursor: 'pointer', padding: '0.5rem' }}
          >
            <ChevronLeft size={20} />
          </button>
          
          <span style={{ fontWeight: 600, color: 'var(--text-dim)' }}>
            <span style={{ color: 'var(--text)' }}>{currentPage}</span> / {totalPages}
          </span>
          
          <button 
            disabled={currentPage === totalPages}
            onClick={() => setCurrentPage(prev => prev + 1)}
            style={{ display: 'flex', alignItems: 'center', background: 'transparent', border: '1px solid var(--border)', color: currentPage === totalPages ? 'var(--border)' : 'var(--text)', cursor: 'pointer', padding: '0.5rem' }}
          >
            <ChevronRight size={20} />
          </button>
        </div>
      )}
    </div>
  );
}
