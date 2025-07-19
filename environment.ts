/**
 * Environnement de l'application
 * Ces variables peuvent être remplacées lors du build pour différents environnements
 */
export const environment = {
  // Indique si l'application est en production ou développement
  production: false,

  // Version de l'application
  version: '0.1.0',

  // URL de base de l'API REST
  apiUrl: '',

  // Activation des logs de débogage
  enableDebugLogs: true,

  // Activation des mocks (simulation de l'API)
  useMocks: false,

  // Délai artificiel pour les requêtes mockées (ms)
  mockDelay: 500,

  // Configuration des features flags
  features: {
    enableUserRegistration: true,
    enableEventCreation: true,
    enableEventBooking: true,
    enableStructureCreation: true,
    enableSeatingMap: false
  },

  // Configuration Sentry pour la capture des erreurs (désactivé en développement)
  sentry: {
    enabled: false,
    dsn: ''
  }
};
