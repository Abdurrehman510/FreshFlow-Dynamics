-- ═══════════════════════════════════════════════════════════════════════
-- V2__seed_data.sql
-- Demo seed data for development, testing and portfolio demo
-- ═══════════════════════════════════════════════════════════════════════

-- ─────────────────────────────────────────────────────────────────────────────
-- Platform admin user
-- Password: Admin@1234 (BCrypt hash — never store plain text)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO users (username, password_hash, role, wallet_balance) VALUES
('platformadmin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TiGGzgpADyKbPV7NJxeqSI4P4WG', 'PLATFORM_ADMIN', 0.00),
('storeadmin',    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TiGGzgpADyKbPV7NJxeqSI4P4WG', 'STORE_ADMIN',    0.00),
('rahul_sharma',  '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TiGGzgpADyKbPV7NJxeqSI4P4WG', 'CUSTOMER',       5000.00),
('priya_patel',   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TiGGzgpADyKbPV7NJxeqSI4P4WG', 'CUSTOMER',       2500.00);

-- ─────────────────────────────────────────────────────────────────────────────
-- Suppliers (variety of reliability scores to demonstrate routing algorithm)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO suppliers (name, contact_number, email, category, avg_delivery_hours, reliability_score) VALUES
('Amul Dairy Ahmedabad',      '9876543210', 'amul.ahm@example.com',    'MILK',      6.0,  0.97),
('Nandini Fresh Dairy',       '9876543211', 'nandini@example.com',     'MILK',      8.0,  0.91),
('Farmer Direct Milk',        '9876543212', 'farmermilk@example.com',  'MILK',      12.0, 0.78),
('Sahyadri Farms Fruits',     '9876543213', 'sahyadri@example.com',    'FRUIT',     10.0, 0.95),
('Mahagrapes Exporters',      '9876543214', 'mahagrapes@example.com',  'FRUIT',     14.0, 0.88),
('Local Orchard Collective',  '9876543215', 'localorch@example.com',   'FRUIT',     18.0, 0.72),
('Fresh Fields Vegetables',   '9876543216', 'freshfields@example.com', 'VEGETABLE', 8.0,  0.94),
('Rythu Bazaar Vendors',      '9876543217', 'rythu@example.com',       'VEGETABLE', 16.0, 0.83),
('Modern Bakery Supplies',    '9876543218', 'modernbake@example.com',  'BAKERY',    12.0, 0.96),
('Heritage Bakers Ahmedabad', '9876543219', 'heritage@example.com',    'BAKERY',    10.0, 0.89);

-- ─────────────────────────────────────────────────────────────────────────────
-- Products (mixture of expiry states to showcase dynamic pricing tiers)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO products (name, description, base_price, dynamic_price, category, expiry_date, stock_quantity, units_sold_total, units_wasted_total, supplier_id) VALUES

-- MILK PRODUCTS — varied expiry to show all pricing tiers
('Full Cream Milk 1L',    'Fresh full cream milk, 6% fat',       55.00,  55.00,  'MILK',      DATE_ADD(CURDATE(), INTERVAL 7  DAY), 80,  320, 15,  1),
('Toned Milk 500ml',      'Pasteurized toned milk, 3% fat',      28.00,  25.20,  'MILK',      DATE_ADD(CURDATE(), INTERVAL 4  DAY), 60,  210, 8,   1),
('Paneer 200g',           'Fresh cottage cheese, made daily',    85.00,  63.75,  'MILK',      DATE_ADD(CURDATE(), INTERVAL 2  DAY), 25,  95,  10,  2),
('Dahi (Curd) 500g',      'Set curd, probiotic rich',            45.00,  22.50,  'MILK',      DATE_ADD(CURDATE(), INTERVAL 1  DAY), 30,  180, 22,  2),
('Butter 100g',           'Unsalted white butter',               65.00,  65.00,  'MILK',      DATE_ADD(CURDATE(), INTERVAL 14 DAY), 40,  150, 3,   1),
('Ghee 250ml',            'Pure cow ghee, traditionally churned',285.00, 285.00, 'MILK',      DATE_ADD(CURDATE(), INTERVAL 90 DAY), 20,  85,  0,   1),

-- FRUITS — varied to show category search & pricing
('Alphonso Mangoes 1kg',  'Premium Ratnagiri Alphonso mangoes',  120.00, 120.00, 'FRUIT',     DATE_ADD(CURDATE(), INTERVAL 5  DAY), 50,  280, 25,  4),
('Bananas 1 dozen',       'Robusta bananas, ripe & sweet',       35.00,  24.50,  'FRUIT',     DATE_ADD(CURDATE(), INTERVAL 3  DAY), 100, 520, 40,  4),
('Watermelon (half)',     'Seedless watermelon, chilled',        60.00,  18.00,  'FRUIT',     DATE_ADD(CURDATE(), INTERVAL 0  DAY), 15,  90,  30,  5),
('Apple Shimla 500g',     'Crisp Himachali apples',              80.00,  80.00,  'FRUIT',     DATE_ADD(CURDATE(), INTERVAL 10 DAY), 60,  340, 12,  5),
('Grapes 500g',           'Green seedless grapes, Thompson',     70.00,  63.00,  'FRUIT',     DATE_ADD(CURDATE(), INTERVAL 4  DAY), 40,  160, 18,  4),
('Papaya (half)',         'Ripe red papaya, sliced',             45.00,  31.50,  'FRUIT',     DATE_ADD(CURDATE(), INTERVAL 2  DAY), 20,  75,  20,  6),

-- VEGETABLES — high-turnover, varied wastage for analytics demo
('Tomatoes 1kg',          'Farm-fresh red tomatoes',             30.00,  15.00,  'VEGETABLE', DATE_ADD(CURDATE(), INTERVAL 0  DAY), 40,  850, 95,  7),
('Spinach 250g',          'Fresh palak leaves, washed',          25.00,  12.50,  'VEGETABLE', DATE_ADD(CURDATE(), INTERVAL 1  DAY), 30,  420, 65,  7),
('Onions 1kg',            'Nashik red onions, firm',             35.00,  35.00,  'VEGETABLE', DATE_ADD(CURDATE(), INTERVAL 20 DAY), 200, 1200, 30, 8),
('Potatoes 1kg',          'Agra potatoes, washed grade A',       30.00,  30.00,  'VEGETABLE', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 150, 980, 20,  8),
('Capsicum 500g',         'Green capsicum, firm and glossy',     40.00,  32.00,  'VEGETABLE', DATE_ADD(CURDATE(), INTERVAL 3  DAY), 45,  230, 35,  7),
('Coriander 100g',        'Fresh dhania leaves, aromatic',       15.00,   7.50,  'VEGETABLE', DATE_ADD(CURDATE(), INTERVAL 1  DAY), 60,  680, 110, 7),

-- BAKERY PRODUCTS
('Whole Wheat Bread',     'Brown bread, 400g loaf, no maida',    45.00,  31.50,  'BAKERY',    DATE_ADD(CURDATE(), INTERVAL 2  DAY), 35,  420, 45,  9),
('Croissant (4 pcs)',     'Butter croissant, baked fresh daily',  55.00, 27.50,  'BAKERY',    DATE_ADD(CURDATE(), INTERVAL 1  DAY), 20,  180, 40,  9),
('Multigrain Bread',      'Seeds & grains loaf, 400g',           55.00,  55.00,  'BAKERY',    DATE_ADD(CURDATE(), INTERVAL 5  DAY), 25,  210, 15,  10),
('Pav (8 pieces)',        'Soft dinner rolls, Mumbai style',     30.00,  21.00,  'BAKERY',    DATE_ADD(CURDATE(), INTERVAL 2  DAY), 50,  650, 80,  9),
('Cream Rolls (2 pcs)',   'Flaky pastry with fresh cream fill',   40.00, 12.00,  'BAKERY',    DATE_ADD(CURDATE(), INTERVAL 0  DAY), 10,  95,  55,  10);

-- ─────────────────────────────────────────────────────────────────────────────
-- Historical wastage records (for analytics report demo)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO wastage_records (product_id, product_name, units_wasted, value_wasted, recorded_on, reason) VALUES
(13, 'Tomatoes 1kg',     20, 600.00,  DATE_SUB(CURDATE(), INTERVAL 2  DAY), 'EXPIRY'),
(18, 'Coriander 100g',   30, 450.00,  DATE_SUB(CURDATE(), INTERVAL 2  DAY), 'EXPIRY'),
(20, 'Croissant (4 pcs)',8,  440.00,  DATE_SUB(CURDATE(), INTERVAL 3  DAY), 'EXPIRY'),
(3,  'Paneer 200g',      5,  425.00,  DATE_SUB(CURDATE(), INTERVAL 5  DAY), 'EXPIRY'),
(22, 'Pav (8 pieces)',   25, 750.00,  DATE_SUB(CURDATE(), INTERVAL 1  DAY), 'EXPIRY'),
(9,  'Watermelon (half)',6,  360.00,  DATE_SUB(CURDATE(), INTERVAL 4  DAY), 'EXPIRY'),
(14, 'Spinach 250g',     15, 375.00,  DATE_SUB(CURDATE(), INTERVAL 1  DAY), 'EXPIRY'),
(23, 'Cream Rolls (2 pcs)',5,200.00,  DATE_SUB(CURDATE(), INTERVAL 7  DAY), 'EXPIRY');

-- ─────────────────────────────────────────────────────────────────────────────
-- Historical orders (for demand forecasting demo — last 30 days)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO orders (user_id, total_amount, payment_method, status, placed_at) VALUES
(3, 110.00, 'WALLET', 'DELIVERED', DATE_SUB(NOW(), INTERVAL 1  DAY)),
(3, 55.00,  'CASH',   'DELIVERED', DATE_SUB(NOW(), INTERVAL 2  DAY)),
(4, 280.00, 'WALLET', 'DELIVERED', DATE_SUB(NOW(), INTERVAL 3  DAY)),
(3, 85.00,  'WALLET', 'DELIVERED', DATE_SUB(NOW(), INTERVAL 4  DAY)),
(4, 150.00, 'CASH',   'DELIVERED', DATE_SUB(NOW(), INTERVAL 5  DAY)),
(3, 200.00, 'WALLET', 'DELIVERED', DATE_SUB(NOW(), INTERVAL 7  DAY)),
(4, 95.00,  'WALLET', 'DELIVERED', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(3, 175.00, 'CASH',   'DELIVERED', DATE_SUB(NOW(), INTERVAL 14 DAY));

INSERT INTO order_lines (order_id, product_id, product_name, quantity, price_per_unit) VALUES
(1, 1,  'Full Cream Milk 1L',   2, 55.00),
(2, 13, 'Tomatoes 1kg',         1, 30.00),
(2, 15, 'Onions 1kg',           1, 35.00),
(3, 7,  'Alphonso Mangoes 1kg', 2, 120.00),
(3, 10, 'Apple Shimla 500g',    1, 80.00),
(4, 3,  'Paneer 200g',          1, 85.00),
(5, 15, 'Onions 1kg',           2, 35.00),
(5, 16, 'Potatoes 1kg',         2, 30.00),
(6, 1,  'Full Cream Milk 1L',   2, 55.00),
(6, 5,  'Butter 100g',          1, 65.00),
(6, 19, 'Whole Wheat Bread',    1, 45.00),
(7, 7,  'Alphonso Mangoes 1kg', 1, 120.00),
(8, 13, 'Tomatoes 1kg',         2, 30.00),
(8, 17, 'Capsicum 500g',        1, 40.00);
