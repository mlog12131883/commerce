# High-Performance E-Commerce Engine (Kotlin/Spring Boot)

이 프로젝트는 대규모 트래픽과 데이터 정합성을 동시에 해결해야 하는 이커머스 환경의 핵심적인 기술적 도전 과제들을 해결하기 위해 설계된 **글로벌 스케일 백엔드 엔진**입니다.

단순한 기능 구현을 넘어, **헥사고날 아키텍처(Hexagonal Architecture)**를 통한 유연한 도메인 모델링, **2계층 캐시 시스템(L1+L2)**을 활용한 조회 성능 극대화, 그리고 **비관적 락(Pessimistic Lock)**을 이용한 원자적 재고 관리 로직을 실제 서비스 수준으로 구현하였습니다.

---

## Key Technical Highlights

### 1. Hexagonal Architecture (Port & Adapters)
비즈니스 로직이 외부 기술(DB, 외부 API)에 의존하지 않도록 계층을 엄격히 분리했습니다.
- **도메인 순수성**: 프레임워크에 의존하지 않는 순수 도메인 모델을 유지하여 비즈니스 복잡도를 안정적으로 관리합니다.
- **높은 테스트 가능성**: 외부 어댑터를 Mocking하기 쉬운 구조로 설계되어 단위 테스트 및 핵심 로직 검증에 최적화되어 있습니다.

### 2. 분산 환경에서의 2계층 캐시 동기화 (Caffeine + Redis)
네트워크 레이턴시를 최소화하고 글로벌 캐시의 부하를 줄이기 위해 로컬-글로벌 캐시 전략을 적용했습니다.
- **L1 (Local)**: Caffeine 캐시를 사용하여 In-Memory 속도로 데이터에 접근합니다.
- **L2 (Global)**: Redis를 사용하여 다중 서버 간의 데이터 일관성을 유지합니다.
- **Sync Mechanism**: Redis Pub/Sub을 활용하여 데이터 수정 시 클러스터 내 모든 서버의 L1 캐시를 즉시 무효화(Invalidation)하는 동기화 로직을 구현했습니다.

### 3. 원자적 재고 관리 (Concurrency Control)
타임딜이나 이벤트 시 발생하는 동시성 화력(Peak Traffic) 상황에서 재고 수량의 정합성을 100% 보장합니다.
- **Pessimistic Write Lock**: DB 수준에서 행(Row)에 대한 명시적인 락을 획득하여 동시 차감 시 발생할 수 있는 Race Condition을 원천 차단했습니다.
- **No-Cache Policy**: 재고 데이터는 정합성이 최우선이므로 조회부터 수정까지 항상 DB 원천 데이터를 사용하여 인화 오차를 방지합니다.

### 4. 확장 가능한 데이터 설계
- **UUIDv7 적용**: 시간 순으로 정렬 가능한 UUIDv7을 Primary Key로 채택하여 분산 DB 환경에서의 삽입 성능과 정렬 효율성을 동시에 확보했습니다.
- **No Foreign Key**: 서비스 간 결합도를 낮추고 분산 데이터베이스 확장을 용이하게 하기 위해 외래 키 제약 조건을 배제하고 애플리케이션 레벨에서 관계를 관리합니다.

---

## Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin 2.2.21 |
| **Framework** | Spring Boot 4.0.2 |
| **Persistence** | Spring Data JPA (Hibernate), H2 (Local/Test) |
| **Cache** | Caffeine (L1), Redis (L2) |
| **Concurrency** | Spring Distributed Cache Sync (Pub/Sub) |
| **Build Tool** | Gradle (Kotlin DSL) |

---

## Project Structure

```text
src/main/kotlin/com/example/commerce
├── application       # 비즈니스 오케스트레이션 (Use Cases)
│   ├── port          # Input/Output 포트 인터페이스
│   └── service       # 도메인 서비스 협력 제어
├── domain            # 핵심 비즈니스 로직 및 엔티티 (Pure Logic)
│   ├── model         # Product, Inventory, Order, Payment 등
│   └── exception     # 도메인 커스텀 예외
├── infrastructure    # 외부 기술 구현체 (Adapters)
│   ├── cache         # CompositeCache, RedisSync 등 캐시 인프라
│   ├── persistence   # DB 접근 어댑터 구현
│   └── external      # 외부 PG 및 시스템 연동
└── adapter           # 바깥 세상과의 접점
    └── web           # REST API 컨트롤러
```

---

##  핵심 비즈니스 로직 예시

### [InventoryService] 비관적 락을 활용한 재고 차감
```kotlin
@Transactional
fun deductStock(productId: String, quantity: Int): Inventory {
    // 1. PESSIMISTIC_WRITE 락으로 행 선점
    val inventory = inventoryPort.findByProductIdWithLock(productId)
        .orElseThrow { ... }

    // 2. 비즈니스 규칙 검증
    check(inventory.stock >= quantity) { "재고 부족" }

    // 3. 상태 변경 및 저장
    val updated = inventory.copy(stock = inventory.stock - quantity)
    return inventoryPort.save(updated)
}
```

### [CompositeCache] L1 + L2 동시 조회 및 적재
```kotlin
override fun get(key: Any): Cache.ValueWrapper? {
    for ((index, cache) in caches.withIndex()) {
        val value = cache.get(key)
        if (value != null) {
            // L2에서 찾았다면 L1에도 자동으로 즉시 채워줌 (Lazy Loading)
            fillPreviousCaches(index, key, value.get())
            return value
        }
    }
    return null
}
```

---

## Getting Started

### Prerequisites
- JDK 21
- Redis (Optional, Local 테스트 시 H2/In-Memory 활용 가능)

### Run Application
```bash
./gradlew bootRun
```

### Test
```bash
./gradlew test
```

---

## Coding Convention & Standards
- **Strict Null Safety**: Kotlin의 Non-nullable 타입을 적극 활용하여 런타임 NullPointerException을 배제합니다.
- **Functional Style**: `runCatching`, `copy`, `map` 등 고차 함수와 불변 객체를 활용하여 코드의 가독성과 예측 가능성을 높입니다.
- **Fail-Fast**: 비즈니스 유효성 검증을 메서드 도입부에서 즉시 수행하여 부수 효과를 사전에 차단합니다.
