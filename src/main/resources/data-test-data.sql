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

-- Insertion des adresses
INSERT INTO address (id, country, city, zip_code, street, number, structure_id)
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

-- Liaison Structure <=> StructureType
INSERT INTO structure_structure_type (structure_id, type_id)
VALUES (1, 1),  -- Cinéma Paradiso est un Cinéma
       (2, 2),  -- Théâtre National est un Théâtre
       (3, 3),  -- Arena Festival est une Salle de concert
       (4, 1),  -- Le Grand Rex est un Cinéma
       (5, 9),  -- Opéra Garnier est un Opéra
       (6, 3),  -- Zénith de Lille est une Salle de concert
       (7, 2),  -- Théâtre de la Ville est un Théâtre
       (8, 6),  -- Palais des Congrès est un Centre de congrès
       (9, 4),  -- Stade Pierre-Mauroy est un Stade
       (10, 5), -- Musée d'Art Moderne est un Musée
       (1, 7),  -- Cinéma Paradiso est aussi une Galerie d'art
       (2, 8),  -- Théâtre National est aussi un Amphithéâtre
       (3, 10), -- Arena Festival est aussi un Festival
       (6, 10), -- Zénith de Lille est aussi un Festival
       (8, 2);
-- Palais des Congrès est aussi un Théâtre

-- Insertion des utilisateurs
INSERT INTO user (id, email, password, first_name, last_name, role, registration_date, last_connection)
VALUES
-- Administrateurs de structure
(1, 'admin.cinema@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Jean', 'Dupont',
 'STRUCTURE_ADMINISTRATOR', NOW(), NOW()),
(2, 'admin.theatre@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Marie', 'Laurent',
 'STRUCTURE_ADMINISTRATOR', NOW(), NOW()),
(3, 'admin.arena@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Pierre', 'Martin',
 'STRUCTURE_ADMINISTRATOR', NOW(), NOW()),
(4, 'admin.rex@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Sophie', 'Dubois',
 'STRUCTURE_ADMINISTRATOR', NOW(), NOW()),
(5, 'admin.opera@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Thomas', 'Leroy',
 'STRUCTURE_ADMINISTRATOR', NOW(), NOW()),
-- Spectateurs
(6, 'spectateur1@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Lucie', 'Moreau',
 'SPECTATOR', NOW(), NOW()),
(7, 'spectateur2@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Antoine', 'Bernard',
 'SPECTATOR', NOW(), NOW()),
(8, 'spectateur3@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Emma', 'Petit',
 'SPECTATOR', NOW(), NOW()),
(9, 'spectateur4@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Hugo', 'Robert',
 'SPECTATOR', NOW(), NOW()),
(10, 'spectateur5@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Chloé', 'Simon',
 'SPECTATOR', NOW(), NOW()),
(11, 'spectateur6@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Louis', 'Michel',
 'SPECTATOR', NOW(), NOW()),
(12, 'spectateur7@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Camille', 'Lefebvre',
 'SPECTATOR', NOW(), NOW()),
(13, 'spectateur8@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Jules', 'Garcia',
 'SPECTATOR', NOW(), NOW()),
(14, 'spectateur9@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Léa', 'Roux',
 'SPECTATOR', NOW(), NOW()),
(15, 'spectateur10@example.com', '$2a$10$N7Rk.ZbJw7Wc6b6q1R3X.OU4zQcBdK9t0wL1mU7GvNk1sJdLpZaO', 'Théo', 'Fournier',
 'SPECTATOR', NOW(), NOW());

-- Assignation des structures aux administrateurs
UPDATE user
SET structure_id = 1
WHERE id = 1
  AND role = 'STRUCTURE_ADMINISTRATOR';

UPDATE user
SET structure_id = 2
WHERE id = 2
  AND role = 'STRUCTURE_ADMINISTRATOR';

UPDATE user
SET structure_id = 3
WHERE id = 3
  AND role = 'STRUCTURE_ADMINISTRATOR';

UPDATE user
SET structure_id = 4
WHERE id = 4
  AND role = 'STRUCTURE_ADMINISTRATOR';

UPDATE user
SET structure_id = 5
WHERE id = 5
  AND role = 'STRUCTURE_ADMINISTRATOR';

