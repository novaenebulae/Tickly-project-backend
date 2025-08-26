-- Création de la table `users`
-- Ajout d'une contrainte UNIQUE sur l'email pour s'assurer qu'il n'y a pas de doublons.
-- Le mot de passe n'est pas stocké en clair. C'est le résultat du hachage BCrypt.
-- Il n'est pas possible de le déchiffrer.
CREATE TABLE IF NOT EXISTS users
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    email              VARCHAR(255)         NOT NULL,
    password           VARCHAR(255)         NOT NULL,
    first_name         VARCHAR(255)         NOT NULL,
    last_name          VARCHAR(255)         NOT NULL,
    avatar_path        VARCHAR(255)         NULL,
    is_email_validated TINYINT(1) DEFAULT 0 NOT NULL,
    consent_given_at   DATETIME(6)          NOT NULL,
    created_at         DATETIME(6)          NOT NULL,
    updated_at         DATETIME(6)          NOT NULL,
    CONSTRAINT UK_users_email UNIQUE (email)
);

-- Création de la table `reservations`
CREATE TABLE IF NOT EXISTS reservations
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT      NOT NULL,
    reservation_date DATETIME(6) NOT NULL,

    -- Définition de la contrainte de clé étrangère
    -- Elle lie chaque réservation à un utilisateur.
    CONSTRAINT FK_reservations_users_user_id
        FOREIGN KEY (user_id) REFERENCES users (id)
)
