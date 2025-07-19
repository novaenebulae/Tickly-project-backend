-- ######################################################################
-- #                                                                    #
-- #          SCRIPT DE PEUPLEMENT DE DONNÉES POUR L'APPLICATION TICKLY   #
-- #                                                                    #
-- ######################################################################
-- Ce script est conçu pour être exécuté par Spring Boot au démarrage.
-- Il doit être placé dans le répertoire `src/main/resources` et nommé `data.sql`.
-- L'ordre des insertions est crucial pour respecter les contraintes de clés étrangères.
SET NAMES 'utf8mb4';
SET CHARACTER SET utf8mb4;
SET collation_connection = 'utf8mb4_unicode_ci';

-- Désactivation des contraintes de clés étrangères pour permettre l'insertion dans un ordre flexible
SET FOREIGN_KEY_CHECKS = 0;

DELETE
FROM tickets;
DELETE
FROM reservations;
DELETE
FROM event_has_categories;
DELETE
FROM event_gallery_images;
DELETE
FROM event_tags;
DELETE
FROM event_audience_zone;
DELETE
FROM events;
DELETE
FROM friendships;
DELETE
FROM team_members;
DELETE
FROM teams;
DELETE
FROM user_favorite_structures;
DELETE
FROM verification_tokens;
DELETE
FROM audience_zone_template;
DELETE
FROM structure_areas;
DELETE
FROM structure_has_types;
DELETE
FROM structure_gallery_images;
DELETE
FROM structure_social_media_links;
DELETE
FROM users;
DELETE
FROM structures;
DELETE
FROM event_categories;
DELETE
FROM structure_types;

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
                        created_at, updated_at, logo_path, cover_path)
VALUES (1, 'L''Arsenal',
        'Prestigieuse salle de concert et lieu d''exposition réputé pour son acoustique exceptionnelle.', '0387399200',
        'contact@arsenal-metz.fr', 'https://www.citemusicale-metz.fr', 1, '3 Avenue Ney', 'Metz', '57000', 'France',
        NOW(), NOW(), 'logo_arsenal.webp',
        'cover_arsenal.webp'),
       (2, 'La BAM (Boîte à Musiques)',
        'Salle de musiques actuelles moderne, avec studios de répétition et une programmation éclectique.',
        '0387393470', 'contact@bam-metz.fr', 'https://www.citemusicale-metz.fr/la-bam', 1, '20 Boulevard d''Alsace',
        'Metz', '57070', 'France', NOW(), NOW(), 'logo_bam.webp',
        'cover_bam.webp'),
       (3, 'Opéra-Théâtre de Metz',
        'Le plus ancien opéra-théâtre en activité en France, proposant des productions lyriques et théâtrales.',
        '0387156060', 'billetterie@opera.metzmetropole.fr', 'https://opera.eurometropolemetz.eu', 1,
        '4-5 Place de la Comédie', 'Metz', '57000', 'France', NOW(), NOW(),
        'logo_opera.webp', 'cover_opera.webp'),
       (4, 'Stade Saint-Symphorien', 'Principal stade de football de la ville, accueillant les matchs du FC Metz.',
        '0387667215', 'contact@fcmetz.com', 'https://www.fcmetz.com', 1, '3 Allée Saint-Symphorien',
        'Longeville-lès-Metz', '57050', 'France', NOW(), NOW(), 'logo_fcmetz.webp',
        'cover_fcmetz.webp'),
       (5, 'Parc des Expositions de Metz Métropole',
        'Vaste complexe pour foires, salons professionnels et expositions de grande envergure.', '0387556600',
        'info@metz-expo.com', 'https://www.metz-expo.com', 1, 'Rue de la Grange aux Bois', 'Metz', '57070', 'France',
        NOW(), NOW(), 'logo_parcexpo.webp',
        'cover_parcexpo.webp'),
       (6, 'Les Trinitaires',
        'Lieu culturel historique situé dans un ancien couvent, avec un caveau jazz et une chapelle pour concerts.',
        '0387200303', 'contact@lestrinitaires.com', 'https://www.citemusicale-metz.fr/les-trinitaires', 1,
        '12 Rue des Trinitaires', 'Metz', '57000', 'France', NOW(), NOW(),
        'logo_trinitaires.webp',
        'cover_trinitaires.webp'),
       (7, 'Comédie de Metz', 'Théâtre dédié à l''humour et aux comédies, situé dans un quartier historique.',
        '0781511512', 'comediedemetz@gmail.com', 'https://www.comediedemetz.fr', 1, '1/3 Rue du Pont Saint-Marcel',
        'Metz', '57000', 'France', NOW(), NOW(), 'logo_comedie.webp',
        'cover_comedie.webp'),
       (8, 'Salle Braun', 'Théâtre intimiste proposant une programmation variée, notamment pour le jeune public.',
        '0668092756', 'directionsallebraun@gmail.com', 'https://sallebraun.com', 1, '18 Rue Mozart', 'Metz', '57000',
        'France', NOW(), NOW(), 'logo_braun.webp',
        'cover_braun.webp'),
       (9, 'Metz Congrès Robert Schuman',
        'Centre de congrès moderne face au Centre Pompidou, idéal pour les conventions et séminaires.', '0387556600',
        'congres@metz-evenements.com', 'https://www.metz-evenements.com', 1, '112 Rue aux Arènes', 'Metz', '57000',
        'France', NOW(), NOW(), 'logo_congres.webp',
        'cover_congres.webp'),
       (10, 'Centre Pompidou-Metz',
        'Musée d''art moderne et contemporain de renommée internationale, à l''architecture audacieuse.', '0387153939',
        'contact@centrepompidou-metz.fr', 'https://www.centrepompidou-metz.fr', 1, '1 Parvis des Droits-de-l''Homme',
        'Metz', '57000', 'France', NOW(), NOW(), 'logo_pompidou.webp',
        'cover_pompidou.webp'),
       (11, 'Les Arènes de Metz',
        'Palais omnisports polyvalent accueillant des événements sportifs majeurs et des concerts de grande ampleur.',
        '0387629360', 'contact@arenes-metz.com', 'https://www.arenes-metz.com', 1, '5 Avenue Louis le Débonnaire',
        'Metz', '57000', 'France', NOW(), NOW(), 'logo_arenes.webp',
        'cover_arenes.webp');

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
 'STRUCTURE_ADMINISTRATOR', 1, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', 'avatar_1.webp', 1),
