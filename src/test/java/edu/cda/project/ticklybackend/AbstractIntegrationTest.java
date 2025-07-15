// src/test/java/edu/cda/project/ticklybackend/AbstractIntegrationTest.java
package edu.cda.project.ticklybackend;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractIntegrationTest {

    // Crée un conteneur MySQL qui sera partagé entre toutes les classes de test qui héritent de celle-ci.
    // 'mysql:8.0' spécifie l'image Docker à utiliser.
    @Container
    public static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    // Cette méthode magique intercepte le démarrage de Spring et injecte dynamiquement
    // les propriétés de connexion (host, port, etc.) du conteneur qui vient de démarrer.
    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("jwt.secret", () -> "O1axdTNTzgrKfQKzrgzgSgQj71F3IIzdzdzdve5glsnbXwkVCKn0kzgzgcdn5209zrgzrg2602");
        registry.add("jwt.expiration.access-token-ms", () -> "3600000");
        registry.add("file.upload-dir", () -> "target/uploads");
    }
}