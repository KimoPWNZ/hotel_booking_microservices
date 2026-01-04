-- Insert seed users (password for all: "password" - BCrypt hashed)
INSERT INTO users(id, username, password, role, created_at) VALUES
    (1, 'admin', '$2a$10$q3bONUpHG7Y82S2uT/YJyO1sYwL6YHnHYF/eP0T5TBuuSSMzdON3m', 'ROLE_ADMIN', CURRENT_TIMESTAMP()),
    (2, 'user', '$2a$10$q3bONUpHG7Y82S2uT/YJyO1sYwL6YHnHYF/eP0T5TBuuSSMzdON3m', 'ROLE_USER', CURRENT_TIMESTAMP()),
    (3, 'user2', '$2a$10$q3bONUpHG7Y82S2uT/YJyO1sYwL6YHnHYF/eP0T5TBuuSSMzdON3m', 'ROLE_USER', CURRENT_TIMESTAMP()),
    (4, 'user3', '$2a$10$q3bONUpHG7Y82S2uT/YJyO1sYwL6YHnHYF/eP0T5TBuuSSMzdON3m', 'ROLE_USER', CURRENT_TIMESTAMP());
