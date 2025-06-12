package edu.cda.project.ticklybackend.repositories;

import edu.cda.project.ticklybackend.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Trouve un utilisateur par son email
    Optional<User> findByEmail(String email);

    // Vérifie si un utilisateur existe avec cet email
    boolean existsByEmail(String email);
}