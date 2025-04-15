-- Insertion des types de structure
INSERT INTO structure_type (id, type)
VALUES (1, 'Cinéma'),
       (2, 'Théâtre'),
       (3, 'Salle de concert'),
       (4, 'Stade'),
       (5, 'Musée'),
       (6, 'Centre de congrès'),
       (7, 'Galerie d''art'),
       (8, 'Amphithéâtre'),
       (9, 'Opéra'),
       (10, 'Festival');

-- Insertion des structures
INSERT INTO structure (id, name, description)
VALUES (1, 'Cinéma Paradiso', 'Cinéma indépendant avec 5 salles et une programmation variée'),
       (2, 'Théâtre National', 'Scène principale de 800 places proposant des pièces classiques et contemporaines'),
       (3, 'Arena Festival', 'Salle de concert en plein air avec capacité de 5000 personnes'),
       (4, 'Le Grand Rex', 'Cinéma historique avec la plus grande salle d''Europe'),
       (5, 'Opéra Garnier', 'Salle d''opéra prestigieuse au cœur de Paris'),
       (6, 'Zénith de Lille', 'Grande salle de spectacle pour concerts et événements'),
       (7, 'Théâtre de la Ville', 'Théâtre municipal proposant une programmation diversifiée'),
       (8, 'Palais des Congrès', 'Centre de congrès accueillant conférences et spectacles'),
       (9, 'Stade Pierre-Mauroy', 'Stade multifonctionnel avec toit rétractable'),
       (10, 'Musée d''Art Moderne', 'Espace d''exposition d''art contemporain et moderne');

-- Liaison Structure <=> StructureType
INSERT INTO structure_structure_type (structure_id, type_id)
VALUES (1, 1),
       (2, 2),
       (3, 3),
       (4, 1),
       (5, 9),
       (6, 3),
       (7, 2),
       (8, 6),
       (9, 4),
       (10, 5),
       (1, 7),
       (2, 8),
       (3, 10);

-- Insertion des adresses
INSERT INTO address (id, country, city, postal_code, street, number, structure_id)
VALUES (1, 'France', 'Paris', '75001', 'Rue de la Paix', '8', 1),
       (2, 'France', 'Lyon', '69002', 'Quai Charles de Gaulle', '12', 2),
       (3, 'France', 'Marseille', '13002', 'Promenade Robert Laffont', '1', 3),
       (4, 'France', 'Paris', '75002', 'Boulevard Poissonnière', '1', 4),
       (5, 'France', 'Paris', '75009', 'Place de l''Opéra', '8', 5),
       (6, 'France', 'Lille', '59000', 'Boulevard des Cités Unies', '1', 6),
       (7, 'France', 'Paris', '75004', 'Place du Châtelet', '2', 7),
       (8, 'France', 'Paris', '75017', 'Boulevard Malesherbes', '17', 8),
       (9, 'France', 'Villeneuve-d''Ascq', '59650', 'Boulevard de Tournai', '261', 9),
       (10, 'France', 'Paris', '75016', 'Avenue du Président Wilson', '11', 10);

-- Insertion des utilisateurs (rôles: SPECTATOR et STRUCTURE_ADMINISTRATOR)
INSERT INTO user (id, email, password, first_name, last_name, role, registration_date, last_connection, structure_id)
VALUES
-- Administrateurs de structure
(1, 'admin.cinema@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Jean', 'Dupont',
 'STRUCTURE_ADMINISTRATOR', NOW(), NOW(), 1),
(2, 'admin.theatre@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Marie', 'Laurent',
 'STRUCTURE_ADMINISTRATOR', NOW(), NOW(), 2),
(3, 'admin.arena@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Pierre', 'Martin',
 'STRUCTURE_ADMINISTRATOR', NOW(), NOW(), 3),
(4, 'admin.rex@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Sophie', 'Dubois',
 'STRUCTURE_ADMINISTRATOR', NOW(), NOW(), 4),
(5, 'admin.opera@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Thomas', 'Leroy',
 'STRUCTURE_ADMINISTRATOR', NOW(), NOW(), 5),
