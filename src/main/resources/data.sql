-- ######################################################################
-- #                                                                    #
-- #          SCRIPT DE PEUPLEMENT DE DONNÉES POUR L'APPLICATION TICKLY   #
-- #                                                                    #
-- ######################################################################
-- Ce script est conçu pour être exécuté par Spring Boot au démarrage.
-- Il doit être placé dans le répertoire `src/main/resources` et nommé `data.sql`.
-- L'ordre des insertions est crucial pour respecter les contraintes de clés étrangères.

-- ##################################################
-- # 1. PEUPLEMENT DE LA TABLE `structure_types`    #
-- ##################################################
-- Ces données de base définissent les catégories de structures.
-- Elles n'ont aucune dépendance et doivent être insérées en premier.

INSERT INTO structure_types (id, name, icon)
VALUES (1, 'Salle de concert', 'icon-concert-hall'),
       (2, 'Théâtre', 'icon-theater-masks'),
       (3, 'Opéra', 'icon-opera'),
       (4, 'Stade / Arène', 'icon-stadium'),
       (5, 'Centre de congrès / Parc des expositions', 'icon-convention-center'),
       (6, 'Musée / Galerie d''exposition', 'icon-museum'),
       (7, 'Cinéma', 'icon-cinema'),
       (8, 'Café-théâtre / Comédie club', 'icon-comedy-club');

-- ##################################################
-- # 2. PEUPLEMENT DE LA TABLE `structures`         #
-- ##################################################
-- Insertion des structures avec `administrator_id` à NULL pour l'instant,
-- afin de résoudre la dépendance circulaire avec la table `users`.
-- La mise à jour sera faite à l'étape 4.

INSERT INTO structures (id, name, description, phone, email, website_url, is_active, street, city, zip_code, country,
                        created_at, updated_at, logo_path, cover_path, administrator_id)
