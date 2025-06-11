# Utiliser l'image Nginx officielle comme base.
# Il est recommandé d'utiliser une version spécifique pour la reproductibilité (ex: nginx:1.25-alpine).
# 'nginx:latest' prendra la dernière version stable.
# L'image 'alpine' est une version plus légère, souvent préférée en production.
FROM nginx:1.27.5-alpine
# Commentaire: Choisir une version spécifique (e.g., nginx:1.25 ou nginx:1.25-alpine)
# est une bonne pratique pour éviter les mises à jour inattendues qui pourraient casser la configuration.

# Optionnel : Supprimer la configuration Nginx par défaut.
# Si votre fichier de configuration personnalisé s'appelle 'default.conf',
# il remplacera automatiquement celui existant dans '/etc/nginx/conf.d/default.conf'.
# Si votre fichier a un autre nom (ex: 'my-app.conf'), vous pourriez vouloir supprimer
# le 'default.conf' pour éviter les conflits ou les comportements inattendus.
# RUN rm /etc/nginx/conf.d/default.conf

# Copier le fichier de configuration personnalisé (default.conf de l'hôte)
# dans le répertoire de configuration standard de Nginx à l'intérieur du conteneur.
# Nginx charge automatiquement les fichiers.conf présents dans ce répertoire.
COPY default.conf /etc/nginx/conf.d/default.conf
# Commentaire: 'default.conf' est le nom de notre fichier de configuration Nginx personnalisé
# situé dans le même répertoire que ce Dockerfile (le contexte de build).

# Exposer le port 80 (HTTP) et/ou 443 (HTTPS si vous configurez SSL).
# Ceci est une documentation pour Docker ; la publication réelle du port
# se fait via l'option -p de 'docker run' ou dans la section 'ports' de docker-compose.yml.
EXPOSE 80
# Commentaire: Indique que le service Nginx dans ce conteneur écoutera sur le port 80.

# La commande par défaut de l'image Nginx de base est de démarrer Nginx en mode avant-plan.
# Il n'est généralement pas nécessaire de spécifier CMD ou ENTRYPOINT ici,
# sauf si vous voulez modifier le comportement de démarrage par défaut de Nginx.
# La commande CMD ["nginx", "-g", "daemon off;"] est typiquement héritée de l'image de base.