-- Spectateurs
(6, 'spectateur1@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Lucie', 'Moreau',
 'SPECTATOR', NOW(), NOW(), NULL),
(7, 'spectateur2@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Antoine', 'Bernard',
 'SPECTATOR', NOW(), NOW(), NULL),
(8, 'spectateur3@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Emma', 'Petit',
 'SPECTATOR', NOW(), NOW(), NULL),
(9, 'spectateur4@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Hugo', 'Robert',
 'SPECTATOR', NOW(), NOW(), NULL),
(10, 'spectateur5@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Chloé', 'Simon',
 'SPECTATOR', NOW(), NOW(), NULL),
(11, 'spectateur6@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Louis', 'Michel',
 'SPECTATOR', NOW(), NOW(), NULL),
(12, 'spectateur7@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Camille', 'Lefebvre',
 'SPECTATOR', NOW(), NOW(), NULL),
(13, 'spectateur8@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Jules', 'Garcia',
 'SPECTATOR', NOW(), NOW(), NULL),
(14, 'spectateur9@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Léa', 'Roux',
 'SPECTATOR', NOW(), NOW(), NULL),
(15, 'spectateur10@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Théo', 'Fournier',
 'SPECTATOR', NOW(), NOW(), NULL);

-- Insertion des locations (salles/espaces)
INSERT INTO location (id, name, structure_id)
VALUES (1, 'Salle 1', 1),
       (2, 'Salle 2', 1),
       (3, 'Salle 3', 1),
       (4, 'Grande Scène', 2),
       (5, 'Petite Scène', 2),
       (6, 'Scène Principale', 3),
       (7, 'Zone VIP', 3),
       (8, 'Salle Prestige', 4),
       (9, 'Salle Classique', 4),
       (10, 'Grande Salle', 5),
       (11, 'Foyer', 5),
       (12, 'Parterre', 6),
       (13, 'Balcon', 6),
       (14, 'Salle A', 7),
       (15, 'Salle B', 7),
       (16, 'Auditorium', 8),
       (17, 'Salle de Conférence', 8),
       (18, 'Terrain', 9),
       (19, 'Tribune Nord', 9),
       (20, 'Galerie Moderne', 10),
       (21, 'Galerie Contemporaine', 10);

-- Insertion des placements
INSERT INTO placement (id, name, price, capacity, placement_type, location_id)
VALUES
-- Cinéma Paradiso
(1, 'Rang A', 1500, 50, 'SEAT_PLACEMENT', 1),
(2, 'Rang B', 1200, 60, 'SEAT_PLACEMENT', 1),
(3, 'Rang C', 1000, 70, 'SEAT_PLACEMENT', 1),
(4, 'Rang A', 1500, 50, 'SEAT_PLACEMENT', 2),
(5, 'Rang B', 1200, 60, 'SEAT_PLACEMENT', 2),
-- Théâtre National
(6, 'Orchestre', 2500, 300, 'FREE_PLACEMENT', 4),
(7, 'Balcon', 1800, 200, 'SEAT_PLACEMENT', 4),
(8, 'Loge', 3000, 100, 'SEAT_PLACEMENT', 4),
(9, 'Parterre', 1500, 150, 'FREE_PLACEMENT', 5),
-- Arena Festival
(10, 'Fosse', 3000, 2000, 'FREE_PLACEMENT', 6),
(11, 'Gradin', 3500, 2500, 'SEAT_PLACEMENT', 6),
(12, 'Carré Or', 5000, 500, 'SEAT_PLACEMENT', 7),
-- Le Grand Rex
(13, 'Premium', 2000, 200, 'SEAT_PLACEMENT', 8),
(14, 'Standard', 1500, 500, 'SEAT_PLACEMENT', 8),
(15, 'Économique', 1000, 300, 'SEAT_PLACEMENT', 9),
-- Opéra Garnier
(16, 'Premier Balcon', 4000, 150, 'SEAT_PLACEMENT', 10),
(17, 'Parterre', 3500, 300, 'SEAT_PLACEMENT', 10),
(18, 'Amphithéâtre', 2000, 200, 'SEAT_PLACEMENT', 10),
-- Zénith de Lille
(19, 'Catégorie 1', 4500, 1000, 'SEAT_PLACEMENT', 12),
(20, 'Catégorie 2', 3500, 1500, 'SEAT_PLACEMENT', 12),
(21, 'Catégorie 3', 2500, 2000, 'SEAT_PLACEMENT', 13),
-- Théâtre de la Ville
(22, 'Fauteuil', 2200, 300, 'SEAT_PLACEMENT', 14),
(23, 'Strapontin', 1500, 100, 'SEAT_PLACEMENT', 14),
(24, 'Placement Libre', 1800, 200, 'FREE_PLACEMENT', 15),
-- Palais des Congrès
(25, 'Section A', 3000, 500, 'SEAT_PLACEMENT', 16),
(26, 'Section B', 2500, 500, 'SEAT_PLACEMENT', 16),
(27, 'Section C', 2000, 300, 'SEAT_PLACEMENT', 17),
-- Stade Pierre-Mauroy
(28, 'Tribune Présidentielle', 8000, 1000, 'SEAT_PLACEMENT', 18),
(29, 'Tribune Latérale', 6000, 5000, 'SEAT_PLACEMENT', 18),
(30, 'Virage', 4000, 10000, 'SEAT_PLACEMENT', 19),
-- Musée d'Art Moderne
(31, 'Exposition Permanente', 1200, 200, 'FREE_PLACEMENT', 20),
(32, 'Exposition Temporaire', 1500, 150, 'FREE_PLACEMENT', 21);


