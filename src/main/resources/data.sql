-- ######################################################################
-- #                                                                    #
-- #          SCRIPT DE PEUPLEMENT DE DONNÉES POUR L'APPLICATION TICKLY   #
-- #                                                                    #
-- ######################################################################
-- Ce script est conçu pour être exécuté par Spring Boot au démarrage.
-- Il doit être placé dans le répertoire `src/main/resources` et nommé `data.sql`.
-- L'ordre des insertions est crucial pour respecter les contraintes de clés étrangères.

-- Désactivation des contraintes de clés étrangères pour permettre l'insertion dans un ordre flexible
SET FOREIGN_KEY_CHECKS = 0;

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
        NOW(), NOW(), 'logo_arsenal.jpg',
        'cover_arsenal.jpg', NULL),
       (2, 'La BAM (Boîte à Musiques)',
        'Salle de musiques actuelles moderne, avec studios de répétition et une programmation éclectique.',
        '0387393470', 'contact@bam-metz.fr', 'https://www.citemusicale-metz.fr/la-bam', 1, '20 Boulevard d''Alsace',
        'Metz', '57070', 'France', NOW(), NOW(), 'logo_bam.jpg',
        'cover_bam.jpg', NULL),
       (3, 'Opéra-Théâtre de Metz',
        'Le plus ancien opéra-théâtre en activité en France, proposant des productions lyriques et théâtrales.',
        '0387156060', 'billetterie@opera.metzmetropole.fr', 'https://opera.eurometropolemetz.eu', 1,
        '4-5 Place de la Comédie', 'Metz', '57000', 'France', NOW(), NOW(),
        'logo_opera.jpg', 'cover_opera.jpg', NULL),
       (4, 'Stade Saint-Symphorien', 'Principal stade de football de la ville, accueillant les matchs du FC Metz.',
        '0387667215', 'contact@fcmetz.com', 'https://www.fcmetz.com', 1, '3 Allée Saint-Symphorien',
        'Longeville-lès-Metz', '57050', 'France', NOW(), NOW(), 'logo_fcmetz.png',
        'cover_fcmetz.jpg', NULL),
       (5, 'Parc des Expositions de Metz Métropole',
        'Vaste complexe pour foires, salons professionnels et expositions de grande envergure.', '0387556600',
        'info@metz-expo.com', 'https://www.metz-expo.com', 1, 'Rue de la Grange aux Bois', 'Metz', '57070', 'France',
        NOW(), NOW(), 'logo_parcexpo.jpg',
        'cover_parcexpo.jpg', NULL),
       (6, 'Les Trinitaires',
        'Lieu culturel historique situé dans un ancien couvent, avec un caveau jazz et une chapelle pour concerts.',
        '0387200303', 'contact@lestrinitaires.com', 'https://www.citemusicale-metz.fr/les-trinitaires', 1,
        '12 Rue des Trinitaires', 'Metz', '57000', 'France', NOW(), NOW(),
        'logo_trinitaires.jpg',
        'cover_trinitaires.jpg', NULL),
       (7, 'Comédie de Metz', 'Théâtre dédié à l''humour et aux comédies, situé dans un quartier historique.',
        '0781511512', 'comediedemetz@gmail.com', 'https://www.comediedemetz.fr', 1, '1/3 Rue du Pont Saint-Marcel',
        'Metz', '57000', 'France', NOW(), NOW(), 'logo_comedie.png',
        'cover_comedie.jpg', NULL),
       (8, 'Salle Braun', 'Théâtre intimiste proposant une programmation variée, notamment pour le jeune public.',
        '0668092756', 'directionsallebraun@gmail.com', 'https://sallebraun.com', 1, '18 Rue Mozart', 'Metz', '57000',
        'France', NOW(), NOW(), 'logo_braun.png',
        'cover_braun.jpg', NULL),
       (9, 'Metz Congrès Robert Schuman',
        'Centre de congrès moderne face au Centre Pompidou, idéal pour les conventions et séminaires.', '0387556600',
        'congres@metz-evenements.com', 'https://www.metz-evenements.com', 1, '112 Rue aux Arènes', 'Metz', '57000',
        'France', NOW(), NOW(), 'logo_congres.jpg',
        'cover_congres.png', NULL),
       (10, 'Centre Pompidou-Metz',
        'Musée d''art moderne et contemporain de renommée internationale, à l''architecture audacieuse.', '0387153939',
        'contact@centrepompidou-metz.fr', 'https://www.centrepompidou-metz.fr', 1, '1 Parvis des Droits-de-l''Homme',
        'Metz', '57000', 'France', NOW(), NOW(), 'logo_pompidou.png',
        'cover_pompidou.jpg', NULL),
       (11, 'Les Arènes de Metz',
        'Palais omnisports polyvalent accueillant des événements sportifs majeurs et des concerts de grande ampleur.',
        '0387629360', 'contact@arenes-metz.com', 'https://www.arenes-metz.com', 1, '5 Avenue Louis le Débonnaire',
        'Metz', '57000', 'France', NOW(), NOW(), 'logo_arenes.png',
        'cover_arenes.jpg', NULL);

