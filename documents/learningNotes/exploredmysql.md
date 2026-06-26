# Explored MySQL

Revision notes for a 1-5 years MySQL developer interview. Covers SQL queries, joins, clauses, schema design, indexing, transactions, optimization, version updates, and practical implementation patterns.

Current version context: MySQL 8.4 is the Long-Term Support (LTS) line. MySQL 8.0 reached End of Life in April 2026. MySQL 9.x is the Innovation release track for newer features and faster upgrade cycles. For most production interviews, MySQL 8.0/8.4 knowledge is the practical baseline.

---

# 1. MySQL In One Paragraph

MySQL is an open-source relational database management system. It stores structured data in tables, supports SQL for querying and modifying data, provides transactions through InnoDB, uses indexes for fast lookup, supports joins, constraints, views, stored routines, triggers, JSON data, window functions, common table expressions, replication, partitioning, and query optimization tools like `EXPLAIN`.

Interview answer:

```text
MySQL is a relational database. Data is stored in tables with rows and columns. SQL is used to query, insert, update, and delete data. InnoDB provides transactions, row-level locking, foreign keys, indexes, and crash recovery. Performance depends heavily on schema design, indexes, query plans, transaction handling, and proper use of joins and filters.
```

---

# 2. MySQL Architecture Basics

## High-Level Flow

```text
Client
  -> MySQL Server
    -> Parser
    -> Optimizer
    -> Execution Engine
    -> Storage Engine
      -> Data files / Index files / Logs
```

## Main Components

- **Connection manager**: Handles client connections.
- **Parser**: Checks SQL syntax.
- **Optimizer**: Decides query execution plan.
- **Execution engine**: Runs the plan.
- **Storage engine**: Stores and retrieves rows.
- **InnoDB buffer pool**: Caches data and indexes in memory.
- **Redo log**: Crash recovery.
- **Undo log**: Rollback and MVCC.
- **Binary log**: Replication and point-in-time recovery.

## Storage Engines

Common engines:

- **InnoDB**: Default, transactional, row-level locking, foreign keys.
- **MyISAM**: Legacy, table-level locking, no transactions.
- **Memory**: Stores data in memory.
- **Archive**: Compresses historical data.

Interview answer:

```text
In modern MySQL, InnoDB is the default and most commonly used storage engine because it supports ACID transactions, row-level locking, crash recovery, indexes, and foreign keys.
```

---

# 3. SQL Categories

## DDL: Data Definition Language

Used to define structure.

```sql
CREATE TABLE users (...);
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
DROP TABLE users;
TRUNCATE TABLE users;
```

## DML: Data Manipulation Language

Used to modify data.

```sql
INSERT INTO users (...) VALUES (...);
UPDATE users SET name = 'A' WHERE id = 1;
DELETE FROM users WHERE id = 1;
```

## DQL: Data Query Language

Used to query data.

```sql
SELECT * FROM users;
```

## DCL: Data Control Language

Used for permissions.

```sql
GRANT SELECT ON app_db.* TO 'report_user'@'%';
REVOKE SELECT ON app_db.* FROM 'report_user'@'%';
```

## TCL: Transaction Control Language

Used for transactions.

```sql
START TRANSACTION;
COMMIT;
ROLLBACK;
SAVEPOINT before_update;
```

---

# 4. Common Data Types

## Numeric

| Type | Use |
|---|---|
| `TINYINT` | Small integer, booleans often use `TINYINT(1)` |
| `INT` | General integer |
| `BIGINT` | Large IDs/counts |
| `DECIMAL(p,s)` | Money/exact decimal |
| `FLOAT`, `DOUBLE` | Approximate values |

Money:

```sql
amount DECIMAL(12,2) NOT NULL
```

Avoid `FLOAT` for money due to precision issues.

## String

| Type | Use |
|---|---|
| `CHAR(n)` | Fixed length |
| `VARCHAR(n)` | Variable length |
| `TEXT` | Large text |
| `ENUM` | Fixed set of values, use carefully |
| `JSON` | JSON document |

Example:

```sql
email VARCHAR(255) NOT NULL
description TEXT
```

## Date And Time

| Type | Use |
|---|---|
| `DATE` | Date only |
| `TIME` | Time only |
| `DATETIME` | Date and time, no timezone conversion |
| `TIMESTAMP` | Stored UTC, timezone conversion behavior |

Common columns:

```sql
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

## Boolean

MySQL has `BOOLEAN` alias, but it maps to `TINYINT(1)`.

```sql
is_active BOOLEAN NOT NULL DEFAULT TRUE
```

---

# 5. Create Table Example

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'BLOCKED') NOT NULL DEFAULT 'ACTIVE',
    age INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_status_created (status, created_at)
) ENGINE = InnoDB;
```

Orders table:

```sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status ENUM('CREATED', 'PAID', 'SHIPPED', 'CANCELLED') NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_orders_order_number (order_number),
    KEY idx_orders_user_created (user_id, created_at),
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE = InnoDB;
```

Order items:

```sql
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    KEY idx_order_items_order (order_id),
    KEY idx_order_items_product (product_id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE = InnoDB;
```

---

# 6. Basic SELECT Query

Syntax order:

```sql
SELECT column_list
FROM table_name
WHERE condition
GROUP BY columns
HAVING group_condition
ORDER BY columns
LIMIT count OFFSET start;
```

Logical execution order:

```text
FROM / JOIN
WHERE
GROUP BY
HAVING
SELECT
DISTINCT
ORDER BY
LIMIT
```

Example:

```sql
SELECT id, name, email
FROM users
WHERE status = 'ACTIVE'
ORDER BY created_at DESC
LIMIT 10;
```

Interview point:

```text
SQL writing order and logical execution order are different. WHERE filters rows before grouping. HAVING filters after grouping.
```

---

# 7. WHERE Clause

## Basic Conditions

```sql
SELECT *
FROM users
WHERE status = 'ACTIVE';
```

```sql
SELECT *
FROM orders
WHERE total_amount > 1000;
```

## AND / OR

```sql
SELECT *
FROM orders
WHERE status = 'PAID'
  AND total_amount >= 500;
```

Use parentheses when mixing `AND` and `OR`:

```sql
SELECT *
FROM users
WHERE status = 'ACTIVE'
  AND (age >= 18 OR age IS NULL);
```