(2, 'Baptiste', 'Dubois', 'baptiste.dubois@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 2, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', 'avatar_2.webp', 1),
(3, 'Chloé', 'Bernard', 'chloe.bernard@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 3, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', 'avatar_3.webp', 1),
(4, 'Damien', 'Robert', 'damien.robert@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 4, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', 'avatar_4.webp', 1),
(5, 'Élise', 'Moreau', 'elise.moreau@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 5, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', 'avatar_5.webp', 1),
(6, 'François', 'Petit', 'francois.petit@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 6, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', 'avatar_6.webp', 1),
(7, 'Gabrielle', 'Laurent', 'gabrielle.laurent@tickly.dev',
 '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii', 'STRUCTURE_ADMINISTRATOR', 7, NOW(), NOW(),
 'STRUCTURE_ADMINISTRATOR',
 'avatar_7.webp', 1),
(8, 'Hugo', 'Simon', 'hugo.simon@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 11, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', 'avatar_8.webp', 1),
-- Spectateurs
(9, 'Inès', 'Michel', 'ines.michel@email.com', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'SPECTATOR', NULL, NOW(), NOW(), 'SPECTATOR', 'avatar_9.webp', 1),
(10, 'Julien', 'Garcia', 'julien.garcia@email.com', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'SPECTATOR', NULL, NOW(), NOW(), 'SPECTATOR', 'avatar_10.webp', 1),
(11, 'Karine', 'Lefebvre', 'karine.lefebvre@email.com', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'SPECTATOR', NULL, NOW(), NOW(), 'SPECTATOR', 'avatar_11.webp', 1),
(12, 'Léo', 'Roux', 'leo.roux@email.com', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii', 'SPECTATOR',
 NULL, NOW(), NOW(), 'SPECTATOR', 'avatar_12.webp', 1),
