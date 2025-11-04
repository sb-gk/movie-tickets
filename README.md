# 🍿 Movie Tickets API - Because Cinema Needs Us

Ever wondered what happens when cinemas fight back against Netflicks?

This little Spring Boot app calculates movie ticket costs like a boss, complete with senior discounts and family group deals.

## What This Does

This REST API takes transaction data (customer ages) and spits out exactly how much those movie tickets are going to cost, including all applicable discounts.

### Ticket Pricing (AKA Why Your Wallet Cries)
- **Adult** (18-64): $25.00 - Because adulthood is expensive enough already
- **Senior** (65+): $17.50 (30% off) - Retirement has its perks
- **Teen** (11-17): $12.00 - The teenage discount phase
- **Children** (0-10): $5.00 - They're cheap but loud
- **Children Group Discount**: 25% off when you bring 3+ little ones (some deals are worth the earplugs)

## 🚀 Quick Start

### Prerequisites
- Java 21
- Gradle (included via wrapper, no installation needed)

### Running the Beast

```bash
# Clone and run
./gradlew bootRun

# Running tests (recommended before production)
./gradlew test
./gradlew integrationTest  # Full integration tests
./gradlew check            # All tests + quality checks
```

The API will start dancing at `http://localhost:8080`

## API Usage

### Calculate Ticket Costs

**Endpoint**: `POST /api/tickets/calculate`

**Request Example**:
```json
{
  "transactionId": 1,
  "customers": [
    { "name": "John Smith", "age": 70 },
    { "name": "Jane Doe", "age": 5 },
    { "name": "Bob Doe", "age": 6 }
  ]
}
```

**Response**:
```json
{
  "transactionId": 1,
  "tickets": [
    { "ticketType": "Children", "quantity": 2, "totalCost": 10.00 },
    { "ticketType": "Senior", "quantity": 1, "totalCost": 17.50 }
  ],
  "totalCost": 27.50
}
```

### Interactive Documentation
Visit [Swagger UI](http://localhost:8080/swagger-ui/index.html) for the full OpenAPI documentation. Because reading code is so 2023.

## 🏗️ Architecture (For the Curious)

### The Clean Approach
- **Controller**: Handles HTTP (shockingly, right?)
- **Service Layer**: Does the actual math (what a concept)
- **Models**: Holds data like a digital filing cabinet
- **Configuration**: Externalized settings (so you don't need to recompile for pricing changes)
- **Discount Policies**: Strategy pattern for future extensibility (when seniors get their own discount tier)

### The Money Story
- Uses `BigDecimal` for precise monetary calculations (because floating point math is the devil)
- Automatic money validation because nobody likes negative prices
- Configurable pricing via `application.properties`

### The Testing Journey
- **Unit Tests**: Fast and focused on individual components
- **Integration Tests**: Complete HTTP request/response cycle testing
- **Test Coverage**: All sample scenarios from the original PDF specification

## 🧪 Test Examples

The codebase includes tests for all the fun scenarios:

1. **PDF Sample 1**: Senior + 2 Children = $27.50
2. **PDF Sample 2**: Adult + 3 Children (discount kicks in!) + Teen = $48.25  
3. **PDF Sample 3**: One of each ticket type = $59.50
4. **Edge Cases**: Empty customer lists, negative ages, large transactions
5. **Discount Validation**: Exactly 3 children gets the discount, 2 doesn't

## ⚙️ Configuration (For the Control Freaks)

All pricing and age limits live in `application.properties`:

```properties
# Age boundaries (you can change these for different regions)
ticket.age.children-max=10
ticket.age.teen-min=11
ticket.age.teen-max=17
ticket.age.adult-min=18
ticket.age.adult-max=64
ticket.age.senior-min=65

# Ticket pricing
ticket.price.adult-price=25.00
ticket.price.teen-price=12.00
ticket.price.children-price=5.00
ticket.price.senior-discount-rate=0.30
ticket.price.children-group-discount-rate=0.25
ticket.price.children-group-threshold=3
```

## 🛠️ Tech Stack (The Boring But Important Bits)

- **Spring Boot 3.5.7** - The framework that does the heavy lifting
- **Java 21** - Latest and greatest (we like staying current)
- **Gradle + Kotlin DSL** - Modern build tooling
- **JUnit 5** - Testing framework (because hope is not a strategy)
- **SpringDoc** - Auto-generates API documentation
- **Spotless** - Code formatting (consistency is key)
- **Jakarta Validation** - Because input validation should be automatic

## 🔍 Key Features

- **Comprehensive Validation**: Catches invalid ages, empty customer lists, and all the corner cases you never thought of
- **Configurable Pricing**: Update ticket prices without recompiling
- **Extensible Architecture**: Easy to add new discount policies or ticket types
- **Proper Error Handling**: Descriptive error messages that actually help
- **Money Precision**: No floating point surprises (your accounting team will thank you)
- **Alphabetical Ordering**: Tickets appear in consistent order (Adult, Children, Senior, Teen)
- **Real API Documentation**: Auto-generated Swagger UI

## 🎨 Code Quality

The codebase follows clean architecture principles with:
- Single Responsibility Principle (each class does one thing)
- Dependency Inversion (depends on abstractions, not concretions)
- Configuration as Code (externalized settings)
- Comprehensive Test Coverage (because tests are documentation)
- Consistent Code Style (Spotless keeps things tidy)

## 🤝 Contributing

While this project was born from a coding test, it demonstrates production-ready patterns. If you're looking to extend it:

1. Add new discount policies by implementing `DiscountPolicy`
2. Update age ranges in configuration for different regions
3. Add new ticket types by extending the `TicketType` enum
4. Enhance validation rules in the model classes

---
Built with ❤️ and a healthy respect for proper software engineering practices.