-- ##################################################
-- # 3. PEUPLEMENT DE LA TABLE `users`              #
-- ##################################################
-- Insertion des utilisateurs.
-- Le mot de passe commun est 'Tickly123!'.
-- Le hachage Bcrypt correspondant (cost factor 10) est utilisé pour tous les utilisateurs.
-- Hachage : $2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii
-- Les utilisateurs 'STRUCTURE_ADMINISTRATOR' sont liés à une structure via `structure_id`.
-- La colonne `user_type` est définie à 'User' pour correspondre à la stratégie d'héritage de base.


INSERT INTO users (id, first_name, last_name, email, password, role, structure_id, created_at, updated_at, user_type,
                   avatar_path, is_email_validated)
VALUES
-- Administrateurs de structure
(1, 'Alice', 'Martin', 'alice.martin@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 1, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', 'avatar_1.png', 1),
(2, 'Baptiste', 'Dubois', 'baptiste.dubois@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 2, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', 'avatar_2.png', 1),
(3, 'Chloé', 'Bernard', 'chloe.bernard@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 3, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', 'avatar_3.png', 1),
(4, 'Damien', 'Robert', 'damien.robert@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 4, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', 'avatar_4.png', 1),
(5, 'Élise', 'Moreau', 'elise.moreau@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 5, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', 'avatar_5.png', 1),
(6, 'François', 'Petit', 'francois.petit@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 6, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', 'avatar_6.png', 1),
(7, 'Gabrielle', 'Laurent', 'gabrielle.laurent@tickly.dev',
 '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii', 'STRUCTURE_ADMINISTRATOR', 7, NOW(), NOW(),
 'STRUCTURE_ADMINISTRATOR',
 'avatar_7.png', 1),
(8, 'Hugo', 'Simon', 'hugo.simon@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 11, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', 'avatar_8.png', 1),
-- Spectateurs
(9, 'Inès', 'Michel', 'ines.michel@email.com', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'SPECTATOR', NULL, NOW(), NOW(), 'SPECTATOR', 'avatar_9.png', 1),
(10, 'Julien', 'Garcia', 'julien.garcia@email.com', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'SPECTATOR', NULL, NOW(), NOW(), 'SPECTATOR', 'avatar_10.png', 1),
(11, 'Karine', 'Lefebvre', 'karine.lefebvre@email.com', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'SPECTATOR', NULL, NOW(), NOW(), 'SPECTATOR', 'avatar_11.png', 1),
(12, 'Léo', 'Roux', 'leo.roux@email.com', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii', 'SPECTATOR',
 NULL, NOW(), NOW(), 'SPECTATOR', 'avatar_12.png', 1),
