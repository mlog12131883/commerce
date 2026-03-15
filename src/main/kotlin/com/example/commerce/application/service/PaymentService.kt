package com.example.commerce.application.service

import com.example.commerce.application.command.PaymentRequest
import com.example.commerce.application.port.`in`.PaymentUseCase
import com.example.commerce.application.port.out.OrderPort
import com.example.commerce.application.port.out.PaymentGateway
import com.example.commerce.application.port.out.PaymentPort
import com.example.commerce.domain.model.Payment
import com.example.commerce.domain.PaymentMethod
import com.example.commerce.domain.PaymentStatus
import com.example.commerce.domain.TransactionType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal


@Service
class PaymentService(
    private val orderRepository: OrderPort,
    private val paymentRepository: PaymentPort, // 결과 저장
    private val paymentGateway: PaymentGateway,      // 원격 호출 (PG)
    private val pointService: PointService           // 원격/로컬 서비스 (포인트)
) : PaymentUseCase {

    @Transactional
    override fun processPayment(orderId: String, userId: String, paymentRequests: List<PaymentRequest>) {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found") }

        // 1. 검증
        val requestTotal = paymentRequests.sumOf { it.amount }
        if (requestTotal.compareTo(order.totalAmount) != 0) {
            throw IllegalArgumentException("Payment amount mismatch. Order: ${order.totalAmount}, Request: $requestTotal")
        }

        // 2. 원자적 처리
        try {
            // A. 포인트 차감 (선승인 / 잠금)
            val pointRequest = paymentRequests.find { it.method == PaymentMethod.POINT }
            if (pointRequest != null && pointRequest.amount > BigDecimal.ZERO) {
                pointService.usePoints(userId, pointRequest.amount)
            }

            // B. PG 승인 (주요 결제 수단)
            // 로직: 포인트 이외의 결제 수단 처리
            paymentRequests.filter { it.method != PaymentMethod.POINT }.forEach { req ->
                paymentGateway.approve(req.amount, req.method)
            }

            // C. 엔티티 저장
            paymentRequests.forEach { req ->
                paymentRepository.save(Payment(
                    orderId = orderId,
                    originalAmount = req.amount,
                    repaymentAmount = req.amount, // 초기에는 전체 금액 설정
                    paymentMethod = req.method,
                    transactionType = TransactionType.PAYMENT,
                    isSettled = true,
                    status = PaymentStatus.APPROVED
                ))
            }
            
            // D. 주문 상태 업데이트 (선택 사항, 주문에 상태 필드가 있는 경우)
            // order.status = OrderStatus.PAID 

        } catch (e: Exception) {
            // 실패 처리: 보상 트랜잭션
            // 포인트를 사용했지만 PG 승인이 실패한 경우, 전체를 catch하여 포인트를 복구해야 합니다.
            // Spring @Transactional은 DB를 롤백하지만, PointService가 원격인 경우 별도의 처리가 필요할 수 있습니다.
            // PointService가 로컬 목(Mock)이고 DB에 쓴다면 DB 롤백으로 충분할 수 있습니다.
            // 그러나 API 호출 방식이라면 명시적인 보상 로직이 필요합니다.
            val pointRequest = paymentRequests.find { it.method == PaymentMethod.POINT }
            if (pointRequest != null) {
                // 보상 처리
                println("Payment Failed. Compensating Points...")
                pointService.restorePoints(userId, pointRequest.amount)
            }
            throw e
        }
    }
}
