package com.example.commerce.application.service

import com.example.commerce.application.command.OrderCommand
import com.example.commerce.application.command.OrderItemCommand
import com.example.commerce.application.command.PaymentRequest
import com.example.commerce.application.port.`in`.CartUseCase
import com.example.commerce.application.port.`in`.CheckoutUseCase
import com.example.commerce.application.port.`in`.OrderUseCase
import com.example.commerce.application.port.`in`.PaymentUseCase
import com.example.commerce.domain.model.OrderSheet
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CheckoutService(
    private val cartUseCase: CartUseCase,
    private val orderUseCase: OrderUseCase,
    private val paymentUseCase: PaymentUseCase
) : CheckoutUseCase {

    override fun getOrderSheet(userId: String): OrderSheet {
        val cart = cartUseCase.getCart(userId)
        if (cart.items.isEmpty()) throw IllegalStateException("Cart is empty")
        
        return OrderSheet(
            cartId = cart.cartId ?: 0L,
            items = cart.items,
            totalAmount = cart.totalAmount
        )
    }

    @Transactional
    override fun placeOrderFromSheet(userId: String, paymentRequests: List<PaymentRequest>): String {
        val cart = cartUseCase.getCart(userId)
        if (cart.items.isEmpty()) throw IllegalStateException("Cart is empty")

        // 1. Create Order through OrderUseCase
        val orderCommand = OrderCommand(
            items = cart.items.map { 
                OrderItemCommand(
                    productId = it.productId, 
                    productName = it.productName,
                    price = it.price, 
                    quantity = it.quantity,
                    selectedOption = it.selectedOption
                ) 
            },
            deliveryFee = java.math.BigDecimal(3000)
        )
        val orderId = orderUseCase.placeOrder(userId, orderCommand)

        // 2. Process Payment through PaymentUseCase
        paymentUseCase.processPayment(orderId, userId, paymentRequests)

        // 3. Clear Cart
        cartUseCase.clearCart(userId)

        return orderId
    }
}