(13, 'Alice', 'Martin', 'a@a.com', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 1, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', '', 1);


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
VALUES (1, 'arsenal_1.webp'),
       (1, 'arsenal_2.webp'),
       (2, 'bam_1.webp'),
       (4, 'stade_1.webp'),
       (4, 'stade_2.webp'),
       (10, 'pompidou_1.webp');

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
        1, 1, NOW(), NOW(), 'orchestre_metz.webp', '3 Avenue Ney', 'Metz', '57000', 'France'),

       (2, 'Festival Electronic Waves',
        'Trois jours de musique électronique avec les meilleurs DJs internationaux.',
        'Electronic Waves revient pour sa 8ème édition avec une programmation exceptionnelle. Découvrez les sonorités les plus avant-gardistes de la scène électronique internationale dans l''ambiance unique de la BAM. Trois scènes, plus de 20 artistes, et une expérience immersive garantie.',
        DATE_ADD(NOW(), INTERVAL 45 DAY), DATE_ADD(NOW(), INTERVAL 47 DAY), 'PUBLISHED', 1, 1, 2, 2, NOW(), NOW(),
        'electronic_waves.webp', '20 Boulevard d''Alsace', 'Metz', '57070', 'France'),

       (3, 'La Traviata - Opéra de Verdi',
        'Production exceptionnelle de l''opéra le plus célèbre de Verdi.',
        'L''Opéra-Théâtre de Metz présente une nouvelle production de La Traviata dans une mise en scène contemporaine saisissante. Avec la soprano internationale Maria Dolores et le ténor français Jean-Baptiste Millot. Direction musicale : Maestro Antonio Benedetti.',
        DATE_ADD(NOW(), INTERVAL 60 DAY), DATE_ADD(NOW(), INTERVAL 60 DAY) + INTERVAL 3 HOUR, 'PUBLISHED', 1, 1,
        3, 3, NOW(), NOW(), 'traviata.webp', '4-5 Place de la Comédie', 'Metz', '57000', 'France'),

       (4, 'FC Metz vs Olympique Lyonnais',
        'Match de Ligue 1 au Stade Saint-Symphorien.',
        'Venez encourager les Grenats lors de ce match crucial de Ligue 1 face à l''Olympique Lyonnais. Ambiance garantie dans le chaudron messin ! Billets disponibles pour toutes les tribunes. Ouverture des portes 1h30 avant le coup d''envoi.',
        DATE_ADD(NOW(), INTERVAL 25 DAY), DATE_ADD(NOW(), INTERVAL 25 DAY) + INTERVAL 2 HOUR, 'PUBLISHED', 1, 0,
        4, 4, NOW(), NOW(), 'fcmetz_lyon.webp', '3 Allée Saint-Symphorien', 'Longeville-lès-Metz', '57050', 'France'),

       (5, 'Salon Habitat & Jardin',
        'Le salon de référence pour l''habitat et le jardinage en Lorraine.',
        'Découvrez les dernières tendances en matière d''habitat, de décoration et de jardinage. Plus de 200 exposants, des démonstrations, des conférences thématiques et de nombreux conseils d''experts. Idéal pour vos projets de rénovation et d''aménagement.',
        DATE_ADD(NOW(), INTERVAL 40 DAY), DATE_ADD(NOW(), INTERVAL 43 DAY), 'PUBLISHED', 0, 0, 5, 5, NOW(), NOW(),
        'salon_habitat.webp', 'Rue de la Grange aux Bois', 'Metz', '57070', 'France'),

       (6, 'Jazz Session - Les Trinitaires',
        'Soirée jazz intime dans le caveau historique.',
        'Plongez dans l''atmosphère feutrée du caveau des Trinitaires pour une soirée jazz exceptionnelle. Le quartet de Sarah Mitchell vous transportera dans l''univers du jazz moderne avec des reprises revisitées et des compositions originales.',
        DATE_ADD(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 15 DAY) + INTERVAL 2 HOUR, 'PUBLISHED', 1, 0,
        6, 6, NOW(), NOW(), 'jazz_trinitaires.webp', '12 Rue des Trinitaires', 'Metz', '57000', 'France'),

       (7, 'Jamel Comedy Club - Tournée',
        'Les humoristes du Jamel Comedy Club en spectacle.',
        'Retrouvez les talents du Jamel Comedy Club pour une soirée d''humour inoubliable. Au programme : Yacine Belhousse, Sofia Aram et Ahmed Sylla dans leurs derniers spectacles. Rires garantis dans l''intimité de la Comédie de Metz.',
        DATE_ADD(NOW(), INTERVAL 20 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY) + INTERVAL 2 HOUR, 'PUBLISHED', 1, 1,
        7, 7, NOW(), NOW(), 'comedy_club.webp', '1/3 Rue du Pont Saint-Marcel', 'Metz', '57000', 'France'),

       (8, 'Congrès International de Cybersécurité',
        'Trois jours dédiés aux enjeux de la cybersécurité.',
        'Le plus grand événement cybersécurité de l''Est de la France. Conférences, ateliers, démonstrations et networking avec les experts du secteur. Plus de 50 intervenants internationaux et 1000 participants attendus.',
        DATE_ADD(NOW(), INTERVAL 80 DAY), DATE_ADD(NOW(), INTERVAL 82 DAY), 'PUBLISHED', 0, 0, 9, 2, NOW(), NOW(),
        'cybersec_congress.webp', '112 Rue aux Arènes', 'Metz', '57000', 'France'),

       (9, 'Exposition : "Art et Intelligence Artificielle"',
        'Découverte des nouvelles formes d''art générées par l''IA.',
        'Le Centre Pompidou-Metz explore les frontières entre art et technologie dans cette exposition révolutionnaire. Œuvres interactives, installations immersives et rencontres avec les artistes pionniers de l''art numérique.',
        DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), 'PUBLISHED', 1, 1, 10, 3, NOW(), NOW(),
        'expo_ia.webp', '1 Parvis des Droits-de-l''Homme', 'Metz', '57000', 'France'),

       (10, 'Concert Rap - Nekfeu',
        'Nekfeu en concert aux Arènes de Metz.',
        'L''un des rappeurs français les plus talentueux de sa génération se produit aux Arènes de Metz. Venez découvrir ses nouveaux titres dans une mise en scène spectaculaire avec un écran géant et des effets pyrotechniques.',
        DATE_ADD(NOW(), INTERVAL 35 DAY), DATE_ADD(NOW(), INTERVAL 35 DAY) + INTERVAL 3 HOUR, 'PUBLISHED', 1, 1,
        11, 8, NOW(), NOW(), 'nekfeu_concert.webp', '5 Avenue Louis le Débonnaire', 'Metz', '57000', 'France'),

       (11, 'Spectacle Familial - "Le Petit Prince"',
        'Adaptation théâtrale du chef-d''œuvre d''Antoine de Saint-Exupéry.',
        'Une mise en scène poétique et moderne du Petit Prince destinée à toute la famille. Avec des marionnettes, des projections et une bande sonore originale, ce spectacle enchantera petits et grands.',
        DATE_ADD(NOW(), INTERVAL 50 DAY), DATE_ADD(NOW(), INTERVAL 50 DAY) + INTERVAL 90 MINUTE, 'PUBLISHED', 1, 0,
        8, 1, NOW(), NOW(), 'petit_prince.webp', '18 Rue Mozart', 'Metz', '57000', 'France'),

       (12, 'Festival de Danse Contemporaine',
        'Trois jours de danse contemporaine avec des compagnies internationales.',
        'Le Festival Mouvements revient avec une programmation éclectique mêlant danse contemporaine, performance et arts numériques. 8 compagnies, 15 représentations et des masterclass ouvertes au public.',
        DATE_ADD(NOW(), INTERVAL 70 DAY), DATE_ADD(NOW(), INTERVAL 72 DAY), 'DRAFT', 0, 0, 1, 1, NOW(), NOW(),
        'festival_danse.webp', '3 Avenue Ney', 'Metz', '57000', 'France');

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
VALUES (3, 'fcmetz_1.webp'),
       (3, 'fcmetz_2.webp'),
       (6, 'pompidou_gallery_1.webp'),
       (8, 'popstar_1.webp'),
       (8, 'popstar_2.webp'),
       (8, 'popstar_3.webp');


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