-- Insertion des événements
INSERT INTO event (id, name, start_date, end_date, description, image_url, status, category, structure_id)
VALUES
-- Cinéma Paradiso (Structure ID: 1)
(1, 'Festival du Film Indépendant', '2025-06-15 18:00:00', '2025-06-20 23:00:00',
 'Une semaine dédiée au cinéma indépendant avec projections et rencontres avec les réalisateurs',
 'https://example.com/images/indie_film_fest.jpg', 'PUBLISHED', 'FESTIVAL', 1),
(2, 'Nuit du Cinéma Fantastique', '2025-07-31 20:00:00', '2025-08-01 06:00:00',
 'Marathon de films fantastiques toute la nuit avec animations thématiques',
 'https://example.com/images/fantasy_night.jpg', 'PUBLISHED', 'OTHER', 1),
(3, 'Rétrospective Hitchcock', '2025-09-10 14:00:00', '2025-09-15 22:00:00',
 'Projection des chefs-d\'œuvre d\'Alfred Hitchcock avec conférences', 'https://example.com/images/hitchcock.jpg',
 'DRAFT', 'EXHIBITION', 1),

-- Théâtre National (Structure ID: 2)
(4, 'Le Misanthrope', '2025-05-25 19:30:00', '2025-05-25 22:00:00',
 'Pièce classique de Molière dans une mise en scène contemporaine', 'https://example.com/images/misanthrope.jpg',
 'PUBLISHED', 'THEATRE', 2),
(5, 'Hamlet', '2025-06-10 20:00:00', '2025-06-10 23:30:00',
 'La tragédie de Shakespeare revisitée par un metteur en scène de renom', 'https://example.com/images/hamlet.jpg',
 'PUBLISHED', 'THEATRE', 2),
(6, 'Festival de Théâtre Contemporain', '2025-08-01 10:00:00', '2025-08-07 23:00:00',
 'Une semaine dédiée aux nouvelles écritures théâtrales', 'https://example.com/images/contemporary_theatre.jpg',
 'DRAFT', 'FESTIVAL', 2),

-- Arena Festival (Structure ID: 3)
(7, 'Concert de Rock Symphonique', '2025-07-15 20:00:00', '2025-07-15 23:30:00',
 'Fusion entre musique classique et rock avec orchestre complet', 'https://example.com/images/symphonic_rock.jpg',
 'PUBLISHED', 'CONCERT', 3),