(13, 'Alice', 'Martin', 'a@a.com', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 1, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', '', 1);

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
VALUES
-- L'Arsenal (ID 1)
(1, 1, 'Grande Salle', 'La salle de concert principale de l''Arsenal.', 1354, 1),
(2, 1, 'Salle de l''Esplanade', 'Salle pour concerts de musique de chambre.', 350, 1),

-- La BAM (ID 2)
(3, 2, 'Grande Salle BAM', 'Salle de concert principale de la BAM.', 1115, 1),

-- Opéra-Théâtre de Metz (ID 3)
(4, 3, 'Salle principale', 'La salle historique de l''Opéra-Théâtre.', 750, 1),

-- Stade Saint-Symphorien (ID 4)
(5, 4, 'Tribune Nord', 'Tribune officielle du stade.', 7000, 1),
(6, 4, 'Tribune Sud', 'Nouvelle tribune du stade.', 8000, 1),
(7, 4, 'Tribune Est', 'Tribune latérale.', 7000, 1),

-- Parc des Expositions (ID 5)
(8, 5, 'Hall A', 'Hall d''exposition principal.', 5000, 1),
(9, 5, 'Hall B', 'Hall d''exposition secondaire.', 3000, 1),

-- Les Trinitaires (ID 6)
(10, 6, 'La Chapelle', 'Salle de concert dans l''ancienne chapelle.', 350, 1),
(11, 6, 'Le Caveau', 'Caveau voûté pour concerts de jazz.', 200, 1),

-- Comédie de Metz (ID 7)
(12, 7, 'Scène principale', 'La scène de la Comédie de Metz.', 120, 1),

-- Salle Braun (ID 8) - MANQUAIT
(13, 8, 'Salle Braun', 'Théâtre intimiste pour spectacles variés.', 80, 1),

-- Metz Congrès Robert Schuman (ID 9) - MANQUAIT
(14, 9, 'Auditorium principal', 'Grand auditorium pour conférences et congrès.', 500, 1),
(15, 9, 'Salle de réunion A', 'Salle modulable pour séminaires.', 150, 1),

-- Centre Pompidou-Metz (ID 10)
(16, 10, 'Galerie 1', 'Espace d''exposition principal au RDC.', 400, 1),
(17, 10, 'Galerie 2', 'Espace d''exposition à l''étage.', 300, 1),

-- Les Arènes de Metz (ID 11)
(18, 11, 'Arène centrale', 'Espace modulable pour concerts et sports.', 7000, 1);

-- ##########################################################
-- # 7. PEUPLEMENT DE LA TABLE `audience_zone_templates`    #
-- ##########################################################
-- Création des modèles de zones d'audience pour chaque espace physique.

INSERT INTO audience_zone_template (id, area_id, name, seating_type, max_capacity, is_active)
VALUES
-- L'Arsenal - Grande Salle (Area 1)
(1, 1, 'Parterre', 'SEATED', 800, 1),
(2, 1, 'Balcon', 'SEATED', 554, 1),

-- L'Arsenal - Salle de l'Esplanade (Area 2)
(3, 2, 'Placement libre', 'SEATED', 350, 1),

-- La BAM - Grande Salle (Area 3)
(4, 3, 'Fosse', 'STANDING', 1115, 1),

-- Opéra-Théâtre - Salle principale (Area 4)
(5, 4, 'Orchestre', 'SEATED', 400, 1),
(6, 4, 'Loges', 'SEATED', 150, 1),
(7, 4, 'Balcons', 'SEATED', 200, 1),

-- Stade - Tribune Nord (Area 5)
(8, 5, 'Tribune Nord - Basse', 'SEATED', 4000, 1),
(9, 5, 'Tribune Nord - Haute', 'SEATED', 3000, 1),

-- Stade - Tribune Sud (Area 6)
(10, 6, 'Tribune Sud - Basse', 'SEATED', 5000, 1),
(11, 6, 'Tribune Sud - Haute', 'SEATED', 2500, 1),
(12, 6, 'Loges VIP', 'SEATED', 500, 1),

-- Stade - Tribune Est (Area 7)
(13, 7, 'Tribune Est - Basse', 'SEATED', 4000, 1),
(14, 7, 'Tribune Est - Haute', 'SEATED', 3000, 1),

-- Parc Expo - Hall A (Area 8)
(15, 8, 'Zone exposition A', 'STANDING', 5000, 1),

-- Parc Expo - Hall B (Area 9)
(16, 9, 'Zone exposition B', 'STANDING', 3000, 1),

-- Trinitaires - La Chapelle (Area 10)
(17, 10, 'Fosse Chapelle', 'STANDING', 350, 1),

-- Trinitaires - Le Caveau (Area 11)
(18, 11, 'Placement libre Caveau', 'MIXED', 200, 1),

-- Comédie de Metz - Scène principale (Area 12)
(19, 12, 'Salle spectacle', 'SEATED', 120, 1),

-- Salle Braun - Salle Braun (Area 13)
(20, 13, 'Parterre', 'SEATED', 60, 1),
(21, 13, 'Balcon', 'SEATED', 20, 1),

-- Metz Congrès - Auditorium principal (Area 14)
(22, 14, 'Parterre auditorium', 'SEATED', 300, 1),
(23, 14, 'Balcon auditorium', 'SEATED', 200, 1),

-- Metz Congrès - Salle de réunion A (Area 15)
(24, 15, 'Configuration théâtre', 'SEATED', 150, 1),

-- Centre Pompidou - Galerie 1 (Area 16)
(25, 16, 'Espace principal', 'STANDING', 400, 1),

-- Centre Pompidou - Galerie 2 (Area 17)
(26, 17, 'Espace secondaire', 'STANDING', 300, 1),

-- Arènes de Metz - Arène centrale (Area 18)
(27, 18, 'Parterre central', 'MIXED', 3500, 1),
(28, 18, 'Gradins', 'SEATED', 3500, 1);

-- ###############################################################
-- # 8. PEUPLEMENT DE LA TABLE `structure_gallery_images`        #
-- ###############################################################
-- Ajout de quelques images de galerie pour les structures.

INSERT INTO structure_gallery_images (structure_id, image_path)
VALUES (1, 'arsenal_1.jpg'),
       (1, 'arsenal_2.jpg'),
       (2, 'bam_1.jpg'),
       (4, 'stade_1.jpg'),
       (4, 'stade_2.jpg'),
       (10, 'pompidou_1.jpg');

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
-- # 11. PEUPLEMENT DE LA TABLE `event_categories`  #
-- ##################################################
-- Ces données sont nécessaires pour créer des événements.

INSERT INTO event_categories (id, name)
VALUES (1, 'Concert'),
       (2, 'Théâtre'),
       (3, 'Festival'),
       (4, 'Sport'),
       (5, 'Conférence'),
       (6, 'Exposition'),
       (7, 'Humour'),
       (8, 'Opéra'),
       (9, 'Danse'),
       (10, 'Jeune public');

-- ##################################################
-- # 12. PEUPLEMENT DE LA TABLE `events`            #
-- ##################################################
-- Ajout d'événements variés pour peupler l'application.
-- Les dates sont définies dynamiquement par rapport à la date d'exécution du script.
-- NOTE: La colonne category_id sera supprimée ultérieurement au profit de la relation Many-to-Many

INSERT INTO events (id, name, short_description, full_description, start_date, end_date, status,
                    display_on_homepage, is_featured_event, structure_id, creator_id, created_at,
                    updated_at, main_photo_path, street, city, zip_code, country)
VALUES (1, 'Orchestre National de Metz - Saison Classique',
        'Une soirée exceptionnelle avec l''Orchestre National de Metz.',
        'L''Orchestre National de Metz Grand Est vous invite à une soirée inoubliable sous la direction de son chef principal. Au programme, des œuvres de Beethoven et Mozart qui raviront les amateurs de musique classique. Une expérience acoustique unique dans la Grande Salle de l''Arsenal.',
        DATE_ADD(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY) + INTERVAL 3 HOUR, 'PUBLISHED', 1, 1,
        1, 1, NOW(), NOW(), 'orchestre_metz.jpg', '3 Avenue Ney', 'Metz', '57000', 'France'),

       (2, 'Festival Electronic Waves',
        'Trois jours de musique électronique avec les meilleurs DJs internationaux.',
        'Electronic Waves revient pour sa 8ème édition avec une programmation exceptionnelle. Découvrez les sonorités les plus avant-gardistes de la scène électronique internationale dans l''ambiance unique de la BAM. Trois scènes, plus de 20 artistes, et une expérience immersive garantie.',
        DATE_ADD(NOW(), INTERVAL 45 DAY), DATE_ADD(NOW(), INTERVAL 47 DAY), 'PUBLISHED', 1, 1, 2, 2, NOW(), NOW(),
        'electronic_waves.jpg', '20 Boulevard d''Alsace', 'Metz', '57070', 'France'),

       (3, 'La Traviata - Opéra de Verdi',
        'Production exceptionnelle de l''opéra le plus célèbre de Verdi.',
        'L''Opéra-Théâtre de Metz présente une nouvelle production de La Traviata dans une mise en scène contemporaine saisissante. Avec la soprano internationale Maria Dolores et le ténor français Jean-Baptiste Millot. Direction musicale : Maestro Antonio Benedetti.',
        DATE_ADD(NOW(), INTERVAL 60 DAY), DATE_ADD(NOW(), INTERVAL 60 DAY) + INTERVAL 3 HOUR, 'PUBLISHED', 1, 1,
        3, 3, NOW(), NOW(), 'traviata.jpg', '4-5 Place de la Comédie', 'Metz', '57000', 'France'),

       (4, 'FC Metz vs Olympique Lyonnais',
        'Match de Ligue 1 au Stade Saint-Symphorien.',
        'Venez encourager les Grenats lors de ce match crucial de Ligue 1 face à l''Olympique Lyonnais. Ambiance garantie dans le chaudron messin ! Billets disponibles pour toutes les tribunes. Ouverture des portes 1h30 avant le coup d''envoi.',
        DATE_ADD(NOW(), INTERVAL 25 DAY), DATE_ADD(NOW(), INTERVAL 25 DAY) + INTERVAL 2 HOUR, 'PUBLISHED', 1, 0,
        4, 4, NOW(), NOW(), 'fcmetz_lyon.jpg', '3 Allée Saint-Symphorien', 'Longeville-lès-Metz', '57050', 'France'),

       (5, 'Salon Habitat & Jardin',
        'Le salon de référence pour l''habitat et le jardinage en Lorraine.',
        'Découvrez les dernières tendances en matière d''habitat, de décoration et de jardinage. Plus de 200 exposants, des démonstrations, des conférences thématiques et de nombreux conseils d''experts. Idéal pour vos projets de rénovation et d''aménagement.',
        DATE_ADD(NOW(), INTERVAL 40 DAY), DATE_ADD(NOW(), INTERVAL 43 DAY), 'PUBLISHED', 0, 0, 5, 5, NOW(), NOW(),
        'salon_habitat.jpg', 'Rue de la Grange aux Bois', 'Metz', '57070', 'France'),

       (6, 'Jazz Session - Les Trinitaires',
        'Soirée jazz intime dans le caveau historique.',
        'Plongez dans l''atmosphère feutrée du caveau des Trinitaires pour une soirée jazz exceptionnelle. Le quartet de Sarah Mitchell vous transportera dans l''univers du jazz moderne avec des reprises revisitées et des compositions originales.',
        DATE_ADD(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 15 DAY) + INTERVAL 2 HOUR, 'PUBLISHED', 1, 0,
        6, 6, NOW(), NOW(), 'jazz_trinitaires.jpg', '12 Rue des Trinitaires', 'Metz', '57000', 'France'),

       (7, 'Jamel Comedy Club - Tournée',
        'Les humoristes du Jamel Comedy Club en spectacle.',
        'Retrouvez les talents du Jamel Comedy Club pour une soirée d''humour inoubliable. Au programme : Yacine Belhousse, Sofia Aram et Ahmed Sylla dans leurs derniers spectacles. Rires garantis dans l''intimité de la Comédie de Metz.',
        DATE_ADD(NOW(), INTERVAL 20 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY) + INTERVAL 2 HOUR, 'PUBLISHED', 1, 1,
        7, 7, NOW(), NOW(), 'comedy_club.jpg', '1/3 Rue du Pont Saint-Marcel', 'Metz', '57000', 'France'),

       (8, 'Congrès International de Cybersécurité',
        'Trois jours dédiés aux enjeux de la cybersécurité.',
        'Le plus grand événement cybersécurité de l''Est de la France. Conférences, ateliers, démonstrations et networking avec les experts du secteur. Plus de 50 intervenants internationaux et 1000 participants attendus.',
        DATE_ADD(NOW(), INTERVAL 80 DAY), DATE_ADD(NOW(), INTERVAL 82 DAY), 'PUBLISHED', 0, 0, 9, 2, NOW(), NOW(),
        'cybersec_congress.jpg', '112 Rue aux Arènes', 'Metz', '57000', 'France'),

       (9, 'Exposition : "Art et Intelligence Artificielle"',
        'Découverte des nouvelles formes d''art générées par l''IA.',
        'Le Centre Pompidou-Metz explore les frontières entre art et technologie dans cette exposition révolutionnaire. Œuvres interactives, installations immersives et rencontres avec les artistes pionniers de l''art numérique.',
        DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), 'PUBLISHED', 1, 1, 10, 3, NOW(), NOW(),
        'expo_ia.jpg', '1 Parvis des Droits-de-l''Homme', 'Metz', '57000', 'France'),

       (10, 'Concert Rap - Nekfeu',
        'Nekfeu en concert aux Arènes de Metz.',
        'L''un des rappeurs français les plus talentueux de sa génération se produit aux Arènes de Metz. Venez découvrir ses nouveaux titres dans une mise en scène spectaculaire avec un écran géant et des effets pyrotechniques.',
        DATE_ADD(NOW(), INTERVAL 35 DAY), DATE_ADD(NOW(), INTERVAL 35 DAY) + INTERVAL 3 HOUR, 'PUBLISHED', 1, 1,
        11, 8, NOW(), NOW(), 'nekfeu_concert.jpg', '5 Avenue Louis le Débonnaire', 'Metz', '57000', 'France'),

       (11, 'Spectacle Familial - "Le Petit Prince"',
        'Adaptation théâtrale du chef-d''œuvre d''Antoine de Saint-Exupéry.',
        'Une mise en scène poétique et moderne du Petit Prince destinée à toute la famille. Avec des marionnettes, des projections et une bande sonore originale, ce spectacle enchantera petits et grands.',
        DATE_ADD(NOW(), INTERVAL 50 DAY), DATE_ADD(NOW(), INTERVAL 50 DAY) + INTERVAL 90 MINUTE, 'PUBLISHED', 1, 0,
        8, 1, NOW(), NOW(), 'petit_prince.jpg', '18 Rue Mozart', 'Metz', '57000', 'France'),

       (12, 'Festival de Danse Contemporaine',
        'Trois jours de danse contemporaine avec des compagnies internationales.',
        'Le Festival Mouvements revient avec une programmation éclectique mêlant danse contemporaine, performance et arts numériques. 8 compagnies, 15 représentations et des masterclass ouvertes au public.',
        DATE_ADD(NOW(), INTERVAL 70 DAY), DATE_ADD(NOW(), INTERVAL 72 DAY), 'DRAFT', 0, 0, 1, 1, NOW(), NOW(),
        'festival_danse.jpg', '3 Avenue Ney', 'Metz', '57000', 'France');

