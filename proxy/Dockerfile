FROM nginx:alpine

# Supprimer toutes les configurations par défaut
RUN rm -f /etc/nginx/conf.d/*

# Copier votre configuration personnalisée
COPY nginx.conf /etc/nginx/conf.d/tickly.conf

# Créer les répertoires nécessaires pour les fichiers statiques
RUN mkdir -p /var/www/html/static

# Exposer les ports
EXPOSE 80 443

# Démarrer Nginx avec validation de la configuration
CMD ["sh", "-c", "nginx -t && nginx -g 'daemon off;'"]