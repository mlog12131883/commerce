import React, { useState, useEffect } from 'react';
import { 
  ShoppingBag, 
  History, 
  Store, 
  Clock, 
  CreditCard, 
  Wallet, 
  Star, 
  Trash2, 
  RotateCcw, 
  CheckCircle2, 
  ChevronRight,
  ArrowLeft,
  Loader2,
  Gem,
  AlertCircle
} from 'lucide-react';

const API_BASE = '/api';
const USER_ID = 'user-prime-07';

// --- Components ---

const Badge = ({ children, variant = 'success' }) => (
  <span className={`badge badge-${variant}`}>{children}</span>
);

const Loader = () => <Loader2 className="loader" />;

const CircleDot = ({ color, size }) => (
  <div style={{ width: size, height: size, borderRadius: '50%', backgroundColor: color }} />
);

const PAYMENT_METHODS = [
  { id: 'CREDIT_CARD', name: 'Credit Card', icon: <CreditCard size={18} />, partial: true },
  { id: 'DEBIT_CARD', name: 'Debit Card', icon: <Wallet size={18} />, partial: false },
  { id: 'KAKAO_PAY', name: 'Kakao Pay', icon: <CircleDot size={18} color="#FFD700" />, partial: true },
  { id: 'NAVER_PAY', name: 'Naver Pay', icon: <CircleDot size={18} color="#03C75A" />, partial: true },
  { id: 'POINT', name: 'Member Points', icon: <Star size={18} />, balance: 50000, partial: true }
];

// --- Main App ---

