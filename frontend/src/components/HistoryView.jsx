import React, { useState } from 'react';
import { Filter, ChevronLeft, ChevronRight } from 'lucide-react';
import { getProductIcon } from './Shared';

export default function HistoryView({ orderHistory, claimedOrders, cancelClaimRequest, prepareClaim, confirmCollectionSimulation }) {
  const [filter, setFilter] = useState('ALL'); // ALL, COMPLETED, CLAIMED
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;

  // Filtering
  const filteredOrders = orderHistory.filter(ord => {
    if (filter === 'COMPLETED') return ord.status === 'PAYMENT_FINISHED';
    if (filter === 'CLAIMED') return ord.status !== 'PAYMENT_FINISHED';
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
              {f === 'ALL' ? '전체' : f === 'COMPLETED' ? '배송 완료' : '반품/취소'}
            </button>
          ))}
        </div>
      </div>

      {currentOrders.length === 0 ? (
        <p style={{ textAlign: 'center', color: 'var(--text-dim)', padding: '4rem 0' }}>조건에 맞는 주문 내역이 없습니다.</p>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {currentOrders.map((ord) => (
            <div key={ord.orderId} className="list-card glass" style={{ flexDirection: 'column', alignItems: 'stretch', gap: '1.2rem', padding: '1.8rem', position: 'relative', overflow: 'hidden' }}>
              {/* Top Accent Decoration */}
              <div style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '4px', background: ord.status !== 'PAYMENT_FINISHED' ? 'var(--error)' : 'var(--accent)', opacity: 0.8 }} />
              
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.2rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                    <span style={{ fontWeight: 800, color: 'var(--text)', fontSize: '1.1rem' }}>Order #{getShortId(ord.orderId)}</span>
                    <span style={{ 
                      fontSize: '0.7rem', 
                      padding: '3px 8px', 
                      borderRadius: '99px', 
                      background: ord.status === 'CANCELED' ? 'rgba(239, 68, 68, 0.15)' : 
                                 ord.status === 'COLLECTING' || ord.status === 'RETURN_PENDING' ? 'rgba(245, 158, 11, 0.15)' :
                                 'rgba(16, 185, 129, 0.15)', 
                      color: ord.status === 'CANCELED' ? 'var(--error)' : 
                             ord.status === 'COLLECTING' || ord.status === 'RETURN_PENDING' ? 'var(--warning)' : 
                             'var(--success)',
                      fontWeight: 800,
                      textTransform: 'uppercase'
                    }}>
                      {ord.status === 'CANCELED' ? '취소 완료' : 
                       ord.status === 'COLLECTING' ? '회수 중' : 
                       ord.status === 'RETURN_PENDING' ? '반품 진행 중' : 
                       ord.status === 'RETURN_CONFIRMED' ? '회수 확정' : '결제 완료'}
                    </span>
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>
                    {new Date(ord.createdAt).toLocaleDateString()} · {new Date(ord.createdAt).toLocaleTimeString()}
                  </div>
                </div>
                
                {(ord.status === 'COLLECTING' || ord.status === 'RETURN_PENDING') ? (
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button className="btn btn-primary btn-sm" onClick={() => confirmCollectionSimulation(ord.orderId)} style={{ background: 'var(--accent)' }}>
                      회수 확정 시뮬레이션
                    </button>
                    <button className="btn btn-outline btn-sm" onClick={() => cancelClaimRequest(ord.orderId)} style={{ borderColor: 'var(--error)', color: 'var(--error)' }}>
                      반품 취소
                    </button>
                  </div>
                ) : ord.status === 'PAYMENT_FINISHED' && (
                  <button className="btn btn-outline btn-sm" onClick={() => prepareClaim(ord)}>반품 신청</button>
                )}
              </div>
              
              {/* Item Grouping Container */}
              <div style={{ background: 'rgba(255,255,255,0.03)', borderRadius: 'var(--radius-md)', padding: '1rem', border: '1px solid var(--border)' }}>
                {ord.items.map((it, i) => (
                  <div key={i} style={{ 
                    display: 'flex', 
                    justifyContent: 'space-between', 
                    alignItems: 'center', 
                    padding: i > 0 ? '1rem 0 0' : '0 0',
                    borderTop: i > 0 ? '1px solid var(--border)' : 'none'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.8rem' }}>
                      <div style={{ width: '40px', height: '40px', background: 'var(--surface)', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.2rem' }}>
                        {getProductIcon(it.productName, it.productId)}
                      </div>
                      <div>
                        <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>{it.productName || it.productId}</div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                          수량: {it.quantity}개 | 옵션: <span style={{ color: 'var(--text)', fontWeight: 600 }}>{it.selectedOption || '기본'}</span>
                        </div>
                      </div>
                    </div>
                    <div style={{ fontWeight: 700, color: 'var(--text)' }}>
                      ₩{(it.price * it.quantity).toLocaleString()}
                    </div>
                  </div>
                ))}
              </div>
              
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', paddingTop: '0.5rem' }}>
                <div style={{ color: 'var(--text-dim)', fontSize: '0.85rem' }}>총 주문 상품: {ord.items.length}건</div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-dim)', marginBottom: '0.1rem' }}>Total Amount</div>
                  <div style={{ fontWeight: 900, fontSize: '1.6rem', color: 'var(--accent)', letterSpacing: '-0.03em' }}>
                    ₩{ord.totalAmount.toLocaleString()}
                  </div>
                </div>
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
