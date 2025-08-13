# Projet CDA - Tickly [![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/novaenebulae/Tickly-project-backend)

Ce projet vise à développer une application web pour la gestion de billetterie de structures culturelles. Il combine une
interface utilisateur en Angular pour le frontend et une API en Java Spring pour le backend.
Les structures peuvent s'inscrire au service afin d'ajouter leurs événements.
Le public peut réserver les billets au sein de ces structures, générant un flashcode pour le controle d'accès.

## Architecture du système

L'application utilise une architecture microservices containerisée avec Docker Compose :

- **Reverse Proxy** : Nginx pour la gestion des requêtes et le service des fichiers statiques
- **Backend API** : Application Spring Boot exposant l'API REST
- **Base de données** : MySQL 8.0 pour la persistance des données
- **Frontend** : Application Angular (séparée de ce repository)

## Stack technique

### Backend

- **Java 21** - Langage de programmation principal
- **Spring Boot 3.3.1** - Framework principal
- **Spring Data JPA** - Couche de persistance
- **Spring Security** - Authentification et autorisation
- **Spring WebSocket** - Communication temps réel
- **JWT (jsonwebtoken 0.12.3)** - Gestion des tokens d'authentification
- **MySQL 8.0** - Base de données relationnelle
- **Lombok 1.18.30** - Réduction du boilerplate code
- **MapStruct 1.5.5** - Mapping d'objets
- **SpringDoc OpenAPI 2.5.0** - Documentation API automatique
- **Maven** - Gestionnaire de dépendances et build

### Fonctionnalités avancées

- **Gmail API OAuth 2.0** - Envoi d'emails via Gmail
- **Thymeleaf** - Moteur de templates
- **Bean Validation** - Validation des données
- **Testcontainers** - Tests d'intégration avec Docker
- **WebSocket** - Notifications temps réel

### Infrastructure

- **Docker** - Containerisation
- **Docker Compose** - Orchestration multi-conteneurs
- **Nginx** - Reverse proxy et serveur de fichiers statiques

## Prérequis

- **Java 21** ou supérieur
- **Maven 3.6+**
- **Docker** et **Docker Compose**
- **Git**

## Configuration de l'environnement

### Variables d'environnement

Créez un fichier `.env` à la racine du projet avec les variables suivantes :

```
bash
# Configuration MySQL
MYSQL_ROOT_PASSWORD=votre_mot_de_passe_root MYSQL_DATABASE_NAME=tickly_db MYSQL_USER=tickly_user MYSQL_PASSWORD=votre_mot_de_passe_utilisateur
# Configuration JWT
JWT_SECRET=votre_secret_jwt_tres_long_et_securise JWT_EXPIRATION_S=900 JWT_REFRESH_EXPIRATION_S=2592000
# Configuration Gmail OAuth
GOOGLE_CLIENT_ID=votre_client_id_google GOOGLE_CLIENT_SECRET=votre_client_secret_google GOOGLE_REFRESH_TOKEN=votre_refresh_token_google
# Configuration Frontend
APP_FRONTEND_URL=[http://localhost:4200](http://localhost:4200)
``` 

### Configuration Gmail OAuth 2.0

1. Créez un projet dans la [Console Google Cloud](https://console.cloud.google.com/)
2. Activez l'API Gmail
3. Créez des identifiants OAuth 2.0
4. Configurez l'écran de consentement OAuth
5. Générez un refresh token pour l'adresse email `tickly.project@gmail.com`

## Lancement de l'application

### Méthode 1 : Docker Compose (Recommandée)

1. Clonez le repository :

```
bash 
git clone <url-du-repository> cd tickly-backend
``` 

2. Configurez le fichier `.env` avec vos variables d'environnement

3. Lancez l'application avec Docker Compose :

```
bash docker-compose up -d
``` 

4. Vérifiez que tous les services sont démarrés :

```
bash docker-compose ps
``` 

L'application sera accessible sur :

- API Backend : `http://localhost:80`
- Documentation API (Swagger) : `http://localhost:80/swagger-ui.html`
- Base de données MySQL : `localhost:3306`

### Méthode 2 : Développement local

1. Démarrez uniquement la base de données :

```bash
docker-compose up -d db
```

```
1. Compilez et lancez l'application Spring Boot :
``` bash
./mvnw clean compile
./mvnw spring-boot:run
```

L'application sera accessible sur `http://localhost:8080`

## Structure du projet

``` 
tickly-backend/
├── src/
│   ├── main/
│   │   ├── java/edu/cda/project/ticklybackend/
│   │   │   ├── controllers/     # Contrôleurs REST
│   │   │   ├── services/        # Logique métier
│   │   │   ├── repositories/    # Couche d'accès aux données
│   │   │   ├── models/          # Entités JPA
│   │   │   ├── mappers/         # MapStruct mappers
│   │   │   ├── config/          # Configuration Spring
│   │   │   └── security/        # Configuration sécurité
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── schema.sql       # Structure de la BDD
│   │       └── data.sql         # Données initiales
│   └── test/                    # Tests unitaires et d'intégration
├── proxy/                       # Configuration Nginx
├── docs/                        # Documentation projet
├── docker-compose.yaml          # Orchestration Docker
├── Dockerfile                   # Image Docker backend
└── pom.xml                      # Configuration Maven
```

## API Documentation

Une fois l'application démarrée, la documentation interactive de l'API est disponible via Swagger UI :

- URL : `http://localhost:8080/swagger-ui.html` (mode dev) ou `http://localhost:80/swagger-ui.html` (Docker)
- Spécification OpenAPI : `/api/v1/api-docs`

## Tests

Le projet utilise Testcontainers pour les tests d'intégration avec une base de données MySQL réelle :

## Gestion des fichiers

L'application gère l'upload de fichiers (images des événements, structures, etc.) :

- Répertoire de stockage : (dans le conteneur) `/app/uploads`
- Taille maximale par fichier : 10MB
- Taille maximale de requête : 50MB
- URL de base pour l'accès aux fichiers statiques : `/static`

## Sécurité

- Authentification basée sur JWT avec refresh tokens
- Validation des données d'entrée avec Bean Validation
- CORS configuré pour le frontend Angular
- Hashage sécurisé des mots de passe avec Spring Security
- Protection CSRF désactivée pour l'API REST