-- Insertion des locations (emplacements)
INSERT INTO location (id, name)
VALUES (1, 'Salle 1'),
       (2, 'Salle 2'),
       (3, 'Salle 3'),
       (4, 'Grande Scène'),
       (5, 'Petite Scène'),
       (6, 'Scène Principale'),
       (7, 'Zone VIP'),
       (8, 'Salle Prestige'),
       (9, 'Salle Classique'),
       (10, 'Grande Salle'),
       (11, 'Foyer'),
       (12, 'Parterre'),
       (13, 'Balcon'),
       (14, 'Salle A'),
       (15, 'Salle B'),
       (16, 'Auditorium'),
       (17, 'Salle de Conférence'),
       (18, 'Terrain'),
       (19, 'Tribune Nord'),
       (20, 'Galerie Moderne'),
       (21, 'Galerie Contemporaine');

-- Liaison Structure <=> Location
INSERT INTO structure_location (structure_id, location_id)
VALUES (1, 1),   -- Cinéma Paradiso - Salle 1
       (1, 2),   -- Cinéma Paradiso - Salle 2
       (1, 3),   -- Cinéma Paradiso - Salle 3
       (2, 4),   -- Théâtre National - Grande Scène
       (2, 5),   -- Théâtre National - Petite Scène
       (3, 6),   -- Arena Festival - Scène Principale
       (3, 7),   -- Arena Festival - Zone VIP
       (4, 8),   -- Le Grand Rex - Salle Prestige
       (4, 9),   -- Le Grand Rex - Salle Classique
       (5, 10),  -- Opéra Garnier - Grande Salle
       (5, 11),  -- Opéra Garnier - Foyer
       (6, 12),  -- Zénith de Lille - Parterre
       (6, 13),  -- Zénith de Lille - Balcon
       (7, 14),  -- Théâtre de la Ville - Salle A
       (7, 15),  -- Théâtre de la Ville - Salle B
       (8, 16),  -- Palais des Congrès - Auditorium
       (8, 17),  -- Palais des Congrès - Salle de Conférence
       (9, 18),  -- Stade Pierre-Mauroy - Terrain
       (9, 19),  -- Stade Pierre-Mauroy - Tribune Nord
       (10, 20), -- Musée d'Art Moderne - Galerie Moderne
       (10, 21);
-- Musée d'Art Moderne - Galerie Contemporaine

-- Insertion des placements
INSERT INTO placement (id, name, price, capacity, placement_type, location_id)
VALUES
-- Cinéma Paradiso
(1, 'Rang A', 1500, 50, 'SEAT_PLACEMENT', 1),
(2, 'Rang B', 1200, 60, 'SEAT_PLACEMENT', 1),
(3, 'Rang C', 1000, 70, 'SEAT_PLACEMENT', 1),
(4, 'Rang A', 1500, 50, 'SEAT_PLACEMENT', 2),
(5, 'Rang B', 1200, 60, 'SEAT_PLACEMENT', 2),
(6, 'Rang C', 1000, 70, 'SEAT_PLACEMENT', 2),
(7, 'Rang A', 1500, 50, 'SEAT_PLACEMENT', 3),
(8, 'Rang B', 1200, 60, 'SEAT_PLACEMENT', 3),
-- Théâtre National
(9, 'Orchestre', 2500, 300, 'FREE_PLACEMENT', 4),
(10, 'Balcon', 1800, 200, 'SEAT_PLACEMENT', 4),
(11, 'Loge', 3000, 100, 'SEAT_PLACEMENT', 4),
(12, 'Parterre', 1500, 150, 'FREE_PLACEMENT', 5),
-- Arena Festival
(13, 'Fosse', 3000, 2000, 'FREE_PLACEMENT', 6),
(14, 'Gradin', 3500, 2500, 'SEAT_PLACEMENT', 6),
(15, 'Carré Or', 5000, 500, 'SEAT_PLACEMENT', 7),
-- Le Grand Rex
(16, 'Premium', 2000, 200, 'SEAT_PLACEMENT', 8),
(17, 'Standard', 1500, 500, 'SEAT_PLACEMENT', 8),
(18, 'Économique', 1000, 300, 'SEAT_PLACEMENT', 9),
-- Opéra Garnier
(19, 'Premier Balcon', 4000, 150, 'SEAT_PLACEMENT', 10),
(20, 'Parterre', 3500, 300, 'SEAT_PLACEMENT', 10),
(21, 'Amphithéâtre', 2000, 200, 'SEAT_PLACEMENT', 10),
-- Zénith de Lille
(22, 'Catégorie 1', 4500, 1000, 'SEAT_PLACEMENT', 12),
(23, 'Catégorie 2', 3500, 1500, 'SEAT_PLACEMENT', 12),
(24, 'Catégorie 3', 2500, 2000, 'SEAT_PLACEMENT', 13),
-- Théâtre de la Ville
(25, 'Fauteuil', 2200, 300, 'SEAT_PLACEMENT', 14),
(26, 'Strapontin', 1500, 100, 'SEAT_PLACEMENT', 14),
(27, 'Placement Libre', 1800, 200, 'FREE_PLACEMENT', 15),
-- Palais des Congrès
(28, 'Section A', 3000, 500, 'SEAT_PLACEMENT', 16),
(29, 'Section B', 2500, 500, 'SEAT_PLACEMENT', 16),
(30, 'Section C', 2000, 300, 'SEAT_PLACEMENT', 17),
-- Stade Pierre-Mauroy
(31, 'Tribune Présidentielle', 8000, 1000, 'SEAT_PLACEMENT', 18),
(32, 'Tribune Latérale', 6000, 5000, 'SEAT_PLACEMENT', 18),
(33, 'Virage', 4000, 10000, 'SEAT_PLACEMENT', 19);

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
 'PUBLISHED', 'CONCERT', 5),

