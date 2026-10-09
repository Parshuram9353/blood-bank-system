-- Seed Data for Blood Bank Management System (MySQL reference)
USE blood_bank;

-- 1. Insert Roles
INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN') ON DUPLICATE KEY UPDATE name=name;
INSERT INTO roles (id, name) VALUES (2, 'ROLE_STAFF') ON DUPLICATE KEY UPDATE name=name;
INSERT INTO roles (id, name) VALUES (3, 'ROLE_DONOR') ON DUPLICATE KEY UPDATE name=name;

-- 2. Insert Default Users (Passwords encrypted with BCrypt)
-- Admin Account (admin / admin123)
INSERT INTO users (id, username, password, email, enabled, created_at)
VALUES (1, 'admin', '$2a$10$tZg/uG2q8x1m9v6D.XNfUu47l73t37N3H.nZlH38yL0Q7eC1l9X5q', 'admin@bloodbank.org', 1, NOW())
ON DUPLICATE KEY UPDATE username=username;

-- Staff Account (staff / staff123)
INSERT INTO users (id, username, password, email, enabled, created_at)
VALUES (2, 'staff', '$2a$10$eE61H7rIoeW5r15yW6H4feLd.g8u7pQe5w8H0/3E0D1zV2l8eN3Kq', 'staff@bloodbank.org', 1, NOW())
ON DUPLICATE KEY UPDATE username=username;

-- Map Users to Roles
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1) ON DUPLICATE KEY UPDATE user_id=user_id;
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2) ON DUPLICATE KEY UPDATE user_id=user_id;

-- 3. Insert Hospitals
INSERT INTO hospitals (id, name, address, contact_number, email, created_at)
VALUES (1, 'City General Hospital', '102 Medical Park Drive, Metro City', '9876543210', 'info@cityhospital.org', NOW())
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO hospitals (id, name, address, contact_number, email, created_at)
VALUES (2, 'St. Jude Research Hospital', '501 St. Jude Place, Memphis', '8882772589', 'contact@stjude.org', NOW())
ON DUPLICATE KEY UPDATE name=name;