VALUES (1, 'L''Arsenal',
        'Prestigieuse salle de concert et lieu d''exposition réputé pour son acoustique exceptionnelle.', '0387399200',
        'contact@arsenal-metz.fr', 'https://www.citemusicale-metz.fr', 1, '3 Avenue Ney', 'Metz', '57000', 'France',
        NOW(), NOW(), '/static/images/structures/logos/logo_arsenal.png',
        '/static/images/structures/covers/cover_arsenal.jpg', NULL),
       (2, 'La BAM (Boîte à Musiques)',
        'Salle de musiques actuelles moderne, avec studios de répétition et une programmation éclectique.',
        '0387393470', 'contact@bam-metz.fr', 'https://www.citemusicale-metz.fr/la-bam', 1, '20 Boulevard d''Alsace',
        'Metz', '57070', 'France', NOW(), NOW(), '/static/images/structures/logos/logo_bam.png',
        '/static/images/structures/covers/cover_bam.jpg', NULL),
       (3, 'Opéra-Théâtre de Metz',
        'Le plus ancien opéra-théâtre en activité en France, proposant des productions lyriques et théâtrales.',
        '0387156060', 'billetterie@opera.metzmetropole.fr', 'https://opera.eurometropolemetz.eu', 1,
        '4-5 Place de la Comédie', 'Metz', '57000', 'France', NOW(), NOW(),
        '/static/images/structures/logos/logo_opera.png', '/static/images/structures/covers/cover_opera.jpg', NULL),
       (4, 'Stade Saint-Symphorien', 'Principal stade de football de la ville, accueillant les matchs du FC Metz.',
        '0387667215', 'contact@fcmetz.com', 'https://www.fcmetz.com', 1, '3 Allée Saint-Symphorien',
        'Longeville-lès-Metz', '57050', 'France', NOW(), NOW(), '/static/images/structures/logos/logo_fcmetz.png',
        '/static/images/structures/covers/cover_stade.jpg', NULL),
       (5, 'Parc des Expositions de Metz Métropole',
        'Vaste complexe pour foires, salons professionnels et expositions de grande envergure.', '0387556600',
        'info@metz-expo.com', 'https://www.metz-expo.com', 1, 'Rue de la Grange aux Bois', 'Metz', '57070', 'France',
        NOW(), NOW(), '/static/images/structures/logos/logo_parcexpo.png',
        '/static/images/structures/covers/cover_parcexpo.jpg', NULL),
       (6, 'Les Trinitaires',
        'Lieu culturel historique situé dans un ancien couvent, avec un caveau jazz et une chapelle pour concerts.',
        '0387200303', 'contact@lestrinitaires.com', 'https://www.citemusicale-metz.fr/les-trinitaires', 1,
        '12 Rue des Trinitaires', 'Metz', '57000', 'France', NOW(), NOW(),
        '/static/images/structures/logos/logo_trinitaires.png',
        '/static/images/structures/covers/cover_trinitaires.jpg', NULL),
       (7, 'Comédie de Metz', 'Théâtre dédié à l''humour et aux comédies, situé dans un quartier historique.',
        '0781511512', 'comediedemetz@gmail.com', 'https://www.comediedemetz.fr', 1, '1/3 Rue du Pont Saint-Marcel',
        'Metz', '57000', 'France', NOW(), NOW(), '/static/images/structures/logos/logo_comedie.png',
        '/static/images/structures/covers/cover_comedie.jpg', NULL),
       (8, 'Salle Braun', 'Théâtre intimiste proposant une programmation variée, notamment pour le jeune public.',
        '0668092756', 'directionsallebraun@gmail.com', 'https://sallebraun.com', 1, '18 Rue Mozart', 'Metz', '57000',
        'France', NOW(), NOW(), '/static/images/structures/logos/logo_braun.png',
        '/static/images/structures/covers/cover_braun.jpg', NULL),
       (9, 'Metz Congrès Robert Schuman',
        'Centre de congrès moderne face au Centre Pompidou, idéal pour les conventions et séminaires.', '0387556600',
        'congres@metz-evenements.com', 'https://www.metz-evenements.com', 1, '112 Rue aux Arènes', 'Metz', '57000',
        'France', NOW(), NOW(), '/static/images/structures/logos/logo_congres.png',
        '/static/images/structures/covers/cover_congres.jpg', NULL),
       (10, 'Centre Pompidou-Metz',
        'Musée d''art moderne et contemporain de renommée internationale, à l''architecture audacieuse.', '0387153939',
        'contact@centrepompidou-metz.fr', 'https://www.centrepompidou-metz.fr', 1, '1 Parvis des Droits-de-l''Homme',
        'Metz', '57000', 'France', NOW(), NOW(), '/static/images/structures/logos/logo_pompidou.png',
        '/static/images/structures/covers/cover_pompidou.jpg', NULL),
       (11, 'Les Arènes de Metz',
        'Palais omnisports polyvalent accueillant des événements sportifs majeurs et des concerts de grande ampleur.',
        '0387629360', 'contact@arenes-metz.com', 'https://www.arenes-metz.com', 1, '5 Avenue Louis le Débonnaire',
        'Metz', '57000', 'France', NOW(), NOW(), '/static/images/structures/logos/logo_arenes.png',
        '/static/images/structures/covers/cover_arenes.jpg', NULL);

-- ##################################################
-- # 3. PEUPLEMENT DE LA TABLE `users`              #
-- ##################################################
-- Insertion des utilisateurs.
-- Le mot de passe commun est 'Tickly123!'.
-- Le hachage Bcrypt correspondant (cost factor 10) est utilisé pour tous les utilisateurs.
-- Hachage : $2a$10$3zZ.N1yOCD4Y0Y1QGvI4X.D.44v9b4nZf.c4nF5iE6sQ2gH3jK5uG
-- Les utilisateurs 'STRUCTURE_ADMINISTRATOR' sont liés à une structure via `structure_id`.

INSERT INTO users (id, first_name, last_name, email, password, role, structure_id, created_at, updated_at,
                   needs_structure_setup, user_type, avatar_path)
VALUES
-- Administrateurs de structure
(1, 'Alice', 'Martin', 'alice.martin@tickly.dev', '$2a$10$3zZ.N1yOCD4Y0Y1QGvI4X.D.44v9b4nZf.c4nF5iE6sQ2gH3jK5uG',
 'STRUCTURE_ADMINISTRATOR', 1, NOW(), NOW(), 0, 'User', '/static/images/avatars/avatar_1.png'),