-- ###################################################################
-- # 13. PEUPLEMENT DE LA TABLE DE JOINTURE `event_has_categories`   #
-- ###################################################################
-- Attribution de catégories multiples aux événements pour démontrer la flexibilité du système

INSERT INTO event_has_categories (event_id, category_id)
VALUES
-- Orchestre National de Metz (Événement 1) - Concert
(1, 1),

-- Festival Electronic Waves (Événement 2) - Concert + Festival
(2, 1),
(2, 3),

-- La Traviata (Événement 3) - Opéra + Théâtre
(3, 8),
(3, 2),

-- FC Metz vs Olympique Lyonnais (Événement 4) - Sport
(4, 4),

-- Salon Habitat & Jardin (Événement 5) - Conférence + Exposition
(5, 5),
(5, 6),

-- Jazz Session (Événement 6) - Concert
(6, 1),

-- Jamel Comedy Club (Événement 7) - Humour + Théâtre
(7, 7),
(7, 2),

-- Congrès Cybersécurité (Événement 8) - Conférence
(8, 5),

-- Exposition IA (Événement 9) - Exposition
(9, 6),

-- Concert Nekfeu (Événement 10) - Concert + Festival
(10, 1),
(10, 3),

-- Le Petit Prince (Événement 11) - Théâtre + Jeune public
(11, 2),
(11, 10),

