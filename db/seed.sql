USE javaweb_shop;

INSERT INTO users (username, password_hash, email, phone, role, status)
VALUES
  ('admin', '$2a$10$replace_with_bcrypt_hash', 'admin@example.com', '13800000000', 'MERCHANT', 1),
  ('demo', '$2a$10$replace_with_bcrypt_hash', 'demo@example.com', '13900000000', 'CUSTOMER', 1);

INSERT INTO categories (name, parent_id, sort_order, status)
VALUES
  ('女装/男装/配饰', NULL, 1, 1),
  ('家具/家装/家居', NULL, 2, 1),
  ('家电/手机/数码', NULL, 3, 1),
  ('女鞋/男鞋/运动', NULL, 4, 1),
  ('母婴/童装/潮玩', NULL, 5, 1),
  ('美妆/洗护/宠物', NULL, 6, 1),
  ('汽车/车载/出行', NULL, 7, 1),
  ('食品/生鲜/健康', NULL, 8, 1),
  ('电脑/办公/文具', NULL, 9, 1),
  ('娱乐/图书/鲜花', NULL, 10, 1),
  ('腕表/珠宝/眼镜', NULL, 11, 1);

INSERT INTO products (category_id, merchant_id, name, price, stock, status, description, image_url)
VALUES
  ((SELECT id FROM categories WHERE name = '家电/手机/数码'), (SELECT id FROM users WHERE username = 'admin'), 'Bluetooth Headphones', 199.00, 100, 1, 'Wireless over-ear headphones.', 'images/headphones.jpg'),
  ((SELECT id FROM categories WHERE name = '电脑/办公/文具'), (SELECT id FROM users WHERE username = 'admin'), 'Lightweight Laptop 14', 3999.00, 50, 1, '14-inch portable laptop.', 'images/laptop14.jpg'),
  ((SELECT id FROM categories WHERE name = '娱乐/图书/鲜花'), (SELECT id FROM users WHERE username = 'admin'), 'Programming Basics', 59.00, 200, 1, 'Beginner friendly programming book.', 'images/book1.jpg'),
  ((SELECT id FROM categories WHERE name = '娱乐/图书/鲜花'), (SELECT id FROM users WHERE username = 'admin'), 'Sci-Fi Novel', 45.00, 150, 1, 'Classic science fiction novel.', 'images/book2.jpg');