(2, 'Baptiste', 'Dubois', 'baptiste.dubois@tickly.dev', '$2a$10$3zZ.N1yOCD4Y0Y1QGvI4X.D.44v9b4nZf.c4nF5iE6sQ2gH3jK5uG',
 'STRUCTURE_ADMINISTRATOR', 2, NOW(), NOW(), 0, 'User', '/static/images/avatars/avatar_2.png'),
(3, 'Chloé', 'Bernard', 'chloe.bernard@tickly.dev', '$2a$10$3zZ.N1yOCD4Y0Y1QGvI4X.D.44v9b4nZf.c4nF5iE6sQ2gH3jK5uG',
 'STRUCTURE_ADMINISTRATOR', 3, NOW(), NOW(), 0, 'User', '/static/images/avatars/avatar_3.png'),
(4, 'Damien', 'Robert', 'damien.robert@tickly.dev', '$2a$10$3zZ.N1yOCD4Y0Y1QGvI4X.D.44v9b4nZf.c4nF5iE6sQ2gH3jK5uG',
 'STRUCTURE_ADMINISTRATOR', 4, NOW(), NOW(), 0, 'User', '/static/images/avatars/avatar_4.png'),
(5, 'Élise', 'Moreau', 'elise.moreau@tickly.dev', '$2a$10$3zZ.N1yOCD4Y0Y1QGvI4X.D.44v9b4nZf.c4nF5iE6sQ2gH3jK5uG',
 'STRUCTURE_ADMINISTRATOR', 5, NOW(), NOW(), 0, 'User', '/static/images/avatars/avatar_5.png'),
(6, 'François', 'Petit', 'francois.petit@tickly.dev', '$2a$10$3zZ.N1yOCD4Y0Y1QGvI4X.D.44v9b4nZf.c4nF5iE6sQ2gH3jK5uG',
 'STRUCTURE_ADMINISTRATOR', 6, NOW(), NOW(), 0, 'User', '/static/images/avatars/avatar_6.png'),
(7, 'Gabrielle', 'Laurent', 'gabrielle.laurent@tickly.dev',
 '$2a$10$3zZ.N1yOCD4Y0Y1QGvI4X.D.44v9b4nZf.c4nF5iE6sQ2gH3jK5uG', 'STRUCTURE_ADMINISTRATOR', 7, NOW(), NOW(), 0, 'User',
 '/static/images/avatars/avatar_7.png'),
(8, 'Hugo', 'Simon', 'hugo.simon@tickly.dev', '$2a$10$3zZ.N1yOCD4Y0Y1QGvI4X.D.44v9b4nZf.c4nF5iE6sQ2gH3jK5uG',
 'STRUCTURE_ADMINISTRATOR', 11, NOW(), NOW(), 0, 'User', '/static/images/avatars/avatar_8.png'),
-- Spectateurs
(9, 'Inès', 'Michel', 'ines.michel@email.com', '$2a$10$3zZ.N1yOCD4Y0Y1QGvI4X.D.44v9b4nZf.c4nF5iE6sQ2gH3jK5uG',
 'SPECTATOR', NULL, NOW(), NOW(), 0, 'User', '/static/images/avatars/avatar_9.png'),
(10, 'Julien', 'Garcia', 'julien.garcia@email.com', '$2a$10$3zZ.N1yOCD4Y0Y1QGvI4X.D.44v9b4nZf.c4nF5iE6sQ2gH3jK5uG',
 'SPECTATOR', NULL, NOW(), NOW(), 0, 'User', '/static/images/avatars/avatar_10.png'),
(11, 'Karine', 'Lefebvre', 'karine.lefebvre@email.com', '$2a$10$3zZ.N1yOCD4Y0Y1QGvI4X.D.44v9b4nZf.c4nF5iE6sQ2gH3jK5uG',
 'SPECTATOR', NULL, NOW(), NOW(), 0, 'User', '/static/images/avatars/avatar_11.png'),
