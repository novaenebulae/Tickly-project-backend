-- Structures
INSERT INTO structure (id, name, description)
VALUES (1, 'Théâtre des Lumières', 'Un théâtre moderne situé au cœur de la ville'),
       (2, 'Galerie d''Art Contemporain', 'Espace dédié aux expositions d''art contemporain'),
       (3, 'Salle de Concert Harmonie', 'Salle accueillant des concerts de musique classique et moderne'),
       (4, 'Cinéma Étoile', 'Cinéma proposant des films indépendants et internationaux'),
       (5, 'Centre Culturel Horizon', 'Lieu polyvalent pour événements culturels et communautaires'),
       (6, 'Auditorium Galaxy', 'Auditorium high-tech pour conférences et spectacles');

-- Types de structures
INSERT INTO structure_type (id, type)
VALUES (1, 'Théâtre'),
       (2, 'Galerie d''art'),
       (3, 'Salle de concert'),
       (4, 'Cinéma'),
       (5, 'Centre culturel'),
       (6, 'Auditorium');

-- Relations structure-type
INSERT INTO structure_structure_type (structure_id, type_id)
VALUES (1, 1),
       (2, 2),
       (3, 3),
       (4, 4),
       (5, 5),
       (6, 6);

-- Adresses
INSERT INTO address (id, country, city, postal_code, street, number, structure_id)
VALUES (1, 'France', 'Paris', '75001', 'Rue de Rivoli', NULL, 1),
       (2, 'France', 'Lyon', '69002', 'Place Bellecour', NULL, 2),
       (3, 'France', 'Marseille', '13001', 'Cours Julien', NULL, 3),
       (4, 'France', 'Bordeaux', '33000', 'Quai Louis XVIII', NULL, 4),
       (5, 'France', 'Toulouse', '31000', 'Allées Jean Jaurès', NULL, 5),
       (6, 'France', 'Nice', '06000', 'Promenade des Anglais', NULL, 6);

-- Locations (zones)
INSERT INTO location (id, name, structure_id)
VALUES (1, 'Salle Principale', 1),
       (2, 'Balcon', 1),
       (3, 'Espace d''Exposition', 2),
       (4, 'Salle VIP', 2),
       (5, 'Auditorium Central', 3),
       (6, 'Zone Acoustique', 3),
       (7, 'Salle IMAX', 4),
       (8, 'Espace Lounge', 4),
       (9, 'Grand Hall', 5),
       (10, 'Salle Polyvalente', 5),
       (11, 'Amphithéâtre', 6),
       (12, 'Cabine Technique', 6);

-- Placings (zones de placement)
INSERT INTO placement (id, name, price, capacity, location_id, placement_type)
VALUES (1, 'Zone Orchestra', 50.00, 150, 1, 'SEAT_PLACEMENT'),
       (2, 'Zone Balcon Avant', 35.00, 80, 2, 'SEAT_PLACEMENT'),
       (3, 'Zone Debout', 20.00, 200, 2, 'FREE_PLACEMENT'),
       (4, 'Carré Or', 75.00, 50, 3, 'SEAT_PLACEMENT'),
       (5, 'Espace Libre', 15.00, 300, 4, 'FREE_PLACEMENT'),
       (6, 'Fosse', 40.00, 500, 5, 'FREE_PLACEMENT'),
       (7, 'Siège Premium', 60.00, 120, 7, 'SEAT_PLACEMENT'),
       (8, 'Zone Relax', 25.00, 100, 8, 'FREE_PLACEMENT'),
       (9, 'Espace Principal', 30.00, 400, 9, 'SEAT_PLACEMENT'),
       (10, 'Zone Modulable', 45.00, 250, 10, 'FREE_PLACEMENT'),
       (11, 'Gradins', 55.00, 600, 11, 'SEAT_PLACEMENT'),
       (12, 'Zone Technicien', 0.00, 10, 12, 'FREE_PLACEMENT');