## IN

```sql
SELECT *
FROM orders
WHERE status IN ('PAID', 'SHIPPED');
```

## BETWEEN

```sql
SELECT *
FROM orders
WHERE created_at BETWEEN '2026-01-01' AND '2026-01-31';
```

For date ranges with time, prefer half-open interval:

```sql
SELECT *
FROM orders
WHERE created_at >= '2026-01-01'
  AND created_at < '2026-02-01';
```

## LIKE

```sql
SELECT *
FROM users
WHERE email LIKE '%@gmail.com';
```

Index note:

```text
LIKE 'abc%' can use index. LIKE '%abc' usually cannot use normal B-tree index efficiently.
```

## IS NULL

```sql
SELECT *
FROM users
WHERE age IS NULL;
```

Do not use:

```sql
WHERE age = NULL
```

`NULL` means unknown, so use `IS NULL` or `IS NOT NULL`.

---

# 8. ORDER BY

```sql
SELECT id, name, created_at
FROM users
ORDER BY created_at DESC;
```

Multiple columns:

```sql
SELECT *
FROM orders
ORDER BY status ASC, created_at DESC;
```

Interview note:

```text
Without ORDER BY, database does not guarantee row order. Even if it appears ordered today, it can change with execution plan, index, or data changes.
```

---

# 9. LIMIT And Pagination

## Basic LIMIT

```sql
SELECT *
FROM orders
ORDER BY created_at DESC
LIMIT 10;
```

## OFFSET Pagination

```sql
SELECT *
FROM orders
ORDER BY created_at DESC
LIMIT 20 OFFSET 40;
```

Problem:

```text
Large OFFSET becomes slow because MySQL still scans/skips previous rows.
```

## Keyset Pagination

Better for large data:

```sql
SELECT *
FROM orders
WHERE created_at < '2026-06-01 10:00:00'
ORDER BY created_at DESC
LIMIT 20;
```

For stable ordering:

```sql
SELECT *
FROM orders
WHERE (created_at, id) < ('2026-06-01 10:00:00', 1000)
ORDER BY created_at DESC, id DESC
LIMIT 20;
```

Index:

```sql
CREATE INDEX idx_orders_created_id ON orders (created_at, id);
```

---

# 10. DISTINCT

```sql
SELECT DISTINCT status
FROM orders;
```

Multiple columns:

```sql
SELECT DISTINCT user_id, status
FROM orders;
```

This returns unique combinations of `user_id` and `status`.

---

# 11. Aggregate Functions

Common functions:

- `COUNT`
- `SUM`
- `AVG`
- `MIN`
- `MAX`

Examples:

```sql
SELECT COUNT(*) AS total_users
FROM users;
```

```sql
SELECT SUM(total_amount) AS revenue
FROM orders
WHERE status = 'PAID';
```

```sql
SELECT MIN(total_amount), MAX(total_amount), AVG(total_amount)
FROM orders;
```

`COUNT(*)` vs `COUNT(column)`:

```sql
SELECT COUNT(*) FROM users;      -- counts rows
SELECT COUNT(age) FROM users;    -- counts non-null ages
```

---

# 12. GROUP BY

Group rows and aggregate.

```sql
SELECT status, COUNT(*) AS total
FROM orders
GROUP BY status;
```

Revenue by user:

```sql
SELECT user_id, SUM(total_amount) AS total_spent
FROM orders
WHERE status = 'PAID'
GROUP BY user_id;
```

Date grouping:

```sql
SELECT DATE(created_at) AS order_date, COUNT(*) AS total_orders
FROM orders
GROUP BY DATE(created_at)
ORDER BY order_date DESC;
```

Optimization note:

```text
Applying DATE(created_at) prevents direct range index use for filtering. For WHERE, use range conditions on created_at instead.
```

Better:

```sql
SELECT DATE(created_at) AS order_date, COUNT(*) AS total_orders
FROM orders
WHERE created_at >= '2026-06-01'
  AND created_at < '2026-07-01'
GROUP BY DATE(created_at);
```

---

# 13. HAVING

`HAVING` filters grouped results.

```sql
SELECT user_id, COUNT(*) AS order_count
FROM orders
GROUP BY user_id
HAVING COUNT(*) >= 5;
```

WHERE vs HAVING:

```sql
SELECT user_id, SUM(total_amount) AS revenue
FROM orders
WHERE status = 'PAID'
GROUP BY user_id
HAVING SUM(total_amount) > 10000;
```

Explanation:

- `WHERE status = 'PAID'` filters rows before grouping.
- `HAVING SUM(total_amount) > 10000` filters groups after aggregation.

---

# 14. Joins

## Sample Tables

```text
users(id, name, email)
orders(id, user_id, total_amount, status)
order_items(id, order_id, product_id, quantity, price)
products(id, name, category_id)
categories(id, name)
```

## INNER JOIN

Returns matching rows from both tables.

```sql
SELECT u.id, u.name, o.id AS order_id, o.total_amount
FROM users u
INNER JOIN orders o ON o.user_id = u.id;
```

Use when both sides must exist.

## LEFT JOIN

Returns all rows from left table and matching rows from right table.

```sql
SELECT u.id, u.name, o.id AS order_id
FROM users u
LEFT JOIN orders o ON o.user_id = u.id;
```

Users without orders:

```sql
SELECT u.id, u.name
FROM users u
LEFT JOIN orders o ON o.user_id = u.id
WHERE o.id IS NULL;
```

## RIGHT JOIN

Returns all rows from right table and matching rows from left table.

```sql
SELECT u.name, o.id AS order_id
FROM users u
RIGHT JOIN orders o ON o.user_id = u.id;
```

In practice, prefer rewriting as `LEFT JOIN` for readability.

## FULL OUTER JOIN

MySQL does not directly support `FULL OUTER JOIN`.

Simulate:

```sql
SELECT u.id AS user_id, o.id AS order_id
FROM users u
LEFT JOIN orders o ON o.user_id = u.id

UNION

SELECT u.id AS user_id, o.id AS order_id
FROM users u
RIGHT JOIN orders o ON o.user_id = u.id;
```

## CROSS JOIN

Returns Cartesian product.

```sql
SELECT c.name AS color, s.name AS size
FROM colors c
CROSS JOIN sizes s;
```

Use carefully because rows multiply.

## SELF JOIN

