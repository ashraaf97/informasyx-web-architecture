-- Insert test data for admin user (using camelCase column names as per PhysicalNamingStrategyStandardImpl)
-- Using auto-generated IDs instead of hardcoded values to avoid conflicts
INSERT INTO Person (firstName, lastName, email, phoneNumber, address) 
VALUES ('Admin', 'User', 'admin@example.com', '+1234567890', 'System Administrator');

-- Insert admin user with BCrypt hashed password for 'admin'
-- Password hash for 'admin' using BCrypt (generated with strength 10)
-- Use a subquery to get the person_id from the inserted person record
INSERT INTO app_user (username, password, person_id, active, email_verified, role) 
VALUES ('admin', '$2a$10$dXJ3SW6G7P8LKxMmrhdQfOOF9THkYJsHfYYF.E4QPe6H./WJ.FFqG', 
        (SELECT id FROM Person WHERE email = 'admin@example.com'), true, true, 'ADMIN');