-- Festival de Danse (Événement 12) - Danse + Festival
(12, 9),
(12, 3);

-- ######################################################
-- # 13. PEUPLEMENT DE LA TABLE `event_audience_zone`  #
-- ######################################################
-- Création des zones d'audience pour les événements.
-- Certaines sont basées sur des modèles, d'autres sont spécifiques.

INSERT INTO event_audience_zone (id, event_id, template_id, allocated_capacity)
VALUES
-- Événement 1: Orchestre National de Metz (Arsenal - Grande Salle)
(1, 1, 1, 750),     -- Parterre avec capacité réduite
(2, 1, 2, 500),     -- Balcon avec capacité réduite

-- Événement 2: Festival Electronic Waves (BAM)
(3, 2, 4, 1000),    -- Fosse avec capacité légèrement réduite

-- Événement 3: La Traviata (Opéra-Théâtre)
(4, 3, 5, 380),     -- Orchestre
(5, 3, 6, 140),     -- Loges
(6, 3, 7, 180),     -- Balcons

-- Événement 4: FC Metz vs Olympique Lyonnais (Stade)
(7, 4, 8, 3800),    -- Tribune Nord - Basse
(8, 4, 9, 2800),    -- Tribune Nord - Haute
(9, 4, 10, 4500),   -- Tribune Sud - Basse
(10, 4, 11, 2200),  -- Tribune Sud - Haute

