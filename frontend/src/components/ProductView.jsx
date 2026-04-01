import React from 'react';
import { Badge, Loader } from './Shared';

export default function ProductView({ product, addToCart, buyNow, loading }) {
  if (!product) return null;
  return (
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
  );
}