(8, 'Festival Électro Plein Air', '2025-08-20 16:00:00', '2025-08-21 02:00:00',
 'Les meilleurs DJs internationaux pour une nuit électro inoubliable',
 'https://example.com/images/electro_festival.jpg', 'PUBLISHED', 'FESTIVAL', 3),
(9, 'Concert Caritatif', '2025-09-05 19:00:00', '2025-09-05 23:00:00',
 'Concert au profit d\'associations humanitaires avec artistes locaux et internationaux',
 'https://example.com/images/charity_concert.jpg', 'DRAFT', 'CONCERT', 3),

-- Le Grand Rex (Structure ID: 4)
(10, 'Avant-première Star Wars', '2025-06-01 20:00:00', '2025-06-01 23:00:00',
 'Projection en avant-première du nouveau film Star Wars avec animations thématiques',
 'https://example.com/images/star_wars.jpg', 'PUBLISHED', 'OTHER', 4),
(11, 'Marathon Marvel', '2025-07-04 10:00:00', '2025-07-04 23:59:00',
 'Projection de la saga Marvel avec pauses animations et quiz', 'https://example.com/images/marvel_marathon.jpg',
 'PUBLISHED', 'OTHER', 4),
(12, 'Ciné-Concert Film Muet', '2025-08-15 19:30:00', '2025-08-15 22:00:00',
 'Projection d\'un film muet accompagné par un orchestre en direct', 'https://example.com/images/silent_movie.jpg',
 'DRAFT', 'CONCERT', 4),

-- Opéra Garnier (Structure ID: 5)
(13, 'La Traviata', '2025-06-20 20:00:00', '2025-06-20 23:00:00', 'Opéra de Verdi dans une mise en scène somptueuse',
 'https://example.com/images/traviata.jpg', 'PUBLISHED', 'THEATRE', 5),
(14, 'Ballet Le Lac des Cygnes', '2025-07-15 19:30:00', '2025-07-15 22:00:00',
 'Ballet classique de Tchaïkovski interprété par les danseurs étoiles', 'https://example.com/images/swan_lake.jpg',
 'PUBLISHED', 'OTHER', 5),
(15, 'Concert Symphonique', '2025-08-10 20:00:00', '2025-08-10 22:30:00',
 'Concert de l\'orchestre philharmonique avec œuvres de Mozart et Beethoven', 'https://example.com/images/symphony.jpg',
 'PUBLISHED', 'CONCERT', 5);

-- Insertion des associations Event-Location (table de liaison)
INSERT INTO event_location (event_id, location_id)
VALUES
-- Cinéma Paradiso
(1, 1),   -- Festival du Film Indépendant dans Salle 1
(1, 2),   -- Festival du Film Indépendant dans Salle 2
(1, 3),   -- Festival du Film Indépendant dans Salle 3
(2, 1),   -- Nuit du Cinéma Fantastique dans Salle 1
(3, 2),   -- Rétrospective Hitchcock dans Salle 2

-- Théâtre National
(4, 4),   -- Le Misanthrope sur Grande Scène
(5, 4),   -- Hamlet sur Grande Scène
(6, 4),   -- Festival de Théâtre Contemporain sur Grande Scène
(6, 5),   -- Festival de Théâtre Contemporain sur Petite Scène

-- Arena Festival
(7, 6),   -- Concert de Rock Symphonique sur Scène Principale
(8, 6),   -- Festival Électro Plein Air sur Scène Principale
(8, 7),   -- Festival Électro Plein Air dans Zone VIP
(9, 6),   -- Concert Caritatif sur Scène Principale

-- Le Grand Rex
(10, 8),  -- Avant-première Star Wars dans Salle Prestige
(11, 8),  -- Marathon Marvel dans Salle Prestige
(11, 9),  -- Marathon Marvel dans Salle Classique
(12, 8),  -- Ciné-Concert Film Muet dans Salle Prestige

-- Opéra Garnier
(13, 10), -- La Traviata dans Grande Salle
(14, 10), -- Ballet Le Lac des Cygnes dans Grande Salle
(15, 10); -- Concert Symphonique dans Grande Salle