-- Zénith de Lille (Structure ID: 6)
(16, 'Concert Pop-Rock', '2025-06-25 20:00:00', '2025-06-25 23:30:00',
 'Concert d\'un groupe pop-rock international en tournée mondiale', 'https://example.com/images/pop_rock.jpg',
 'PUBLISHED', 'CONCERT', 6),
(17, 'Festival de Jazz', '2025-07-10 19:00:00', '2025-07-12 23:00:00',
 'Trois jours dédiés au jazz avec artistes nationaux et internationaux', 'https://example.com/images/jazz_festival.jpg',
 'PUBLISHED', 'FESTIVAL', 6),
(18, 'Spectacle Humoristique', '2025-08-05 20:30:00', '2025-08-05 22:30:00', 'One-man-show d\'un humoriste célèbre',
 'https://example.com/images/comedy.jpg', 'DRAFT', 'OTHER', 6),

-- Théâtre de la Ville (Structure ID: 7)
(19, 'Dom Juan', '2025-06-05 19:30:00', '2025-06-05 22:00:00', 'Pièce de Molière dans une mise en scène moderne',
 'https://example.com/images/dom_juan.jpg', 'PUBLISHED', 'THEATRE', 7),
(20, 'Cyrano de Bergerac', '2025-07-20 20:00:00', '2025-07-20 23:00:00', 'La célèbre pièce d\'Edmond Rostand',
 'https://example.com/images/cyrano.jpg', 'PUBLISHED', 'THEATRE', 7),
(21, 'Festival de Danse Contemporaine', '2025-09-01 19:00:00', '2025-09-05 22:00:00',
 'Spectacles de danse contemporaine par des compagnies internationales', 'https://example.com/images/dance.jpg',
 'DRAFT', 'FESTIVAL', 7),

-- Palais des Congrès (Structure ID: 8)
(22, 'Conférence Tech', '2025-06-15 09:00:00', '2025-06-16 18:00:00',
 'Conférence sur les dernières innovations technologiques', 'https://example.com/images/tech_conf.jpg', 'PUBLISHED',
 'CONFERENCE', 8),
(23, 'Salon du Livre', '2025-07-25 10:00:00', '2025-07-27 19:00:00',
 'Salon littéraire avec présence d\'auteurs et séances de dédicace', 'https://example.com/images/book_fair.jpg',
 'PUBLISHED', 'EXHIBITION', 8),