Join table with itself.

```sql
CREATE TABLE employees (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100),
    manager_id BIGINT
);
```

Employee with manager:

```sql
SELECT e.name AS employee, m.name AS manager
FROM employees e
LEFT JOIN employees m ON e.manager_id = m.id;
```

## Multiple Joins

```sql
SELECT
    o.id AS order_id,
    u.name AS user_name,
    p.name AS product_name,
    oi.quantity,
    oi.price
FROM orders o
JOIN users u ON u.id = o.user_id
JOIN order_items oi ON oi.order_id = o.id
JOIN products p ON p.id = oi.product_id
WHERE o.status = 'PAID';
```

## Join Interview Tips

```text
INNER JOIN returns only matched records. LEFT JOIN returns all rows from the left table and matched rows from the right table. If right-side columns are NULL, there was no match. MySQL has no direct FULL OUTER JOIN, but it can be simulated with LEFT JOIN UNION RIGHT JOIN.
```

---

# 15. ON vs WHERE With LEFT JOIN

Important interview topic.

## Condition In WHERE

```sql
SELECT u.id, u.name, o.id AS order_id
FROM users u
LEFT JOIN orders o ON o.user_id = u.id
WHERE o.status = 'PAID';
```

This behaves like inner join for orders because unmatched rows have `o.status = NULL`, which fails `WHERE`.

## Condition In ON

```sql
SELECT u.id, u.name, o.id AS paid_order_id
FROM users u
LEFT JOIN orders o
    ON o.user_id = u.id
   AND o.status = 'PAID';
```

This keeps all users and only joins paid orders.

Interview answer:

```text
For LEFT JOIN, filtering right table in WHERE can remove unmatched rows and turn it effectively into an INNER JOIN. If I want to preserve left rows, I put right-table filter conditions in the ON clause.
```

---

# 16. Subqueries

## Subquery In WHERE

Users with orders:

```sql
SELECT *
FROM users
WHERE id IN (
    SELECT user_id
    FROM orders
);
```

## EXISTS

Often better for existence checks:

```sql
SELECT *
FROM users u
WHERE EXISTS (
    SELECT 1
    FROM orders o
    WHERE o.user_id = u.id
);
```

Users without orders:

```sql
SELECT *
FROM users u
WHERE NOT EXISTS (
    SELECT 1
    FROM orders o
    WHERE o.user_id = u.id
);
```

## Subquery In FROM

```sql
SELECT user_id, total_spent
FROM (
    SELECT user_id, SUM(total_amount) AS total_spent
    FROM orders
    WHERE status = 'PAID'
    GROUP BY user_id
) t
WHERE total_spent > 10000;
```

## Correlated Subquery

Runs logically per outer row.

```sql
SELECT u.id, u.name,
       (
           SELECT COUNT(*)
           FROM orders o
           WHERE o.user_id = u.id
       ) AS order_count
FROM users u;
```

Can be slower than join/grouping for large data.

Alternative:

```sql
SELECT u.id, u.name, COUNT(o.id) AS order_count
FROM users u
LEFT JOIN orders o ON o.user_id = u.id
GROUP BY u.id, u.name;
```

---

# 17. Common Table Expressions

CTE uses `WITH`.

```sql
WITH paid_orders AS (
    SELECT *
    FROM orders
    WHERE status = 'PAID'
)
SELECT user_id, SUM(total_amount) AS total_spent
FROM paid_orders
GROUP BY user_id;
```

## Multiple CTEs

```sql
WITH paid_orders AS (
    SELECT *
    FROM orders
    WHERE status = 'PAID'
),
user_spend AS (
    SELECT user_id, SUM(total_amount) AS total_spent
    FROM paid_orders
    GROUP BY user_id
)
SELECT *
FROM user_spend
WHERE total_spent > 10000;
```

## Recursive CTE

Employee hierarchy:

```sql
WITH RECURSIVE employee_tree AS (
    SELECT id, name, manager_id, 1 AS level
    FROM employees
    WHERE manager_id IS NULL

    UNION ALL

    SELECT e.id, e.name, e.manager_id, et.level + 1
    FROM employees e
    JOIN employee_tree et ON e.manager_id = et.id
)
SELECT *
FROM employee_tree;
```

Interview point:

```text
CTEs improve readability and can express recursive queries. MySQL 8.0 added CTE support.
```

---

# 18. Window Functions

Window functions calculate values across related rows without collapsing rows like `GROUP BY`.

## ROW_NUMBER

Latest order per user:

```sql
SELECT *
FROM (
    SELECT
        o.*,
        ROW_NUMBER() OVER (
            PARTITION BY user_id
            ORDER BY created_at DESC
        ) AS rn
    FROM orders o
) ranked
WHERE rn = 1;
```

## RANK And DENSE_RANK

```sql
SELECT
    user_id,
    total_amount,
    RANK() OVER (ORDER BY total_amount DESC) AS amount_rank,
    DENSE_RANK() OVER (ORDER BY total_amount DESC) AS dense_amount_rank
FROM orders;
```

Difference:

- `RANK`: leaves gaps after ties.
- `DENSE_RANK`: no gaps after ties.

## Running Total

```sql
SELECT
    id,
    user_id,
    total_amount,
    created_at,
    SUM(total_amount) OVER (
        PARTITION BY user_id
        ORDER BY created_at
    ) AS running_total
FROM orders
WHERE status = 'PAID';
```

## LAG And LEAD

```sql
SELECT
    id,
    user_id,
    total_amount,
    LAG(total_amount) OVER (
        PARTITION BY user_id
        ORDER BY created_at
    ) AS previous_amount
FROM orders;
```

Interview point:

```text
GROUP BY reduces rows. Window functions keep rows and add calculations like rank, row number, running total, previous/next row, and partition-based aggregates.
```

---

# 19. UNION And UNION ALL

## UNION

Removes duplicates.

```sql
SELECT email FROM customers
UNION
SELECT email FROM vendors;
```

## UNION ALL

Keeps duplicates and is usually faster.

```sql
SELECT email FROM customers
UNION ALL
SELECT email FROM vendors;
```

Interview point:

```text
Use UNION ALL when duplicates are acceptable or impossible. UNION performs duplicate elimination, which adds cost.
```

---

# 20. INSERT

## Single Row

