import React from 'react';
import { Loader2, CreditCard, Wallet, Star } from 'lucide-react';

export const Badge = ({ children, variant = 'success' }) => (
  <span className={`badge badge-${variant}`}>{children}</span>
);

export const Loader = () => <Loader2 className="loader" />;

export const CircleDot = ({ color, size }) => (
  <div style={{ width: size, height: size, borderRadius: '50%', backgroundColor: color }} />
);

export const PAYMENT_METHODS = [
  { id: 'CREDIT_CARD', name: 'Credit Card', icon: <CreditCard size={18} />, partial: true },
  { id: 'DEBIT_CARD', name: 'Debit Card', icon: <Wallet size={18} />, partial: false },
  { id: 'KAKAO_PAY', name: 'Kakao Pay', icon: <CircleDot size={18} color="#FFD700" />, partial: true },
  { id: 'NAVER_PAY', name: 'Naver Pay', icon: <CircleDot size={18} color="#03C75A" />, partial: true },
  { id: 'POINT', name: 'Member Points', icon: <Star size={18} />, balance: 50000, partial: true }
];

export const getProductIcon = (name = '', id = '') => {
  const lowerName = (name || '').toLowerCase();
  const lowerId = (id || '').toLowerCase();
  
  if (lowerName.includes('hoodie') || lowerName.includes('cloth') || lowerId.includes('cloth')) return '👕';
  if (lowerName.includes('watch') || lowerName.includes('apex') || lowerId.includes('apex')) return '⌚';
  
  return '📦'; // Default package icon
};
