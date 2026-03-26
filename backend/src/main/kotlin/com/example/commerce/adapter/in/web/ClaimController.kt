package com.example.commerce.adapter.`in`.web

import com.example.commerce.application.command.CancelItem
import com.example.commerce.application.port.`in`.ClaimUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/claims")
class ClaimController(
    private val claimUseCase: ClaimUseCase
) {

    /** Request Claim (Refund/Cancellation) */
    @PostMapping("/{orderId}/refund")
    fun requestRefund(
        @PathVariable orderId: String,
        @RequestBody req: ClaimRequest
    ): ResponseEntity<MessageResponse> {
        val cancelItems = req.cancelItems.map { CancelItem(it.productId, it.quantity) }
        claimUseCase.requestClaim(orderId, cancelItems, req.reason)
        return ResponseEntity.ok(MessageResponse("Refund request processed successfully."))
    }
}

data class ClaimRequest(
    val cancelItems: List<CancelItemDto>,
    val reason: String
)

data class CancelItemDto(val productId: String, val quantity: Int)

data class MessageResponse(val message: String)