```sql
INSERT INTO users (name, email, status)
VALUES ('Anjali', 'anjali@example.com', 'ACTIVE');
```

## Multiple Rows

```sql
INSERT INTO users (name, email, status)
VALUES
    ('A', 'a@example.com', 'ACTIVE'),
    ('B', 'b@example.com', 'INACTIVE');
```

## Insert From Select

```sql
INSERT INTO archived_orders (order_id, user_id, total_amount, archived_at)
SELECT id, user_id, total_amount, NOW()
FROM orders
WHERE created_at < '2025-01-01';
```

## Upsert

```sql
INSERT INTO users (email, name, status)
VALUES ('a@example.com', 'Anjali', 'ACTIVE')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    status = VALUES(status);
```

Note:

```text
Upsert requires a primary key or unique key conflict.
```

---

# 21. UPDATE

```sql
UPDATE users
SET status = 'INACTIVE'
WHERE id = 10;
```

Update with join:

```sql
UPDATE users u
JOIN orders o ON o.user_id = u.id
SET u.status = 'ACTIVE'
WHERE o.created_at >= '2026-01-01';
```

Safe update checklist:

- Always use `WHERE` unless updating all rows intentionally.
- Preview rows first with `SELECT`.
- Use transaction for critical updates.
- Use primary key or indexed filters.

```sql
START TRANSACTION;

UPDATE orders
SET status = 'CANCELLED'
WHERE id = 100;

-- check result
SELECT * FROM orders WHERE id = 100;

COMMIT;
```

---

# 22. DELETE, TRUNCATE, DROP

## DELETE

Removes selected rows. Can be rolled back in transaction.

```sql
DELETE FROM orders
WHERE status = 'CANCELLED'
  AND created_at < '2025-01-01';
```

## TRUNCATE

Removes all rows and resets auto-increment. DDL operation.

```sql
TRUNCATE TABLE temp_imports;
```

## DROP

Removes table structure and data.

```sql
DROP TABLE temp_imports;
```

Interview answer:

```text
DELETE removes rows and can have WHERE. TRUNCATE removes all rows faster and resets auto increment. DROP removes the table itself.
```

---

# 23. Constraints

## PRIMARY KEY

Unique row identifier.

```sql
id BIGINT PRIMARY KEY AUTO_INCREMENT
```

## UNIQUE

No duplicate values.

```sql
UNIQUE KEY uk_users_email (email)
```

## NOT NULL

Requires value.

```sql
name VARCHAR(100) NOT NULL
```

## DEFAULT

Default value.

```sql
status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
```

## CHECK

```sql
age INT CHECK (age >= 0)
```

## FOREIGN KEY

```sql
CONSTRAINT fk_orders_user
    FOREIGN KEY (user_id) REFERENCES users(id)
```

Cascade:

```sql
CONSTRAINT fk_order_items_order
    FOREIGN KEY (order_id) REFERENCES orders(id)
    ON DELETE CASCADE
```

Use cascade carefully. Accidental parent delete can delete many child rows.

---

# 24. Indexes

Indexes speed up reads by allowing MySQL to find rows without scanning entire table.

## Primary Index

In InnoDB, primary key is clustered index. Table data is stored ordered by primary key.

```sql
PRIMARY KEY (id)
```

## Secondary Index

```sql
CREATE INDEX idx_users_status ON users (status);
```

## Composite Index

```sql
CREATE INDEX idx_orders_user_status_created
ON orders (user_id, status, created_at);
```

Useful query:

```sql
SELECT *
FROM orders
WHERE user_id = 10
  AND status = 'PAID'
ORDER BY created_at DESC;
```

## Leftmost Prefix Rule

Index:

```sql
(user_id, status, created_at)
```

Can help:

```sql
WHERE user_id = 10
WHERE user_id = 10 AND status = 'PAID'
WHERE user_id = 10 AND status = 'PAID' AND created_at >= '2026-01-01'
```

May not help well:

```sql
WHERE status = 'PAID'
```

because `user_id` is missing.

## Covering Index

If all needed columns are in index, MySQL may avoid reading table rows.

```sql
CREATE INDEX idx_orders_user_status_amount
ON orders (user_id, status, total_amount);
```

Query:

```sql
SELECT user_id, status, total_amount
FROM orders
WHERE user_id = 10
  AND status = 'PAID';
```

## Unique Index

```sql
CREATE UNIQUE INDEX uk_users_email ON users (email);
```

## Functional Index

MySQL 8 supports indexes on expressions.

```sql
CREATE INDEX idx_users_lower_email ON users ((LOWER(email)));
```

Query:

```sql
SELECT *
FROM users
WHERE LOWER(email) = 'a@example.com';
```

## Invisible Index

Test removing an index without dropping it.

```sql
ALTER TABLE orders ALTER INDEX idx_orders_user_created INVISIBLE;
ALTER TABLE orders ALTER INDEX idx_orders_user_created VISIBLE;
```

Interview answer:

```text
Indexes improve read performance but slow writes and consume storage. I create indexes based on WHERE, JOIN, ORDER BY, GROUP BY, and query frequency. Composite index order matters because of the leftmost prefix rule.
```

---

# 25. EXPLAIN

Use `EXPLAIN` to understand query plan.

```sql
EXPLAIN
SELECT *
FROM orders
WHERE user_id = 10
  AND status = 'PAID';
```

Important columns:

| Column | Meaning |
|---|---|
| `id` | Query step id |
| `select_type` | Simple/subquery/derived |
| `table` | Table being accessed |
| `type` | Access type |
| `possible_keys` | Indexes MySQL could use |
| `key` | Index actually used |
| `rows` | Estimated rows scanned |
| `Extra` | Extra operations |

Access type rough order:

```text
system / const -> eq_ref -> ref -> range -> index -> ALL
```

`ALL` means full table scan. Sometimes acceptable for small tables, but often a warning for large tables.

`Extra` examples:

- `Using index`: covering index.
- `Using where`: server filters rows.
- `Using filesort`: additional sort operation.
- `Using temporary`: temporary table used.

JSON format:

```sql
EXPLAIN FORMAT=JSON
SELECT *
FROM orders
WHERE user_id = 10;
```

Runtime plan:

```sql
EXPLAIN ANALYZE
SELECT *
FROM orders
WHERE user_id = 10;
```

Interview answer:

```text
I use EXPLAIN to check whether the query uses the expected index, how many rows MySQL estimates it will scan, join order, access type, and whether it needs filesort or temporary table.
```

---

# 26. Query Optimization

## Use Selective WHERE Conditions

Good:

```sql
SELECT id, order_number, total_amount
FROM orders
WHERE user_id = 10
  AND status = 'PAID';
```

Avoid unnecessary:

```sql
SELECT *
FROM orders;
```

## Avoid Functions On Indexed Columns In WHERE

Bad:

```sql
WHERE DATE(created_at) = '2026-06-26'
```

Better:

```sql
WHERE created_at >= '2026-06-26'
  AND created_at < '2026-06-27'
```

## Avoid Leading Wildcard LIKE

May not use normal index:

```sql
WHERE name LIKE '%john'
```

Can use index:

```sql
WHERE name LIKE 'john%'
```

For full text search:

```sql
CREATE FULLTEXT INDEX ft_products_name_desc
ON products (name, description);

SELECT *
FROM products
WHERE MATCH(name, description) AGAINST ('phone charger');
```

## Avoid Large OFFSET

Bad for deep pages:

```sql
LIMIT 20 OFFSET 100000
```

Better keyset pagination:

```sql
WHERE id > 100000
ORDER BY id
LIMIT 20
```

## Select Only Needed Columns

Bad:

```sql
SELECT * FROM users;
```

Better:

```sql
SELECT id, name, email FROM users;
```

## Index Join Columns

```sql
CREATE INDEX idx_orders_user_id ON orders (user_id);
```

## Use EXISTS For Existence

```sql
SELECT *
FROM users u
WHERE EXISTS (
    SELECT 1
    FROM orders o
    WHERE o.user_id = u.id
);
```

## Avoid N+1 Queries

Bad application behavior:

```text
SELECT * FROM users LIMIT 100;
Then 100 times:
SELECT * FROM orders WHERE user_id = ?;
```

Better:

```sql
SELECT u.*, o.*
FROM users u
LEFT JOIN orders o ON o.user_id = u.id
WHERE u.id IN (...);
```

Or use batching.

## Keep Transactions Short

Long transactions:

- Hold locks longer.
- Increase undo log usage.
- Can block other queries.
- Can delay purge.

## Optimize Count

```sql
SELECT COUNT(*) FROM orders WHERE status = 'PAID';
```

Index:

```sql
CREATE INDEX idx_orders_status ON orders (status);
```

For very large data and frequent counts, consider summary tables/caches.

---

# 27. Transactions

Transaction groups multiple operations atomically.

```sql
START TRANSACTION;

UPDATE accounts
SET balance = balance - 100
WHERE id = 1;

UPDATE accounts
SET balance = balance + 100
WHERE id = 2;

COMMIT;
```

Rollback:

```sql
START TRANSACTION;

UPDATE accounts SET balance = balance - 100 WHERE id = 1;

ROLLBACK;
```

## ACID

- **Atomicity**: All or nothing.
- **Consistency**: Valid state before and after transaction.
- **Isolation**: Concurrent transactions do not corrupt each other.
- **Durability**: Committed data survives crash.

## Savepoint

```sql
START TRANSACTION;

UPDATE orders SET status = 'PAID' WHERE id = 1;
SAVEPOINT after_order_update;

UPDATE inventory SET stock = stock - 1 WHERE product_id = 10;

ROLLBACK TO SAVEPOINT after_order_update;

COMMIT;
```

---

# 28. Isolation Levels

MySQL InnoDB default isolation level is commonly `REPEATABLE READ`.

## Problems

| Problem | Meaning |
|---|---|
| Dirty read | Read uncommitted data |
| Non-repeatable read | Same row returns different value in same transaction |
| Phantom read | Re-running range query returns new rows |

## Levels

| Level | Dirty Read | Non-repeatable Read | Phantom |
|---|---|---|---|
| READ UNCOMMITTED | Possible | Possible | Possible |
| READ COMMITTED | Prevented | Possible | Possible |
| REPEATABLE READ | Prevented | Prevented | InnoDB handles many cases with next-key locks |
| SERIALIZABLE | Prevented | Prevented | Prevented |

Check:

```sql
SELECT @@transaction_isolation;
```

Set:

```sql
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
```

Interview answer:

```text
Isolation controls how concurrent transactions see each other's changes. Higher isolation gives stronger consistency but can reduce concurrency. InnoDB commonly uses REPEATABLE READ by default with MVCC and locking behavior.
```

---

# 29. Locks And MVCC

## Row-Level Lock

```sql
START TRANSACTION;

SELECT *
FROM orders
WHERE id = 100
FOR UPDATE;

UPDATE orders
SET status = 'PAID'
WHERE id = 100;

COMMIT;
```

`FOR UPDATE` locks selected rows for update.

## Shared Lock

```sql
SELECT *
FROM orders
WHERE id = 100
LOCK IN SHARE MODE;
```

## MVCC

Multi-Version Concurrency Control lets readers see a consistent snapshot without blocking writers in many cases.
MVCC allows robust databases to be fast, highly concurrent, and completely safe from data corruption during parallel processing.

#### The Problem MVCC Solves
In older or simpler databases (like MyISAM), if a user is updating a table, the database has to "lock" that data so nobody else can read a half-finished update.
* The result: Readers have to wait for writers to finish, and writers have to wait for readers to finish. In a high-traffic backend system, this creates a massive bottleneck.

### How MVCC Works (The "Multi-Version" Part)
MVCC solves this by creating snapshots (multiple versions) of the data.

* Instead of locking the data and forcing everyone to wait, the database keeps the old version of the row available while the new version is being written.

* Every transaction gets an ID: When a transaction starts, it is assigned a unique, incrementing Transaction ID.

* Writers create new versions: When Transaction A updates a row, it doesn't overwrite the original data immediately. Instead, it creates a new version of that row stamped with its Transaction ID.

 Readers see old versions: If Transaction B tries to read that same row while Transaction A is still working, the database looks at Transaction B's ID, realizes it shouldn't see the uncommitted changes, and serves it the older, original version of the row.

## Why This Matters for Backend Architecture
* High Throughput: Your application can handle thousands of concurrent SELECT queries while bulk UPDATE operations are happening in the background, without any locking conflicts.