-- ######################################################################
-- #                                                                    #
-- #        JEU DE DONNÉES DE TEST POUR LA STRUCTURE 'LES TRINITAIRES' (ID 6)      #
-- #                                                                    #
-- ######################################################################
-- Ce script ajoute des données complètes pour la structure avec l'ID 6.
-- Il est conçu pour être ajouté à la fin de votre fichier data.sql existant.

-- ###################################################################
-- # 1. UTILISATEURS SUPPLÉMENTAIRES POUR L'ÉQUIPE ET LES AMIS       #
-- ###################################################################
-- Création de 7 nouveaux utilisateurs qui formeront l'équipe des Trinitaires
-- et de quelques spectateurs supplémentaires pour les relations.
-- Le mot de passe est 'Tickly123!' ($2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii)

INSERT INTO users (id, first_name, last_name, email, password, role, structure_id, created_at, updated_at, user_type,
                   avatar_path, is_email_validated)
VALUES
-- Équipe des Trinitaires (l'admin François Petit avec l'ID 6 existe déjà)
(20, 'Hélène', 'Mercier', 'helene.mercier@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'STRUCTURE_ADMINISTRATOR', 6, NOW(), NOW(), 'STRUCTURE_ADMINISTRATOR', 'avatar_20.webp', 1),
(21, 'Isaac', 'Garnier', 'isaac.garnier@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'ORGANIZATION_SERVICE', 6, NOW(), NOW(), 'ORGANIZATION_SERVICE', 'avatar_21.webp', 1),
(22, 'Justine', 'Faure', 'justine.faure@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'ORGANIZATION_SERVICE', 6, NOW(), NOW(), 'ORGANIZATION_SERVICE', 'avatar_22.webp', 1),
(23, 'Kévin', 'Roussel', 'kevin.roussel@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'RESERVATION_SERVICE', 6, NOW(), NOW(), 'RESERVATION_SERVICE', 'avatar_23.webp', 1),
(24, 'Léa', 'Blanc', 'lea.blanc@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'RESERVATION_SERVICE', 6, NOW(), NOW(), 'RESERVATION_SERVICE', 'avatar_24.webp', 1),
(25, 'Marc', 'Guérin', 'marc.guerin@tickly.dev', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'RESERVATION_SERVICE', 6, NOW(), NOW(), 'RESERVATION_SERVICE', 'avatar_25.webp', 1),
-- Spectateurs supplémentaires pour les billets et amitiés
(26, 'Nadia', 'Henry', 'nadia.henry@email.com', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'SPECTATOR', NULL, NOW(), NOW(), 'SPECTATOR', 'avatar_26.webp', 1),
(27, 'Olivier', 'Perrin', 'olivier.perrin@email.com', '$2a$10$5D.wJYGi0g79PajRmSwhG.URJPss/OelTTxPcIpyAQ0ZFdg/WKKii',
 'SPECTATOR', NULL, NOW(), NOW(), 'SPECTATOR', 'avatar_27.webp', 1);


-- ###################################################################
-- # 2. ÉQUIPE ET MEMBRES POUR 'LES TRINITAIRES' (ID 6)              #
-- ###################################################################

-- Création de l'équipe pour la structure 6
INSERT INTO teams (id, structure_id, name)
VALUES (6, 6, 'Équipe Les Trinitaires');

-- Ajout des 7 membres à l'équipe
INSERT INTO team_members (id, team_id, user_id, email, role, status, joined_at)
VALUES
-- L'admin principal (François Petit, ID 6) est déjà dans la structure, on l'ajoute à l'équipe
(10, 6, 6, 'francois.petit@tickly.dev', 'STRUCTURE_ADMINISTRATOR', 'ACTIVE', NOW() - INTERVAL 100 DAY),
-- Les 6 nouveaux membres
(11, 6, 20, 'helene.mercier@tickly.dev', 'STRUCTURE_ADMINISTRATOR', 'ACTIVE', NOW() - INTERVAL 90 DAY),
(12, 6, 21, 'isaac.garnier@tickly.dev', 'ORGANIZATION_SERVICE', 'ACTIVE', NOW() - INTERVAL 85 DAY),
(13, 6, 22, 'justine.faure@tickly.dev', 'ORGANIZATION_SERVICE', 'ACTIVE', NOW() - INTERVAL 85 DAY),
(14, 6, 23, 'kevin.roussel@tickly.dev', 'RESERVATION_SERVICE', 'ACTIVE', NOW() - INTERVAL 80 DAY),
(15, 6, 24, 'lea.blanc@tickly.dev', 'RESERVATION_SERVICE', 'ACTIVE', NOW() - INTERVAL 75 DAY),
(16, 6, 25, 'marc.guerin@tickly.dev', 'RESERVATION_SERVICE', 'ACTIVE', NOW() - INTERVAL 75 DAY);


-- ###################################################################
-- # 3. AMITIÉS ENTRE LES MEMBRES DE L'ÉQUIPE                        #
-- ###################################################################

INSERT INTO friendships (id, sender_id, receiver_id, status, created_at, updated_at)
VALUES
-- Amitiés acceptées
(10, 6, 20, 'ACCEPTED', NOW() - INTERVAL 50 DAY, NOW() - INTERVAL 49 DAY),  -- François et Hélène
(11, 21, 22, 'ACCEPTED', NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 38 DAY), -- Isaac et Justine
(12, 23, 24, 'ACCEPTED', NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 29 DAY), -- Kévin et Léa
-- Demandes en attente
(13, 20, 21, 'PENDING', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),    -- Hélène a demandé Isaac en ami
(14, 25, 6, 'PENDING', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY);
-- Marc a demandé François en ami


-- ###################################################################
-- # 4. ÉVÉNEMENTS POUR 'LES TRINITAIRES' (ID 6)                     #
-- ###################################################################
-- 10 nouveaux événements, dont 2 (ID 20 et 21) avec des données riches pour les statistiques.

INSERT INTO events (id, name, short_description, full_description, start_date, end_date, status, display_on_homepage,
                    is_featured_event, structure_id, creator_id, created_at, updated_at, main_photo_path, street, city,
                    zip_code, country)
VALUES
-- Événement 20 : Pour les statistiques de remplissage et de statuts
(20, 'Nuit du Blues & Soul', 'Une nuit entière dédiée aux légendes du blues et de la soul.',
 'Le Caveau des Trinitaires se transforme en club de blues de Chicago. Retrouvez des artistes locaux et internationaux pour des reprises endiablées et des compositions originales. Une soirée authentique et pleine d''émotion.',
 DATE_ADD(NOW(), INTERVAL 14 DAY), DATE_ADD(NOW(), INTERVAL 14 DAY) + INTERVAL 4 HOUR, 'PUBLISHED', 1, 1, 6, 6,
 NOW() - INTERVAL 30 DAY, NOW(), 'nuit_blues.webp', '12 Rue des Trinitaires', 'Metz', '57000', 'France'),

-- Événement 21 : Pour les statistiques d'évolution des réservations
(21, 'Scène Ouverte Poésie & Slam', 'Donnez de la voix lors de notre scène ouverte mensuelle.',
 'Que vous soyez poète aguerri ou simple curieux, la scène de La Chapelle vous est ouverte. Venez partager vos textes, écouter ceux des autres dans une ambiance bienveillante et créative. Inscription sur place.',
 DATE_ADD(NOW(), INTERVAL 35 DAY), DATE_ADD(NOW(), INTERVAL 35 DAY) + INTERVAL 3 HOUR, 'PUBLISHED', 1, 0, 6, 20,
 NOW() - INTERVAL 25 DAY, NOW(), 'scene_poesie.webp', '12 Rue des Trinitaires', 'Metz', '57000', 'France'),

-- 8 autres événements
(22, 'Trio de Jazz Manouche', 'Hommage à Django Reinhardt.',
 'Un concert virtuose qui vous fera voyager dans le Paris des années 30.', DATE_ADD(NOW(), INTERVAL 40 DAY),
 DATE_ADD(NOW(), INTERVAL 40 DAY) + INTERVAL 2 HOUR, 'PUBLISHED', 1, 0, 6, 21, NOW(), NOW(), 'jazz_manouche.webp',
 '12 Rue des Trinitaires', 'Metz', '57000', 'France'),
(23, 'Théâtre d''Improvisation : Le Match', 'Deux équipes s''affrontent dans un match d''improvisation déjanté.',
 'Le public vote, les comédiens improvisent ! Une soirée unique et hilarante.', DATE_ADD(NOW(), INTERVAL 48 DAY),
 DATE_ADD(NOW(), INTERVAL 48 DAY) + INTERVAL 2 HOUR, 'PUBLISHED', 0, 0, 6, 22, NOW(), NOW(), 'theatre_impro.webp',
 '12 Rue des Trinitaires', 'Metz', '57000', 'France'),
(24, 'Exposition Photos : "Regards sur Metz"', 'Le travail de 10 photographes locaux sur la ville de Metz.',
 'Une vision plurielle et poétique de la ville, capturée par des talents de la région.',
 DATE_ADD(NOW(), INTERVAL 55 DAY), DATE_ADD(NOW(), INTERVAL 85 DAY), 'PUBLISHED', 1, 0, 6, 6, NOW(), NOW(),
 'expo_metz.webp', '12 Rue des Trinitaires', 'Metz', '57000', 'France'),
(25, 'Concert Folk : The Wandering Souls', 'Un duo folk acoustique pour une soirée intimiste.',
 'Des mélodies envoûtantes et des textes poignants par ce duo montant de la scène folk.',
 DATE_ADD(NOW(), INTERVAL 62 DAY), DATE_ADD(NOW(), INTERVAL 62 DAY) + INTERVAL 2 HOUR, 'PUBLISHED', 1, 1, 6, 20, NOW(),
 NOW(), 'folk_concert.webp', '12 Rue des Trinitaires', 'Metz', '57000', 'France'),
(26, 'Conférence : L''Histoire des Templiers', 'Par l''historien Jean-Marc Durand.',
 'Découvrez les mythes et réalités de l''ordre des Templiers, de leur création à leur chute.',
 DATE_ADD(NOW(), INTERVAL 70 DAY), DATE_ADD(NOW(), INTERVAL 70 DAY) + INTERVAL 90 MINUTE, 'PUBLISHED', 0, 0, 6, 21,
 NOW(), NOW(), 'conf_templiers.webp', '12 Rue des Trinitaires', 'Metz', '57000', 'France'),
(27, 'Spectacle de Danse : "Origines"', 'Une performance de danse contemporaine explorant nos racines.',
 'La compagnie "Corps en Mouvement" présente sa dernière création, un voyage chorégraphique puissant.',
 DATE_ADD(NOW(), INTERVAL 78 DAY), DATE_ADD(NOW(), INTERVAL 78 DAY) + INTERVAL 80 MINUTE, 'PUBLISHED', 0, 0, 6, 22,
 NOW(), NOW(), 'danse_origines.webp', '12 Rue des Trinitaires', 'Metz', '57000', 'France'),
(28, 'Concert Rock Indé : "The Fuzz"', 'Le groupe de rock garage "The Fuzz" en concert unique.',
 'Énergie brute et guitares saturées pour les amateurs de rock sans concession.', DATE_ADD(NOW(), INTERVAL 85 DAY),
 DATE_ADD(NOW(), INTERVAL 85 DAY) + INTERVAL 2 HOUR, 'DRAFT', 0, 0, 6, 6, NOW(), NOW(), 'rock_fuzz.webp',
 '12 Rue des Trinitaires', 'Metz', '57000', 'France'),
(29, 'Ciné-Concert : "Metropolis"', 'Projection du chef-d''oeuvre de Fritz Lang avec musique live.',
 'Redécouvrez ce classique du cinéma muet accompagné au piano par l''artiste international Marc Vella.',
 DATE_ADD(NOW(), INTERVAL 92 DAY), DATE_ADD(NOW(), INTERVAL 92 DAY) + INTERVAL 3 HOUR, 'PUBLISHED', 1, 0, 6, 20, NOW(),
 NOW(), 'cine_metropolis.webp', '12 Rue des Trinitaires', 'Metz', '57000', 'France');

-- Attribution des catégories aux nouveaux événements
INSERT INTO event_has_categories (event_id, category_id)
VALUES (20, 1),
       (20, 3), -- Nuit du Blues & Soul: Concert, Festival
       (21, 2),
       (21, 7), -- Scène Ouverte: Théâtre, Humour
       (22, 1), -- Trio de Jazz Manouche: Concert
       (23, 2),
       (23, 7), -- Théâtre d'Improvisation: Théâtre, Humour
       (24, 6), -- Exposition Photos: Exposition
       (25, 1), -- Concert Folk: Concert
       (26, 5), -- Conférence: Conférence
       (27, 9), -- Spectacle de Danse: Danse
       (28, 1), -- Concert Rock Indé: Concert
       (29, 1),
       (29, 10);
-- Ciné-Concert: Concert, Jeune public

-- Attribution des zones d'audience aux nouveaux événements
INSERT INTO event_audience_zone (id, event_id, template_id, allocated_capacity)
VALUES (30, 20, 18, 200), -- Nuit du Blues & Soul (Caveau)
       (31, 21, 17, 350), -- Scène Ouverte (Chapelle)
       (32, 22, 18, 180), -- Trio de Jazz (Caveau)
       (33, 23, 17, 300), -- Théâtre d'Impro (Chapelle)
       (34, 24, 17, 150), -- Expo Photos (Chapelle)
       (35, 25, 18, 150), -- Concert Folk (Caveau)
       (36, 26, 17, 250), -- Conférence (Chapelle)
       (37, 27, 17, 280), -- Danse (Chapelle)
       (38, 28, 18, 200), -- Rock Indé (Caveau)
       (39, 29, 17, 350);
-- Ciné-Concert (Chapelle)

-- ###################################################################
-- # 5. DONNÉES DE BILLETS POUR LES STATISTIQUES (VERSION CORRIGÉE)  #
-- ###################################################################
-- Objectif: Fournir un jeu de données riche et cohérent pour les statistiques de l'événement 20,
-- en simulant des réservations réalistes sur une période donnée.
-- L'événement 20 "Nuit du Blues & Soul" a une capacité allouée de 200 places (event_audience_zone_id = 30).
-- Toutes les réservations et tous les billets ci-dessous sont pour cet événement.

-- Suppression des données de réservation et de billetterie précédemment générées pour les événements 20 et 21
-- pour garantir un état propre avant la nouvelle insertion.
DELETE
FROM tickets
WHERE event_id IN (20, 21);
DELETE
FROM reservations
WHERE id >= 10;
ALTER TABLE reservations
    AUTO_INCREMENT = 10;


-- ######################################################################
-- # A. PEUPLEMENT POUR ÉVÉNEMENT 20 : "Nuit du Blues & Soul" (ID 20)   #
-- ######################################################################
-- Création de réservations détaillées pour simuler une montée en charge réaliste.

INSERT INTO reservations (id, user_id, reservation_date)
VALUES
-- Vague 1: Réservations initiales (peu après la mise en ligne de l'événement)
(10, 9, NOW() - INTERVAL 28 DAY),  -- Inès Michel (ID 9), 2 billets
(11, 26, NOW() - INTERVAL 25 DAY), -- Nadia Henry (ID 26), 4 billets
-- Vague 2: Intérêt croissant
(12, 10, NOW() - INTERVAL 20 DAY), -- Julien Garcia (ID 10), 3 billets (dont 1 annulé)
(13, 11, NOW() - INTERVAL 18 DAY), -- Karine Lefebvre (ID 11), 1 billet
(14, 27, NOW() - INTERVAL 15 DAY), -- Olivier Perrin (ID 27), 2 billets
-- Vague 3: Pic de réservations
(15, 12, NOW() - INTERVAL 10 DAY), -- Léo Roux (ID 12), 5 billets
(16, 9, NOW() - INTERVAL 9 DAY),   -- Inès Michel (ID 9), nouvelle réservation pour des amis, 3 billets
(17, 10, NOW() - INTERVAL 8 DAY),  -- Julien Garcia (ID 10), 2 billets annulés
(18, 26, NOW() - INTERVAL 7 DAY),  -- Nadia Henry (ID 26), 6 billets
-- Vague 4: Dernières places
(19, 11, NOW() - INTERVAL 3 DAY),  -- Karine Lefebvre (ID 11), 2 billets
(20, 27, NOW() - INTERVAL 1 DAY);
-- Olivier Perrin (ID 27), 1 billet

-- Création des billets associés à chaque réservation pour l'événement 20.
-- Total de billets VALIDES: 21
-- Total de billets ANNULÉS: 5
-- Taux de remplissage (valide / capacité): 21 / 200 = 10.5% (ceci est un exemple, nous allons en ajouter plus)

-- Let's generate a more significant number of tickets to be realistic.
-- Total: 155 Billet (140 VALIDES, 15 ANNULÉS) -> Taux de remplissage de 70%

-- Billets pour la Réservation 10 (Inès Michel, 2 billets)
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
VALUES (UUID_TO_BIN(UUID()), 20, 30, 9, 10, UUID(), 'Inès', 'Michel', 'ines.michel@email.com', 'VALID',
        (SELECT reservation_date FROM reservations WHERE id = 10)),
       (UUID_TO_BIN(UUID()), 20, 30, 9, 10, UUID(), 'Ami', 'Un', 'ami1@email.com', 'VALID',
        (SELECT reservation_date FROM reservations WHERE id = 10));

-- Billets pour la Réservation 11 (Nadia Henry, 4 billets)
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
WITH RECURSIVE a(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM a WHERE n < 5)
SELECT UUID_TO_BIN(UUID()),
       20,
       30,
       26,
       11,
       UUID(),
       'Participant',
       CONCAT('Nadia', n),
       'nadia.henry@email.com',
       'VALID',
       (SELECT reservation_date FROM reservations WHERE id = 11)
FROM a;

-- Billets pour la Réservation 12 (Julien Garcia, 2 valides, 1 annulé)
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
VALUES (UUID_TO_BIN(UUID()), 20, 30, 10, 12, UUID(), 'Julien', 'Garcia', 'julien.garcia@email.com', 'VALID',
        (SELECT reservation_date FROM reservations WHERE id = 12)),
       (UUID_TO_BIN(UUID()), 20, 30, 10, 12, UUID(), 'Ami', 'Deux', 'ami2@email.com', 'VALID',
        (SELECT reservation_date FROM reservations WHERE id = 12)),
       (UUID_TO_BIN(UUID()), 20, 30, 10, 12, UUID(), 'Ami', 'Trois', 'ami3@email.com', 'CANCELLED',
        (SELECT reservation_date FROM reservations WHERE id = 12));

-- Billets pour la Réservation 13 (Karine Lefebvre, 1 billet)
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
VALUES (UUID_TO_BIN(UUID()), 20, 30, 11, 13, UUID(), 'Karine', 'Lefebvre', 'karine.lefebvre@email.com', 'VALID',
        (SELECT reservation_date FROM reservations WHERE id = 13));

-- Billets pour la Réservation 14 (Olivier Perrin, 2 billets)
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
WITH RECURSIVE a(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM a WHERE n < 3)
SELECT UUID_TO_BIN(UUID()),
       20,
       30,
       27,
       14,
       UUID(),
       'Participant',
       CONCAT('Olivier', n),
       'olivier.perrin@email.com',
       'VALID',
       (SELECT reservation_date FROM reservations WHERE id = 14)
FROM a;

-- Billets pour la Réservation 15 (Léo Roux, 4 valides, 1 annulé)
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
VALUES (UUID_TO_BIN(UUID()), 20, 30, 12, 15, UUID(), 'Léo', 'Roux', 'leo.roux@email.com', 'VALID',
        (SELECT reservation_date FROM reservations WHERE id = 15)),
       (UUID_TO_BIN(UUID()), 20, 30, 12, 15, UUID(), 'Ami', 'Quatre', 'ami4@email.com', 'VALID',
        (SELECT reservation_date FROM reservations WHERE id = 15)),
       (UUID_TO_BIN(UUID()), 20, 30, 12, 15, UUID(), 'Ami', 'Cinq', 'ami5@email.com', 'VALID',
        (SELECT reservation_date FROM reservations WHERE id = 15)),
       (UUID_TO_BIN(UUID()), 20, 30, 12, 15, UUID(), 'Ami', 'Six', 'ami6@email.com', 'VALID',
        (SELECT reservation_date FROM reservations WHERE id = 15)),
       (UUID_TO_BIN(UUID()), 20, 30, 12, 15, UUID(), 'Ami', 'Sept', 'ami7@email.com', 'CANCELLED',
        (SELECT reservation_date FROM reservations WHERE id = 15));

-- Billets pour la Réservation 16 (Inès Michel, 3 billets)
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
WITH RECURSIVE a(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM a WHERE n < 4)
SELECT UUID_TO_BIN(UUID()),
       20,
       30,
       9,
       16,
       UUID(),
       'Invité',
       CONCAT('Ines', n),
       'ines.michel@email.com',
       'VALID',
       (SELECT reservation_date FROM reservations WHERE id = 16)
FROM a;

-- Billets pour la Réservation 17 (Julien Garcia, 2 billets annulés)
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
WITH RECURSIVE a(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM a WHERE n < 3)
SELECT UUID_TO_BIN(UUID()),
       20,
       30,
       10,
       17,
       UUID(),
       'Annulation',
       CONCAT('Julien', n),
       'julien.garcia@email.com',
       'CANCELLED',
       (SELECT reservation_date FROM reservations WHERE id = 17)
FROM a;

-- Insertion de 120 billets 'VALID' supplémentaires pour atteindre un remplissage élevé
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
WITH RECURSIVE a(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM a WHERE n < 121)
SELECT UUID_TO_BIN(UUID()),
       20,
       30,
       26,
       18,
       UUID(),
       'Spectateur',
       CONCAT('Anonyme', n),
       'spectateur@test.com',
       'VALID',
       (SELECT reservation_date FROM reservations WHERE id = 18)
FROM a;

-- Insertion de 10 billets 'CANCELLED' supplémentaires
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
WITH RECURSIVE a(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM a WHERE n < 11)
SELECT UUID_TO_BIN(UUID()),
       20,
       30,
       11,
       19,
       UUID(),
       'Annulation',
       CONCAT('Tardive', n),
       'annulation@test.com',
       'CANCELLED',
       (SELECT reservation_date FROM reservations WHERE id = 19)
FROM a;

-- Total final pour l'événement 20 :
-- BILLETS VALIDES : 2 + 4 + 2 + 1 + 2 + 4 + 3 + 120 = 138
-- BILLETS ANNULÉS: 1 + 1 + 2 + 10 = 14
-- TOTAL BILLETS ÉMIS: 152
-- TAUX DE REMPLISSAGE: 138 / 200 = 69%


-- ###############################################################################
-- # B. PEUPLEMENT POUR ÉVÉNEMENT 21 : "Scène Ouverte Poésie & Slam" (ID 21)     #
-- ###############################################################################
-- L'objectif reste de tester l'évolution des réservations dans le temps.

INSERT INTO reservations (id, user_id, reservation_date)
VALUES (21, 9, NOW() - INTERVAL 24 DAY),
       (22, 10, NOW() - INTERVAL 20 DAY),
       (23, 11, NOW() - INTERVAL 15 DAY),
       (24, 12, NOW() - INTERVAL 10 DAY),
       (25, 26, NOW() - INTERVAL 5 DAY),
       (26, 27, NOW() - INTERVAL 2 DAY);

-- Jour 1 (-24d): 20 billets
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
WITH RECURSIVE a(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM a WHERE n < 21)
SELECT UUID_TO_BIN(UUID()),
       21,
       31,
       9,
       21,
       UUID(),
       'User',
       'D',
       'd@test.com',
       'VALID',
       (SELECT reservation_date FROM reservations WHERE id = 21)
FROM a;

-- Jour 2 (-20d): 30 billets
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
WITH RECURSIVE a(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM a WHERE n < 31)
SELECT UUID_TO_BIN(UUID()),
       21,
       31,
       10,
       22,
       UUID(),
       'User',
       'E',
       'e@test.com',
       'VALID',
       (SELECT reservation_date FROM reservations WHERE id = 22)
FROM a;

-- Jour 3 (-15d): 50 billets
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
WITH RECURSIVE a(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM a WHERE n < 51)
SELECT UUID_TO_BIN(UUID()),
       21,
       31,
       11,
       23,
       UUID(),
       'User',
       'F',
       'f@test.com',
       'VALID',
       (SELECT reservation_date FROM reservations WHERE id = 23)
FROM a;

-- Jour 4 (-10d): 40 billets
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
WITH RECURSIVE a(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM a WHERE n < 41)
SELECT UUID_TO_BIN(UUID()),
       21,
       31,
       12,
       24,
       UUID(),
       'User',
       'G',
       'g@test.com',
       'VALID',
       (SELECT reservation_date FROM reservations WHERE id = 24)
FROM a;

-- Jour 5 (-5d): 60 billets
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
WITH RECURSIVE a(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM a WHERE n < 61)
SELECT UUID_TO_BIN(UUID()),
       21,
       31,
       26,
       25,
       UUID(),
       'User',
       'H',
       'h@test.com',
       'VALID',
       (SELECT reservation_date FROM reservations WHERE id = 25)
FROM a;

-- Jour 6 (-2d): 25 billets
INSERT INTO tickets (id, event_id, event_audience_zone_id, user_id, reservation_id, qr_code_value,
                     participant_first_name, participant_last_name, participant_email, status, reservation_date)
WITH RECURSIVE a(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM a WHERE n < 26)
SELECT UUID_TO_BIN(UUID()),
       21,
       31,
       27,
       26,
       UUID(),
       'User',
       'I',
       'i@test.com',
       'VALID',
       (SELECT reservation_date FROM reservations WHERE id = 26)
FROM a;


-- Réactivation des contraintes de clés étrangères
SET FOREIGN_KEY_CHECKS = 1;

-- ##################################################
-- #          FIN DU SCRIPT DE PEUPLEMENT           #
-- ##################################################