-- =====================================================================
--  Sample records (works on PostgreSQL and MySQL).
--  NOTE: the application inserts exactly this data automatically on the first start
--  (see config/DataSeeder.java), so running this file is optional.
-- =====================================================================

INSERT INTO menu_items (item_type, name, description, price, category, available, vegetarian, spice_level, volume_ml, served_cold) VALUES
('FOOD',     'Paneer Tikka',         'Grilled cottage cheese with spices',  220, 'Starter',     true, true,  'MEDIUM', NULL, NULL),
('FOOD',     'Chicken 65',           'Spicy deep-fried chicken',            240, 'Starter',     true, false, 'HOT',    NULL, NULL),
('FOOD',     'Veg Spring Roll',      'Crispy rolls with vegetable filling', 160, 'Starter',     true, true,  'MILD',   NULL, NULL),
('FOOD',     'Butter Chicken',       'Chicken in creamy tomato gravy',      320, 'Main Course', true, false, 'MILD',   NULL, NULL),
('FOOD',     'Paneer Butter Masala', 'Paneer in rich tomato gravy',         280, 'Main Course', true, true,  'MILD',   NULL, NULL),
('FOOD',     'Veg Biryani',          'Basmati rice with vegetables',        210, 'Main Course', true, true,  'MEDIUM', NULL, NULL),
('FOOD',     'Chicken Biryani',      'Hyderabadi dum biryani',              260, 'Main Course', true, false, 'HOT',    NULL, NULL),
('FOOD',     'Butter Naan',          'Tandoor bread with butter',            50, 'Breads',      true, true,  'MILD',   NULL, NULL),
('FOOD',     'Gulab Jamun',          'Two pieces, served warm',              90, 'Dessert',     true, true,  'MILD',   NULL, NULL),
('BEVERAGE', 'Masala Chai',          'Indian spiced tea',                    40, 'Drinks',      true, NULL,  NULL,     150,  false),
('BEVERAGE', 'Filter Coffee',        'South Indian filter coffee',           50, 'Drinks',      true, NULL,  NULL,     150,  false),
('BEVERAGE', 'Sweet Lassi',          'Chilled yoghurt drink',                80, 'Drinks',      true, NULL,  NULL,     300,  true),
('BEVERAGE', 'Fresh Lime Soda',      'Sweet or salted',                      70, 'Drinks',      true, NULL,  NULL,     300,  true);

INSERT INTO customer_orders (customer_name, table_number, status, order_time, subtotal) VALUES
('Arjun',   3, 'PAID',   '2026-10-20 12:30:00', 1060),   -- 1 x 220 + 2 x 320 + 4 x 50
('Meera',   5, 'SERVED', '2026-10-20 13:05:00',  580),   -- 2 x 210 + 2 x 80
('Karthik', 1, 'PLACED', '2026-10-20 13:20:00',  170);   -- 2 x 40 + 1 x 90

INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price) VALUES
(1, 1, 1, 220), (1, 4, 2, 320), (1, 8, 4, 50),
(2, 6, 2, 210), (2, 12, 2, 80),
(3, 10, 2, 40), (3, 9, 1, 90);
