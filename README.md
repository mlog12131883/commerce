# Commerce Payment & Claim System

## Project Overview
This project implements a robust **Complex Payment & Claim System** enabling:
- **Atomic Processing:** Handling composite payments (Credit Card + Points).
- **Waterfall Refunds:** Priority-based deduction logic.
- **Re-approval Strategy:** Automated Void & Re-auth for non-cancellable payments.

## Problem Solving: Legacy Refactoring & Domain Modeling

### The Challenge
The legacy system suffered from:
- **Obscure Naming:** Variables like `pay_due_amt`, `poss_yn` made code unreadable and error-prone.
- **Fragmented Logic:** Business rules for refunds were scattered across JSP and Controller layers.
- **Rigidity:** Adding new payment methods (e.g., Coupons with specific refund rules) required invasive changes.

### The Solution
We refactored the legacy domain using **Domain-Driven Design (DDD)** principles:

1.  **Ubiquitous Language:**
    -   Renamed `pay_due_amt` -> `repaymentAmount` (Clear intent).
    -   Renamed `poss_yn` -> `isPartialCancelable` (Boolean capability).
    -   Renamed `pay_fin_yn` -> `isSettled`.

2.  **Strategy Pattern for Re-approval:**
    -   Encapsulated the complexity of "Void vs. Partial Cancel" into the `ClaimService`.
    -   `PaymentMethod` enum defines the `isPartialCancelable` policy, driving the strategy selection dynamically.

3.  **Waterfall Refund Algorithm:**
    -   Implemented a centralized `ClaimService` that iterates through payments based on priority (Points First -> Cash/Card Second).
    -   Ensures financial integrity by calculating `repaymentAmount` server-side, preventing frontend manipulation.

### Result
- **Readability:** Code now reads like English specifications.
- **Extensibility:** New payment methods can be added by simply defining their `PaymentMethod` properties.
- **Reliability:** Server-side calculations and atomic transactions guarantee data consistency.
