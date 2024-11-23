UPDATE products
SET price = price * 1.1
WHERE created_at < '2023-01-01';