(24, 'Comédie Musicale', '2025-08-30 20:00:00', '2025-08-30 22:30:00', 'Adaptation d\'une comédie musicale à succès',
 'https://example.com/images/musical.jpg', 'DRAFT', 'THEATRE', 8),

-- Stade Pierre-Mauroy (Structure ID: 9)
(25, 'Match de Football', '2025-06-10 20:45:00', '2025-06-10 22:30:00', 'Match de championnat de première division',
 'https://example.com/images/football.jpg', 'PUBLISHED', 'SPORT', 9),
(26, 'Concert de Rock', '2025-07-05 20:00:00', '2025-07-05 23:30:00', 'Concert d\'un groupe de rock légendaire',
 'https://example.com/images/rock_concert.jpg', 'PUBLISHED', 'CONCERT', 9),
(27, 'Compétition d\'Athlétisme', '2025-08-15 14:00:00', '2025-08-15 18:00:00', 'Meeting international d\'athlétisme',
 'https://example.com/images/athletics.jpg', 'DRAFT', 'SPORT', 9),

-- Musée d'Art Moderne (Structure ID: 10)
(28, 'Exposition Art Nouveau', '2025-06-01 10:00:00', '2025-08-31 18:00:00',
 'Exposition temporaire sur l\'Art Nouveau européen', 'https://example.com/images/art_nouveau.jpg', 'PUBLISHED',
 'EXHIBITION', 10),
(29, 'Nuit des Musées', '2025-07-15 19:00:00', '2025-07-16 01:00:00',
 'Ouverture nocturne avec animations et performances artistiques', 'https://example.com/images/museum_night.jpg',
 'PUBLISHED', 'OTHER', 10),
(30, 'Atelier d\'Art pour Enfants', '2025-08-10 14:00:00', '2025-08-10 16:00:00',
 'Atelier créatif pour initier les enfants à l\'art contemporain', 'https://example.com/images/kids_workshop.jpg',
 'PUBLISHED', 'OTHER', 10);

-- Suite des associations Event-Location
INSERT INTO event_location (event_id, location_id)
VALUES
-- Opéra Garnier
(15, 10), -- Concert Symphonique dans Grande Salle

-- Zénith de Lille
(16, 12), -- Concert Pop-Rock dans Parterre
(16, 13), -- Concert Pop-Rock dans Balcon
(17, 12), -- Festival de Jazz dans Parterre
(18, 12), -- Spectacle Humoristique dans Parterre

-- Théâtre de la Ville
(19, 14), -- Dom Juan dans Salle A
(20, 14), -- Cyrano de Bergerac dans Salle A
(21, 14), -- Festival de Danse Contemporaine dans Salle A
(21, 15), -- Festival de Danse Contemporaine dans Salle B

-- Palais des Congrès
(22, 16), -- Conférence Tech dans Auditorium
(22, 17), -- Conférence Tech dans Salle de Conférence
(23, 16), -- Salon du Livre dans Auditorium
(24, 16), -- Comédie Musicale dans Auditorium

-- Stade Pierre-Mauroy
(25, 18), -- Match de Football sur Terrain
(26, 18), -- Concert de Rock sur Terrain
(27, 18), -- Compétition d'Athlétisme sur Terrain

-- Musée d'Art Moderne
(28, 20), -- Exposition Art Nouveau dans Galerie Moderne
(28, 21), -- Exposition Art Nouveau dans Galerie Contemporaine
(29, 20), -- Nuit des Musées dans Galerie Moderne
(29, 21), -- Nuit des Musées dans Galerie Contemporaine
(30, 21);
-- Atelier d'Art pour Enfants dans Galerie Contemporaine

