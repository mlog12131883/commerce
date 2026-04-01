import React, { useState, useEffect } from 'react';
import { Routes, Route, useNavigate, useLocation, Link, Navigate } from 'react-router-dom';
import ProductView from './components/ProductView';
import CartView from './components/CartView';
import CheckoutView from './components/CheckoutView';
import SuccessView from './components/SuccessView';
import HistoryView from './components/HistoryView';
import ClaimView from './components/ClaimView';
import { PAYMENT_METHODS } from './components/Shared';

const API_BASE = '/api';
const USER_ID = 'user-prime-07';

export default function App() {
  const navigate = useNavigate();
  const location = useLocation();
  
  const [product, setProduct] = useState(null);
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState({ items: [], totalAmount: 0 });
  const [orderSheet, setOrderSheet] = useState(null);
  const [allocations, setAllocations] = useState({});
  const [orderHistory, setOrderHistory] = useState([]);
  const [claimOrder, setClaimOrder] = useState(null);
  const [claimItems, setClaimItems] = useState([]);
  const [claimReasonType, setClaimReasonType] = useState('변심 (고객 부담, 반품비 3,000원 제외)');
  const [claimReason, setClaimReason] = useState('');
  const [loading, setLoading] = useState({});
  const [lastOrderId, setLastOrderId] = useState(null);
  const [successPayments, setSuccessPayments] = useState([]);
  const [claimedOrders, setClaimedOrders] = useState(() => JSON.parse(localStorage.getItem('claimedOrders') || '{}'));

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

  useEffect(() => {
    loadProducts();
    updateCartCount();
  }, []);

  const loadProducts = async () => {
    try {
      const data = await fetchAPI('/products');
      setProducts(data);
    } catch (e) {}
  };

  const loadProduct = async (id) => {
    try {
      const data = await fetchAPI(`/products/${id}`);
      setProduct(data);
    } catch (e) {}
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
    const opt = document.getElementById('pOpt')?.value;
    setLoading({ ...loading, adding: true });
    try {
      const updatedCart = await fetchAPI(`/cart/${USER_ID}/items`, {
        method: 'POST',
        body: JSON.stringify({ productId: product.productId, quantity: qty, option: opt })
      });
      setCart(updatedCart);
      alert(`${qty} items (${opt}) added to cart! ✨`);
    } finally {
      setLoading({ ...loading, adding: false });
    }
  };

  const buyNow = async () => {
    const qty = parseInt(document.getElementById('pQty')?.value || 1);
    const opt = document.getElementById('pOpt')?.value;
    setLoading({ ...loading, buyingInstantly: true });
    try {
      const updatedCart = await fetchAPI(`/cart/${USER_ID}/items`, {
        method: 'POST',
        body: JSON.stringify({ productId: product.productId, quantity: qty, option: opt })
      });
      setCart(updatedCart);
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
      navigate('/cart');
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
      
      navigate('/checkout');
    } finally {
      setLoading({ ...loading, main: false });
    }
  };

  const updateAllocation = (id, val) => {
    let amount = parseFloat(val) || 0;
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
      navigate('/success');
    } finally {
      setLoading({ ...loading, paying: false });
    }
  };

  const loadHistory = async () => {
    setLoading({ ...loading, main: true });
    try {
      const data = await fetchAPI(`/orders/user/${USER_ID}`);
      setOrderHistory([...data].reverse());
      navigate('/history');
    } finally {
      setLoading({ ...loading, main: false });
    }
  };

  const prepareClaim = (order) => {
    setClaimOrder(order);
    setClaimItems(order.items.map(it => ({ ...it, claimQty: 1 })));
    setClaimReasonType('변심 (고객 부담, 반품비 3,000원 제외)');
    setClaimReason('');
    navigate('/claim');
  };

  const submitClaim = async () => {
    setLoading({ ...loading, claiming: true });
    try {
      await fetchAPI(`/claims/${claimOrder.orderId}/refund`, {
        method: 'POST',
        body: JSON.stringify({
          cancelItems: claimItems.map(it => ({ productId: it.productId, quantity: it.claimQty })),
          reason: claimReason.trim() ? `[${claimReasonType}] ${claimReason}` : `[${claimReasonType}]`
        })
      });
      alert('반품 신청이 완료되었습니다.');
      const nextClaimed = { ...claimedOrders, [claimOrder.orderId]: true };
      setClaimedOrders(nextClaimed);
      localStorage.setItem('claimedOrders', JSON.stringify(nextClaimed));
      loadHistory();
    } finally {
      setLoading({ ...loading, claiming: false });
    }
  };

  const cancelClaimRequest = (orderId) => {
    if(window.confirm('반품 신청을 취소하시겠습니까?')) {
        const nextClaimed = { ...claimedOrders };
        delete nextClaimed[orderId];
        setClaimedOrders(nextClaimed);
        localStorage.setItem('claimedOrders', JSON.stringify(nextClaimed));
        alert('반품 취소가 완료되었습니다.');
    }
  };

  return (
    <div className="app">
      <header className="glass">
        <Link to="/" className="logo">SleekCommerce</Link>
        <nav>
          <Link to="/" className={location.pathname === '/' ? 'active' : ''}>상점</Link>
          <button onClick={loadCart} className={location.pathname === '/cart' ? 'active' : ''}>
            장바구니 ({cart.items.length})
          </button>
          <button onClick={loadHistory} className={location.pathname === '/history' ? 'active' : ''}>내 주문</button>
        </nav>
      </header>
      <div className="container">
        <Routes>
          <Route path="/" element={
            <div className="view">
              <h2 style={{ fontSize: '2.5rem', marginBottom: '2rem' }}>추천 상품</h2>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '2rem' }}>
                {products.map(p => (
                  <div key={p.productId} className="list-card glass" style={{ flexDirection: 'column', cursor: 'pointer', transition: '0.3s', padding: '1.5rem' }} onClick={() => { setProduct(p); navigate('/product/' + p.productId); }}>
                    <div style={{ fontSize: '4rem', marginBottom: '1rem', textAlign: 'center', background: 'var(--surface)', borderRadius: 'var(--radius-md)', padding: '2rem' }}>
                      {p.name.toLowerCase().includes('hoodie') ? '👕' : '⌚'}
                    </div>
                    <h3 style={{ marginBottom: '0.5rem' }}>{p.name}</h3>
                    <div className="price" style={{ fontSize: '1.3rem', marginBottom: '0.5rem' }}>₩{p.price.toLocaleString()}</div>
                    <p style={{ color: 'var(--text-dim)', fontSize: '0.9rem', height: '3rem', overflow: 'hidden' }}>{p.description}</p>
                  </div>
                ))}
              </div>
            </div>
          } />
          <Route path="/product/:id" element={<ProductView product={product} addToCart={addToCart} buyNow={buyNow} loading={loading} />} />
          <Route path="/cart" element={<CartView cart={cart} goToCheckout={goToCheckout} />} />
          <Route path="/checkout" element={
            orderSheet ? (
              <CheckoutView 
                orderSheet={orderSheet} allocations={allocations} updateAllocation={updateAllocation}
                fillRemaining={fillRemaining} useMaxPoints={useMaxPoints} currentAllocated={currentAllocated}
                isPayReady={isPayReady} processPayment={processPayment} loading={loading}
              />
            ) : <Navigate to="/" />
          } />
          <Route path="/success" element={
            lastOrderId ? (
              <SuccessView lastOrderId={lastOrderId} successPayments={successPayments} loadHistory={loadHistory} />
            ) : <Navigate to="/" />
          } />
          <Route path="/history" element={<HistoryView orderHistory={orderHistory} claimedOrders={claimedOrders} cancelClaimRequest={cancelClaimRequest} prepareClaim={prepareClaim} />} />
          <Route path="/claim" element={
            claimOrder ? (
              <ClaimView 
                claimOrder={claimOrder} claimItems={claimItems} setClaimItems={setClaimItems}
                claimReasonType={claimReasonType} setClaimReasonType={setClaimReasonType} claimReason={claimReason}
                setClaimReason={setClaimReason} submitClaim={submitClaim} setView={() => navigate('/history')} loading={loading}
              />
            ) : <Navigate to="/history" />
          } />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </div>
    </div>
  );
}
