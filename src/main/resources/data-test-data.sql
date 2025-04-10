INSERT INTO structure (id, name, description)
VALUES (1, 'Théâtre des Lumières', 'Un théâtre moderne situé au cœur de la ville'),
       (2, 'Galerie d\'Art Contemporain', 'Espace dédié aux expositions d\'art contemporain'),
       (3, 'Salle de Concert Harmonie', 'Salle accueillant des concerts de musique classique et moderne'),
       (4, 'Cinéma Étoile', 'Cinéma proposant des films indépendants et internationaux'),
       (5, 'Centre Culturel Horizon', 'Lieu polyvalent pour événements culturels et communautaires'),
       (6, 'Auditorium Galaxy', 'Auditorium high-tech pour conférences et spectacles'),
       (7, 'Musée des Sciences', 'Musée interactif sur les sciences et technologies'),
       (8, 'Opéra Renaissance', 'Lieu prestigieux pour opéras et ballets'),
       (9, 'Studio Créatif Pixel', 'Espace pour ateliers artistiques et numériques'),
       (10, 'Bibliothèque Universelle', 'Grande bibliothèque avec collections variées'),
       (11, 'Parc des Expositions', 'Site pour salons et foires internationales'),
       (12, 'Maison de la Danse', 'Centre dédié aux spectacles de danse contemporaine'),
       (13, 'Cinéma Grand Écran', 'Multiplex avec écrans IMAX'),
       (14, 'Stade Olympique', 'Grand stade pour événements sportifs et concerts'),
       (15, 'Théâtre Antique', 'Lieu historique pour représentations en plein air'),
       (16, 'Galerie PhotoVision', 'Galerie spécialisée en photographie moderne'),
       (17, 'Café Littéraire Écrivains', 'Espace culturel pour lectures et débats littéraires'),
       (18, 'Centre Musical Mélodie', 'École et salle de concert musicale'),
       (19, 'Cinéma Lunaire', 'Cinéma spécialisé en science-fiction et fantastique'),
       (20, 'Complexe Sportif Élite', 'Centre multifonctionnel pour sports et fitness');

INSERT INTO structure_type (id, type)
VALUES (1, 'Théâtre'),
       (2, 'Galerie d\'art'),
       (3, 'Salle de concert'),
       (4, 'Cinéma'),
       (5, 'Centre culturel'),
       (6, 'Auditorium'),
       (7, 'Musée'),
       (8, 'Opéra'),
       (9, 'Studio créatif'),
       (10, 'Bibliothèque'),
       (11, 'Parc des expositions'),
       (12, 'Maison de la danse'),
       (13, 'Multiplex cinéma'),
       (14, 'Stade sportif'),
       (15, 'Théâtre antique'),
       (16, 'Galerie photo'),
       (17, 'Café littéraire'),
       (18, 'Centre musical'),
       (19, 'Cinéma spécialisé'),
       (20, 'Complexe sportif');

INSERT INTO structure_structure_type (structure_id, type_id)
VALUES (1, 1),   -- Théâtre des Lumières -> Théâtre
       (2, 2),   -- Galerie d'Art Contemporain -> Galerie d'art
       (3, 3),   -- Salle de Concert Harmonie -> Salle de concert
       (4, 4),   -- Cinéma Étoile -> Cinéma
       (5, 5),   -- Centre Culturel Horizon -> Centre culturel
       (6, 6),   -- Auditorium Galaxy -> Auditorium
       (7, 7),   -- Musée des Sciences -> Musée
       (8, 8),   -- Opéra Renaissance -> Opéra
       (9, 9),   -- Studio Créatif Pixel -> Studio créatif
       (10, 10), -- Bibliothèque Universelle -> Bibliothèque
       (11, 11), -- Parc des Expositions -> Parc des expositions
       (12, 12), -- Maison de la Danse -> Maison de la danse
       (13, 13), -- Cinéma Grand Écran -> Multiplex cinéma
       (14, 14), -- Stade Olympique -> Stade sportif
       (15, 15), -- Théâtre Antique -> Théâtre antique
       (16, 16), -- Galerie PhotoVision -> Galerie photo
       (17, 17), -- Café Littéraire Écrivains -> Café littéraire
       (18, 18), -- Centre Musical Mélodie -> Centre musical
       (19, 19), -- Cinéma Lunaire -> Cinéma spécialisé
       (20, 20), -- Complexe Sportif Élite -> Complexe sportif
       (5, 11),  -- Centre Culturel Horizon -> Parc des expositions (exemple de multi-classification)
       (6, 3),   -- Auditorium Galaxy -> Salle de concert (utilisation secondaire)
       (14, 3);
-- Stade Olympique -> Salle de concert (pour concerts)