# -- Insertion des favoris structure
# INSERT INTO structure_favorite (id, add_date, structure_id, user_id)
# VALUES (1, '2025-03-15 10:30:00', 1, 6),   -- Lucie Moreau aime Cinéma Paradiso
#        (2, '2025-03-16 14:45:00', 3, 6),   -- Lucie Moreau aime Arena Festival
#        (3, '2025-03-10 09:15:00', 2, 7),   -- Antoine Bernard aime Théâtre National
#        (4, '2025-03-12 18:20:00', 5, 7),   -- Antoine Bernard aime Opéra Garnier
#        (5, '2025-03-20 11:05:00', 4, 8),   -- Emma Petit aime Le Grand Rex
#        (6, '2025-03-22 16:30:00', 9, 9),   -- Hugo Robert aime Stade Pierre-Mauroy
#        (7, '2025-03-25 13:40:00', 6, 10),  -- Chloé Simon aime Zénith de Lille
#        (8, '2025-03-18 10:10:00', 10, 11), -- Louis Michel aime Musée d'Art Moderne
#        (9, '2025-03-19 15:25:00', 8, 12),  -- Camille Lefebvre aime Palais des Congrès
#        (10, '2025-03-21 12:50:00', 7, 13);
# -- Jules Garcia aime Théâtre de la Ville
#
# -- Insertion des commandes
# INSERT INTO order_table (id, ticket_count, user_id)
# VALUES (1, 2, 6),  -- Commande de Lucie Moreau (spectateur1)
#        (2, 4, 7),  -- Commande d'Antoine Bernard (spectateur2)
#        (3, 3, 8),  -- Commande d'Emma Petit (spectateur3)
#        (4, 2, 9),  -- Commande d'Hugo Robert (spectateur4)
#        (5, 1, 10), -- Commande de Chloé Simon (spectateur5)
#        (6, 2, 11), -- Commande de Louis Michel (spectateur6)
#        (7, 3, 12), -- Commande de Camille Lefebvre (spectateur7)
#        (8, 2, 13), -- Commande de Jules Garcia (spectateur8)
#        (9, 1, 14), -- Commande de Léa Roux (spectateur9)
#        (10, 2, 15);
# -- Commande de Théo Fournier (spectateur10)
#
# -- Insertion des billets
# INSERT INTO ticket (id, qr_code, status, issue_date, event_rating, scanned, placement_id, event_id, order_id)
# VALUES
# -- Commande 1 (Lucie Moreau)
# (1, 'QR12345678901', 'VALID', '2025-04-01 10:15:00', NULL, false, 10, 5, 1),
# (2, 'QR12345678902', 'VALID', '2025-04-01 10:15:00', NULL, false, 10, 5, 1),
#
# -- Commande 2 (Antoine Bernard)
# (3, 'QR12345678903', 'VALID', '2025-04-02 14:30:00', NULL, false, 13, 7, 2),
# (4, 'QR12345678904', 'VALID', '2025-04-02 14:30:00', NULL, false, 13, 7, 2),
# (5, 'QR12345678905', 'VALID', '2025-04-02 14:30:00', NULL, false, 13, 7, 2),
# (6, 'QR12345678906', 'VALID', '2025-04-02 14:30:00', NULL, false, 13, 7, 2),
#
# -- Commande 3 (Emma Petit)
# (7, 'QR12345678907', 'VALID', '2025-04-03 09:45:00', NULL, false, 16, 10, 3),
# (8, 'QR12345678908', 'VALID', '2025-04-03 09:45:00', NULL, false, 16, 10, 3),
# (9, 'QR12345678909', 'VALID', '2025-04-03 09:45:00', NULL, false, 16, 10, 3),
#
# -- Commande 4 (Hugo Robert)
# (10, 'QR12345678910', 'VALID', '2025-04-04 16:20:00', NULL, false, 19, 13, 4),
# (11, 'QR12345678911', 'VALID', '2025-04-04 16:20:00', NULL, false, 19, 13, 4),
#
# -- Commande 5 (Chloé Simon)
# (12, 'QR12345678912', 'VALID', '2025-04-05 11:10:00', NULL, false, 1, 1, 5),
#
# -- Commande 6 (Louis Michel)
# (13, 'QR12345678913', 'VALID', '2025-04-06 13:25:00', NULL, false, 9, 4, 6),
# (14, 'QR12345678914', 'VALID', '2025-04-06 13:25:00', NULL, false, 9, 4, 6),
#
# -- Commande 7 (Camille Lefebvre)
# (15, 'QR12345678915', 'VALID', '2025-04-07 15:40:00', NULL, false, 14, 8, 7),
# (16, 'QR12345678916', 'VALID', '2025-04-07 15:40:00', NULL, false, 14, 8, 7),
# (17, 'QR12345678917', 'VALID', '2025-04-07 15:40:00', NULL, false, 14, 8, 7),
#
# -- Commande 8 (Jules Garcia)
# (18, 'QR12345678918', 'VALID', '2025-04-08 10:05:00', NULL, false, 20, 14, 8),
# (19, 'QR12345678919', 'VALID', '2025-04-08 10:05:00', NULL, false, 20, 14, 8),
#
# -- Commande 9 (Léa Roux)
# (20, 'QR12345678920', 'VALID', '2025-04-09 14:15:00', NULL, false, 2, 2, 9),
#
# -- Commande 10 (Théo Fournier)
# (21, 'QR12345678921', 'VALID', '2025-04-10 09:30:00', NULL, false, 22, 16, 10),
# (22, 'QR12345678922', 'VALID', '2025-04-10 09:30:00', NULL, false, 22, 16, 10);
#
# -- Insertion des spectateurs
# INSERT INTO spectator (id, first_name, last_name, email, ticket_id)
# VALUES (1, 'Marie', 'Dupont', 'marie.dupont@example.com', 1),
#        (2, 'Pierre', 'Martin', 'pierre.martin@example.com', 2),
#        (3, 'Sophie', 'Leroy', 'sophie.leroy@example.com', 3),
#        (4, 'Thomas', 'Bernard', 'thomas.bernard@example.com', 4),
#        (5, 'Julie', 'Petit', 'julie.petit@example.com', 5),
#        (6, 'Nicolas', 'Durand', 'nicolas.durand@example.com', 6),
#        (7, 'Camille', 'Moreau', 'camille.moreau@example.com', 7),
#        (8, 'Lucas', 'Simon', 'lucas.simon@example.com', 8),
#        (9, 'Emma', 'Roux', 'emma.roux@example.com', 9),
#        (10, 'Hugo', 'Michel', 'hugo.michel@example.com', 10),
#        (11, 'Léa', 'Fournier', 'lea.fournier@example.com', 11),
#        (12, 'Maxime', 'Girard', 'maxime.girard@example.com', 12),
#        (13, 'Clara', 'Lambert', 'clara.lambert@example.com', 13),
#        (14, 'Théo', 'Mercier', 'theo.mercier@example.com', 14),
#        (15, 'Manon', 'Bonnet', 'manon.bonnet@example.com', 15);
#
# -- Insertion des historiques d'événements (pour les événements passés ou en cours)
# INSERT INTO event_history (id, expected_tickets, sold_tickets, attendees, event_id)
# VALUES (1, 500, 450, 430, 1),         -- Festival du Film Indépendant
#        (2, 300, 290, 275, 2),         -- Nuit du Cinéma Fantastique
#        (3, 800, 750, 720, 4),         -- Le Misanthrope
#        (4, 800, 780, 760, 5),         -- Hamlet
#        (5, 5000, 4800, 4600, 7),      -- Concert de Rock Symphonique
#        (6, 5000, 4900, 4700, 8),      -- Festival Électro Plein Air
#        (7, 1000, 950, 930, 10),       -- Avant-première Star Wars
#        (8, 1500, 1400, 1350, 11),     -- Marathon Marvel
#        (9, 1500, 1450, 1400, 13),     -- La Traviata
#        (10, 1500, 1480, 1450, 14),    -- Ballet Le Lac des Cygnes
#        (11, 2000, 1950, 1900, 16),    -- Concert Pop-Rock
#        (12, 3000, 2800, 2750, 17),    -- Festival de Jazz
#        (13, 600, 580, 570, 19),       -- Dom Juan
#        (14, 600, 590, 580, 20),       -- Cyrano de Bergerac
#        (15, 1200, 1100, 1050, 22),    -- Conférence Tech
#        (16, 2000, 1800, 1750, 23),    -- Salon du Livre
#        (17, 30000, 28000, 27500, 25), -- Match de Football
#        (18, 35000, 34000, 33500, 26), -- Concert de Rock
#        (19, 1000, 950, 900, 28),      -- Exposition Art Nouveau
#        (20, 800, 750, 730, 29); -- Nuit des Musées

