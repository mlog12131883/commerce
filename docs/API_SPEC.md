# Payment & Claim API Specification

## 1. Claim Request (Refund)

**Endpoint:** `POST /api/claims`

**Description:** Requests a partial or full refund for an order. The system automatically calculates the refund amount and executes the refund strategy (Waterfall & Re-approval).

### Request Body
```json
{
  "orderId": "ORD-20260214-001",
  "reason": "Customer changed mind",
  "cancelItems": [
    {
      "productId": "PROD-123",
      "quantity": 1
    }
  ]
}
```

### Response
```json
{
  "status": "SUCCESS",
  "refundedTotal": 15000,
  "refundDetails": [
    {
      "paymentId": 101,
      "method": "POINT",
      "amount": 5000,
      "status": "CANCELED"
    },
    {
      "paymentId": 102,
      "method": "CREDIT_CARD",
      "amount": 10000,
      "status": "PARTIAL_CANCELED"
    }
  ]
}
```

## 2. Payment Inquiry

**Endpoint:** `GET /api/payments/{orderId}`

**Description:** Retrieves the current status of all payments associated with an order, including repayment amounts.

### Response
```json
[
  {
    "paymentId": 102,
    "method": "CREDIT_CARD",
    "originalAmount": 20000,
    "repaymentAmount": 10000,
    "totalRefundAmount": 10000,
    "status": "PARTIAL_CANCELED",
    "isPartialCancelable": true
  }
]
```
