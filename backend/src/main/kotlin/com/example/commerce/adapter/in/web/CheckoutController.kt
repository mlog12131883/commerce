package com.example.commerce.adapter.`in`.web

import com.example.commerce.application.command.PaymentRequest
import com.example.commerce.application.port.`in`.CheckoutUseCase
import com.example.commerce.domain.PaymentMethod
import com.example.commerce.domain.model.OrderSheet
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/checkout")
class CheckoutController(
    private val checkoutUseCase: CheckoutUseCase
) {

    /** 1. Get Order Sheet (based on cart) */
    @GetMapping("/{userId}/sheet")
    fun getOrderSheet(@PathVariable userId: String): ResponseEntity<OrderSheetResponse> {
        val sheet = checkoutUseCase.getOrderSheet(userId)
        return ResponseEntity.ok(toResponse(sheet))
    }

    /** 2. Process Final Checkout (Pay) */
    @PostMapping("/{userId}/pay")
    fun pay(
        @PathVariable userId: String,
        @RequestBody req: PayRequest
    ): ResponseEntity<PayResponse> {
        val paymentRequests = req.payments.map { 
            PaymentRequest(method = PaymentMethod.valueOf(it.method), amount = it.amount) 
        }
        val orderId = checkoutUseCase.placeOrderFromSheet(userId, paymentRequests)
        return ResponseEntity.ok(PayResponse(orderId = orderId, message = "Payment processed and order created successfully."))
    }

    private fun toResponse(sheet: OrderSheet) = OrderSheetResponse(
        cartId = sheet.cartId,
        items = sheet.items.map { CartItemDto(it.productId, it.productName, it.price, it.quantity) },
        totalAmount = sheet.totalAmount,
        deliveryFee = sheet.deliveryFee,
        finalAmount = sheet.finalAmount
    )
}

data class OrderSheetResponse(
    val cartId: Long,
    val items: List<CartItemDto>,
    val totalAmount: BigDecimal,
    val deliveryFee: BigDecimal,
    val finalAmount: BigDecimal
)

data class CartItemDto(val productId: String, val productName: String, val price: BigDecimal, val quantity: Int)

data class PayRequest(
    val payments: List<PaymentMethodDto>
)

data class PaymentMethodDto(val method: String, val amount: BigDecimal)

data class PayResponse(val orderId: String, val message: String)
