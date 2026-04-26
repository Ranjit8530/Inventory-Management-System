# Simple Inventory Management System (Spring Boot + MySQL)

This is a **beginner-friendly backend** with clear layering and interview-friendly logic.

## 1) System Flow (step-by-step)

1. **Create Product**
   - `POST /products` saves product details.
   - Creates an inventory row with quantity `0` and default threshold `10`.
2. **Update Product**
   - `PUT /products/{id}` updates name/price and optionally threshold.
3. **Add Stock**
   - `POST /inventory/{productId}/add` increases quantity.
   - Logs an `ADD` transaction.
4. **Sell Product**
   - `POST /inventory/{productId}/sell` does all steps inside one DB transaction:
     - lock inventory row (`PESSIMISTIC_WRITE`)
     - check available stock
     - reduce quantity
     - save `SALE` transaction
5. **Low Stock Alerts**
   - `GET /inventory/alerts` returns rows where `quantity < threshold`.
   - Frontend can poll this endpoint at login/refresh.
6. **Daily Sales**
   - `GET /inventory/sales/daily` groups `SALE` transactions by date and sums units.

---

## 2) Database Schema

### Tables

- `product(id, name, price)`
- `inventory(product_id, quantity, threshold)`
- `inventory_transaction(id, product_id, quantity, type, timestamp)`

### Why separate Inventory and Transaction tables?

- `inventory` = current state (fast read for current quantity).
- `inventory_transaction` = history/audit trail (who sold/added how many and when).
- Separation keeps updates simple and reports easy (daily sales comes from transactions).

---

## 3) API List

### Product APIs

- `POST /products`
  ```json
  {
    "name": "Keyboard",
    "price": 1200.00,
    "threshold": 5
  }
  ```

- `PUT /products/{id}`
  ```json
  {
    "name": "Mechanical Keyboard",
    "price": 1500.00,
    "threshold": 8
  }
  ```

### Inventory APIs

- `POST /inventory/{productId}/add`
  ```json
  { "quantity": 10 }
  ```

- `POST /inventory/{productId}/sell`
  ```json
  { "quantity": 2 }
  ```

### Alert API

- `GET /inventory/alerts`

### Daily Sales API

- `GET /inventory/sales/daily`

---

## 4) Concurrency + Transaction Safety

### How concurrency is handled

- `sellProduct()` is annotated with `@Transactional`.
- It uses `findByProductIdForUpdate()` with `PESSIMISTIC_WRITE` lock.
- This means while one sale is updating a product row, another concurrent sale waits.

### If 2 users buy the last item at same time

Example: stock = 1

- User A acquires lock first, checks stock (1), sells 1, commits. stock becomes 0.
- User B then acquires lock, checks stock (0), gets "Not enough stock" error.
- Result: stock never becomes negative.

### DB failure after stock update

Because stock update + transaction insert are in one DB transaction:

- If an error happens before commit, **everything rolls back**.
- So no half-written state (e.g., stock reduced but transaction missing).

---

## 5) Low Stock Query Performance

- Query: `WHERE quantity < threshold`.
- Index: `idx_inventory_quantity` on `inventory(quantity)`.
- Why this index: DB can use quantity ordering to narrow scan quickly for small quantities.

### Time complexity (interview-friendly)

- Without index: typically full table scan => **O(n)**.
- With quantity index: lookup is often close to **O(log n + k)**
  - `log n` to navigate index
  - `k` for matching rows returned

---

## 6) Why polling instead of push notifications?

- Simpler for students and interview explanation.
- No WebSocket/SSE server complexity.
- Frontend calls `/inventory/alerts` on login/refresh or periodic timer.
- Good enough when alert freshness requirements are not real-time.

---

## 7) Edge Cases

1. **Selling more than available stock**
   - returns 400 with clear message.
2. **Duplicate API calls**
   - Current version is intentionally simple and not idempotent.
   - In production, add idempotency key (`X-Idempotency-Key`) table to avoid double sells.
3. **DB failure during sell**
   - `@Transactional` rollback keeps data consistent.

---

## Run

```bash
mvn spring-boot:run
```

Make sure MySQL DB `inventory_db` exists and credentials in `application.properties` are correct.