-- Événement 5: Salon Habitat & Jardin (Parc Expo)
(11, 5, 15, 4500),  -- Zone exposition A
(12, 5, 16, 2800),  -- Zone exposition B

-- Événement 6: Jazz Session (Trinitaires - Caveau)
(13, 6, 18, 180),   -- Placement libre Caveau

-- Événement 7: Jamel Comedy Club (Comédie de Metz)
(14, 7, 19, 120),   -- Salle spectacle

-- Événement 8: Congrès Cybersécurité (Metz Congrès)
(15, 8, 22, 280),   -- Parterre auditorium
(16, 8, 23, 180),   -- Balcon auditorium

-- Événement 9: Exposition Art & IA (Centre Pompidou)
(17, 9, 25, 350),   -- Espace principal
(18, 9, 26, 250),   -- Espace secondaire

-- Événement 10: Concert Nekfeu (Arènes de Metz)
(19, 10, 27, 3200), -- Parterre central
(20, 10, 28, 3000), -- Gradins

-- Événement 11: Le Petit Prince (Salle Braun)
(21, 11, 20, 50),   -- Parterre
(22, 11, 21, 18),   -- Balcon

-- Événement 12: Festival de Danse (Trinitaires - Chapelle)
(23, 12, 17, 320);
-- Fosse Chapelle

