package com.example.commerce.adapter.`in`.web

import com.example.commerce.application.service.ProductService
import com.example.commerce.domain.model.Product
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService
) {

    /** Product Detail Page View */
    @GetMapping("/{productId}")
    fun getProduct(@PathVariable productId: String): ResponseEntity<ProductResponse> {
        val product = productService.getProduct(productId)
        return ResponseEntity.ok(toResponse(product))
    }

    private fun toResponse(product: Product) = ProductResponse(
        productId = product.productId,
        name = product.name,
        description = product.description,
        price = product.price
    )
}

data class ProductResponse(
    val productId: String,
    val name: String,
    val description: String,
    val price: java.math.BigDecimal
)
