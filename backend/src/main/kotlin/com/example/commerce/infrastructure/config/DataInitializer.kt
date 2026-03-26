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
        if (productPort.findById("PROD-APEX-001").isEmpty) {
            productPort.save(
                Product(
                    productId = "PROD-APEX-001",
                    name = "Apex Chronograph",
                    description = "Precision engineered stainless steel casing with a sapphire glass dome. Designed for those who value every second.",
                    price = BigDecimal(159000),
                    dealStartAt = LocalDateTime.now().minusDays(1),
                    dealEndAt = LocalDateTime.now().plusDays(7)
                )
            )
        }
    }
}