# INSERT INTO adresse (id, country, city, postal_code, street, number, stucture_id)
# VALUES
#     (1, 'France', 'Paris', 75001, 'Rue de Rivoli', NULL, 1),
#     (2, 'France', 'Lyon', 69002, 'Place Bellecour', NULL, 2),
#     (3, 'France', 'Marseille', 13001, 'Cours Julien', NULL, 3),
#     (4, 'France', 'Bordeaux', 33000, 'Quai Louis XVIII', NULL, 4),
#     (5, 'France', 'Toulouse', 31000, 'Allées Jean Jaurès', NULL, 5),
#     (6, 'France', 'Nice', 06000, "Promenade des Anglais", NULL ,6),
#     (7,'Belgique','Bruxelles','1000','Boulevard Anspach','32A ',7),
#     (8,'Italie','Rome','00100','Via del Corso','15 ',8),
#     (9,'Espagne','Madrid','28001','Gran Via','10 ',9),
#     (10,'Allemagne','Berlin','10115','Unter den Linden ','22 ',10),
#     (11,'Portugal','Lisbonne ','1100-038 ','Rua Augusta ','45 ',11),
#     (12,'Suisse ','Genève ','1201 ','Rue du Rhône ','5 ',12 ),
#     (13,'Pays-Bas ','Amsterdam ','1012AB ','Damstraat ','18 ',13 ),
#     (14,'Autriche ','Vienne ','1010 ','Kärntner Straße ','30 ',14 ),
#     (15,'Suède ','Stockholm ','11122 ','Drottninggatan ','25 ',15 ),
#     (16,'Norvège ','Oslo ','0154 ','Karl Johans gate ','40 ',16 ),
#     (17,'Danemark ','Copenhague ','1165 ','Strøget ','35 ',17 ),
#     (18,'Finlande ','Helsinki ','00100 ','Aleksanterinkatu ','50 ',18 ),
#     (19,'Irlande ','Dublin ','D02 X285','Grafton Street','55 ',19 ),
#     (20,'Royaume-Uni','Londres','SW1A1AA','Buckingham Palace Road','60 ',20);

-- Insertion des rôles (basés sur l'enum RoleType)
INSERT INTO role (id, role_type)
VALUES (1, 'SPECTATOR'),
       (2, 'RESERVATION_SERVICE'),
       (3, 'ORGANIZATION_SERVICE'),
       (4, 'STRUCTURE_ADMINISTRATOR');

-- Utilisateurs avec différents rôles et structures
INSERT INTO user (id, mail, password, lastName, firstName, createdAt, structure_id)
VALUES
-- Spectateurs sans structure
(100, 'spectateur1@test.com', '$2a$10$6ZhQ4J7bDdKZ1U7fKQqB6eZ7gM8V9X1N3rY6vA2sWjJkLmNpOqRs', 'Dupont', 'Jean', NOW(),
 NULL),
(101, 'spectateur2@test.com', '$2a$10$8YhG7tR6dE5fS3qW2vJ9Ku1M4N5B6V7C8X9Z0A1B2D3E4F5G6H7I', 'Martin', 'Sophie', NOW(),
 NULL),

-- Administrateurs de structure
(200, 'admin@theatre.com', '$2a$10$9ZiH8YgT7R5eF4S3qW2vJ9Ku1M4N5B6V7C8X9Z0A1B2D3E4F5G6H7I', 'Leroy', 'Paul', NOW(), 1),
(201, 'admin@galerie.com', '$2a$10$7XkP9qR5tY6uI8O0P1Q2wS3D4F5G6H7J8K9L0M1N2O3P4Q5R6S7T', 'Dubois', 'Marie', NOW(), 2),

-- Staff avec rôles spécifiques
(300, 'reservation@theatre.com', '$2a$10$5YgU7vH2JkL3M4N5B6V7C8X9Z0A1B2D3E4F5G6H7I8J9K0L1M2N3O', 'Roux', 'Luc', NOW(),
 1),
(301, 'organisation@concert.com', '$2a$10$4W3E5R6T7Y8U9I0O1P2Q3S4D5F6G7H8J9K0L1M2N3O4P5Q6R7S8T', 'Blanc', 'Emma', NOW(),
 3),

-- Utilisateur multi-rôles
(400, 'superuser@test.com', '$2a$10$3Q4W5E6R7T8Y9U0I1O2P3A4S5D6F7G8H9J0K1L2M3N4O5P6Q7R8S', 'Noir', 'Pierre', NOW(), 1);

-- Assignation des rôles dans user_role
INSERT INTO user_role (user_id, role_id)
VALUES
-- Spectateurs
(100, 1),
(101, 1),

-- Admins de structure
(200, 4),
(201, 4),

-- Staff
(300, 2), -- RESERVATION_SERVICE
(301, 3), -- ORGANIZATION_SERVICE

-- Multi-rôles
(400, 1), -- SPECTATOR
(400, 4), -- STRUCTURE_ADMINISTRATOR
(400, 3); -- ORGANIZATION_SERVICE