-- ######################################################
-- # 14. PEUPLEMENT DE LA TABLE `event_tags`            #
-- ######################################################
-- Ajout de tags pour faciliter la recherche d'événements.

INSERT INTO event_tags (event_id, tag)
VALUES (1, 'Classique'),
       (1, 'Orchestre'),
       (1, 'Beethoven'),
       (2, 'Rock'),
       (2, 'Festival'),
       (2, 'Musiques Actuelles'),
       (3, 'Football'),
       (3, 'Ligue 1'),
       (3, 'FC Metz'),
       (4, 'Opéra'),
       (4, 'Verdi'),
       (4, 'Lyrique'),
       (6, 'Art Contemporain'),
       (6, 'Exposition'),
       (6, 'Pompidou'),
       (7, 'Humour'),
       (7, 'Stand-up'),
       (8, 'Pop'),
       (8, 'Concert'),
       (8, 'International');

-- ######################################################
-- # 15. PEUPLEMENT DE LA TABLE `event_gallery_images`  #
-- ######################################################
-- Ajout d'images de galerie pour certains événements.

INSERT INTO event_gallery_images (event_id, image_path)
VALUES (3, 'fcmetz_1.jpg'),
       (3, 'fcmetz_2.jpg'),
       (6, 'pompidou_gallery_1.jpg'),
       (8, 'popstar_1.jpg'),
       (8, 'popstar_2.jpg'),
       (8, 'popstar_3.jpg');


-- ######################################################
-- # 16. PEUPLEMENT DE LA TABLE `reservations`          #
-- ######################################################
-- Création de réservations groupant plusieurs billets pour différents utilisateurs.

INSERT INTO reservations (id, user_id, reservation_date, total_amount)
VALUES
-- Réservation d'Inès Michel (ID 9) pour le concert à l'Arsenal (Event 1)
(1, 9, NOW() - INTERVAL 10 DAY, 0.00),
-- Réservation de Julien Garcia (ID 10) pour le match du FC Metz (Event 3)
(2, 10, NOW() - INTERVAL 5 DAY, 0.00),
-- Réservation de Karine Lefebvre (ID 11) pour le festival à la BAM (Event 2)
(3, 11, NOW() - INTERVAL 3 DAY, 0.00),
-- Autre réservation d'Inès Michel (ID 9) pour le concert Pop aux Arènes (Event 8)
(4, 9, NOW() - INTERVAL 2 DAY, 0.00),
-- Réservation de Léo Roux (ID 12) pour l'exposition à Pompidou (Event 6)
(5, 12, NOW() - INTERVAL 1 DAY, 0.00);


-- ######################################################
-- # 17. PEUPLEMENT DE LA TABLE `tickets`               #
-- ######################################################
-- Création des billets individuels associés aux réservations.
-- Les UUIDs pour les ID de billets sont générés ici pour la démonstration.
-- En pratique, la base de données ou l'application les générerait.

INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
VALUES
-- Billets pour la Réservation 1 (Inès Michel, Event 1, Zone 1 - Parterre)
(UUID_TO_BIN(UUID()), 1, 1, 9, 1, UUID(), 'Inès', 'Michel', 'ines.michel@email.com', 'VALID',
 (SELECT reservation_date FROM reservations WHERE id = 1)),
(UUID_TO_BIN(UUID()), 1, 1, 9, 1, UUID(), 'Lucas', 'Petit', 'lucas.petit@email.com', 'VALID',
 (SELECT reservation_date FROM reservations WHERE id = 1)),

-- Billets pour la Réservation 2 (Julien Garcia, Event 3, Zone 5 - Tribune Sud)
(UUID_TO_BIN(UUID()), 3, 5, 10, 2, UUID(), 'Julien', 'Garcia', 'julien.garcia@email.com', 'VALID',
 (SELECT reservation_date FROM reservations WHERE id = 2)),
(UUID_TO_BIN(UUID()), 3, 5, 10, 2, UUID(), 'Emma', 'Leroy', 'emma.leroy@email.com', 'USED',
 (SELECT reservation_date FROM reservations WHERE id = 2)),