(12, 'Léo', 'Roux', 'leo.roux@email.com', '$2a$10$3zZ.N1yOCD4Y0Y1QGvI4X.D.44v9b4nZf.c4nF5iE6sQ2gH3jK5uG', 'SPECTATOR',
 NULL, NOW(), NOW(), 0, 'User', '/static/images/avatars/avatar_12.png');

-- ##################################################
-- # 4. MISE À JOUR DE LA TABLE `structures`        #
-- ##################################################
-- Assignation des administrateurs aux structures pour finaliser la relation.

UPDATE structures
SET administrator_id = 1
WHERE id = 1;
UPDATE structures
SET administrator_id = 2
WHERE id = 2;
UPDATE structures
SET administrator_id = 3
WHERE id = 3;
UPDATE structures
SET administrator_id = 4
WHERE id = 4;
UPDATE structures
SET administrator_id = 5
WHERE id = 5;
UPDATE structures
SET administrator_id = 6
WHERE id = 6;
UPDATE structures
SET administrator_id = 7
WHERE id = 7;
UPDATE structures
SET administrator_id = 8
WHERE id = 11;
-- Assignation des structures restantes à des administrateurs existants
UPDATE structures
SET administrator_id = 1
WHERE id = 8; -- Salle Braun gérée par Alice Martin
UPDATE structures
SET administrator_id = 2
WHERE id = 9; -- Metz Congrès géré par Baptiste Dubois
UPDATE structures
SET administrator_id = 3
WHERE id = 10;
-- Pompidou géré par Chloé Bernard

-- ##############################################################
-- # 5. PEUPLEMENT DE LA TABLE DE JOINTURE `structure_has_types` #
-- ##############################################################
-- Cette table établit la relation Many-to-Many entre les structures et leurs types.

INSERT INTO structure_has_types (structure_id, type_id)
VALUES
-- L'Arsenal (ID 1)
(1, 1),
(1, 6),
-- La BAM (ID 2)
(2, 1),
-- Opéra-Théâtre (ID 3)
(3, 2),
(3, 3),
-- Stade Saint-Symphorien (ID 4)
(4, 4),
-- Parc des Expositions (ID 5)
(5, 5),
-- Les Trinitaires (ID 6)
(6, 1),
(6, 2),
-- Comédie de Metz (ID 7)
(7, 2),
(7, 8),
-- Salle Braun (ID 8)
(8, 2),
-- Metz Congrès Robert Schuman (ID 9)
(9, 5),
-- Centre Pompidou-Metz (ID 10)
(10, 6),
-- Les Arènes de Metz (ID 11)
(11, 1),
(11, 4);

-- ##################################################
-- # 6. PEUPLEMENT DE LA TABLE `structure_areas`    #
-- ##################################################
-- Création des espaces physiques (salles, scènes, tribunes) pour chaque structure.

INSERT INTO structure_areas (id, structure_id, name, description, max_capacity, is_active)
VALUES (1, 1, 'Grande Salle', 'La salle de concert principale de l''Arsenal.', 1354, 1),
       (2, 1, 'Salle de l''Esplanade', 'Salle pour concerts de musique de chambre.', 350, 1),
       (3, 2, 'Grande Salle BAM', 'Salle de concert principale de la BAM.', 1115, 1),
       (4, 3, 'Salle principale', 'La salle historique de l''Opéra-Théâtre.', 750, 1),
       (5, 4, 'Tribune Nord', 'Tribune officielle du stade.', 7000, 1),
       (6, 4, 'Tribune Sud', 'Nouvelle tribune du stade.', 8000, 1),
       (7, 4, 'Tribune Est', 'Tribune latérale.', 7000, 1),
       (8, 5, 'Hall A', 'Hall d''exposition principal.', 5000, 1),
       (9, 6, 'La Chapelle', 'Salle de concert dans l''ancienne chapelle.', 350, 1),
       (10, 6, 'Le Caveau', 'Caveau voûté pour concerts de jazz.', 200, 1),
       (11, 7, 'Scène principale', 'La scène de la Comédie de Metz.', 120, 1),
       (12, 10, 'Galerie 1', 'Espace d''exposition principal au RDC.', 400, 1),
       (13, 11, 'Arène centrale', 'Espace modulable pour concerts et sports.', 7000, 1);

