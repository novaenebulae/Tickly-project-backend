# --- Stage 1: Build ---
# Utiliser une image JDK complète (Eclipse Temurin JDK 21 sur Ubuntu Jammy) pour la compilation avec Maven.
# 'builder' est un alias pour cette étape de construction.
FROM eclipse-temurin:21-jdk-jammy AS builder
# Commentaire: Cette étape utilise un JDK complet nécessaire pour la compilation.

# Définir le répertoire de travail dans le conteneur pour cette étape.
WORKDIR /app
# Commentaire: Toutes les commandes suivantes (COPY, RUN) seront exécutées depuis ce répertoire.

# Copier les fichiers de description du projet Maven (pom.xml et le wrapper.mvn).
# Cette séparation permet de tirer parti du cache de couches Docker :
# si seul le code source change mais pas les dépendances (pom.xml),
# la couche de téléchargement des dépendances sera réutilisée.
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Commentaire: Copier d'abord les fichiers de build optimise la mise en cache des dépendances.

# Télécharger les dépendances du projet pour les mettre en cache.
# L'option '-B' (batch mode) évite les prompts interactifs de Maven.
RUN ./mvnw dependency:go-offline -B
# Commentaire: 'dependency:go-offline' télécharge toutes les dépendances listées dans pom.xml.

# Copier le reste du code source de l'application.
COPY src ./src
# Commentaire: Le code source est copié après le téléchargement des dépendances.

# Compiler l'application et la packager en un fichier JAR exécutable.
# '-DskipTests' permet d'ignorer l'exécution des tests unitaires pendant la construction de l'image Docker,
# ce qui accélère le build. Les tests devraient idéalement être exécutés dans une étape CI séparée.
RUN ./mvnw package -DskipTests
# Commentaire: La commande 'package' compile le code et crée le fichier JAR dans le répertoire 'target/'.

# --- Stage 2: Run ---
# Utiliser une image JRE (Java Runtime Environment) plus légère pour l'exécution.
# Eclipse Temurin JRE 21 sur Ubuntu Jammy est plus petite que l'image JDK complète.
FROM eclipse-temurin:21-jre-jammy
# Commentaire: Cette étape utilise une image JRE, optimisée pour l'exécution et plus sécurisée
# car elle ne contient pas les outils de compilation.

# Définir le répertoire de travail pour l'exécution.
WORKDIR /app

# Copier le fichier JAR construit depuis l'étape 'builder' (stage 1) vers l'image finale.
# Le JAR se trouve typiquement dans /app/target/nom-de-lapplication.jar.
# Nous le renommons 'app.jar' pour simplifier la commande ENTRYPOINT.
COPY --from=builder /app/target/*.jar app.jar
# Commentaire: Seul l'artefact compilé (le JAR) est copié dans l'image finale.

# Exposer le port sur lequel l'application Spring Boot tourne (défini par server.port dans application.properties, par défaut 8080).
# Ceci est une documentation ; la publication réelle du port se fait avec l'option -p de 'docker run' ou dans docker-compose.yml.
EXPOSE 8080
# Commentaire: Informe Docker que l'application dans le conteneur écoute sur ce port.

# Commande pour lancer l'application lorsque le conteneur démarre.
# ["java", "-jar", "app.jar"] est la manière standard de lancer un JAR Spring Boot.
ENTRYPOINT ["java", "-jar", "app.jar"]
# Commentaire: ENTRYPOINT configure le conteneur pour s'exécuter comme un exécutable.
# L'application démarrera dès que le conteneur sera lancé.