* Consistent Backups: You can take a snapshot backup of a massive database without having to take the database offline or lock tables. The backup process just reads the version of the data that existed at the exact millisecond the backup started.

* Isolation Levels: MVCC is the magic behind the ACID isolation level called Repeatable Read (which is InnoDB's default). It guarantees that if your application queries the same row twice within a single transaction, it will get the exact same result both times, even if another transaction modified that row in between the two queries.

InnoDB uses:

- Undo logs.
- Read views.
- Row versions.

## Deadlock

Deadlock happens when transactions wait for each other.

Example:

```text
T1 locks row A, waits for row B
T2 locks row B, waits for row A
```

MySQL detects deadlock and rolls back one transaction.

Prevention:

- Access rows in consistent order.
- Keep transactions short.
- Use proper indexes.
- Retry deadlocked transaction in application.

---

# 30. Normalization

## 1NF

Atomic values, no repeating groups.

Bad:

```text
user(id, name, phone_numbers)
```

Good:

```text
users(id, name)
user_phones(id, user_id, phone)
```

## 2NF

No partial dependency on composite key.

## 3NF

No transitive dependency.

Example:

Bad:

```text
orders(id, user_id, user_email, total)
```

Better:

```text
users(id, email)
orders(id, user_id, total)
```

## Denormalization

Sometimes duplicate data intentionally for performance.

Example:

```text
orders(id, user_id, user_email_snapshot, total)
```

Why:

- Preserve historical value.
- Reduce join cost.
- Reporting optimization.

Interview answer:

```text
Normalize for consistency and reduce duplication. Denormalize carefully when read performance, reporting, or historical snapshots justify it.
```

---

# 31. Relationships

## One-To-One

```text
users(id)
user_profiles(user_id unique)
```

```sql
CREATE TABLE user_profiles (
    user_id BIGINT PRIMARY KEY,
    bio TEXT,
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## One-To-Many

```text
users -> orders
```

```sql
orders.user_id references users.id
```

## Many-To-Many

```text
students <-> courses
```

```sql
CREATE TABLE student_courses (
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);
```

---

# 32. Views

View is a saved query.

```sql
CREATE VIEW paid_order_summary AS
SELECT user_id, COUNT(*) AS order_count, SUM(total_amount) AS total_spent
FROM orders
WHERE status = 'PAID'
GROUP BY user_id;
```

Use:

```sql
SELECT *
FROM paid_order_summary
WHERE total_spent > 10000;
```

Interview note:

```text
A normal MySQL view does not store data like a materialized view. It is a stored query definition. Performance still depends on the underlying query.
```

---

# 33. Stored Procedures And Functions

## Stored Procedure

```sql
DELIMITER //

CREATE PROCEDURE get_user_orders(IN p_user_id BIGINT)
BEGIN
    SELECT *
    FROM orders
    WHERE user_id = p_user_id
    ORDER BY created_at DESC;
END //

DELIMITER ;
```

Call:

```sql
CALL get_user_orders(10);
```

## Function

```sql
DELIMITER //

CREATE FUNCTION calculate_tax(amount DECIMAL(12,2))
RETURNS DECIMAL(12,2)
DETERMINISTIC
BEGIN
    RETURN amount * 0.18;
END //

DELIMITER ;
```

Use:

```sql
SELECT calculate_tax(1000);
```

Interview note:

```text
Stored routines centralize logic in database, but too much business logic in DB can make app testing/deployment harder. Use carefully.
```

---

# 34. Triggers

Trigger runs automatically on table event.

Audit example:

```sql
CREATE TABLE user_audit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

```sql
DELIMITER //

CREATE TRIGGER trg_users_status_audit
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO user_audit (user_id, old_status, new_status)
        VALUES (NEW.id, OLD.status, NEW.status);
    END IF;
END //

DELIMITER ;
```

Interview note:

```text
Triggers are useful for auditing and enforcing DB-side behavior, but hidden side effects can make debugging difficult.
```

---

# 35. JSON In MySQL

MySQL supports JSON type and functions.

```sql
CREATE TABLE user_settings (
    user_id BIGINT PRIMARY KEY,
    settings JSON NOT NULL
);
```

Insert:

```sql
INSERT INTO user_settings (user_id, settings)
VALUES (1, '{"theme":"dark","notifications":true}');
```

Read:

```sql
SELECT JSON_EXTRACT(settings, '$.theme') AS theme
FROM user_settings
WHERE user_id = 1;
```

Shortcut:

```sql
SELECT settings->>'$.theme' AS theme
FROM user_settings;
```

Update:

```sql
UPDATE user_settings
SET settings = JSON_SET(settings, '$.theme', 'light')
WHERE user_id = 1;
```

Index JSON value using generated column:

```sql
ALTER TABLE user_settings
ADD COLUMN theme VARCHAR(20)
    GENERATED ALWAYS AS (settings->>'$.theme') STORED,
ADD INDEX idx_user_settings_theme (theme);
```

Interview answer:

```text
JSON is useful for flexible attributes, but if fields are frequently filtered, joined, or constrained, normal columns are better. For JSON filtering, use generated columns or functional indexes where appropriate.
```

---

# 36. Partitioning

Partitioning splits one table into logical partitions.

Example by year:

```sql
CREATE TABLE orders_partitioned (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    created_at DATE NOT NULL,
    PRIMARY KEY (id, created_at)
)
PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);
```

Use cases:

- Large historical tables.
- Easy archival/drop old partitions.
- Query pruning by partition key.

Interview note:

```text
Partitioning is not a replacement for indexes. Queries must filter on partition key to benefit from partition pruning.
```

---

# 37. Full-Text Search

```sql
CREATE FULLTEXT INDEX ft_products_name_description
ON products (name, description);
```

Query:

```sql
SELECT *
FROM products
WHERE MATCH(name, description) AGAINST ('wireless mouse');
```

Boolean mode:

```sql
SELECT *
FROM products
WHERE MATCH(name, description) AGAINST ('+wireless +mouse' IN BOOLEAN MODE);
```

For advanced search, Elasticsearch/OpenSearch may be better.

---

# 38. Users And Permissions

Create user:

```sql
CREATE USER 'app_user'@'%' IDENTIFIED BY 'strong_password';
```

Grant:

```sql
GRANT SELECT, INSERT, UPDATE, DELETE
ON app_db.*
TO 'app_user'@'%';
```

Apply:

```sql
FLUSH PRIVILEGES;
```

Show grants:

```sql
SHOW GRANTS FOR 'app_user'@'%';
```

Principle:

```text
Use least privilege. App user should not have DROP, SUPER, or broad admin permissions in production.
```

---

# 39. Backup And Restore

## Logical Backup

```bash
mysqldump -u root -p app_db > app_db_backup.sql
```

Restore:

```bash
mysql -u root -p app_db < app_db_backup.sql
```

## Binary Logs

Used for replication and point-in-time recovery.

Point-in-time recovery concept:

```text
Restore full backup
Replay binary logs until desired time
```

## Production Notes

- Test restore, not only backup.
- Use consistent backups.
- Encrypt backups.
- Store backups off-server.
- Define RPO and RTO.

---

# 40. Replication

Replication copies data from source to replica.

```text
Primary -> Binary Log -> Replica IO Thread -> Relay Log -> SQL Thread
```

Use cases:

- Read scaling.
- Reporting.
- Backup off replica.
- High availability.

Replication lag:

```sql
SHOW REPLICA STATUS\G
```

Interview note:

```text
Replication is usually asynchronous, so replicas can lag. Do not read immediately-after-write data from a lagging replica if strong consistency is required.
```

---

# 41. MySQL Version Highlights

## MySQL 5.7

Important features:

- JSON data type.
- Generated columns.
- Better optimizer than older versions.

Still common in legacy systems but old for new projects.

## MySQL 8.0

Major developer features:

- Window functions.
- Common table expressions.
- Recursive CTEs.
- Roles.
- Invisible indexes.
- Descending indexes.
- Functional indexes.
- Histograms.
- `EXPLAIN ANALYZE`.
- Better JSON functions.
- `NOWAIT` and `SKIP LOCKED`.
- Atomic DDL improvements.
- `utf8mb4` default improvements.

Examples:

```sql
WITH active_users AS (
    SELECT *
    FROM users
    WHERE status = 'ACTIVE'
)
SELECT COUNT(*) FROM active_users;
```

```sql
SELECT
    user_id,
    total_amount,
    ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY created_at DESC) AS rn
FROM orders;
```

## MySQL 8.4 LTS

Important because:

- Current LTS line.
- Recommended long-term target over EOL 8.0.
- Focuses on stability, security, and long support lifecycle.

Interview point:

```text
For production stability, I would prefer MySQL 8.4 LTS. For latest features and faster upgrade cycles, MySQL 9.x Innovation track is available.
```

## MySQL 9.x Innovation

Use when:

- Team can upgrade frequently.
- You need newest features.
- Automated testing and rollback plans are mature.

Do not choose only because version number is higher.

---

# 42. Common Interview Query Problems

## Second Highest Salary

```sql
SELECT MAX(salary) AS second_highest_salary
FROM employees
WHERE salary < (
    SELECT MAX(salary)
    FROM employees
);
```

Using dense rank:

```sql
SELECT salary
FROM (
    SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk
    FROM employees
) t
WHERE rnk = 2
LIMIT 1;
```

## Duplicate Emails

```sql
SELECT email, COUNT(*) AS total
FROM users
GROUP BY email
HAVING COUNT(*) > 1;
```

## Delete Duplicate Rows Keeping Lowest ID

```sql
DELETE u1
FROM users u1
JOIN users u2
  ON u1.email = u2.email
 AND u1.id > u2.id;
```

## Customers Without Orders

```sql
SELECT u.*
FROM users u
LEFT JOIN orders o ON o.user_id = u.id
WHERE o.id IS NULL;
```

Using `NOT EXISTS`:

```sql
SELECT u.*
FROM users u
WHERE NOT EXISTS (
    SELECT 1
    FROM orders o
    WHERE o.user_id = u.id
);
```

## Top 3 Orders Per User

```sql
SELECT *
FROM (
    SELECT
        o.*,
        ROW_NUMBER() OVER (
            PARTITION BY user_id
            ORDER BY total_amount DESC
        ) AS rn
    FROM orders o
) ranked
WHERE rn <= 3;
```

## Monthly Revenue

```sql
SELECT
    DATE_FORMAT(created_at, '%Y-%m') AS month,
    SUM(total_amount) AS revenue
FROM orders
WHERE status = 'PAID'
GROUP BY DATE_FORMAT(created_at, '%Y-%m')
ORDER BY month;
```

## Running Total

```sql
SELECT
    created_at,
    total_amount,
    SUM(total_amount) OVER (ORDER BY created_at) AS running_total
FROM orders
WHERE status = 'PAID';
```

## Find Gaps In IDs

```sql
SELECT t1.id + 1 AS gap_start
FROM orders t1
LEFT JOIN orders t2 ON t2.id = t1.id + 1
WHERE t2.id IS NULL;
```

## Users With More Than 5 Orders

```sql
SELECT u.id, u.name, COUNT(o.id) AS order_count
FROM users u
JOIN orders o ON o.user_id = u.id
GROUP BY u.id, u.name
HAVING COUNT(o.id) > 5;
```

## Latest Order Per User

```sql
SELECT *
FROM (
    SELECT
        o.*,
        ROW_NUMBER() OVER (
            PARTITION BY user_id
            ORDER BY created_at DESC, id DESC
        ) AS rn
    FROM orders o
) t
WHERE rn = 1;
```

---

# 43. Practical Application Design

## Order API Query

Requirement:

```text
List orders for one user, newest first, filter by status, paginate.
```

Query:

```sql
SELECT id, order_number, total_amount, status, created_at
FROM orders
WHERE user_id = ?
  AND status = ?
ORDER BY created_at DESC, id DESC
LIMIT ?;
```

Index:

```sql
CREATE INDEX idx_orders_user_status_created_id
ON orders (user_id, status, created_at, id);
```

## Search Users

```sql
SELECT id, name, email
FROM users
WHERE status = 'ACTIVE'
  AND name LIKE 'anj%'
ORDER BY name
LIMIT 20;
```

Index:

```sql
CREATE INDEX idx_users_status_name ON users (status, name);
```

## Soft Delete

```sql
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP NULL;
```

Query:

```sql
SELECT *
FROM users
WHERE deleted_at IS NULL;
```

Index:

```sql
CREATE INDEX idx_users_deleted_status ON users (deleted_at, status);
```

Tradeoff:

```text
Soft delete preserves history but every query must filter deleted rows. For large tables, indexing and archival strategy matter.
```

## Audit Columns

```sql
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
created_by BIGINT,
updated_by BIGINT
```

---

# 44. Common Mistakes

- Using `SELECT *` in APIs.
- Missing indexes on foreign keys.
- Using `LIKE '%abc'` and expecting index usage.
- Applying functions to indexed columns in `WHERE`.
- Not using transactions for multi-step writes.
- Long-running transactions.
- Large `OFFSET` pagination.
- Missing `WHERE` in `UPDATE` or `DELETE`.
- Not checking execution plan.
- Too many indexes slowing writes.
- Wrong composite index order.
- Storing money in `FLOAT`.
- Using `DATETIME`/timezone inconsistently.
- Overusing JSON for relational data.
- Reading from replica when immediate consistency is needed.
- Assuming row order without `ORDER BY`.
- Ignoring character set and collation.

---

# 45. Interview Questions And Answers

## What Is MySQL?

MySQL is a relational database management system. It stores data in tables and uses SQL for querying and modification. InnoDB provides transactions, row-level locking, indexes, and foreign keys.

## What Is Primary Key?

A primary key uniquely identifies each row. In InnoDB, the primary key is clustered, meaning data is stored organized by primary key.

## What Is Foreign Key?

Foreign key enforces relationship between tables.

## INNER JOIN vs LEFT JOIN

INNER JOIN returns matching rows only. LEFT JOIN returns all rows from the left table and matching rows from the right table; unmatched right columns become NULL.

## WHERE vs HAVING

WHERE filters rows before grouping. HAVING filters groups after aggregation.

## DELETE vs TRUNCATE vs DROP

DELETE removes selected rows and supports WHERE. TRUNCATE removes all rows and resets auto increment. DROP removes the table itself.

## CHAR vs VARCHAR

CHAR is fixed length. VARCHAR is variable length. Use VARCHAR for most variable text.

## DATETIME vs TIMESTAMP

DATETIME stores date and time as given. TIMESTAMP has timezone conversion behavior and is commonly used for audit timestamps.

## Index Advantages And Disadvantages

Indexes improve reads, joins, sorting, and filtering. They consume storage and slow insert/update/delete because indexes must be maintained.

## Composite Index

Index on multiple columns. Column order matters due to leftmost prefix rule.

## What Is Covering Index?

An index that contains all columns needed by the query, allowing MySQL to satisfy query from index without table lookup.

## What Is EXPLAIN?

EXPLAIN shows the execution plan: table access order, index usage, estimated rows, join type, and extra operations like filesort or temporary table.

## What Is Transaction?

A transaction groups operations into one unit of work. It can commit all changes or rollback all changes.

## What Is ACID?

Atomicity, Consistency, Isolation, Durability.

## What Is Deadlock?

Two or more transactions wait for each other locks. MySQL detects deadlock and rolls back one transaction.

## What Is Normalization?

Process of organizing data to reduce redundancy and improve consistency.

## What Is Denormalization?

Intentional duplication of data for performance or reporting.

## What Is CTE?

A temporary named result set defined using `WITH`, available within a single SQL statement.

## What Is Window Function?

A function that calculates across related rows while keeping individual rows in result.

## What Is Replication?

Copying data from primary to replica, commonly using binary logs. Used for read scaling, backups, and high availability.

## What Is Slow Query Optimization Process?

```text
Identify slow query -> check EXPLAIN/EXPLAIN ANALYZE -> verify indexes -> check filters/joins/order/group -> reduce selected columns -> rewrite query if needed -> test with production-like data.
```

---

# 46. Quick Revision Cheat Sheet

```text
SELECT = read data
INSERT = add rows
UPDATE = modify rows
DELETE = remove rows
TRUNCATE = remove all rows
DROP = remove object
WHERE = row filter
GROUP BY = group rows
HAVING = group filter
ORDER BY = sort result
LIMIT = restrict rows
INNER JOIN = matched rows only
LEFT JOIN = all left rows + matched right rows
RIGHT JOIN = all right rows + matched left rows
FULL OUTER JOIN = not directly supported in MySQL
UNION = combine and remove duplicates
UNION ALL = combine and keep duplicates
PRIMARY KEY = unique row identifier
FOREIGN KEY = relationship constraint
INDEX = faster lookup
COMPOSITE INDEX = multi-column index
LEFTMOST PREFIX = index usable from left columns
COVERING INDEX = query satisfied by index only
EXPLAIN = query execution plan
TRANSACTION = atomic unit of work
COMMIT = save transaction
ROLLBACK = undo transaction
CTE = WITH query
WINDOW FUNCTION = analytics without collapsing rows
MVCC = consistent reads with row versions
DEADLOCK = transactions waiting on each other
REPLICATION = primary data copied to replica
```

---

# 47. Final Interview Story

```text
I use MySQL mainly with InnoDB. I design normalized schemas with primary keys, foreign keys, proper data types, and indexes based on actual query patterns. For queries, I am comfortable with SELECT, WHERE, JOINs, GROUP BY, HAVING, ORDER BY, LIMIT, subqueries, CTEs, and window functions.

For performance, I avoid SELECT *, avoid functions on indexed columns in WHERE, use composite indexes carefully, check EXPLAIN, and prefer keyset pagination for large datasets. I understand transactions, ACID, isolation levels, row locks, MVCC, and deadlocks. For production, I consider backups, replication lag, permissions, slow query logs, and upgrade path. For current versions, MySQL 8.4 LTS is the stable long-term target, while 9.x is the innovation track.
```

---

# 48. References

- MySQL 8.4 Reference Manual: https://docs.oracle.com/cd/E17952_01/mysql-8.4-en/
- MySQL 9.7 Reference Manual: https://dev.mysql.com/doc/mysql/en/
- MySQL 8.0 Release Notes: https://dev.mysql.com/doc/relnotes/mysql/8.0/en/
- MySQL 9.7 Release Notes: https://dev.mysql.com/doc/relnotes/mysql/9.7/en/
- MySQL Server Versions: https://docs.oracle.com/en-us/iaas/mysql-database/doc/mysql-server-versions.html
- MySQL 8.0 New Features: https://dev.mysql.com/doc/refman/8.0/en/mysql-nutshell.html