-- ##########################################################
-- # 7. PEUPLEMENT DE LA TABLE `audience_zone_templates`    #
-- ##########################################################
-- Création des modèles de zones d'audience pour chaque espace physique.

INSERT INTO audience_zone_templates (id, area_id, name, seating_type, max_capacity, is_active)
VALUES
-- Zones pour l'Arsenal (Area 1 & 2)
(1, 1, 'Parterre', 'SEATED', 800, 1),
(2, 1, 'Balcon', 'SEATED', 554, 1),
(3, 2, 'Placement libre', 'SEATED', 350, 1),
-- Zones pour la BAM (Area 3)
(4, 3, 'Fosse', 'STANDING', 1115, 1),
-- Zones pour l'Opéra-Théâtre (Area 4)
(5, 4, 'Orchestre', 'SEATED', 400, 1),
(6, 4, 'Loges', 'SEATED', 150, 1),
(7, 4, 'Balcons', 'SEATED', 200, 1),
-- Zones pour le Stade (Area 5, 6, 7)
(8, 5, 'Tribune Nord - Basse', 'SEATED', 4000, 1),
(9, 5, 'Tribune Nord - Haute', 'SEATED', 3000, 1),
(10, 6, 'Tribune Sud - Basse', 'SEATED', 5000, 1),
(11, 6, 'Tribune Sud - Loges VIP', 'SEATED', 500, 1),
-- Zones pour Les Trinitaires (Area 9 & 10)
(12, 9, 'Fosse Chapelle', 'STANDING', 350, 1),
(13, 10, 'Placement libre Caveau', 'MIXED', 200, 1);

-- ###############################################################
-- # 8. PEUPLEMENT DE LA TABLE `structure_gallery_images`        #
-- ###############################################################
-- Ajout de quelques images de galerie pour les structures.

INSERT INTO structure_gallery_images (structure_id, image_path)
VALUES (1, '/static/images/structures/gallery/arsenal_1.jpg'),
       (1, '/static/images/structures/gallery/arsenal_2.jpg'),
       (2, '/static/images/structures/gallery/bam_1.jpg'),
       (4, '/static/images/structures/gallery/stade_1.jpg'),
       (4, '/static/images/structures/gallery/stade_2.jpg'),
       (10, '/static/images/structures/gallery/pompidou_1.jpg');

-- ###################################################################
-- # 9. PEUPLEMENT DE LA TABLE `structure_social_media_links`        #
-- ###################################################################
-- Ajout de quelques liens de réseaux sociaux pour les structures.

INSERT INTO structure_social_media_links (structure_id, link)
VALUES (1, 'https://www.facebook.com/CiteMusicaleMetz'),
       (1, 'https://twitter.com/CiteMusicaleM'),
       (4, 'https://www.facebook.com/fcmetz'),
       (4, 'https://www.instagram.com/fcmetz'),
       (10, 'https://www.facebook.com/centrepompidoumetz.fr');

-- ###################################################################
-- # 10. PEUPLEMENT DE LA TABLE `user_favorite_structures`           #
-- ###################################################################
-- Ajout de quelques structures favorites pour les utilisateurs spectateurs.

INSERT INTO user_favorite_structures (id, user_id, structure_id, added_at)
VALUES (1, 9, 1, NOW()),  -- Inès Michel aime L'Arsenal
       (2, 9, 3, NOW()),  -- Inès Michel aime l'Opéra-Théâtre
       (3, 10, 4, NOW()), -- Julien Garcia aime le Stade Saint-Symphorien
       (4, 10, 11, NOW()),-- Julien Garcia aime Les Arènes
       (5, 11, 2, NOW()), -- Karine Lefebvre aime La BAM
       (6, 12, 10, NOW());
-- Léo Roux aime le Centre Pompidou

-- ##################################################
-- #          FIN DU SCRIPT DE PEUPLEMENT           #
-- ##################################################