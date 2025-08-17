-- Insert test data for admin user
INSERT INTO person (id, first_name, last_name, email, phone_number, address) 
VALUES (1, 'Admin', 'User', 'admin@example.com', '+1234567890', 'System Administrator');

-- Insert admin user with BCrypt hashed password for 'admin'
-- Password hash for 'admin' using BCrypt (generated with strength 10)
INSERT INTO app_user (id, username, password, person_id, active, roles) 
VALUES (1, 'admin', '$2a$10$dXJ3SW6G7P8LKxMmrhdQfOOF9THkYJsHfYYF.E4QPe6H./WJ.FFqG', 1, true, 'ADMIN,USER');