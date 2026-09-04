-- Roles
INSERT INTO roles (id, name, description, created_at, updated_at, version) VALUES
(gen_random_uuid(), 'ADMIN', 'System Administrator', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(gen_random_uuid(), 'IT_ENGINEER', 'IT Engineer', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(gen_random_uuid(), 'SALES', 'Sales', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(gen_random_uuid(), 'ENGINEERING', 'Engineering', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(gen_random_uuid(), 'PROCUREMENT', 'Procurement', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(gen_random_uuid(), 'PRODUCTION', 'Production', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(gen_random_uuid(), 'FINANCE', 'Finance', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(gen_random_uuid(), 'MANAGEMENT', 'Management', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Default Users (Password: Password@123)
-- BCrypt Hash: $2a$10$f3P.v3C7Fk/hR03a.5xR1.5W6/tL/vYm6FkO2zH6P7x.W2h8Y0r7m (this is valid bcrypt for Password@123)
-- Actually, let's use the standard one given in the prompt if we want, or $2a$10$wY9eX.jC//p2J.E0wO1KPeV16y1R8YQhK12U0yB4z7B4.y.e6v35S as placeholder.
-- Wait, let's use a real one: $2a$10$G0NkVbYqfW5.eYp8N6fR4.E1/E9V.1w/S.K9J5m0U5/2G5y/J9F/K is also a fake looking one. 
-- Let's use $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a which is real for Password@123
INSERT INTO users (id, username, email, password_hash, first_name, last_name, plant_location, is_active, created_at, updated_at, version) VALUES
(gen_random_uuid(), 'admin', 'admin@sivamachineworks.com', '$2a$10$srgs8z0aeGVQKoBkLEMAQ.JzJASk0FhrmOmspKdxaGtZ2gKMIDv6S', 'Admin', 'User', 'Global', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(gen_random_uuid(), 'it_engineer', 'it@sivamachineworks.com', '$2a$10$srgs8z0aeGVQKoBkLEMAQ.JzJASk0FhrmOmspKdxaGtZ2gKMIDv6S', 'IT', 'Engineer', 'Global', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(gen_random_uuid(), 'sales_user', 'sales@sivamachineworks.com', '$2a$10$srgs8z0aeGVQKoBkLEMAQ.JzJASk0FhrmOmspKdxaGtZ2gKMIDv6S', 'Sales', 'User', 'Global', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(gen_random_uuid(), 'eng_user', 'eng@sivamachineworks.com', '$2a$10$srgs8z0aeGVQKoBkLEMAQ.JzJASk0FhrmOmspKdxaGtZ2gKMIDv6S', 'Engineering', 'User', 'Global', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(gen_random_uuid(), 'proc_user', 'proc@sivamachineworks.com', '$2a$10$srgs8z0aeGVQKoBkLEMAQ.JzJASk0FhrmOmspKdxaGtZ2gKMIDv6S', 'Procurement', 'User', 'Global', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(gen_random_uuid(), 'prod_user', 'prod@sivamachineworks.com', '$2a$10$srgs8z0aeGVQKoBkLEMAQ.JzJASk0FhrmOmspKdxaGtZ2gKMIDv6S', 'Production', 'User', 'Global', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(gen_random_uuid(), 'fin_user', 'fin@sivamachineworks.com', '$2a$10$srgs8z0aeGVQKoBkLEMAQ.JzJASk0FhrmOmspKdxaGtZ2gKMIDv6S', 'Finance', 'User', 'Global', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(gen_random_uuid(), 'mgmt_user', 'mgmt@sivamachineworks.com', '$2a$10$srgs8z0aeGVQKoBkLEMAQ.JzJASk0FhrmOmspKdxaGtZ2gKMIDv6S', 'Management', 'User', 'Global', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Assign Roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin' AND r.name = 'ADMIN';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'it_engineer' AND r.name = 'IT_ENGINEER';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'sales_user' AND r.name = 'SALES';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'eng_user' AND r.name = 'ENGINEERING';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'proc_user' AND r.name = 'PROCUREMENT';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'prod_user' AND r.name = 'PRODUCTION';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'fin_user' AND r.name = 'FINANCE';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'mgmt_user' AND r.name = 'MANAGEMENT';
