-- Structure Types
INSERT INTO structure_type (id, type) VALUES
                                          (1, 'Concert Hall'),
                                          (2, 'Theater'),
                                          (3, 'Stadium'),
                                          (4, 'Conference Center'),
                                          (5, 'Cinema');

-- Structures
INSERT INTO structure (id, name, description) VALUES
                                                  (1, 'Le Zénith', 'Large concert hall with capacity for 6000 people'),
                                                  (2, 'National Theater', 'Historic theater in city center'),
                                                  (3, 'Olympic Stadium', 'Large stadium for sports events and concerts'),
                                                  (4, 'Congress Center', 'Modular space for conferences and exhibitions');

-- Structure_Structure_Type associations
INSERT INTO structure_structure_type (structure_id, type_id) VALUES
                                                                 (1, 1), -- Le Zénith is a Concert Hall
                                                                 (2, 2), -- National Theater is a Theater
                                                                 (3, 3), -- Olympic Stadium is a Stadium
                                                                 (4, 4); -- Congress Center is a Conference Center

-- Addresses
INSERT INTO address (id, country, city, postal_code, street, number, structure_id) VALUES
                                                                                       (1, 'France', 'Paris', '75019', 'Avenue Jean Jaurès', '211', 1),
                                                                                       (2, 'France', 'Lyon', '69001', 'Rue de la République', '15', 2),
                                                                                       (3, 'France', 'Marseille', '13008', 'Boulevard Michelet', '33', 3),
                                                                                       (4, 'France', 'Bordeaux', '33000', 'Cours du Chapeau Rouge', '42', 4);

-- Locations
INSERT INTO location (id, name, structure_id) VALUES
                                                  (1, 'Main Hall', 1),
                                                  (2, 'Small Hall', 1),
                                                  (3, 'Main Stage', 2),
                                                  (4, 'Central Field', 3),
                                                  (5, 'Conference Room A', 4),
                                                  (6, 'Conference Room B', 4);

-- Users - Spectators
INSERT INTO user (id, email, password, first_name, last_name, last_connection, registration_date, role, structure_id, created_at, updated_at) VALUES
                                                                                                                                                  (1, 'john.doe@example.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'John', 'Doe', '2025-04-12 14:30:00', '2024-01-15', 'SPECTATOR', NULL, NOW(), NOW()),
                                                                                                                                                  (2, 'jane.smith@example.com', '$2a$10$h6UJQ7PB.dRLC9dGK59vlu5KY05617yoZdOVUcRPs7VWyR3cX/YmG', 'Jane', 'Smith', '2025-04-13 09:45:00', '2024-02-20', 'SPECTATOR', NULL, NOW(), NOW()),
                                                                                                                                                  (3, 'mike.brown@example.com', '$2a$10$jKJjGefU5P0ViC3qKP7VhuGKNGHJ/FKepDnI8wFy66CxgqnX6wTvW', 'Mike', 'Brown', '2025-04-10 18:20:00', '2024-01-05', 'SPECTATOR', NULL, NOW(), NOW());

-- Users - Staff
INSERT INTO user (id, email, password, first_name, last_name, last_connection, registration_date, role, structure_id, created_at, updated_at) VALUES
                                                                                                                                                  (4, 'reservation.zenith@example.com', '$2a$10$B7o8Bwn5WK.4s5Vh5QoFgeEWR3zT1JyZ1D2jRNWFtWXE94Ou1G7ti', 'Thomas', 'Grand', '2025-04-13 10:30:00', '2023-11-05', 'RESERVATION_SERVICE', 1, NOW(), NOW()),
                                                                                                                                                  (5, 'reservation.theater@example.com', '$2a$10$9CZmWv7s2UHHEcKnw4qVlODQ5ulVhQ9LpF3Kvr9iE5lHT3Qi9U0sG', 'Julie', 'Moreau', '2025-04-12 16:45:00', '2023-12-10', 'RESERVATION_SERVICE', 2, NOW(), NOW()),
                                                                                                                                                  (6, 'org.zenith@example.com', '$2a$10$YqSafX7PNkV8xjOGPr4W8eBw1sX8GQs/nU4SKwmRb3J5TwALIcFuG', 'François', 'Petit', '2025-04-13 14:00:00', '2023-10-15', 'ORGANIZATION_SERVICE', 1, NOW(), NOW()),
                                                                                                                                                  (7, 'admin.zenith@example.com', '$2a$10$RDXsP1vt9a/ViEbXXn0x/.Fye6P4e9ff3BDeXDjM3GUMHr7bH7bRC', 'Antoine', 'Leroy', '2025-04-13 11:20:00', '2023-08-01', 'STRUCTURE_ADMINISTRATOR', 1, NOW(), NOW()),
                                                                                                                                                  (8, 'admin.theater@example.com', '$2a$10$kJ9FKS5a2MkWuL0r1g1ZfuNTCY5rJjtOkSGwYL30CzhW6KXrD2cFK', 'Isabelle', 'Girard', '2025-04-12 15:30:00', '2023-07-15', 'STRUCTURE_ADMINISTRATOR', 2, NOW(), NOW())