export default function App() {
  const [view, setView] = useState('productView');
  const [product, setProduct] = useState(null);
  const [cart, setCart] = useState({ items: [], totalAmount: 0 });
  const [orderSheet, setOrderSheet] = useState(null);
  const [allocations, setAllocations] = useState({});
  const [orderHistory, setOrderHistory] = useState([]);
  const [claimOrder, setClaimOrder] = useState(null);
  const [claimItems, setClaimItems] = useState([]);
  const [claimReason, setClaimReason] = useState('');
  const [loading, setLoading] = useState({});
  const [lastOrderId, setLastOrderId] = useState(null);
  const [successPayments, setSuccessPayments] = useState([]);

  // --- API Helpers ---

  const fetchAPI = async (path, config = {}) => {
    try {
      const res = await fetch(API_BASE + path, {
        headers: { 'Content-Type': 'application/json' },
        ...config
      });
      if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'API Error');
      }
      return await res.json();
    } catch (e) {
      alert('Error: ' + e.message);
      throw e;
    }
  };

  // --- Initial Load ---

  useEffect(() => {
    loadProduct();
    updateCartCount();
  }, []);

  const loadProduct = async () => {
    try {
      const data = await fetchAPI('/products/PROD-APEX-001');
      setProduct(data);
    } catch (e) {
      console.error('Failed to load product');
    }
  };

  const updateCartCount = async () => {
    try {
      const data = await fetchAPI(`/cart/${USER_ID}`);
      setCart(data);
    } catch (e) {}
  };

  // --- Actions ---

  const addToCart = async () => {
    const qty = parseInt(document.getElementById('pQty')?.value || 1);
    setLoading({ ...loading, adding: true });
    try {
      const updatedCart = await fetchAPI(`/cart/${USER_ID}/items`, {
        method: 'POST',
        body: JSON.stringify({ productId: product.productId, quantity: qty })
      });
      setCart(updatedCart);
      alert(`${qty} items added to cart! ✨`);
    } finally {
      setLoading({ ...loading, adding: false });
    }
  };

  const buyNow = async () => {
    const qty = parseInt(document.getElementById('pQty')?.value || 1);
    setLoading({ ...loading, buyingInstantly: true });
    try {
      const updatedCart = await fetchAPI(`/cart/${USER_ID}/items`, {
        method: 'POST',
        body: JSON.stringify({ productId: product.productId, quantity: qty })
      });
      setCart(updatedCart);
      // After adding to cart, immediately go to checkout
      await goToCheckout();
    } finally {
      setLoading({ ...loading, buyingInstantly: false });
    }
  };

  const loadCart = async () => {
    setLoading({ ...loading, main: true });
    try {
      const data = await fetchAPI(`/cart/${USER_ID}`);
      setCart(data);
      setView('cartView');
    } finally {
      setLoading({ ...loading, main: false });
    }
  };

  const goToCheckout = async () => {
    setLoading({ ...loading, main: true });
    try {
      const sheet = await fetchAPI(`/checkout/${USER_ID}/sheet`);
      setOrderSheet(sheet);
      
      const initialAlloc = {};
      PAYMENT_METHODS.forEach(m => initialAlloc[m.id] = 0);
      initialAlloc['CREDIT_CARD'] = sheet.finalAmount;
      setAllocations(initialAlloc);
      
      setView('checkoutView');
    } finally {
      setLoading({ ...loading, main: false });
    }
  };

  const updateAllocation = (id, val) => {
    let amount = parseFloat(val) || 0;
    // Round to nearest 1000 as requested by user
    amount = Math.round(amount / 1000) * 1000;
    setAllocations({ ...allocations, [id]: amount });
  };

  const fillRemaining = (id) => {
    const allocated = Object.keys(allocations).reduce((sum, key) => key === id ? sum : sum + allocations[key], 0);
    const remaining = Math.max(0, orderSheet.finalAmount - allocated);
    setAllocations({ ...allocations, [id]: remaining });
  };

  const useMaxPoints = (id, balance) => {
    const allocated = Object.keys(allocations).reduce((sum, key) => key === id ? sum : sum + allocations[key], 0);
    const remaining = Math.max(0, orderSheet.finalAmount - allocated);
    const toUse = Math.min(balance, remaining);
    setAllocations({ ...allocations, [id]: toUse });
  };

  const currentAllocated = Object.values(allocations).reduce((a, b) => a + b, 0);
  const isPayReady = orderSheet && currentAllocated === orderSheet.finalAmount;

  const processPayment = async () => {
    setLoading({ ...loading, paying: true });
    try {
      const payments = Object.entries(allocations)
        .filter(([_, amt]) => amt > 0)
        .map(([method, amount]) => ({ method, amount }));
        
      const res = await fetchAPI(`/checkout/${USER_ID}/pay`, {
        method: 'POST',
        body: JSON.stringify({ payments })
      });
      
      setLastOrderId(res.orderId);
      setSuccessPayments(payments);
      updateCartCount();
      setView('successView');
    } finally {
      setLoading({ ...loading, paying: false });
    }
  };

  const loadHistory = async () => {
    setLoading({ ...loading, main: true });
    try {
      const data = await fetchAPI(`/orders/user/${USER_ID}`);
      // Show latest orders at the top
      setOrderHistory([...data].reverse());
      setView('historyView');
    } finally {
      setLoading({ ...loading, main: false });
    }
  };

  const prepareClaim = (order) => {
    setClaimOrder(order);
    setClaimItems(order.items.map(it => ({ ...it, claimQty: 1 })));
    setClaimReason('');
    setView('claimView');
  };

  const submitClaim = async () => {
    if (!claimReason) return alert('Please enter a reason');
    setLoading({ ...loading, claiming: true });
    try {
      await fetchAPI(`/claims/${claimOrder.orderId}/refund`, {
        method: 'POST',
        body: JSON.stringify({
          cancelItems: claimItems.map(it => ({ productId: it.productId, quantity: it.claimQty })),
          reason: claimReason
        })
      });
      alert('Claim submitted successfully.');
      loadHistory();
    } finally {
      setLoading({ ...loading, claiming: false });
    }
  };

  // --- Views ---

  return (
    <div className="app">
      <header className="glass">
        <div className="logo" onClick={() => setView('productView')}>SleekCommerce</div>
        <nav>
          <button onClick={() => setView('productView')} className={view === 'productView' ? 'active' : ''}>상점</button>
          <button onClick={loadCart} className={view === 'cartView' ? 'active' : ''}>
            장바구니 ({cart.items.length})
          </button>
          <button onClick={loadHistory} className={view === 'historyView' ? 'active' : ''}>내 주문</button>
        </nav>
      </header>

      <div className="container">
        
        {view === 'productView' && product && (
          <div className="view">
            <div className="product-hero">
              <div className="product-image">⌚</div>
              <div className="product-info">
                <Badge>Limited Edition</Badge>
                <h1>{product.name}</h1>
                <div className="price">₩{product.price.toLocaleString()}</div>
                <p className="desc">{product.description}</p>
                
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2rem' }}>
                  <label style={{ color: 'var(--text-dim)', fontWeight: 600 }}>Quantity:</label>
                  <input type="number" id="pQty" defaultValue="1" min="1" max="10" 
                    style={{ width: '100px', background: 'var(--surface)', border: '2px solid var(--border)', color: '#fff', padding: '0.8rem 1.5rem 0.8rem 0.8rem', borderRadius: 'var(--radius-md)', fontWeight: 700, outline: 'none', textAlign: 'center' }} 
                  />
                </div>

                <div style={{ display: 'flex', gap: '1rem' }}>
                  <button className="btn btn-outline" onClick={addToCart} disabled={loading.adding}>
                    <span>장바구니 담기</span>
                    {loading.adding && <Loader />}
                  </button>
                  <button className="btn btn-primary" onClick={buyNow} disabled={loading.buyingInstantly}>
                    <span>바로 구매</span>
                    {loading.buyingInstantly && <Loader />}
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {view === 'cartView' && (
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
        )}

        {view === 'checkoutView' && orderSheet && (
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
        )}

        {view === 'successView' && (
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
        )}

        {view === 'historyView' && (
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
                    <button className="btn btn-outline btn-sm" onClick={() => prepareClaim(ord)}>반품 신청</button>
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
        )}

        {view === 'claimView' && claimOrder && (
          <div className="view">
             <h2 style={{ fontSize: '2.5rem', marginBottom: '1rem' }}>Request Return</h2>
             <p style={{ color: 'var(--text-dim)', marginBottom: '3rem' }}>Select items to return and provide a reason.</p>
             
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
                <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-dim)' }}>Reason for Return</label>
                <textarea value={claimReason} onChange={(e) => setClaimReason(e.target.value)}
                  style={{ width: '100%', background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: '12px', padding: '1rem', color: '#fff', height: '120px', fontFamily: 'inherit', fontSize: '1rem', outline: 'none' }}
                  placeholder="Tell us why..."
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
        )}

      </div>
    </div>
  );
}
