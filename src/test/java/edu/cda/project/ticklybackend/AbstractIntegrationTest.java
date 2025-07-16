package edu.cda.project.ticklybackend;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    public static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            // On dit au conteneur de chercher les scripts d'init dans le classpath de test
            .withInitScript("init_test_db.sql");

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        // --- Propriétés pour la connexion à la base de données ---
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");

        // --- CORRECTION ---
        // On désactive complètement la génération de schéma par Hibernate.
        // On laisse les scripts SQL faire 100% du travail.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");

        // On s'assure que Spring n'essaie pas d'exécuter les scripts lui-même.
        // C'est Testcontainers qui va s'en charger avec `withInitScript`.
        registry.add("spring.sql.init.mode", () -> "never");

        // --- Propriétés pour la sécurité et autres ---
        registry.add("jwt.secret", () -> "ceci-est-un-secret-de-test-tres-long-et-securise-pour-eviter-les-erreurs");
        registry.add("jwt.expiration.access-token-ms", () -> "3600000");
        registry.add("file.upload-dir", () -> "target/uploads");
        registry.add("tickly.mail.gmail.client-id", () -> "dummy-google-client-id");
        registry.add("tickly.mail.gmail.client-secret", () -> "dummy-google-client-secret");
        registry.add("tickly.mail.gmail.refresh-token", () -> "dummy-google-refresh-token");
    }
}