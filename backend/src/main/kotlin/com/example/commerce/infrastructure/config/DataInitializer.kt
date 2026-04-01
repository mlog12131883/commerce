package com.example.commerce.infrastructure.config

import com.example.commerce.application.port.out.ProductPort
import com.example.commerce.domain.model.Product
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal
import java.time.LocalDateTime

@Configuration
class DataInitializer {

    @Bean
    fun initData(productPort: ProductPort) = CommandLineRunner {
        // 1. Apex Chronograph (Watch)
        if (productPort.findById("PROD-APEX-001").isEmpty) {
            productPort.save(
                Product(
                    productId = "PROD-APEX-001",
                    name = "Apex Chronograph",
                    description = "Precision engineered stainless steel casing with a sapphire glass dome. Designed for those who value every second.",
                    price = BigDecimal(159000),
                    options = listOf("Default"),
                    dealStartAt = LocalDateTime.now().minusDays(1),
                    dealEndAt = LocalDateTime.now().plusDays(7)
                )
            )
        }

        // 2. Oversized Essential Hoodie (Clothing)
        if (productPort.findById("PROD-CLOTH-001").isEmpty) {
            productPort.save(
                Product(
                    productId = "PROD-CLOTH-001",
                    name = "Oversized Essential Hoodie",
                    description = "Premium heavyweight cotton fleece with a relaxed fit. Perfect for comfort and style.",
                    price = BigDecimal(89000),
                    options = listOf("S", "M", "L", "XL"),
                    dealStartAt = LocalDateTime.now().minusDays(1),
                    dealEndAt = LocalDateTime.now().plusDays(14)
                )
            )
        }
    }
}