(UUID_TO_BIN(UUID()), 3, 5, 10, 2, UUID(), 'Hugo', 'Martinez', 'hugo.martinez@email.com', 'CANCELLED',
 (SELECT reservation_date FROM reservations WHERE id = 2));


-- ######################################################
-- # 18. PEUPLEMENT DE LA TABLE `friendships`           #
-- ######################################################
-- Ajout de relations d'amitié entre les utilisateurs.
INSERT INTO friendships (id, sender_id, receiver_id, status, created_at, updated_at)
VALUES (1, 9, 10, 'ACCEPTED', NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 19 DAY), -- Inès et Julien sont amis
       (2, 9, 11, 'PENDING', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),    -- Inès a envoyé une demande à Karine
       (3, 12, 9, 'PENDING', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),    -- Léo a envoyé une demande à Inès
       (4, 10, 12, 'REJECTED', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 4 DAY);
-- Julien a envoyé une demande à Léo, qui a refusé


-- ##################################################
-- # 19. PEUPLEMENT DE LA TABLE `teams` & `team_members` #
-- ##################################################
-- Création d'équipes pour les structures et ajout de membres.
-- Chaque structure a une équipe par défaut. L'administrateur est le premier membre.
INSERT INTO teams (id, structure_id, name)
VALUES (1, 1, 'Équipe L''Arsenal'),
       (2, 2, 'Équipe La BAM'),
       (3, 3, 'Équipe Opéra-Théâtre');

INSERT INTO team_members (id, team_id, user_id, email, role, status, invited_at, joined_at)
VALUES
-- Membres pour L'Arsenal (Team 1)
(1, 1, 1, 'alice.martin@tickly.dev', 'STRUCTURE_ADMINISTRATOR', 'ACTIVE', NOW() - INTERVAL 30 DAY,
 NOW() - INTERVAL 30 DAY),
(2, 1, NULL, 'membre.orga@arsenal.fr', 'ORGANIZATION_SERVICE', 'PENDING_INVITATION', NOW() - INTERVAL 5 DAY, NULL),
(3, 1, NULL, 'membre.reserv@arsenal.fr', 'RESERVATION_SERVICE', 'PENDING_INVITATION', NOW() - INTERVAL 5 DAY, NULL),

-- Membres pour La BAM (Team 2)
(4, 2, 2, 'baptiste.dubois@tickly.dev', 'STRUCTURE_ADMINISTRATOR', 'ACTIVE', NOW() - INTERVAL 30 DAY,
 NOW() - INTERVAL 30 DAY),
(5, 2, 10, 'julien.garcia@email.com', 'ORGANIZATION_SERVICE', 'ACTIVE', NOW() - INTERVAL 10 DAY,
 NOW() - INTERVAL 9 DAY), -- Julien est aussi dans l'équipe de la BAM

-- Membres pour Opéra-Théâtre (Team 3)
(6, 3, 3, 'chloe.bernard@tickly.dev', 'STRUCTURE_ADMINISTRATOR', 'ACTIVE', NOW() - INTERVAL 30 DAY,
 NOW() - INTERVAL 30 DAY);


-- ##################################################
-- # 20. PEUPLEMENT DE LA TABLE `verification_tokens` #
-- ##################################################
-- Ajout de quelques tokens de vérification pour illustrer différents cas.
INSERT INTO verification_tokens (id, user_id, token, token_type, expiry_date, is_used, payload)
VALUES (1, 11, UUID(), 'EMAIL_VALIDATION', NOW() + INTERVAL 1 DAY, 0, NULL), -- Token de validation d'email pour Karine
       (2, 12, UUID(), 'PASSWORD_RESET', NOW() + INTERVAL 1 HOUR, 0, NULL),  -- Token de reset de mot de passe pour Léo
       (3, NULL, UUID(), 'TEAM_INVITATION', NOW() + INTERVAL 7 DAY, 0,
        '{"memberId": 2}'),                                                  -- Token d'invitation pour membre.orga@arsenal.fr
       (4, 9, UUID(), 'ACCOUNT_DELETION_CONFIRMATION', NOW() - INTERVAL 2 HOUR, 1, NULL);
-- Token de suppression de compte expiré et utilisé pour Inès


-- Réactivation des contraintes de clés étrangères
SET FOREIGN_KEY_CHECKS = 1;

-- ##################################################
-- #          FIN DU SCRIPT DE PEUPLEMENT           #
-- ##################################################